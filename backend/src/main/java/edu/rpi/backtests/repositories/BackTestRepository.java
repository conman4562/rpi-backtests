package edu.rpi.backtests.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.rpi.backtests.models.BackTest;

public interface BackTestRepository extends JpaRepository<BackTest, UUID> {
  List<BackTest> findByCourseId(UUID courseId);
  List<BackTest> findByCourseCode(String courseCode);
  Optional<BackTest> findByFileKey(String fileKey);
}
