import csv
import hashlib
import json
import os
import re
import shutil
import struct
import zlib
from pathlib import Path


ROOT = Path("work/jar_extract")
OUT = Path("outputs")
AN = Path("work/analysis")
PNG_OUT = OUT / "decoded_assets" / "png"
JSON_OUT = OUT / "decoded_assets" / "json"


def ensure_dirs():
    for p in [OUT, AN, PNG_OUT, JSON_OUT]:
        p.mkdir(parents=True, exist_ok=True)


class Reader:
    def __init__(self, data):
        self.data = data
        self.pos = 0

    def remain(self):
        return len(self.data) - self.pos

    def read(self, n):
        if self.pos + n > len(self.data):
            raise EOFError(f"need {n}, remain {self.remain()}, pos {self.pos}")
        b = self.data[self.pos:self.pos + n]
        self.pos += n
        return b

    def u1(self):
        return self.read(1)[0]

    def i1(self):
        v = self.u1()
        return v - 256 if v >= 128 else v

    def u2(self):
        return struct.unpack(">H", self.read(2))[0]

    def i2(self):
        return struct.unpack(">h", self.read(2))[0]

    def i4(self):
        return struct.unpack(">i", self.read(4))[0]

    def skip(self, n):
        self.read(n)


def rel(path):
    return str(path.relative_to(ROOT)).replace("\\", "/")


def sha256(path):
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            h.update(chunk)
    return h.hexdigest()


def fix_text(s):
    if not isinstance(s, str):
        return s
    if not any(mark in s for mark in ("Ã", "Ä", "Â", "áº", "á»", "Æ")):
        return s
    try:
        fixed = s.encode("latin1").decode("utf-8")
        vietnamese_marks = "ăâđêôơưáàảãạấầẩẫậắằẳẵặéèẻẽẹếềểễệíìỉĩịóòỏõọốồổỗộớờởỡợúùủũụứừửữựýỳỷỹỵ"
        if any(ch in fixed for ch in vietnamese_marks):
            return fixed
    except Exception:
        pass
    return s


def classify(path, data):
    r = rel(path)
    if data.startswith(b"\x89PNG\r\n\x1a\n"):
        return "png_image"
    if data.startswith(b"MThd"):
        return "midi_audio"
    if r.endswith(".ui"):
        return "ui_layout_binary"
    if data[:1] == b"\x1b" and r.endswith(".mib"):
        return "vm_bytecode_script"
    if r.startswith("data/event/"):
        return "event_scene_binary"
    if r.startswith("data/map/"):
        return "map_binary"
    if r.startswith("data/script/"):
        return "script_table_binary"
    if r.startswith("data/mod/"):
        return "module_tileset_binary"
    if r.startswith("data/tex/"):
        return "png_chunk_container_or_raw_png"
    if r.startswith("data/img/"):
        return "png_image_disguised_as_mid"
    if r.startswith("data/spr/"):
        return "sprite_binary"
    if r.endswith(".class"):
        return "java_class"
    return "unknown_or_misc"


def inventory():
    rows = []
    for path in sorted(ROOT.rglob("*")):
        if not path.is_file():
            continue
        data = path.read_bytes()
        rows.append({
            "path": rel(path),
            "size": len(data),
            "ext": path.suffix,
            "magic_hex": data[:16].hex(" "),
            "kind": classify(path, data),
            "sha256": hashlib.sha256(data).hexdigest(),
        })
    with open(OUT / "file_inventory.csv", "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=rows[0].keys())
        w.writeheader()
        w.writerows(rows)
    return rows


def png_chunk(chunk_type, payload):
    return (
        struct.pack(">I", len(payload)) +
        chunk_type +
        payload +
        struct.pack(">I", zlib.crc32(chunk_type + payload) & 0xFFFFFFFF)
    )


def reconstruct_tex_png(data):
    r = Reader(data)
    width = r.i4()
    height = r.i4()
    bit_depth = r.u1()
    color_type = r.u1()
    chunks = [b"\x89PNG\r\n\x1a\n"]
    chunks.append(png_chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, bit_depth, color_type, 0, 0, 0)))
    if color_type == 3:
        plen = r.i4()
        chunks.append(png_chunk(b"PLTE", r.read(plen)))
        maybe_type = r.i4()
        if maybe_type == 0x74524E53:  # tRNS
            n = r.u1()
            n = 256 if n == 0 else n
            transparent_index = r.u1()
            chunks.append(png_chunk(b"tRNS", bytes(0 if i == transparent_index else 255 for i in range(n))))
            idat_len = r.i4()
        else:
            idat_len = maybe_type
    elif color_type == 6:
        idat_len = r.i4()
    else:
        idat_len = r.i4()
    chunks.append(png_chunk(b"IDAT", r.read(idat_len)))
    chunks.append(png_chunk(b"IEND", b""))
    return b"".join(chunks)


