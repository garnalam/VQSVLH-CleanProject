package vqsv.game;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public final class TanNguyetLongMaRoomRenderTest {
   public static void main(String[] args) throws Exception {
      VqsvGameRuntime.Scene scene = new VqsvGameRuntime.Scene();
      VqsvSceneLoaders.loadWorldRoomWithoutMapLoading(scene, 5, 3, 337, 368);
      scene.player.visible = true;
      scene.player.x = 337;
      scene.player.y = 400;
      TanNguyetLongMaBossRuntime.tick(scene);
      BufferedImage image = new BufferedImage(240, 320, BufferedImage.TYPE_INT_ARGB);
      Graphics2D graphics = image.createGraphics();
      scene.render(graphics);
      graphics.dispose();
      File output = new File("build/Tan-Nguyet-Long-Ma-room-proof.png");
      ImageIO.write(image, "png", output);
      if (!scene.tanNguyetLongMaBoss.visible) throw new IllegalStateException("boss hidden in rendered room");
      System.out.println(output.getAbsolutePath());
   }
}
