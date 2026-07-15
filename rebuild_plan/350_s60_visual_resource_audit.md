# 350 - S60 Visual Resource Audit

Date: 2026-07-14

Status: AUDIT PASS WITH CAVEATS / NO BROKEN PNG ATLAS FOUND / MAP99 PENDING.

Scope:

- decoded image atlases
- tex/logo images
- UI layout resources
- map rendering
- panel smoke PNG
- Battle Lab catch/NPC smoke PNG

No client window was opened.

## Inputs

Active resource baseline:

```text
modules/
```

S60 merge docs:

```text
rebuild_plan/347_aowvn_240x320_resource_candidate_decode_audit.md
rebuild_plan/348_s60_resource_merge_into_modules_status.md
rebuild_plan/349_s60_battle_smoke_rebaseline_status.md
```

## Generated Audit Outputs

Tools:

```text
build/visual_audit_tools/visual_resource_audit.py
build/visual_audit_tools/smoke_contact_sheet.py
build/visual_audit_tools/MapContactSheet.java
```

Output folder:

```text
build/visual_audit/
```

Key files:

```text
build/visual_audit/image_inventory.csv
build/visual_audit/image_suspicious.csv
build/visual_audit/ui_layout_inventory.csv
build/visual_audit/ui_layout_issues.csv
build/visual_audit/map_inventory.csv
build/visual_audit/visual_audit_summary.md
build/visual_audit/contact_sheets/
build/visual_audit/map_renders/
```

## Image Decode / Atlas Audit

Command:

```text
python build/visual_audit_tools/visual_resource_audit.py
```

Result:

```text
images=335
suspicious=1
uiIssues=291
maps=102
```

Decoded images by group:

```text
img decoded: 308
tex decoded: 25
logo decoded: 2
```

Suspicious image:

```text
tex/data__tex__bk.png 24x32 uniform
```

Interpretation:

```text
Likely OK. This looks like a small texture/placeholder tile, not decode
corruption. No other decoded PNG was fully transparent, unreadable, or visibly
broken in the generated contact sheets.
```

Contact sheets inspected:

```text
build/visual_audit/contact_sheets/img_page_01.png
build/visual_audit/contact_sheets/img_page_02.png
build/visual_audit/contact_sheets/img_page_03.png
build/visual_audit/contact_sheets/img_page_04.png
build/visual_audit/contact_sheets/img_page_05.png
build/visual_audit/contact_sheets/img_page_06.png
build/visual_audit/contact_sheets/img_page_07.png
build/visual_audit/contact_sheets/tex_logo_page_01.png
```

Visual conclusion:

```text
PASS. NPC/pet/effect/portrait/menu/logo/background atlases render normally.
No obvious atlas row shift, broken transparency, wrong palette, or sliced image
corruption was seen.
```

Important note:

```text
tex/menu.mid.png remains old-only mixed resource by design. It was visible and
not corrupt in the contact sheet.
```

## UI Layout Audit

The automatic UI scan reported:

```text
uiIssues=291 missing-image-ref
```

Interpretation:

```text
Do not treat this as 291 real errors. The decoded UI widget refs are often
source UI cell/sprite ids, not direct data/img ids. This scanner is useful for
finding suspicious rows, but runtime smoke PNG is the real check for now.
```

UI layout contact sheets:

```text
build/visual_audit/contact_sheets/ui_layout_page_01.png
build/visual_audit/contact_sheets/ui_layout_page_02.png
build/visual_audit/contact_sheets/ui_layout_page_03.png
build/visual_audit/contact_sheets/ui_layout_page_04.png
```

Runtime UI smoke checked below is more important than raw layout rectangle
preview.

## Map Render Audit

Command:

```text
javac -cp rebuild_game/build/classes -d build/visual_audit_tools build/visual_audit_tools/MapContactSheet.java
java -cp "build/visual_audit_tools;rebuild_game/build/classes" MapContactSheet E:\VQSVLH-CleanProject E:\VQSVLH-CleanProject\build\visual_audit\map_renders
```

Result:

```text
renderedMaps=102
```

Contact sheets:

