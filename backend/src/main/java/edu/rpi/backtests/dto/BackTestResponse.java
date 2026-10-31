package edu.rpi.backtests.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import edu.rpi.backtests.model.BackTest;
import edu.rpi.backtests.model.Semester;
import edu.rpi.backtests.model.TestState;

// What the frontend sees for a backtest; the entity itself is never sent as JSON
public record BackTestResponse(UUID id, String courseCode, Semester semester, Integer year, String professor,
    String testType, TestState state, String uploader, OffsetDateTime createdAt) {

  public static BackTestResponse from(BackTest test) {
    String uploader = test.getAnonymous() ? "anonymous" : test.getUploader();
    return new BackTestResponse(test.getId(), test.getCourse().getCode(), test.getSemester(), test.getYear(),
        test.getProfessor(), test.getTestType(), test.getState(), uploader, test.getCreatedAt());
  }
}
