package vqsv.game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import vqsv.quest.QuestMarker;
import vqsv.render.SpriteAnimator;
import vqsv.render.UnifiedItemIconRenderer;
import vqsv.session.GameSession;
import vqsv.ui.layout.VqsvUiLayout;
import vqsv.ui.text.TextBox;
import vqsv.ui.text.TextBoxRenderer;
import vqsv.ui.text.UiFont;
import vqsv.world.WorldTalkPromptEngine;

final class VqsvSceneView {
   private static final int AVOID_MONSTER_PLAYER_ALPHA = 64;
   private static final SpriteAnimator PLAYER_SHADOW = SpriteAnimator.load(337);

   private VqsvSceneView() {
   }

   static void render(VqsvGameRuntime.Scene var0, Graphics2D var1) {
      if (var0.sourceMapLoading.active()) {
         var0.sourceMapLoading.render(var0, var1);
      } else if (!(var0.session.runtime.activity instanceof VqsvWharfTravelRuntime) || !((VqsvWharfTravelRuntime)var0.session.runtime.activity).renderExclusive(var0, var1)) {
         AffineTransform var2 = var1.getTransform();
         var1.translate(var0.effect.shakeX(), var0.effect.shakeY());
         renderWorld(var0, var1, true, true, true, false);
         NguyenMocC4RaceRuntime.renderOverlay(var0, var1);
         VqsvDarkRoomPresentation.render(var0, var1);
         var1.setTransform(var2);
         VqsvBattleRenderer.render(var0, var1);
         var0.effect.renderOverlay(var1);
         if (var0.session.runtime.battleOverlayTicks <= 0 && !((VqsvPanelRuntime)var0.session.runtime.ui).visible && !var0.sourceReleaseConfirmVisible) {
            var0.worldUi.render(var1, var0.font, var0.session.world.useMap, var0.session.runtime.speedX2);
         }

         if (var0.session.runtime.battleOverlayTicks <= 0 && var0.worldPetstateVisible) {
            VqsvBattleRenderer.renderPetStateOverlay(var1, var0.font, var0, false);
         }

         if (var0.session.runtime.battleOverlayTicks <= 0 && var0.sourcePetSettingVisible) {
            renderSourcePetSetting(var0, var1);
         }

         if (var0.session.runtime.battleOverlayTicks <= 0 && var0.sourceSkillVisible) {
            renderSourceSkillUi(var0, var1);
         }

         if (var0.session.runtime.battleOverlayTicks <= 0 && var0.sourceItemChoiceVisible) {
            renderSourceItemChoiceUi(var0, var1);
         }

         if (var0.session.runtime.battleOverlayTicks <= 0 && var0.sourceEquipmentChoiceVisible) {
            renderSourceChoiceUi(var0, var1, var0.sourceEquipmentChoiceView());
         }

         if (var0.session.runtime.battleOverlayTicks <= 0 && var0.sourceReleaseConfirmVisible) {
            renderSourceReleaseConfirm(var0, var1);
         }

         if (var0.session.runtime.battleOverlayTicks <= 0 && var0.qualityUpgradeVisible) {
            VqsvBattleRenderer.renderQualityUpgradeOverlay(var1, var0.font, var0);
         }

         if (var0.session.runtime.battleOverlayTicks <= 0 && var0.sourceEvolveVisible) {
            VqsvBattleRenderer.renderEvolutionOverlay(var1, var0.font, var0);
         }

         if (var0.session.runtime.battleOverlayTicks <= 0 && ((VqsvPanelRuntime)var0.session.runtime.ui).visible) {
            ((VqsvPanelRuntime)var0.session.runtime.ui).render(var1, var0.font, var0);
         }

         if (var0.text != null) {
            TextBoxRenderer.render(var0.text, var1, var0.font);
         }

         if (var0.choice != null) {
            var0.choice.render(var1, var0.font);
         }

         if (var0.savePromptVisible) {
            renderSavePrompt(var0, var1);
         }

      }
   }

   static BufferedImage captureBattleBackground(VqsvGameRuntime.Scene var0) {
      BufferedImage var1 = new BufferedImage(240, 320, 2);
      Graphics2D var2 = var1.createGraphics();
      var2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
      var2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
      renderWorld(var0, var2, false, false, false, true);
      VqsvDarkRoomPresentation.render(var0, var2);
      var2.dispose();
      return var1;
   }

   private static void renderWorld(VqsvGameRuntime.Scene var0, Graphics2D var1, boolean var2, boolean var3, boolean var4, boolean var5) {
      Color var6 = NguyenMocRoutePresentationPolicy.background(var0);
      var1.setColor(var0.session.world.useMap ? (var6 == null ? Color.BLACK : var6) : new Color(8, 16, 80));
      var1.fillRect(0, 0, 240, 320);
      if (var0.session.world.useMap) {
         renderSourceBackPic(var0, var1);
         renderMapLayer(var0, var1, 1);
         renderMapLayer(var0, var1, 2);
      }

      if (var4 || var5) {
         if (var4) {
            renderActorLayer(var0, var1, 2, false, false);
            renderActorAndPlayerLayer(var0, var1);
         } else {
            renderActorLayer(var0, var1, 1, true, true);
            renderActorLayer(var0, var1, 2, false, true);
         }
      }

      if (var0.session.world.useMap) {
         renderMapLayer(var0, var1, 3);
      }

      if (var4 || var5) {
         renderActorLayer(var0, var1, 0, false, var5 && !var4);
      }

      if (var3) {
         for(TempSprite var8 : var0.tempSprites) {
            var8.render(var1, var0);
         }

         renderSourceActorMarkers(var0, var1);

         for(QuestMarker var10 : var0.session.story.questMarkers) {
            VqsvQuestMarkerRuntime.render(var10, var1, var0);
         }

         VqsvTalkPromptRuntime.render(var1, var0);
      }

      if (var2) {
         var0.effect.renderParticles(var1);
      }

   }

