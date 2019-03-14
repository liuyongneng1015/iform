package tech.ascs.icity.iform.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.UUID;

import javax.imageio.ImageIO;


/**
 * @Title: 合同二维码背景图片.java
 */

public class MergedQrCodeImages {


        public static InputStream mergeImage(InputStream backFile, InputStream qrCodeFile, String x, String y) throws IOException {

            if(backFile == null){
                return qrCodeFile;
            }

            File file = null;
            try {
                file = new File(UUID.randomUUID()+"_back.png");
                writeToLocal(file.getAbsolutePath(), backFile);

                BufferedImage small =ImageIO.read(qrCodeFile);;
                BufferedImage big = ImageIO.read(file);

                Graphics2D g = big.createGraphics();

                float fx = Float.parseFloat(x);
                float fy = Float.parseFloat(y);
                int x_i = (int) fx;
                int y_i = (int) fy;
                g.drawImage(small, x_i, y_i, small.getWidth(), small.getHeight(), null);
                g.dispose();
                ImageIO.write(big, "png", file);
                return new ByteArrayInputStream(cloneInputStream(new FileInputStream(file)).toByteArray());
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if(file != null && file.exists()){
                    file.delete();
                }
            }
            return null;
        }
    public static void test(String[] args) throws Exception {
        try {
            MergedQrCodeImages.mergeImage(new FileInputStream(new File("E:/lyn/qrcode/1.jpg")) ,new FileInputStream(new File("E:/lyn/qrcode/testQrCode.png")), "63", "163");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //写在本地
    public static void writeToLocal(String path, InputStream input)
            throws IOException {
        int index;
        byte[] bytes = new byte[2048];
        FileOutputStream downloadFile = new FileOutputStream(path);
        while ((index = input.read(bytes)) != -1) {
            downloadFile.write(bytes, 0, index);
            downloadFile.flush();
        }
        input.close();
        downloadFile.close();
    }

    //克隆文件流
    public static ByteArrayOutputStream cloneInputStream(InputStream input) throws  Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int len;
            while ((len = input.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return baos;
    }

}




