package com.codesync.file.controller;

import com.codesync.file.dto.*;
import com.codesync.file.entity.CodeFile;
import com.codesync.file.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping
    public ResponseEntity<CodeFile> createFile(@RequestBody CreateFileRequest request) {
        logger.info("API HIT: POST /files - name: {}, projectId: {}", request.getName(), request.getProjectId());
        return ResponseEntity.ok(fileService.createFile(request));
    }

    @PostMapping("/folder")
    public ResponseEntity<CodeFile> createFolder(@RequestBody CreateFolderRequest request) {
        logger.info("API HIT: POST /files/folder - name: {}, projectId: {}", request.getName(), request.getProjectId());
        return ResponseEntity.ok(fileService.createFolder(request));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<CodeFile> getFile(@PathVariable Long fileId) {
        logger.info("API HIT: GET /files/{}", fileId);
        return ResponseEntity.ok(fileService.getFileById(fileId));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<CodeFile>> getFilesByProject(@PathVariable Long projectId) {
        logger.info("API HIT: GET /files/project/{}", projectId);
        return ResponseEntity.ok(fileService.getFilesByProject(projectId));
    }

    @GetMapping("/content/{fileId}")
    public ResponseEntity<Map<String, String>> getFileContent(@PathVariable Long fileId) {
        logger.info("API HIT: GET /files/content/{}", fileId);
        String content = fileService.getFileContent(fileId);
        return ResponseEntity.ok(Map.of("content", content));
    }

    @GetMapping("/tree/{projectId}")
    public ResponseEntity<List<FileTreeNode>> getFileTree(@PathVariable Long projectId) {
        logger.info("API HIT: GET /files/tree/{}", projectId);
        return ResponseEntity.ok(fileService.getFileTree(projectId));
    }

    @GetMapping("/children")
    public ResponseEntity<List<CodeFile>> getChildren(
            @RequestParam Long projectId,
            @RequestParam String parentPath) {
        logger.info("API HIT: GET /files/children?projectId={}&parentPath={}", projectId, parentPath);
        return ResponseEntity.ok(fileService.getChildren(projectId, parentPath));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CodeFile>> searchFiles(
            @RequestParam Long projectId,
            @RequestParam String keyword) {
        logger.info("API HIT: GET /files/search?projectId={}&keyword={}", projectId, keyword);
        return ResponseEntity.ok(fileService.searchInProject(projectId, keyword));
    }

    @PutMapping("/content/{fileId}")
    public ResponseEntity<CodeFile> updateContent(
            @PathVariable Long fileId,
            @RequestBody UpdateContentRequest request) {
        logger.info("API HIT: PUT /files/content/{}", fileId);
        return ResponseEntity.ok(fileService.updateFileContent(fileId, request));
    }

    @PutMapping("/rename/{fileId}")
    public ResponseEntity<CodeFile> renameFile(
            @PathVariable Long fileId,
            @RequestBody Map<String, String> request) {
        logger.info("API HIT: PUT /files/rename/{}", fileId);
        String newName = request.get("name");
        return ResponseEntity.ok(fileService.renameFile(fileId, newName));
    }

    @PutMapping("/move/{fileId}")
    public ResponseEntity<CodeFile> moveFile(
            @PathVariable Long fileId,
            @RequestBody Map<String, String> request) {
        logger.info("API HIT: PUT /files/move/{}", fileId);
        String newPath = request.get("path");
        return ResponseEntity.ok(fileService.moveFile(fileId, newPath));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId) {
        logger.info("API HIT: DELETE /files/{}", fileId);
        fileService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/restore/{fileId}")
    public ResponseEntity<Void> restoreFile(@PathVariable Long fileId) {
        logger.info("API HIT: POST /files/restore/{}", fileId);
        fileService.restoreFile(fileId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/copy")
    public ResponseEntity<Void> copyProjectFiles(
            @RequestParam Long sourceProjectId,
            @RequestParam Long targetProjectId) {
        logger.info("API HIT: POST /files/copy?sourceProjectId={}&targetProjectId={}", sourceProjectId, targetProjectId);
        fileService.copyProjectFiles(sourceProjectId, targetProjectId);
        return ResponseEntity.ok().build();
    }
}