   private static void renderSourceBackPic(VqsvGameRuntime.Scene var0, Graphics2D var1) {
      BufferedImage var2 = NguyenMocRoutePresentationPolicy.backgroundImage(var0);
      if (var2 != null && var2.getWidth() > 0) {
         int var3 = 240 / var2.getWidth();

         for(int var4 = 0; var4 < var3; ++var4) {
            var1.drawImage(var2, var4 * var2.getWidth(), 0, (ImageObserver)null);
         }

      }
   }

   private static void renderSourceActorMarkers(VqsvGameRuntime.Scene var0, Graphics2D var1) {
      if (var0.text == null && !((VqsvPanelRuntime)var0.session.runtime.ui).visible) {
         WorldTalkPromptEngine.TalkPromptRenderState var2 = VqsvFreeWorldRuntime.talkPromptRenderState(var0);

         for(int var3 = 0; var3 < var0.actors.length; ++var3) {
            Actor var4 = var0.actors[var3];
            if (var4 != null && (!var2.visible || var2.actorId != var3)) {
               var4.renderSourceMarker(var1, var0.session.world.cameraX, var0.session.world.cameraY);
            }
         }

      }
   }

   static void setCameraCenter(VqsvGameRuntime.Scene var0, int var1, int var2) {
      if (var0.session.world.useMap && var0.mapRenderer != null) {
         var0.mapRenderer.centerCameraOn(var1, var2);
         var0.session.world.cameraX = var0.mapRenderer.cameraX();
         var0.session.world.cameraY = var0.mapRenderer.cameraY();
      } else {
         var0.session.world.cameraX = clamp(var1 - 120, 0, 400);
         var0.session.world.cameraY = clamp(var2 - 160, 0, 160);
      }

   }

   static void moveCameraToward(VqsvGameRuntime.Scene var0, int var1, int var2, int var3) {
      int var4;
      int var5;
      if (var0.session.world.useMap && var0.mapRenderer != null) {
         var0.mapRenderer.centerCameraOn(var1, var2);
         var4 = var0.mapRenderer.cameraX();
         var5 = var0.mapRenderer.cameraY();
      } else {
         var4 = clamp(var1 - 120, 0, 400);
         var5 = clamp(var2 - 160, 0, 160);
      }

      if (var3 <= 0) {
         var0.session.world.cameraX = var4;
         var0.session.world.cameraY = var5;
      } else {
         int var6 = var4 - var0.session.world.cameraX;
         int var7 = var5 - var0.session.world.cameraY;
         int var8 = (int)Math.sqrt((double)(var6 * var6 + var7 * var7));
         if (var8 <= var3) {
            var0.session.world.cameraX = var4;
            var0.session.world.cameraY = var5;
         } else {
            GameSession.WorldState var10000 = var0.session.world;
            var10000.cameraX += var6 * var3 / var8;
            var10000 = var0.session.world;
            var10000.cameraY += var7 * var3 / var8;
         }
      }

      if (var0.session.world.useMap && var0.mapRenderer != null) {
         var0.mapRenderer.setCamera(var0.session.world.cameraX, var0.session.world.cameraY);
         var0.session.world.cameraX = var0.mapRenderer.cameraX();
         var0.session.world.cameraY = var0.mapRenderer.cameraY();
      }

   }

   static boolean cameraCenteredOn(VqsvGameRuntime.Scene var0, int var1, int var2) {
      int var3 = var0.session.world.cameraX;
      int var4 = var0.session.world.cameraY;
      if (var0.session.world.useMap && var0.mapRenderer != null) {
         var0.mapRenderer.centerCameraOn(var1, var2);
         boolean var5 = var3 == var0.mapRenderer.cameraX() && var4 == var0.mapRenderer.cameraY();
         var0.mapRenderer.setCamera(var3, var4);
         return var5;
      } else {
         return var3 == clamp(var1 - 120, 0, 400) && var4 == clamp(var2 - 160, 0, 160);
      }
   }

   static void followActor(VqsvGameRuntime.Scene var0, int var1) {
      var0.session.world.followActorId = var1;
      updateCameraFollow(var0);
   }

   static void stopCameraFollow(VqsvGameRuntime.Scene var0) {
      var0.session.world.followActorId = -1;
   }

   static void updateCameraFollow(VqsvGameRuntime.Scene var0) {
      if (var0.session.world.followActorId >= 0 && var0.session.world.followActorId < var0.actors.length && var0.actors[var0.session.world.followActorId] != null) {
         Actor var1 = var0.actors[var0.session.world.followActorId];
         setCameraCenter(var0, var1.x, var1.y);
      }
   }

   private static void renderMapLayer(VqsvGameRuntime.Scene var0, Graphics2D var1, int var2) {
      if (var0.session.world.useMap && var0.mapRenderer != null && var0.mapRenderer.hasLayer(var2)) {
         var0.mapRenderer.renderLayer(var1, var2);
      }

   }

   private static void renderActorLayer(VqsvGameRuntime.Scene var0, Graphics2D var1, int var2, boolean var3, boolean var4) {
      ArrayList<Actor> var5 = new ArrayList<>();

      for(Actor var9 : var0.actors) {
         if (var9 != null && var9.visible && var9.layer == var2 && (!var4 || var9.variant == 0)) {
            var5.add(var9);
         }
      }

      if (var3) {
         var5.sort(Comparator.comparingInt((var0x) -> var0x.y));
      }

      for(Actor var11 : var5) {
         renderSourceActor(var0, var1, var11);
      }

   }

