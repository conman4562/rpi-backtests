package edu.rpi.backtests.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.rpi.backtests.dto.BacktestResponseObject;
import edu.rpi.backtests.models.BackTest;
import edu.rpi.backtests.services.BackTestService;

@RestController
public class BackTestController {

  private final BackTestService backTestService;

  public BackTestController(BackTestService backTestService) {
    this.backTestService = backTestService;
  }

  @GetMapping("/courses/{courseCode}/backtests")
  public ResponseEntity<List<BacktestResponseObject>> getTestsForCourse(@PathVariable String courseCode) {
    List<BacktestResponseObject> responseObjects = backTestService.getTestsForCourse(courseCode).stream()
        .map(BacktestResponseObject::convertBacktestObjectToBacktestResponseObject)
        .collect(Collectors.toList());
    return ResponseEntity.ok(responseObjects);
  }
  
  @GetMapping("/backtests/files")
  public ResponseEntity<Map<String, String>> getFileDownload(@RequestParam UUID testId) {
    return ResponseEntity.ok(Map.of("url", backTestService.getDownloadUrl(testId)));
  }

  // This method is used to seed the database with backtests
  // The file name carries the course, professor, term and test type; see BackTestService.upload
  @PostMapping(value = "/backtests", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BacktestResponseObject> upload(
      @RequestPart("file") MultipartFile file,
      @RequestParam(defaultValue = "anonymous") String uploader,
      @RequestParam(defaultValue = "false") Boolean anonymous) throws IOException {
    BackTest backTest = backTestService.upload(file, uploader, anonymous);
    BacktestResponseObject created = BacktestResponseObject.convertBacktestObjectToBacktestResponseObject(backTest);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
  }

  // Thrown by BackTestService when an uploaded file name doesn't match the expected format
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
  }
}
