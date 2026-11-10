package edu.rpi.backtests.services;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.rpi.backtests.repositories.CourseRepository;
import edu.rpi.backtests.models.Course;

@Service
public class CourseService {
  private final CourseRepository courses;

  public CourseService(CourseRepository courses) {
    this.courses = courses;
  }

  public List<Course> getCourses(String query) {
    return courses.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(query, query);
  }

  public Course uploadCourse(String code, String name) {
    if (courses.findByCode(code).isPresent()) {
      throw new IllegalArgumentException("Course with code " + code + " already exists");
    }
    return courses.save(new Course(code, name));
  }
}
