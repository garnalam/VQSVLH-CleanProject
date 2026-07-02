# Runtime Skeleton

Tài liệu này mô tả khung project Java mới cần dựng để build ra JAR chạy giống bản gốc.

## Mục Tiêu Project

- Source code mới, có cấu trúc rõ, build lặp lại được.
- Đọc resource gốc từ `res/data/...`.
- Có hai target nếu cần:
  - Java SE debug target: chạy nhanh trên PC, dễ screenshot/so sánh.
  - MIDP/J2ME target: đóng JAR điện thoại/emulator sau khi logic ổn.

## Cấu Trúc Đề Xuất

```text
vqsv-rebuild/
  build.gradle hoặc build.xml
  res/
    data/
      event/
      img/
      map/
      mod/
      script/
      spr/
      tex/
      ui/
      sound/
    font.bin
    icon.png
  src/
    game/
      MainMidlet.java
      DebugLauncher.java
    platform/
      GameCanvas.java
      GraphicsPort.java
      ImagePort.java
      Input.java
      Clock.java
    resource/
      ResourceLoader.java
      BinaryReader.java
      ImageStore.java
      ScriptTables.java
    render/
      SpriteStore.java
      SpriteRenderer.java
      MapRenderer.java
      FontBitmap.java
      EffectRenderer.java
      DisplayList.java
    event/
      EventScene.java
      EventRecord.java
      EventVm.java
      OpcodeHandlers.java
    world/
      WorldState.java
      RoomLoader.java
      Actor.java
      Camera.java
    ui/
      UiManager.java
      UiParser.java
      UiComponent.java
    battle/
      BattleState.java
    save/
      SaveStore.java
```

## Build Milestones

1. Empty project builds an executable JAR.
2. Resource loader reads original `res/data` paths.
3. Renderer can draw a known sprite and font text.
4. World state can load scene 0 room 0.
5. Event VM can run intro scene.
6. UI manager can load one `.ui`.
7. Main menu/world/battle states are connected.

## Compatibility Layer

MIDP APIs cần mô phỏng hoặc bọc:

- `Canvas`, `MIDlet`, `Graphics`, `Image`, `Font`.
- `Graphics.drawRegion` với 8 transform.
- Anchor constants, đặc biệt anchor `20` trong nhiều `drawImage/drawRegion`.
- Key codes: `48` -> bit `1`, số `0`; các phím số, `*`, `#`, softkey, d-pad.
- Timing: loop khoảng `an.B() = 66ms`.

## Quy Tắc Port

- Port theo hành vi, không cần giữ tên class obfuscated.
- Giữ bảng mapping từ class gốc sang class mới.
- Mỗi module mới cần test nhỏ hoặc scene chứng minh.
