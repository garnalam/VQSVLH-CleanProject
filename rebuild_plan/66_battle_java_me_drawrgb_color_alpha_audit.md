# 66 - Battle Java ME `drawRGB` Color/Alpha Audit

Status: PORTED/PARTIAL.

Scope: the color/alpha transform helpers used by battle `ah` effects, especially state-1 `L` effects:

- `l.a(Image,e)`
- `l.a(e, alpha, red, green, blue)`
- `l.b(e, alpha)`
- `l.b(e, multiplier, add)`
- `Graphics.drawRGB(..., processAlpha=true)`

## Source Facts

Source files:

- `modules/source_code/decoded/decompiled_source_cfr/l.java`
- `modules/source_code/decoded/decompiled_source_cfr/e.java`
- `modules/source_code/decoded/decompiled_source_cfr/ae.java`
- `modules/source_code/decoded/decompiled_source_cfr/ah.java`

### `l.a(Image,e)`

The original converts a MIDP image to an `e` pixel buffer by calling `Image.getRGB(...)`, then rewrites:

```text
0xFFFFFFFF -> 0x00FFFFFF
0xFF000000 -> 0x00FFFFFF
```

This means opaque white and opaque black are used as transparent-key pixels inside transformed effect buffers.

### `l.a(e, alpha, red, green, blue)`

This recolor helper skips only pixels equal to `0x00FFFFFF`.

For every other pixel:

```text
if alpha < 0 or alpha > 255:
    pixel = red << 16 | green << 8 | blue
else:
    pixel = alpha << 24 | red << 16 | green << 8 | blue
```

There is no channel clamp in this method.

### `l.b(e, alpha)`

This alpha helper returns unchanged when `alpha` is outside `0..255`.

For valid alpha it skips pixels equal to `0x00FFFFFF` or `0x00000000`.

For every other pixel:

```text
if pixel == 0xFF000000:
    pixel = 0
else:
    pixel = alpha << 24 | (pixel & 0x00FFFFFF)
```

### `l.b(e, multiplier, add)`

This RGB adjust helper applies to every pixel, preserving the original alpha byte and clamping RGB channels:

```text
r = clamp(r * multiplier + add)
g = clamp(g * multiplier + add)
b = clamp(b * multiplier + add)
pixel = oldAlpha | r << 16 | g << 8 | b
```

### `Graphics.drawRGB(..., true)`

All audited AH draw paths call MIDP `drawRGB(..., processAlpha=true)`.

The rebuild uses `BufferedImage.TYPE_INT_ARGB` and `Graphics2D.drawImage(...)`. This is source-shaped for alpha compositing, but exact Java ME vendor/device blending still needs MIDP pixel capture before claiming pixel-perfect parity.

## Rebuild Changes

Updated `rebuild_game/src/main/java/VqsvBattleRenderer.java`.

| Source behavior | Rebuild equivalent | Status |
| --- | --- | --- |
| `l.a(Image,e)` white/black transparent-key rewrite | `normalizeJavaMeEffectPixels(...)` converts alpha-zero, opaque white, and opaque black pixels to `0x00FFFFFF` for AH effect buffers | PORTED/PARTIAL |
| `l.a(e, alpha,r,g,b)` skips only `0x00FFFFFF` | `tintOpaque(...)` now skips the Java ME key and applies the same alpha-valid branch | PORTED/PARTIAL |
| `l.b(e, alpha)` skips `0x00FFFFFF` and `0` | `alphaCopy(...)` now preserves those skipped pixels and applies the source black-to-zero rule | PORTED/PARTIAL |
| `l.b(e, multiplier,add)` applies to every pixel | `adjustRgb(...)` now adjusts all pixels while preserving alpha bits | PORTED/PARTIAL |
| MIDP `drawRGB(..., true)` compositing | Java SE ARGB image drawing | PARTIAL |

## Remaining Pending

- Need frame capture from the original MIDP runtime to prove exact `drawRGB(..., true)` blending on the target device/emulator.
- Need pixel diff between original and rebuild for representative AH types `11`, `12`, `13`, `14`, and `15`.
- Java SE and Java ME may differ subtly in alpha compositing after `drawRGB`; current rebuild is source-shaped, not yet pixel-certified.
