# 347 - AowVN 240x320 Resource Candidate Decode Audit

Date: 2026-07-14

Status: CANDIDATE RESOURCE SET DECODED / NOT YET WIRED INTO REBUILD.

User supplied:

```text
C:/Users/itado/Downloads/AowVN.org-S60-VuongQuoc-Pokemon-240x320.jar
```

The JAR was extracted and decoded into a separate candidate folder:

```text
modules_aowvn_240x320/
```

The existing `modules/` folder was not replaced or modified.

## Decode Commands / Tools

Tooling used:

```text
jar xf
CFR 0.152 from C:/Users/itado/Downloads/cfr-0.152.jar
javap -verbose
modules/reports/decoded/tools/analyze_jar.py
```

Intermediate workspace:

```text
build/decode_aowvn_240x320/
```

Analyzer result:

```text
inventory=931 decoded_images=335 decoded_tables=181 refs=376
```

## Candidate Module Counts

```text
event        original=  16 decoded=  14
img          original= 308 decoded= 308
logo         original=   2 decoded=   2
map          original= 102 decoded= 102
mod          original=   8 decoded=   8
reports      original=   0 decoded=   7
root_misc    original=   8 decoded=   1
script       original=  19 decoded=  15
sound        original=   7 decoded=   0
source_code  original=  70 decoded= 145
spr          original= 325 decoded=   0
tex          original=  24 decoded=  24
ui           original=  42 decoded=  42
```

Notes:

- `source_code` has CFR Java and javap bytecode.
- `spr` is copied as original binary only. Current rebuild tooling can read
  original sprite binaries directly, but there is no full human-readable decoded
  JSON sheet for all sprites yet.
- `sound` is copied as original MIDI only.
- Some event/script files are copied but not decoded by the old analyzer because
  the parser does not yet cover them.

## Compared With Current `modules/`

Main original-file inventory comparison:

```text
event        old=  13 new=  16 added=   3 missing=   0
img          old= 242 new= 308 added=  66 missing=   0
logo         old=   3 new=   2 added=   0 missing=   1
map          old=  94 new= 102 added=   8 missing=   0
mod          old=   8 new=   8 added=   0 missing=   0
script       old=  17 new=  19 added=   2 missing=   0
sound        old=   1 new=   7 added=   6 missing=   0
spr          old= 299 new= 325 added=  26 missing=   0
tex          old=  23 new=  24 added=   2 missing=   1
ui           old=  40 new=  42 added=   2 missing=   0
```

The source/root class split differs between the old folder and this candidate,
so `source_code/root_misc` should be compared by package/class audit, not only
by per-module count.

Clarifications:

- The missing logo file is only `Thumbs.db`; not gameplay-relevant.
- The new candidate does not include `data/tex/menu.mid`, but it adds
  `ikon_2.mid` and `ikon_3.mid`. Before a full swap, any rebuild path depending
  on `tex/menu.mid` must be checked.

## Notable Added Files

Added event resources:

```text
minimap.mid
scene_8.mid
worldEvt.mid
```

Added scripts:

```text
layer.mid
media.mid
```

Full candidate script list:

```text
backPic.mid
battleNpc.mid
blood.mid
bqTask.mid
bTask.mid
bufDebuf.mid
chs.mid
cpos.mid
db.mid
effect.mid
layer.mid
media.mid
mTask.mid
npcDialog.mid
petArea.mid
petRide.mid
pos.mid
speffect.mid
sprite.mid
```

Added UI:

```text
menu.ui
menu1.ui
```

Added sound:

```text
1.mid
2.mid
3.mid
4.mid
5.mid
6.mid
```

Added image count:

```text
66 extra data/img files
```

Examples:

