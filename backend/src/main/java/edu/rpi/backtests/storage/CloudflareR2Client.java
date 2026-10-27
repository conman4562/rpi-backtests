package edu.rpi.backtests.storage;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Component
public class CloudflareR2Client {

  @Value("${r2.bucket}")
  String bucketName;

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  public CloudflareR2Client(S3Client s3Client, S3Presigner s3Presigner) {
    this.s3Client = s3Client;
    this.s3Presigner = s3Presigner;
  }

  public List<S3Object> listAllObjects() {
    try {
      ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).build();
      return s3Client.listObjectsV2(request).contents();
    } catch (Exception e) {
      String errorMessage = String.format("Error occured listing objects from R2 bucket %s", bucketName);
      throw new RuntimeException(errorMessage, e);
    }
  }

  public List<S3Object> listObjectsBasedOnPrefix(String prefix) {
    try {
      ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix).build();
      return s3Client.listObjectsV2(request).contents();
    } catch (Exception e) {
      String errorMessage = String.format("Error occured listing objects from R2 bucket %s with prefix %s", bucketName,
          prefix);
      throw new RuntimeException(errorMessage, e);
    }
  }

  public void putObject(String fileName, String key, byte[] data) {
    try {
      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .build();

      s3Client.putObject(request, RequestBody.fromBytes(data));
    } catch (Exception e) {
      String errorMessage = String.format("Error occured putting object %s in R2 bucket %s with key %s", fileName,
          bucketName, key);
      throw new RuntimeException(errorMessage, e);
    }
  }

  public String getContentType(String fileName) {
    try {
      HeadObjectRequest request = HeadObjectRequest.builder().bucket(bucketName).key(fileName).build();
      return s3Client.headObject(request).contentType();
    } catch (Exception e) {
      String errorMessage = String.format("Error occured getting content type for file %s", fileName);
      throw new RuntimeException(errorMessage, e);
    }
  }

  public String getDownloadUrl(String fileName) {
    try {
      GetObjectRequest request = GetObjectRequest.builder()
          .bucket(bucketName)
          .responseContentDisposition("attachment")
          .key(fileName)
          .build();

      GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
          .signatureDuration(Duration.ofMinutes(10))
          .getObjectRequest(request)
          .build();
      return s3Presigner.presignGetObject(presignRequest).url().toString();
    } catch (Exception e) {
      String errorMessage = String.format("Error occured getting download URL for file %s", fileName);
      throw new RuntimeException(errorMessage, e);
    }
  }
}
