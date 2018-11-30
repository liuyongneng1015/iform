package tech.ascs.icity.iform.utils;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;

@Controller
public class MinioConfig {
    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.access.key}")
    private String accessKey;

    @Value("${minio.secret.key}")
    private String secretKey;

    @Value("${minio.host}")
    private String host;

    @Value("${minio.url}")
    private String url;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

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

    //@Bean
    public MinioClient getMinioClient() {
        try {
            MinioClient minioClient = new MinioClient(host, accessKey, secretKey);
            if(!minioClient.bucketExists(bucket)){
                minioClient.makeBucket(bucket);
            }
            minioClient.setBucketPolicy(bucket,"*");
            return minioClient;
        } catch (Exception e) {
            e.printStackTrace();
        }
       return null;
    }

}