   private static void renderActorAndPlayerLayer(VqsvGameRuntime.Scene var0, Graphics2D var1) {
      ArrayList<Actor> var2 = new ArrayList<>();
      TanNguyetLongMaBossRuntime.prepareForRender(var0);

      for(Actor var6 : var0.actors) {
         if (var6 != null && var6.visible && var6.layer == 1) {
            var2.add(var6);
         }
      }

      if (var0.petCompanion.visible) {
         var2.add(var0.petCompanion);
      }

      if (var0.tanNguyetLongMaBoss.visible) {
         var2.add(var0.tanNguyetLongMaBoss);
      }

      var2.sort(Comparator.comparingInt((var0x) -> var0x.y));
      if (var0.session.world.useMap && var0.player.visible) {
         PLAYER_SHADOW.setState(var0.player.visualSpriteIndex == 4 ? 0 : 1);
         PLAYER_SHADOW.draw(var1, var0.player.x - var0.session.world.cameraX, var0.player.y - var0.session.world.cameraY, 0);
      }

      boolean var7 = false;

      for(Actor var9 : var2) {
         if (!var7 && var0.session.world.useMap && var0.player.visible && var0.player.y <= var9.y) {
            renderPlayer(var0, var1);
            var7 = true;
         }

         boolean customBoss = var9 == var0.tanNguyetLongMaBoss;
         if (customBoss) {
            TanNguyetLongMaWorldDecorationRenderer.renderShadow(var0, var1, var9);
         }
         renderSourceActor(var0, var1, var9);
         if (customBoss) {
            TanNguyetLongMaWorldDecorationRenderer.renderNameplate(var0, var1, var9);
         }
      }

      if (!var7 && var0.session.world.useMap && var0.player.visible) {
         renderPlayer(var0, var1);
      }

   }

   static int playerWorldAlpha(VqsvGameRuntime.Scene var0) {
      return var0.session.progression.avoidMonsterTicks > 0 ? 64 : 255;
   }

   private static void renderPlayer(VqsvGameRuntime.Scene var0, Graphics2D var1) {
      var0.player.render(var1, var0.session.world.cameraX, var0.session.world.cameraY, playerWorldAlpha(var0));
   }

   private static void renderSourceActor(VqsvGameRuntime.Scene var0, Graphics2D var1, Actor var2) {
      var2.render(var1, var0.session.world.cameraX, var0.session.world.cameraY);
      VqsvSourceLaserRuntime.render(var0, var1, var2);
   }

   private static int clamp(int var0, int var1, int var2) {
      return Math.max(var1, Math.min(var2, var0));
   }

   private static void renderSavePrompt(VqsvGameRuntime.Scene var0, Graphics2D var1) {
      VqsvUiLayout var2 = VqsvUiLayout.load("msgtip.ui");
      SpriteAnimator var3 = SpriteAnimator.load(257);
      VqsvUiLayout.UiWidget var4 = var2.widget(1);
      int var5 = var4 != null && var4.altId >= 0 ? var4.altId : 124;
      drawCellTopLeft(var3, var1, var5, var2.x(1, 51), var2.y(1, 134));
      String var6 = var0.savePromptStatus != null && !var0.savePromptStatus.isEmpty() ? var0.savePromptStatus : var0.savePromptMessage;
      drawSavePromptText(var0, var1, var6, var2);
      if (var0.savePromptStatus == null || var0.savePromptStatus.isEmpty()) {
         drawSavePromptWidgetCell(var3, var1, var2, 4, 75, 1, 298);
         drawSavePromptWidgetCell(var3, var1, var2, 3, 133, 218, 298);
      }

   }

   static void renderSourcePetSetting(VqsvGameRuntime.Scene var0, Graphics2D var1) {
      VqsvUiLayout var2 = VqsvUiLayout.load("petsetting.ui");
      SpriteAnimator var3 = SpriteAnimator.load(257);
      drawPetSettingBand(var1, var2, 1, 8, 13038591);
      drawPetSettingBand(var1, var2, 2, 104, 12444911);
      drawPetSettingBand(var1, var2, 3, 12, 7129979);
      drawPetSettingWidgetCell(var3, var1, var2, 4, false);
      int[] var4 = new int[]{5, 6, 7, 8, 10, 9};

      for(int var5 = 0; var5 < var4.length; ++var5) {
         int var6 = var0.sourcePetSettingScroll + var5;
         if (var6 >= var0.sourcePetSettingCount) {
            break;
         }

         int var7 = var4[var5];
         boolean var8 = var6 == var0.sourcePetSettingIndex;
         drawPetSettingWidgetCell(var3, var1, var2, var7, var8);
         drawPetSettingText(var0, var1, var2, var7, var0.sourcePetSettingActionLabel(var6), var8);
      }

   }

   private static void drawPetSettingBand(Graphics2D var0, VqsvUiLayout var1, int var2, int var3, int var4) {
      VqsvUiLayout.UiWidget var5 = var1.widget(var2);
      if (var5 != null) {
         int var6 = var5.jColor != 0 && var5.jColor != -1 ? var5.jColor & 16777215 : var4;
         var0.setColor(new Color(var6));
         var0.fillRect(var5.x, var5.y, Math.max(1, var5.w), Math.max(1, var1.bandHeight(var2, var3)));
      }
   }

   private static void drawPetSettingWidgetCell(SpriteAnimator var0, Graphics2D var1, VqsvUiLayout var2, int var3, boolean var4) {
      VqsvUiLayout.UiWidget var5 = var2.widget(var3);
      if (var5 != null) {
         int var6 = var4 && var5.altId >= 0 ? var5.altId : var5.imageId;
         if (var6 < 0) {
            var6 = var5.altId;
         }

         if (var6 >= 0) {
            drawCellTopLeft(var0, var1, var6, var5.x, var5.y);
         }
      }
   }

   private static void drawPetSettingText(VqsvGameRuntime.Scene var0, Graphics2D var1, VqsvUiLayout var2, int var3, String var4, boolean var5) {
      VqsvUiLayout.UiWidget var6 = var2.widget(var3);
      if (var6 != null && var4 != null && !var4.isEmpty()) {
         int var7 = var5 && var6.jColor != 0 && var6.jColor != -1 ? var6.jColor & 16777215 : (var6.lColor != 0 && var6.lColor != -1 ? var6.lColor & 16777215 : 1862801);
         Shape var8 = var1.getClip();
         int var9 = Math.max(var6.w, 76);
         var1.clipRect(var6.x - 18, var6.y - 1, var9, Math.max(12, var2.h(var3, 12)));
         int var10 = var6.x;
         if (var6.b == 4) {
            var10 = var6.x - 18 + Math.max(0, (var9 - var0.font.taggedWidth(var4)) / 2);
         }

         var0.font.drawTaggedLine(var1, var4, var10, var6.y, TextBox.visibleLength(TextBox.decodeMojibake(var4)), var7);
         var1.setClip(var8);
      }
   }

