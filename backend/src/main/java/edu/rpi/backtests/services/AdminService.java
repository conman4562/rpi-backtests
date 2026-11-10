package edu.rpi.backtests.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

import edu.rpi.backtests.models.BackTest;
import edu.rpi.backtests.repositories.BackTestRepository;
import edu.rpi.backtests.storage.CloudflareR2Client;

@Service
public class AdminService {

  private final BackTestRepository backTests;
  private final CloudflareR2Client r2;
  private final Logger logger = LoggerFactory.getLogger(AdminService.class);

  public AdminService(BackTestRepository backTests, CloudflareR2Client r2) {
    this.backTests = backTests;
    this.r2 = r2;
  }

  public List<BackTest> getPending() {
    return backTests.findByApprovedFalse();
  }

  @Transactional
  public BackTest approve(UUID id) {
    BackTest test = backTests.findById(id)
        .orElseThrow(() -> {
          logger.error("No backtest with id: {}", id);
          return new NoSuchElementException("No backtest with id: " + id);
        });
    test.approve();
    logger.info("Approved backtest with id: {}", id);
    return test;
  }

  @Transactional
  public BackTest reject(UUID id) {
    BackTest test = backTests.findById(id)
        .orElseThrow(() -> {
          logger.error("No backtest with id: {}", id);
          return new NoSuchElementException("No backtest with id: " + id);
        });
    backTests.delete(test);
    backTests.flush();
    logger.info("Rejected backtest with id: {}", id);
    r2.deleteObject(test.getFileKey());
    logger.info("Deleted file from R2 with key: {}", test.getFileKey());
    return test;
  }
}
