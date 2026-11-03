package edu.rpi.backtests.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.rpi.backtests.dto.CourseRequestObject;
import edu.rpi.backtests.models.Course;
import edu.rpi.backtests.services.CourseService;

@RestController 
public class CourseController {
  private final CourseService courseService;

  public CourseController(CourseService courseService) {
    this.courseService = courseService;
  }

  @GetMapping("/courses")
  public ResponseEntity<List<Course>> getCourses(@RequestParam String query) {
    return ResponseEntity.ok(courseService.getCourses(query));
  }

  @PostMapping("/courses")
  public ResponseEntity<Course> uploadCourse(@RequestBody CourseRequestObject request) {
    return ResponseEntity.ok(courseService.uploadCourse(request.code(), request.name()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
  }
}
