from __future__ import annotations

from collections import deque
from pathlib import Path
import shutil

import numpy as np
from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
SOURCE = Path(r"C:\Users\PHAMDU~1\AppData\Local\Temp\codex-clipboard-035f4307-4ed2-4127-a0f2-554b2a4aeaac.png")
ASSET_DIR = ROOT / "data/unified/liet-hoa-mutations/LH-064-EVO-01"
BUILD_DIR = ROOT / "build"

# Bounds were measured on the original 1344x896 concept sheet. Each tuple is
# one isolated character pose; extracting them separately guarantees that a
# neighboring animation frame can never leak into the current one.
IDLE_BOUNDS = [
    (27, 345, 142, 546),
    (159, 348, 272, 546),
    (277, 348, 390, 545),
    (404, 345, 519, 546),
]
ACTION_BOUNDS = [
    (585, 357, 708, 543),
    (721, 390, 844, 540),
    (831, 354, 1006, 542),
    (1009, 366, 1206, 542),
    (1193, 357, 1315, 544),
]


def flood_white_background(rgb: np.ndarray) -> np.ndarray:
    """Build an antialiased alpha matte without deleting enclosed white detail."""
    height, width, _ = rgb.shape
    chroma = rgb.max(axis=2).astype(np.int16) - rgb.min(axis=2).astype(np.int16)
    luminance = rgb.mean(axis=2)
    # The concept sheet contains a soft neutral drop-shadow around every pose,
    # not just pure white. Treat only border-connected neutral pixels as matte;
    # enclosed whites in eyes, flames and costume remain opaque.
    candidate = (luminance >= 148) & (chroma <= 62)

    outside = np.zeros((height, width), dtype=bool)
    queue: deque[tuple[int, int]] = deque()
    for x in range(width):
        if candidate[0, x]:
            outside[0, x] = True
            queue.append((x, 0))
        if candidate[height - 1, x] and not outside[height - 1, x]:
            outside[height - 1, x] = True
            queue.append((x, height - 1))
    for y in range(height):
        if candidate[y, 0] and not outside[y, 0]:
            outside[y, 0] = True
            queue.append((0, y))
        if candidate[y, width - 1] and not outside[y, width - 1]:
            outside[y, width - 1] = True
            queue.append((width - 1, y))
    while queue:
        x, y = queue.popleft()
        for nx, ny in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)):
            if 0 <= nx < width and 0 <= ny < height and candidate[ny, nx] and not outside[ny, nx]:
                outside[ny, nx] = True
                queue.append((nx, ny))

    alpha = np.full((height, width), 255, dtype=np.uint8)
    # The final 2:1 premultiplied downsample supplies a clean one-pixel
    # antialiased edge, so keeping any of this old neutral matte is harmful.
    alpha[outside] = 0
    return alpha


def remove_border_components(rgba: np.ndarray) -> np.ndarray:
    """Drop fragments belonging to adjacent poses that enter a crop edge."""
    alpha = rgba[:, :, 3]
    height, width = alpha.shape
    foreground = alpha > 16
    seen = np.zeros((height, width), dtype=bool)
    for start_y in range(height):
        for start_x in range(width):
            if not foreground[start_y, start_x] or seen[start_y, start_x]:
                continue
            queue = deque([(start_x, start_y)])
            seen[start_y, start_x] = True
            points = []
            touches_edge = False
            while queue:
                x, y = queue.popleft()
                points.append((x, y))
                touches_edge |= x == 0 or y == 0 or x == width - 1 or y == height - 1
                for nx, ny in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)):
                    if 0 <= nx < width and 0 <= ny < height and foreground[ny, nx] and not seen[ny, nx]:
                        seen[ny, nx] = True
                        queue.append((nx, ny))
            if touches_edge:
                for x, y in points:
                    rgba[y, x] = 0
    return rgba


