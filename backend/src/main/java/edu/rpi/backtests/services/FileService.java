package edu.rpi.backtests.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import edu.rpi.backtests.dto.FileObject;
import edu.rpi.backtests.storage.CloudflareR2Client;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class FileService {

  private final CloudflareR2Client cloudflareR2Client;

  public FileService(CloudflareR2Client cloudflareR2Client) {
    this.cloudflareR2Client = cloudflareR2Client;
  }

  // debugging function, not used by endpoints
  public List<FileObject> getFilesByName(String substring) {
    List<S3Object> s3Objects = cloudflareR2Client.listObjectsBasedOnPrefix(substring);
    return s3Objects.stream()
        .map(this::convertS3ObjectToFileObject) // method reference similar to passing a lambda function
        .collect(Collectors.toList());
  }

  public FileObject getFile(String key) {
    return convertS3ObjectToFileObject(cloudflareR2Client.getObject(key));
  }

  public FileObject convertS3ObjectToFileObject(S3Object s3Object) {
    String fileName = s3Object.key();
    String contentType = cloudflareR2Client.getContentType(fileName);
    String downloadUrl = cloudflareR2Client.getDownloadUrl(fileName);
    LocalDateTime uploadDate = s3Object.lastModified().atZone(ZoneId.systemDefault()).toLocalDateTime();
    return new FileObject(fileName, contentType, downloadUrl, uploadDate);
  }
}