   private static void renderSourceSkillUi(VqsvGameRuntime.Scene var0, Graphics2D var1) {
      var1.setColor(new Color(2, 12, 28, 155));
      var1.fillRect(0, 0, 240, 320);
      var1.setColor(new Color(2, 18, 34, 225));
      var1.fillRect(8, 34, 224, 260);
      var1.setColor(new Color(16172879));
      var1.fillRect(10, 32, 220, 258);
      var1.setColor(new Color(409441));
      var1.fillRect(12, 34, 216, 254);
      var1.setColor(new Color(12381425));
      var1.drawRect(14, 36, 211, 249);
      var1.setColor(new Color(554650));
      var1.fillRect(15, 37, 210, 20);
      var1.setColor(new Color(16172879));
      var1.fillRect(15, 56, 210, 2);
      String var2 = var0.sourceSkillLearnMode ? (var0.sourceSkillLearnReplaceMode ? "CHỌN KỸ NĂNG CẦN QUÊN" : "HỌC KỸ NĂNG") : "QUẢN LÝ KỸ NĂNG";
      drawSkillDashboardText(var0, var1, var2, 18, 41, 204, true, 16777215);
      var1.setColor(new Color(15398900));
      var1.fillRect(15, 60, 210, 47);
      var1.setColor(new Color(8440277));
      var1.drawRect(15, 60, 209, 46);
      drawSkillDashboardPet(var1, var0.sourceSkillPetVisualId(), 18, 62, 48, 43);
      drawSkillDashboardText(var0, var1, var0.sourceSkillPetName(), 70, 64, 147, false, 1195861);
      drawSkillDashboardText(var0, var1, "Cấp " + var0.sourceSkillPetLevel(), 70, 77, 56, false, 12991302);
      drawSkillDashboardChip(var0, var1, "Trang bị " + var0.sourceSkillEquippedCount() + "/5", 70, 91, 70, 13, 1535875);
      drawSkillDashboardChip(var0, var1, "Bể " + var0.sourceSkillPoolUnlockedCount() + "/" + var0.sourceSkillPoolTotalCount(), 144, 91, 73, 13, 7296916);
      if (var0.sourceSkillLearnMode) {
         var1.setColor(new Color(1527145));
         var1.fillRect(18, 110, 204, 22);
         String var3;
         if (var0.sourceSkillLearnReplaceMode) {
            var3 = "Chọn chiêu cũ để thay thế";
         } else {
            int var4 = Math.max(1, var0.sourceSkillLearnQueueIndex + 1);
            int var5 = Math.max(1, var0.sourceSkillLearnQueueSkillIds.length);
            var3 = "Chiêu mới " + var4 + "/" + var5;
         }

         drawSkillDashboardText(var0, var1, var3, 21, 115, 198, true, 16777215);
      } else {
         int var10006 = var0.sourceSkillEquippedCount();
         drawSkillDashboardTab(var0, var1, 18, 110, 102, 22, "TRANG BỊ " + var10006 + "/5", !var0.sourceSkillPoolTab());
         drawSkillDashboardTab(var0, var1, 120, 110, 102, 22, "BỂ CHIÊU " + var0.sourceSkillPoolTotalCount(), var0.sourceSkillPoolTab());
      }

      for(int var11 = 0; var11 < 4; ++var11) {
         int var13 = var0.sourceSkillDisplayIndexAt(var11);
         int var15 = 134 + var11 * 19;
         boolean var6 = var13 >= 0 && var13 < var0.sourceSkillCount;
         boolean var7 = var6 && var13 == var0.sourceSkillIndex;
         int var8 = var6 ? var0.sourceSkillStateAt(var13) : 4;
         int var9 = var7 ? 16773018 : (var8 == 2 ? 14542051 : (var8 == 3 ? 15261424 : (var8 == 4 ? 14147042 : 16055285)));
         var1.setColor(new Color(var9));
         var1.fillRect(18, var15, 204, 18);
         var1.setColor(new Color(var7 ? 14781225 : 7646905));
         var1.drawRect(18, var15, 203, 17);
         if (var6) {
            var1.setColor(new Color(sourceSkillStateColor(var8)));
            var1.fillRect(22, var15 + 4, 5, 10);
            int var10 = var8 >= 2 ? 6583420 : 1195861;
            drawSkillDashboardText(var0, var1, var0.sourceSkillNameAt(var13), 31, var15 + 3, 112, false, var10);
            drawSkillDashboardText(var0, var1, var0.sourceSkillRowStatusAt(var13), 145, var15 + 3, 72, true, sourceSkillStateColor(var8));
         }
      }

      if (var0.sourceSkillCount == 0) {
         drawSkillDashboardText(var0, var1, "Chưa có kỹ năng", 22, 163, 196, true, 6583420);
      }

      drawSkillDashboardScrollbar(var1, var0.sourceSkillScroll, var0.sourceSkillCount, 4);
      var1.setColor(new Color(15398900));
      var1.fillRect(18, 212, 204, 75);
      var1.setColor(new Color(16172879));
      var1.drawRect(18, 212, 203, 74);
      if (var0.sourceSkillCount > 0) {
         int var12 = var0.sourceSkillIndex;
         int var14 = var0.sourceSkillStateAt(var12);
         drawSkillDashboardText(var0, var1, var0.sourceSkillNameAt(var12), 23, 216, 125, false, 1195861);
         drawSkillDashboardText(var0, var1, var0.sourceSkillStatusAt(var12), 150, 216, 66, true, sourceSkillStateColor(var14));
         drawSkillDashboardText(var0, var1, var0.sourceSkillMetaAt(var12), 23, 229, 194, false, 1535875);
         String var16 = TextBox.decodeMojibake(var0.sourceSkillDescription()).trim();
         List var17 = wrapChoiceDescription(var0, var16, 194, 3);

         for(int var18 = 0; var18 < var17.size(); ++var18) {
            drawSkillDashboardText(var0, var1, (String)var17.get(var18), 23, 244 + var18 * 12, 194, false, 3755350);
         }
      } else {
         drawSkillDashboardText(var0, var1, "Chọn BỂ CHIÊU để xem lộ trình học.", 23, 242, 194, true, 3755350);
      }

      drawSkillDashboardSoftkey(var0, var1, true, var0.sourceSkillPrimaryActionLabel());
      drawSkillDashboardSoftkey(var0, var1, false, "Quay lại");
   }

