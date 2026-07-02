import csv
import json
import shutil
from collections import Counter, defaultdict
from pathlib import Path


OUT = Path("outputs")
JSON_DIR = OUT / "decoded_assets" / "json"


def load_json(name):
    return json.loads((JSON_DIR / name).read_text(encoding="utf-8"))


def write_scene_overview():
    rows = []
    for p in sorted(JSON_DIR.glob("data__event__scene_*.mid.json")):
        if p.name.endswith("scene_13.mib.json"):
            continue
        obj = json.loads(p.read_text(encoding="utf-8"))
        scene_id = obj.get("scene_id")
        for room in obj.get("rooms", []):
            rows.append({
                "scene_id": scene_id,
                "room_index": room.get("room_index"),
                "world_key": ((scene_id or 0) << 8) | int(room.get("room_index", 0)),
                "room_name": room.get("room_name", ""),
                "actor_count": room.get("actor_count", 0),
                "event_group_count": room.get("event_group_count", 0),
                "string_pool_count": room.get("string_pool_count", 0),
                "declared_block_size": room.get("declared_block_size", ""),
            })
    with open(OUT / "scene_room_overview.csv", "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=list(rows[0].keys()))
        w.writeheader()
        w.writerows(rows)
    return rows


def write_resource_graph():
    refs = list(csv.DictReader(open(OUT / "code_resource_references.csv", encoding="utf-8")))
    grouped = defaultdict(list)
    for row in refs:
        grouped[row["resource"]].append(row)
    lines = ["# Code Resource Call Graph", ""]
    for resource, items in sorted(grouped.items(), key=lambda kv: (-len(kv[1]), kv[0]))[:120]:
        lines.append(f"## {resource} ({len(items)} refs)")
        for item in items[:12]:
            source = item["source"].replace("work/decompiled/cfr/", "")
            lines.append(f"- `{source}:{item['line']}` - `{item['context']}`")
        if len(items) > 12:
            lines.append(f"- ... {len(items) - 12} more")
        lines.append("")
    (OUT / "resource_call_graph.md").write_text("\n".join(lines), encoding="utf-8")


def write_report(scene_rows):
    inv = list(csv.DictReader(open(OUT / "file_inventory.csv", encoding="utf-8")))
    refs = list(csv.DictReader(open(OUT / "code_resource_references.csv", encoding="utf-8")))
    table_index = list(csv.DictReader(open(OUT / "decoded_tables_index.csv", encoding="utf-8")))
    img_rows = list(csv.DictReader(open(OUT / "decoded_images.csv", encoding="utf-8")))

    kind_counts = Counter(row["kind"] for row in inv)
    fmt_counts = Counter(row["format"] for row in table_index)
    top_refs = Counter(row["resource"] for row in refs).most_common(20)

    scene_counts = defaultdict(list)
    for row in scene_rows:
        scene_counts[int(row["scene_id"])].append(row)

    lines = []
    lines.append("# Báo Cáo Giải Mã JAR Game")
    lines.append("")
    lines.append("## Kết Luận Nhanh")
    lines.append("- Đây là game Java ME MIDP 2.0/CLDC 1.0; entrypoint là `game.GameMIDLet`.")
    lines.append("- Các class đã bị obfuscate tên ngắn (`a`, `b`, `game.k`, hàm `a()`, `b()`...), nhưng decompile được đầy đủ bằng CFR và bytecode `javap` đã lưu kèm.")
    lines.append("- Chỉ `data/sound/0.mid` là MIDI âm thanh thật. Hầu hết `.mid` còn lại là dữ liệu nhị phân riêng của game: map, scene, script table, ảnh PNG trá hình, hoặc container PNG.")
    lines.append("- `data/event/scene_*.mid` điều khiển scene/room: string pool, tên room, actor records, event groups/opcodes. Code gọi trực tiếp nằm ở `game.k`, phần thực thi điều kiện/sự kiện nằm nhiều ở `game.c`, actor/world state ở `game.g`/`game.a`.")
    lines.append("- `data/ui/*.ui` là layout nhị phân, được parser `ao` dựng thành container/control/style. `data/event/scene_13.mib` mới là bytecode VM của package `a.*`.")
    lines.append("")

    lines.append("## Deliverables")
    lines.append("- `reverse_engineering_report_vi.md`: báo cáo này.")
    lines.append("- `file_inventory.csv`: inventory 814 file, SHA-256, magic bytes, loại file.")
    lines.append("- `decoded_assets/png/`: 268 ảnh PNG giải mã, tất cả đã verify mở được.")
    lines.append("- `decoded_assets/json/`: 170 JSON đã decode: map, scene, script, UI, VM bytecode.")
    lines.append("- `scene_room_overview.csv`: danh sách scene/room, `world_key`, actor count, event group count.")
    lines.append("- `resource_call_graph.md`: file resource nào được class/line nào gọi.")
    lines.append("- `decompiled_source_cfr/`: source Java decompiled cho 68 class.")
    lines.append("- `bytecode_javap/`: disassembly verbose cho 68 class để đối chiếu khi CFR chưa rõ.")
    lines.append("")

    lines.append("## Thống Kê File")
    for kind, count in sorted(kind_counts.items()):
        lines.append(f"- `{kind}`: {count}")
    lines.append("")

    lines.append("## Luồng Chạy Chính")
    lines.append("1. `game.GameMIDLet` tạo `game.e` và set làm Canvas hiện tại.")
    lines.append("2. `game.e` bật fullscreen, khởi tạo `game.i`, rồi start thread chính.")
    lines.append("3. Vòng lặp `game.e.run()` gọi `game.i.b()` để update logic, `repaint/serviceRepaints`, sau đó sleep theo `an.B()`. Đây là nhịp nhanh/chậm frame của game.")
    lines.append("4. `game.i` quản lý trạng thái màn hình, logo/audio/loading, rồi chuyển sang world/battle/menu.")
    lines.append("5. `game.k` là world/scene controller: giữ `f=scene_id`, `g=room_index`, load `/data/event/scene_<f>.mid`, chọn block room `g`, tạo actor, nạp event groups, map và asset liên quan.")
    lines.append("6. `j` nạp `/data/map/map_<id>.mid`; map chỉ chứa tile/object entries, còn tileset rectangle lấy từ `/data/mod/mod_<id>.mid` và danh sách ảnh tileset từ `/data/mod/modInfo.mid`.")
    lines.append("")

    lines.append("## Vai Trò Class Quan Trọng")
    role_rows = [
        ("game.GameMIDLet", "MIDlet entrypoint."),
        ("game.e", "Canvas + main loop + key/touch dispatch; điều tiết frame bằng `an.B()`."),
        ("game.i", "Game state manager, logo/loading/audio; chỉ load MIDI thật ở `/data/sound/0.mid`."),
        ("game.k", "World/scene controller, scene loader, map transition, actor creation, RMS save/load."),
        ("game.c", "Event/quest trigger engine; đọc event group `p/ad`, kiểm tra opcode điều kiện và chạy phản ứng."),
        ("game.d", "Battle scene/controller; nạp script combat như `pos`, `effect`, `speffect`, `blood`, `bufDebuf`."),
        ("game.g", "Player/pet/world entity state lớn; được event và battle logic thao tác."),
        ("j", "Map renderer/loader cho `data/map` và `data/mod`."),
        ("aq", "Global data table loader: sprite, modInfo, chs, npcDialog, db, tex."),
        ("ae", "Utility giải mã resource: bảng short/byte/string, PNG container, image loader, text helpers."),
        ("aj", "Resource stream wrapper `getResourceAsStream`."),
        ("ao/ab/al/af/ac/z", "UI layout manager/parser và control tree cho `.ui`."),
        ("a.*", "VM bytecode runtime dùng bởi `scene_13.mib` SMS/payment script."),
    ]
    for name, role in role_rows:
        lines.append(f"- `{name}`: {role}")
    lines.append("")

    lines.append("## Định Dạng Resource Đã Giải")
    for fmt, count in sorted(fmt_counts.items()):
        lines.append(f"- `{fmt}`: {count} JSON")
    lines.append("")
    lines.append("- `string_matrix`: short count -> rows -> string length -> UTF-16BE chars, có repair mojibake tiếng Việt.")
    lines.append("- `short_matrix` / `byte_matrix`: cấu trúc count/row length/values theo `ae.a` và `ae.b`.")
    lines.append("- `event_scene`: block table theo room, string pool, room name, actors, post actor strings, event groups.")
    lines.append("- `ui_layout`: root container, navigation map, styles, visual controls, list/grid controls theo parser `ao`.")
    lines.append("- `vm_bytecode_tree`: function tree/constant/instruction sample cho VM `a.*`.")
    lines.append("")

    lines.append("## Scene Và Room")
    for scene_id in sorted(scene_counts):
        rooms = scene_counts[scene_id]
        sample = ", ".join(r["room_name"] for r in rooms[:5])
        lines.append(f"- `scene_{scene_id}.mid`: {len(rooms)} room. Ví dụ: {sample}")
    lines.append("")
    lines.append("Mỗi room có `world_key = scene_id << 8 | room_index`; đây là key mà `game.c`/`p` giữ để biết event thuộc scene-room nào. Chi tiết từng room nằm trong `scene_room_overview.csv` và JSON `decoded_assets/json/data__event__scene_*.mid.json`.")
    lines.append("")

    lines.append("## Top Resource Được Code Gọi")
    for resource, count in top_refs:
        lines.append(f"- `{resource}`: {count} refs")
    lines.append("")

    lines.append("## Cách Scene Truy Asset")
    lines.append("- Scene file không nhúng ảnh trực tiếp; actor records chứa sprite/id/type/position/state. Khi actor được dựng, code đi qua `game.a`, `game.g`, `am`, `aq.a`, và `data/spr/spr_<id>_all(r)` để lấy animation/sprite metadata.")
    lines.append("- Map room được chọn bởi `game.k.f`/`game.k.g`; `j.a(mapId)` load `data/map/map_<id>.mid`, trong map có `mod_id`; `j.d()` dùng `data/mod/mod_<mod_id>.mid` và `aq.b[mod_id]` từ `modInfo.mid` để biết ảnh `data/img/img_<id>.mid` nào cần render.")
    lines.append("- UI được gọi bằng `ab/ao` từ `game.h`, `game.k`, `game.f`, ví dụ `msgwarm.ui`, `dialog.ui`, `battle.ui`, `bag.ui`; call sites cụ thể nằm trong `resource_call_graph.md`.")
    lines.append("- Script tables như `bTask`, `mTask`, `npcDialog`, `chs`, `db` cung cấp text/quest/NPC database; event opcode trong `game.c` trỏ tới các bảng này qua string pool hoặc id.")
    lines.append("")

    lines.append("## Giới Hạn Còn Lại")
    lines.append("- Tên biến/hàm gốc không thể phục hồi 100% vì obfuscation đã xóa metadata. Báo cáo dùng vai trò suy ra từ bytecode, loader, và call graph.")
    lines.append("- Opcode event đã được tách thành `opcode`, `short_args`, `string_args`; đặt tên ngữ nghĩa cho mọi opcode cần thêm một vòng annotation thủ công dựa trên switch lớn trong `game.c`.")
    lines.append("- Sprite binary `data/spr/spr_*_all(r)` đã inventory đầy đủ; để render từng animation frame cần reverse thêm class sprite/entity (`f`, `am`, `aa`, `game.a`) hoặc chạy emulator/instrumentation.")
    lines.append("")
    lines.append(f"Ảnh decoded: {len(img_rows)} PNG. JSON decoded: {len(table_index)}. Code-resource refs: {len(refs)}.")
    (OUT / "reverse_engineering_report_vi.md").write_text("\n".join(lines), encoding="utf-8")


def main():
    scene_rows = write_scene_overview()
    write_resource_graph()
    write_report(scene_rows)
    tool_out = OUT / "tools"
    tool_out.mkdir(exist_ok=True)
    shutil.copy2("work/analyze_jar.py", tool_out / "analyze_jar.py")
    shutil.copy2("work/generate_report.py", tool_out / "generate_report.py")
    print("wrote report, scene overview, resource graph")


if __name__ == "__main__":
    main()
