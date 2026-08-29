package vqsv.game;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

final class TanNguyetLongMaWorldDecorationRenderer {
   private static final BufferedImage NAMEPLATE = loadNameplate();

   private TanNguyetLongMaWorldDecorationRenderer() {}

   static void renderShadow(VqsvGameRuntime.Scene scene, Graphics2D graphics, Actor boss) {
      int x = boss.x - scene.session.world.cameraX;
      int y = boss.y - scene.session.world.cameraY;
      Composite oldComposite = graphics.getComposite();
      Color oldColor = graphics.getColor();
      graphics.setComposite(AlphaComposite.SrcOver.derive(0.34F));
      graphics.setColor(Color.BLACK);
      graphics.fillOval(x - 40, y - 10, 80, 20);
      graphics.setComposite(AlphaComposite.SrcOver.derive(0.24F));
      graphics.fillOval(x - 28, y - 7, 56, 14);
      graphics.setComposite(oldComposite);
      graphics.setColor(oldColor);
   }

   static void renderNameplate(VqsvGameRuntime.Scene scene, Graphics2D graphics, Actor boss) {
      int x = boss.x - scene.session.world.cameraX - NAMEPLATE.getWidth() / 2;
      int y = boss.y - scene.session.world.cameraY - 158;
      graphics.drawImage(NAMEPLATE, x, y, null);
   }

   private static BufferedImage loadNameplate() {
      String path = "/vqsv/data/unified/custom-bosses/TAN-NGUYET-LONG-MA/nameplate.png";
      try (InputStream input = TanNguyetLongMaWorldDecorationRenderer.class.getResourceAsStream(path)) {
         if (input == null) throw new IllegalStateException("Missing boss nameplate " + path);
         BufferedImage image = ImageIO.read(input);
         if (image == null || image.getWidth() != 120 || image.getHeight() != 50) {
            throw new IllegalStateException("Invalid boss nameplate dimensions");
         }
         return image;
      } catch (IOException error) {
         throw new IllegalStateException("Cannot load boss nameplate", error);
      }
   }
}
