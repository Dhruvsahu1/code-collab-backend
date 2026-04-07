package com.codesync.file.controller;

import com.codesync.file.dto.*;
import com.codesync.file.entity.CodeFile;
import com.codesync.file.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping
    public ResponseEntity<CodeFile> createFile(@RequestBody CreateFileRequest request) {
        return ResponseEntity.ok(fileService.createFile(request));
    }

    @PostMapping("/folder")
    public ResponseEntity<CodeFile> createFolder(@RequestBody CreateFolderRequest request) {
        return ResponseEntity.ok(fileService.createFolder(request));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<CodeFile> getFile(@PathVariable Long fileId) {
        return ResponseEntity.ok(fileService.getFileById(fileId));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<CodeFile>> getFilesByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(fileService.getFilesByProject(projectId));
    }

    @GetMapping("/content/{fileId}")
    public ResponseEntity<Map<String, String>> getFileContent(@PathVariable Long fileId) {
        String content = fileService.getFileContent(fileId);
        return ResponseEntity.ok(Map.of("content", content));
    }

    @GetMapping("/tree/{projectId}")
    public ResponseEntity<List<FileTreeNode>> getFileTree(@PathVariable Long projectId) {
        return ResponseEntity.ok(fileService.getFileTree(projectId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CodeFile>> searchFiles(
            @RequestParam Long projectId,
            @RequestParam String keyword) {
        return ResponseEntity.ok(fileService.searchInProject(projectId, keyword));
    }

    @PutMapping("/content/{fileId}")
    public ResponseEntity<CodeFile> updateContent(
            @PathVariable Long fileId,
            @RequestBody UpdateContentRequest request) {
        return ResponseEntity.ok(fileService.updateFileContent(fileId, request));
    }

    @PutMapping("/rename/{fileId}")
    public ResponseEntity<CodeFile> renameFile(
            @PathVariable Long fileId,
            @RequestBody Map<String, String> request) {
        String newName = request.get("name");
        return ResponseEntity.ok(fileService.renameFile(fileId, newName));
    }

    @PutMapping("/move/{fileId}")
    public ResponseEntity<CodeFile> moveFile(
            @PathVariable Long fileId,
            @RequestBody Map<String, String> request) {
        String newPath = request.get("path");
        return ResponseEntity.ok(fileService.moveFile(fileId, newPath));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId) {
        fileService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/restore/{fileId}")
    public ResponseEntity<Void> restoreFile(@PathVariable Long fileId) {
        fileService.restoreFile(fileId);
        return ResponseEntity.ok().build();
    }
}
