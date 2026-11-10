package edu.rpi.backtests.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.rpi.backtests.dto.BacktestResponseObject;
import edu.rpi.backtests.services.AdminService;
import edu.rpi.backtests.services.BackTestService;
import edu.rpi.backtests.models.BackTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class AdminController {
  private AdminService adminService;
  private BackTestService backTestService;

  public AdminController(AdminService adminService, BackTestService backTestService) {
    this.adminService = adminService;
    this.backTestService = backTestService;
  }

  @GetMapping("/backtests/pending")
  public ResponseEntity<List<BacktestResponseObject>> getPendingBacktests() {
    List<BacktestResponseObject> responseObjects = adminService.getPending().stream()
        .map(BacktestResponseObject::convertBacktestObjectToBacktestResponseObject).collect(Collectors.toList());
    return ResponseEntity.ok(responseObjects);
  }

  @PostMapping("/backtests/{id}/approve")
  public ResponseEntity<BacktestResponseObject> postApproval(@PathVariable UUID id) {
    BackTest approved = adminService.approve(id);
    return ResponseEntity.ok(BacktestResponseObject.convertBacktestObjectToBacktestResponseObject(approved));
  }

  @PostMapping("/backtests/{id}/reject")
  public ResponseEntity<BacktestResponseObject> postRejection(@PathVariable UUID id) {
    BackTest rejected = adminService.reject(id);
    return ResponseEntity.ok(BacktestResponseObject.convertBacktestObjectToBacktestResponseObject(rejected));
  }

  // This method is used to seed the database with backtests
  // The file name carries the course, professor, term, test type and state; see BackTestService.adminUpload
  @PostMapping(value = "/backtests/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BacktestResponseObject> upload(
      @RequestPart("file") MultipartFile file,
      @RequestParam(defaultValue = "anonymous") String uploader,
      @RequestParam(defaultValue = "false") Boolean anonymous) throws IOException {
    BackTest backTest = backTestService.adminUpload(file, uploader, anonymous);
    BacktestResponseObject created = BacktestResponseObject.convertBacktestObjectToBacktestResponseObject(backTest);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }
  
  // Thrown by BackTestService when an uploaded file name doesn't match the expected format
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
  }
}
