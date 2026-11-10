package edu.rpi.backtests.dto;

import edu.rpi.backtests.models.Semester;
import edu.rpi.backtests.models.TestState;

public record BacktestRequestObject(
    String courseCode,
    String professor,
    Semester semester,
    Integer year,
    String testType,
    TestState state,
    String fileType,
    String uploader,
    Boolean anonymous
  ) {
  public BacktestRequestObject {
    if (anonymous) {
      uploader = "anonymous";
    }
  }
}