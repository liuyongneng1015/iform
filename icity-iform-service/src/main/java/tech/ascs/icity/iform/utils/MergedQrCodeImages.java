package tech.ascs.icity.iform.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;


/**
 * @Title: 合同二维码背景图片.java
 */

public class MergedQrCodeImages {


        public static void mergeImage(String bigPath, String smallPath, String x, String y) throws IOException {

            try {
                BufferedImage small;
                BufferedImage big = ImageIO.read(new File(bigPath));
                if (smallPath.contains("http")) {

                    URL url = new URL(smallPath);
                    small = ImageIO.read(url);
                } else {
                    small = ImageIO.read(new File(smallPath));
                }

                Graphics2D g = big.createGraphics();

                float fx = Float.parseFloat(x);
                float fy = Float.parseFloat(y);
                int x_i = (int) fx;
                int y_i = (int) fy;
                g.drawImage(small, x_i, y_i, small.getWidth(), small.getHeight(), null);
                g.dispose();
                ImageIO.write(big, "png", new File(bigPath));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    public static void test(String[] args) throws Exception {
        try {
            MergedQrCodeImages.mergeImage("E:/lyn/qrcode/1.jpg", "E:/lyn/qrcode/testQrCode.png", "63", "163");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}