def decode_images():
    decoded = []
    for path in sorted(ROOT.rglob("*")):
        if not path.is_file():
            continue
        r = rel(path)
        data = path.read_bytes()
        out_name = r.replace("/", "__").replace("\\", "__")
        try:
            if data.startswith(b"\x89PNG\r\n\x1a\n"):
                out = PNG_OUT / (out_name + ".png" if not out_name.lower().endswith(".png") else out_name)
                out.write_bytes(data)
                decoded.append({"path": r, "decoded_png": str(out).replace("\\", "/"), "method": "raw_png"})
            elif r.startswith("data/img/") and r.endswith(".mid"):
                out = PNG_OUT / (out_name[:-4] + ".png")
                out.write_bytes(data)
                decoded.append({"path": r, "decoded_png": str(out).replace("\\", "/"), "method": "raw_png_disguised_mid"})
            elif r == "data/tex/menu.mid":
                out = PNG_OUT / "data__tex__menu.png"
                out.write_bytes(data)
                decoded.append({"path": r, "decoded_png": str(out).replace("\\", "/"), "method": "raw_png_menu_mid"})
            elif r.startswith("data/tex/") and r.endswith(".mid"):
                png = reconstruct_tex_png(data)
                out = PNG_OUT / (out_name[:-4] + ".png")
                out.write_bytes(png)
                decoded.append({"path": r, "decoded_png": str(out).replace("\\", "/"), "method": "reconstructed_png_chunks"})
        except Exception as e:
            decoded.append({"path": r, "decoded_png": "", "method": "failed", "error": repr(e)})
    with open(OUT / "decoded_images.csv", "w", newline="", encoding="utf-8") as f:
        keys = sorted({k for row in decoded for k in row})
        w = csv.DictWriter(f, fieldnames=keys)
        w.writeheader()
        w.writerows(decoded)
    return decoded


def parse_short_matrix(data, start=0):
    r = Reader(data)
    r.pos = start
    count = r.i2()
    arr = []
    for _ in range(count):
        n = r.i2()
        arr.append([r.i2() for _ in range(n)])
    return arr, r.pos


def parse_byte_matrix(data, start=0):
    r = Reader(data)
    r.pos = start
    count = r.i2()
    arr = []
    for _ in range(count):
        n = r.i2()
        arr.append([r.i1() for _ in range(n)])
    return arr, r.pos


def parse_string_matrix(data, start=0):
    r = Reader(data)
    r.pos = start
    count = r.i2()
    arr = []
    for _ in range(count):
        n = r.i2()
        row = []
        for _ in range(n):
            ln = r.u1()
            if ln == 255:
                ln = r.i2()
            chars = [chr(r.u2()) for _ in range(ln)]
            row.append(fix_text("".join(chars)))
        arr.append(row)
    return arr, r.pos


def parse_mod_info(data):
    r = Reader(data)
    count = r.i1()
    rows = []
    for i in range(count):
        n = r.i1()
        rows.append({"mod_index": i, "image_ids": [r.i2() for _ in range(n)]})
    return rows


