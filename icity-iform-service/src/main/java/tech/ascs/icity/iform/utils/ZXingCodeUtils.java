package tech.ascs.icity.iform.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.ascs.icity.iform.controller.DataModelController;

/**
 * 画制定logo和制定描述的二维码
 *
 *
 */
public class ZXingCodeUtils {
    private static final int QRCOLOR = 0xFF000000; // 默认是黑色
    private static final int BGWHITE = 0xFFFFFFFF; // 背景颜色

    private static final int WIDTH = 400; // 二维码宽
    private static final int HEIGHT = 400; // 二维码高
    private static final Logger log = LoggerFactory.getLogger(ZXingCodeUtils.class);

    // 用于设置QR二维码参数
    private static Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>() {
        {
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);// 设置QR二维码的纠错级别（H为最高级别）具体级别信息
            put(EncodeHintType.CHARACTER_SET, "utf-8");// 设置编码方式
            put(EncodeHintType.MARGIN, 2);
        }
    };

        public static void main(String[] args) throws Exception {
            File logoFile = new File("E:/lyn/qrcode/back.jpg");
            File QrCodeFile = new File("E:/lyn/qrcode/lyn.jpg");
            String url = "http://www.baidu.com";
            String note = "访问百度连接123ssz";
            ZXingCodeUtils zXingCode=new ZXingCodeUtils();
            zXingCode.drawLogoQRCode(new FileInputStream(logoFile), QrCodeFile, url, note);
        }

        // 生成带logo的二维码图片
        public static BufferedImage drawLogoQRCode(InputStream inputStream, File codeFile, String qrUrl, String note) {
            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            try {
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                // 参数顺序分别为：编码内容，编码类型，生成图片宽度，生成图片高度，设置参数
                BitMatrix bm = multiFormatWriter.encode(qrUrl, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);

                // 开始利用二维码数据创建Bitmap图片，分别设为黑（0xFFFFFFFF）白（0xFF000000）两色
                for (int x = 0; x < WIDTH; x++) {
                    for (int y = 0; y < HEIGHT; y++) {
                        image.setRGB(x, y, bm.get(x, y) ? QRCOLOR : BGWHITE);
                    }
                }

                int width = image.getWidth();
                int height = image.getHeight();
                if (inputStream != null) {
                    // 构建绘图对象
                    Graphics2D g = image.createGraphics();
                    // 读取Logo图片
                    BufferedImage logo = ImageIO.read(inputStream);
                    // 开始绘制logo图片
                    g.drawImage(logo, width * 2 / 5, height * 2 / 5, width * 2 / 10, height * 2 / 10, null);
                    g.dispose();
                    logo.flush();
                }

                // 自定义文本描述
                if (StringUtils.isNotEmpty(note)) {
                    System.out.println(note+"____"+new String(note.getBytes("UTF-8"),"UTF-8"));
                    log.error(note+"____"+new String(note.getBytes("UTF-8"),"UTF-8"));
                    // 新的图片，把带logo的二维码下面加上文字
                    BufferedImage outImage = new BufferedImage(400, 445, BufferedImage.TYPE_4BYTE_ABGR);
                    Graphics2D outg = outImage.createGraphics();
                    // 画二维码到新的面板
                    outg.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
                    // 画文字到新的面板
                    outg.setColor(Color.BLACK);
                    outg.setFont(new Font("宋体", Font.BOLD, 24)); // 字体、字型、字号
                    int strWidth = outg.getFontMetrics().stringWidth(note);
                    if (strWidth > 399) {
                        // //长度过长就截取前面部分
                        // 长度过长就换行
                        String note1 = note.substring(0, note.length() / 2);
                        String note2 = note.substring(note.length() / 2, note.length());
                        int strWidth1 = outg.getFontMetrics().stringWidth(note1);
                        int strWidth2 = outg.getFontMetrics().stringWidth(note2);
                        outg.drawString(new String(note1.getBytes("UTF-8"),"UTF-8"), 200 - strWidth1 / 2, height + (outImage.getHeight() - height) / 2 + 12);
                        BufferedImage outImage2 = new BufferedImage(400, 485, BufferedImage.TYPE_4BYTE_ABGR);
                        Graphics2D outg2 = outImage2.createGraphics();
                        outg2.drawImage(outImage, 0, 0, outImage.getWidth(), outImage.getHeight(), null);
                        outg2.setColor(Color.BLACK);
                        outg2.setFont(new Font("宋体", Font.BOLD, 24)); // 字体、字型、字号
                        outg2.drawString(new String(note2.getBytes("UTF-8"),"UTF-8"), 200 - strWidth2 / 2,outImage.getHeight() + (outImage2.getHeight() - outImage.getHeight()) / 2 + 5);
                        outg2.dispose();
                        outImage2.flush();
                        outImage = outImage2;
                    } else {
                        outg.drawString(new String(note.getBytes("UTF-8"),"UTF-8") , 200 - strWidth / 2, height + (outImage.getHeight() - height) / 2 + 12); // 画文字
                    }
                    outg.dispose();
                    outImage.flush();
                    image = outImage;
                }

                image.flush();

                ImageIO.write(image, "png", codeFile); // TODO
            } catch (Exception e) {
                e.printStackTrace();
            }
            return image;
        }


    // 创建带logo的二维码图片
    public static InputStream createLogoQRCode(URL logoFile, String url, String note) {
        File file = new File(System.currentTimeMillis()+"_"+ (new Random().nextInt(10000))+"_qrCode.png");
        try {
            boolean logoFlag = logoFile == null || StringUtils.isBlank(logoFile.getFile()) ? true : false ;
            if(logoFlag && StringUtils.isBlank(note)){
                QRCodeGenerator.generateQRCodeImage( url, 400, 400, file.getAbsolutePath());
                return getInputStream(file);
            }else {
                drawLogoQRCode(logoFlag ? null : logoFile.openStream(), file, url, note);
                return getInputStream(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(file != null && file.exists()){
                file.delete();
            }
        }
        return null;
    }

    private static InputStream getInputStream(File file) throws  Exception{
        FileInputStream fileInputStream = new FileInputStream(file);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(MergedQrCodeImages.cloneInputStream(fileInputStream).toByteArray());
        fileInputStream.close();
        return byteArrayInputStream;
    }

}