```text
build/visual_audit/map_renders/maps_contact_sheet_page_01.png
build/visual_audit/map_renders/maps_contact_sheet_page_02.png
build/visual_audit/map_renders/maps_contact_sheet_page_03.png
build/visual_audit/map_renders/maps_contact_sheet_page_04.png
build/visual_audit/map_renders/maps_contact_sheet_page_05.png
build/visual_audit/map_renders/maps_contact_sheet_page_06.png
```

Visual conclusion:

```text
PASS/PARTIAL. All 102 maps load and render without renderer crash. The visible
map sheets do not show broad tile atlas corruption.
```

Map caveats:

```text
map_101 has layer0 entry_count=0 but renders normally from other layers.
map_99 has all layer entry counts 0 and renders blank/black.
```

Classification:

```text
map_101: PORTED/PARTIAL, visually OK.
map_99: PENDING reachability/source-purpose audit. It may be an unused blank
placeholder map. Do not fix unless a source event/route actually loads it.
```

## Runtime Smoke Audit

### Panel / World Petstate

Command:

```text
java -cp build\classes VqsvIntroDemo --smoke-suite panel_wheel build\visual_audit\smoke_panel_wheel
```

Result:

```text
PASS 8/8
```

Contact sheet:

```text
build/visual_audit/contact_sheets/smoke_panel_wheel.png
```

Visual conclusion:

```text
PASS. bag.ui, task.ui, petmap.ui, petstate.ui, skill panel, world overlay, mouse
wheel/hover/click states render without obvious broken atlas or missing panel
background.
```

### Battle Lab Catch

Command:

```text
run_battle_lab_suite_smoke.cmd -Lane catch -Suite all
```

Result:

```text
PASS 30/30
```

Contact sheet:

```text
build/visual_audit/contact_sheets/smoke_battle_lab_catch_all.png
```

Visual conclusion:

```text
PASS/PARTIAL. P21 list, P17 capture, warning/openbox, storage result, and
world petstate smoke all render. Direct Battle Lab catch screens often have a
black battle background because the lab instantiates battle runtime directly
without a world-background snapshot. That is Battle Lab harness behavior, not
proof of a broken map asset.
```

### Battle Lab NPC

Command:

```text
run_battle_lab_suite_smoke.cmd -Lane npc -Suite all
```

Result:

```text
PASS 33/33
```

Contact sheet:

```text
build/visual_audit/contact_sheets/smoke_battle_lab_npc_all.png
```

Visual conclusion:

```text
PASS/PARTIAL. NPC VS entry, command UI, skill list, petstate forced switch,
item target, shopbuy/msgyn, EXP/level-up, and loss/revive UI all render without
obvious broken atlas.
```

Known visual debt visible/related:

```text
P24 lose/revive still uses smsinfo-style UI/mode naming. This is not image
corruption, but it remains a PC rebuild UX/source-route debt after SMS removal.
```

## Overall Findings

### PASS

```text
Decoded image atlases open and look coherent.
S60 effect/pet/NPC/portrait assets are present.
All 102 maps render through the Java renderer.
Panel wheel runtime smoke passes.
Battle Lab catch all passes.
Battle Lab NPC all passes.
```

### PARTIAL / Needs Care

```text
UI raw missing-image-ref scan is noisy because source UI refs are not direct
decoded img ids. Use runtime smoke as the practical verifier.

Battle Lab direct battle background is black in many synthetic checkpoints.
This should not be confused with route/world battle background unless reproduced
in a real route smoke.

map_99 is blank by data. Needs source reachability audit before deciding if it
is a real bug.
```

### Not Claimed

```text
No pixel-perfect claim.
No original-client-vs-rebuild frame compare.
No guarantee that every one of the 325 sprite binaries has every animation state
visually correct; this pass verifies resource load/coherence and representative
runtime screens.
```

## Recommended Next Step

Do not start broad hand-polishing. The next practical step is:

```text
Create a permanent visual regression suite that bundles:
1. image/tex/logo contact sheet generation,
2. map contact sheet generation,
3. panel_wheel,
4. Battle Lab catch all,
5. Battle Lab NPC all,
6. selected route-world smoke frames for real battle background.
```

After that, fix visual debt one concrete route/UI at a time. The first likely
target is either:

```text
route-world battle background snapshot verification
```

or:

```text
P24 lose/revive UI after SMS removal.
```

