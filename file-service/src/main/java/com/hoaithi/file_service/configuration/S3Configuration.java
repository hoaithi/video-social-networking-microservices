package com.hoaithi.file_service.configuration;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
@Slf4j
@Configuration
public class S3Configuration {

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        log.info("=== Initializing S3 Client ===");
        log.info("Region: {}", region);
        log.info("Access Key: {}****", accessKey.substring(0, Math.min(4, accessKey.length())));

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        S3Client client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();

        log.info("=== S3 Client Initialized Successfully ===");

        return client;
    }
}
