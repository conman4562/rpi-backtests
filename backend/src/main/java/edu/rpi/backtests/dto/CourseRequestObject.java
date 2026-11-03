package edu.rpi.backtests.dto;

import edu.rpi.backtests.models.Course;

public record CourseRequestObject(String code, String name) {
  public Course convertToCourseObject() {
    return new Course(code, name);
  }
}
