package com.mediwise.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsConfig {

    @Value("${application.aws.access-key}")
    private String accessKey;

    @Value("${application.aws.secret-key}")
    private String secretKey;

    @Value("${application.aws.region}")
    private String region;

    @Bean
    public AmazonS3 amazonS3() {
        if (accessKey.isEmpty()) {
            // Local dev — use the configured region without requiring AWS metadata
            return AmazonS3ClientBuilder.standard()
                    .withRegion(region)
                    .build();
        }
        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(accessKey, secretKey)))
                .build();
    }
}
