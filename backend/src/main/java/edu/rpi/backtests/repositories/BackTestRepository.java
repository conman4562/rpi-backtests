package edu.rpi.backtests.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.rpi.backtests.model.BackTest;

public interface BackTestRepository extends JpaRepository<BackTest, UUID> {
   List<BackTest> findByCourseId(UUID courseId);
   Optional<BackTest> findByFileKey(String fileKey);
}
