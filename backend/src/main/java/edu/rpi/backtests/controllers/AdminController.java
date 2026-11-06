package edu.rpi.backtests.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.rpi.backtests.dto.BacktestResponseObject;
import edu.rpi.backtests.services.AdminService;

import edu.rpi.backtests.models.BackTest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController 
@RequestMapping ("/admin")
public class AdminController {
   private AdminService adminService;
   public AdminController(AdminService adminService){
      this.adminService = adminService;
   }

   @GetMapping ("/backtests/pending")
   public ResponseEntity<List<BacktestResponseObject>> getPendingBacktests(){
      List<BacktestResponseObject> responseObjects = adminService.getPending().stream().map(BacktestResponseObject::convertBacktestObjectToBacktestResponseObject).collect(Collectors.toList());
      return ResponseEntity.ok(responseObjects);
   }

   @PostMapping ("/backtests/{id}/approve")
   public ResponseEntity<BacktestResponseObject> postApproval(@PathVariable UUID id){
      BackTest approved = adminService.approve(id);
      return ResponseEntity.ok(BacktestResponseObject.convertBacktestObjectToBacktestResponseObject(approved));
   }

   @PostMapping ("/backtests/{id}/reject")
   public ResponseEntity<BacktestResponseObject> postRejection(@PathVariable UUID id){
      BackTest rejected = adminService.reject(id);
      return ResponseEntity.ok(BacktestResponseObject.convertBacktestObjectToBacktestResponseObject(rejected));
   }
}
