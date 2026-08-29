package vqsv.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import vqsv.battle.data.BattleItemRow;
import vqsv.battle.data.BattleSkillRow;
import vqsv.battle.data.BattleSpeciesRow;
import vqsv.battle.data.VqsvBattleAnimationTables;
import vqsv.battle.data.VqsvBattleTables;
import vqsv.battle.ui.BattleStatusPresentation;
import vqsv.battle.ui.PetStateDetailUiState;
import vqsv.battle.ui.RainbowSkillAnimationCatalog;
import vqsv.battle.ui.RainbowSkillAnimationView;
import vqsv.battle.ui.VqsvBattleCatchRenderState;
import vqsv.battle.ui.VqsvBattleCommandRenderState;
import vqsv.battle.ui.VqsvBattleLevelUpView;
import vqsv.battle.ui.VqsvBattleNoticeRenderState;
import vqsv.battle.ui.VqsvBattleNpcEnemyEntryRenderState;
import vqsv.battle.ui.VqsvBattlePetStateView;
import vqsv.battle.ui.VqsvBattleRenderState;
import vqsv.battle.ui.VqsvBattleShopConfirmRenderState;
import vqsv.battle.ui.VqsvBattleSkillRenderState;
import vqsv.battle.ui.VqsvBattleTargetRenderState;
import vqsv.battle.ui.VqsvBattleUiMode;
import vqsv.battle.ui.VqsvDoubleBattlePresentationState;
import vqsv.battle.ui.VqsvMsgWarmView;
import vqsv.charm.RainbowCharmCatalog;
import vqsv.core.GameConfig;
import vqsv.data.ItemDefinition;
import vqsv.fashion.SourceFashionCatalog;
import vqsv.fashion.SourceFashionRecord;
import vqsv.gameplay.PetQualityUpgradeService;
import vqsv.pet.PetState;
import vqsv.pet.data.TanNguyetLongMaCatalog;
import vqsv.pet.data.UnifiedPetCatalog;
import vqsv.pet.data.UnifiedV4PetCatalog;
import vqsv.progression.EvolutionCandidate;
import vqsv.render.MidpTransform;
import vqsv.render.SpriteAnimator;
import vqsv.render.UnifiedItemIconRenderer;
import vqsv.resource.AssetPaths;
import vqsv.resource.ImageLoader;
import vqsv.text.VqsvText;
import vqsv.ui.layout.UiScrollbarMath;
import vqsv.ui.layout.VqsvUiLayout;
import vqsv.ui.text.TextBox;
import vqsv.ui.text.UiFont;

final class VqsvBattleRenderer {
   private static final int W = 240;
   private static final int H = 320;
   private static final int ENEMY_RECT_X = 132;
   private static final int ENEMY_RECT_Y = 70;
   private static final int ENEMY_RECT_W = 96;
   private static final int ENEMY_RECT_H = 118;
   private static final int PLAYER_RECT_X = 18;
   private static final int PLAYER_RECT_Y = 140;
   private static final int PLAYER_RECT_W = 96;
   private static final int PLAYER_RECT_H = 95;
   private static final int JAVA_ME_EFFECT_TRANSPARENT_KEY = 16777215;
   private static final Color SOURCE_UI_TEXT = new Color(1862801);
   private static final Map<Integer, BufferedImage> TEX_CACHE = new HashMap();
   private static final Map<String, BufferedImage> STATUS_ICON_CACHE = new HashMap();
   private static final BufferedImage[] BATTLE_ELEMENT_ICONS = new BufferedImage[7];

   private VqsvBattleRenderer() {
   }

   static void render(VqsvGameRuntime.Scene var0, Graphics2D var1) {
      if (var0.session.runtime.battleOverlayTicks > 0) {
         renderSourceLikeBattleUi(var0, var1);
      }
   }

   static void renderSourceLikeBattleUi(VqsvGameRuntime.Scene var0, Graphics2D var1) {
      VqsvBattleUiMode var2 = var0.session.runtime.battleUiMode;
      if (var2 != VqsvBattleUiMode.NPC_ENEMY && !npcEnemyEntryRenderState(var0).visibleEntry) {
         boolean var3 = var2 == VqsvBattleUiMode.PET_STATE;
         boolean var4 = var2 == VqsvBattleUiMode.LEVEL_UP && (var0.battleLevelUpView == null || var0.battleLevelUpView.leveled);
         boolean var5 = var3 || var4;
         drawBattleBackground(var1, var0);
         if (!var5) {
            boolean var6 = battleRenderState(var0).simultaneous2v2();
            if (!var6 || battleRenderState(var0).enemy.hp > 0) {
               drawBattleUiCellTopLeft(var1, 92, 0, 0);
            }

            if (!var6 || battleRenderState(var0).player.hp > 0) {
               drawBattleUiCellTopLeft(var1, 93, 0, 235);
            }

            var1.setColor(new Color(46552));
            var1.fillRect(91, 248, 10, 10);
         }

         drawBattleGroundMarkers(var1, var0);
         drawP7SpecialEffect(var1, var0);
         if (battleRenderState(var0).simultaneous2v2()) {
            drawSimultaneousFormationActors(var1, var0);
         } else {
            drawBattleActorWithAttachedEffects(var1, var0, false);
            drawBattleActorWithAttachedEffects(var1, var0, true);
         }

         drawRainbowSkillAnimation(var1, var0);
         drawP7DeathEffect(var1, var0);
         if (!var5) {
            drawBattleUiCellTopLeft(var1, 101, 97, 14);
         }

         if (var2 == VqsvBattleUiMode.COMMAND) {
            drawBattleCommandBar(var1, var0.font, commandRenderState(var0).selectedIndex);
         }

         if (!var5) {
            drawBattleHudWidgets(var1, var0.font, var0);
            if (battleRenderState(var0).simultaneous2v2()) {
               drawSecondaryDoubleBattleHudWidgets(var1, var0.font, var0);
            }

            drawRainbowCharmHud(var1, var0.font, var0);
            drawPvpTurnTimer(var1, var0.font, var0);
         }

         if (var2 != VqsvBattleUiMode.SHOP_BUY && var2 != VqsvBattleUiMode.SHOP_CONFIRM) {
            if (var2 == VqsvBattleUiMode.CHOICE) {
               drawChoiceOverlay(var1, var0.font, var0);
            } else if (var2 == VqsvBattleUiMode.CHOICE_SKILL) {
               drawChoiceSkillOverlay(var1, var0.font, var0);
            } else if (var2 == VqsvBattleUiMode.PET_STATE) {
               renderPetStateOverlay(var1, var0.font, var0, true);
            } else if (var2 == VqsvBattleUiMode.TARGET) {
               drawTargetCursor(var1, var0.font, var0);
            } else if (var2 == VqsvBattleUiMode.SMS_INFO) {
               drawSmsInfoOverlay(var1, var0.font, var0);
            } else if (var2 == VqsvBattleUiMode.WARNING && !hasSourceTextBox(var0, 3)) {
               drawWarningOverlay(var1, var0.font, var0);
            } else if (var2 == VqsvBattleUiMode.LEVEL_UP) {
               drawLevelUpOverlay(var1, var0.font, var0);
            }
         } else {
            drawShopBuyOverlay(var1, var0.font, var0);
            if (var2 == VqsvBattleUiMode.SHOP_CONFIRM) {
               drawShopConfirmOverlay(var1, var0.font, var0);
            }
         }

         VqsvBattleCatchRenderState var7 = catchRenderState(var0);
         if (var7.visible && var7.spriteId >= 0) {
            drawCatchAnimation(var1, var0, var7);
         }

         if (var0.battleP7DamageVisible) {
            drawP7Damage(var1, var0.font, var0);
         }

         if (var0.battleP7PostEffectVisible) {
            drawP7PostEffect(var1, var0.font, var0);
         }

      } else {
         drawNpcEnemyEntryOverlay(var1, var0.font, var0);
      }
   }

   private static void drawRainbowCharmHud(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2) {
      int[] var3 = new int[]{var2.session.progression.rainbowCharms.activeId(RainbowCharmCatalog.Slot.SURVIVAL), var2.session.progression.rainbowCharms.activeId(RainbowCharmCatalog.Slot.TACTICAL)};
      byte var4 = 101;
      byte var5 = 20;

      for(int var6 = 0; var6 < var3.length; ++var6) {
         int var7 = var3[var6];
         RainbowCharmCatalog.Definition var8 = RainbowCharmCatalog.instance().byRuntimeId(var7);
         if (var8 != null) {
            int var9 = var2.session.progression.rainbowCharms.tier(var7);
            int var10 = var4 + var6 * 23;
            var0.setColor(new Color(0, 0, 0, 180));
            var0.fillRoundRect(var10 - 2, var5, 20, 23, 4, 4);
            var0.setColor(new Color(var6 == 0 ? 6607523 : 15911010));
            var0.drawRoundRect(var10 - 2, var5, 19, 22, 4, 4);
            UnifiedItemIconRenderer.draw(var0, var8.item().iconResource, var10, var5 + 2, 14, 14);
            String var11 = RainbowCharmCatalog.romanTier(var9);
            var1.drawTaggedLine(var0, var11, var10 + 4, var5 + 15, TextBox.visibleLength(var11), 16777215);
         }
      }

   }

   private static boolean hasSourceTextBox(VqsvGameRuntime.Scene var0, int var1) {
      return var0.text != null && var0.text.sourceUiKind == var1;
   }

   private static void drawBattleBackground(Graphics2D var0, VqsvGameRuntime.Scene var1) {
      if (var1.battleBackgroundSnapshot != null) {
         var0.drawImage(var1.battleBackgroundSnapshot, 0, 0, (ImageObserver)null);
         var0.setColor(new Color(0, 0, 0, 140));
         var0.fillRect(0, 0, 240, 320);
      } else {
         var0.setColor(Color.BLACK);
         var0.fillRect(0, 0, 240, 320);
      }
   }

   private static void clearBattleBackgroundRegion(Graphics2D var0, VqsvGameRuntime.Scene var1, int var2, int var3, int var4, int var5) {
      Shape var6 = var0.getClip();
      var0.clipRect(var2, var3, var4, var5);
      drawBattleBackground(var0, var1);
      var0.setClip(var6);
   }

   private static void drawBattleUiCellTopLeft(Graphics2D var0, int var1, int var2, int var3) {
      SpriteAnimator var4 = SpriteAnimator.load(257);
      int[] var5 = var4.cellBounds(var1);
      if (var5 != null && var5[2] > 0 && var5[3] > 0) {
         var4.drawCell(var0, var1, var2 - var5[0], var3 - var5[1], 0);
      }
   }

   private static void drawSpriteCellTopLeft(Graphics2D var0, int var1, int var2, int var3, int var4) {
      SpriteAnimator var5 = SpriteAnimator.load(var1);
      int[] var6 = var5.cellBounds(var2);
      if (var6 != null && var6[2] > 0 && var6[3] > 0) {
         var5.drawCell(var0, var2, var3 - var6[0], var4 - var6[1], 0);
      }
   }

   private static void drawBattleUiStateTopLeft(Graphics2D var0, int var1, int var2, int var3) {
      SpriteAnimator var4 = SpriteAnimator.load(257);
      var4.setState(var1);
      int[] var5 = var4.animationBounds(var1);
      if (var5 != null && var5[2] > 0 && var5[3] > 0) {
         var4.draw(var0, var2 - var5[0], var3 - var5[1], 0);
      }
   }

   private static void drawBattleGroundMarkers(Graphics2D var0, VqsvGameRuntime.Scene var1) {
      VqsvBattleRenderState var2 = battleRenderState(var1);
      VqsvBattleRenderState.MarkerState var3 = var2.markers;
      if (var3.groundVisible) {
         SpriteAnimator var4 = SpriteAnimator.load(294);
         var4.setState(0);
         var4.setCursor(0);
         if (!var2.simultaneous2v2()) {
            drawSourceMarker(var0, var4, var1, false, false);
            drawSourceMarker(var0, var4, var1, true, false);
            if (var3.activeVisible) {
               var4.setState(1);
               var4.setCursor(Math.max(0, var1.battleAnimationTick / 2 % 2));
               drawSourceMarker(var0, var4, var1, var3.activePlayerSide, true);
            }
         } else {
            for(int var5 = 0; var5 < var2.formationSlots.length; ++var5) {
               drawFormationMarker(var0, var4, var1, var5);
            }

            if (var3.activeVisible && var2.activeActorSlot >= 0) {
               var4.setState(1);
               var4.setCursor(Math.max(0, var1.battleAnimationTick / 2 % 2));
               drawFormationMarker(var0, var4, var1, var2.activeActorSlot);
            }

         }
      }
   }

   private static void drawSourceMarker(Graphics2D var0, SpriteAnimator var1, VqsvGameRuntime.Scene var2, boolean var3, boolean var4) {
      int var5 = sourceBattleMarkerX(var2, var3) + sideOffsetX(var2, var3);
      int var6 = sourceBattleMarkerY(var2, var3) + sideOffsetY(var2, var3);
      if (var4) {
         VqsvBattleRenderState.MarkerState var7 = battleRenderState(var2).markers;
         var5 = (var3 ? var7.playerX : var7.enemyX) + sideOffsetX(var2, var3);
         var6 = (var3 ? var7.playerY : var7.enemyY) + sideOffsetY(var2, var3);
      }

      var1.draw(var0, var5, var6, 0);
   }

   private static void drawFormationMarker(Graphics2D var0, SpriteAnimator var1, VqsvGameRuntime.Scene var2, int var3) {
      var1.draw(var0, formationMarkerX(var2, var3), formationMarkerY(var2, var3), 0);
   }

   private static int formationActorX(VqsvGameRuntime.Scene var0, int var1) {
      VqsvDoubleBattlePresentationState var2 = battleRenderState(var0).doubleBattlePresentation;
      if (var2.hasSlot(var1)) {
         return var2.slotAt(var1).actorX;
      } else {
         short[] var3 = VqsvBattleAnimationTables.instance().posRow(sourceCposGroup(var0));
         int var4 = Math.max(0, var1) << 2;
         return var3.length >= var4 + 4 ? var3[var4] : (var1 < 2 ? 177 : 70);
      }
   }

   private static int formationActorY(VqsvGameRuntime.Scene var0, int var1) {
      VqsvDoubleBattlePresentationState var2 = battleRenderState(var0).doubleBattlePresentation;
      if (var2.hasSlot(var1)) {
         return var2.slotAt(var1).actorY;
      } else {
         short[] var3 = VqsvBattleAnimationTables.instance().posRow(sourceCposGroup(var0));
         int var4 = Math.max(0, var1) << 2;
         return var3.length >= var4 + 4 ? var3[var4 + 1] : (var1 < 2 ? 103 : 223);
      }
   }

   private static int formationMarkerX(VqsvGameRuntime.Scene var0, int var1) {
      VqsvDoubleBattlePresentationState var2 = battleRenderState(var0).doubleBattlePresentation;
      if (var2.hasSlot(var1)) {
         return var2.slotAt(var1).markerX;
      } else {
         short[] var3 = VqsvBattleAnimationTables.instance().posRow(sourceCposGroup(var0));
         int var4 = Math.max(0, var1) << 2;
         return var3.length >= var4 + 4 ? var3[var4 + 2] : (var1 < 2 ? 144 : 36);
      }
   }

   private static int formationMarkerY(VqsvGameRuntime.Scene var0, int var1) {
      VqsvDoubleBattlePresentationState var2 = battleRenderState(var0).doubleBattlePresentation;
      if (var2.hasSlot(var1)) {
         return var2.slotAt(var1).markerY;
      } else {
         short[] var3 = VqsvBattleAnimationTables.instance().posRow(sourceCposGroup(var0));
         int var4 = Math.max(0, var1) << 2;
         return var3.length >= var4 + 4 ? var3[var4 + 3] : (var1 < 2 ? 85 : 206);
      }
   }

   private static int sourceBattleActorX(VqsvGameRuntime.Scene var0, boolean var1) {
      short[] var2 = VqsvBattleAnimationTables.instance().posRow(sourceCposGroup(var0));
      int var3 = sourcePosQuadOffset(var0, var1);
      if (var2.length >= var3 + 4) {
         return var2[var3];
      } else {
         return var1 ? 70 : 177;
      }
   }

   private static int sourceBattleActorY(VqsvGameRuntime.Scene var0, boolean var1) {
      short[] var2 = VqsvBattleAnimationTables.instance().posRow(sourceCposGroup(var0));
      int var3 = sourcePosQuadOffset(var0, var1);
      if (var2.length >= var3 + 4) {
         return var2[var3 + 1];
      } else {
         return var1 ? 223 : 103;
      }
   }

   private static int sourceBattleMarkerX(VqsvGameRuntime.Scene var0, boolean var1) {
      short[] var2 = VqsvBattleAnimationTables.instance().posRow(sourceCposGroup(var0));
      int var3 = sourcePosQuadOffset(var0, var1);
      if (var2.length >= var3 + 4) {
         return var2[var3 + 2];
      } else {
         return var1 ? 36 : 144;
      }
   }

   private static int sourceBattleMarkerY(VqsvGameRuntime.Scene var0, boolean var1) {
      short[] var2 = VqsvBattleAnimationTables.instance().posRow(sourceCposGroup(var0));
      int var3 = sourcePosQuadOffset(var0, var1);
      if (var2.length >= var3 + 4) {
         return var2[var3 + 3];
      } else {
         return var1 ? 206 : 85;
      }
   }

   private static int sourceP8ExpAnchorX(VqsvGameRuntime.Scene var0) {
      short[] var1 = VqsvBattleAnimationTables.instance().posRow(sourceCposGroup(var0));
      int var2 = sourcePosQuadOffset(var0, true);
      return var1.length >= var2 + 2 ? var1[var2] : 70;
   }

   private static int sourceP8ExpAnchorY(VqsvGameRuntime.Scene var0) {
      short[] var1 = VqsvBattleAnimationTables.instance().posRow(sourceCposGroup(var0));
      int var2 = sourcePosQuadOffset(var0, true);
      return var1.length >= var2 + 2 ? var1[var2 + 1] : 223;
   }

   private static int sourcePosQuadOffset(VqsvGameRuntime.Scene var0, boolean var1) {
      if (sourceCposGroup(var0) == 1) {
         return var1 ? 8 : 0;
      } else {
         return var1 ? 4 : 0;
      }
   }

   private static int sourceCposGroup(VqsvGameRuntime.Scene var0) {
      return var0.battleMode == 0 ? (var0.battleBackgroundMode == 1 ? 2 : 0) : 1;
   }

   static int sourceBattleOrientation(boolean var0) {
      return var0 ? 1 : 0;
   }

   static int battlePetOrientation(int var0, boolean var1) {
      // Tàn Nguyệt Long Ma's source art faces right. In battle the boss is
      // always presented facing the player's side, so mirror it to the left.
      if (TanNguyetLongMaCatalog.isVisual(var0)) {
         return 1;
      }
      return !UnifiedPetCatalog.instance().isRainbowVisual(var0) && !UnifiedV4PetCatalog.isV4Visual(var0) ? 0 : sourceBattleOrientation(var1);
   }

   private static int battleSpriteAnchorX(VqsvGameRuntime.Scene var0, boolean var1) {
      return sourceBattleActorX(var0, var1) + sideOffsetX(var0, var1);
   }

   private static int battleSpriteAnchorY(VqsvGameRuntime.Scene var0, boolean var1) {
      return sourceBattleActorY(var0, var1) + sideOffsetY(var0, var1);
   }

   private static int battleSlotAnchorX(VqsvGameRuntime.Scene var0, boolean var1) {
      return sourceBattleActorX(var0, var1);
   }

   private static int battleSlotAnchorY(VqsvGameRuntime.Scene var0, boolean var1) {
      return sourceBattleActorY(var0, var1);
   }

   private static int battleSpriteAnchor(int var0, int var1, int var2, int var3, boolean var4) {
      if (var0 < 0) {
         return var2;
      } else {
         SpriteAnimator var5 = SpriteAnimator.load(var0);
         int[] var6 = var5.animationBounds(Math.max(0, var1));
         if (var6 == null) {
            return var2;
         } else {
            int var7 = var4 ? var6[0] : var6[1];
            int var8 = var4 ? var6[2] : var6[3];
            return var4 ? var2 + (var3 - var8) / 2 - var7 : var2 + (var3 - var8) - var7;
         }
      }
   }

   private static void drawBattleSprite(Graphics2D var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      drawBattleSprite(var0, var1, var2, var3, var4, var5, var6, var7, 0, 0);
   }

   private static void drawBattleSprite(Graphics2D var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9) {
      if (var1 >= 0) {
         SpriteAnimator var10 = SpriteAnimator.load(var1);
         var10.setState(Math.max(0, var8));
         var10.setCursorClamped(var9);
         var10.drawAligned(var0, var2, var3, var4, var5, var6, var7);
      }
   }

   private static void drawBattleSpriteAtSource(Graphics2D var0, int var1, int var2, int var3, int var4, int var5, int var6) {
      if (var1 >= 0) {
         SpriteAnimator var7 = SpriteAnimator.load(var1);
         var7.setState(Math.max(0, var5));
         var7.setCursorClamped(var6);
         var7.draw(var0, var2, var3, var4);
      }
   }

   private static void drawBattlePanel(Graphics2D var0, int var1, int var2, int var3, int var4, boolean var5) {
      if (var5) {
         var0.setColor(new Color(0, 0, 0, 150));
         var0.fillRect(var1, var2, var3, var4);
      }

      var0.setColor(new Color(232, 244, 255));
      var0.drawRect(var1, var2, var3 - 1, var4 - 1);
      var0.setColor(new Color(52, 88, 105));
      var0.drawRect(var1 + 1, var2 + 1, var3 - 3, var4 - 3);
   }

   private static void drawBattleCommandBar(Graphics2D var0, UiFont var1, int var2) {
      String[][] var3 = VqsvText.Battle.COMMAND_LABELS;
      int[] var4 = new int[]{7, 48, 88, 128, 168, 208};
      int[] var5 = new int[]{20, 56, 98, 137, 176, 218};
      if (var2 >= 0 && var2 < var5.length) {
         drawBattleUiCellTopLeft(var0, 31, var5[var2], 293);
      }

      for(int var6 = 0; var6 < var3.length; ++var6) {
         Color var7 = var6 == var2 ? new Color(16773482) : Color.WHITE;
         drawTinyBattleText(var0, var1, var3[var6][0], var4[var6], 299, 34, var7);
         drawTinyBattleText(var0, var1, var3[var6][1], var4[var6], 309, 34, var7);
      }

   }

   private static void drawBattleHudWidgets(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2) {
      VqsvUiLayout var3 = VqsvUiLayout.load("battle.ui");
      VqsvBattleRenderState var4 = battleRenderState(var2);
      VqsvBattleRenderState.UnitState var5 = var4.enemy;
      VqsvBattleRenderState.UnitState var6 = var4.player;
      boolean var7 = var4.simultaneous2v2();
      if (!var7 || var5.hp > 0) {
         drawBattleUiText(var0, var1, var3, 15, var5.name, Color.WHITE, var2.battleAnimationTick);
         drawBattleUiText(var0, var1, var3, 16, "lv" + var5.level, Color.WHITE, var2.battleAnimationTick);
         drawSourcePercentLayer(var0, var3, 42, hpPercent(var5.hp, var5.maxHp), 8);
         drawSourcePercentLayer(var0, var3, 56, hpPercent(var5.hp, var5.maxHp), 8);
         drawSourcePercentLayer(var0, var3, 14, hpPercent(var5.hp, var5.maxHp), 8);
         drawBattleUiText(var0, var1, var3, 39, var5.hp + "/" + var5.maxHp, new Color(16775601), var2.battleAnimationTick);
         if (var5.ownedSpecies) {
            drawBattleUiCellTopLeft(var0, 101, var3.x(19, 97), var3.y(19, 14));
         } else {
            clearBattleBackgroundRegion(var0, var2, var3.x(19, 97) - 1, var3.y(19, 14) - 1, 15, 15);
         }

         drawBattleElementBadge(var0, var5.elementId, var3.x(18, 92), var3.y(18, 2));
         drawStatusSlots(var0, var3.x(32, 2), var3.y(32, 25), var3.x(49, 10), var3.y(49, 30), false, var5.statusIconCells, var5.statusDurationCells, var5.statusPresentations);
      }

      VqsvBattleLevelUpView var8 = normalP8ExpView(var2);
      String var9 = var8 == null ? var6.name : var8.name;
      int var10 = var8 == null ? var6.level : var8.level;
      int var11 = var8 == null ? var6.energy : var8.expValue;
      int var12 = var8 == null ? var6.maxEnergy : var8.expMax;
      int var13 = var8 == null ? hpPercent(var6.energy, var6.maxEnergy) : var8.expPercent;
      int var14 = var8 == null ? var6.elementId : var8.elementId;
      if (!var7 || var6.hp > 0) {
         drawBattleUiText(var0, var1, var3, 12, var9, Color.WHITE, var2.battleAnimationTick);
         drawBattleUiText(var0, var1, var3, 13, "lv" + var10, Color.WHITE, var2.battleAnimationTick);
         drawSourcePercentLayer(var0, var3, 41, hpPercent(var6.hp, var6.maxHp), 8);
         drawSourcePercentLayer(var0, var3, 55, hpPercent(var6.hp, var6.maxHp), 8);
         drawSourcePercentLayer(var0, var3, 11, hpPercent(var6.hp, var6.maxHp), 8);
         drawBattleUiText(var0, var1, var3, 38, var6.hp + "/" + var6.maxHp, new Color(16775601), var2.battleAnimationTick);
         drawSourcePercentLayer(var0, var3, 9, var13, 8);
         drawBattleUiText(var0, var1, var3, 40, var11 + "/" + var12, new Color(16775601), var2.battleAnimationTick);
         drawBattleElementBadge(var0, var14, var3.x(17, 139), var3.y(17, 249));
         drawStatusSlots(var0, var3.x(26, 226), var3.y(26, 221), var3.x(43, 234), var3.y(43, 226), true, var6.statusIconCells, var6.statusDurationCells, var6.statusPresentations);
      }

      if (Boolean.getBoolean("vqsv.battle.debugLog.visible")) {
         drawBattleUiText(var0, var1, var3, 10, var4.log, Color.WHITE, var2.battleAnimationTick);
      }

      if (var2.battleCaptureTutorial) {
         drawBattleUiText(var0, var1, var3, 4, "Bắt", new Color(16775601), var2.battleAnimationTick);
      }

   }

   private static void drawSecondaryDoubleBattleHudWidgets(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2) {
      VqsvBattleRenderState var3 = battleRenderState(var2);
      if (var3.formationSlots.length == 4) {
         VqsvUiLayout var4 = VqsvUiLayout.load("battle.ui");
         drawSecondaryEnemyHud(var0, var1, var4, var3.formationSlots[1], 1, var2.battleAnimationTick);
         drawSecondaryPlayerHud(var0, var1, var4, var3.formationSlots[3], 3, var2.battleAnimationTick);
      }
   }

   private static void drawSecondaryEnemyHud(Graphics2D var0, UiFont var1, VqsvUiLayout var2, VqsvBattleRenderState.UnitState var3, int var4, int var5) {
      drawSecondaryCompactHud(var0, var1, var2, var3, var4, var5, 132, 0);
   }

   private static void drawSecondaryPlayerHud(Graphics2D var0, UiFont var1, VqsvUiLayout var2, VqsvBattleRenderState.UnitState var3, int var4, int var5) {
      drawSecondaryCompactHud(var0, var1, var2, var3, var4, var5, 0, 235);
   }

   private static void drawSecondaryCompactHud(Graphics2D var0, UiFont var1, VqsvUiLayout var2, VqsvBattleRenderState.UnitState var3, int var4, int var5, int var6, int var7) {
      if (var3 != null && var3.hp > 0) {
         Graphics2D var8 = (Graphics2D)var0.create();
         var8.translate(var6, var7);
         drawBattleUiCellTopLeft(var8, 92, 0, 0);
         drawBattleUiText(var8, var1, var2, 15, var3.name, Color.WHITE, var5);
         drawBattleUiText(var8, var1, var2, 16, "lv" + var3.level, Color.WHITE, var5);
         drawSourcePercentLayer(var8, var2, 42, hpPercent(var3.hp, var3.maxHp), 8);
         drawSourcePercentLayer(var8, var2, 56, hpPercent(var3.hp, var3.maxHp), 8);
         drawSourcePercentLayer(var8, var2, 14, hpPercent(var3.hp, var3.maxHp), 8);
         drawBattleUiText(var8, var1, var2, 39, var3.hp + "/" + var3.maxHp, new Color(16775601), var5);
         drawBattleElementBadge(var8, var3.elementId, var2.x(18, 92), var2.y(18, 2));
         var8.dispose();
      }
   }

   private static void drawBattleElementBadge(Graphics2D var0, int var1, int var2, int var3) {
      BufferedImage var4 = battleElementIcon(var1);
      if (var4 != null) {
         int var5 = var2 - 4;
         int var6 = Math.max(0, var3 - 3);
         int var7 = skillElementColor(var1);
         var0.setColor(new Color(0, 23, 36, 218));
         var0.fillRect(var5, var6, 17, 16);
         var0.setColor(new Color(var7));
         var0.drawRect(var5, var6, 16, 15);
         var0.setColor(new Color(lighterColor(var7)));
         var0.drawLine(var5 + 1, var6 + 1, var5 + 15, var6 + 1);
         Object var8 = var0.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
         var0.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
         var0.drawImage(var4, var5 + 1, var6 + 2, 15, 13, (ImageObserver)null);
         if (var8 == null) {
            var0.getRenderingHints().remove(RenderingHints.KEY_INTERPOLATION);
         } else {
            var0.setRenderingHint(RenderingHints.KEY_INTERPOLATION, var8);
         }

      }
   }

