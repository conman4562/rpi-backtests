package edu.rpi.backtests.dto;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import edu.rpi.backtests.models.BackTest;
import edu.rpi.backtests.models.Semester;
import edu.rpi.backtests.models.TestState;

// What the frontend sees for a backtest; the entity itself is never sent as JSON
public record BacktestResponseObject(UUID id, String courseCode, Semester semester, Integer year, String professor,
    String testType, TestState state, String uploader, OffsetDateTime createdAt) {
  // created_at comes from the database clock, which can run ahead of this server's clock,
  // so a small lead isn't treated as being in the future
  private static final Duration ALLOWED_CLOCK_SKEW = Duration.ofMinutes(1);

  public BacktestResponseObject {
    if (createdAt == null) {
      throw new IllegalArgumentException("Created at is missing");
    }
    if (createdAt.isAfter(OffsetDateTime.now().plus(ALLOWED_CLOCK_SKEW))) {
      throw new IllegalArgumentException("Created at cannot be in the future");
    }
  }
  public static BacktestResponseObject convertBacktestObjectToBacktestResponseObject(BackTest test) {
    String uploader = test.getAnonymous() ? "anonymous" : test.getUploader();
    return new BacktestResponseObject(test.getId(), test.getCourse().getCode(), test.getSemester(), test.getYear(),
        test.getProfessor(), test.getTestType(), test.getState(), uploader, test.getCreatedAt());
  }
}
