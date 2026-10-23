package edu.rpi.backtests.controllers;

import java.util.List;

import org.springframework.http.MediaType;
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

  @GetMapping("/files")
  public ResponseEntity<List<FileObject>> getFiles(@RequestParam String name) {
    List<FileObject> files = fileService.getFilesByName(name);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(files);
  }
}