   private static void drawSkillDashboardTab(VqsvGameRuntime.Scene var0, Graphics2D var1, int var2, int var3, int var4, int var5, String var6, boolean var7) {
      var1.setColor(new Color(var7 ? 16172879 : 1527145));
      var1.fillRect(var2, var3, var4, var5);
      var1.setColor(new Color(var7 ? 8145952 : 8440277));
      var1.drawRect(var2, var3, var4 - 1, var5 - 1);
      drawSkillDashboardText(var0, var1, var6, var2 + 2, var3 + 5, var4 - 4, true, var7 ? 1523282 : 16777215);
   }

   private static void drawSkillDashboardChip(VqsvGameRuntime.Scene var0, Graphics2D var1, String var2, int var3, int var4, int var5, int var6, int var7) {
      var1.setColor(new Color(var7));
      var1.fillRect(var3, var4, var5, var6);
      drawSkillDashboardText(var0, var1, var2, var3 + 2, var4 + 1, var5 - 4, true, 16777215);
   }

   private static void drawSkillDashboardPet(Graphics2D var0, int var1, int var2, int var3, int var4, int var5) {
      if (var1 >= 0) {
         SpriteAnimator var6 = SpriteAnimator.load(var1);
         var6.setState(0);
         var6.setCursor(0);
         Shape var7 = var0.getClip();
         var0.clipRect(var2, var3, var4, var5);
         var6.drawAligned(var0, var2, var3, var4, var5, 7, 0);
         var0.setClip(var7);
      }
   }

   private static void drawSkillDashboardScrollbar(Graphics2D var0, int var1, int var2, int var3) {
      if (var2 > var3) {
         short var4 = 135;
         byte var5 = 73;
         int var6 = Math.max(10, var5 * var3 / var2);
         int var7 = Math.max(1, var2 - var3);
         int var8 = var4 + (var5 - var6) * var1 / var7;
         var0.setColor(new Color(2379618));
         var0.fillRect(224, var4, 3, var5);
         var0.setColor(new Color(16172879));
         var0.fillRect(224, var8, 3, var6);
      }
   }

   private static int sourceSkillStateColor(int var0) {
      switch (var0) {
         case 0 -> {
            return 1410141;
         }
         case 1 -> {
            return 1472666;
         }
         case 2 -> {
            return 11889441;
         }
         case 3 -> {
            return 7754138;
         }
         default -> {
            return 6845562;
         }
      }
   }

   private static void drawSkillDashboardSoftkey(VqsvGameRuntime.Scene var0, Graphics2D var1, boolean var2, String var3) {
      int var4 = var2 ? 0 : 177;
      byte var5 = 63;
      var1.setColor(new Color(409441));
      var1.fillRect(var4, 294, var5, 26);
      var1.setColor(new Color(16172879));
      var1.drawRect(var4, 294, var5 - 1, 25);
      drawSkillDashboardText(var0, var1, var3, var4 + 2, 301, var5 - 4, true, 16777215);
   }

   private static void drawSkillDashboardText(VqsvGameRuntime.Scene var0, Graphics2D var1, String var2, int var3, int var4, int var5, boolean var6, int var7) {
      if (var2 != null && !var2.isEmpty() && var5 > 0) {
         String var8 = TextBox.decodeMojibake(var2);
         String var9 = fitSkillDashboardText(var0, var8, var5);
         int var10 = var6 ? var3 + Math.max(0, (var5 - var0.font.taggedWidth(var9)) / 2) : var3;
         Shape var11 = var1.getClip();
         var1.clipRect(var3, var4 - 1, var5, 13);
         var0.font.drawTaggedLine(var1, var9, var10, var4, TextBox.visibleLength(var9), var7);
         var1.setClip(var11);
      }
   }

   private static String fitSkillDashboardText(VqsvGameRuntime.Scene var0, String var1, int var2) {
      if (var0.font.taggedWidth(var1) <= var2) {
         return var1;
      } else {
         String var3 = "..";

         int var4;
         for(var4 = var1.length(); var4 > 0; --var4) {
            UiFont var10000 = var0.font;
            String var10001 = var1.substring(0, var4);
            if (var10000.taggedWidth(var10001 + var3) <= var2) {
               break;
            }
         }

         String var5 = var1.substring(0, var4);
         return var5 + var3;
      }
   }

   private static void drawSkillBand(Graphics2D var0, VqsvUiLayout var1, int var2, int var3, int var4) {
      VqsvUiLayout.UiWidget var5 = var1.widget(var2);
      if (var5 != null) {
         int var6 = var5.jColor != 0 && var5.jColor != -1 ? var5.jColor & 16777215 : var4;
         var0.setColor(new Color(var6));
         var0.fillRect(var5.x, var5.y, Math.max(1, var5.w), Math.max(1, var1.bandHeight(var2, var3)));
      }
   }