def remove_white_matte(rgb: np.ndarray, alpha: np.ndarray) -> np.ndarray:
    """Recover foreground RGB from pixels previously composited on white."""
    out = rgb.astype(np.float32)
    partial = (alpha > 0) & (alpha < 250)
    a = alpha.astype(np.float32) / 255.0
    for channel in range(3):
        values = out[:, :, channel]
        recovered = 255.0 - (255.0 - values) / np.maximum(a, 1.0 / 255.0)
        values[partial] = np.clip(recovered[partial], 0, 255)
        out[:, :, channel] = values
    out[alpha == 0] = 0
    return out.astype(np.uint8)


def extract_pose(source: Image.Image, bounds: tuple[int, int, int, int]) -> Image.Image:
    left, top, right, bottom = bounds
    pad = 7
    crop = source.crop((left - pad, top - pad, right + pad, bottom + pad)).convert("RGB")
    rgb = np.asarray(crop, dtype=np.uint8)
    alpha = flood_white_background(rgb)
    clean_rgb = remove_white_matte(rgb, alpha)
    rgba = remove_border_components(np.dstack((clean_rgb, alpha)))
    image = Image.fromarray(rgba, "RGBA")
    bbox = image.getchannel("A").getbbox()
    if bbox is None:
        raise RuntimeError(f"Empty pose at {bounds}")
    return image.crop(bbox)


def premultiplied_resize(image: Image.Image, size: tuple[int, int]) -> Image.Image:
    array = np.asarray(image, dtype=np.float32)
    alpha = array[:, :, 3:4] / 255.0
    premult = np.concatenate((array[:, :, :3] * alpha, array[:, :, 3:4]), axis=2)
    channels = []
    for channel in range(4):
        plane = Image.fromarray(np.clip(premult[:, :, channel], 0, 255).astype(np.uint8), "L")
        channels.append(np.asarray(plane.resize(size, Image.Resampling.LANCZOS), dtype=np.float32))
    resized = np.stack(channels, axis=2)
    out_alpha = resized[:, :, 3:4]
    out_rgb = np.where(out_alpha > 0.5, resized[:, :, :3] * 255.0 / np.maximum(out_alpha, 0.5), 0)
    return Image.fromarray(np.concatenate((np.clip(out_rgb, 0, 255), np.clip(out_alpha, 0, 255)), axis=2).astype(np.uint8), "RGBA")


def torso_anchor(image: Image.Image) -> float:
    array = np.asarray(image)
    alpha = array[:, :, 3]
    height, width = alpha.shape
    yy, xx = np.indices(alpha.shape)
    # The chest/waist is stable across idle poses; hair, staff and feet are not.
    mask = (alpha >= 160) & (yy >= height * 0.42) & (yy <= height * 0.78)
    mask &= (xx >= width * 0.22) & (xx <= width * 0.78)
    if not mask.any():
        return width / 2.0
    weights = alpha[mask].astype(np.float64)
    return float(np.average(xx[mask], weights=weights))


def make_sheet(poses: list[Image.Image], cell_width: int, stabilize_idle: bool) -> tuple[Image.Image, list[Image.Image]]:
    sheet = Image.new("RGBA", (cell_width * len(poses), 110), (0, 0, 0, 0))
    frames = []
    prepared = []
    for pose in poses:
        scale = min(0.5, (cell_width - 4) / pose.width, 106 / pose.height)
        size = (max(1, round(pose.width * scale)), max(1, round(pose.height * scale)))
        prepared.append(premultiplied_resize(pose, size))

    reference_anchor = np.median([torso_anchor(frame) - frame.width / 2.0 for frame in prepared])
    for index, frame in enumerate(prepared):
        canvas = Image.new("RGBA", (cell_width, 110), (0, 0, 0, 0))
        if stabilize_idle:
            offset = torso_anchor(frame) - frame.width / 2.0
            x = round(cell_width / 2.0 - frame.width / 2.0 + reference_anchor - offset)
        else:
            x = round((cell_width - frame.width) / 2.0)
        y = 108 - frame.height
        canvas.alpha_composite(frame, (x, y))
        # Zero RGB under fully transparent pixels, preventing texture sampling
        # from pulling a pale edge into the next rendered frame.
        data = np.asarray(canvas).copy()
        data[data[:, :, 3] == 0, :3] = 0
        canvas = Image.fromarray(data, "RGBA")
        frames.append(canvas)
        sheet.alpha_composite(canvas, (index * cell_width, 0))
    return sheet, frames


