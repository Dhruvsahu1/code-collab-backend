package com.codesync.file.service;

import com.codesync.file.dto.*;
import com.codesync.file.entity.CodeFile;
import com.codesync.file.repository.FileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;

    public FileServiceImpl(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Override
    @Transactional
    public CodeFile createFile(CreateFileRequest request) {
        CodeFile file = new CodeFile();
        file.setProjectId(request.getProjectId());
        file.setName(request.getName());
        file.setPath(request.getPath());
        file.setLanguage(request.getLanguage());
        file.setContent(request.getContent());
        file.setSize(request.getContent() != null ? (long) request.getContent().length() : 0L);
        file.setCreatedById(request.getCreatedById());
        file.setLastEditedBy(request.getCreatedById());
        file.setDeleted(false);

        return fileRepository.save(file);
    }

    @Override
    @Transactional(readOnly = true)
    public CodeFile getFileById(Long fileId) {
        return fileRepository.findByFileId(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found with ID: " + fileId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodeFile> getFilesByProject(Long projectId) {
        return fileRepository.findByProjectIdAndIsDeleted(projectId, false);
    }

    @Override
    @Transactional(readOnly = true)
    public String getFileContent(Long fileId) {
        CodeFile file = getFileById(fileId);
        if (file.isDeleted()) {
            throw new IllegalArgumentException("File is deleted");
        }
        return file.getContent();
    }

    @Override
    @Transactional
    public CodeFile updateFileContent(Long fileId, UpdateContentRequest request) {
        CodeFile file = getFileById(fileId);
        file.setContent(request.getContent());
        file.setSize(request.getContent() != null ? (long) request.getContent().length() : 0L);
        file.setLastEditedBy(request.getUserId());
        return fileRepository.save(file);
    }

    @Override
    @Transactional
    public CodeFile renameFile(Long fileId, String newName) {
        CodeFile file = getFileById(fileId);
        file.setName(newName);
        return fileRepository.save(file);
    }

    @Override
    @Transactional
    public void deleteFile(Long fileId) {
        CodeFile file = getFileById(fileId);
        file.setDeleted(true);
        fileRepository.save(file);
    }

    @Override
    @Transactional
    public void restoreFile(Long fileId) {
        CodeFile file = getFileById(fileId);
        file.setDeleted(false);
        fileRepository.save(file);
    }

    @Override
    @Transactional
    public CodeFile moveFile(Long fileId, String newPath) {
        CodeFile file = getFileById(fileId);
        file.setPath(newPath);
        return fileRepository.save(file);
    }

    @Override
    @Transactional
    public CodeFile createFolder(CreateFolderRequest request) {
        String folderPath = request.getPath();
        if (!folderPath.endsWith("/")) {
            folderPath = folderPath + "/";
        }
        folderPath = folderPath + request.getName() + "/";

        CodeFile folder = new CodeFile();
        folder.setProjectId(request.getProjectId());
        folder.setName(request.getName());
        folder.setPath(folderPath);
        folder.setLanguage(null);
        folder.setContent("");
        folder.setSize(0L);
        folder.setCreatedById(request.getCreatedById());
        folder.setLastEditedBy(request.getCreatedById());
        folder.setDeleted(false);

        return fileRepository.save(folder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileTreeNode> getFileTree(Long projectId) {
        List<CodeFile> files = fileRepository.findByProjectIdAndIsDeleted(projectId, false);
        return buildFileTree(files);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodeFile> searchInProject(Long projectId, String keyword) {
        List<CodeFile> files = fileRepository.findByProjectIdAndIsDeleted(projectId, false);
        return files.stream()
                .filter(file -> {
                    boolean nameMatch = file.getName() != null && 
                                       file.getName().toLowerCase().contains(keyword.toLowerCase());
                    boolean contentMatch = file.getContent() != null && 
                                            file.getContent().toLowerCase().contains(keyword.toLowerCase());
                    return nameMatch || contentMatch;
                })
                .collect(Collectors.toList());
    }

    private List<FileTreeNode> buildFileTree(List<CodeFile> files) {
        Map<String, FileTreeNode> nodeMap = new LinkedHashMap<>();
        List<FileTreeNode> rootNodes = new ArrayList<>();

        for (CodeFile file : files) {
            String path = file.getPath();
            boolean isFolder = file.getContent() == null || file.getContent().isEmpty();
            FileTreeNode node = new FileTreeNode(file.getFileId(), file.getName(), path, isFolder);
            nodeMap.put(path, node);
        }

        for (FileTreeNode node : nodeMap.values()) {
            String path = node.getPath();
            if (path == null || path.isEmpty() || path.equals("/")) {
                rootNodes.add(node);
            } else {
                String parentPath = getParentPath(path);
                FileTreeNode parent = nodeMap.get(parentPath);
                if (parent != null) {
                    parent.addChild(node);
                } else {
                    rootNodes.add(node);
                }
            }
        }
        return rootNodes;
    }

    private String getParentPath(String path) {
        if (path == null || path.isEmpty()) return "";
        String normalizedPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int lastSlash = normalizedPath.lastIndexOf('/');
        if (lastSlash <= 0) return "";
        return normalizedPath.substring(0, lastSlash + 1);
    }
}