   private static void drawSkillCell(SpriteAnimator var0, Graphics2D var1, VqsvUiLayout var2, int var3, boolean var4) {
      VqsvUiLayout.UiWidget var5 = var2.widget(var3);
      if (var5 != null) {
         int var6 = var4 && var5.altId >= 0 ? var5.altId : var5.imageId;
         if (var6 < 0) {
            var6 = var5.altId;
         }

         drawCellTopLeft(var0, var1, var6, var5.x, var5.y);
      }
   }

   private static void drawSkillPetSprite(Graphics2D var0, VqsvUiLayout var1, int var2) {
      VqsvUiLayout.UiWidget var3 = var1.widget(16);
      if (var3 != null && var2 >= 0) {
         SpriteAnimator var4 = SpriteAnimator.load(var2);
         var4.setState(0);
         var4.setCursor(0);
         Shape var5 = var0.getClip();
         var0.clipRect(var3.x, var3.y, Math.max(1, var3.w), Math.max(1, var1.h(16, 88)));
         var4.drawAligned(var0, var3.x, var3.y, Math.max(1, var3.w), Math.max(1, var1.h(16, 88)), 7, 0);
         var0.setClip(var5);
      }
   }

   private static void drawSkillWidgetText(VqsvGameRuntime.Scene var0, Graphics2D var1, VqsvUiLayout var2, int var3, String var4, int var5, boolean var6, int var7) {
      VqsvUiLayout.UiWidget var8 = var2.widget(var3);
      if (var8 != null && var4 != null && !var4.isEmpty()) {
         String var9 = TextBox.decodeMojibake(var4);
         int var10 = var2.w(var3, var5);
         Shape var11 = var1.getClip();
         var1.clipRect(var8.x, var8.y - 1, Math.max(1, var10), Math.max(12, var2.h(var3, 13)));
         int var12 = var8.x;
         if (var6 || var8.b == 4) {
            var12 = var8.x + Math.max(0, (var10 - var0.font.taggedWidth(var9)) / 2);
         }

         var0.font.drawTaggedLine(var1, var9, var12, var8.y, TextBox.visibleLength(var9), var7);
         var1.setClip(var11);
      }
   }

   private static int sourceWidgetColor(VqsvUiLayout var0, int var1, int var2) {
      VqsvUiLayout.UiWidget var3 = var0.widget(var1);
      return var3 != null && var3.lColor != 0 && var3.lColor != -1 ? var3.lColor & 16777215 : var2;
   }

   private static int sourceWidgetJColor(VqsvUiLayout var0, int var1, int var2) {
      VqsvUiLayout.UiWidget var3 = var0.widget(var1);
      return var3 != null && var3.jColor != 0 && var3.jColor != -1 ? var3.jColor & 16777215 : var2;
   }

   private static void renderSourceItemChoiceUi(VqsvGameRuntime.Scene var0, Graphics2D var1) {
      renderSourceChoiceUi(var0, var1, var0.sourceItemChoiceView());
   }

   private static void renderSourceReleaseConfirm(VqsvGameRuntime.Scene var0, Graphics2D var1) {
      VqsvUiLayout var2 = VqsvUiLayout.load("msgconfirm.ui");
      SpriteAnimator var3 = SpriteAnimator.load(257);
      VqsvUiLayout.UiWidget var4 = var2.widget(1);
      int var5 = var4 != null && var4.altId >= 0 ? var4.altId : 124;
      drawCellTopLeft(var3, var1, var5, var2.x(1, 50), var2.y(1, 137));
      drawPromptMessageText(var0, var1, var2, var0.sourceReleaseConfirmMessage, sourceWidgetColor(var2, 4, 1862801));
      drawPromptSoftkeyBackground(var3, var1, var2, 2, 15, 1, 296);
      drawPromptSoftkeyText(var0, var1, var2, 2, var0.sourceReleaseConfirmAction, true, sourceWidgetColor(var2, 2, 16777215));
      drawPromptSoftkeyBackground(var3, var1, var2, 3, 15, 196, 296);
      drawPromptSoftkeyText(var0, var1, var2, 3, "Quay lại", false, sourceWidgetColor(var2, 3, 16777215));
   }

