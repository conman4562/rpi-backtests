package edu.rpi.backtests.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import edu.rpi.backtests.models.BackTest;
import edu.rpi.backtests.models.Semester;
import edu.rpi.backtests.models.TestState;

// What the frontend sees for a backtest; the entity itself is never sent as JSON
public record BacktestResponseObject(UUID id, String courseCode, Semester semester, Integer year, String professor,
    String testType, TestState state, String uploader, OffsetDateTime createdAt) {
  public BacktestResponseObject {
    if (createdAt == null || createdAt.isAfter(OffsetDateTime.now())) {
      throw new IllegalArgumentException("Created at cannot be in the future");
    }
  }
  public static BacktestResponseObject convertBacktestObjectToBacktestResponseObject(BackTest test) {
    String uploader = test.getAnonymous() ? "anonymous" : test.getUploader();
    return new BacktestResponseObject(test.getId(), test.getCourse().getCode(), test.getSemester(), test.getYear(),
        test.getProfessor(), test.getTestType(), test.getState(), uploader, test.getCreatedAt());
  }
}
