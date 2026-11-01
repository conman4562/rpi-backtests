package edu.rpi.backtests.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.rpi.backtests.dto.FileObject;
import edu.rpi.backtests.services.FileService;

@RestController
public class FileController {

  private final FileService fileService;

  public FileController(FileService fileService) {
    this.fileService = fileService;
  }

  @GetMapping("/backtests/files")
  public ResponseEntity<FileObject> getFileDownload(@RequestParam String name) {
    FileObject file = fileService.getFile(name);
    return ResponseEntity.ok(file);
  }
}