def parse_mod(data):
    r = Reader(data)
    n = r.i2()
    rows = []
    for i in range(n):
        rows.append({
            "index": i,
            "image_slot": r.i1(),
            "x": r.i2(),
            "y": r.i2(),
            "w": r.i2(),
            "h": r.i2(),
        })
    return rows


def parse_map(data):
    r = Reader(data)
    compact = r.i1()
    mod_id = r.i1()
    width = r.i1() if compact == 1 else r.i2()
    height = r.i1() if compact == 1 else r.i2()
    tile_size = r.i1()
    layer_count = r.i1()
    layers = []
    for _ in range(layer_count):
        layer_index = r.i1()
        layer_type = r.i1()
        entry_count = r.i2()
        entries = []
        for _ in range(entry_count):
            x = r.i1() if compact == 1 else r.i2()
            y = r.i1() if compact == 1 else r.i2()
            raw = r.i2()
            entries.append({
                "x": x,
                "y": y,
                "tile": raw & 0x0FFF,
                "flags": (raw & 0x7000) >> 12,
                "raw": raw,
            })
        layers.append({
            "layer_index": layer_index,
            "layer_type": layer_type,
            "entry_count": entry_count,
            "entries_sample": entries[:80],
        })
    return {
        "compact": compact,
        "mod_id": mod_id,
        "width_tiles": width,
        "height_tiles": height,
        "tile_size": tile_size,
        "pixel_width": width * tile_size,
        "pixel_height": height * tile_size,
        "layer_count": layer_count,
        "layers": layers,
        "bytes_consumed": r.pos,
        "bytes_total": len(data),
    }


def parse_utf8_len(r):
    ln = r.u2()
    if ln == 0:
        return ""
    return r.read(ln).decode("utf-8", errors="replace")


def parse_ui_func(r, inherited_name=None, depth=0):
    name = parse_utf8_len(r) or inherited_name or ""
    line_defined = r.i4()
    last_line = r.i4()
    max_stack = r.u1()
    param_count = r.u1()
    flags = r.u1()
    upvalue_count = r.u1()
    code_count = r.i4()
    code = [r.i4() for _ in range(code_count)]
    const_count = r.i4()
    consts = []
    for _ in range(const_count):
        typ = r.u1()
        if typ == 0:
            consts.append({"type": "nil", "value": None})
        elif typ == 1:
            consts.append({"type": "bool", "value": bool(r.u1())})
        elif typ == 3:
            consts.append({"type": "int", "value": r.i4()})
        elif typ == 4:
            consts.append({"type": "string", "value": parse_utf8_len(r)})
        else:
            consts.append({"type": f"unknown_{typ}", "value": None})
            raise ValueError(f"unknown ui const type {typ} at {r.pos}")
    child_count = r.i4()
    children = [parse_ui_func(r, name, depth + 1) for _ in range(child_count)]
    trailer = [r.i4(), r.i4(), r.i4()]
    return {
        "name": name,
        "line_defined": line_defined,
        "last_line": last_line,
        "max_stack": max_stack,
        "param_count": param_count,
        "flags": flags,
        "vararg": bool(flags & 2),
        "upvalue_count": upvalue_count,
        "instruction_count": code_count,
        "instructions_sample": code[:80],
        "constants": consts,
        "child_count": child_count,
        "children": children,
        "trailer": trailer,
    }


def parse_ui_bytecode(data):
    r = Reader(data)
    sig = r.u1()
    if sig != 0x1B:
        raise ValueError("bad vm bytecode signature")
    header_rest = list(r.read(11))
    fn = parse_ui_func(r)
    return {"signature": sig, "header_rest": header_rest, "root": fn, "bytes_consumed": r.pos, "bytes_total": len(data)}


def parse_ui(data):
    return parse_ui_layout(data)


def parse_ui_text(r):
    n = r.i2()
    return fix_text(r.read(n).decode("utf-16-be", errors="replace"))


