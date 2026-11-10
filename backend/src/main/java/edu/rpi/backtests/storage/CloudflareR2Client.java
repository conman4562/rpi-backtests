package edu.rpi.backtests.storage;

import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
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

  // debugging functions, not used by endpoints
  public List<S3Object> listAllObjects() {
    try {
      ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).build();
      return s3Client.listObjectsV2(request).contents();
    } catch (Exception e) {
      String errorMessage = String.format("Error occured listing objects from R2 bucket %s", bucketName);
      throw new RuntimeException(errorMessage, e);
    }
  }

  // debugging functions, not used by endpoints
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

  public S3Object getObject(String key) {
    try {
      HeadObjectRequest request = HeadObjectRequest.builder().bucket(bucketName).key(key).build();
      HeadObjectResponse headObject = s3Client.headObject(request);
      return S3Object.builder().key(key).size(headObject.contentLength()).lastModified(headObject.lastModified()).eTag(headObject.eTag()).build();
    } catch (Exception e) {
      String errorMessage = String.format("Error occured getting object %s from R2 bucket %s", key, bucketName);
      throw new RuntimeException(errorMessage, e);
    }
  }

  public void putObject(String fileName, String key, String fileType, byte[] data) {
    try {
      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .contentType(fileType)
          .build();

      s3Client.putObject(request, RequestBody.fromBytes(data));
    } catch (Exception e) {
      String errorMessage = String.format("Error occured putting object %s in R2 bucket %s with key %s", fileName,
          bucketName, key);
      throw new RuntimeException(errorMessage, e);
    }
  }

  public void deleteObject(String key){
   try{
      DeleteObjectRequest request = DeleteObjectRequest.builder()
         .bucket(bucketName)
         .key(key)
         .build();
      
      s3Client.deleteObject(request);
   } catch(Exception e){
      String errorMessage = String.format("Error occured deleting object %s from R2 bucket %s", key, bucketName);
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
