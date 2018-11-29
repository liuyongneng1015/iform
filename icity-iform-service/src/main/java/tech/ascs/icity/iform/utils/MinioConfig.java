package tech.ascs.icity.iform.utils;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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

     /*@Bean
    public MinioClient getMinioClient() throws Exception {
       MinioClient minioClient = new MinioClient(host, accessKey, secretKey);
        if(!minioClient.bucketExists(bucket)){
            minioClient.makeBucket(bucket);
        }
        domian = url+"/"+bucket;
        return null;
    }*/
}