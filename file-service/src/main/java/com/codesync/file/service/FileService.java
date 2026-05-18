package com.codesync.file.service;

import com.codesync.file.dto.*;
import com.codesync.file.entity.CodeFile;

import java.util.List;

public interface FileService {

    CodeFile createFile(CreateFileRequest request);

    CodeFile getFileById(Long fileId);

    List<CodeFile> getFilesByProject(Long projectId);

    String getFileContent(Long fileId);

    CodeFile updateFileContent(Long fileId, UpdateContentRequest request);

    CodeFile renameFile(Long fileId, String newName);

    void deleteFile(Long fileId);

    void restoreFile(Long fileId);

    CodeFile moveFile(Long fileId, String newPath);

    CodeFile createFolder(CreateFolderRequest request);

    List<FileTreeNode> getFileTree(Long projectId);

    List<CodeFile> getChildren(Long projectId, String parentPath);

    List<CodeFile> searchInProject(Long projectId, String keyword);

    void copyProjectFiles(Long sourceProjectId, Long targetProjectId);
}
