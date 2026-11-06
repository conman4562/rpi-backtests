package edu.rpi.backtests.services;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.NoSuchElementException;

import edu.rpi.backtests.models.BackTest;
import edu.rpi.backtests.repositories.BackTestRepository;
import edu.rpi.backtests.storage.CloudflareR2Client;

@Service
public class AdminService {
   
   private final BackTestRepository backTests;
   private final CloudflareR2Client r2;

   public AdminService(BackTestRepository backTests, CloudflareR2Client r2){
      this.backTests = backTests;
      this.r2 = r2;
   }

   public List<BackTest> getPending(){
      return backTests.findByApprovedFalse();
   }
   
   @Transactional
   public BackTest approve(UUID id){
      BackTest test = backTests.findById(id)
         .orElseThrow(() -> new NoSuchElementException("No backtest with id: " + id));
      test.approve();
      return test;
   }

   @Transactional 
   public BackTest reject(UUID id){
      BackTest test = backTests.findById(id)
         .orElseThrow(() -> new NoSuchElementException("No backtest with id: " + id));
      backTests.delete(test);
      backTests.flush();
      r2.deleteObject(test.getFileKey());
      return test;
   }
}
