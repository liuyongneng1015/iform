package tech.ascs.icity.iform.utils;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;

@Controller
public class MinioConfig {
    public static String bucket;
    public static String domian;

    @Value("${minio.bucket}")
    public void setBucket(String bucketName) {
        bucket=bucketName;
    }

    @Value("${minio.access.key}")
    private String accessKey;

    @Value("${minio.secret.key}")
    private String secretKey;

    @Value("${minio.host}")
    private String host;

    @Value("${minio.url}")
    private String url;

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

   // @Bean
    public MinioClient getMinioClient() throws Exception {
       MinioClient minioClient = new MinioClient(host, accessKey, secretKey);
        if(!minioClient.bucketExists(bucket)){
            minioClient.makeBucket(bucket);
        }
        domian = url+"/"+bucket;
        return minioClient;
    }
}