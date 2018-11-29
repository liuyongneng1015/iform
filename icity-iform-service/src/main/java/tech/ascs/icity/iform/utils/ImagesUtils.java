package tech.ascs.icity.iform.utils;

import java.io.*;
import java.util.Base64;

public class ImagesUtils {
    public static boolean GenerateImage(String base64Str, String imgFilePath) {// 对字节数组字符串进行Base64解码并生成图片
        try { // Base64解码
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] bytes = decoder.decode(base64Str);
            for (int i = 0; i < bytes.length; ++i) {
                if (bytes[i] < 0)
                    bytes[i] += 256;
            } // 生成图片
            OutputStream out = new FileOutputStream(imgFilePath);
            out.write(bytes);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 对base64字符串进行Base64解码
    public static InputStream base64ToInputStream(String base64Str) throws IOException {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] bytes = decoder.decode(base64Str);
        for (int i = 0; i < bytes.length; ++i) {
            if (bytes[i] < 0)
                bytes[i] += 256;
        }
        return new ByteArrayInputStream(bytes);
    }

    public static String getImgType(String base64Header){
        String header = base64Header.toLowerCase();
        if(header.contains("data:image/png;base64")) {
            return "png";
        } else if(header.contains("data:image/jpg;base64")
                ||header.contains("data:image/jpeg;base64")) {
            return "jpg";
        }else if(header.contains("data:image/x-icon;base64")) {
            return "icon";
        } else {
            return null;
        }
    }
}