def checkerboard(size: tuple[int, int], tile: int = 8) -> Image.Image:
    result = Image.new("RGBA", size, (0, 0, 0, 255))
    draw = ImageDraw.Draw(result)
    for y in range(0, size[1], tile):
        for x in range(0, size[0], tile):
            shade = 42 if (x // tile + y // tile) % 2 == 0 else 68
            draw.rectangle((x, y, x + tile - 1, y + tile - 1), fill=(shade, shade, shade, 255))
    return result


def proof_frame(frame: Image.Image, scale: int = 3) -> Image.Image:
    bg = checkerboard(frame.size)
    bg.alpha_composite(frame)
    return bg.resize((frame.width * scale, frame.height * scale), Image.Resampling.NEAREST).convert("P", palette=Image.Palette.ADAPTIVE)


def white_fringe_count(image: Image.Image) -> int:
    rgba = np.asarray(image)
    alpha = rgba[:, :, 3]
    near_white = (rgba[:, :, :3].min(axis=2) > 238) & (rgba[:, :, :3].max(axis=2) - rgba[:, :, :3].min(axis=2) < 10)
    return int(np.count_nonzero(near_white & (alpha > 0) & (alpha < 250)))


def main() -> None:
    if not SOURCE.exists():
        raise FileNotFoundError(SOURCE)
    ASSET_DIR.mkdir(parents=True, exist_ok=True)
    BUILD_DIR.mkdir(parents=True, exist_ok=True)
    for name in ("idle.png", "action.png"):
        current = ASSET_DIR / name
        backup = BUILD_DIR / name.replace(".png", "-before-hoa-diem-cleanup.png")
        if current.exists() and not backup.exists():
            shutil.copy2(current, backup)

    source = Image.open(SOURCE).convert("RGB")
    idle_poses = [extract_pose(source, bounds) for bounds in IDLE_BOUNDS]
    action_poses = [extract_pose(source, bounds) for bounds in ACTION_BOUNDS]
    idle_sheet, idle_frames = make_sheet(idle_poses, 75, True)
    action_sheet, action_frames = make_sheet(action_poses, 100, False)
    idle_sheet.save(ASSET_DIR / "idle.png", optimize=True)
    action_sheet.save(ASSET_DIR / "action.png", optimize=True)

    idle_proof = [proof_frame(frame) for frame in idle_frames]
    action_proof = [proof_frame(frame) for frame in action_frames]
    idle_proof[0].save(BUILD_DIR / "Hoa-Diem-Hau-Vuong-idle-proof.gif", save_all=True, append_images=idle_proof[1:] + idle_proof[-2:0:-1], duration=150, loop=0, disposal=2)
    action_proof[0].save(BUILD_DIR / "Hoa-Diem-Hau-Vuong-action-proof.gif", save_all=True, append_images=action_proof[1:], duration=[90, 90, 110, 110, 220], loop=0, disposal=2)

    contact = checkerboard((500, 220))
    contact.alpha_composite(idle_sheet, (100, 0))
    contact.alpha_composite(action_sheet, (0, 110))
    contact.resize((1000, 440), Image.Resampling.NEAREST).convert("RGB").save(BUILD_DIR / "Hoa-Diem-Hau-Vuong-cleanup-contact.png")
    print("idle", idle_sheet.size, "fringe", white_fringe_count(idle_sheet))
    print("action", action_sheet.size, "fringe", white_fringe_count(action_sheet))
    print("idle anchors", [round(torso_anchor(frame), 2) for frame in idle_frames])


if __name__ == "__main__":
    main()
