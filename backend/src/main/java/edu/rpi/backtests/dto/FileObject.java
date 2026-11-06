package edu.rpi.backtests.dto;

import java.time.LocalDateTime;

public record FileObject(String fileName, String fileType, String downloadUrl, LocalDateTime uploadDate) {
  public FileObject {
    if (uploadDate == null || uploadDate.isAfter(LocalDateTime.now())) {
      throw new IllegalArgumentException("Upload date cannot be in the future");
    }
  }
}