   private static void renderSourceChoiceUi(VqsvGameRuntime.Scene var0, Graphics2D var1, VqsvChoiceUiView var2) {
      VqsvUiLayout var3 = VqsvUiLayout.load("choice.ui");
      SpriteAnimator var4 = SpriteAnimator.load(257);
      drawSkillBand(var1, var3, 4, 8, 13038079);
      drawSkillBand(var1, var3, 2, 160, 12444911);
      drawSkillBand(var1, var3, 3, 14, 8571643);
      drawSkillBand(var1, var3, 7, 82, 12444911);
      drawSkillCell(var4, var1, var3, 1, false);
      drawChoiceWidgetText(var0, var1, var3, 8, var2.widgetText(8, "Đạo cụ"), 46, true, sourceWidgetColor(var3, 8, 1862801));
      drawChoiceWidgetText(var0, var1, var3, 9, var2.widgetText(9, "Số lượng"), 36, true, sourceWidgetColor(var3, 9, 1862801));
      int var5 = var2.visibleStart();
      int var6 = var2.visibleCount();
      SpriteAnimator var7 = SpriteAnimator.load(258);

      for(int var8 = 0; var8 < 5; ++var8) {
         int var9 = var5 + var8;
         boolean var10 = var9 == var2.selectedIndex;
         int var11 = 11 + var8 * 5;
         int var12 = 54 + var8;
         int var13 = 13 + var8 * 5;
         int var14 = 14 + var8 * 5;
         drawSkillCell(var4, var1, var3, var11, var10);
         if (var8 < var6) {
            if (var2.rowIconVisible(var8)) {
               int var15 = var3.x(var12, 54);
               int var16 = var3.y(var12, 95 + var8 * 15);
               if (!UnifiedItemIconRenderer.draw(var1, var2.rowIconResource(var8), var15, var16, 13, 13)) {
                  drawCellTopLeft(var7, var1, var2.rowIconCell(var8), var15, var16);
               }
            }

            int var20 = var10 ? sourceWidgetJColor(var3, var13, 16773482) : sourceWidgetColor(var3, var13, 1862801);
            drawChoiceWidgetText(var0, var1, var3, var13, var2.widgetText(var13, ""), 72, false, var20);
            int var21 = var10 ? sourceWidgetJColor(var3, var14, var20) : sourceWidgetColor(var3, var14, 1862801);
            drawChoiceWidgetText(var0, var1, var3, var14, var2.widgetText(var14, ""), 36, true, var21);
         }
      }

      if (var2.size() > 5) {
         drawSkillBand(var1, var3, 50, 72, 5363945);
         int var17 = var2.scrollbarThumbY(var3.y(50, 98), 72);
         VqsvUiLayout.UiWidget var19 = var3.widget(51);
         var1.setColor(new Color(sourceWidgetJColor(var3, 51, 13038079)));
         var1.fillRect(var3.x(51, 183), var17, var3.w(51, 4), Math.max(1, var19 == null ? 8 : var3.h(51, 8)));
      }

      if (var2.size() == 0) {
         drawChoiceWidgetText(var0, var1, var3, 13, "...", 72, true, sourceWidgetColor(var3, 13, 1862801));
      }

      String var18 = var2.selectedDescription();
      if (!var18.isEmpty()) {
         drawSkillCell(var4, var1, var3, 52, false);
         drawChoiceDescriptionText(var0, var1, var18);
      }

      drawSkillCell(var4, var1, var3, 59, false);
      drawChoiceWidgetText(var0, var1, var3, 59, var2.widgetText(59, "Sử dụng"), 43, true, sourceWidgetColor(var3, 59, 16777215));
      drawSkillCell(var4, var1, var3, 60, false);
      drawChoiceWidgetText(var0, var1, var3, 60, var2.widgetText(60, "Quay lại"), 43, true, sourceWidgetColor(var3, 60, 16777215));
   }

   private static void drawChoiceWidgetText(VqsvGameRuntime.Scene var0, Graphics2D var1, VqsvUiLayout var2, int var3, String var4, int var5, boolean var6, int var7) {
      VqsvUiLayout.UiWidget var8 = var2.widget(var3);
      if (var8 != null && var4 != null && !var4.isEmpty()) {
         String var9 = TextBox.decodeMojibake(var4);
         int var10 = var2.w(var3, var5);
         Shape var11 = var1.getClip();
         var1.clipRect(var8.x, var8.y - 1, Math.max(1, var10), Math.max(12, var2.h(var3, 13)));
         int var12 = var8.x;
         if (var6 || var8.b == 4) {
            var12 = var8.x + Math.max(0, (var10 - var0.font.taggedWidth(var9)) / 2);
         }

         var0.font.drawTaggedLine(var1, var9, var12, var8.y, TextBox.visibleLength(var9), var7);
         var1.setClip(var11);
      }
   }

   private static void drawChoiceDescriptionText(VqsvGameRuntime.Scene var0, Graphics2D var1, String var2) {
      if (var2 != null && !var2.isEmpty()) {
         String var3 = TextBox.decodeMojibake(var2).trim();
         if (!var3.isEmpty()) {
            List var4 = wrapChoiceDescription(var0, var3, 128, 2);
            Shape var5 = var1.getClip();
            var1.clipRect(56, 180, 128, 26);

            for(int var6 = 0; var6 < var4.size(); ++var6) {
               String var7 = (String)var4.get(var6);
               int var8 = 56 + Math.max(0, (128 - var0.font.taggedWidth(var7)) / 2);
               var0.font.drawTaggedLine(var1, var7, var8, 181 + var6 * 12, TextBox.visibleLength(var7), 1862801);
            }

            var1.setClip(var5);
         }
      }
   }

   private static List<String> wrapChoiceDescription(VqsvGameRuntime.Scene var0, String var1, int var2, int var3) {
      ArrayList var4 = new ArrayList();
      StringBuilder var5 = new StringBuilder();

      for(String var9 : var1.split("\\s+")) {
         if (!var9.isEmpty()) {
            String var10 = var5.length() == 0 ? var9 : String.valueOf(var5) + " " + var9;
            if (var5.length() > 0 && var0.font.taggedWidth(var10) > var2 && var4.size() + 1 < var3) {
               var4.add(var5.toString());
               var5.setLength(0);
               var5.append(var9);
            } else {
               var5.setLength(0);
               var5.append(var10);
            }
         }
      }

      if (var5.length() > 0 && var4.size() < var3) {
         var4.add(var5.toString());
      }

      return var4;
   }

   private static void drawSavePromptText(VqsvGameRuntime.Scene var0, Graphics2D var1, String var2, VqsvUiLayout var3) {
      VqsvUiLayout.UiWidget var4 = var3.widget(2);
      int var5 = var3.x(2, 56);
      int var6 = var3.y(2, 137);
      int var7 = var3.w(2, 138);
      int var8 = var3.h(2, 16);
      int var9 = var4 != null && var4.lColor != 0 && var4.lColor != -1 ? var4.lColor & 16777215 : 1862801;
      Shape var10 = var1.getClip();
      var1.clipRect(var5, var6, var7, Math.max(1, var8));
      String var11 = TextBox.decodeMojibake(var2);
      int var12 = var0.font.taggedWidth(var11);
      int var13 = var5 + Math.max(0, (var7 - var12) / 2);
      var0.font.drawTaggedLine(var1, var11, var13, var6, TextBox.visibleLength(var11), var9);
      var1.setClip(var10);
   }

   private static void drawSavePromptWidgetCell(SpriteAnimator var0, Graphics2D var1, VqsvUiLayout var2, int var3, int var4, int var5, int var6) {
      VqsvUiLayout.UiWidget var7 = var2.widget(var3);
      int var8 = var7 != null && var7.altId >= 0 ? var7.altId : var4;
      drawCellTopLeft(var0, var1, var8, var2.x(var3, var5), var2.y(var3, var6));
   }

