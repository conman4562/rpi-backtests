package edu.rpi.backtests.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.rpi.backtests.model.Course;

public interface CourseRepository extends JpaRepository<Course, UUID> {
   List<Course> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(String code, String name);
   Optional<Course> findByCode(String code);
}