def parse_ui_style(r):
    style_id = r.i1()
    enabled = bool(r.i1())
    depth = r.i2()
    text_count = r.i2()
    h = r.i1()
    i = r.i1()
    texts = []
    for _ in range(text_count):
        text_id = r.i2()
        text = parse_ui_text(r)
        texts.append({"id": text_id, "text": text})
    range_group_count = r.i2()
    range_groups = []
    for _ in range(range_group_count):
        slot = r.i2()
        count = r.i2()
        rects = []
        for _ in range(count):
            rects.append([r.i2(), r.i2(), r.i2(), r.i2(), r.i2()])
        range_groups.append({"slot": slot, "rects": rects})
    return {
        "style_id": style_id,
        "enabled": enabled,
        "depth": depth,
        "text_count": text_count,
        "h": h,
        "i": i,
        "texts": texts,
        "range_groups": range_groups,
    }


def parse_ui_common_node(r, node_type):
    return {
        "type": node_type,
        "id": r.i2(),
        "x": r.i2(),
        "y": r.i2(),
        "w": r.i2(),
        "h": r.i2(),
    }


def parse_ui_af(r):
    node = parse_ui_common_node(r, "visual")
    node["text"] = parse_ui_text(r)
    node["b"] = r.i1()
    node["c"] = r.i1()
    node["d"] = bool(r.i1())
    node["e_color"] = r.i4()
    node["f_color"] = r.i4()
    node["g_color"] = r.i4()
    idx = r.i2()
    mode = r.i1()
    node["image_ref"] = None if idx < 0 else {"id": idx, "mode": mode}
    node["j_color"] = r.i4()
    node["k_color"] = r.i4()
    node["l_color"] = r.i4()
    idx = r.i2()
    mode = r.i1()
    node["alt_image_ref"] = None if idx < 0 else {"id": idx, "mode": mode}
    node["h"] = r.i1()
    node["a_byte"] = r.i1()
    node["b_byte"] = r.i1()
    return node


def parse_ui_ac(r):
    node = {
        "type": "list_grid",
        "id": r.i2(),
        "x": r.i2(),
        "y": r.i2(),
        "a0": r.i1(),
        "b0": r.i1(),
        "c0": r.i1(),
        "d0": r.i1(),
        "e0": r.i1(),
        "f0": r.i1(),
        "g0": r.i1(),
        "h0": r.i1(),
        "k0": r.i1(),
        "l0": r.i1(),
        "m0": r.i1(),
        "n0": r.i1(),
        "o0": r.i1(),
        "p0": r.i1(),
        "i0": r.i1(),
        "j0": r.i1(),
        "a_color": r.i4(),
    }
    idx = r.i2()
    mode = r.i1()
    node["image_ref"] = None if idx < 0 else {"id": idx, "mode": mode}
    idx = r.i2()
    mode = r.i1()
    node["alt_image_ref"] = None if idx < 0 else {"id": idx, "mode": mode}
    node["r_value"] = r.i2()
    fill_mode = r.i1()
    node["fill_mode"] = fill_mode
    if fill_mode == 1:
        count = r.i2()
        cells = []
        for _ in range(count):
            cells.append({
                "index": r.i2(),
                "value": r.i2(),
                "byte": r.i1(),
                "x": r.i2(),
                "y": r.i2(),
                "w": r.i2(),
                "h": r.i2(),
            })
        node["cells"] = cells
    return node


def parse_ui_container_body(r, container):
    nav_count = r.i1()
    if nav_count > 0:
        container["navigation"] = [[r.i1(), r.i1(), r.i1(), r.i1()] for _ in range(nav_count)]
    else:
        container["navigation"] = []

    style_count = r.i1()
    styles = []
    for _ in range(style_count):
        styles.append(parse_ui_style(r))
    container["styles"] = styles

    child_count = r.i2()
    children = []
    for child_index in range(child_count):
        node_type = r.i1()
        if node_type == 0:
            child = parse_ui_common_node(r, "container")
            parse_ui_container_body(r, child)
        elif node_type == 1:
            child = parse_ui_af(r)
        elif node_type == 2:
            child = parse_ui_ac(r)
        else:
            raise ValueError(f"unknown ui node type {node_type} at {r.pos}")
        tail = []
        while child_index < child_count - 1 and r.remain() > 0 and r.data[r.pos] not in (0, 1, 2):
            tail.append(r.i1())
        if tail:
            child["tail_padding_or_unknown"] = tail
        children.append(child)
    container["children"] = children
    return container