```text
img_10093.mid
img_105.mid
img_106.mid
img_109.mid
img_110.mid
img_111.mid
img_112.mid
img_114.mid
img_115.mid
img_116.mid
img_128.mid
img_129.mid
img_13.mid
img_133.mid
img_138.mid
img_139.mid
img_140.mid
img_141.mid
img_147.mid
img_15.mid
img_150.mid
img_153.mid
img_19.mid
img_20.mid
img_21.mid
img_22.mid
img_255.mid
img_256.mid
img_257.mid
img_258.mid
img_262.mid
img_268.mid
img_302.mid
img_304.mid
img_515.mid
img_532.mid
img_546.mid
img_547.mid
img_548.mid
img_550.mid
img_551.mid
img_568.mid
img_569.mid
img_570.mid
img_571.mid
img_572.mid
img_573.mid
img_589.mid
img_590.mid
img_591.mid
img_603.mid
img_815.mid
img_816.mid
img_817.mid
img_822.mid
img_823.mid
img_824.mid
img_825.mid
img_826.mid
img_827.mid
img_828.mid
img_829.mid
img_830.mid
img_831.mid
img_832.mid
img_837.mid
```

## Candidate Reports

Useful generated files:

```text
modules_aowvn_240x320/reports/decoded/file_inventory.csv
modules_aowvn_240x320/reports/decoded/decoded_images.csv
modules_aowvn_240x320/reports/decoded/decoded_tables_index.csv
modules_aowvn_240x320/reports/decoded/code_resource_references.csv
modules_aowvn_240x320/reports/decoded/reverse_engineering_summary.md
modules_aowvn_240x320/reports/decoded/cfr.log
modules_aowvn_240x320/source_code/decoded/decompiled_source_cfr/
modules_aowvn_240x320/source_code/decoded/bytecode_javap/
```

## Current Classification

| Area | Status | Notes |
|---|---|---|
| JAR extraction | PORTED | 931 files extracted from 962 JAR entries including directories. |
| Image decode | PORTED | 308 `data/img` images decoded for candidate. |
| Texture decode | PORTED/PARTIAL | 24 texture PNG outputs; one old texture mismatch should be inspected before replacing. |
| Event decode | PORTED/PARTIAL | Scene `.mid` decoded; `minimap.mid/worldEvt.mid` are copied but parser coverage is not proven. |
| Script decode | PORTED/PARTIAL | Known tables decoded; new `layer.mid/media.mid` copied but need parser audit. |
| UI decode | PORTED | 42 UI layouts decoded, including `menu.ui/menu1.ui`. |
| Sprite decode | PORTED/PARTIAL | 325 original sprite binaries copied; rebuild can read originals, but no full decoded sprite report yet. |
| Source decompile | PORTED/PARTIAL | CFR output exists for 72 classes; bytecode exists for cross-checking decompiler oddities. |
| Rebuild integration | BLOCKED/PENDING | Read-only `--check` with candidate root currently fails because rebuild expects `tex/decoded/data__tex__menu.mid.png`, but this AowVN JAR has no `data/tex/menu.mid`. |

## Read-only Rebuild Check

Command:

```text
java "-Dvqsv.modules=..\modules_aowvn_240x320" -cp build\classes com.vqsv.rebuild.Main --check
```

Result:

```text
Missing resource file:
modules_aowvn_240x320/tex/decoded/data__tex__menu.mid.png
```

Interpretation:

```text
The candidate decode is valid, but it cannot be swapped into the current rebuild
as-is because the current PC rebuild boot path still expects the old decoded
menu texture.
```

Do not blindly copy old `menu.mid` into the candidate without marking it as a
fallback/mixed resource.

## Recommended Next Step

Before replacing `modules/`, do a read-only candidate audit:

1. Decide how to handle missing `tex/menu.mid`: source-backed replacement from
   `menu.ui/menu1.ui`, a documented old-resource fallback, or a rebuild boot UI
   change.
2. Compare `db.mid`, `effect.mid`, `speffect.mid`, `sprite.mid`, and `chs.mid`
   row counts/content against current modules.
3. Render candidate contact sheets for battle actor sprites `262..268`, `299..309`,
   especially skill effects like HAT BUI `sprite264/img305`.
4. Try a temporary runtime override pointing `-Dvqsv.modules=../modules_aowvn_240x320`
   in smoke only, not release.
5. Run battle smoke suites and compare PNGs.
6. Only after smoke passes should we consider switching the main resource root.
