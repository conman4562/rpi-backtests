package edu.rpi.backtests.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.rpi.backtests.repositories.CourseRepository;
import edu.rpi.backtests.models.Course;

@Service
public class CourseService {
  private final CourseRepository courses;
  private final Logger logger = LoggerFactory.getLogger(CourseService.class);

  public CourseService(CourseRepository courses) {
    this.courses = courses;
  }

  public List<Course> getCourses(String query) {
    return courses.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(query, query);
  }

  public Course uploadCourse(String code, String name) {
    if (courses.findByCode(code).isPresent()) {
      logger.error("Course with code {} already exists", code);
      throw new IllegalArgumentException("Course with code " + code + " already exists");
    }
    Course course = courses.save(new Course(code, name));
    logger.info("Created new course: {}", course.getId());
    return course;
  }
}