def parse_ui_layout(data):
    r = Reader(data)
    header = {
        "magic_or_flags": r.i2(),
        "version": r.i2(),
    }
    root = {"type": "root_container"}
    root["h"] = r.i1()
    root["id"] = r.i2()
    root["x"] = r.i2()
    root["y"] = r.i2()
    root["w"] = r.i2()
    root["h_px"] = r.i2()
    parse_ui_container_body(r, root)
    return {
        "format": "ui_layout",
        "header": header,
        "root": root,
        "bytes_consumed": r.pos,
        "bytes_total": len(data),
    }


def read_utf16be_short_string(r):
    ln = r.i2()
    return fix_text("".join(chr(r.u2()) for _ in range(ln)))


def read_utf16be_byte_string(r):
    ln = r.i1()
    return fix_text("".join(chr(r.u2()) for _ in range(ln)))


SCENE_OFFSETS = [0, 2, 9, 17, 25, 38, 45, 47, 60, 67, 75, 90]


def parse_actor(r):
    param_len = r.i2()
    vals = [0] * param_len
    vals[0] = r.i1()
    vals[1] = r.i2()
    vals[2] = r.i2()
    vals[3] = r.i2()
    vals[4] = r.i2()
    vals[5] = r.i1()
    vals[6] = r.i1()
    kind = vals[0]
    if kind == 1:
        vals[7] = r.i1()
        vals[8] = r.i2()
        vals[9] = r.i2()
        vals[10] = r.i2()
    elif kind == 2:
        vals[7] = r.i2()
        if vals[7] == 1 and param_len >= 13:
            vals[8] = r.i1()
            vals[9] = r.i1()
            vals[10] = r.i1()
            vals[11] = r.i1()
            vals[12] = r.i1()
    elif kind == 0:
        vals[7] = r.i1()
        vals[8] = r.i1()
        vals[9] = r.i1()
        vals[10] = r.i1()
        vals[11] = r.i2()
        vals[12] = r.i2()
    elif kind == 3:
        vals[7] = r.i1()
        vals[8] = r.i1()
        vals[9] = r.i1()
        vals[10] = r.i2()
        vals[11] = r.i2()
    else:
        for i in range(7, param_len):
            vals[i] = None
    return {"kind": kind, "param_len": param_len, "values": vals}


def parse_ad_record(r, string_pool):
    opcode = r.i2()
    total_arg_count = r.i1()
    short_arg_count = r.i1()
    short_args = [r.i2() for _ in range(short_arg_count)]
    string_args = []
    for _ in range(max(0, total_arg_count - short_arg_count)):
        idx = r.i2()
        string_args.append({
            "string_index": idx,
            "text": string_pool[idx] if string_pool and 0 <= idx < len(string_pool) else None,
        })
    return {
        "opcode": opcode,
        "total_arg_count": total_arg_count,
        "short_args": short_args,
        "string_args": string_args,
    }


def parse_event_group(r, index, scene_id, room_index, string_pool):
    record_count = r.i2()
    records = [parse_ad_record(r, string_pool) for _ in range(record_count)]
    return {
        "group_index": index,
        "scene_id": scene_id,
        "room_index": room_index,
        "world_key": (scene_id << 8) | room_index,
        "record_count": record_count,
        "records": records,
    }