   private static void drawPromptMessageText(VqsvGameRuntime.Scene var0, Graphics2D var1, VqsvUiLayout var2, String var3, int var4) {
      VqsvUiLayout.UiWidget var5 = var2.widget(4);
      if (var5 != null && var3 != null && !var3.isEmpty()) {
         String var6 = TextBox.decodeMojibake(var3);
         int var7 = var2.x(4, 50);
         int var8 = var2.y(4, 137);
         int var9 = var2.w(4, 150);
         byte var10 = 28;
         int var11 = var7 + Math.max(0, (var9 - var0.font.taggedWidth(var6)) / 2);
         int var12 = var8 + Math.max(0, (var10 - 10) / 2);
         Shape var13 = var1.getClip();
         var1.clipRect(var7, var8, var9, var10);
         var0.font.drawTaggedLine(var1, var6, var11, var12, TextBox.visibleLength(var6), var4);
         var1.setClip(var13);
      }
   }

   private static void drawPromptSoftkeyText(VqsvGameRuntime.Scene var0, Graphics2D var1, VqsvUiLayout var2, int var3, String var4, boolean var5, int var6) {
      VqsvUiLayout.UiWidget var7 = var2.widget(var3);
      if (var7 != null && var4 != null && !var4.isEmpty()) {
         String var8 = TextBox.decodeMojibake(var4);
         int var9 = var0.font.taggedWidth(var8);
         int var10 = Math.max(1, var2.w(var3, 44));
         int var11 = Math.max(14, var2.h(var3, 13));
         BufferedImage var12 = new BufferedImage(Math.max(1, var9 + 4), var11 + 4, 2);
         Graphics2D var13 = var12.createGraphics();
         var13.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
         var13.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
         var0.font.drawTaggedLine(var13, var8, 2, 2, TextBox.visibleLength(var8), var6);
         var13.dispose();
         int[] var14 = opaqueBounds(var12);
         if (var14 != null) {
            BufferedImage var15 = var12.getSubimage(var14[0], var14[1], var14[2], var14[3]);
            int var16 = var15.getWidth();
            int var17 = var15.getHeight();
            int var18 = Math.max(1, var10 - 2);
            int var19 = Math.max(1, var11 - 2);
            if (var16 > var18) {
               var17 = Math.max(1, var17 * var18 / var16);
               var16 = var18;
            }

            if (var17 > var19) {
               var16 = Math.max(1, var16 * var19 / var17);
               var17 = var19;
            }

            int var20 = var7.x + Math.max(0, (var10 - var16) / 2);
            int var21 = var7.y + Math.max(0, (var11 - var17) / 2);
            Shape var22 = var1.getClip();
            var1.clipRect(var7.x, var7.y - 1, var10, var11);
            var1.drawImage(var15, var20, var21, var16, var17, (ImageObserver)null);
            var1.setClip(var22);
         }
      }
   }

   private static int[] opaqueBounds(BufferedImage var0) {
      int var1 = var0.getWidth();
      int var2 = var0.getHeight();
      int var3 = -1;
      int var4 = -1;

      for(int var5 = 0; var5 < var0.getHeight(); ++var5) {
         for(int var6 = 0; var6 < var0.getWidth(); ++var6) {
            if ((var0.getRGB(var6, var5) >>> 24 & 255) != 0) {
               var1 = Math.min(var1, var6);
               var2 = Math.min(var2, var5);
               var3 = Math.max(var3, var6);
               var4 = Math.max(var4, var5);
            }
         }
      }

      if (var3 >= var1 && var4 >= var2) {
         return new int[]{var1, var2, var3 - var1 + 1, var4 - var2 + 1};
      } else {
         return null;
      }
   }

   private static void drawPromptSoftkeyBackground(SpriteAnimator var0, Graphics2D var1, VqsvUiLayout var2, int var3, int var4, int var5, int var6) {
      VqsvUiLayout.UiWidget var7 = var2.widget(var3);
      int var8 = var2.x(var3, var5);
      int var9 = var2.y(var3, var6);
      int var10 = Math.max(1, var2.w(var3, 44));
      int var11 = Math.max(14, var2.h(var3, 16));
      if (var7 != null && var7.altId >= 0 && var7.altMode == 3) {
         var0.setState(var7.altId);
         var0.drawAligned(var1, var8, var9, var10, var11, var7.c, 0);
      } else if (var7 != null && var7.altId >= 0 && var7.altMode == 2) {
         drawSavePromptWidgetCell(var0, var1, var2, var3, var4, var5, var6);
      } else {
         var1.setColor(new Color(528464));
         var1.fillRect(var8, var9, var10, var11);
         var1.setColor(new Color(1812712));
         var1.drawRect(var8, var9, var10 - 1, var11 - 1);
      }
   }

   private static void drawCellTopLeft(SpriteAnimator var0, Graphics2D var1, int var2, int var3, int var4) {
      int[] var5 = var0.cellBounds(var2);
      if (var5 != null) {
         var0.drawCell(var1, var2, var3 - var5[0], var4 - var5[1], 0);
      }
   }

   private static void drawConfirmSoftkey(Graphics2D var0, int var1, int var2, boolean var3) {
      Stroke var4 = var0.getStroke();
      var0.setColor(new Color(1812712));
      var0.fillOval(var1, var2, 24, 24);
      var0.setColor(Color.WHITE);
      var0.setStroke(new BasicStroke(4.0F, 1, 1));
      if (var3) {
         var0.drawLine(var1 + 5, var2 + 13, var1 + 10, var2 + 18);
         var0.drawLine(var1 + 10, var2 + 18, var1 + 20, var2 + 6);
      } else {
         var0.drawLine(var1 + 7, var2 + 7, var1 + 17, var2 + 17);
         var0.drawLine(var1 + 17, var2 + 7, var1 + 7, var2 + 17);
      }

      var0.setStroke(var4);
   }
}
