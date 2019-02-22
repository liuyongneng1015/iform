package tech.ascs.icity.iform.utils;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
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

    @Value("${minio.logoUrl}")
    private String logoUrl;

    @Value("${minio.backUrl}")
    private String backUrl;

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

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getBackUrl() {
        return backUrl;
    }

    public void setBackUrl(String backUrl) {
        this.backUrl = backUrl;
    }

    @Bean
    public MinioClient getMinioClient() {
        try {
            MinioClient minioClient = new MinioClient(host, accessKey, secretKey);
            if(!minioClient.bucketExists(bucket)){
                minioClient.makeBucket(bucket);
            }

            StringBuilder builder = new StringBuilder();
            builder.append("{\n");
            builder.append("    \"Statement\": [\n");
            builder.append("        {\n");
            builder.append("            \"Action\": [\n");
            builder.append("                \"s3:GetBucketLocation\",\n");
            builder.append("                \"s3:ListBucket\"\n");
            builder.append("            ],\n");
            builder.append("            \"Effect\": \"Allow\",\n");
            builder.append("            \"Principal\": \"*\",\n");
            builder.append("            \"Resource\": \"arn:aws:s3:::"+bucket+"\"\n");
            builder.append("        },\n");
            builder.append("        {\n");
            builder.append("            \"Action\": \"s3:GetObject\",\n");
            builder.append("            \"Effect\": \"Allow\",\n");
            builder.append("            \"Principal\": \"*\",\n");
            builder.append("            \"Resource\": \"arn:aws:s3:::"+bucket+"/*\"\n");
            builder.append("        }\n");
            builder.append("    ],\n");
            builder.append("    \"Version\": \"2012-10-17\"\n");
            builder.append("}\n");
            minioClient.setBucketPolicy(bucket, builder.toString());
            return minioClient;
        } catch (Exception e) {
            e.printStackTrace();
        }
       return null;
    }

}