def parse_scene_room(data, room_index, scene_id=None):
    r = Reader(data)
    block_count = r.i2()
    sizes = [r.i2() for _ in range(block_count)]
    if room_index >= block_count:
        raise IndexError("room out of range")
    r.skip(sum(sizes[:room_index]))
    room_start = r.pos
    string_count = r.i2()
    pool = [read_utf16be_short_string(r) for _ in range(string_count)]
    name = read_utf16be_byte_string(r)
    unknown_ab = r.i2()
    unknown_skip = r.i2()
    actor_count = r.i2()
    actors = [parse_actor(r) for _ in range(actor_count)]
    post_actor_strings = []
    # In normal files this section exists after actor_count, but very small rooms may end early.
    if r.remain() >= 2:
        try:
            n = r.i2()
            for _ in range(n):
                post_actor_strings.append(read_utf16be_byte_string(r))
        except Exception:
            pass
    event_group_count = 0
    event_groups = []
    if r.remain() >= 2:
        event_group_count = r.i2()
        for i in range(max(0, event_group_count)):
            event_groups.append(parse_event_group(r, i, scene_id or 0, room_index, pool))
    return {
        "room_index": room_index,
        "block_size": sizes[room_index],
        "room_start": room_start,
        "room_name": name,
        "string_pool_count": string_count,
        "string_pool": pool,
        "unknown_ab": unknown_ab,
        "unknown_skip": unknown_skip,
        "actor_count": actor_count,
        "actors": actors,
        "post_actor_strings": post_actor_strings,
        "event_group_count": event_group_count,
        "event_groups": event_groups,
        "bytes_consumed_in_room": r.pos - room_start,
        "declared_block_size": sizes[room_index],
    }


def parse_scene(data, scene_id=None):
    r = Reader(data)
    block_count = r.i2()
    sizes = [r.i2() for _ in range(block_count)]
    rooms = []
    for idx in range(block_count):
        try:
            rooms.append(parse_scene_room(data, idx, scene_id))
        except Exception as e:
            rooms.append({"room_index": idx, "error": repr(e), "declared_block_size": sizes[idx]})
    return {"block_count": block_count, "block_sizes": sizes, "rooms": rooms}


