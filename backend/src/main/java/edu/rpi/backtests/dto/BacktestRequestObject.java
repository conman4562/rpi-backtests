package edu.rpi.backtests.dto;

public record BacktestRequestObject(
    String courseCode,
    String professorName,
    String semester,
    String testType,
    String fileType,
    byte[] fileData) {
  public BacktestRequestObject {
    if (fileData == null || fileData.length == 0) {
      throw new IllegalArgumentException("File data cannot be null or empty");
    }
  }
}