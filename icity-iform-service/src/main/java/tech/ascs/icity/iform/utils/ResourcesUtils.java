package tech.ascs.icity.iform.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class ResourcesUtils {

    /**
     * 读取文本资源,如果文件是以classpath:开头的,则去resources目录下读取,否则读取文件系统内的文件
     * @param path 文件路径
     * @return 字符串
     */
    public static String readFileToString(String path) throws IOException {
        if (path.startsWith("classpath:")) {
            path = path.substring(10);
            Resource resource = new ClassPathResource(path);
            return FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream()));
        } else {
            return FileCopyUtils.copyToString(new FileReader(path));
        }
    }
}