def decode_known_tables():
    decoded = {}

    def save(name, obj):
        decoded[name] = obj
        target = JSON_OUT / (name.replace("/", "__") + ".json")
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(json.dumps(obj, ensure_ascii=False, indent=2), encoding="utf-8")

    known_string = ["data/script/bTask.mid", "data/script/mTask.mid", "data/script/chs.mid", "data/script/npcDialog.mid"]
    for rp in known_string:
        p = ROOT / rp
        if p.exists():
            arr, pos = parse_string_matrix(p.read_bytes())
            save(rp, {"format": "string_matrix", "rows": arr, "bytes_consumed": pos, "bytes_total": p.stat().st_size})

    known_short = [
        "data/script/sprite.mid", "data/script/pos.mid", "data/script/speffect.mid",
        "data/script/blood.mid", "data/script/petArea.mid", "data/script/backPic.mid",
    ]
    for rp in known_short:
        p = ROOT / rp
        if p.exists():
            arr, pos = parse_short_matrix(p.read_bytes())
            save(rp, {"format": "short_matrix", "rows": arr, "bytes_consumed": pos, "bytes_total": p.stat().st_size})

    known_byte = ["data/script/effect.mid"]
    for rp in known_byte:
        p = ROOT / rp
        if p.exists():
            arr, pos = parse_byte_matrix(p.read_bytes())
            save(rp, {"format": "byte_matrix", "rows": arr, "bytes_consumed": pos, "bytes_total": p.stat().st_size})

    p = ROOT / "data/script/bqTask.mid"
    if p.exists():
        data = p.read_bytes()
        first, pos = parse_byte_matrix(data)
        second, pos2 = parse_byte_matrix(data, pos)
        save("data/script/bqTask.mid", {"format": "two_byte_matrices", "first": first, "second": second, "bytes_consumed": pos2, "bytes_total": len(data)})

    p = ROOT / "data/script/db.mid"
    if p.exists():
        data = p.read_bytes()
        pos = 0
        groups = []
        for i in range(9):
            arr, pos = parse_short_matrix(data, pos)
            groups.append(arr)
        save("data/script/db.mid", {"format": "nine_short_matrices", "groups": groups, "bytes_consumed": pos, "bytes_total": len(data)})

    p = ROOT / "data/script/cpos.mid"
    if p.exists():
        data = p.read_bytes()
        pos = 0
        groups = []
        for i in range(3):
            arr, pos = parse_short_matrix(data, pos)
            groups.append(arr)
        save("data/script/cpos.mid", {"format": "three_short_matrices", "groups": groups, "bytes_consumed": pos, "bytes_total": len(data)})

    p = ROOT / "data/script/bufDebuf.mid"
    if p.exists():
        data = p.read_bytes()
        pos = 0
        groups = []
        for i in range(3):
            arr, pos = parse_byte_matrix(data, pos)
            groups.append(arr)
        save("data/script/bufDebuf.mid", {"format": "three_byte_matrices", "groups": groups, "bytes_consumed": pos, "bytes_total": len(data)})

    p = ROOT / "data/mod/modInfo.mid"
    if p.exists():
        save("data/mod/modInfo.mid", {"format": "mod_info", "mods": parse_mod_info(p.read_bytes())})

    for p in sorted((ROOT / "data/mod").glob("mod_*.mid")):
        save(rel(p), {"format": "mod_tile_rects", "records": parse_mod(p.read_bytes())})

    for p in sorted((ROOT / "data/map").glob("map_*.mid")):
        save(rel(p), {"format": "map", **parse_map(p.read_bytes())})

    for p in sorted((ROOT / "data/event").glob("scene_*.mid")):
        m = re.search(r"scene_(\d+)\.mid$", p.name)
        scene_id = int(m.group(1)) if m else None
        save(rel(p), {"format": "event_scene", "scene_id": scene_id, **parse_scene(p.read_bytes(), scene_id)})

    for p in sorted((ROOT / "data/ui").glob("*.ui")):
        try:
            save(rel(p), parse_ui(p.read_bytes()))
        except Exception as e:
            save(rel(p), {"format": "ui_layout", "error": repr(e)})

    p = ROOT / "data/event/scene_13.mib"
    if p.exists():
        try:
            save("data/event/scene_13.mib", {"format": "vm_bytecode_tree", **parse_ui_bytecode(p.read_bytes())})
        except Exception as e:
            save("data/event/scene_13.mib", {"format": "vm_bytecode_tree", "error": repr(e)})

    with open(OUT / "decoded_tables_index.csv", "w", newline="", encoding="utf-8") as f:
        rows = []
        for name, obj in decoded.items():
            rows.append({
                "path": name,
                "format": obj.get("format", ""),
                "json": str(JSON_OUT / (name.replace("/", "__") + ".json")).replace("\\", "/"),
            })
        w = csv.DictWriter(f, fieldnames=["path", "format", "json"])
        w.writeheader()
        w.writerows(rows)
    return decoded