   private static BufferedImage battleElementIcon(int var0) {
      if (var0 >= 0 && var0 < BATTLE_ELEMENT_ICONS.length) {
         BufferedImage var1 = BATTLE_ELEMENT_ICONS[var0];
         if (var1 != null) {
            return var1;
         } else {
            SpriteAnimator var2 = SpriteAnimator.load(257);
            int var3 = 94 + var0;
            int[] var4 = var2.cellBounds(var3);
            if (var4 != null && var4[2] > 0 && var4[3] > 0) {
               BufferedImage var5 = new BufferedImage(var4[2], var4[3], 2);
               Graphics2D var6 = var5.createGraphics();
               var2.drawCell(var6, var3, -var4[0], -var4[1], 0);
               var6.dispose();
               BATTLE_ELEMENT_ICONS[var0] = var5;
               return var5;
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private static VqsvBattleRenderState battleRenderState(VqsvGameRuntime.Scene var0) {
      return var0.battleRenderState == null ? VqsvBattleRenderState.EMPTY : var0.battleRenderState;
   }

   private static VqsvBattleCatchRenderState catchRenderState(VqsvGameRuntime.Scene var0) {
      return var0.battleCatchRenderState == null ? VqsvBattleCatchRenderState.EMPTY : var0.battleCatchRenderState;
   }

   private static VqsvBattleTargetRenderState targetRenderState(VqsvGameRuntime.Scene var0) {
      return var0.battleTargetRenderState == null ? VqsvBattleTargetRenderState.EMPTY : var0.battleTargetRenderState;
   }

   private static VqsvBattleSkillRenderState skillRenderState(VqsvGameRuntime.Scene var0) {
      return var0.battleSkillRenderState == null ? VqsvBattleSkillRenderState.EMPTY : var0.battleSkillRenderState;
   }

   private static VqsvBattleShopConfirmRenderState shopConfirmRenderState(VqsvGameRuntime.Scene var0) {
      return var0.battleShopConfirmRenderState == null ? VqsvBattleShopConfirmRenderState.EMPTY : var0.battleShopConfirmRenderState;
   }

   private static VqsvBattleNoticeRenderState noticeRenderState(VqsvGameRuntime.Scene var0) {
      return var0.battleNoticeRenderState == null ? VqsvBattleNoticeRenderState.EMPTY : var0.battleNoticeRenderState;
   }

   private static VqsvBattleCommandRenderState commandRenderState(VqsvGameRuntime.Scene var0) {
      return var0.battleCommandRenderState == null ? VqsvBattleCommandRenderState.EMPTY : var0.battleCommandRenderState;
   }

   private static VqsvBattleNpcEnemyEntryRenderState npcEnemyEntryRenderState(VqsvGameRuntime.Scene var0) {
      return var0.battleNpcEnemyEntryRenderState == null ? VqsvBattleNpcEnemyEntryRenderState.EMPTY : var0.battleNpcEnemyEntryRenderState;
   }

   private static VqsvChoiceUiView choiceRenderState(VqsvGameRuntime.Scene var0) {
      return var0.battleChoiceUi == null ? VqsvChoiceUiView.EMPTY : var0.battleChoiceUi;
   }

   private static VqsvBattleLevelUpView normalP8ExpView(VqsvGameRuntime.Scene var0) {
      VqsvBattleLevelUpView var1 = var0.battleLevelUpView == null ? VqsvBattleLevelUpView.EMPTY : var0.battleLevelUpView;
      return var0.session.runtime.battleUiMode == VqsvBattleUiMode.LEVEL_UP && var1.visible && !var1.leveled ? var1 : null;
   }

   private static void drawBattleUiText(Graphics2D var0, UiFont var1, VqsvUiLayout var2, int var3, String var4, Color var5, int var6) {
      VqsvUiLayout.UiWidget var7 = var2.widget(var3);
      if (var7 != null) {
         drawSourceWidgetText(var0, var1, var4, var7.x, var7.y, Math.max(1, var7.w), sourceWidgetHeight(var7), widgetTextColor(var7, false, var5), var6, var7.b);
      }
   }

   private static void drawSourcePercentLayer(Graphics2D var0, VqsvUiLayout var1, int var2, int var3, int var4) {
      VqsvUiLayout.UiWidget var5 = var1.widget(var2);
      if (var5 != null) {
         int var6 = sourceWidgetHeight(var5);
         if (var6 <= 0) {
            var6 = var4;
         }

         int var7 = Math.max(1, var5.w);
         fillSourceColor(var0, var5.jColor, var5.x, var5.y, var7, var6);
         drawSourceRect(var0, var5.kColor, var5.x, var5.y, var7, var6);
         int var8 = Math.max(0, Math.min(var7, var3 * var7 / 100));
         if (var8 > 1 && var6 > 1) {
            fillSourceColor(var0, var5.lColor, var5.x + 1, var5.y + 1, var8 - 1, var6 - 1);
         }

      }
   }

   private static void fillSourceColor(Graphics2D var0, int var1, int var2, int var3, int var4, int var5) {
      if (var4 > 0 && var5 > 0 && var1 >> 24 != 0) {
         var0.setColor(new Color(var1, true));
         var0.fillRect(var2, var3, var4, var5);
      }
   }

   private static void drawSourceRect(Graphics2D var0, int var1, int var2, int var3, int var4, int var5) {
      if (var4 > 0 && var5 > 0 && var1 >> 24 != 0) {
         var0.setColor(new Color(var1, true));
         var0.drawRect(var2, var3, var4, var5);
      }
   }

   private static Color battlePercentColor(int var0) {
      return var0 > 100 ? new Color(16773536) : (var0 < 100 ? new Color(12114175) : Color.WHITE);
   }

   private static void drawShopBuyOverlay(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2) {
      VqsvChoiceUiView var3 = choiceRenderState(var2);
      int var4 = 3054715;
      drawSourceUiFill(var0, 15, 46, 216, 246, 466733);
      drawSourceUiFill(var0, 12, 42, 216, 246, 13167080);
      drawSourceUiFill(var0, 15, 45, 210, 27, 1592911);
      drawSourceUiFill(var0, 15, 69, 210, 3, 15911010);
      drawChoiceFrame(var0, 12, 42, 216, 246);
      drawCenteredTinyText(var0, var1, "THƯƠNG ĐIẾM TRẬN", 22, 53, 126, Color.WHITE);
      drawModernChoiceHeaderPill(var0, var1, var3.size() == 0 ? "0/0" : var3.selectedIndex + 1 + "/" + var3.size(), 166, 51, 51, 16, var4);
      drawSourceUiFill(var0, 20, 76, 200, 19, 2318176);
      drawTinyBattleText(var0, var1, "KIM TIỀN", 28, 81, 48, new Color(12577250));
      drawTinyBattleText(var0, var1, String.valueOf(var2.battleShopBuyMoney), 79, 81, 52, new Color(15911010));
      drawTinyBattleText(var0, var1, "HUY HIỆU", 136, 81, 51, new Color(12577250));
      drawTinyBattleText(var0, var1, String.valueOf(var2.battleShopBuyBadges), 190, 81, 24, new Color(15911010));
      int var5 = var3.visibleStart();

      for(int var6 = 0; var6 < 5; ++var6) {
         int var7 = var5 + var6;
         int var8 = 99 + var6 * 21;
         boolean var9 = var7 < var3.size() && var7 == var3.selectedIndex;
         drawSourceUiFill(var0, 20 + (var9 ? 3 : 0), var8 + (var9 ? 2 : 0), 196, 19, var9 ? 601903 : 10867152);
         drawSourceUiFill(var0, 20, var8, 196, 19, var9 ? 2649957 : 15793144);
         drawSourceUiFill(var0, 20, var8, 5, 19, var9 ? 15911010 : var4);
         var0.setColor(new Color(var9 ? 15911010 : 7711401));
         var0.drawRect(20, var8, 195, 18);
         if (var7 < var3.size()) {
            int var10 = var3.idAt(var7);
            BattleItemRow var11 = VqsvBattleTables.instance().item(var10);
            if (var11 != null) {
               drawSpriteCellTopLeft(var0, 258, var11.iconId, 29, var8 + 3);
            }

            drawMarqueeTinyBattleText(var0, var1, var3.nameAt(var7), 48, var8 + 5, 102, var9 ? Color.WHITE : SOURCE_UI_TEXT, var2.battleAnimationTick);
            drawShopPricePill(var0, var1, var3.valueAt(var7), 157, var8 + 3, 51, 14, var11 == null ? 0 : var11.currencyOrType, var9);
         }
      }

      if (var3.size() > 5) {
         drawSourceUiFill(var0, 218, 99, 4, 103, 2383207);
         int var12 = UiScrollbarMath.thumbHeight(103, var3.size(), 5, 9);
         int var14 = UiScrollbarMath.thumbY(99, 103, var12, var3.size(), 5, var3.visibleStart());
         drawSourceUiFill(var0, 217, var14, 6, var12, 15911010);
      }

      int var13 = var3.size() == 0 ? -1 : var3.idAt(var3.selectedIndex);
      BattleItemRow var15 = VqsvBattleTables.instance().item(var13);
      String var16 = var15 == null ? "" : var15.description("");
      drawSourceUiFill(var0, 20, 205, 200, 46, 1592911);
      var0.setColor(new Color(15911010));
      var0.drawRoundRect(20, 205, 199, 45, 7, 7);
      String var17 = var3.size() == 0 ? "Không có hàng" : var3.nameAt(var3.selectedIndex);
      drawMarqueeTinyBattleText(var0, var1, var17, 28, 210, 184, new Color(15911010), var2.battleAnimationTick);
      drawFixedTinyLines(var0, var1, var16.isEmpty() ? "Chọn món hàng để xem thông tin." : var16, 28, 222, 184, 3, Color.WHITE);
      drawModernChoiceSoftkey(var0, var1, 25, 264, 76, 19, "Mua", true, var4);
      drawModernChoiceSoftkey(var0, var1, 140, 264, 75, 19, "Quay lại", false, var4);
   }

   private static void drawShopPricePill(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
      drawSourceUiFill(var0, var3, var4, var5, var6, var8 ? 15911010 : 1523542);
      var0.setColor(new Color(var8 ? 16777215 : 6924964));
      var0.drawRoundRect(var3, var4, var5 - 1, var6 - 1, 6, 6);
      String var9 = var7 == 1 ? "H " : (var7 == 2 ? "V " : "K ");
      drawCenteredTinyText(var0, var1, var9 + var2, var3 + 2, var4 + 3, var5 - 4, var8 ? new Color(6047770) : Color.WHITE);
   }

   private static void drawShopConfirmOverlay(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2) {
      VqsvBattleShopConfirmRenderState var3 = shopConfirmRenderState(var2);
      drawShopConfirmOverlay(var0, var1, var3.quantity, var3.total, var3.currency, var2.battleAnimationTick);
   }

   static void drawShopConfirmOverlay(Graphics2D var0, UiFont var1, int var2, int var3, int var4, int var5) {
      byte var6 = 31;
      byte var7 = 92;
      short var8 = 178;
      short var9 = 139;
      var0.setColor(new Color(0, 16, 24, 170));
      var0.fillRect(0, 36, 240, 252);
      drawSourceUiFill(var0, var6 + 3, var7 + 4, var8, var9, 466733);
      drawSourceUiFill(var0, var6, var7, var8, var9, 13955052);
      drawSourceUiFill(var0, var6 + 2, var7 + 2, var8 - 4, 28, 1592911);
      drawSourceUiFill(var0, var6 + 2, var7 + 29, var8 - 4, 3, 15911010);
      drawChoiceFrame(var0, var6, var7, var8, var9);
      drawCenteredTinyText(var0, var1, "XÁC NHẬN MUA", var6 + 20, var7 + 9, var8 - 40, Color.WHITE);
      drawTinyBattleText(var0, var1, "Số lượng", var6 + 10, var7 + 43, 56, SOURCE_UI_TEXT);
      drawSourceUiFill(var0, var6 + 64, var7 + 38, 50, 23, 16777215);
      var0.setColor(new Color(3054715));
      var0.drawRoundRect(var6 + 64, var7 + 38, 49, 22, 7, 7);
      drawCenteredTinyText(var0, var1, String.valueOf(var2), var6 + 68, var7 + 45, 42, new Color(1592911));
      drawModernChoiceSoftkey(var0, var1, 46, 136, 38, 31, "-", false, 3054715);
      drawModernChoiceSoftkey(var0, var1, 156, 136, 38, 31, "+", false, 3054715);
      drawSourceUiFill(var0, var6 + 11, var7 + 78, var8 - 22, 23, 2318176);
      drawTinyBattleText(var0, var1, "TổNG", var6 + 20, var7 + 85, 38, new Color(12577250));
      VqsvShopCurrencyRenderer.draw(var0, var1, var3, var4, var6 + 70, var7 + 80, 84, 20, new Color(15911010));
      drawModernChoiceSoftkey(var0, var1, 45, 200, 64, 20, "Xác nhận", true, 3054715);
      drawModernChoiceSoftkey(var0, var1, 132, 200, 64, 20, "Hủy", false, 3054715);
   }

   private static int shopCurrencyCell(int var0) {
      if (var0 == 1) {
         return 83;
      } else {
         return var0 == 2 ? 74 : 84;
      }
   }

   private static void drawShopWidgetText(Graphics2D var0, UiFont var1, VqsvUiLayout var2, int var3, String var4, int var5, Color var6, int var7) {
      VqsvUiLayout.UiWidget var8 = var2.widget(var3);
      if (var8 != null) {
         drawSourceWidgetText(var0, var1, var4, var8.x, var8.y, Math.max(var5, Math.max(1, var8.w)), sourceWidgetHeight(var8), widgetTextColor(var8, false, var6), var7, var8.b);
      }
   }

   private static void drawShopSoftKeyText(Graphics2D var0, UiFont var1, VqsvUiLayout var2, int var3, String var4, int var5) {
      VqsvUiLayout.UiWidget var6 = var2.widget(var3);
      if (var6 != null) {
         int var7 = var3 == 39 ? Math.max(1, var2.x(41, 81) - var6.x - 2) : Math.max(1, 240 - var6.x - 43);
         drawSourceWidgetText(var0, var1, var4, var6.x, var6.y, var7, sourceWidgetHeight(var6), widgetTextColor(var6, false, SOURCE_UI_TEXT), var5, var6.b);
      }
   }

   private static void drawMsgynOptionText(Graphics2D var0, UiFont var1, VqsvUiLayout var2, int var3, String var4, int var5) {
      VqsvUiLayout.UiWidget var6 = var2.widget(var3);
      if (var6 != null) {
         int var7 = var2.x(5, 77);
         int var8 = Math.max(1, var2.x(15, 128) - var7 - 8);
         drawSourceWidgetText(var0, var1, var4, var7, var6.y, var8, sourceWidgetHeight(var6), widgetTextColor(var6, false, SOURCE_UI_TEXT), var5, 4);
      }
   }

   private static void drawChoiceOverlay(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2) {
      VqsvUiLayout var3 = VqsvUiLayout.load("choice.ui");
      VqsvChoiceUiView var4 = choiceRenderState(var2);
      if (var4.isCatchMenu()) {
         drawCatchBallOverlay(var0, var1, var2, var4);
      } else if (modernBattleChoice(var4)) {
         drawModernBattleChoiceOverlay(var0, var1, var2, var4);
      } else {
         drawChoiceStaticWidgets(var0, var1, var3, var4, var2.battleAnimationTick);
         int var5 = choiceAccentColor(var4);
         int var6 = var4.visibleStart();
         int var7 = var4.visibleCount();

         for(int var8 = 0; var8 < var7; ++var8) {
            int var9 = var6 + var8;
            boolean var10 = var9 == var4.selectedIndex;
            int var11 = 11 + var8 * 5;
            int var12 = 54 + var8;
            int var13 = 13 + var8 * 5;
            int var14 = 14 + var8 * 5;
            if (var4.widgetVisible(var11)) {
               drawChoiceRowPanel(var0, var3, var11, var8, var10, var5);
            }

            Color var15 = var10 ? Color.WHITE : widgetTextColor(var3.widget(var13), false, SOURCE_UI_TEXT);
            if (var4.widgetVisible(var12) && var4.rowIconVisible(var8)) {
               drawChoiceRowIcon(var0, var3, var4, var8, var12, var10, var5);
            }

            if (var4.widgetVisible(var13)) {
               drawSourceWidgetText(var0, var1, var4.widgetText(var13, ""), var3.x(var13, 77), var3.y(var13, 97 + var8 * 15), var3.w(var13, 72), sourceWidgetHeight(var3.widget(var13)), var15, var2.battleAnimationTick, var3.widget(var13) == null ? 3 : var3.widget(var13).b);
            }

            if (var4.widgetVisible(var14)) {
               VqsvUiLayout.UiWidget var16 = var3.widget(var14);
               int var17 = var3.x(var14, 141);
               int var18 = var3.y(var14, 97 + var8 * 15);
               int var19 = var3.w(var14, 36);
               int var20 = Math.max(11, sourceWidgetHeight(var16));
               drawChoiceValuePill(var0, var1, var4.widgetText(var14, ""), var17, var18, var19, var20, var10, var5, var2.battleAnimationTick);
            }
         }

         if (var4.size() > 5) {
            VqsvUiLayout.UiWidget var21 = var3.widget(50);
            drawSourceWidgetFill(var0, var3, 50, 72, 1208209);
            int var23 = var21 == null ? 98 : var21.y;
            byte var24 = 72;
            int var25 = var4.scrollbarThumbY(var23, var24);
            VqsvUiLayout.UiWidget var26 = var3.widget(51);
            drawSourceUiFill(var0, var3.x(51, 183), var25, var3.w(51, 4), 8, widgetFillColor(var26, false, new Color(16773482)).getRGB() & 16777215);
         }

         if (var4.size() == 0) {
            drawTinyBattleText(var0, var1, "...", 105, 136, 40, Color.WHITE);
         }

         String var22 = var4.selectedDescription();
         if (var4.widgetVisible(52) && (!var22.isEmpty() || var4.isCatchMenu())) {
            drawChoiceDescription(var0, var1, var3, var4, var22, var2.battleAnimationTick);
         }

         if (var4.widgetVisible(5)) {
            drawChoiceSoftkey(var0, var1, var3, 5, var4.widgetText(5, var3.text(5, "")), true, var5);
         }

         if (var4.widgetVisible(6)) {
            drawChoiceSoftkey(var0, var1, var3, 6, var4.widgetText(6, var3.text(6, "Quay lại")), false, var5);
         }

         if (var4.widgetVisible(59)) {
            drawChoiceSoftkey(var0, var1, var3, 59, var4.widgetText(59, var3.text(59, "")), true, var5);
         }

         if (var4.widgetVisible(60)) {
            drawChoiceSoftkey(var0, var1, var3, 60, var4.widgetText(60, var3.text(60, "Quay lại")), false, var5);
         }

      }
   }

   private static void drawCatchBallOverlay(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2, VqsvChoiceUiView var3) {
      byte var4 = 15;
      byte var5 = 44;
      short var6 = 210;
      short var7 = 242;
      int var8 = 14977594;
      drawSourceUiFill(var0, var4 + 3, var5 + 4, var6, var7, 466733);
      drawSourceUiFill(var0, var4, var5, var6, var7, 13430001);
      drawSourceUiFill(var0, var4 + 2, var5 + 2, var6 - 4, 28, 4797800);
      drawSourceUiFill(var0, var4 + 2, var5 + 29, var6 - 4, 3, var8);
      drawSourceUiFill(var0, var4, var5 + var7 - 25, var6, 25, 7032195);
      drawChoiceFrame(var0, var4, var5, var6, var7);
      drawCenteredTinyText(var0, var1, "Bắt sủng vật", var4 + 9, var5 + 9, 105, Color.WHITE);
      drawModernChoiceHeaderPill(var0, var1, var3.size() == 0 ? "0 bóng" : var3.size() + " loại", var4 + 132, var5 + 7, 61, 16, var8);
      VqsvBattleRenderState.UnitState var9 = battleRenderState(var2).enemy;
      String var10 = var9.name != null && !var9.name.isEmpty() ? var9.name : "Mục tiêu hoang dã";
      int var11 = var9.maxHp <= 0 ? 100 : Math.max(0, Math.min(100, var9.hp * 100 / var9.maxHp));
      drawSourceUiFill(var0, 23, 77, 194, 18, 1523542);
      drawSourceUiFill(var0, 23, 77, 5, 18, var8);
      drawMarqueeTinyBattleText(var0, var1, var10, 32, 81, 112, Color.WHITE, var2.battleAnimationTick);
      drawTinyBattleText(var0, var1, "HP " + var11 + "%", 166, 81, 45, var11 <= 25 ? new Color(16748925) : new Color(10414784));
      int var12 = var3.visibleStart();

      for(int var13 = 0; var13 < 5; ++var13) {
         int var14 = var12 + var13;
         int var15 = 99 + var13 * 23;
         boolean var16 = var14 < var3.size() && var14 == var3.selectedIndex;
         drawSourceUiFill(var0, 23 + (var16 ? 3 : 0), var15 + (var16 ? 2 : 0), 92, 21, var16 ? 1840423 : 11196383);
         drawSourceUiFill(var0, 23, var15, 92, 21, var16 ? 6900352 : 15399162);
         drawSourceUiFill(var0, 23, var15, 5, 21, var16 ? 16763231 : var8);
         var0.setColor(new Color(var16 ? 16763231 : 8105657));
         var0.drawRect(23, var15, 91, 20);
         if (var14 < var3.size()) {
            drawSourceUiFill(var0, 31, var15 + 3, 16, 16, var16 ? 16766845 : 12969965);
            drawSpriteCellTopLeft(var0, 258, var3.iconAt(var14), 33, var15 + 5);
            drawMarqueeTinyBattleText(var0, var1, var3.nameAt(var14), 52, var15 + 6, 56, var16 ? Color.WHITE : SOURCE_UI_TEXT, var2.battleAnimationTick);
         }
      }

      if (var3.size() > 5) {
         drawSourceUiFill(var0, 116, 99, 3, 113, 2975104);
         int var20 = UiScrollbarMath.thumbHeight(113, var3.size(), 5, 9);
         int var22 = UiScrollbarMath.thumbY(99, 113, var20, var3.size(), 5, var3.visibleStart());
         drawSourceUiFill(var0, 115, var22, 5, var20, 16763231);
      }

      drawSourceUiFill(var0, 121, 99, 96, 113, 1523542);
      var0.setColor(new Color(16763231));
      var0.drawRoundRect(121, 99, 95, 112, 8, 8);
      String var21 = var3.size() == 0 ? "Không có bóng" : var3.nameAt(var3.selectedIndex);
      drawMarqueeTinyBattleText(var0, var1, var21, 129, 105, 80, new Color(16766845), var2.battleAnimationTick);
      int var23 = Math.abs(var2.battleAnimationTick % 24 - 12);
      short var24 = 169;
      short var25 = 141;
      var0.setColor(new Color(228, 138, 58, 55 + (12 - var23) * 8));
      var0.fillOval(var24 - 19, var25 - 19, 38, 38);
      var0.setColor(new Color(255, 201, 95, 120));
      var0.drawOval(var24 - 15 - var23 / 3, var25 - 15 - var23 / 3, 30 + var23 * 2 / 3, 30 + var23 * 2 / 3);
      if (var3.size() > 0) {
         drawSpriteCellTopLeft(var0, 258, var3.iconAt(var3.selectedIndex), var24 - 6, var25 - 6 - (12 - var23) / 4);
      }

      String var17 = var3.size() == 0 ? "--" : var3.valueAt(var3.selectedIndex);
      drawCenteredTinyText(var0, var1, "Tỷ lệ bắt", 129, 166, 80, new Color(12181480));
      drawCenteredTinyText(var0, var1, var17, 129, 179, 80, new Color(16763231));
      int var18 = parsePercent(var17);
      drawSourceUiFill(var0, 131, 195, 76, 6, 665397);
      drawSourceUiFill(var0, 132, 196, Math.max(0, Math.min(74, var18 * 74 / 100)), 4, var18 >= 70 ? 5687690 : (var18 >= 35 ? 16763231 : 14641243));
      drawSourceUiFill(var0, 23, 216, 194, 38, 2904934);
      var0.setColor(new Color(9226195));
      var0.drawRect(23, 216, 193, 37);
      String var19 = var3.selectedDescription();
      if (var19.isEmpty()) {
         var19 = "Không có bóng phù hợp.";
      }

      drawFixedTinyLines(var0, var1, var19, 31, 221, 178, 3, Color.WHITE);
      drawModernChoiceSoftkey(var0, var1, 34, 263, 72, 19, var3.action, true, var8);
      drawModernChoiceSoftkey(var0, var1, 137, 263, 69, 19, var3.backAction, false, var8);
   }

   private static int parsePercent(String var0) {
      if (var0 == null) {
         return 0;
      } else {
         try {
            return Math.max(0, Math.min(100, Integer.parseInt(var0.replace("%", "").trim())));
         } catch (NumberFormatException var2) {
            return 0;
         }
      }
   }

   private static void drawFixedTinyLines(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, int var6, Color var7) {
      List var8 = wrapPetStateText(var1, TextBox.decodeMojibake(var2), var5);
      Shape var9 = var0.getClip();
      var0.clipRect(var3, var4 - 1, var5, var6 * 10);

      for(int var10 = 0; var10 < Math.min(var6, var8.size()); ++var10) {
         String var11 = (String)var8.get(var10);
         if (var10 == var6 - 1 && var8.size() > var6) {
            var11 = fitTinyBattleText(var1, var11 + "...", var5);
         }

         drawTinyBattleText(var0, var1, var11, var3, var4 + var10 * 10, var5, var7);
      }

      var0.setClip(var9);
   }

   private static void drawModernBattleChoiceOverlay(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2, VqsvChoiceUiView var3) {
      byte var4 = 23;
      byte var5 = 50;
      short var6 = 194;
      short var7 = 232;
      int var8 = choiceAccentColor(var3);
      boolean var9 = var3.isCatchMenu();
      drawSourceUiFill(var0, var4 + 3, var5 + 4, var6, var7, 466733);
      drawSourceUiFill(var0, var4, var5, var6, var7, 12181480);
      drawSourceUiFill(var0, var4 + 2, var5 + 2, var6 - 4, 30, 1064790);
      drawSourceUiFill(var0, var4 + 2, var5 + 31, var6 - 4, 2, 16773482);
      drawSourceUiFill(var0, var4, var5 + var7 - 25, var6, 25, 2792387);
      drawChoiceFrame(var0, var4, var5, var6, var7);
      drawCenteredTinyText(var0, var1, var9 ? "BÓNG BẮT" : "HÀNH TRANG TRẬN", var4 + 8, var5 + 8, 112, Color.WHITE);
      drawModernChoiceHeaderPill(var0, var1, var3.size() == 0 ? "0/0" : var3.selectedIndex + 1 + "/" + var3.size(), var4 + 128, var5 + 7, 50, 16, var8);
      byte var10 = 32;
      byte var11 = 88;
      short var12 = 176;
      byte var13 = 21;
      byte var14 = 23;
      int var15 = var3.visibleStart();
      int var16 = var3.visibleCount();

      for(int var17 = 0; var17 < 5; ++var17) {
         int var18 = var15 + var17;
         int var19 = var11 + var17 * var14;
         if (var17 < var16 && var18 < var3.size()) {
            drawModernChoiceRow(var0, var1, var2, var3, var17, var18, var10, var19, var12, var13, var8);
         } else {
            drawModernChoiceEmptyRow(var0, var10, var19, var12, var13);
         }
      }

      if (var3.size() > 5) {
         short var22 = 211;
         byte var23 = 88;
         byte var24 = 113;
         drawSourceUiFill(var0, var22, var23, 4, var24, 1208209);
         int var20 = UiScrollbarMath.thumbHeight(var24, var3.size(), 5, 9);
         int var21 = UiScrollbarMath.thumbY(var23, var24, var20, var3.size(), 5, var3.visibleStart());
         drawSourceUiFill(var0, var22 - 1, var21, 6, var20, 16773482);
      }

      drawModernChoiceDescription(var0, var1, var2, var3, var8);
      drawModernChoiceSoftkey(var0, var1, 42, 254, 66, 20, var3.action, true, var8);
      drawModernChoiceSoftkey(var0, var1, 138, 254, 60, 20, var3.backAction, false, var8);
   }

   private static void drawModernChoiceHeaderPill(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, int var6, int var7) {
      drawSourceUiFill(var0, var3, var4, var5, var6, 16773482);
      var0.setColor(new Color(var7));
      var0.drawRoundRect(var3, var4, var5 - 1, var6 - 1, 7, 7);
      drawCenteredTinyText(var0, var1, var2, var3 + 2, var4 + 4, var5 - 4, new Color(6309911));
   }

   private static void drawModernChoiceRow(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2, VqsvChoiceUiView var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10) {
      boolean var11 = var5 == var3.selectedIndex;
      int var12 = var11 ? darkerColor(var10) : 15267834;
      if (var11) {
         drawSourceUiFill(var0, var6 + 3, var7 + 3, var8, var9, 601920);
      }

      drawSourceUiFill(var0, var6, var7, var8, var9, var12);
      drawSourceUiFill(var0, var6, var7, 5, var9, var11 ? 16773482 : var10);
      var0.setColor(new Color(var11 ? 16773482 : 5018021));
      var0.drawRect(var6, var7, var8 - 1, var9 - 1);
      if (var11) {
         drawSourceUiFill(var0, var6 + 5, var7 + 1, var8 - 6, 2, 16773482);
      }

      drawSourceUiFill(var0, var6 + 8, var7 + 3, 16, 16, var11 ? 16773482 : 13430003);
      var0.setColor(new Color(1523542));
      var0.drawRect(var6 + 8, var7 + 3, 15, 15);
      String var13 = var3.rowIconResource(var4);
      boolean var14 = !var13.isEmpty() && UnifiedItemIconRenderer.draw(var0, var13, var6 + 10, var7 + 5, 12, 12);
      if (!var14 && var3.rowIconVisible(var4)) {
         drawSpriteCellTopLeft(var0, 258, var3.rowIconCell(var4), var6 + 10, var7 + 5);
      }

      Color var15 = var11 ? Color.WHITE : SOURCE_UI_TEXT;
      drawMarqueeTinyBattleText(var0, var1, var3.nameAt(var5), var6 + 30, var7 + 6, 92, var15, var2.battleAnimationTick);
      drawModernChoiceValue(var0, var1, var3.valueAt(var5), var6 + 128, var7 + 4, 42, 14, var11, var10, var2.battleAnimationTick);
   }

   private static void drawModernChoiceEmptyRow(Graphics2D var0, int var1, int var2, int var3, int var4) {
      drawSourceUiFill(var0, var1, var2, var3, var4, 13298417);
      var0.setColor(new Color(9159118));
      var0.drawRect(var1, var2, var3 - 1, var4 - 1);
   }

   private static void drawModernChoiceValue(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, int var6, boolean var7, int var8, int var9) {
      int var10 = var7 ? 16773482 : 1523542;
      var0.setColor(new Color(var10));
      var0.fillRoundRect(var3, var4, var5, var6, 6, 6);
      var0.setColor(new Color(var7 ? 16777215 : var8));
      var0.drawRoundRect(var3, var4, var5 - 1, var6 - 1, 6, 6);
      drawSourceWidgetText(var0, var1, var2, var3 + 2, var4 + 1, var5 - 4, var6 - 1, var7 ? new Color(7162646) : Color.WHITE, var9, 4);
   }

   private static void drawModernChoiceDescription(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2, VqsvChoiceUiView var3, int var4) {
      byte var5 = 32;
      short var6 = 208;
      short var7 = 176;
      byte var8 = 37;
      drawSourceUiFill(var0, var5 + 2, var6 + 2, var7, var8, 466733);
      drawSourceUiFill(var0, var5, var6, var7, var8, 1523542);
      drawSourceUiFill(var0, var5, var6, 5, var8, var4);
      drawChoiceFrame(var0, var5, var6, var7, var8);
      String var9 = var3.size() == 0 ? "Chưa có vật phẩm" : var3.nameAt(var3.selectedIndex);
      drawMarqueeTinyBattleText(var0, var1, var9, var5 + 10, var6 + 4, var7 - 20, new Color(16773482), var2.battleAnimationTick);
      String var10 = var3.selectedDescription();
      if (var10.isEmpty()) {
         var10 = var3.isCatchMenu() ? "Chọn bóng để bắt sủng vật." : "Chọn đạo cụ để sử dụng trong trận.";
      }

      List var11 = wrapPetStateText(var1, TextBox.decodeMojibake(var10), var7 - 20);
      byte var12 = 10;
      byte var13 = 2;
      Shape var14 = var0.getClip();
      var0.clipRect(var5 + 10, var6 + 15, var7 - 20, var8 - 17);

      for(int var15 = 0; var15 < var13 && var15 < var11.size(); ++var15) {
         String var16 = (String)var11.get(var15);
         if (var15 == var13 - 1 && var11.size() > var13) {
            var16 = fitTinyBattleText(var1, var16 + "...", var7 - 20);
         }

         drawTinyBattleText(var0, var1, var16, var5 + 10, var6 + 15 + var15 * var12, var7 - 20, Color.WHITE);
      }

      var0.setClip(var14);
   }

   private static void drawModernChoiceSoftkey(Graphics2D var0, UiFont var1, int var2, int var3, int var4, int var5, String var6, boolean var7, int var8) {
      drawSourceUiFill(var0, var2 + 2, var3 + 2, var4, var5, 731192);
      drawSourceUiFill(var0, var2, var3, var4, var5, var7 ? var8 : 12049638);
      drawSourceUiFill(var0, var2 + 2, var3 + 2, var4 - 4, 2, var7 ? 6276831 : 14939898);
      drawSourceUiFill(var0, var2 + 2, var3 + var5 - 4, var4 - 4, 2, var7 ? 876414 : 7450301);
      var0.setColor(new Color(var7 ? 16773482 : 3432558));
      var0.drawRect(var2, var3, var4 - 1, var5 - 1);
      drawSourceWidgetText(var0, var1, var6, var2 + 2, var3, var4 - 4, var5, var7 ? Color.WHITE : SOURCE_UI_TEXT, 0, 4);
   }

   private static boolean modernBattleChoice(VqsvChoiceUiView var0) {
      String var1 = TextBox.decodeMojibake(var0.title);
      return var0.isCatchMenu() || "Đạo cụ".equals(var1);
   }

   private static void drawChoiceStaticWidgets(Graphics2D var0, UiFont var1, VqsvUiLayout var2, VqsvChoiceUiView var3, int var4) {
      int var5 = choiceAccentColor(var3);
      drawSourceWidgetFill(var0, var2, 4, 8, 731192);
      drawSourceWidgetFill(var0, var2, 2, 160, 7981013);
      drawSourceWidgetFill(var0, var2, 3, 14, 2067892);
      drawSourceWidgetCell(var0, var2, 1, false, false);
      drawSourceWidgetFill(var0, var2, 7, 82, 11067369);
      int var6 = var2.x(4, 45);
      int var7 = var2.y(4, 70);
      int var8 = var2.w(4, 150);
      drawSourceUiFill(var0, var6, var7, var8, 12, var5);
      drawSourceUiFill(var0, var6, var7 + 11, var8, 2, 16773482);
      if (var3.widgetVisible(8)) {
         drawChoiceText(var0, var1, var2, 8, var3.widgetText(8, ""), Color.WHITE, var4);
      }

      if (var3.widgetVisible(9)) {
         drawChoiceText(var0, var1, var2, 9, var3.widgetText(9, ""), Color.WHITE, var4);
      }

   }

   private static void drawChoiceText(Graphics2D var0, UiFont var1, VqsvUiLayout var2, int var3, String var4, Color var5, int var6) {
      VqsvUiLayout.UiWidget var7 = var2.widget(var3);
      drawSourceWidgetText(var0, var1, var4, var2.x(var3, 0), var2.y(var3, 0), var2.w(var3, 1), sourceWidgetHeight(var7), widgetTextColor(var7, false, var5), var6, var7 == null ? 0 : var7.b);
   }

   private static void drawChoiceDescription(Graphics2D var0, UiFont var1, VqsvUiLayout var2, VqsvChoiceUiView var3, String var4, int var5) {
      VqsvUiLayout.UiWidget var6 = var2.widget(52);
      VqsvUiLayout.UiWidget var7 = var2.widget(53);
      int var8 = var2.x(52, 52);
      int var9 = var2.y(52, 174);
      int var10 = var2.w(52, 136);
      int var11 = Math.max(26, var2.bandHeight(52, 29));
      drawSourceUiFill(var0, var8 + 2, var9 + 2, var10, var11, 466733);
      drawSourceUiFill(var0, var8, var9, var10, var11, 1523542);
      drawSourceUiFill(var0, var8 + 2, var9 + 2, 43, 12, choiceAccentColor(var3));
      drawChoiceFrame(var0, var8, var9, var10, var11);
      drawTinyBattleText(var0, var1, var3.isCatchMenu() ? "Bóng" : "Chi tiết", var8 + 6, var9 + 3, 38, Color.WHITE);
      int var12 = var2.x(53, 57);
      int var13 = var2.y(53, 180);
      int var14 = var2.w(53, 125);
      int var15 = Math.max(12, sourceWidgetHeight(var7));
      List var16 = wrapPetStateText(var1, TextBox.decodeMojibake(var4 == null ? "" : var4), var14);
      byte var17 = 10;
      int var18 = Math.max(1, var15 / var17);
      int var19 = Math.max(0, var16.size() - var18);
      int var20 = var19 == 0 ? 0 : Math.max(0, var5) / 48 % (var19 + 1);
      Shape var21 = var0.getClip();
      var0.clipRect(var12, var13, var14, var15);

      for(int var22 = 0; var22 < var18 && var20 + var22 < var16.size(); ++var22) {
         drawTinyBattleText(var0, var1, (String)var16.get(var20 + var22), var12, var13 + var22 * var17, var14, Color.WHITE);
      }

      var0.setClip(var21);
   }

   private static void drawChoiceRowPanel(Graphics2D var0, VqsvUiLayout var1, int var2, int var3, boolean var4, int var5) {
      VqsvUiLayout.UiWidget var6 = var1.widget(var2);
      int var7 = var1.x(var2, 54);
      int var8 = var1.y(var2, 95 + var3 * 15);
      int var9 = var1.w(var2, 128);
      int var10 = Math.max(13, sourceWidgetHeight(var6));
      if (var4) {
         drawSourceUiFill(var0, var7 + 3, var8 + 3, var9, var10, 601920);
      }

      drawSourceUiFill(var0, var7, var8, var9, var10, var4 ? darkerColor(var5) : 13823733);
      drawSourceUiFill(var0, var7, var8, 4, var10, var4 ? 16773482 : var5);
      if (var4) {
         drawSourceUiFill(var0, var7 + 4, var8 + 1, var9 - 5, 2, 16773482);
      }

      var0.setColor(new Color(var4 ? 16773482 : 5018021));
      var0.drawRect(var7, var8, var9 - 1, var10 - 1);
   }

   private static void drawChoiceRowIcon(Graphics2D var0, VqsvUiLayout var1, VqsvChoiceUiView var2, int var3, int var4, boolean var5, int var6) {
      int var7 = var1.x(var4, 54);
      int var8 = var1.y(var4, 95 + var3 * 15);
      drawSourceUiFill(var0, var7 - 2, var8 - 2, 17, 17, var5 ? 16773482 : lighterColor(var6));
      var0.setColor(new Color(1523542));
      var0.drawRect(var7 - 2, var8 - 2, 16, 16);
      String var9 = var2.rowIconResource(var3);
      boolean var10 = !var9.isEmpty() && UnifiedItemIconRenderer.draw(var0, var9, var7, var8, 13, 13);
      if (!var10) {
         drawSpriteCellTopLeft(var0, 258, var2.rowIconCell(var3), var7, var8);
      }

   }

   private static void drawChoiceValuePill(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, int var6, boolean var7, int var8, int var9) {
      int var10 = var7 ? 16773482 : 1523542;
      int var11 = var7 ? 7162646 : 16777215;
      var0.setColor(new Color(var10));
      var0.fillRoundRect(var3 - 2, var4 - 1, var5 + 4, var6 + 1, 6, 6);
      var0.setColor(new Color(var7 ? 16777215 : var8));
      var0.drawRoundRect(var3 - 2, var4 - 1, var5 + 3, var6, 6, 6);
      drawSourceWidgetText(var0, var1, var2, var3, var4, var5, var6, new Color(var11), var9, 4);
   }

   private static void drawChoiceSoftkey(Graphics2D var0, UiFont var1, VqsvUiLayout var2, int var3, String var4, boolean var5, int var6) {
      VqsvUiLayout.UiWidget var7 = var2.widget(var3);
      int var8 = var2.x(var3, var5 ? 47 : 147);
      int var9 = var2.y(var3, 235);
      int var10 = var2.w(var3, 48);
      int var11 = Math.max(15, sourceWidgetHeight(var7));
      drawSourceUiFill(var0, var8 + 2, var9 + 2, var10, var11, 731192);
      drawSourceUiFill(var0, var8, var9, var10, var11, var5 ? var6 : 12049638);
      drawSourceUiFill(var0, var8 + 2, var9 + 2, var10 - 4, 2, var5 ? 6276831 : 14939898);
      drawSourceUiFill(var0, var8 + 2, var9 + var11 - 4, var10 - 4, 2, var5 ? 876414 : 7450301);
      var0.setColor(new Color(var5 ? 16773482 : 3432558));
      var0.drawRect(var8, var9, var10 - 1, var11 - 1);
      drawSourceWidgetText(var0, var1, var4, var8 + 2, var9, Math.max(1, var10 - 4), var11, var5 ? Color.WHITE : SOURCE_UI_TEXT, 0, 4);
   }

   private static void drawChoiceFrame(Graphics2D var0, int var1, int var2, int var3, int var4) {
      var0.setColor(new Color(15267071));
      var0.drawRect(var1, var2, var3 - 1, var4 - 1);
      var0.setColor(new Color(3432558));
      var0.drawRect(var1 + 1, var2 + 1, var3 - 3, var4 - 3);
   }

   private static int choiceAccentColor(VqsvChoiceUiView var0) {
      String var1 = TextBox.decodeMojibake(var0.title);
      if ("Pokemon ball".equals(var1)) {
         return 12077650;
      } else {
         return "Đạo cụ".equals(var1) ? 1277345 : 1208209;
      }
   }

   private static void drawSourceWidgetFill(Graphics2D var0, VqsvUiLayout var1, int var2, int var3, int var4) {
      VqsvUiLayout.UiWidget var5 = var1.widget(var2);
      if (var5 != null) {
         drawSourceUiFill(var0, var5.x, var5.y, Math.max(1, var5.w), var1.bandHeight(var2, var3), widgetFillColor(var5, false, new Color(var4)).getRGB() & 16777215);
      }

   }

   private static void drawSourceWidgetCell(Graphics2D var0, VqsvUiLayout var1, int var2, boolean var3, boolean var4) {
      VqsvUiLayout.UiWidget var5 = var1.widget(var2);
      if (var5 != null) {
         int var6;
         int var7;
         if (var3) {
            var6 = var4 ? var5.altId : var5.imageId;
            var7 = var4 ? var5.altMode : var5.imageMode;
         } else {
            var6 = var4 ? var5.imageId : var5.altId;
            var7 = var4 ? var5.imageMode : var5.altMode;
         }

         if (var6 < 0) {
            var6 = var5.imageId >= 0 ? var5.imageId : var5.altId;
            var7 = var5.imageId >= 0 ? var5.imageMode : var5.altMode;
         }

         if (var6 >= 0) {
            if (var7 == 3) {
               drawBattleUiStateTopLeft(var0, var6, var5.x, var5.y);
            } else {
               drawBattleUiCellTopLeft(var0, var6, var5.x, var5.y);
            }

         }
      }
   }

   static void renderPetStateOverlay(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2, boolean var3) {
      VqsvChoiceUiView var4 = choiceRenderState(var2).withVisibleRows(6);
      VqsvUiLayout var5 = VqsvUiLayout.load("petstate.ui");
      drawPetStateStaticWidgets(var0, var1, var5, var2, var4);
      drawPetStatePageTabs(var0, var1, var2);
      drawPetStateArrows(var0, var5, var4);

      for(int var6 = 0; var6 < 6; ++var6) {
         drawPetStateRow(var0, var1, var5, var2, var4, var6);
      }

      if (var4.size() == 0) {
         drawCenteredTinyText(var0, var1, "...", 43, 131, 66, SOURCE_UI_TEXT);
      }

      drawPetStateDetails(var0, var1, var5, var2, var4);
      int var9 = petstateUiTick(var2);
      String var7 = var3 ? "Xuất chiến" : var5.text(64, "Xác định");
      String var8 = var4.action;
      if (var8 == null || var8.isEmpty()) {
         var8 = var2.battleMenuAction != null && !var2.battleMenuAction.isEmpty() ? var2.battleMenuAction : var7;
      }

      drawPetStateSoftkey(var0, var1, 42, 263, 64, 22, var8, true, var9);
      drawPetStateSoftkey(var0, var1, 144, 263, 56, 22, var5.text(76, "Quay lại"), false, var9);
   }

   static void renderQualityUpgradeOverlay(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2) {
      var0.setColor(new Color(133390));
      var0.fillRoundRect(5, 34, 230, 255, 12, 12);
      var0.setColor(new Color(597803));
      var0.fillRoundRect(7, 36, 226, 251, 10, 10);
      var0.setColor(new Color(2850711));
      var0.drawRoundRect(7, 36, 225, 250, 10, 10);
      var0.setColor(new Color(1195344));
      var0.fillRoundRect(9, 38, 222, 29, 8, 8);
      var0.fillRect(9, 57, 222, 10);
      var0.setColor(new Color(14133555));
      var0.fillRect(12, 65, 216, 2);
      int var3 = var2.qualityUpgradeResult == null ? (var2.qualityUpgradeMain == null ? 0 : var2.qualityUpgradeMain.quality) : var2.qualityUpgradeResult.qualityBefore;
      int var4 = var2.qualityUpgradeResult == null ? Math.min(5, var3 + 1) : var2.qualityUpgradeResult.qualityAfter;
      drawCenteredTinyText(var0, var1, "NÂNG PHẨM  +" + var3 + "  →  +" + var4, 35, 46, 170, new Color(16180643));
      if (var2.qualityUpgradePickerVisible) {
         renderQualityUpgradePicker(var0, var1, var2);
      } else {
         PetState var5 = var2.qualityUpgradeMain;
         drawQualityFodderCard(var0, var1, var2, referencePet(var2.qualityUpgradeFodders[0]), 11, 78, 53, 97, "PHÔI 1", var2.qualityUpgradeFocus == 0, var2.qualityUpgradePhase == 2);
         drawQualityFodderCard(var0, var1, var2, referencePet(var2.qualityUpgradeFodders[1]), 176, 78, 53, 97, "PHÔI 2", var2.qualityUpgradeFocus == 1, var2.qualityUpgradePhase == 2);
         int var6 = var2.qualityUpgradeResult == null ? var3 : var4;
         drawQualityMainCard(var0, var1, var2, var5, 70, 72, 100, 113, var6, var2.qualityUpgradePhase);
         if (var2.qualityUpgradePhase == 1) {
            drawQualityCharging(var0, var2);
         } else if (var2.qualityUpgradePhase == 2 && var2.qualityUpgradeResult != null) {
            drawQualityResultEffect(var0, var2);
         }

         drawQualityStars(var0, var3, var4, var2);
         drawCenteredTinyText(var0, var1, "XÁC SUẤT THÀNH CÔNG", 12, 207, 216, new Color(8964312));
         int var7 = var2.qualityUpgradeRateBasisPoints;
         drawSegmentedQualityBar(var0, var7);
         drawCenteredTinyText(var0, var1, qualityPercent(var7), 188, 217, 40, new Color(var7 > 0 ? 15981170 : 8360858));
         boolean var8 = qualityUpgradeResultReady(var2);
         if (var2.qualityUpgradePhase == 2) {
            String var9 = !var8 ? "ĐANG XÁC ĐỊNH..." : (var2.qualityUpgradeResult.outcome == PetQualityUpgradeService.Outcome.SUCCESS ? "THÀNH CÔNG  +" + var2.qualityUpgradeResult.qualityAfter : "THẤT BẠI  CÒN +" + var2.qualityUpgradeResult.qualityAfter);
            drawCenteredTinyText(var0, var1, var9, 12, 237, 216, new Color(!var8 ? 10406871 : (var2.qualityUpgradeResult.outcome == PetQualityUpgradeService.Outcome.SUCCESS ? 6939818 : 16744584)));
         } else {
            drawCenteredTinyText(var0, var1, "Thành công: +" + Math.min(5, var3 + 1) + "   |   Thất bại: +" + Math.max(1, var3 - 1), 12, 237, 216, new Color(10406871));
         }

         boolean var11 = var7 > 0 && var5 != null && var5.quality < 5;
         var0.setColor(new Color(var2.qualityUpgradeFocus == 2 && var2.qualityUpgradePhase == 0 ? (var11 ? 12817711 : 4477786) : (var11 ? 9137964 : 4477786)));
         var0.fillRoundRect(70, 251, 100, 28, 7, 7);
         var0.setColor(new Color(var11 ? 16045679 : 7307656));
         var0.drawRoundRect(70, 251, 99, 27, 7, 7);
         drawCenteredTinyText(var0, var1, var2.qualityUpgradePhase == 1 ? "ĐANG CƯỜNG HÓA" : (var2.qualityUpgradePhase == 2 ? (var8 ? "TIẾP TỤC" : "...") : "NÂNG PHẨM"), 73, 259, 94, Color.WHITE);
         String var10 = var2.qualityUpgradeStatus == null ? "" : var2.qualityUpgradeStatus;
         drawCenteredTinyText(var0, var1, var10, 10, 281, 220, var2.qualityUpgradePhase == 2 && var2.qualityUpgradeResult != null && var2.qualityUpgradeResult.outcome == PetQualityUpgradeService.Outcome.FAILURE ? new Color(16747925) : new Color(9423062));
         drawEvolutionSoftkey(var0, var1, 1, 291, 58, 25, var2.qualityUpgradePhase == 2 && var8 ? "Tiếp tục" : (var2.qualityUpgradePhase == 0 ? "Chọn phôi" : ""), 2854259);
         drawEvolutionSoftkey(var0, var1, 181, 291, 58, 25, var2.qualityUpgradePhase != 1 && var2.qualityUpgradePhase != 2 ? "Quay lại" : "", 2651542);
      }
   }

   private static void drawSegmentedQualityBar(Graphics2D var0, int var1) {
      byte var2 = 20;
      int var3 = (int)Math.round((double)(var2 * Math.max(0, Math.min(10000, var1))) / (double)10000.0F);

      for(int var4 = 0; var4 < var2; ++var4) {
         int var5 = 14 + var4 * 8;
         var0.setColor(new Color(var4 < var3 ? 14002488 : 2572620));
         var0.fillRoundRect(var5, 219, 6, 10, 3, 3);
         if (var4 < var3) {
            var0.setColor(new Color(16112762));
            var0.fillRect(var5 + 1, 220, 4, 2);
         }
      }

   }

   private static boolean qualityUpgradeResultReady(VqsvGameRuntime.Scene var0) {
      return var0.qualityUpgradePhase == 2 && var0.qualityUpgradeResult != null && var0.qualityUpgradeTicks >= PetQualityUpgradeAssets.resultDurationTicks(var0.qualityUpgradeResult.outcome == PetQualityUpgradeService.Outcome.SUCCESS);
   }

   private static void renderQualityUpgradePicker(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2) {
      drawCenteredTinyText(var0, var1, "Chọn phôi cho ô " + (var2.qualityUpgradeFocus + 1), 12, 72, 216, new Color(15849589));
      int var3 = var2.qualityUpgradePickerScroll;

      for(int var4 = 0; var4 < 5; ++var4) {
         int var5 = var3 + var4;
         int var6 = 82 + var4 * 34;
         boolean var7 = var5 == var2.qualityUpgradePickerIndex;
         var0.setColor(new Color(var7 ? 1460314 : 862774));
         var0.fillRoundRect(12, var6, 216, 31, 7, 7);
         var0.setColor(new Color(var7 ? 13738810 : 3632508));
         var0.drawRoundRect(12, var6, 215, 30, 7, 7);
         if (var5 < var2.qualityUpgradeCandidates.size()) {
            PetQualityUpgradeService.PetReference var8 = (PetQualityUpgradeService.PetReference)var2.qualityUpgradeCandidates.get(var5);
            PetState var9 = var8.pet;
            if (var9.visualSpriteId >= 0) {
               Shape var10 = var0.getClip();
               var0.clipRect(16, var6 + 2, 34, 27);
               drawBattleSprite(var0, var9.visualSpriteId, 17, var6, 32, 30, 7, 0, 0, idleCursor(var9.visualSpriteId, 0, var2.battleAnimationTick));
               var0.setClip(var10);
            }

            String var14 = var8.location == PetQualityUpgradeService.Location.ROSTER ? "Đội" : "Ngân hàng";
            int var11 = var2.qualityUpgradeCandidateFormDistance(var8);
            String var12 = var11 == 0 ? "Cùng form" : "Form trước " + var11;
            String var10000 = qualityPetName(var9);
            String var13 = var10000 + " [" + var14 + "]";
            var1.drawTaggedLine(var0, var13, 54, var6 + 4, TextBox.visibleLength(var13), 15399163);
            var1.drawTaggedLine(var0, var9.quality + " sao | +" + qualityPercent(var2.qualityUpgradeCandidateRateBasisPoints(var8)) + " | " + var12, 54, var6 + 16, 40, 8633802);
         }
      }

      if (var2.qualityUpgradeCandidates.isEmpty()) {
         drawCenteredTinyText(var0, var1, "Không có Pet phôi phù hợp", 20, 150, 200, new Color(16747925));
      }

      drawEvolutionSoftkey(var0, var1, 181, 291, 58, 25, "Quay lại", 2651542);
   }

   private static void drawQualityMainCard(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2, PetState var3, int var4, int var5, int var6, int var7, int var8, int var9) {
      int var10 = var9 == 1 ? var2.qualityUpgradeTicks / 2 & 1 : 0;
      var0.setColor(new Color(132875));
      var0.fillRoundRect(var4 - 2 - var10, var5 - 2 - var10, var6 + 4 + var10 * 2, var7 + 4 + var10 * 2, 10, 10);
      var0.setColor(new Color(var9 == 1 ? 5559784 : 13673017));
      var0.fillRoundRect(var4, var5, var6, var7, 9, 9);
      var0.setColor(new Color(863032));
      var0.fillRoundRect(var4 + 2, var5 + 2, var6 - 4, var7 - 4, 7, 7);
      var0.setColor(new Color(1457999));
      var0.fillRoundRect(var4 + 5, var5 + 22, var6 - 10, 63, 6, 6);
      var0.setColor(new Color(14068285));
      var0.fillRoundRect(var4 + 7, var5 + 6, 28, 18, 6, 6);
      drawCenteredTinyText(var0, var1, "+" + var8, var4 + 8, var5 + 10, 26, Color.WHITE);
      drawCenteredTinyText(var0, var1, "PET CHÍNH", var4 + 37, var5 + 8, var6 - 42, new Color(10801371));
      if (var3 != null) {
         if (var3.visualSpriteId >= 0) {
            Shape var11 = var0.getClip();
            var0.clipRect(var4 + 12, var5 + 23, var6 - 24, 60);
            drawBattleSprite(var0, var3.visualSpriteId, var4 + 12, var5 + 18, var6 - 24, 70, 7, 0, 0, idleCursor(var3.visualSpriteId, 0, var2.battleAnimationTick));
            var0.setClip(var11);
         }

         drawCenteredTinyText(var0, var1, qualityPetName(var3), var4 + 5, var5 + 86, var6 - 10, new Color(16054778));
         drawCenteredTinyText(var0, var1, "Lv " + var3.level + "  |  " + var8 + " sao", var4 + 5, var5 + 99, var6 - 10, new Color(8567238));
      }
   }

   private static void drawQualityFodderCard(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2, PetState var3, int var4, int var5, int var6, int var7, String var8, boolean var9, boolean var10) {
      var0.setColor(new Color(var9 ? 13870138 : 3235693));
      var0.fillRoundRect(var4, var5, var6, var7, 7, 7);
      var0.setColor(new Color(731188));
      var0.fillRoundRect(var4 + 2, var5 + 2, var6 - 4, var7 - 4, 5, 5);
      drawCenteredTinyText(var0, var1, var10 ? "ĐÃ DÙNG" : var8, var4 + 2, var5 + 4, var6 - 4, new Color(var9 ? 16112501 : 9355982));
      if (var10) {
         BufferedImage var12 = PetQualityUpgradeAssets.STAR_STATES[0];
         if (var12 != null) {
            var0.drawImage(var12, var4 + 14, var5 + 30, 24, 24, (ImageObserver)null);
         }

         drawCenteredTinyText(var0, var1, "Hấp thụ", var4 + 2, var5 + 69, var6 - 4, new Color(7180955));
      } else if (var3 == null) {
         var0.setColor(new Color(var9 ? 14002755 : 5668231));
         var0.drawRoundRect(var4 + 12, var5 + 29, 28, 28, 6, 6);
         drawCenteredTinyText(var0, var1, "+", var4 + 13, var5 + 34, 27, new Color(var9 ? 16112501 : 7379362));
         drawCenteredTinyText(var0, var1, "Chọn", var4 + 2, var5 + 68, var6 - 4, new Color(7904678));
      } else {
         if (var3.visualSpriteId >= 0) {
            Shape var11 = var0.getClip();
            var0.clipRect(var4 + 6, var5 + 18, var6 - 12, 45);
            drawBattleSprite(var0, var3.visualSpriteId, var4 + 5, var5 + 14, var6 - 10, 54, 7, 0, 0, idleCursor(var3.visualSpriteId, 0, var2.battleAnimationTick));
            var0.setClip(var11);
         }

         drawCenteredTinyText(var0, var1, qualityPetName(var3), var4 + 2, var5 + 65, var6 - 4, new Color(15398648));
         drawCenteredTinyText(var0, var1, var3.quality + " sao", var4 + 2, var5 + 80, var6 - 4, new Color(14992989));
      }
   }

   private static void drawQualityStars(Graphics2D var0, int var1, int var2, VqsvGameRuntime.Scene var3) {
      drawQualityStarGroup(var0, 36, 188, var1, var3, false);
      var0.setColor(new Color(2978184));
      var0.drawLine(105, 198, 130, 198);
      var0.drawLine(125, 193, 130, 198);
      var0.drawLine(125, 203, 130, 198);
      drawQualityStarGroup(var0, 136, 188, var2, var3, true);
   }

   private static void drawQualityStarGroup(Graphics2D var0, int var1, int var2, int var3, VqsvGameRuntime.Scene var4, boolean var5) {
      int var6 = Math.max(1, Math.min(5, var3));

      for(int var7 = 0; var7 < 5; ++var7) {
         int var8 = var7 < var6 ? 1 : 0;
         if (var5 && var4.qualityUpgradePhase == 1 && var7 == var6 - 1) {
            var8 = 2;
         } else if (var5 && var4.qualityUpgradePhase == 0 && var4.qualityUpgradeRateBasisPoints > 0 && var7 == var6 - 1) {
            var8 = 2;
         } else if (var5 && var4.qualityUpgradePhase == 2 && var4.qualityUpgradeResult != null) {
            if (var4.qualityUpgradeResult.outcome == PetQualityUpgradeService.Outcome.SUCCESS && var7 == var4.qualityUpgradeResult.qualityAfter - 1) {
               var8 = 3;
            } else if (var4.qualityUpgradeResult.outcome == PetQualityUpgradeService.Outcome.FAILURE && var7 == var4.qualityUpgradeResult.qualityBefore - 1) {
               var8 = 4;
            }
         }

         BufferedImage var9 = PetQualityUpgradeAssets.STAR_STATES[var8];
         if (var9 != null) {
            var0.drawImage(var9, var1 + var7 * 14, var2, 16, 16, (ImageObserver)null);
         }
      }

   }

   private static void drawQualityCharging(Graphics2D var0, VqsvGameRuntime.Scene var1) {
      int var2 = var1.qualityUpgradeTicks - 4;
      int var3 = var1.qualityUpgradeTicks - 10;
      BufferedImage var4 = var2 < 0 ? null : PetQualityUpgradeAssets.timedFrame(PetQualityUpgradeAssets.ENERGY_CORE, PetQualityUpgradeAssets.ENERGY_CORE_DURATIONS_MS, var2, false);
      BufferedImage var5 = var3 < 0 ? null : PetQualityUpgradeAssets.timedFrame(PetQualityUpgradeAssets.ENERGY_STREAM, PetQualityUpgradeAssets.ENERGY_STREAM_DURATIONS_MS, var3, false);
      BufferedImage var6 = PetQualityUpgradeAssets.timedFrame(PetQualityUpgradeAssets.STAR_GLOW, PetQualityUpgradeAssets.STAR_GLOW_DURATIONS_MS, var1.qualityUpgradeTicks, true);
      if (var5 != null) {
         if (var1.qualityUpgradeFodders[0] != null) {
            var0.drawImage(var5, 55, 112, 111, 132, 0, 0, var5.getWidth(), var5.getHeight(), (ImageObserver)null);
         }

         if (var1.qualityUpgradeFodders[1] != null) {
            var0.drawImage(var5, 185, 112, 129, 132, 0, 0, var5.getWidth(), var5.getHeight(), (ImageObserver)null);
         }
      }

      if (var4 != null) {
         var0.drawImage(var4, 96, 99, 48, 48, (ImageObserver)null);
      }

      int var7 = var1.qualityUpgradeMain == null ? 1 : Math.min(5, var1.qualityUpgradeMain.quality + 1);
      if (var6 != null) {
         var0.drawImage(var6, 132 + (var7 - 1) * 14, 184, 24, 24, (ImageObserver)null);
      }

   }

   private static void drawQualityResultEffect(Graphics2D var0, VqsvGameRuntime.Scene var1) {
      boolean var2 = var1.qualityUpgradeResult.outcome == PetQualityUpgradeService.Outcome.SUCCESS;
      BufferedImage var3 = PetQualityUpgradeAssets.timedFrame(var2 ? PetQualityUpgradeAssets.SUCCESS : PetQualityUpgradeAssets.FAILURE, var2 ? PetQualityUpgradeAssets.SUCCESS_DURATIONS_MS : PetQualityUpgradeAssets.FAILURE_DURATIONS_MS, var1.qualityUpgradeTicks, false);
      if (var3 != null) {
         var0.drawImage(var3, 72, 78, 96, 96, (ImageObserver)null);
      }

   }

   private static PetState referencePet(PetQualityUpgradeService.PetReference var0) {
      return var0 == null ? null : var0.pet;
   }

   private static String qualityPetName(PetState var0) {
      if (var0 == null) {
         return "";
      } else {
         BattleSpeciesRow var1 = VqsvBattleTables.instance().species(var0.speciesId);
         return var1 == null ? "Pet " + var0.speciesId : var1.name("Pet " + var0.speciesId);
      }
   }

   private static String qualityPercent(int var0) {
      return var0 % 100 == 0 ? var0 / 100 + "%" : var0 / 100 + "," + var0 % 100 / 10 + "%";
   }

   static void renderEvolutionOverlay(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2) {
      EvolutionCandidate var3 = var2.sourceEvolveNotice;
      boolean var4 = var3 != null && var3.kind == EvolutionCandidate.Kind.MUTATION;
      String var5 = var4 ? "Dị hoá" : "Tiến hóa";
      var0.setColor(new Color(0, 24, 35, 180));
      var0.fillRoundRect(7, 39, 226, 249, 12, 12);
      var0.setColor(new Color(13498111));
      var0.fillRoundRect(9, 41, 222, 245, 10, 10);
      var0.setColor(new Color(1527657));
      var0.drawRoundRect(9, 41, 221, 244, 10, 10);
      var0.setColor(new Color(var4 ? 7819688 : 2197933));
      var0.fillRoundRect(11, 43, 218, 27, 8, 8);
      var0.fillRect(11, 61, 218, 9);
      drawBattleUiCellTopLeft(var0, 27, 0, 264);
      drawCenteredTinyText(var0, var1, var5, 70, 47, 100, Color.WHITE);
      drawEvolutionPill(var0, var1, var4 ? "Nhánh dị hóa" : "Nhánh thường", 151, 48, 69, var4 ? 12159711 : 8575464, 1589074);
      int var6 = var2.sourceEvolvePetIndex >= 0 && var2.sourceEvolvePetIndex < var2.session.pets.roster.size() ? ((PetState)var2.session.pets.roster.get(var2.sourceEvolvePetIndex)).level : 0;
      int var7 = var3 == null ? (var2.sourceEvolvePetIndex >= 0 && var2.sourceEvolvePetIndex < var2.session.pets.roster.size() ? ((PetState)var2.session.pets.roster.get(var2.sourceEvolvePetIndex)).speciesId : -1) : var3.currentSpeciesId;
      int var8 = speciesFormStars(var7);
      int var9 = var3 == null ? 0 : speciesFormStars(var3.targetSpeciesId);
      drawEvolutionPetCard(var0, var1, var2, 14, 74, 89, 64, var3 == null ? currentPetName(var2) : currentName(var3), "Hiện tại", "Lv " + var6 + "  |  " + var8 + " sao", var2.sourceEvolveOldVisualId, false);
      drawEvolutionPetCard(var0, var1, var2, 137, 74, 89, 64, var3 == null ? "Chưa có" : targetName(var3), var4 ? "Dị hóa" : "Sau tiến hóa", var3 == null ? "Không có nhánh" : "Lv " + var6 + "  |  " + var9 + " sao", var2.sourceEvolveNewVisualId, true);
      drawEvolutionArrow(var0, var3 != null, var4);
      if (var2.sourceEvolvePhase == 1) {
         var0.setColor(new Color(10, 45, 61, 220));
         var0.fillRoundRect(73, 75, 94, 62, 9, 9);
         drawSourceEvolutionType0Effect(var0, var2, 75, 77, 90, 59);
      }

      drawEvolutionStats(var0, var1, var2, var3);
      drawEvolutionMaterials(var0, var1, var2, var3);
      drawEvolutionEligibility(var0, var1, var2, var3, var6);
      boolean var10 = evolutionReady(var2, var3, var6);
      drawEvolutionSoftkey(var0, var1, 1, 291, 49, 25, var2.sourceEvolvePhase == 1 ? "Đang..." : (var10 ? "Xác nhận" : "Kiểm tra"), var2.sourceEvolvePhase == 1 ? 7505034 : (var10 ? 2857842 : 9208177));
      drawEvolutionSoftkey(var0, var1, 190, 291, 49, 25, "Quay lại", 2651542);
   }

   private static void drawEvolutionPetCard(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2, int var3, int var4, int var5, int var6, String var7, String var8, String var9, int var10, boolean var11) {
      var0.setColor(new Color(var11 ? 15400435 : 15268095));
      var0.fillRoundRect(var3, var4, var5, var6, 8, 8);
      var0.setColor(new Color(var11 ? 5811587 : 5087166));
      var0.drawRoundRect(var3, var4, var5 - 1, var6 - 1, 8, 8);
      drawCenteredTinyText(var0, var1, var8, var3 + 3, var4 + 1, var5 - 6, new Color(var11 ? 2585684 : 2319745));
      if (var10 >= 0) {
         Shape var12 = var0.getClip();
         var0.clipRect(var3 + 15, var4 + 13, var5 - 30, 35);
         drawBattleSprite(var0, var10, var3 + 15, var4 + 10, var5 - 30, 40, 7, 0, 0, idleCursor(var10, 0, var2.battleAnimationTick));
         var0.setClip(var12);
      }

      drawMarqueeTinyBattleText(var0, var1, var7, var3 + 4, var4 + 45, var5 - 8, new Color(1523279), var2.battleAnimationTick);
      drawCenteredTinyText(var0, var1, var9, var3 + 4, var4 + 55, var5 - 8, new Color(4355206));
   }

   private static void drawEvolutionArrow(Graphics2D var0, boolean var1, boolean var2) {
      Color var3 = new Color(!var1 ? 10071732 : (var2 ? 9395636 : 2923388));
      var0.setColor(var3);
      var0.fillRoundRect(108, 99, 18, 6, 5, 5);
      int[] var4 = new int[]{124, 124, 133};
      int[] var5 = new int[]{94, 110, 102};
      var0.fillPolygon(var4, var5, 3);
      var0.setColor(new Color(16777215));
      var0.drawLine(110, 100, 124, 100);
   }

   private static void drawEvolutionStats(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2, EvolutionCandidate var3) {
      byte var4 = 14;
      short var5 = 142;
      short var6 = 212;
      var0.setColor(new Color(16121343));
      var0.fillRoundRect(var4, var5, var6, 69, 7, 7);
      var0.setColor(new Color(7387080));
      var0.drawRoundRect(var4, var5, var6 - 1, 68, 7, 7);
      var0.setColor(new Color(13233651));
      var0.fillRoundRect(var4 + 1, var5 + 1, var6 - 2, 14, 6, 6);
      var0.fillRect(var4 + 1, var5 + 8, var6 - 2, 7);
      drawTinyBattleText(var0, var1, "Chỉ số", 19, 143, 49, new Color(2580080));
      drawCenteredTinyText(var0, var1, "Trước", 69, 143, 37, new Color(2580080));
      drawCenteredTinyText(var0, var1, "Sau", 122, 143, 37, new Color(2580080));
      drawCenteredTinyText(var0, var1, "Thay đổi", 166, 143, 55, new Color(2580080));
      var0.setColor(new Color(10933471));
      var0.drawLine(67, var5 + 2, 67, var5 + 66);
      var0.drawLine(108, var5 + 2, 108, var5 + 66);
      var0.drawLine(162, var5 + 2, 162, var5 + 66);
      String[] var7 = new String[]{"Sinh mệnh", "Sức mạnh", "Phòng ngự", "Linh xảo"};
      int[] var8 = new int[]{157, 170, 183, 196};

      for(int var9 = 0; var9 < var7.length; ++var9) {
         if ((var9 & 1) == 1) {
            var0.setColor(new Color(14939898));
            var0.fillRect(var4 + 1, var8[var9] - 1, var6 - 2, 13);
         }

         drawTinyBattleText(var0, var1, var7[var9], 19, var8[var9], 45, new Color(2381674));
         int var10 = statAt(var2.sourceEvolveOldStats, var9);
         int var11 = var3 == null ? 0 : statAt(var2.sourceEvolveNewStats, var9);
         drawCenteredTinyText(var0, var1, String.valueOf(var10), 70, var8[var9], 35, new Color(2706778));
         drawCenteredTinyText(var0, var1, var3 == null ? "--" : String.valueOf(var11), 123, var8[var9], 35, new Color(2706778));
         if (var3 != null) {
            int var12 = var11 - var10;
            String var13 = var12 > 0 ? "+" + var12 : String.valueOf(var12);
            Color var14 = var12 > 0 ? new Color(1673044) : (var12 < 0 ? new Color(12863303) : new Color(6322046));
            drawCenteredTinyText(var0, var1, var13, 166, var8[var9], 55, var14);
         }
      }

   }

   private static void drawEvolutionMaterials(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2, EvolutionCandidate var3) {
      drawTinyBattleText(var0, var1, "Nguyên liệu", 16, 213, 74, new Color(2645616));
      if (var3 != null && !var3.ingredients.isEmpty()) {
         int var4 = Math.min(4, var3.ingredients.size());

         for(int var5 = 0; var5 < var4; ++var5) {
            EvolutionCandidate.Ingredient var6 = (EvolutionCandidate.Ingredient)var3.ingredients.get(var5);
            int var7 = var5 & 1;
            int var8 = var5 >> 1;
            int var9 = 14 + var7 * 107;
            int var10 = 225 + var8 * 20;
            int var11 = VqsvSourceEvolutionRuntime.materialCount(var2, var6.materialId);
            boolean var12 = var11 >= var6.quantity;
            var0.setColor(new Color(var12 ? 15137005 : 16772846));
            var0.fillRoundRect(var9, var10, 105, 18, 6, 6);
            var0.setColor(new Color(var12 ? 5613434 : 13003370));
            var0.drawRoundRect(var9, var10, 104, 17, 6, 6);
            ItemDefinition var13 = VqsvSourceOps.sourceMaterialItem(var6.materialId);
            if (!UnifiedItemIconRenderer.draw(var0, var13.iconResource, var9 + 3, var10 + 2, 13, 13)) {
               drawSpriteCellTopLeft(var0, 258, var13.iconCell, var9 + 3, var10 + 2);
            }

            String var14 = var6.name.isEmpty() ? var13.name : var6.name;
            drawMarqueeTinyBattleText(var0, var1, var14, var9 + 19, var10 + 1, 50, new Color(2641241), var2.battleAnimationTick + var5 * 13);
            String var15 = var2.sourceEvolvePhase > 0 ? "Đã trừ" : var11 + "/" + var6.quantity;
            drawCenteredTinyText(var0, var1, var15, var9 + 69, var10 + 1, 33, new Color(var2.sourceEvolvePhase > 0 ? 2651542 : (var12 ? 1673044 : 12863303)));
         }

      } else {
         drawTinyBattleText(var0, var1, "Không có công thức phù hợp", 91, 213, 132, new Color(9070439));
      }
   }

   private static void drawEvolutionEligibility(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2, EvolutionCandidate var3, int var4) {
      boolean var5 = evolutionReady(var2, var3, var4);
      int var6 = var5 ? 2326369 : 11818325;
      if (var2.sourceEvolvePhase == 1) {
         var6 = 2651542;
      }

      var0.setColor(new Color(var2.sourceEvolvePhase == 1 ? 14807544 : (var5 ? 14677736 : 16771561)));
      var0.fillRoundRect(14, 266, 212, 17, 7, 7);
      var0.setColor(new Color(var6));
      var0.drawRoundRect(14, 266, 211, 16, 7, 7);
      drawCenteredTinyText(var0, var1, evolutionEligibilityText(var2, var3, var4), 18, 267, 204, new Color(var6));
   }

   static boolean evolutionReady(VqsvGameRuntime.Scene var0, EvolutionCandidate var1, int var2) {
      if (var1 != null && var1.available() && var2 >= var1.requiredLevel) {
         if (currentEvolutionQuality(var0) < var1.requiredQuality) {
            return false;
         } else {
            for(EvolutionCandidate.Ingredient var4 : var1.ingredients) {
               if (VqsvSourceEvolutionRuntime.materialCount(var0, var4.materialId) < var4.quantity) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   static String evolutionEligibilityText(VqsvGameRuntime.Scene var0, EvolutionCandidate var1, int var2) {
      if (var0.sourceEvolvePhase == 1) {
         return "Đang chuyển hóa...";
      } else if (var0.sourceEvolveSucceeded) {
         return "Chuyển hóa thành công";
      } else if (var1 != null && var1.available()) {
         if (var2 < var1.requiredLevel) {
            return "Cần cấp " + var1.requiredLevel + "  (hiện tại " + var2 + ")";
         } else {
            int var3 = currentEvolutionQuality(var0);
            if (var3 < var1.requiredQuality) {
               return "Cần " + var1.requiredQuality + " sao  (hiện tại " + var3 + ")";
            } else {
               for(EvolutionCandidate.Ingredient var5 : var1.ingredients) {
                  int var6 = VqsvSourceEvolutionRuntime.materialCount(var0, var5.materialId);
                  if (var6 < var5.quantity) {
                     String var7 = var5.name.isEmpty() ? VqsvSourceOps.sourceMaterialItem(var5.materialId).name : var5.name;
                     return "Thiếu " + var7 + ": " + var6 + "/" + var5.quantity;
                  }
               }

               return var1.kind == EvolutionCandidate.Kind.MUTATION ? "Đủ điều kiện dị hóa" : "Đủ điều kiện tiến hóa";
            }
         }
      } else {
         return "Không có nhánh phù hợp";
      }
   }

   private static int currentEvolutionQuality(VqsvGameRuntime.Scene var0) {
      return var0.sourceEvolvePetIndex >= 0 && var0.sourceEvolvePetIndex < var0.session.pets.roster.size() ? ((PetState)var0.session.pets.roster.get(var0.sourceEvolvePetIndex)).quality : 1;
   }

   private static void drawEvolutionPill(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, int var6, int var7) {
      var0.setColor(new Color(var6));
      var0.fillRoundRect(var3, var4, var5, 15, 8, 8);
      drawCenteredTinyText(var0, var1, var2, var3 + 2, var4 + 1, var5 - 4, new Color(var7));
   }

   private static void drawEvolutionSoftkey(Graphics2D var0, UiFont var1, int var2, int var3, int var4, int var5, String var6, int var7) {
      var0.setColor(new Color(800070));
      var0.fillRoundRect(var2 + 1, var3 + 1, var4, var5, 8, 8);
      var0.setColor(new Color(var7));
      var0.fillRoundRect(var2, var3, var4, var5, 8, 8);
      drawCenteredTinyText(var0, var1, var6, var2 + 2, var3 + 5, var4 - 4, Color.WHITE);
   }

   private static void drawSourceEvolutionType0Effect(Graphics2D var0, VqsvGameRuntime.Scene var1, int var2, int var3, int var4, int var5) {
      int var6 = Math.max(0, Math.min(9, var1.sourceEvolveEffectTicks));
      boolean var7 = var6 >= 2 && var6 < 8;
      boolean var8 = var7 && var6 % 4 != 1 && var6 % 4 != 2;
      if (var6 >= 8) {
         var8 = true;
      }

      int var9 = var8 ? var1.sourceEvolveNewVisualId : var1.sourceEvolveOldVisualId;
      if (var9 < 0) {
         var9 = var8 ? var1.sourceEvolveOldVisualId : var1.sourceEvolveNewVisualId;
      }

      if (var9 >= 0) {
         int var10;
         int var11;
         if (var7) {
            var10 = var6 % 2 == 1 ? 8 : 4;
            var11 = var6 % 2 == 1 ? 8 : 4;
         } else {
            var10 = var6 % 2 == 1 ? 6 : 10;
            var11 = var6 % 2 == 1 ? 5 : 2;
         }

         BufferedImage var12 = renderSpriteAnimationFrameImage(var9, 0, 0, 0);
         if (var12 != null) {
            BufferedImage var13 = sourceMultiplyAddCopy(var12, var11, 1);
            int var14 = Math.max(1, var13.getWidth() * var10 / 10);
            int var15 = Math.max(1, var13.getHeight() * var10 / 10);
            int var16 = var2 + (var4 - var14) / 2;
            int var17 = var3 + var5 - var15;
            Shape var18 = var0.getClip();
            Object var19 = var0.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
            var0.clipRect(var2, var3, var4, var5);
            var0.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            var0.drawImage(var13, var16, var17, var14, var15, (ImageObserver)null);
            if (var19 == null) {
               var0.getRenderingHints().remove(RenderingHints.KEY_INTERPOLATION);
            } else {
               var0.setRenderingHint(RenderingHints.KEY_INTERPOLATION, var19);
            }

            var0.setClip(var18);
         }
      }
   }

   private static BufferedImage sourceMultiplyAddCopy(BufferedImage var0, int var1, int var2) {
      BufferedImage var3 = new BufferedImage(var0.getWidth(), var0.getHeight(), 2);

      for(int var4 = 0; var4 < var0.getHeight(); ++var4) {
         for(int var5 = 0; var5 < var0.getWidth(); ++var5) {
            int var6 = var0.getRGB(var5, var4);
            if (var6 >>> 24 != 0 && var6 != 16777215) {
               int var7 = var6 >>> 24;
               int var8 = Math.min(255, (var6 >>> 16 & 255) * var1 + var2);
               int var9 = Math.min(255, (var6 >>> 8 & 255) * var1 + var2);
               int var10 = Math.min(255, (var6 & 255) * var1 + var2);
               var3.setRGB(var5, var4, var7 << 24 | var8 << 16 | var9 << 8 | var10);
            } else {
               var3.setRGB(var5, var4, 16777215);
            }
         }
      }

      return var3;
   }

   private static int statAt(int[] var0, int var1) {
      return var0 != null && var1 >= 0 && var1 < var0.length ? var0[var1] : 0;
   }

   private static String currentName(EvolutionCandidate var0) {
      BattleSpeciesRow var1 = VqsvBattleTables.instance().species(var0.currentSpeciesId);
      return var1 == null ? "Pet " + var0.currentSpeciesId : var1.name("Pet " + var0.currentSpeciesId);
   }

   private static String targetName(EvolutionCandidate var0) {
      BattleSpeciesRow var1 = VqsvBattleTables.instance().species(var0.targetSpeciesId);
      return var1 == null ? "Pet " + var0.targetSpeciesId : var1.name("Pet " + var0.targetSpeciesId);
   }

   private static String currentPetName(VqsvGameRuntime.Scene var0) {
      if (var0.sourceEvolvePetIndex >= 0 && var0.sourceEvolvePetIndex < var0.session.pets.roster.size()) {
         int var1 = ((PetState)var0.session.pets.roster.get(var0.sourceEvolvePetIndex)).speciesId;
         BattleSpeciesRow var2 = VqsvBattleTables.instance().species(var1);
         return var2 == null ? "Pet " + var1 : var2.name("Pet " + var1);
      } else {
         return "";
      }
   }

   private static void drawPetStateStaticWidgets(Graphics2D var0, UiFont var1, VqsvUiLayout var2, VqsvGameRuntime.Scene var3, VqsvChoiceUiView var4) {
      VqsvBattlePetStateView var5 = selectedPetStateView(var3, var4);
      int var6 = petStateAccent(var5 == null ? -1 : var5.elementId);
      int var7 = petstateUiTick(var3);
      var0.setColor(new Color(399396));
      var0.fillRoundRect(18, 44, 208, 249, 13, 13);
      var0.setColor(new Color(15333108));
      var0.fillRoundRect(15, 40, 208, 249, 12, 12);
      var0.setColor(new Color(darkerColor(var6)));
      var0.fillRoundRect(15, 40, 208, 33, 10, 10);
      var0.fillRect(15, 60, 208, 13);
      var0.setColor(new Color(var6));
      var0.fillRect(15, 70, 208, 3);
      int var8 = 43 + var7 * 3 % 148;
      var0.setColor(new Color(255, 255, 255, 42));
      var0.fillRoundRect(var8, 43, 24, 14, 8, 8);
      var0.setColor(new Color(lighterColor(var6)));
      var0.drawRoundRect(15, 40, 207, 248, 12, 12);
      var0.setColor(new Color(16777215));
      var0.drawRoundRect(17, 42, 203, 244, 9, 9);
      drawSourceUiFill(var0, 25, 81, 188, 132, 14676206);
      var0.setColor(new Color(12113876));

      for(int var9 = 33; var9 < 213; var9 += 16) {
         var0.drawLine(var9, 81, var9, 212);
      }

      drawChoiceFrame(var0, 25, 81, 188, 132);
      drawSourceUiFill(var0, 25, 216, 188, 41, 1591642);
      drawChoiceFrame(var0, 25, 216, 188, 41);
      drawPetStateHeaderBackArrow(var0);
      drawMarqueeTinyBattleText(var0, var1, "HỒ SƠ SỦNG VẬT", 59, 58, 107, Color.WHITE, var7);
      String var10 = var4.size() == 0 ? "0/0" : Math.max(0, var4.selectedIndex) + 1 + "/" + var4.size();
      drawPetStatePill(var0, var1, var10, 174, 53, 28, 14, var6, 16777215);
      drawTinyBattleText(var0, var1, "Đội hình nhanh", 31, 218, 92, new Color(16251903));
      drawTinyBattleText(var0, var1, "chọn để xem", 128, 218, 69, new Color(10409434));
   }

   private static void drawPetStateHeaderBackArrow(Graphics2D var0) {
      int[] var1 = new int[]{33, 43, 43, 51, 51, 43, 43};
      int[] var2 = new int[]{61, 53, 57, 57, 65, 65, 69};
      var0.setColor(new Color(600892));
      var0.fillPolygon(var1, var2, var1.length);
      var0.setColor(new Color(16765802));
      int[] var3 = new int[]{35, 43, 43, 49, 49, 43, 43};
      int[] var4 = new int[]{61, 55, 58, 58, 64, 64, 67};
      var0.fillPolygon(var3, var4, var3.length);
      var0.setColor(new Color(16777215));
      var0.drawLine(38, 61, 48, 61);
   }

   private static void drawPetStateColorBand(Graphics2D var0, VqsvUiLayout var1, int var2, int var3, int var4) {
      VqsvUiLayout.UiWidget var5 = var1.widget(var2);
      if (var5 != null) {
         drawSourceUiFill(var0, var5.x, var5.y, var5.w, var1.bandHeight(var2, var3), widgetColor(var5, false, var4, false));
      }

   }

   private static void drawPetStateWidgetCell(Graphics2D var0, VqsvUiLayout var1, int var2) {
      drawPetStateWidgetCell(var0, var1, var2, false);
   }

   private static void drawPetStateWidgetCell(Graphics2D var0, VqsvUiLayout var1, int var2, boolean var3) {
      VqsvUiLayout.UiWidget var4 = var1.widget(var2);
      if (var4 != null) {
         int var5 = var3 && var4.imageId >= 0 ? var4.imageId : var4.altId;
         int var6 = var3 && var4.imageId >= 0 ? var4.imageMode : var4.altMode;
         if (var5 < 0 && var4.imageId >= 0) {
            var5 = var4.imageId;
            var6 = var4.imageMode;
         }

         if (var5 >= 0) {
            if (var6 == 3) {
               drawBattleUiStateTopLeft(var0, var5, var4.x, var4.y);
            } else {
               drawBattleUiCellTopLeft(var0, var5, var4.x, var4.y);
            }

         }
      }
   }

   private static void drawPetStateArrows(Graphics2D var0, VqsvUiLayout var1, VqsvChoiceUiView var2) {
      if (var2.selectedIndex > 0) {
         drawPetStateArrow(var0, 203, 225, true);
      }

      if (var2.selectedIndex < var2.size() - 1) {
         drawPetStateArrow(var0, 203, 244, false);
      }

   }

   private static void drawPetStatePageTabs(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2) {
      PetStateDetailUiState.Page var3 = var2.battlePetStateDetailUi.page();
      drawPetStatePageTab(var0, var1, "Hồ sơ", 30, 73, var3 == PetStateDetailUiState.Page.OVERVIEW);
      drawPetStatePageTab(var0, var1, "Chỉ số", 74, 116, var3 == PetStateDetailUiState.Page.STATS);
      drawPetStatePageTab(var0, var1, "Đặc tính", 117, 161, var3 == PetStateDetailUiState.Page.TRAITS);
      drawPetStatePageTab(var0, var1, "Kỹ năng", 162, 207, var3 == PetStateDetailUiState.Page.SKILLS);
   }

   private static void drawPetStatePageTab(Graphics2D var0, UiFont var1, String var2, int var3, int var4, boolean var5) {
      int var6 = Math.max(1, var4 - var3);
      byte var7 = 11;
      drawSourceUiFill(var0, var3, 68, var6, var7, var5 ? 16765802 : 11196636);
      var0.setColor(new Color(var5 ? 6046231 : 6134948));
      var0.drawRect(var3, 68, var6 - 1, var7 - 1);
      Color var8 = var5 ? new Color(6046231) : new Color(2382443);
      drawTinyBattleText(var0, var1, var2, var3 + 2, 68, var6 - 4, var8);
   }

   private static void drawPetStateRow(Graphics2D var0, UiFont var1, VqsvUiLayout var2, VqsvGameRuntime.Scene var3, VqsvChoiceUiView var4, int var5) {
      int var6 = var5 % 3;
      int var7 = var5 / 3;
      int var8 = 32 + var6 * 60;
      int var9 = 228 + var7 * 18;
      byte var10 = 52;
      byte var11 = 15;
      int var12 = petStateMenuIndexForRow(var4, var5);
      boolean var13 = var12 == var4.selectedIndex;
      VqsvBattlePetStateView var14 = petStateViewAt(var3, var5);
      if (var14 != null && var14.visible) {
         int var15 = petStateAccent(var14.elementId);
         if (var13) {
            drawSourceUiFill(var0, var8 + 2, var9 + 2, var10, var11, 466733);
         }

         drawSourceUiFill(var0, var8, var9, var10, var11, var13 ? darkerColor(var15) : (var14.alive ? 15529971 : 9413537));
         drawSourceUiFill(var0, var8, var9, 4, var11, var14.active ? 5429385 : (var13 ? var15 : 6662578));
         if (var13 && (petstateUiTick(var3) / 4 & 1) == 0) {
            drawSourceUiFill(var0, var8 + 5, var9 + 1, var10 - 7, 2, lighterColor(var15));
         }

         var0.setColor(new Color(var13 ? 16769930 : 8369846));
         var0.drawRect(var8, var9, var10 - 1, var11 - 1);
         Color var16 = var13 ? Color.WHITE : (var14.alive ? new Color(2116696) : new Color(6120805));
         drawTinyBattleText(var0, var1, String.valueOf(Math.max(0, var12) + 1), var8 + 6, var9 + 2, 9, var13 ? new Color(16769930) : new Color(10967356));
         String var17 = var14.name != null && !var14.name.isEmpty() ? var14.name : (var12 >= 0 ? var4.nameAt(var12) : "");
         drawMarqueeTinyBattleText(var0, var1, var17, var8 + 16, var9 + 2, 31, var16, var3.battleAnimationTick);
         drawPetStateMiniGauge(var0, var8 + 16, var9 + 12, 29, 2, var14.hpPercent, var14.alive ? 5622151 : 9071210, 10269357);
      } else {
         drawPetStateEmptyRow(var0, var8, var9, var10, var11);
      }
   }

   private static void drawPetStateDetails(Graphics2D var0, UiFont var1, VqsvUiLayout var2, VqsvGameRuntime.Scene var3, VqsvChoiceUiView var4) {
      VqsvBattlePetStateView var5 = selectedPetStateView(var3, var4);
      if (var5 != null && var5.visible) {
         switch (var3.battlePetStateDetailUi.page()) {
            case STATS:
               drawPetStateStatDetails(var0, var1, var3, var5);
               return;
            case TRAITS:
               drawPetStateTraitDetails(var0, var1, var3, var5);
               return;
            case SKILLS:
               drawPetStateSkillDetails(var0, var1, var3, var5);
               return;
            case OVERVIEW:
            default:
               drawPetStateOverview(var0, var1, var3, var5);
         }
      }
   }

   private static void drawPetStateOverview(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2, VqsvBattlePetStateView var3) {
      int var4 = petstateUiTick(var2);
      int var5 = petStateAccent(var3.elementId);
      drawSourceUiFill(var0, 29, 84, 180, 29, darkerColor(var5));
      drawSourceUiFill(var0, 29, 111, 180, 2, 16768889);
      drawMarqueeTinyBattleText(var0, var1, var3.name, 36, 87, 96, Color.WHITE, var4);
      String var10002 = blankAsDash(var3.taxonomyElementName);
      drawTinyBattleText(var0, var1, "Hệ " + var10002 + "  •  " + blankAsDash(var3.taxonomySpeciesName), 36, 99, 113, new Color(13233648));
      drawPetStatePill(var0, var1, "Lv " + var3.level, 153, 87, 46, 14, lighterColor(var5), 1521991);
      drawSourceUiFill(var0, 30, 117, 76, 70, 12048853);
      drawSourceUiFill(var0, 32, 119, 72, 66, 15989752);
      var0.setColor(new Color(var5));
      var0.drawRect(30, 117, 75, 69);
      int var6 = 20 + (var4 % 16 < 8 ? var4 % 8 : 15 - var4 % 16);
      var0.setColor(new Color(var5 >> 16 & 255, var5 >> 8 & 255, var5 & 255, 34));
      var0.fillOval(51 - var6 / 4, 133 - var6 / 4, 36 + var6 / 2, 36 + var6 / 2);
      if (var3.visualId >= 0) {
         Shape var7 = var0.getClip();
         var0.clipRect(32, 119, 72, 66);
         int var8 = (var4 / 7 & 1) == 0 ? 0 : -1;
         drawBattleSprite(var0, var3.visualId, 32, 112 + var8, 72, 73, 7, 0, 0, idleCursor(var3.visualId, 0, var2.battleAnimationTick));
         var0.setClip(var7);
      }

      drawPetStateInfoChip(var0, var1, "Chiến", var3.alive ? (var3.active ? "Xuất chiến" : "Sẵn sàng") : "Không thể chiến", 111, 118, 96, var3.alive ? 4367987 : 11752532, var4);
      drawPetStateStarChip(var0, var1, var3, 111, 135, 96, 14064434);
      drawPetStateInfoChip(var0, var1, "Đeo", blankAsDash(var3.heldItemName), 111, 152, 96, 5406640, var4);
      drawPetStateInfoChip(var0, var1, "Hóa", var3.evolutionText != null && !var3.evolutionText.isEmpty() ? compactPetStateEvolution(var3.evolutionText) : "Hình thái cuối", 111, 169, 96, 8807338, var4);
      drawPetStateGauge(var0, 31, 191, 176, 6, var3.hpPercent, new Color(5293180), new Color(10204334));
      drawTinyBattleText(var0, var1, "HP " + var3.hp + "/" + var3.maxHp, 33, 198, 85, new Color(2382443));
      drawPetStateGauge(var0, 31, 207, 121, 3, var3.expPercent, new Color(5149656), new Color(11913412));
      drawTinyBattleText(var0, var1, "EXP " + var3.expPercent + "%", 156, 203, 48, new Color(2382443));
   }

   private static void drawPetStateStatDetails(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2, VqsvBattlePetStateView var3) {
      int var4 = petstateUiTick(var2);
      int var5 = petStateAccent(var3.elementId);
      drawSourceUiFill(var0, 29, 84, 180, 25, darkerColor(var5));
      drawMarqueeTinyBattleText(var0, var1, var3.name, 36, 88, 80, Color.WHITE, var4);
      drawPetQualityStars(var0, 122, 90, var3);
      int var10002 = var3.level;
      drawPetStatePill(var0, var1, "Lv " + var10002, 164, 87, 37, 14, lighterColor(var5), 1521991);
      drawPetStateStatDashboardCard(var0, var1, "HP", var3.maxHp, 30, 114, 85, 38, 5160824);
      drawPetStateStatDashboardCard(var0, var1, "SỨC MẠNH", var3.attack, 121, 114, 86, 38, 13983061);
      drawPetStateStatDashboardCard(var0, var1, "PHÒNG NGỰ", var3.defense, 30, 157, 85, 38, 4886465);
      drawPetStateStatDashboardCard(var0, var1, "LINH XẢO", var3.speed, 121, 157, 86, 38, 12814902);
      drawSourceUiFill(var0, 30, 200, 177, 10, 1523542);
      var10002 = var3.hp;
      drawTinyBattleText(var0, var1, "HP " + var10002 + "/" + var3.maxHp, 34, 201, 76, new Color(14283762));
      var10002 = var3.expPercent;
      drawTinyBattleText(var0, var1, "EXP " + var10002 + "%", 111, 201, 42, new Color(12181503));
      var10002 = var3.buffCount;
      drawTinyBattleText(var0, var1, "B" + var10002 + " / D" + var3.debuffCount, 157, 201, 46, new Color(var3.debuffCount > 0 ? 16757417 : 13101785));
   }

   private static void drawPetStateTraitDetails(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2, VqsvBattlePetStateView var3) {
      int var4 = petstateUiTick(var2);
      int var5 = petStateAccent(var3.elementId);
      drawSourceUiFill(var0, 30, 84, 178, 27, darkerColor(var5));
      drawSourceUiFill(var0, 30, 109, 178, 2, lighterColor(var5));
      drawMarqueeTinyBattleText(var0, var1, var3.name, 36, 88, 74, Color.WHITE, var4);
      String var10002 = var3.taxonomyElementName;
      drawMarqueeTinyBattleText(var0, var1, "Hệ: " + var10002, 114, 88, 86, new Color(12380142), var4);
      var10002 = var3.taxonomySpeciesName;
      drawMarqueeTinyBattleText(var0, var1, "Chủng: " + var10002, 36, 99, 164, new Color(12380142), var4 + 17);
      boolean var6 = var2.battlePetStateDetailUi.traitDetail() == PetStateDetailUiState.TraitDetail.SPECIES;
      var10002 = var3.speciesTraitName;
      drawPetStateTraitRow(var0, var1, "Đặc tính: " + var10002, 113, var6, var4);
      var10002 = var3.physicalTraitName;
      drawPetStateTraitRow(var0, var1, "Thiên phú: " + var10002, 128, !var6, var4);
      String var7 = var6 ? var3.speciesTraitKey : var3.physicalTraitKey;
      String var8 = var6 ? var3.speciesTraitDescription : var3.physicalTraitDescription;
      String var9 = var6 ? "Mô tả đặc tính" : "Mô tả thiên phú";
      drawSourceUiFill(var0, 30, 148, 176, 62, 15924217);
      drawSourceUiFill(var0, 30, 148, 176, 12, var6 ? 2980994 : 13985106);
      var0.setColor(new Color(8239288));
      var0.drawRect(30, 148, 175, 61);
      drawMarqueeTinyBattleText(var0, var1, var7 != null && !var7.isEmpty() ? var9 + " " + var7 : var9, 36, 150, 164, Color.WHITE, var4);
      drawWrappedPetStateText(var0, var1, var8, 36, 164, 164, 41, var4);
   }

   private static void drawPetStateSkillDetails(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2, VqsvBattlePetStateView var3) {
      int var4 = petstateUiTick(var2);
      int var5 = petStateAccent(var3.elementId);
      int var6 = 0;

      for(int var10 : var3.skillIds) {
         if (var10 >= 0) {
            ++var6;
         }
      }

      drawSourceUiFill(var0, 29, 84, 180, 25, darkerColor(var5));
      drawMarqueeTinyBattleText(var0, var1, var3.name, 36, 88, 93, Color.WHITE, var4);
      drawPetStatePill(var0, var1, var6 + "/5 chiêu", 143, 87, 58, 14, lighterColor(var5), 1521991);

      for(int var15 = 0; var15 < 5; ++var15) {
         int var16 = 113 + var15 * 19;
         int var17 = var15 < var3.skillIds.length ? var3.skillIds[var15] : -1;
         BattleSkillRow var18 = var17 < 0 ? null : VqsvBattleTables.instance().skill(var17);
         if (var18 == null) {
            drawSourceUiFill(var0, 30, var16, 177, 17, 13097430);
            var0.setColor(new Color(10204851));
            var0.drawRect(30, var16, 176, 16);
            drawTinyBattleText(var0, var1, String.valueOf(var15 + 1), 35, var16 + 3, 9, new Color(7506827));
            drawTinyBattleText(var0, var1, "Chưa trang bị kỹ năng", 51, var16 + 3, 116, new Color(7506827));
         } else {
            int var11 = skillElementColor(var18.elementFamily);
            int var12 = var15 < var3.skillPp.length ? Math.max(0, var3.skillPp[var15]) : 0;
            int var13 = Math.max(1, var18.ppMax);
            drawSourceUiFill(var0, 30, var16, 177, 17, 16055288);
            drawSourceUiFill(var0, 30, var16, 5, 17, var11);
            var0.setColor(new Color(lighterColor(var11)));
            var0.drawRect(30, var16, 176, 16);
            drawPetStateSlotBadge(var0, var1, var15 + 1, 37, var16 + 2, var11);
            if (var18.elementFamily >= 0) {
               drawBattleUiCellTopLeft(var0, 94 + var18.elementFamily, 51, var16 + 3);
            }

            drawMarqueeTinyBattleText(var0, var1, var18.name("Kỹ năng " + var17), 66, var16 + 3, 76, new Color(1855070), var4 + var15 * 11);
            String var14 = var12 + "/" + var13;
            drawTinyBattleText(var0, var1, var14, 161, var16 + 3, 41, var12 > 0 ? new Color(2584455) : new Color(11750990));
            drawPetStateMiniGauge(var0, 148, var16 + 13, 54, 2, var12 * 100 / var13, var12 > 0 ? var11 : 10123127, 11452348);
         }
      }

   }

   private static void drawPetStateTraitRow(Graphics2D var0, UiFont var1, String var2, int var3, boolean var4, int var5) {
      drawSourceUiFill(var0, 30, var3, 176, 14, var4 ? 16765802 : 14282733);
      drawSourceUiFill(var0, 30, var3, 5, 14, var4 ? 11883090 : 7254711);
      var0.setColor(new Color(var4 ? 6046231 : 7711401));
      var0.drawRect(30, var3, 175, 13);
      drawMarqueeTinyBattleText(var0, var1, var2, 39, var3 + 2, 160, var4 ? new Color(6046231) : new Color(2382443), var5);
   }

   private static void drawPetStateEmptyRow(Graphics2D var0, int var1, int var2, int var3, int var4) {
      drawSourceUiFill(var0, var1, var2, var3, var4, 14017503);
      var0.setColor(new Color(10796733));
      var0.drawRect(var1, var2, var3 - 1, var4 - 1);
      drawPetStateMiniGauge(var0, var1 + 17, var2 + 11, 29, 2, 0, 9545384, 12044743);
      drawPetStateMiniGauge(var0, var1 + 48, var2 + 11, 14, 2, 0, 9545384, 12044743);
   }

   private static void drawPetStateMiniGauge(Graphics2D var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      drawSourceUiFill(var0, var1, var2, var3, var4, var7);
      int var8 = Math.max(0, Math.min(var3, var3 * Math.max(0, Math.min(100, var5)) / 100));
      if (var8 > 0) {
         drawSourceUiFill(var0, var1, var2, var8, var4, var6);
      }

   }

   private static void drawPetStateArrow(Graphics2D var0, int var1, int var2, boolean var3) {
      int[] var4 = new int[]{var1 + 1, var1 + 5, var1 + 9};
      int[] var5 = var3 ? new int[]{var2 + 8, var2 + 2, var2 + 8} : new int[]{var2 + 2, var2 + 8, var2 + 2};
      var0.setColor(new Color(600892));
      var0.fillPolygon(var4, var5, 3);
      var0.setColor(new Color(16765802));
      var0.drawPolygon(var4, var5, 3);
   }

   private static void drawPetStatePill(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      var0.setColor(new Color(var7));
      var0.fillRoundRect(var3, var4, var5, var6, 7, 7);
      var0.setColor(new Color(16777215));
      var0.drawRoundRect(var3, var4, var5 - 1, var6 - 1, 7, 7);
      drawCenteredTinyText(var0, var1, var2, var3 + 2, var4 + 1, var5 - 4, new Color(var8));
   }

   private static void drawPetStateInfoChip(Graphics2D var0, UiFont var1, String var2, String var3, int var4, int var5, int var6, int var7, int var8) {
      drawSourceUiFill(var0, var4, var5, var6, 15, 16120824);
      drawSourceUiFill(var0, var4, var5, 35, 15, var7);
      var0.setColor(new Color(lighterColor(var7)));
      var0.drawRect(var4, var5, var6 - 1, 14);
      drawCenteredTinyText(var0, var1, var2, var4 + 3, var5 + 2, 29, Color.WHITE);
      drawMarqueeTinyBattleText(var0, var1, var3, var4 + 40, var5 + 2, var6 - 44, new Color(1523542), var8);
   }

   private static void drawPetStateStarChip(Graphics2D var0, UiFont var1, VqsvBattlePetStateView var2, int var3, int var4, int var5, int var6) {
      drawSourceUiFill(var0, var3, var4, var5, 15, 16120824);
      drawSourceUiFill(var0, var3, var4, 35, 15, var6);
      var0.setColor(new Color(lighterColor(var6)));
      var0.drawRect(var3, var4, var5 - 1, 14);
      drawCenteredTinyText(var0, var1, "Sao", var3 + 3, var4 + 2, 29, Color.WHITE);
      int var7 = petStateDisplayedStarCount(var2);
      byte var8 = 8;
      int var9 = var7 <= 0 ? 0 : var7 * var8 - 1;
      int var10 = var3 + 40;
      int var11 = var5 - 44;
      int var12 = var10 + Math.max(0, (var11 - var9) / 2);

      for(int var13 = 0; var13 < var7; ++var13) {
         drawPetStateStarGlyph(var0, var12 + var13 * var8, var4 + 5, true);
      }

   }

   private static void drawPetStateStatDashboardCard(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      drawSourceUiFill(var0, var4 + 2, var5 + 2, var6, var7, 9087141);
      drawSourceUiFill(var0, var4, var5, var6, var7, 16186361);
      drawSourceUiFill(var0, var4, var5, var6, 5, var8);
      var0.setColor(new Color(lighterColor(var8)));
      var0.drawRect(var4, var5, var6 - 1, var7 - 1);
      drawCenteredTinyText(var0, var1, var2, var4 + 5, var5 + 9, var6 - 10, new Color(4614768));
      drawCenteredTinyText(var0, var1, String.valueOf(var3), var4 + 5, var5 + 23, var6 - 10, new Color(1523542));
   }

   private static void drawPetStateSlotBadge(Graphics2D var0, UiFont var1, int var2, int var3, int var4, int var5) {
      var0.setColor(new Color(var5));
      var0.fillOval(var3, var4, 11, 11);
      var0.setColor(Color.WHITE);
      var0.drawOval(var3, var4, 10, 10);
      drawCenteredTinyText(var0, var1, String.valueOf(var2), var3 + 1, var4, 9, Color.WHITE);
   }

   private static int petStateAccent(int var0) {
      return skillElementColor(var0);
   }

   private static void drawPetStateSoftkey(Graphics2D var0, UiFont var1, int var2, int var3, int var4, int var5, String var6, boolean var7, int var8) {
      drawSourceUiFill(var0, var2 + 2, var3 + 2, var4, var5, 600892);
      drawSourceUiFill(var0, var2, var3, var4, var5, var7 ? 13985106 : 3573924);
      drawSourceUiFill(var0, var2 + 2, var3 + 2, var4 - 4, 2, var7 ? 16756883 : 9556444);
      var0.setColor(new Color(var7 ? 16765802 : 12315376));
      var0.drawRect(var2, var3, var4 - 1, var5 - 1);
      drawSourceWidgetText(var0, var1, var6, var2 + 2, var3, var4 - 4, var5, Color.WHITE, var8, 4);
   }

   private static void drawPetStateStatCard(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      drawSourceUiFill(var0, var4, var5, var6, var7, 16317431);
      drawSourceUiFill(var0, var4, var5, 4, var7, var8);
      var0.setColor(new Color(lighterColor(var8)));
      var0.drawRect(var4, var5, var6 - 1, var7 - 1);
      drawTinyBattleText(var0, var1, var2, var4 + 6, var5 + 1, 21, new Color(2382443));
      drawSourceWidgetText(var0, var1, String.valueOf(var3), var4 + 26, var5 + 1, var6 - 29, var7 - 1, new Color(1523542), 0, 2);
   }

   private static void drawPetStateMetricRow(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, int var6, int var7) {
      drawSourceUiFill(var0, var4, var5, var6, 14, 16317431);
      drawSourceUiFill(var0, var4, var5, 4, 14, var7);
      var0.setColor(new Color(lighterColor(var7)));
      var0.drawRect(var4, var5, var6 - 1, 13);
      drawTinyBattleText(var0, var1, var2, var4 + 7, var5 + 2, 27, new Color(2382443));
      drawSourceWidgetText(var0, var1, String.valueOf(var3), var4 + var6 - 28, var5 + 2, 24, 11, new Color(1523542), 0, 2);
      int var8 = Math.max(4, Math.min(31, var3 * 31 / 160));
      drawSourceUiFill(var0, var4 + 37, var5 + 10, 31, 2, 13031377);
      drawSourceUiFill(var0, var4 + 37, var5 + 10, var8, 2, var7);
   }

   private static void drawPetStateSummaryStrip(Graphics2D var0, UiFont var1, VqsvBattlePetStateView var2, int var3, int var4, int var5, int var6) {
      String var7 = var2.evolutionText != null && !var2.evolutionText.isEmpty() ? compactPetStateEvolution(var2.evolutionText) : "Chưa mở";
      String var8 = var2.heldItemName != null && !var2.heldItemName.isEmpty() ? var2.heldItemName : "Trống";
      String var9 = "Hóa: " + var7 + "  ĐC: " + var8;
      drawMarqueeTinyBattleText(var0, var1, var9, var3, var4, var5, new Color(2382443), var6 + 23);
   }

   private static void drawPetStateSummaryPanel(Graphics2D var0, UiFont var1, VqsvBattlePetStateView var2, int var3, int var4, int var5) {
      drawPetStateSummaryPanel(var0, var1, var2, var3, var4, 69, 42, var5);
   }

   private static void drawPetStateSummaryPanel(Graphics2D var0, UiFont var1, VqsvBattlePetStateView var2, int var3, int var4, int var5, int var6, int var7) {
      drawSourceUiFill(var0, var3, var4, var5, var6, 16186355);
      drawSourceUiFill(var0, var3, var4, var5, 11, 2980994);
      var0.setColor(new Color(7121317));
      var0.drawRect(var3, var4, var5 - 1, var6 - 1);
      drawTinyBattleText(var0, var1, "Hồ sơ", var3 + 4, var4 + 1, var5 - 8, Color.WHITE);
      drawMarqueeTinyBattleText(var0, var1, "Thân: " + blankAsDash(var2.relationText), var3 + 4, var4 + 13, var5 - 8, new Color(2382443), var7);
      String var8 = var2.evolutionText != null && !var2.evolutionText.isEmpty() ? var2.evolutionText : "Chưa mở";
      drawMarqueeTinyBattleText(var0, var1, "Hóa: " + compactPetStateEvolution(var8), var3 + 4, var4 + 23, var5 - 8, new Color(2382443), var7 + 19);
      String var9 = var2.heldItemName != null && !var2.heldItemName.isEmpty() ? var2.heldItemName : "Trống";
      drawMarqueeTinyBattleText(var0, var1, "ĐC: " + var9, var3 + 4, var4 + 33, var5 - 8, new Color(2382443), var7 + 37);
   }

   private static String blankAsDash(String var0) {
      return var0 != null && !var0.isEmpty() ? var0 : "--";
   }

   static int petStateDisplayedStarCount(VqsvBattlePetStateView var0) {
      return var0 == null ? 0 : Math.max(0, Math.min(5, var0.filledStars));
   }

   static int speciesFormStars(int var0) {
      BattleSpeciesRow var1 = VqsvBattleTables.instance().species(var0);
      return var1 == null ? 0 : Math.max(1, Math.min(5, var1.quality));
   }

   private static String compactPetStateEvolution(String var0) {
      String var1 = TextBox.decodeMojibake(var0 == null ? "" : var0);
      return var1.startsWith("Có thể ") ? var1.substring("Có thể ".length()) : var1;
   }

   private static void drawWrappedPetStateText(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, int var6, int var7) {
      List var8 = wrapPetStateText(var1, TextBox.decodeMojibake(var2 == null ? "" : var2), var5);
      byte var9 = 10;
      int var10 = Math.max(1, var6 / var9);
      int var11 = Math.max(0, var8.size() - var10);
      int var12 = var11 == 0 ? 0 : Math.max(0, var7) / 45 % (var11 + 1);
      Shape var13 = var0.getClip();
      var0.clipRect(var3, var4, var5, var6);

      for(int var14 = 0; var14 < var10 && var12 + var14 < var8.size(); ++var14) {
         drawTinyBattleText(var0, var1, (String)var8.get(var12 + var14), var3, var4 + var14 * var9, var5, SOURCE_UI_TEXT);
      }

      var0.setClip(var13);
   }

   private static List<String> wrapPetStateText(UiFont var0, String var1, int var2) {
      ArrayList var3 = new ArrayList();
      StringBuilder var4 = new StringBuilder();

      for(String var8 : var1.trim().split("\\s+")) {
         if (!var8.isEmpty()) {
            String var9 = var4.length() == 0 ? var8 : String.valueOf(var4) + " " + var8;
            if (var4.length() > 0 && var0.taggedWidth(var9) > var2) {
               var3.add(var4.toString());
               var4.setLength(0);
               var4.append(var8);
            } else {
               var4.setLength(0);
               var4.append(var9);
            }
         }
      }

      if (var4.length() > 0) {
         var3.add(var4.toString());
      }

      if (var3.isEmpty()) {
         var3.add("");
      }

      return var3;
   }

   private static VqsvBattlePetStateView selectedPetStateView(VqsvGameRuntime.Scene var0, VqsvChoiceUiView var1) {
      int var2 = Math.max(0, Math.min(5, var1.selectedIndex - var1.visibleStart()));
      return petStateViewAt(var0, var2);
   }

   private static VqsvBattlePetStateView petStateViewAt(VqsvGameRuntime.Scene var0, int var1) {
      return var0.battlePetStateRows != null && var1 >= 0 && var1 < var0.battlePetStateRows.length ? var0.battlePetStateRows[var1] : null;
   }

   private static int petStateMenuIndexForRow(VqsvChoiceUiView var0, int var1) {
      return var1 >= 0 && var1 < var0.visibleCount() ? var0.visibleStart() + var1 : -1;
   }

   private static void drawPetStateGauge(Graphics2D var0, int var1, int var2, int var3, int var4, int var5, Color var6, Color var7) {
      var0.setColor(var7);
      var0.fillRect(var1, var2, var3, var4);
      var0.setColor(var6);
      int var8 = Math.max(0, Math.min(var3, var3 * Math.max(0, Math.min(100, var5)) / 100));
      if (var8 > 1 && var4 > 1) {
         var0.fillRect(var1 + 1, var2 + 1, var8 - 1, var4 - 1);
      }

      var0.setColor(new Color(5275546));
      var0.drawRect(var1, var2, var3, var4);
   }

   private static void drawPetStateGauge(Graphics2D var0, VqsvUiLayout var1, int var2, boolean var3, int var4, Color var5, Color var6) {
      VqsvUiLayout.UiWidget var7 = var1.widget(var2);
      if (var7 == null) {
         drawPetStateGauge(var0, var1.x(var2, 73), var1.y(var2, 88), var1.w(var2, 26), 4, var4, var5, var6);
      } else {
         Color var8 = widgetTextColor(var7, var3, var5);
         Color var9 = widgetFillColor(var7, var3, var6);
         int var10 = var7.h > 0 ? var7.h : 4;
         drawPetStateGauge(var0, var7.x, var7.y, Math.max(1, var7.w), var10, var4, var8, var9);
      }
   }

   private static void drawPetQualityStars(Graphics2D var0, VqsvUiLayout var1, VqsvBattlePetStateView var2) {
      drawPetStateWidgetCell(var0, var1, 69, false);
      int var3 = Math.max(0, Math.min(5, var2.filledStars));
      int var4 = Math.max(0, Math.min(5, var2.visibleStars));

      for(int var5 = 0; var5 < var4; ++var5) {
         VqsvUiLayout.UiWidget var6 = var1.widget(70 + var5);
         if (var6 != null) {
            int var7 = var5 < var3 ? 14 : Math.max(0, var6.altId);
            if (var6.altMode == 3) {
               drawBattleUiStateTopLeft(var0, var7, var6.x, var6.y);
            } else {
               drawBattleUiCellTopLeft(var0, var7, var6.x, var6.y);
            }
         }
      }

   }

   private static void drawPetQualityStars(Graphics2D var0, int var1, int var2, VqsvBattlePetStateView var3) {
      int var4 = Math.max(0, Math.min(5, var3.filledStars));

      for(int var5 = 0; var5 < var4; ++var5) {
         drawPetStateStarGlyph(var0, var1 - 1 + var5 * 4, var2, true);
      }

   }

   private static void drawPetStateStarGlyph(Graphics2D var0, int var1, int var2, boolean var3) {
      var0.setColor(new Color(var3 ? 16763215 : 12043458));
      int[] var4 = new int[]{var1 + 3, var1 + 4, var1 + 6, var1 + 4, var1 + 3, var1 + 2, var1, var1 + 2};
      int[] var5 = new int[]{var2, var2 + 2, var2 + 2, var2 + 3, var2 + 5, var2 + 3, var2 + 2, var2 + 2};
      var0.fillPolygon(var4, var5, var4.length);
      var0.setColor(new Color(var3 ? 9069601 : 7900042));
      var0.drawPolygon(var4, var5, var4.length);
   }

   private static void drawPetStateText(Graphics2D var0, UiFont var1, VqsvUiLayout var2, int var3, String var4, Color var5) {
      drawPetStateText(var0, var1, var2, var3, var4, var5, 0);
   }

   private static void drawPetStateText(Graphics2D var0, UiFont var1, VqsvUiLayout var2, int var3, String var4, Color var5, int var6) {
      VqsvUiLayout.UiWidget var7 = var2.widget(var3);
      if (var7 != null) {
         drawSourceWidgetText(var0, var1, var4, var7.x, var7.y, Math.max(1, var7.w), sourceWidgetHeight(var7), widgetTextColor(var7, false, var5), var6, var7.b);
      }

   }

   private static void drawPetStateText(Graphics2D var0, UiFont var1, VqsvUiLayout var2, int var3, String var4, int var5, Color var6) {
      drawPetStateText(var0, var1, var2, var3, var4, var5, var6, 0);
   }

   private static void drawPetStateText(Graphics2D var0, UiFont var1, VqsvUiLayout var2, int var3, String var4, int var5, Color var6, int var7) {
      VqsvUiLayout.UiWidget var8 = var2.widget(var3);
      if (var8 != null) {
         drawSourceWidgetText(var0, var1, var4, var8.x, var8.y, Math.max(var8.w, var5), sourceWidgetHeight(var8), widgetTextColor(var8, false, var6), var7, var8.b);
      }

   }

   private static int petstateUiTick(VqsvGameRuntime.Scene var0) {
      return Math.max(0, var0.battleAnimationTick - var0.session.runtime.battleUiModeStartTick);
   }

   private static int sourceWidgetHeight(VqsvUiLayout.UiWidget var0) {
      return var0 != null && var0.h > 0 ? var0.h : 10;
   }

   private static void drawSourceWidgetText(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, int var6, Color var7, int var8, int var9) {
      String var10 = TextBox.decodeMojibake(var2);
      int var11 = var1.taggedWidth(var10);
      int var12;
      if (var11 > var5) {
         var12 = var3 - sourceHorizontalScrollOffset(var11, var5, var8);
      } else {
         switch (var9) {
            case 1:
            case 4:
            case 7:
               var12 = var3 + Math.max(0, (var5 - var11) / 2);
               break;
            case 2:
            case 5:
            case 8:
               var12 = var3 + Math.max(0, var5 - var11);
               break;
            case 3:
            case 6:
            default:
               var12 = var3;
         }
      }

      int var13;
      if (var9 != 3 && var9 != 4 && var9 != 5) {
         if (var9 != 6 && var9 != 7 && var9 != 8) {
            var13 = var4;
         } else {
            var13 = var4 + Math.max(0, var6 - 10);
         }
      } else {
         var13 = var4 + Math.max(0, (var6 - 10) / 2);
      }

      Shape var14 = var0.getClip();
      var0.clipRect(var3, var4, var5, Math.max(1, var6));
      var1.drawTaggedLine(var0, var10, var12, var13, TextBox.visibleLength(var10), var7.getRGB() & 16777215);
      var0.setClip(var14);
   }

   private static int sourceHorizontalScrollOffset(int var0, int var1, int var2) {
      int var3 = -var1 / 2;
      int var4 = Math.max(0, var2) + 1;

      for(int var5 = 0; var5 < var4; ++var5) {
         if (var0 > var3) {
            var3 += 2;
         } else {
            var3 = -var1;
         }
      }

      return var3;
   }

   private static Color widgetTextColor(VqsvUiLayout.UiWidget var0, boolean var1, Color var2) {
      return new Color(widgetColor(var0, var1, var2.getRGB() & 16777215, true));
   }

   private static Color widgetFillColor(VqsvUiLayout.UiWidget var0, boolean var1, Color var2) {
      return new Color(widgetColor(var0, var1, var2.getRGB() & 16777215, false));
   }

   private static int widgetColor(VqsvUiLayout.UiWidget var0, boolean var1, int var2, boolean var3) {
      if (var0 == null) {
         return var2;
      } else {
         int var4 = var1 ? (var3 ? var0.gColor : var0.eColor) : (var3 ? var0.lColor : var0.jColor);
         if (var4 >>> 24 == 0 && var4 < 0) {
            return var4 & 16777215;
         } else {
            return var4 != 0 && var4 != -16777216 && var4 != -1 ? var4 & 16777215 : var2;
         }
      }
   }

   private static void drawCenteredTinyText(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, Color var6) {
      String var7 = TextBox.decodeMojibake(var2);
      int var8 = Math.min(var5, var1.width(var7));
      int var9 = var3 + Math.max(0, (var5 - var8) / 2);
      drawTinyBattleText(var0, var1, var2, var9, var4, var5, var6);
   }

   private static void drawChoiceSkillOverlay(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2) {
      VqsvBattleSkillRenderState var3 = skillRenderState(var2);
      byte var4 = 8;
      byte var5 = 53;
      short var6 = 224;
      short var7 = 236;
      drawSourceUiFill(var0, var4 + 3, var5 + 3, var6, var7, 731192);
      drawSourceUiFill(var0, var4, var5, var6, var7, 7981013);
      drawSourceUiFill(var0, var4 + 2, var5 + 2, var6 - 4, 23, 1208209);
      drawSourceUiFill(var0, var4 + 2, var5 + 25, var6 - 4, 2, 16773482);
      drawSourceUiFill(var0, var4, var5 + var7 - 26, var6, 26, 2067892);
      drawSkillFrame(var0, var4, var5, var6, var7);
      drawTinyBattleText(var0, var1, "Kỹ năng", var4 + 9, var5 + 8, 70, Color.WHITE);
      drawTinyBattleText(var0, var1, "Số lần", 98, var5 + 8, 36, Color.WHITE);
      drawTinyBattleText(var0, var1, "Đòn đang chọn", 150, var5 + 8, 75, Color.WHITE);
      drawSourceUiFill(var0, 14, 88, 121, 115, 11067369);
      drawSkillFrame(var0, 14, 88, 121, 115);
      int var8 = Math.min(5, Math.max(1, var3.visibleRows));

      for(int var9 = 0; var9 < var8; ++var9) {
         int var10 = var3.scroll + var9;
         int var11 = 88 + var9 * 23;
         boolean var12 = var10 == var3.selectedIndex;
         if (var10 >= var3.names.length) {
            drawSkillEmptyRow(var0, var11);
         } else {
            drawSkillRow(var0, var1, var2, var3, var10, var11, var12);
         }
      }

      drawChoiceSkillScroll(var0, var3);
      boolean var13 = skillPpAvailable(var3.ppAt(var3.selectedIndex));
      drawSkillDetail(var0, var1, var2, var3);
      drawSkillDescription(var0, var1, var2, var3.description);
      drawSkillButton(var0, var1, 14, 264, 91, 22, "Sử dụng", true, var13);
      drawSkillButton(var0, var1, 135, 264, 91, 22, "Quay lại", false, true);
   }

   private static void drawChoiceSkillScroll(Graphics2D var0, VqsvBattleSkillRenderState var1) {
      int var2 = Math.max(1, var1.ids.length);
      byte var3 = 5;
      drawSourceUiFill(var0, 137, 90, 4, 111, 1801624);
      int var4 = UiScrollbarMath.thumbHeight(111, var2, var3, 8);
      int var5 = UiScrollbarMath.thumbY(90, 111, var4, var2, var3, var1.scroll);
      drawSourceUiFill(var0, 136, var5, 6, var4, 15990271);
      var0.setColor(new Color(1862801));
      var0.drawRect(136, var5, 5, var4 - 1);
   }

   private static void drawSkillRow(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2, VqsvBattleSkillRenderState var3, int var4, int var5, boolean var6) {
      byte var7 = 14;
      byte var8 = 121;
      byte var9 = 21;
      BattleSkillRow var10 = skillRow(var3, var4);
      boolean var11 = skillPpAvailable(var3.ppAt(var4));
      int var12 = var10 == null ? 2391970 : skillElementColor(var10.elementFamily);
      int var13 = var6 ? darkerColor(var12) : (var11 ? 13823733 : 9415348);
      int var14 = var6 ? 16773482 : 6729412;
      if (var6) {
         drawSourceUiFill(var0, var7 + 4, var5 + 4, var8 - 2, var9, 601920);
      }

      drawSourceUiFill(var0, var7 + 1, var5 + 1, var8 - 2, var9, var13);
      drawSourceUiFill(var0, var7 + 1, var5 + 1, 4, var9, var11 ? var12 : 8559266);
      if (var6) {
         drawSourceUiFill(var0, var7 + 5, var5 + 2, var8 - 7, 2, 16773482);
      }

      var0.setColor(new Color(var14));
      var0.drawRect(var7 + 1, var5 + 1, var8 - 3, var9 - 1);
      if (var10 != null && var10.elementFamily >= 0) {
         drawBattleUiCellTopLeft(var0, 94 + var10.elementFamily, var7 + 7, var5 + 4);
      }

      Color var15 = var11 ? (var6 ? new Color(16777215) : SOURCE_UI_TEXT) : new Color(6518397);
      Color var16 = var11 ? (var6 ? new Color(16773482) : new Color(1800349)) : new Color(6518397);
      drawTinyBattleText(var0, var1, var3.nameAt(var4), var7 + 23, var5 + 4, 60, var15);
      drawTinyBattleText(var0, var1, var3.ppAt(var4), var7 + var8 - 35, var5 + 4, 32, var16);
      drawSkillMiniBar(var0, var7 + var8 - 36, var5 + 16, 31, 3, skillPpCurrent(var3.ppAt(var4)), skillPpMax(var3.ppAt(var4)), var11 ? var12 : 8559266);
   }

   private static void drawSkillEmptyRow(Graphics2D var0, int var1) {
      byte var2 = 14;
      byte var3 = 121;
      drawSourceUiFill(var0, var2 + 1, var1 + 1, var3 - 2, 21, 14020084);
      var0.setColor(new Color(10406357));
      var0.drawRect(var2 + 1, var1 + 1, var3 - 3, 20);
   }

   private static void drawSkillDetail(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2, VqsvBattleSkillRenderState var3) {
      short var4 = 146;
      byte var5 = 88;
      byte var6 = 80;
      byte var7 = 115;
      drawSourceUiFill(var0, var4 + 2, var5 + 2, var6, var7, 3500923);
      drawSourceUiFill(var0, var4, var5, var6, var7, 12905202);
      drawSkillFrame(var0, var4, var5, var6, var7);
      BattleSkillRow var8 = skillRow(var3, var3.selectedIndex);
      if (var8 == null) {
         drawTinyBattleText(var0, var1, "...", var4 + 34, var5 + 48, 20, SOURCE_UI_TEXT);
      } else {
         int var9 = skillElementColor(var8.elementFamily);
         drawSourceUiFill(var0, var4 + 2, var5 + 2, var6 - 4, 20, darkerColor(var9));
         drawSourceUiFill(var0, var4 + 2, var5 + 21, var6 - 4, 2, 16773482);
         drawTinyBattleText(var0, var1, var3.nameAt(var3.selectedIndex), var4 + 5, var5 + 6, var6 - 10, Color.WHITE);
         if (var8.elementFamily >= 0) {
            drawBattleUiCellTopLeft(var0, 94 + var8.elementFamily, var4 + 6, var5 + 28);
         }

         drawSourceUiFill(var0, var4 + 22, var5 + 27, var6 - 28, 14, 14218747);
         var0.setColor(new Color(var9));
         var0.drawRect(var4 + 22, var5 + 27, var6 - 29, 13);
         drawTinyBattleText(var0, var1, skillElementName(var8.elementFamily), var4 + 26, var5 + 29, 45, SOURCE_UI_TEXT);
         drawSkillMetaLine(var0, var1, "Độ mạnh", skillPowerLabel(var8), var4 + 5, var5 + 45, var6 - 10);
         drawSkillMetaLine(var0, var1, "PP", var3.ppAt(var3.selectedIndex), var4 + 5, var5 + 58, var6 - 10);
         drawSkillMeter(var0, var4 + 42, var5 + 69, var6 - 52, 4, skillPpCurrent(var3.ppAt(var3.selectedIndex)), skillPpMax(var3.ppAt(var3.selectedIndex)), var9);
         if (!skillPpAvailable(var3.ppAt(var3.selectedIndex))) {
            drawTinyBattleText(var0, var1, "Hết PP", var4 + 45, var5 + 60, var6 - 48, new Color(11876665));
         }

         drawSkillMetaLine(var0, var1, "Bậc", String.valueOf(Math.max(1, var8.learnTier)), var4 + 5, var5 + 76, var6 - 10);
         drawSkillMetaLine(var0, var1, "Đích", skillTargetLabel(var8.targetSide), var4 + 5, var5 + 89, var6 - 10);
         drawSkillMetaLine(var0, var1, "TL", skillChanceLabel(var8), var4 + 5, var5 + 102, var6 - 10);
      }
   }

   private static void drawSkillDescription(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2, String var3) {
      byte var4 = 14;
      short var5 = 208;
      short var6 = 212;
      byte var7 = 50;
      drawSourceUiFill(var0, var4 + 2, var5 + 2, var6, var7, 466733);
      drawSourceUiFill(var0, var4, var5, var6, var7, 1523542);
      drawSourceUiFill(var0, var4 + 2, var5 + 2, 40, 12, 16773482);
      drawSkillFrame(var0, var4, var5, var6, var7);
      drawTinyBattleText(var0, var1, "Mô tả", var4 + 7, var5 + 3, 30, SOURCE_UI_TEXT);
      List var8 = wrapPetStateText(var1, TextBox.decodeMojibake(var3 == null ? "" : var3), var6 - 10);
      byte var9 = 10;
      int var10 = Math.max(1, (var7 - 19) / var9);
      byte var11 = 0;
      Shape var12 = var0.getClip();
      var0.clipRect(var4 + 5, var5 + 16, var6 - 10, var7 - 19);

      for(int var13 = 0; var13 < var10 && var11 + var13 < var8.size(); ++var13) {
         drawTinyBattleText(var0, var1, (String)var8.get(var11 + var13), var4 + 5, var5 + 16 + var13 * var9, var6 - 10, Color.WHITE);
      }

      var0.setClip(var12);
   }

   private static void drawSkillMetaLine(Graphics2D var0, UiFont var1, String var2, String var3, int var4, int var5, int var6) {
      drawSourceUiFill(var0, var4, var5, var6, 13, 12049638);
      var0.setColor(new Color(6133156));
      var0.drawRect(var4, var5, var6 - 1, 12);
      drawTinyBattleText(var0, var1, var2, var4 + 3, var5 + 2, 38, SOURCE_UI_TEXT);
      drawTinyBattleText(var0, var1, var3, var4 + 41, var5 + 2, var6 - 44, new Color(941952));
   }

   private static void drawSkillButton(Graphics2D var0, UiFont var1, int var2, int var3, int var4, int var5, String var6, boolean var7, boolean var8) {
      drawSourceUiFill(var0, var2 + 2, var3 + 2, var4, var5, 735818);
      drawSourceUiFill(var0, var2, var3, var4, var5, var8 ? (var7 ? 1277345 : 12049638) : 7901851);
      drawSourceUiFill(var0, var2 + 2, var3 + 2, var4 - 4, 3, var8 ? (var7 ? 6276831 : 14939898) : 10993092);
      drawSourceUiFill(var0, var2 + 2, var3 + var5 - 5, var4 - 4, 3, var8 ? (var7 ? 876414 : 7450301) : 6322562);
      var0.setColor(new Color(var8 ? (var7 ? 16773482 : 3432558) : 5269618));
      var0.drawRect(var2, var3, var4 - 1, var5 - 1);
      var0.drawRect(var2 + 1, var3 + 1, var4 - 3, var5 - 3);
      Color var9 = var8 ? (var7 ? Color.WHITE : SOURCE_UI_TEXT) : new Color(14214632);
      drawCenteredTinyText(var0, var1, var6, var2 + 3, var3 + 6, var4 - 6, var9);
   }

   private static void drawSkillFrame(Graphics2D var0, int var1, int var2, int var3, int var4) {
      var0.setColor(new Color(15267071));
      var0.drawRect(var1, var2, var3 - 1, var4 - 1);
      var0.setColor(new Color(3432558));
      var0.drawRect(var1 + 1, var2 + 1, var3 - 3, var4 - 3);
   }

   private static void drawSkillMiniBar(Graphics2D var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      drawSourceUiFill(var0, var1, var2, var3, var4, 7311257);
      int var8 = var6 <= 0 ? 0 : Math.max(0, Math.min(var3, var5 * var3 / var6));
      if (var8 > 0) {
         drawSourceUiFill(var0, var1, var2, var8, var4, var7);
      }

   }

   private static void drawSkillMeter(Graphics2D var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      drawSourceUiFill(var0, var1, var2, var3, var4, 12047580);
      int var8 = var6 <= 0 ? 0 : Math.max(0, Math.min(var3, var5 * var3 / var6));
      if (var8 > 0) {
         drawSourceUiFill(var0, var1, var2, var8, var4, var7);
      }

      var0.setColor(new Color(6133156));
      var0.drawRect(var1, var2, var3 - 1, var4 - 1);
   }

   private static BattleSkillRow skillRow(VqsvBattleSkillRenderState var0, int var1) {
      return var1 >= 0 && var1 < var0.ids.length ? VqsvBattleTables.instance().skill(var0.ids[var1]) : null;
   }

   private static boolean skillPpAvailable(String var0) {
      if (var0 == null) {
         return true;
      } else {
         int var1 = var0.indexOf(47);
         if (var1 <= 0) {
            return true;
         } else {
            try {
               return Integer.parseInt(var0.substring(0, var1).trim()) > 0;
            } catch (NumberFormatException var3) {
               return true;
            }
         }
      }
   }

   private static int skillPpCurrent(String var0) {
      int var1 = var0 == null ? -1 : var0.indexOf(47);
      if (var1 <= 0) {
         return 0;
      } else {
         try {
            return Math.max(0, Integer.parseInt(var0.substring(0, var1).trim()));
         } catch (NumberFormatException var3) {
            return 0;
         }
      }
   }

   private static int skillPpMax(String var0) {
      int var1 = var0 == null ? -1 : var0.indexOf(47);
      if (var1 >= 0 && var1 + 1 < var0.length()) {
         try {
            return Math.max(0, Integer.parseInt(var0.substring(var1 + 1).trim()));
         } catch (NumberFormatException var3) {
            return 0;
         }
      } else {
         return 0;
      }
   }

   private static String skillPowerLabel(BattleSkillRow var0) {
      return var0.powerPercent > 0 ? String.valueOf(var0.powerPercent) : "--";
   }

   private static String skillChanceLabel(BattleSkillRow var0) {
      int var1 = Math.max(0, var0.chanceOrParam);
      if (var1 <= 0) {
         return "Không";
      } else {
         return var1 >= 100 ? "100%" : var1 + "%";
      }
   }

   private static String skillTargetLabel(int var0) {
      return var0 == 1 ? "Bên ta" : "Đối thủ";
   }

   private static String skillElementName(int var0) {
      switch (var0) {
         case 0 -> {
            return "Hỏa";
         }
         case 1 -> {
            return "Mộc";
         }
         case 2 -> {
            return "Thổ";
         }
         case 3 -> {
            return "Thủy";
         }
         case 4 -> {
            return "Điện";
         }
         case 5 -> {
            return "Quỷ";
         }
         case 6 -> {
            return "Phong";
         }
         default -> {
            return "Không";
         }
      }
   }

   private static int skillElementColor(int var0) {
      switch (var0) {
         case 0 -> {
            return 14048321;
         }
         case 1 -> {
            return 4954969;
         }
         case 2 -> {
            return 11700805;
         }
         case 3 -> {
            return 4231114;
         }
         case 4 -> {
            return 13676601;
         }
         case 5 -> {
            return 9133494;
         }
         case 6 -> {
            return 5220751;
         }
         default -> {
            return 2391970;
         }
      }
   }

   private static int darkerColor(int var0) {
      int var1 = Math.max(0, (var0 >>> 16 & 255) * 65 / 100);
      int var2 = Math.max(0, (var0 >>> 8 & 255) * 65 / 100);
      int var3 = Math.max(0, (var0 & 255) * 65 / 100);
      return var1 << 16 | var2 << 8 | var3;
   }

   private static int lighterColor(int var0) {
      int var1 = Math.min(255, (var0 >>> 16 & 255) + 95);
      int var2 = Math.min(255, (var0 >>> 8 & 255) + 95);
      int var3 = Math.min(255, (var0 & 255) + 95);
      return var1 << 16 | var2 << 8 | var3;
   }

   private static void drawTargetCursor(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2) {
      VqsvBattleTargetRenderState var3 = targetRenderState(var2);
      int var4 = var3.selectedSlot();
      int var5 = battleRenderState(var2).simultaneous2v2() && var4 >= 0 ? formationActorX(var2, var4) - 10 : (var3.playerSide ? 55 : 171);
      int var6 = battleRenderState(var2).simultaneous2v2() && var4 >= 0 ? formationActorY(var2, var4) - 38 : (var3.playerSide ? 130 : 60);
      drawBattleUiCellTopLeft(var0, 31, var5, var6);
      String var7 = var3.selectedName();
      if (!var7.isEmpty()) {
         drawBattlePanel(var0, 72, 268, 96, 18, true);
         drawTinyBattleText(var0, var1, var7, 78, 273, 84, new Color(16773482));
      }

   }

   private static void drawP7Damage(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2) {
      int var3 = battleRenderState(var2).doubleBattlePresentation.damageReceiverSlot;
      int var4 = p7ActorX(var2, var3, var2.battleP7DamagePlayerSide);
      int var5 = p7ActorY(var2, var3, var2.battleP7DamagePlayerSide);
      short[] var6 = VqsvBattleAnimationTables.instance().bloodRow(0);
      int var7 = bloodFrame(var2.battleP7Ticks, var6);
      int var8 = bloodValue(var6, var7, 0);
      int var9 = bloodValue(var6, var7, 1);
      int var10 = var2.battleP7DamagePlayerSide ? var4 + var8 + 30 : var4 - var8 - 30;
      int var11 = var5 + var9 - 30;
      if (!var2.battleP7DamageText.isEmpty() && var2.battleP7Ticks < frameCount(var6)) {
         Color var12 = var2.battleP7DamageCritical ? new Color(16735547) : new Color(16773482);
         drawOutlinedTinyBattleText(var0, var1, var2.battleP7DamageText, var10 - 14, var11, 44, var12, new Color(4130567));
      }

      short[] var18 = VqsvBattleAnimationTables.instance().bloodRow(1);
      String var13 = !var2.battleP7MissText.isEmpty() ? var2.battleP7MissText : var2.battleP7DebuffText;
      if (!var13.isEmpty() && var2.battleP7Ticks < frameCount(var18)) {
         int var14 = bloodFrame(var2.battleP7Ticks, var18);
         int var15 = bloodValue(var18, var14, 1);
         int var16 = var2.battleP7DamagePlayerSide ? var4 - 10 : var4 + 10;
         int var17 = var5 + var15 - 30;
         drawOutlinedTinyBattleText(var0, var1, var13, var16 - 22, var17, 62, new Color(16777215), new Color(1324106));
      }

   }

   private static int bloodFrame(int var0, short[] var1) {
      int var2 = var1 == null ? 0 : var1.length / 2;
      return var2 <= 0 ? 0 : Math.max(0, Math.min(var2 - 1, var0));
   }

   private static int frameCount(short[] var0) {
      return var0 == null ? 0 : var0.length / 2;
   }

   private static int bloodValue(short[] var0, int var1, int var2) {
      int var3 = var1 * 2 + var2;
      return var0 != null && var3 >= 0 && var3 < var0.length ? var0[var3] : 0;
   }

   private static void drawP7PostEffect(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2) {
      int var3 = battleRenderState(var2).doubleBattlePresentation.postEffectSlot;
      int var4 = p7ActorX(var2, var3, var2.battleP7PostEffectPlayerSide);
      int var5 = p7ActorY(var2, var3, var2.battleP7PostEffectPlayerSide);
      drawOutlinedTinyBattleText(var0, var1, var2.battleP7PostEffectText, var4 - 20, var5 - 42, 64, new Color(16777215), new Color(1324106));
   }

   private static void drawP7SpecialEffect(Graphics2D var0, VqsvGameRuntime.Scene var1) {
      if (var1.battleP7SpecialVisible && isSupportedP7SpecialType(var1.battleP7SpecialType)) {
         int var2 = battleRenderState(var1).doubleBattlePresentation.effectSlot;
         int var3 = p7VisualId(var1, var2, var1.battleP7SpecialOnPlayerSide ? var1.battlePlayerVisualId : var1.battleEnemyVisualId);
         if (var3 >= 0) {
            if (var1.battleP7SpecialType == 7) {
               drawP7SpecialType7(var0, var1, var3);
            } else if (var1.battleP7SpecialType == 1) {
               drawP7SpecialType1(var0, var1, var3);
            } else if (var1.battleP7SpecialType == 12) {
               drawP7SpecialType12(var0, var1, var3);
            } else {
               BufferedImage var4 = new BufferedImage(240, 320, 2);
               Graphics2D var5 = var4.createGraphics();
               drawP7SpecialBaseSpriteAtSource(var5, var1, var3);
               var5.dispose();
               if (var1.battleP7SpecialType == 9) {
                  var0.drawImage(var4, 0, 0, (ImageObserver)null);
                  if (var1.battleP7Ticks / Math.max(1, var1.battleP7SpecialInterval) % 2 == 0) {
                     BufferedImage var6 = copyImage(var4);
                     applyAhType9Transform(var6, var1.battleP7SpecialAlpha, var1.battleP7SpecialRed, var1.battleP7SpecialGreen, var1.battleP7SpecialBlue);
                     var0.drawImage(var6, 0, 0, (ImageObserver)null);
                  }
               } else if (var1.battleP7SpecialType == 8) {
                  drawP7SpecialType8(var0, var4, var1);
               }

            }
         }
      }
   }

   private static void drawP7SpecialBaseSpriteAtSource(Graphics2D var0, VqsvGameRuntime.Scene var1, int var2) {
      boolean var3 = var1.battleP7SpecialOnPlayerSide;
      int var4 = battleRenderState(var1).doubleBattlePresentation.effectSlot;
      int var5 = var3 ? var1.battleP7BaseStatePlayerSide : var1.battleP7BaseStateEnemySide;
      int var6 = var3 ? var1.battleP7BaseCursorPlayerSide : var1.battleP7BaseCursorEnemySide;
      if (var6 < 0) {
         var6 = idleCursor(var2, var5, var1.battleAnimationTick);
      }

      drawBattleSpriteAtSource(var0, var2, p7ActorX(var1, var4, var3), p7ActorY(var1, var4, var3), battlePetOrientation(var2, var3), var5, var6);
   }

   private static boolean isSupportedP7SpecialType(int var0) {
      return var0 == 1 || var0 == 7 || var0 == 8 || var0 == 9 || var0 == 12;
   }

   private static void drawP7SpecialType7(Graphics2D var0, VqsvGameRuntime.Scene var1, int var2) {
      short[] var3 = var1.battleP7SpecialRow;
      if (var3.length >= 8) {
         int var4 = var1.battleP7SpecialOnPlayerSide ? var1.battleP7BaseStatePlayerSide : var1.battleP7BaseStateEnemySide;
         int var5 = var1.battleP7SpecialOnPlayerSide ? var1.battleP7BaseCursorPlayerSide : var1.battleP7BaseCursorEnemySide;
         if (var5 < 0) {
            var5 = idleCursor(var2, var4, var1.battleAnimationTick);
         }

         int var6 = currentCellId(var2, var4, var5);
         if (var6 >= 0) {
            SpriteAnimator var7 = SpriteAnimator.load(var2);
            int[] var8 = var7.cellBounds(var6);
            if (var8 != null && var8[2] > 0 && var8[3] > 0) {
               BufferedImage var9 = renderSpriteCellImage(var2, var6, battlePetOrientation(var2, var1.battleP7SpecialOnPlayerSide));
               if (var9 != null) {
                  int var10 = battleRenderState(var1).doubleBattlePresentation.effectSlot;
                  int var11 = p7ActorX(var1, var10, var1.battleP7SpecialOnPlayerSide);
                  int var12 = p7ActorY(var1, var10, var1.battleP7SpecialOnPlayerSide);
                  int var13 = var11 + var8[0];
                  int var14 = var12 + var8[1];
                  int var15 = Math.max(1, var3[3]);
                  boolean var16 = Math.max(0, var1.battleP7Ticks) / var15 % 2 == 0;
                  if (!var16) {
                     var0.drawImage(var9, var13, var14, (ImageObserver)null);
                  } else {
                     short var17 = var3.length > 4 ? var3[4] : 1;
                     int var18 = Math.max(1, var3.length > 5 ? var3[5] : 1);
                     short var19 = var3.length > 6 ? var3[6] : var17;
                     int var20 = Math.max(1, var3.length > 7 ? var3[7] : var18);
                     int var21 = Math.max(1, var8[2] * var17 / var18);
                     int var22 = Math.max(1, var8[3] * var19 / var20);
                     int var23 = (var8[2] - var21) / 2;
                     int var24 = var8[3] - var22;
                     Object var25 = var0.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
                     var0.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                     var0.drawImage(var9, var13 + var23, var14 + var24, var21, var22, (ImageObserver)null);
                     if (var25 == null) {
                        var0.getRenderingHints().remove(RenderingHints.KEY_INTERPOLATION);
                     } else {
                        var0.setRenderingHint(RenderingHints.KEY_INTERPOLATION, var25);
                     }

                  }
               }
            }
         }
      }
   }

   private static void drawP7SpecialType8(Graphics2D var0, BufferedImage var1, VqsvGameRuntime.Scene var2) {
      short[] var3 = var2.battleP7SpecialRow;
      if (var3.length < 9) {
         var0.drawImage(var1, 0, 0, (ImageObserver)null);
      } else {
         int var4 = Math.max(1, (var3.length - 6) / 3);
         int var5 = Math.max(var4, var3[2]);
         int var6 = Math.max(1, var5 / var4);
         int var7 = Math.max(0, Math.min(var4 - 1, Math.max(0, var2.battleP7Ticks) / var6));
         int var8 = 6 + var7 * 3;
         int var9 = 10;
         short var10 = 0;
         short var11 = 0;
         if (var8 + 2 < var3.length) {
            var9 = Math.max(1, var3[var8]);
            var10 = var3[var8 + 1];
            var11 = var3[var8 + 2];
         }

         BufferedImage var12 = brightenCopy(var1, 50);
         int var13 = Math.max(1, var12.getWidth() * var9 / 10);
         int var14 = Math.max(1, var12.getHeight() * var9 / 10);
         int var15 = var10 + (var12.getWidth() - var13) / 2;
         int var16 = var11 + (var12.getHeight() - var14) / 2;
         Object var17 = var0.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
         var0.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
         var0.drawImage(var12, var15, var16, var13, var14, (ImageObserver)null);
         if (var17 == null) {
            var0.getRenderingHints().remove(RenderingHints.KEY_INTERPOLATION);
         } else {
            var0.setRenderingHint(RenderingHints.KEY_INTERPOLATION, var17);
         }

      }
   }

   private static BufferedImage brightenCopy(BufferedImage var0, int var1) {
      BufferedImage var2 = new BufferedImage(var0.getWidth(), var0.getHeight(), 2);

      for(int var3 = 0; var3 < var0.getHeight(); ++var3) {
         for(int var4 = 0; var4 < var0.getWidth(); ++var4) {
            int var5 = var0.getRGB(var4, var3);
            int var6 = var5 >>> 24;
            if (var6 != 0) {
               int var7 = Math.min(255, (var5 >> 16 & 255) + var1);
               int var8 = Math.min(255, (var5 >> 8 & 255) + var1);
               int var9 = Math.min(255, (var5 & 255) + var1);
               var2.setRGB(var4, var3, var6 << 24 | var7 << 16 | var8 << 8 | var9);
            }
         }
      }

      return var2;
   }

   private static BufferedImage copyImage(BufferedImage var0) {
      BufferedImage var1 = new BufferedImage(var0.getWidth(), var0.getHeight(), 2);
      Graphics2D var2 = var1.createGraphics();
      var2.drawImage(var0, 0, 0, (ImageObserver)null);
      var2.dispose();
      return var1;
   }

   private static void drawP7SpecialType1(Graphics2D var0, VqsvGameRuntime.Scene var1, int var2) {
      P7SpecialCell var3 = p7SpecialCell(var1, var2);
      if (var3 != null) {
         BufferedImage var4 = copyImage(var3.image);
         applyAhType1Texture(var4, var1);
         var0.drawImage(var4, var3.x, var3.y, (ImageObserver)null);
      }
   }

   private static void drawP7SpecialType12(Graphics2D var0, VqsvGameRuntime.Scene var1, int var2) {
      short[] var3 = var1.battleP7SpecialRow;
      if (var3.length >= 10) {
         P7SpecialCell var4 = p7SpecialCell(var1, var2);
         if (var4 != null) {
            int var5 = Math.max(1, var3[5]);
            int var6 = Math.max(1, var3[7] + 1);
            int var7 = Math.max(0, Math.min(var5 - 1, Math.max(0, var1.battleP7Ticks) / var6));
            byte var8 = 8;
            int var9 = var8 + var7 * 2;
            int var10 = var8 + var5 * 2 + var7 * 2;
            if (var10 + 1 >= var3.length) {
               var0.drawImage(var4.image, var4.x, var4.y, (ImageObserver)null);
            } else {
               int var11 = var3[var9];
               short var12 = var3[var9 + 1];
               int var13 = var3[var10];
               short var14 = var3[var10 + 1];
               if (var1.battleP7SpecialOnPlayerSide) {
                  var11 = -var11;
                  var13 = -var13;
               }

               BufferedImage var15 = alphaCopy(var4.image, Math.max(0, Math.min(255, var3[2])));
               BufferedImage var16 = alphaCopy(var4.image, Math.max(0, Math.min(255, var3[3])));
               var0.drawImage(var16, var4.x + var11 + var13, var4.y + var12 - var14, (ImageObserver)null);
               var0.drawImage(var15, var4.x + var11, var4.y + var12, (ImageObserver)null);
            }
         }
      }
   }

   private static P7SpecialCell p7SpecialCell(VqsvGameRuntime.Scene var0, int var1) {
      boolean var2 = var0.battleP7SpecialOnPlayerSide;
      int var3 = battleRenderState(var0).doubleBattlePresentation.effectSlot;
      int var4 = var2 ? var0.battleP7BaseStatePlayerSide : var0.battleP7BaseStateEnemySide;
      int var5 = var2 ? var0.battleP7BaseCursorPlayerSide : var0.battleP7BaseCursorEnemySide;
      if (var5 < 0) {
         var5 = idleCursor(var1, var4, var0.battleAnimationTick);
      }

      int var6 = currentCellId(var1, var4, var5);
      if (var6 < 0) {
         return null;
      } else {
         SpriteAnimator var7 = SpriteAnimator.load(var1);
         int[] var8 = var7.cellBounds(var6);
         if (var8 != null && var8[2] > 0 && var8[3] > 0) {
            BufferedImage var9 = renderSpriteCellImage(var1, var6, battlePetOrientation(var1, var2));
            if (var9 == null) {
               return null;
            } else {
               int var10 = p7ActorX(var0, var3, var2);
               int var11 = p7ActorY(var0, var3, var2);
               return new P7SpecialCell(var9, var10 + var8[0], var11 + var8[1]);
            }
         } else {
            return null;
         }
      }
   }

   private static int currentCellId(int var0, int var1, int var2) {
      SpriteAnimator var3 = SpriteAnimator.load(var0);
      var3.setState(Math.max(0, var1));
      return var3.cellIdAtFrame(var2);
   }

   private static void drawBattleActorWithAttachedEffects(Graphics2D var0, VqsvGameRuntime.Scene var1, boolean var2) {
      if (var1.battleLVisible && var1.battleLPlayerSide == var2 && !var1.battleLDrawAfter) {
         drawState1LEffect(var0, var1);
      }

      if (var2) {
         if (!var1.battleP7BaseHiddenPlayerSide) {
            drawBattleSpriteAtSource(var0, var1.battlePlayerVisualId, sourceBattleActorX(var1, true) + playerOffsetX(var1), sourceBattleActorY(var1, true) + playerOffsetY(var1), battlePetOrientation(var1.battlePlayerVisualId, true), var1.battleP7BaseStatePlayerSide, baseCursor(var1.battlePlayerVisualId, var1.battleP7BaseStatePlayerSide, var1.battleP7BaseCursorPlayerSide, var1.battleAnimationTick));
         }
      } else if (!catchRenderState(var1).enemyHidden && !var1.battleP7BaseHiddenEnemySide) {
         drawBattleSpriteAtSource(var0, var1.battleEnemyVisualId, sourceBattleActorX(var1, false) + enemyOffsetX(var1), sourceBattleActorY(var1, false) + enemyOffsetY(var1), battlePetOrientation(var1.battleEnemyVisualId, false), var1.battleP7BaseStateEnemySide, baseCursor(var1.battleEnemyVisualId, var1.battleP7BaseStateEnemySide, var1.battleP7BaseCursorEnemySide, var1.battleAnimationTick));
      }

      if (var1.battleLVisible && var1.battleLPlayerSide == var2 && var1.battleLDrawAfter) {
         drawState1LEffect(var0, var1);
      }

      drawP7ActorEffectForSide(var0, var1, var2);
   }

   private static void drawSimultaneousFormationActors(Graphics2D var0, VqsvGameRuntime.Scene var1) {
      VqsvBattleRenderState var2 = battleRenderState(var1);
      VqsvDoubleBattlePresentationState var3 = var2.doubleBattlePresentation;

      for(int var4 = 0; var4 < var2.formationSlots.length; ++var4) {
         VqsvBattleRenderState.UnitState var5 = var2.formationSlots[var4];
         VqsvDoubleBattlePresentationState.SlotState var6 = var3.hasSlot(var4) ? var3.slotAt(var4) : null;
         boolean var7 = var6 == null ? var5 != null && var5.hp > 0 : var6.visible;
         if (var5 != null && var5.visualId >= 0 && var7) {
            boolean var8 = var4 >= 2;
            int var9 = var6 == null ? 0 : var6.spriteState;
            int var10 = var6 == null ? -1 : var6.spriteCursor;
            if (var10 < 0) {
               var10 = idleCursor(var5.visualId, var9, var1.battleAnimationTick);
            }

            if (var1.battleLVisible && !var1.battleLDrawAfter && var3.lEffectSlot == var4) {
               drawState1LEffect(var0, var1);
            }

            drawBattleSpriteAtSource(var0, var5.visualId, formationActorX(var1, var4), formationActorY(var1, var4), battlePetOrientation(var5.visualId, var8), var9, var10);
            if (var1.battleLVisible && var1.battleLDrawAfter && var3.lEffectSlot == var4) {
               drawState1LEffect(var0, var1);
            }

            drawP7ActorEffectForSlot(var0, var1, var4);
         }
      }

   }

   private static void drawP7ActorEffectForSlot(Graphics2D var0, VqsvGameRuntime.Scene var1, int var2) {
      if (var1.battleP7ActorEffectVisible && var1.battleP7ActorEffectSpriteId >= 0 && battleRenderState(var1).doubleBattlePresentation.effectSlot == var2) {
         boolean var3 = var2 >= 2;
         int var4 = formationActorX(var1, var2);
         int var5 = formationActorY(var1, var2);
         if (var1.battleP7ActorEffectSourceId == 20 && var1.battleP7ActorEffectState == 3 || var1.battleP7ActorEffectSourceId == 22 && var1.battleP7ActorEffectState == 4) {
            int var6 = battleRenderState(var1).formationSlots[var2].visualId;
            var5 -= battleSpriteFrameHeight(var6, 0);
         }

         drawBattleSpriteAtSource(var0, var1.battleP7ActorEffectSpriteId, var4, var5, sourceBattleOrientation(var3), var1.battleP7ActorEffectState, var1.battleP7ActorEffectCursor);
      }
   }

   private static void drawP7ActorEffectForSide(Graphics2D var0, VqsvGameRuntime.Scene var1, boolean var2) {
      if (var1.battleP7ActorEffectVisible && var1.battleP7ActorEffectSpriteId >= 0) {
         if (var1.battleP7ActorEffectOnPlayerSide == var2) {
            int var3 = sourceBattleActorX(var1, var2) + sideOffsetX(var1, var2);
            int var4 = sourceBattleActorY(var1, var2) + sideOffsetY(var1, var2);
            if (var1.battleP7ActorEffectSourceId == 20 && var1.battleP7ActorEffectState == 3 || var1.battleP7ActorEffectSourceId == 22 && var1.battleP7ActorEffectState == 4) {
               int var5 = var2 ? var1.battlePlayerVisualId : var1.battleEnemyVisualId;
               int var6 = battleSpriteFrameHeight(var5, 0);
               var4 -= var6;
            }

            drawBattleSpriteAtSource(var0, var1.battleP7ActorEffectSpriteId, var3, var4, sourceBattleOrientation(var2), var1.battleP7ActorEffectState, var1.battleP7ActorEffectCursor);
         }
      }
   }

   private static int battleSpriteFrameHeight(int var0, int var1) {
      if (var0 < 0) {
         return 0;
      } else {
         SpriteAnimator var2 = SpriteAnimator.load(var0);
         int[] var3 = var2.animationBounds(Math.max(0, var1));
         return var3 == null ? 0 : Math.max(0, var3[3]);
      }
   }

   private static void drawP7DeathEffect(Graphics2D var0, VqsvGameRuntime.Scene var1) {
      if (var1.battleP7DeathEffectVisible && var1.battleP7DeathEffectSpriteId >= 0) {
         SpriteAnimator var2 = SpriteAnimator.load(var1.battleP7DeathEffectSpriteId);
         if (UnifiedPetCatalog.instance().isRainbowVisual(var1.battleP7DeathEffectSpriteId)) {
            byte var19 = 2;
            int var20 = var2.cursorAtElapsedTickHoldLast(var19, var1.battleP7DeathEffectTick);
            int var21 = battleRenderState(var1).doubleBattlePresentation.deathSlot;
            drawBattleSpriteAtSource(var0, var1.battleP7DeathEffectSpriteId, p7ActorX(var1, var21, var1.battleP7DeathEffectPlayerSide), p7ActorY(var1, var21, var1.battleP7DeathEffectPlayerSide), sourceBattleOrientation(var1.battleP7DeathEffectPlayerSide), var19, var20);
         } else {
            int var3 = var2.cellIdAtFrame(0, 0);
            int[] var4 = var2.cellBounds(var3);
            if (var4 != null && var4[2] > 0 && var4[3] > 0) {
               BufferedImage var5 = renderSpriteCellImage(var1.battleP7DeathEffectSpriteId, var3, var1.battleP7DeathEffectPlayerSide ? 0 : 1);
               if (var5 != null) {
                  int var6 = Math.max(1, var1.battleP7DeathEffectDuration);
                  int var7 = Math.max(0, Math.min(var6, var1.battleP7DeathEffectTick));
                  byte var8 = 4;
                  int var9 = Math.max(1, var5.getHeight() / var8);
                  BufferedImage var10 = new BufferedImage(var5.getWidth(), var5.getHeight(), 2);

                  for(int var11 = 0; var11 < var5.getHeight(); ++var11) {
                     int var12 = Math.min(var8 - 1, var11 / var9);
                     int var13 = var12 * var6 / var8;
                     boolean var14 = var7 >= var13;
                     int var15 = var14 ? Math.max(0, 255 - (var7 - var13) * 255 / var6) : 255;

                     for(int var16 = 0; var16 < var5.getWidth(); ++var16) {
                        int var17 = var5.getRGB(var16, var11);
                        int var18 = var17 >>> 24;
                        if (var18 != 0) {
                           var10.setRGB(var16, var11, Math.min(var18, var15) << 24 | var17 & 16777215);
                        }
                     }
                  }

                  int var22 = battleRenderState(var1).doubleBattlePresentation.deathSlot;
                  int var23 = p7ActorX(var1, var22, var1.battleP7DeathEffectPlayerSide) + var4[0];
                  int var24 = p7ActorY(var1, var22, var1.battleP7DeathEffectPlayerSide) + var4[1];
                  var0.drawImage(var10, var23, var24, (ImageObserver)null);
               }
            }
         }
      }
   }

   private static int p7ActorX(VqsvGameRuntime.Scene var0, int var1, boolean var2) {
      return battleRenderState(var0).simultaneous2v2() && var1 >= 0 ? formationActorX(var0, var1) : sourceBattleActorX(var0, var2) + sideOffsetX(var0, var2);
   }

   private static int p7ActorY(VqsvGameRuntime.Scene var0, int var1, boolean var2) {
      return battleRenderState(var0).simultaneous2v2() && var1 >= 0 ? formationActorY(var0, var1) : sourceBattleActorY(var0, var2) + sideOffsetY(var0, var2);
   }

   private static int p7VisualId(VqsvGameRuntime.Scene var0, int var1, int var2) {
      VqsvBattleRenderState var3 = battleRenderState(var0);
      return var3.simultaneous2v2() && var1 >= 0 && var1 < var3.formationSlots.length ? var3.formationSlots[var1].visualId : var2;
   }

   private static void drawState1LEffect(Graphics2D var0, VqsvGameRuntime.Scene var1) {
      if (var1.battleLVisible && var1.battleLRow.length != 0 && var1.battleLSpriteId >= 0) {
         if (var1.battleLType == 12) {
            drawState1LEffectType12(var0, var1);
         } else if (var1.battleLType == 13) {
            drawState1LEffectType13(var0, var1);
         } else if (var1.battleLType == 14) {
            drawState1LEffectType14(var0, var1);
         } else if (var1.battleLType == 15) {
            drawState1LEffectType15(var0, var1);
         } else if (var1.battleLType == 11) {
            int var2 = Math.max(1, var1.battleLRow[1]);
            int var3 = var2 - 1;
            int var4 = 2 + (var3 << 2);
            if (var4 + 4 < var1.battleLRow.length) {
               BufferedImage var5 = renderSpriteAnimationFrameImage(var1.battleLSpriteId, 0, 0, var1.battleLDirection);
               if (var5 != null) {
                  int var6 = state1LEffectX(var1);
                  int var7 = state1LEffectY(var1);
                  int var8 = Math.max(0, Math.min(var1.battleLFrame, Math.max(0, var1.battleLRow[var4 + 1] - 1)));

                  for(int var9 = 1; var9 < var2; ++var9) {
                     int var10 = 2 + (var9 - 1 << 2);
                     int var11 = var4 + 4 + (var8 * var3 + var9 - 1 << 1);
                     if (var10 + 3 < var1.battleLRow.length && var11 + 1 < var1.battleLRow.length) {
                        BufferedImage var12 = tintOpaque(var5, var1.battleLRow[var10], var1.battleLRow[var10 + 1], var1.battleLRow[var10 + 2], var1.battleLRow[var10 + 3]);
                        int var13 = var1.battleLRow[var11];
                        short var14 = var1.battleLRow[var11 + 1];
                        if (var1.battleLDirection == 1) {
                           var13 = -var13;
                        }

                        var0.drawImage(var12, var6 + var13, var7 + var14, (ImageObserver)null);
                     }
                  }

               }
            }
         }
      }
   }

   private static void drawState1LEffectType14(Graphics2D var0, VqsvGameRuntime.Scene var1) {
      int var2 = Math.max(1, var1.battleLRow[1]);
      int var3 = var2 - 1;
      int var4 = 2 + (var3 << 2);
      if (var3 > 0 && var4 + 4 < var1.battleLRow.length) {
         BufferedImage var5 = renderSpriteAnimationFrameImage(var1.battleLSpriteId, 0, 0, var1.battleLDirection);
         if (var5 != null) {
            int var6 = state1LEffectX(var1);
            int var7 = state1LEffectY(var1);
            int var8 = Math.max(0, Math.min(var1.battleLFrame, Math.max(0, var1.battleLRow[var4 + 1] - 1)));

            for(int var9 = 1; var9 < var2; ++var9) {
               int var10 = 2 + (var9 - 1 << 2);
               int var11 = var4 + 4 + (var8 * var3 + var9 - 1 << 1);
               if (var10 + 1 < var1.battleLRow.length && var11 + 1 < var1.battleLRow.length) {
                  BufferedImage var12 = adjustRgb(var5, var1.battleLRow[var10], var1.battleLRow[var10 + 1]);
                  int var13 = var1.battleLRow[var11];
                  short var14 = var1.battleLRow[var11 + 1];
                  if (var1.battleLDirection == 1) {
                     var13 = -var13;
                  }

                  var0.drawImage(var12, var6 + var13, var7 + var14, (ImageObserver)null);
               }
            }

         }
      }
   }

   private static void drawState1LEffectType13(Graphics2D var0, VqsvGameRuntime.Scene var1) {
      int var2 = Math.max(1, var1.battleLRow[1]);
      int var3 = 2 + var2;
      if (var2 >= 1 && var3 + 4 < var1.battleLRow.length) {
         BufferedImage var4 = renderSpriteAnimationFrameImage(var1.battleLSpriteId, 0, 0, var1.battleLDirection);
         if (var4 != null) {
            int var5 = Math.max(1, var1.battleLRow[var3 + 1]);
            int var6 = Math.max(0, Math.min(var5 - 1, var1.battleLFrame));
            int var7 = state1LEffectX(var1);
            int var8 = state1LEffectY(var1);

            for(int var9 = 0; var9 < var2; ++var9) {
               int var10 = 2 + var9;
               int var11 = var3 + 4 + (var6 * var2 + var9 << 1);
               if (var10 < var1.battleLRow.length && var11 + 1 < var1.battleLRow.length) {
                  BufferedImage var12 = alphaCopy(var4, var1.battleLRow[var10]);
                  int var13 = var1.battleLRow[var11];
                  short var14 = var1.battleLRow[var11 + 1];
                  if (var1.battleLDirection == 1) {
                     var13 = -var13;
                  }

                  var0.drawImage(var12, var7 + var13, var8 + var14, (ImageObserver)null);
               }
            }

         }
      }
   }

   private static void drawState1LEffectType15(Graphics2D var0, VqsvGameRuntime.Scene var1) {
      int var2 = Math.max(1, var1.battleLRow[1]);
      int var3 = 2 + (var2 - 1 << 2);
      if (var2 >= 1 && var3 + 4 < var1.battleLRow.length) {
         int var4 = Math.max(1, var1.battleLRow[var3 + 1]);
         int var5 = Math.max(0, Math.min(var4 - 1, var1.battleLFrame));
         int var6 = var3 + 4 + var5 * 3;
         if (var6 + 2 < var1.battleLRow.length) {
            BufferedImage var7 = renderSpriteAnimationFrameImage(var1.battleLSpriteId, 0, 0, var1.battleLDirection);
            if (var7 != null) {
               int var8 = Math.max(0, Math.min(var2 - 1, var1.battleLRow[var6]));
               BufferedImage var9 = var7;
               if (var8 > 0) {
                  int var10 = 2 + (var8 - 1 << 2);
                  if (var10 + 3 >= var1.battleLRow.length) {
                     return;
                  }

                  var9 = tintOpaque(var7, var1.battleLRow[var10], var1.battleLRow[var10 + 1], var1.battleLRow[var10 + 2], var1.battleLRow[var10 + 3]);
               }

               int var14 = state1LEffectX(var1);
               int var11 = state1LEffectY(var1);
               int var12 = var1.battleLRow[var6 + 1];
               short var13 = var1.battleLRow[var6 + 2];
               if (var1.battleLDirection == 1) {
                  var12 = -var12;
               }

               var0.drawImage(var9, var14 + var12, var11 + var13, (ImageObserver)null);
            }
         }
      }
   }

   private static void drawState1LEffectType12(Graphics2D var0, VqsvGameRuntime.Scene var1) {
      if (var1.battleLRow.length >= 10) {
         int var2 = Math.max(1, var1.battleLRow[1]);
         if (var2 >= 2) {
            byte var3 = 4;
            int var4 = Math.max(1, var1.battleLRow[var3 + 1]);
            int var5 = var3 + 4 + Math.max(0, Math.min(var4 - 1, var1.battleLFrame)) * 2;
            int var6 = var3 + 4 + (var4 << 1) + Math.max(0, Math.min(var4 - 1, var1.battleLFrame)) * 2;
            if (var6 + 1 < var1.battleLRow.length) {
               BufferedImage var7 = renderSpriteAnimationFrameImage(var1.battleLSpriteId, 0, 0, var1.battleLDirection);
               if (var7 != null) {
                  BufferedImage var8 = alphaCopy(var7, var1.battleLRow[2]);
                  BufferedImage var9 = alphaCopy(var7, var1.battleLRow[3]);
                  int var10 = state1LEffectX(var1);
                  int var11 = state1LEffectY(var1);
                  short var12 = var1.battleLRow[var5];
                  short var13 = var1.battleLRow[var5 + 1];
                  short var14 = var1.battleLRow[var6];
                  short var15 = var1.battleLRow[var6 + 1];
                  if (var1.battleLDirection == 1) {
                     var0.drawImage(var9, var10 - (var14 + var12), var11 - var15 + var13, (ImageObserver)null);
                     var0.drawImage(var8, var10 - var12, var11 + var13, (ImageObserver)null);
                  } else {
                     var0.drawImage(var9, var10 + var14 + var12, var11 - var15 + var13, (ImageObserver)null);
                     var0.drawImage(var8, var10 + var12, var11 + var13, (ImageObserver)null);
                  }

               }
            }
         }
      }
   }

   private static int state1LEffectX(VqsvGameRuntime.Scene var0) {
      int var1 = battleRenderState(var0).doubleBattlePresentation.lEffectSlot;
      return battleRenderState(var0).simultaneous2v2() && var1 >= 0 ? formationActorX(var0, var1) + (var0.battleLPlayerSide ? -52 : -45) : (var0.battleLPlayerSide ? 18 : 132) + sideOffsetX(var0, var0.battleLPlayerSide);
   }

   private static int state1LEffectY(VqsvGameRuntime.Scene var0) {
      int var1 = battleRenderState(var0).doubleBattlePresentation.lEffectSlot;
      return battleRenderState(var0).simultaneous2v2() && var1 >= 0 ? formationActorY(var0, var1) + (var0.battleLPlayerSide ? -83 : -33) : (var0.battleLPlayerSide ? 140 : 70) + sideOffsetY(var0, var0.battleLPlayerSide);
   }

   private static BufferedImage renderSpriteCellImage(int var0, int var1, int var2) {
      SpriteAnimator var3 = SpriteAnimator.load(var0);
      int[] var4 = var3.cellBounds(var1);
      if (var4 != null && var4[2] > 0 && var4[3] > 0) {
         BufferedImage var5 = new BufferedImage(var4[2], var4[3], 2);
         Graphics2D var6 = var5.createGraphics();
         var3.drawCell(var6, var1, -var4[0], -var4[1], var2);
         var6.dispose();
         normalizeJavaMeEffectPixels(var5);
         return var5;
      } else {
         return null;
      }
   }

   private static BufferedImage renderSpriteAnimationFrameImage(int var0, int var1, int var2, int var3) {
      SpriteAnimator var4 = SpriteAnimator.load(var0);
      return renderSpriteCellImage(var0, var4.cellIdAtFrame(var1, var2), var3);
   }

   private static void normalizeJavaMeEffectPixels(BufferedImage var0) {
      for(int var1 = 0; var1 < var0.getHeight(); ++var1) {
         for(int var2 = 0; var2 < var0.getWidth(); ++var2) {
            int var3 = var0.getRGB(var2, var1);
            if (var3 >>> 24 == 0 || var3 == -1 || var3 == -16777216) {
               var0.setRGB(var2, var1, 16777215);
            }
         }
      }

   }

   private static BufferedImage tintOpaque(BufferedImage var0, int var1, int var2, int var3, int var4) {
      BufferedImage var5 = new BufferedImage(var0.getWidth(), var0.getHeight(), 2);
      int var6 = var1 >= 0 && var1 <= 255 ? var1 << 24 | var2 << 16 | var3 << 8 | var4 : var2 << 16 | var3 << 8 | var4;

      for(int var7 = 0; var7 < var0.getHeight(); ++var7) {
         for(int var8 = 0; var8 < var0.getWidth(); ++var8) {
            int var9 = var0.getRGB(var8, var7);
            if (var9 == 16777215) {
               var5.setRGB(var8, var7, 16777215);
            } else {
               var5.setRGB(var8, var7, var6);
            }
         }
      }

      return var5;
   }

   private static BufferedImage alphaCopy(BufferedImage var0, int var1) {
      BufferedImage var2 = new BufferedImage(var0.getWidth(), var0.getHeight(), 2);
      if (var1 >= 0 && var1 <= 255) {
         for(int var6 = 0; var6 < var0.getHeight(); ++var6) {
            for(int var4 = 0; var4 < var0.getWidth(); ++var4) {
               int var5 = var0.getRGB(var4, var6);
               if (var5 != 16777215 && var5 != 0) {
                  var2.setRGB(var4, var6, var5 == -16777216 ? 0 : var1 << 24 | var5 & 16777215);
               } else {
                  var2.setRGB(var4, var6, var5);
               }
            }
         }

         return var2;
      } else {
         Graphics2D var3 = var2.createGraphics();
         var3.drawImage(var0, 0, 0, (ImageObserver)null);
         var3.dispose();
         return var2;
      }
   }

   private static BufferedImage adjustRgb(BufferedImage var0, int var1, int var2) {
      BufferedImage var3 = new BufferedImage(var0.getWidth(), var0.getHeight(), 2);

      for(int var4 = 0; var4 < var0.getHeight(); ++var4) {
         for(int var5 = 0; var5 < var0.getWidth(); ++var5) {
            int var6 = var0.getRGB(var5, var4);
            int var7 = clamp((var6 >> 16 & 255) * var1 + var2);
            int var8 = clamp((var6 >> 8 & 255) * var1 + var2);
            int var9 = clamp((var6 & 255) * var1 + var2);
            var3.setRGB(var5, var4, var6 & -16777216 | var7 << 16 | var8 << 8 | var9);
         }
      }

      return var3;
   }

   private static int clamp(int var0) {
      return Math.max(0, Math.min(255, var0));
   }

   private static int sideOffsetX(VqsvGameRuntime.Scene var0, boolean var1) {
      return var1 ? playerOffsetX(var0) : enemyOffsetX(var0);
   }

   private static int sideOffsetY(VqsvGameRuntime.Scene var0, boolean var1) {
      return var1 ? playerOffsetY(var0) : enemyOffsetY(var0);
   }

   private static int playerOffsetX(VqsvGameRuntime.Scene var0) {
      return var0.battleP7PlayerOffsetX;
   }

   private static int playerOffsetY(VqsvGameRuntime.Scene var0) {
      return var0.battleP7PlayerOffsetY;
   }

   private static int enemyOffsetX(VqsvGameRuntime.Scene var0) {
      return var0.battleP7EnemyOffsetX;
   }

   private static int enemyOffsetY(VqsvGameRuntime.Scene var0) {
      return var0.battleP7EnemyOffsetY;
   }

   private static void applyAhType9Transform(BufferedImage var0, int var1, int var2, int var3, int var4) {
      int var5 = Math.max(0, Math.min(255, var1));
      int var6 = Math.max(0, Math.min(255, var2));
      int var7 = Math.max(0, Math.min(255, var3));
      int var8 = Math.max(0, Math.min(255, var4));

      for(int var9 = 0; var9 < var0.getHeight(); ++var9) {
         for(int var10 = 0; var10 < var0.getWidth(); ++var10) {
            int var11 = var0.getRGB(var10, var9);
            int var12 = var11 >>> 24;
            if (var12 != 0) {
               int var13 = Math.min(255, var5 + 50);
               int var14 = Math.min(255, var6 + 50);
               int var15 = Math.min(255, var7 + 50);
               int var16 = Math.min(255, var8 + 50);
               var0.setRGB(var10, var9, var13 << 24 | var14 << 16 | var15 << 8 | var16);
            }
         }
      }

   }

   private static void applyAhType1Texture(BufferedImage var0, VqsvGameRuntime.Scene var1) {
      BufferedImage var2 = loadTexImage(var1.battleP7SpecialTextureId);
      if (var2 != null && var2.getWidth() > 0 && var2.getHeight() > 0) {
         int var3 = var1.battleP7Ticks * 4;
         int var4 = var1.battleP7SpecialScrollMode;

         for(int var5 = 0; var5 < var0.getHeight(); ++var5) {
            for(int var6 = 0; var6 < var0.getWidth(); ++var6) {
               int var7 = var0.getRGB(var6, var5);
               if (var7 >>> 24 != 0) {
                  int var8 = var6;
                  int var9 = var5;
                  if (var4 == 0) {
                     var9 = var5 + var3;
                  } else if (var4 == 1) {
                     var9 = var5 - var3;
                  } else if (var4 == 2) {
                     var8 = var6 + var3;
                  } else if (var4 == 3) {
                     var8 = var6 - var3;
                  }

                  int var10 = var2.getRGB(Math.floorMod(var8, var2.getWidth()), Math.floorMod(var9, var2.getHeight()));
                  int var11 = brightenForAhType1(var7);
                  int var12;
                  if (var1.battleP7SpecialBlendMode == 1) {
                     var12 = var11 | var10;
                  } else if (var1.battleP7SpecialBlendMode == 2) {
                     var12 = var10;
                  } else {
                     var12 = var11 & var10;
                  }

                  var0.setRGB(var6, var5, var12);
               }
            }
         }

      }
   }

   private static int brightenForAhType1(int var0) {
      int var1 = var0 >>> 24;
      int var2 = Math.min(255, (var0 >>> 16 & 255) * 5 + 5);
      int var3 = Math.min(255, (var0 >>> 8 & 255) * 5 + 5);
      int var4 = Math.min(255, (var0 & 255) * 5 + 5);
      return var1 << 24 | var2 << 16 | var3 << 8 | var4;
   }

   private static BufferedImage loadTexImage(int var0) {
      if (var0 < 0) {
         return null;
      } else if (TEX_CACHE.containsKey(var0)) {
         return (BufferedImage)TEX_CACHE.get(var0);
      } else {
         try {
            AssetPaths var1 = AssetPaths.fromWorkingTree(GameConfig.defaultConfig());
            java.nio.file.Path var2 = var1.texDecodedPng("tex_" + var0);
            BufferedImage var3 = (new ImageLoader(var1)).load(var2);
            TEX_CACHE.put(var0, var3);
            return var3;
         } catch (RuntimeException var4) {
            TEX_CACHE.put(var0, null);
            return null;
         }
      }
   }

   private static void drawSourceUiFill(Graphics2D var0, int var1, int var2, int var3, int var4, int var5) {
      Color var6 = var0.getColor();
      var0.setColor(new Color(var5));
      var0.fillRect(var1, var2, var3, var4);
      var0.setColor(var6);
   }

   private static void drawWarningOverlay(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2) {
      VqsvMsgWarmView var3 = noticeRenderState(var2).warningView();
      VqsvUiLayout var4 = VqsvUiLayout.load("msgwarm.ui");
      drawSourceWidgetFill(var0, var4, 1, 7, 13038591);
      drawSourceWidgetFill(var0, var4, 2, 59, 12510962);
      drawSourceWidgetFill(var0, var4, 3, 10, 7127803);
      drawSourceWidgetFill(var0, var4, 5, 54, 5363945);
      drawBattleUiCellTopLeft(var0, 128, var4.x(8, 76), var4.y(8, 106));
      drawMarqueeTinyBattleText(var0, var1, var3.widgetText(7), var4.x(7, 85), var4.y(7, 119), var4.w(7, 70), SOURCE_UI_TEXT, var2.battleAnimationTick);
      drawMarqueeTinyBattleText(var0, var1, var3.widgetText(6), var4.x(6, 89), var4.y(6, 170), var4.w(6, 60), SOURCE_UI_TEXT, var2.battleAnimationTick);
   }

   static void drawSmsInfoOverlay(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2) {
      VqsvBattleNoticeRenderState var3 = noticeRenderState(var2);
      drawSmsInfoOverlay(var0, var1, var3.title, var3.prompt);
   }

   static void drawSmsInfoOverlay(Graphics2D var0, UiFont var1, String var2, String var3) {
      VqsvUiLayout var4 = VqsvUiLayout.load("smsInfo.ui");
      drawSourceWidgetFill(var0, var4, 3, 9, 13038079);
      drawSourceWidgetFill(var0, var4, 1, 159, 12444911);
      drawSourceWidgetFill(var0, var4, 2, 11, 8573179);
      drawSourceWidgetCell(var0, var4, 4, false, false);
      drawSmsInfoWrappedText(var0, var1, var4, 8, var2, SOURCE_UI_TEXT);
      drawSmsInfoCenteredText(var0, var1, var4, 5, var3, SOURCE_UI_TEXT);
      drawCenteredTinyText(var0, var1, "Xác nhận", var4.x(10, 52), var4.y(10, 240), Math.max(54, var4.w(10, 24)), Color.WHITE);
      drawCenteredTinyText(var0, var1, "Phản hồi", Math.max(130, var4.x(11, 167) - 36), var4.y(11, 240), Math.max(58, var4.w(11, 24)), Color.WHITE);
   }

   private static void drawSmsInfoWrappedText(Graphics2D var0, UiFont var1, VqsvUiLayout var2, int var3, String var4, Color var5) {
      VqsvUiLayout.UiWidget var6 = var2.widget(var3);
      int var7 = var6 == null ? 62 : var6.x;
      int var8 = var6 == null ? 101 : var6.y;
      int var9 = var6 == null ? 118 : Math.max(1, var6.w);
      int var10 = var6 == null ? 68 : Math.max(1, var6.h);
      Shape var11 = var0.getClip();
      var0.clipRect(var7, var8 - 1, var9, var10 + 2);
      String var12 = String.format("#%06x%s", var5.getRGB() & 16777215, TextBox.decodeMojibake(var4));
      var1.drawTagged(var0, var12, var7, var8, var9, TextBox.visibleLength(var12));
      var0.setClip(var11);
   }

   private static void drawSmsInfoCenteredText(Graphics2D var0, UiFont var1, VqsvUiLayout var2, int var3, String var4, Color var5) {
      VqsvUiLayout.UiWidget var6 = var2.widget(var3);
      int var7 = var6 == null ? 66 : var6.x;
      int var8 = var6 == null ? 188 : var6.y;
      int var9 = var6 == null ? 100 : Math.max(1, var6.w);
      drawCenteredTinyText(var0, var1, var4, var7, var8, var9, var5);
   }

   private static void drawLevelUpOverlay(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2) {
      VqsvBattleLevelUpView var3 = var2.battleLevelUpView == null ? VqsvBattleLevelUpView.EMPTY : var2.battleLevelUpView;
      if (var3.visible) {
         if (var3.leveled) {
            drawSourceUiFill(var0, 43, 55, 158, 202, 9033967);
            drawSourceUiFill(var0, 46, 78, 150, 9, 13038591);
            drawSourceUiFill(var0, 46, 87, 150, 159, 16252927);
            drawSourceUiFill(var0, 46, 246, 150, 11, 8573179);
            drawBattleUiCellTopLeft(var0, 1, 43, 55);
            drawBattleUiCellTopLeft(var0, 8, 78, 94);
            drawBattleUiCellTopLeft(var0, 17, 50, 90);
            drawBattleUiCellTopLeft(var0, 15, 144, 90);
            drawCenteredTinyText(var0, var1, "Thăng cấp", 70, 60, 100, Color.WHITE);
            if (var3.visualId >= 0) {
               Shape var4 = var0.getClip();
               var0.clipRect(78, 90, 90, 88);
               drawBattleSprite(var0, var3.visualId, 78, 90, 90, 88, 7, 0, 0, idleCursor(var3.visualId, 0, var2.battleAnimationTick));
               var0.setClip(var4);
            }

            drawMarqueeTinyBattleText(var0, var1, var3.name, 53, 84, 72, SOURCE_UI_TEXT, var2.battleLevelUpTicks);
            drawTinyBattleText(var0, var1, "lv", 150, 82, 12, SOURCE_UI_TEXT);
            drawTinyBattleText(var0, var1, String.valueOf(var3.level), 165, 82, 24, SOURCE_UI_TEXT);
            drawLevelUpStats(var0, var1, var3, var2.battleLevelUpTicks);
            if (!var3.message.isEmpty()) {
               drawMarqueeTinyBattleText(var0, var1, var3.message, 76, 240, 96, SOURCE_UI_TEXT, var2.battleLevelUpTicks);
            }

         }
      }
   }

   private static void drawLevelUpStats(Graphics2D var0, UiFont var1, VqsvBattleLevelUpView var2, int var3) {
      String[] var4 = new String[]{"Mệnh", "Công", "Phòng", "Min"};
      int[] var5 = new int[]{182, 196, 209, 222};
      int[] var6 = new int[]{188, 201, 214, 227};

      for(int var7 = 0; var7 < 4; ++var7) {
         int var8 = var7 == 0 ? 15 : 16;
         drawBattleUiCellTopLeft(var0, var8, 59, var6[var7]);
         drawBattleUiCellTopLeft(var0, var8, 139, var6[var7]);
         drawBattleUiCellTopLeft(var0, 22, 114, var6[var7] - 1);
         drawMarqueeTinyBattleText(var0, var1, var4[var7], 65, var5[var7], 12, SOURCE_UI_TEXT, var3);
         drawTinyBattleText(var0, var1, String.valueOf(var2.oldStats[var7]), 80, var5[var7], 24, SOURCE_UI_TEXT);
         drawMarqueeTinyBattleText(var0, var1, var4[var7], 145, var5[var7], 12, SOURCE_UI_TEXT, var3);
         drawTinyBattleText(var0, var1, String.valueOf(var2.newStats[var7]), 160, var5[var7], 24, SOURCE_UI_TEXT);
      }

   }

   private static void drawCatchAnimation(Graphics2D var0, VqsvGameRuntime.Scene var1, VqsvBattleCatchRenderState var2) {
      if (var2.effectVisible) {
         drawCatchEffectType8(var0, var1, var2);
      }

      SpriteAnimator var3 = SpriteAnimator.load(var2.spriteId);
      var3.setState(Math.max(0, var2.phase));
      var3.setCursor(Math.max(0, var2.animCursor));
      if (var2.phase == 3) {
         int[] var4 = enemyCatchSuccessGroundPoint(var1);
         drawCatchBallVisibleCenterAt(var0, var3, var4[0], var4[1]);
      } else if (var2.phase == 0) {
         int[] var5 = catchThrowPoint(var1, var3);
         drawCatchBallVisibleCenterAt(var0, var3, var5[0], var5[1]);
      } else {
         int[] var6 = enemyCaptureBallPoint(var1, var3);
         drawCatchBallVisibleCenterAt(var0, var3, var6[0], var6[1]);
      }

   }

   private static void drawNpcEnemyEntryOverlay(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2) {
      VqsvBattleNpcEnemyEntryRenderState var3 = npcEnemyEntryRenderState(var2);
      if (var3.step >= 5) {
         drawBattleBackground(var0, var2);
      } else {
         var0.setColor(Color.BLACK);
         var0.fillRect(0, 0, 240, 320);
      }

      VqsvUiLayout var4 = VqsvUiLayout.load("npcEnemy.ui");
      drawSpriteCellTopLeft(var0, 296, var3.mainCell, var4.x(1, 0), var4.y(1, 71));
      drawNpcEntryPortraits(var0, var4, var3);
      drawNpcEntryTeamSlots(var0, var4, var3);
      drawNpcEntryLabels(var0, var1, var4, var3, var2.battleAnimationTick);
      if (var3.overlay36) {
         VqsvUiLayout.UiWidget var5 = var4.widget(36);
         if (var5 != null) {
            Color var6 = var0.getColor();
            var0.setColor(new Color(255, 255, 255, 92));
            var0.fillRect(var5.x, var5.y, Math.max(1, var5.w), sourceWidgetHeight(var5));
            var0.setColor(var6);
         }
      }

   }

   private static void drawNpcEntryPortraits(Graphics2D var0, VqsvUiLayout var1, VqsvBattleNpcEnemyEntryRenderState var2) {
      drawNpcEntryPortrait(var0, var1, var2, 2, var2.enemyVisualId, var2.enemySkinKey, false);
      drawNpcEntryPortrait(var0, var1, var2, 3, var2.playerVisualId, var2.playerSkinKey, true);
      drawNpcEntryPortrait(var0, var1, var2, 34, var2.enemyVisualId, var2.enemySkinKey, false);
      drawNpcEntryPortrait(var0, var1, var2, 35, var2.playerVisualId, var2.playerSkinKey, true);
      drawNpcEntryPortrait(var0, var1, var2, 4, var2.enemyVisualId, var2.enemySkinKey, false);
      drawNpcEntryPortrait(var0, var1, var2, 5, var2.playerVisualId, var2.playerSkinKey, true);
   }

   private static void drawNpcEntryPortrait(Graphics2D var0, VqsvUiLayout var1, VqsvBattleNpcEnemyEntryRenderState var2, int var3, int var4, String var5, boolean var6) {
      if (var2.visible[var3]) {
         VqsvUiLayout.UiWidget var7 = var1.widget(var3);
         if (var7 != null) {
            int var8 = Math.max(48, sourceWidgetHeight(var7));
            if (!drawFashionPortrait(var0, var5, var7.x, var7.y, Math.max(1, var7.w), var8, var6)) {
               drawNpcEntryPortrait(var0, var4, var7.x, var7.y, Math.max(1, var7.w), var8, var6);
            }

         }
      }
   }

   private static boolean drawFashionPortrait(Graphics2D var0, String var1, int var2, int var3, int var4, int var5, boolean var6) {
      if (var1 != null && !var1.isBlank()) {
         SourceFashionRecord var7 = SourceFashionCatalog.instance().byStableKey(var1);
         if (var7 == null) {
            return false;
         } else {
            SpriteAnimator var8 = SpriteAnimator.loadFashion(var7);
            var8.setState(0);
            var8.setCursorClamped(0);
            var8.drawAligned(var0, var2, var3, var4, var5, var6 ? 7 : 1, var6 ? 1 : 0);
            return true;
         }
      } else {
         return false;
      }
   }

   private static void drawPvpTurnTimer(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2) {
      if (var2.battlePvpOnline && var2.battlePvpTurn > 0) {
         String var3 = var2.battlePvpTimerActive ? String.valueOf(Math.max(0, var2.battlePvpRemainingSeconds)) : "--";
         int var4 = !var2.battlePvpTimerActive ? -3680548 : (var2.battlePvpRemainingSeconds <= 15 ? 16739159 : 16773482);
         drawSourceUiFill(var0, 91, 2, 58, 16, 473421);
         var0.setColor(new Color(var4));
         var0.drawRect(91, 2, 57, 15);
         drawTinyBattleText(var0, var1, "L" + var2.battlePvpTurn + "  " + var3 + (var2.battlePvpTimerActive ? "s" : "  TÍNH"), 96, 6, 49, new Color(var4));
      }
   }

   private static void drawNpcEntryPortrait(Graphics2D var0, int var1, int var2, int var3, int var4, int var5, boolean var6) {
      if (var1 >= 0) {
         drawBattleSprite(var0, var1, var2, var3, Math.max(1, var4), Math.max(1, var5), var6 ? 7 : 1, battlePetOrientation(var1, var6));
      }
   }

   private static void drawNpcEntryTeamSlots(Graphics2D var0, VqsvUiLayout var1, VqsvBattleNpcEnemyEntryRenderState var2) {
      for(int var3 = 6; var3 <= 29; ++var3) {
         if (var2.cells[var3] >= 0) {
            VqsvUiLayout.UiWidget var4 = var1.widget(var3);
            if (var4 != null) {
               int var5 = var4.x;
               if (var2.exitSlotRows && var3 >= 7 && var3 < 19 && var3 % 2 == 1) {
                  var5 = 172 + 17 * (var3 - 7) / 2;
               } else if (var2.exitSlotRows && var3 >= 19 && var3 < 31 && var3 % 2 == 1) {
                  var5 = -30 + 17 * (var3 - 19) / 2;
               }

               drawSpriteCellTopLeft(var0, 296, var2.cells[var3], var5, var4.y);
            }
         }
      }

   }

   private static void drawNpcEntryLabels(Graphics2D var0, UiFont var1, VqsvUiLayout var2, VqsvBattleNpcEnemyEntryRenderState var3, int var4) {
      drawNpcEntryCell(var0, var2, var3, 30);
      drawNpcEntryCell(var0, var2, var3, 31);
      drawNpcEntryCell(var0, var2, var3, 32);
      drawNpcEntryCell(var0, var2, var3, 33);
      if (var3.cells[30] >= 0) {
         drawNpcEntryText(var0, var1, var2, 30, var3.enemyName, new Color(16777215), var4);
      }

      if (var3.cells[31] >= 0) {
         drawNpcEntryText(var0, var1, var2, 31, var3.playerName, new Color(16777215), var4);
      }

      if (var3.cells[32] >= 0) {
         drawNpcEntryText(var0, var1, var2, 32, "VS", new Color(16773482), var4);
      }

   }

   private static void drawNpcEntryCell(Graphics2D var0, VqsvUiLayout var1, VqsvBattleNpcEnemyEntryRenderState var2, int var3) {
      if (var2.cells[var3] >= 0) {
         VqsvUiLayout.UiWidget var4 = var1.widget(var3);
         if (var4 != null) {
            drawSpriteCellTopLeft(var0, 296, var2.cells[var3], var4.x, var4.y);
         }

      }
   }

   private static void drawNpcEntryText(Graphics2D var0, UiFont var1, VqsvUiLayout var2, int var3, String var4, Color var5, int var6) {
      VqsvUiLayout.UiWidget var7 = var2.widget(var3);
      if (var7 != null && var4 != null && !var4.isEmpty()) {
         drawSourceWidgetText(var0, var1, var4, var7.x, var7.y, Math.max(1, var7.w), sourceWidgetHeight(var7), var5, var6, var7.b);
      }
   }

   private static void drawCatchBallVisibleCenterAt(Graphics2D var0, SpriteAnimator var1, int var2, int var3) {
      int var4 = var1.currentCellId();
      int[] var5 = var1.currentCellBounds();
      if (var4 >= 0 && var5 != null && var5[2] > 0 && var5[3] > 0) {
         BufferedImage var6 = renderSpriteCellImage(var1, var4, var5, 0);
         if (var6 != null) {
            int[] var7 = opaqueBounds(var6);
            if (var7 != null) {
               int var8 = var2 - var7[0] - var7[2] / 2;
               int var9 = var3 - var7[1] - var7[3] / 2;
               var0.drawImage(var6, var8, var9, (ImageObserver)null);
            }
         }
      }
   }

   private static int[] catchThrowPoint(VqsvGameRuntime.Scene var0, SpriteAnimator var1) {
      int[] var2 = playerThrowBallPoint(var0);
      int[] var3 = enemyCaptureBallPoint(var0, var1);
      int var4 = Math.max(1, var1.frameCount());
      int var5 = Math.max(0, Math.min(var1.cursor(), var4 - 1));
      int var6 = var2[0] + (var3[0] - var2[0]) * var5 / Math.max(1, var4 - 1);
      int var7 = var2[1] + (var3[1] - var2[1]) * var5 / Math.max(1, var4 - 1);
      return new int[]{var6, var7};
   }

   private static int[] playerThrowBallPoint(VqsvGameRuntime.Scene var0) {
      int[] var1 = playerVisibleSpriteRect(var0);
      return new int[]{var1[0] + var1[2] / 2, var1[1] + var1[3] / 2};
   }

   private static int[] enemyCaptureBallPoint(VqsvGameRuntime.Scene var0, SpriteAnimator var1) {
      int[] var2 = enemyVisibleSpriteRect(var0);
      int[] var3 = enemyGroundMarkerRect(var0);
      int var4 = catchBallVisibleHeight(var1);
      int var5 = var3[0] + var3[2] / 2;
      int var6 = var2[1] - Math.max(3, var4 / 2) + 1;
      return new int[]{var5, var6};
   }

   private static int[] enemyCatchSuccessGroundPoint(VqsvGameRuntime.Scene var0) {
      int[] var1 = enemyGroundMarkerRect(var0);
      int var2 = var1[0] + var1[2] / 2;
      int var3 = var1[1] + var1[3] / 2;
      return new int[]{var2, var3};
   }

   private static int catchBallVisibleHeight(SpriteAnimator var0) {
      int var1 = var0.currentCellId();
      int[] var2 = var0.currentCellBounds();
      if (var1 >= 0 && var2 != null && var2[2] > 0 && var2[3] > 0) {
         BufferedImage var3 = renderSpriteCellImage(var0, var1, var2, 0);
         int[] var4 = var3 == null ? null : opaqueBounds(var3);
         return var4 == null ? Math.max(1, var2[3]) : Math.max(1, var4[3]);
      } else {
         return 12;
      }
   }

   private static BufferedImage renderSpriteCellImage(SpriteAnimator var0, int var1, int[] var2, int var3) {
      BufferedImage var4 = new BufferedImage(var2[2], var2[3], 2);
      Graphics2D var5 = var4.createGraphics();
      var0.drawCell(var5, var1, -var2[0], -var2[1], var3);
      var5.dispose();
      normalizeJavaMeEffectPixels(var4);
      return var4;
   }

   private static void drawCatchEffectType8(Graphics2D var0, VqsvGameRuntime.Scene var1, VqsvBattleCatchRenderState var2) {
      if (var1.battleEnemyVisualId >= 0) {
         SpriteAnimator var3 = SpriteAnimator.load(var1.battleEnemyVisualId);
         int var4 = var3.cellIdAtFrame(0, 0);
         int[] var5 = var3.cellBounds(var4);
         if (var5 != null && var5[2] > 0 && var5[3] > 0) {
            BufferedImage var6 = new BufferedImage(var5[2], var5[3], 2);
            Graphics2D var7 = var6.createGraphics();
            var7.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            var3.drawCell(var7, var4, -var5[0], -var5[1], 0);
            var7.dispose();
            brightenOpaquePixels(var6, 50);
            int var8 = Math.max(1, var2.effectScale10);
            int var9 = Math.max(1, var6.getWidth() * var8 / 10);
            int var10 = Math.max(1, var6.getHeight() * var8 / 10);
            int var11 = sourceBattleActorX(var1, false) + enemyOffsetX(var1);
            int var12 = sourceBattleActorY(var1, false) + enemyOffsetY(var1);
            int var13 = var5[0] * var8 / 10;
            int var14 = var5[1] * var8 / 10;
            int var15 = var11 + var13 + var2.effectDx;
            int var16 = var12 + var14 + var2.effectDy;
            Object var17 = var0.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
            var0.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            var0.drawImage(var6, var15, var16, var9, var10, (ImageObserver)null);
            if (var17 == null) {
               var0.getRenderingHints().remove(RenderingHints.KEY_INTERPOLATION);
            } else {
               var0.setRenderingHint(RenderingHints.KEY_INTERPOLATION, var17);
            }

         } else {
            drawBattleSprite(var0, var1.battleEnemyVisualId, sourceBattleActorX(var1, false) + enemyOffsetX(var1) + var2.effectDx, sourceBattleActorY(var1, false) + enemyOffsetY(var1) + var2.effectDy, 96, 118, 7, 0);
         }
      }
   }

   private static int[] enemyVisibleSpriteRect(VqsvGameRuntime.Scene var0) {
      int var1 = sourceBattleActorX(var0, false) + enemyOffsetX(var0);
      int var2 = sourceBattleActorY(var0, false) + enemyOffsetY(var0);
      if (var0.battleEnemyVisualId < 0) {
         return new int[]{var1, var2, 96, 118};
      } else {
         SpriteAnimator var3 = SpriteAnimator.load(var0.battleEnemyVisualId);
         int[] var4 = var3.animationBounds(Math.max(0, var0.battleP7BaseStateEnemySide));
         return var4 != null && var4[2] > 0 && var4[3] > 0 ? new int[]{var1 + var4[0], var2 + var4[1], var4[2], var4[3]} : new int[]{var1, var2, 96, 118};
      }
   }

   private static int[] playerVisibleSpriteRect(VqsvGameRuntime.Scene var0) {
      int var1 = sourceBattleActorX(var0, true) + playerOffsetX(var0);
      int var2 = sourceBattleActorY(var0, true) + playerOffsetY(var0);
      if (var0.battlePlayerVisualId < 0) {
         return new int[]{var1, var2, 96, 95};
      } else {
         SpriteAnimator var3 = SpriteAnimator.load(var0.battlePlayerVisualId);
         int[] var4 = var3.animationBounds(Math.max(0, var0.battleP7BaseStatePlayerSide));
         return var4 != null && var4[2] > 0 && var4[3] > 0 ? new int[]{var1 + var4[0], var2 + var4[1], var4[2], var4[3]} : new int[]{var1, var2, 96, 95};
      }
   }

   private static int[] enemyGroundMarkerRect(VqsvGameRuntime.Scene var0) {
      SpriteAnimator var1 = SpriteAnimator.load(294);
      var1.setState(0);
      int var2 = sourceBattleMarkerX(var0, false) + sideOffsetX(var0, false);
      int var3 = sourceBattleMarkerY(var0, false) + sideOffsetY(var0, false);
      int[] var4 = var1.animationBounds(0);
      return var4 != null && var4[2] > 0 && var4[3] > 0 ? new int[]{var2 + var4[0], var3 + var4[1], var4[2], var4[3]} : new int[]{var2, var3, 64, 24};
   }

   private static int[] opaqueBounds(BufferedImage var0) {
      int var1 = Integer.MAX_VALUE;
      int var2 = Integer.MAX_VALUE;
      int var3 = Integer.MIN_VALUE;
      int var4 = Integer.MIN_VALUE;

      for(int var5 = 0; var5 < var0.getHeight(); ++var5) {
         for(int var6 = 0; var6 < var0.getWidth(); ++var6) {
            int var7 = var0.getRGB(var6, var5);
            if (var7 >>> 24 != 0 && var7 != 16777215) {
               var1 = Math.min(var1, var6);
               var2 = Math.min(var2, var5);
               var3 = Math.max(var3, var6);
               var4 = Math.max(var4, var5);
            }
         }
      }

      if (var1 == Integer.MAX_VALUE) {
         return null;
      } else {
         return new int[]{var1, var2, var3 - var1 + 1, var4 - var2 + 1};
      }
   }

   private static void brightenOpaquePixels(BufferedImage var0, int var1) {
      for(int var2 = 0; var2 < var0.getHeight(); ++var2) {
         for(int var3 = 0; var3 < var0.getWidth(); ++var3) {
            int var4 = var0.getRGB(var3, var2);
            int var5 = var4 >>> 24;
            if (var5 != 0) {
               int var6 = Math.min(255, (var4 >>> 16 & 255) + var1);
               int var7 = Math.min(255, (var4 >>> 8 & 255) + var1);
               int var8 = Math.min(255, (var4 & 255) + var1);
               var0.setRGB(var3, var2, var5 << 24 | var6 << 16 | var7 << 8 | var8);
            }
         }
      }

   }

   private static void drawStatusSlots(Graphics2D var0, int var1, int var2, int var3, int var4, boolean var5, int[] var6, int[] var7, BattleStatusPresentation[] var8) {
      if (var8 != null && var8.length > 0) {
         int var14 = Math.min(6, var8.length);

         for(int var15 = 0; var15 < var14; ++var15) {
            BattleStatusPresentation var11 = var8[var15];
            int var12 = var5 ? -var15 * 15 : var15 * 15;
            if (var11.iconKind == BattleStatusPresentation.IconKind.RAINBOW_RESOURCE) {
               BufferedImage var13 = rainbowStatusIcon(var11.resourcePath);
               if (var13 != null) {
                  var0.drawImage(var13, var1 + var12, var2, (ImageObserver)null);
               }
            } else {
               drawSpriteCellTopLeft(var0, 325, var11.lietHoaAtlasCell, var1 + var12, var2);
            }

            drawBattleUiCellTopLeft(var0, durationCell(var11.remainingDuration), var3 + var12, var4);
         }

      } else {
         for(int var9 = 0; var9 < 6; ++var9) {
            int var10 = var5 ? -var9 * 15 : var9 * 15;
            drawSpriteCellTopLeft(var0, 325, statusCell(var6, var9, 0), var1 + var10, var2);
            drawBattleUiCellTopLeft(var0, statusCell(var7, var9, 145), var3 + var10, var4);
         }

      }
   }

   private static void drawRainbowSkillAnimation(Graphics2D var0, VqsvGameRuntime.Scene var1) {
      RainbowSkillAnimationView var2 = var1.battleRainbowSkillAnimation;
      if (var2 != null && var2.visible && var2.groupId >= 0 && var2.cellId >= 0) {
         if (var2.groupId >= 6) {
            drawVqsv4SkillAnimation(var0, var1, var2);
         } else {
            RainbowSkillAnimationCatalog.Group var3 = RainbowSkillAnimationCatalog.instance().group(var2.groupId);
            int[][] var4 = var3.cellComponents(var2.cellId);
            boolean var5 = var2.targetPlayerSide;
            int var6 = var2.targetSlot;
            boolean var7 = var2.targetPlayerSide;
            if (var2.stableSkillKey.endsWith("-27") || var2.stableSkillKey.endsWith("-28")) {
               var7 = !var7;
               if (battleRenderState(var1).simultaneous2v2() && var6 >= 0) {
                  var6 = (var6 + 2) % 4;
               }
            }

            int var8 = p7ActorX(var1, var6, var7) + (var7 ? -24 : 0);
            int var9 = p7ActorY(var1, var6, var7) + (var7 ? -17 : -6);
            int[] var10 = new int[]{0, 5, 3, 6, 2, 4, 1, 7};

            for(int[] var14 : var4) {
               if (var14.length >= 4) {
                  int[] var15 = var3.frameRegion(var14[0]);
                  if (var15 != null) {
                     int var16 = Math.floorMod(var14[3], 8);
                     int var17 = !var5 ? var16 : (var16 + 4) % 8;
                     int var18 = var10[var17];
                     BufferedImage var19 = var3.image.getSubimage(var15[0], var15[1], var15[2], var15[3]);
                     BufferedImage var20 = MidpTransform.transform(var19, var18);
                     int var21 = var8 + var14[1] * (!var5 ? 1 : -1);
                     if (var5) {
                        var21 -= var20.getWidth();
                     }

                     int var22 = var9 + var14[2];
                     var0.drawImage(var20, var21, var22, (ImageObserver)null);
                  }
               }
            }

         }
      }
   }

   private static void drawVqsv4SkillAnimation(Graphics2D var0, VqsvGameRuntime.Scene var1, RainbowSkillAnimationView var2) {
      RainbowSkillAnimationCatalog.Vqsv4Animation var3 = RainbowSkillAnimationCatalog.instance().vqsv4Animation(var2.groupId);
      if (var2.frameIndex >= 0 && var2.frameIndex < var3.frameCount) {
         boolean var4 = var3.anchoredToCaster();
         int var5 = var4 ? var2.casterSlot : var2.targetSlot;
         boolean var6 = var4 ? var2.casterPlayerSide : var2.targetPlayerSide;
         int var8 = var4 ? 46 : 177;
         int var9 = var4 ? 206 : 97;
         int var10 = var3.anchorX - var8 + var3.originX;
         int var11 = var3.anchorY - var9 + var3.originY;
         boolean var12 = var6 != var4;
         int[] var13 = new int[]{0, 5, 3, 6, 2, 4, 1, 7};
         int var14 = Math.floorMod(var3.globalTransform + (var12 ? 4 : 0), 8);
         BufferedImage var15 = MidpTransform.transform(var3.frame(var2.frameIndex), var13[var14]);
         int var16 = p7ActorX(var1, var5, var6);
         int var17 = p7ActorY(var1, var5, var6);
         int var18 = var12 ? var16 - var10 - var15.getWidth() : var16 + var10;
         int var19 = var17 + var11;
         var0.drawImage(var15, var18, var19, (ImageObserver)null);
      }
   }

   private static int durationCell(int var0) {
      return var0 <= 0 ? 145 : 134 + Math.min(11, var0);
   }

   private static BufferedImage rainbowStatusIcon(String var0) {
      if (var0 != null && !var0.isEmpty()) {
         if (STATUS_ICON_CACHE.containsKey(var0)) {
            return (BufferedImage)STATUS_ICON_CACHE.get(var0);
         } else {
            try {
               InputStream var1 = VqsvBattleRenderer.class.getResourceAsStream(var0);

               BufferedImage var7;
               label62: {
                  BufferedImage var3;
                  try {
                     if (var1 == null) {
                        var7 = null;
                        break label62;
                     }

                     var7 = ImageIO.read(var1);
                     if (var7 != null) {
                        STATUS_ICON_CACHE.put(var0, var7);
                     }

                     var3 = var7;
                  } catch (Throwable var5) {
                     if (var1 != null) {
                        try {
                           var1.close();
                        } catch (Throwable var4) {
                           var5.addSuppressed(var4);
                        }
                     }

                     throw var5;
                  }

                  if (var1 != null) {
                     var1.close();
                  }

                  return var3;
               }

               if (var1 != null) {
                  var1.close();
               }

               return var7;
            } catch (IOException var6) {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private static int statusCell(int[] var0, int var1, int var2) {
      return var0 != null && var1 >= 0 && var1 < var0.length ? var0[var1] : var2;
   }

   private static void drawBattlePercent(Graphics2D var0, UiFont var1, int var2, int var3, int var4) {
      Color var5 = var4 > 100 ? new Color(16773536) : (var4 < 100 ? new Color(12114175) : Color.WHITE);
      drawTinyBattleText(var0, var1, var4 + "%", var2, var3 + 1, 28, var5);
   }

   private static void drawTinyBattleText(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, Color var6) {
      Shape var7 = var0.getClip();
      var0.clipRect(var3, var4 - 1, var5, 18);
      var1.drawTaggedLine(var0, var2, var3, var4, TextBox.visibleLength(TextBox.decodeMojibake(var2)), var6.getRGB() & 16777215);
      var0.setClip(var7);
   }

   private static String fitTinyBattleText(UiFont var0, String var1, int var2) {
      String var3 = TextBox.decodeMojibake(var1 == null ? "" : var1);
      if (var0.taggedWidth(var3) <= var2) {
         return var3;
      } else {
         String var4 = "...";

         int var5;
         for(var5 = var3.length(); var5 > 0; --var5) {
            String var10001 = var3.substring(0, var5);
            if (var0.taggedWidth(var10001 + var4) <= var2) {
               break;
            }
         }

         return var5 <= 0 ? var4 : var3.substring(0, var5).trim() + var4;
      }
   }

   private static void drawMarqueeTinyBattleText(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, Color var6, int var7) {
      String var8 = TextBox.decodeMojibake(var2);
      int var9 = var1.taggedWidth(var8);
      int var10 = 0;
      if (var9 > var5) {
         int var11 = var9 + var5;
         var10 = (Math.max(0, var7) + var5 / 2) % Math.max(1, var11) - var5;
      }

      Shape var12 = var0.getClip();
      var0.clipRect(var3, var4 - 1, var5, 18);
      var1.drawTaggedLine(var0, var8, var3 - var10, var4, TextBox.visibleLength(var8), var6.getRGB() & 16777215);
      var0.setClip(var12);
   }

   private static void drawOutlinedTinyBattleText(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, Color var6, Color var7) {
      drawTinyBattleText(var0, var1, var2, var3 - 1, var4, var5, var7);
      drawTinyBattleText(var0, var1, var2, var3 + 1, var4, var5, var7);
      drawTinyBattleText(var0, var1, var2, var3, var4 - 1, var5, var7);
      drawTinyBattleText(var0, var1, var2, var3, var4 + 1, var5, var7);
      drawTinyBattleText(var0, var1, var2, var3, var4, var5, var6);
   }

   private static int idleCursor(int var0, int var1, int var2) {
      if (var0 < 0) {
         return 0;
      } else {
         SpriteAnimator var3 = SpriteAnimator.load(var0);
         var3.setState(Math.max(0, var1));
         return var3.cursorAtElapsedTick(var2);
      }
   }

   private static int baseCursor(int var0, int var1, int var2, int var3) {
      return var2 >= 0 ? var2 : idleCursor(var0, var1, var3);
   }

   private static void drawSourceHpBar(Graphics2D var0, int var1, int var2, int var3, int var4) {
      int var5 = Math.max(0, Math.min(var3 - 2, var4 * (var3 - 2) / 100));
      var0.setColor(new Color(5723991));
      var0.fillRect(var1, var2, var3, 8);
      var0.setColor(new Color(926000));
      var0.drawRect(var1, var2, var3 - 1, 7);
      var0.setColor(new Color(5894472));
      if (var5 > 0) {
         var0.fillRect(var1 + 1, var2 + 1, var5, 6);
      }

      var0.setColor(new Color(16777215));
      var0.drawLine(var1 + 1, var2 + 1, var1 + var3 - 2, var2 + 1);
   }

   private static int hpPercent(int var0, int var1) {
      return Math.max(0, Math.min(100, var0 * 100 / Math.max(1, var1)));
   }

   private static void drawBattleHpTrack(Graphics2D var0, int var1, int var2, int var3, int var4, int var5) {
      int var6 = Math.max(1, var5);
      int var7 = Math.max(0, Math.min(var3 - 2, var4 * (var3 - 2) / var6));
      var0.setColor(new Color(6579300));
      var0.fillRect(var1, var2, var3, 6);
      var0.setColor(Color.BLACK);
      var0.drawRect(var1, var2, var3, 6);
      var0.setColor(new Color(5817672));
      var0.fillRect(var1 + 1, var2 + 1, var7, 5);
   }

   private static void drawBattleHpBar(Graphics2D var0, int var1, int var2, int var3, int var4, int var5) {
      int var6 = Math.max(1, var4);
      int var7 = Math.max(0, Math.min(96, var3 * 96 / var6));
      var0.setColor(Color.WHITE);
      var0.drawRect(var1, var2, 100, 8);
      var0.setColor(new Color(var5));
      var0.fillRect(var1 + 2, var2 + 2, var7, 5);
   }

   private static final class P7SpecialCell {
      final BufferedImage image;
      final int x;
      final int y;

      P7SpecialCell(BufferedImage var1, int var2, int var3) {
         this.image = var1;
         this.x = var2;
         this.y = var3;
      }
   }
}
