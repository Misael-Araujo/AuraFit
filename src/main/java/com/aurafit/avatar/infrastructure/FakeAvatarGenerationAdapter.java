package com.aurafit.avatar.infrastructure;

import com.aurafit.avatar.application.AvatarGenerationPort;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Component;

@Component
public class FakeAvatarGenerationAdapter implements AvatarGenerationPort {
  @Override
  public byte[] generate(UUID userId, byte[] sourceImage, String contentType) {
    try {
      BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
      Graphics2D graphics = image.createGraphics();
      try {
        graphics.setColor(new java.awt.Color(59, 130, 246));
        graphics.fillRect(0, 0, 256, 256);
        graphics.setColor(new java.awt.Color(255, 255, 255));
        graphics.fillOval(70, 62, 110, 110);
        graphics.fillRect(90, 165, 70, 60);
      } finally {
        graphics.dispose();
      }
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ImageIO.write(image, "png", outputStream);
      return outputStream.toByteArray();
    } catch (Exception exception) {
      throw new IllegalStateException("Avatar generation failed", exception);
    }
  }
}