def extract_code_references():
    code_root = Path("work/decompiled/cfr")
    refs = []
    string_re = re.compile(r'"(/data/[^"]+)"')
    for java in sorted(code_root.rglob("*.java")):
        text = java.read_text(encoding="utf-8", errors="replace").splitlines()
        for idx, line in enumerate(text, 1):
            for m in string_re.finditer(line):
                refs.append({"resource": m.group(1), "source": str(java).replace("\\", "/"), "line": idx, "context": line.strip()})
            for pat in ["/data/event/", "/data/map/", "/data/img/", "/data/tex/", "/data/spr/", "/data/mod/"]:
                if pat in line and '"' not in line:
                    refs.append({"resource": pat + "*", "source": str(java).replace("\\", "/"), "line": idx, "context": line.strip()})
    with open(OUT / "code_resource_references.csv", "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=["resource", "source", "line", "context"])
        w.writeheader()
        w.writerows(refs)
    return refs


def write_summary(inv, decoded_images, decoded_tables, refs):
    counts = {}
    for row in inv:
        counts[row["kind"]] = counts.get(row["kind"], 0) + 1
    lines = []
    lines.append("# Reverse Engineering Summary")
    lines.append("")
    lines.append("## What Was Decoded")
    for k, v in sorted(counts.items()):
        lines.append(f"- {k}: {v}")
    lines.append("")
    lines.append("## Runtime Entry Points")
    lines.append("- `game.GameMIDLet` creates `game.e`, a full-screen MIDP Canvas.")
    lines.append("- `game.e.run()` is the main loop: update `game.i.b()`, repaint, then sleep for `an.B() - frame_time` milliseconds.")
    lines.append("- `game.i` is the high-level state/screen/audio controller. It loads only `/data/sound/0.mid` as real `audio/midi`.")
    lines.append("- `game.k` is the main world/scene controller. It loads `/data/event/scene_<scene>.mid` and selects a room block by `g`.")
    lines.append("- `j` is the map renderer/loader for `/data/map/map_<id>.mid` and `/data/mod/mod_<tileset>.mid`.")
    lines.append("- `aq`, `game.c`, `game.d`, and `game.k` load script tables under `/data/script/*.mid`.")
    lines.append("- `ao`/`ab` load binary UI layout files under `/data/ui/*.ui`.")
    lines.append("- package `a.*` is a compact VM used by `/data/event/scene_13.mib` for SMS/payment scripting.")
    lines.append("")
    lines.append("## Resource Formats")
    lines.append("- `data/sound/0.mid`: real MIDI audio (`MThd`).")
    lines.append("- `data/img/img_*.mid`: PNG bytes stored with `.mid` extension.")
    lines.append("- `data/tex/menu.mid`: PNG bytes stored with `.mid`; other `data/tex/*.mid` are compact PNG chunk containers reconstructed by `ae.f()`.")
    lines.append("- `data/event/scene_*.mid`: scene files containing a block table, per-room string pool, room name, actor records, and action/event records.")
    lines.append("- `data/map/map_*.mid`: map files containing compact flag, tileset/mod id, dimensions, tile size, layer count, and tile/object entries.")
    lines.append("- `data/mod/mod_*.mid`: tileset rectangle metadata for rendering map tiles.")
    lines.append("- `data/script/*.mid`: typed tables: string matrices, short matrices, byte matrices, or multiple such matrices in sequence.")
    lines.append("- `data/ui/*.ui`: binary UI layouts parsed from `ao.a(...)`: root container, styles, visual controls, and list/grid controls.")
    lines.append("- `data/event/scene_13.mib`: compact VM bytecode tree parsed from `a.f`.")
    lines.append("")
    lines.append("## Output Files")
    lines.append("- `file_inventory.csv`: every file, size, SHA-256, magic bytes, inferred type.")
    lines.append("- `decoded_images.csv` and `decoded_assets/png/`: extracted/reconstructed PNG assets.")
    lines.append("- `decoded_tables_index.csv` and `decoded_assets/json/`: decoded map/script/event/ui JSON files.")
    lines.append("- `code_resource_references.csv`: resource path strings and source locations in decompiled code.")
    lines.append("- `decompiled_source_cfr/`: CFR Java source for all classes.")
    lines.append("- `bytecode_javap/`: verbose bytecode disassembly for all classes.")
    (OUT / "reverse_engineering_summary.md").write_text("\n".join(lines), encoding="utf-8")


def copy_code_outputs():
    dst = OUT / "decompiled_source_cfr"
    if dst.exists():
        shutil.rmtree(dst)
    shutil.copytree("work/decompiled/cfr", dst)
    dst2 = OUT / "bytecode_javap"
    if dst2.exists():
        shutil.rmtree(dst2)
    shutil.copytree("work/decompiled/javap", dst2)


def main():
    ensure_dirs()
    inv = inventory()
    decoded_images = decode_images()
    decoded_tables = decode_known_tables()
    refs = extract_code_references()
    copy_code_outputs()
    write_summary(inv, decoded_images, decoded_tables, refs)
    print(f"inventory={len(inv)} decoded_images={len(decoded_images)} decoded_tables={len(decoded_tables)} refs={len(refs)}")


if __name__ == "__main__":
    main()
