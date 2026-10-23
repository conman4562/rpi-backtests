package edu.rpi.backtests.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class CloudflareR2Config {

  @Bean
  public S3Client r2Client(
      @Value("${r2.access-key}") String accessKey,
      @Value("${r2.secret-access-key}") String secretAccessKey,
      @Value("${r2.endpoint}") String endpoint) {
    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretAccessKey);
    S3Configuration serviceConfiguration = S3Configuration.builder()
        .pathStyleAccessEnabled(true)
        .chunkedEncodingEnabled(false)
        .build();

    return S3Client.builder()
        .endpointOverride(URI.create(endpoint))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .region(Region.of("auto")) // Required by SDK but not used by R2
        .serviceConfiguration(serviceConfiguration)
        .build();
  }

  @Bean
  public S3Presigner r2Presigner(
      @Value("${r2.access-key}") String accessKey,
      @Value("${r2.secret-access-key}") String secretAccessKey,
      @Value("${r2.endpoint}") String endpoint) {
    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretAccessKey);
    return S3Presigner.builder()
        .endpointOverride(URI.create(endpoint))
        .region(Region.of("auto")) // Required by SDK but not used by R2
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();
  }
}
