package com.codesync.file.service;

import com.codesync.file.dto.*;
import com.codesync.file.entity.CodeFile;
import com.codesync.file.repository.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
    private final FileRepository fileRepository;

    public FileServiceImpl(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Override
    @Transactional
    public CodeFile createFile(CreateFileRequest request) {
        logger.info("API HIT: createFile - projectId: {}, name: {}, path: {}", request.getProjectId(), request.getName(), request.getPath());
        
        if (request.getProjectId() == null) {
            throw new IllegalArgumentException("Project ID is required");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("File name is required");
        }

        Long createdById = request.getCreatedById();
        if (createdById == null) {
            createdById = 1L;
        }

        String basePath = request.getPath();
        if (basePath == null) basePath = "/";
        basePath = basePath.trim();
        if (basePath.isEmpty()) basePath = "/";
        if (!basePath.endsWith("/")) {
            basePath = basePath + "/";
        }
        
        String filePath = basePath + request.getName();
        logger.info("Creating file with full path: {}, basePath: {}", filePath, basePath);
        
        boolean exists = fileRepository.existsByProjectIdAndPath(request.getProjectId(), filePath);
        if (exists) {
            throw new IllegalArgumentException("File already exists at path: " + filePath);
        }
        
        if (!basePath.equals("/")) {
            logger.info("Checking if parent folder exists at path: {}", basePath);
            boolean parentExists = fileRepository.findByProjectIdAndPathAndIsFolderAndIsDeleted(
                request.getProjectId(), basePath, true, false).isPresent();
            logger.info("Parent folder exists: {}", parentExists);
            if (!parentExists) {
                throw new IllegalArgumentException("Parent folder does not exist: " + basePath);
            }
        }

        CodeFile file = new CodeFile();
        file.setProjectId(request.getProjectId());
        file.setName(request.getName());
        file.setPath(filePath);
        file.setLanguage(request.getLanguage() != null ? request.getLanguage() : inferLanguage(request.getName()));
        file.setContent(request.getContent() != null ? request.getContent() : "");
        file.setSize(request.getContent() != null ? (long) request.getContent().length() : 0L);
        file.setCreatedById(createdById);
        file.setLastEditedBy(createdById);
        file.setDeleted(false);
        file.setFolder(false);

        return fileRepository.save(file);
    }
    
    private String inferLanguage(String fileName) {
        if (fileName == null) return "text";
        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase() : "";
        return switch (ext) {
            case "js", "jsx" -> "javascript";
            case "ts", "tsx" -> "typescript";
            case "py" -> "python";
            case "java" -> "java";
            case "cpp", "cc", "cxx" -> "cpp";
            case "c" -> "c";
            case "go" -> "go";
            case "rs" -> "rust";
            case "html", "htm" -> "html";
            case "css" -> "css";
            case "json" -> "json";
            case "md", "markdown" -> "markdown";
            case "rb" -> "ruby";
            case "php" -> "php";
            case "cs" -> "csharp";
            case "swift" -> "swift";
            case "kt", "kts" -> "kotlin";
            case "xml" -> "xml";
            case "sql" -> "sql";
            case "sh", "bash" -> "bash";
            default -> "text";
        };
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
        String oldName = file.getName();
        String oldPath = file.getPath();
        
        file.setName(newName);
        
        if (file.isFolder()) {
            String prefix = oldPath.endsWith("/") ? oldPath.substring(0, oldPath.length() - 1) : oldPath;
            String parentPath = prefix.contains("/") ? prefix.substring(0, prefix.lastIndexOf("/") + 1) : "/";
            String newPath = parentPath + newName + "/";
            file.setPath(newPath);
            
            List<CodeFile> children = fileRepository.findByProjectIdAndIsDeleted(file.getProjectId(), false);
            for (CodeFile child : children) {
                if (child.getPath().startsWith(oldPath)) {
                    String newChildPath = child.getPath().replaceFirst(oldPath, newPath);
                    child.setPath(newChildPath);
                    fileRepository.save(child);
                }
            }
        } else {
            String prefix = oldPath;
            if (oldName != null && prefix.endsWith(oldName)) {
                String parentPath = prefix.substring(0, prefix.length() - oldName.length());
                file.setPath(parentPath + newName);
            }
        }
        
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
        String oldPath = file.getPath();
        
        if (file.isFolder()) {
            if (!newPath.endsWith("/")) {
                newPath = newPath + "/";
            }
            String newFolderPath = newPath + file.getName() + "/";
            file.setPath(newFolderPath);
            
            List<CodeFile> children = fileRepository.findByProjectIdAndIsDeleted(file.getProjectId(), false);
            for (CodeFile child : children) {
                if (child.getPath().startsWith(oldPath)) {
                    String newChildPath = child.getPath().replaceFirst(oldPath, newFolderPath);
                    child.setPath(newChildPath);
                    fileRepository.save(child);
                }
            }
        } else {
            if (!newPath.endsWith("/")) {
                newPath = newPath + "/";
            }
            file.setPath(newPath + file.getName());
        }
        
        return fileRepository.save(file);
    }

    @Override
    @Transactional
    public CodeFile createFolder(CreateFolderRequest request) {
        logger.info("API HIT: createFolder - projectId: {}, name: {}, path: {}", request.getProjectId(), request.getName(), request.getPath());
        
        if (request.getProjectId() == null) {
            throw new IllegalArgumentException("Project ID is required");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Folder name is required");
        }

        Long createdById = request.getCreatedById();
        if (createdById == null) {
            createdById = 1L;
        }

        String basePath = request.getPath();
        if (basePath == null) basePath = "/";
        basePath = basePath.trim();
        if (basePath.isEmpty()) basePath = "/";
        if (!basePath.endsWith("/")) {
            basePath = basePath + "/";
        }
        
        String folderPath = basePath + request.getName() + "/";
        logger.info("Creating folder with full path: {}", folderPath);
        
        boolean exists = fileRepository.existsByProjectIdAndPath(request.getProjectId(), folderPath);
        if (exists) {
            throw new IllegalArgumentException("Folder already exists at path: " + folderPath);
        }
        
        if (!basePath.equals("/")) {
            logger.info("Checking if parent folder exists at path: {}", basePath);
            boolean parentExists = fileRepository.findByProjectIdAndPathAndIsFolderAndIsDeleted(
                request.getProjectId(), basePath, true, false).isPresent();
            logger.info("Parent folder exists: {}", parentExists);
            if (!parentExists) {
                throw new IllegalArgumentException("Parent folder does not exist: " + basePath);
            }
        }

        CodeFile folder = new CodeFile();
        folder.setProjectId(request.getProjectId());
        folder.setName(request.getName());
        folder.setPath(folderPath);
        folder.setLanguage(null);
        folder.setContent("");
        folder.setSize(0L);
        folder.setCreatedById(createdById);
        folder.setLastEditedBy(createdById);
        folder.setDeleted(false);
        folder.setFolder(true);

        return fileRepository.save(folder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileTreeNode> getFileTree(Long projectId) {
        logger.info("API HIT: getFileTree - projectId: {}", projectId);
        List<CodeFile> files = fileRepository.findByProjectIdAndIsDeleted(projectId, false);
        logger.debug("Found {} files for project {}", files.size(), projectId);
        return buildFileTree(files);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodeFile> getChildren(Long projectId, String parentPath) {
        logger.info("API HIT: getChildren - projectId: {}, parentPath: {}", projectId, parentPath);
        
        if (parentPath == null || parentPath.isEmpty() || parentPath.equals("/")) {
            return fileRepository.findByProjectIdAndIsDeleted(projectId, false).stream()
                    .filter(f -> {
                        String path = f.getPath();
                        if (path == null) return false;
                        int slashCount = path.length() - path.replace("/", "").length();
                        return slashCount <= 1;
                    })
                    .collect(Collectors.toList());
        }
        
        final String searchPath;
        if (!parentPath.endsWith("/")) {
            searchPath = parentPath + "/";
        } else {
            searchPath = parentPath;
        }
        
        final String finalSearchPath = searchPath;
        List<CodeFile> allFiles = fileRepository.findByParentPath(projectId, searchPath);
        
        return allFiles.stream()
                .filter(f -> {
                    String path = f.getPath();
                    if (path == null) return false;
                    String relativePath = path.substring(finalSearchPath.length());
                    return !relativePath.contains("/");
                })
                .collect(Collectors.toList());
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
            if (path == null || path.isEmpty()) continue;
            
            boolean isFolder = file.isFolder();
            
            FileTreeNode node = new FileTreeNode(
                    file.getFileId(),
                    file.getName(),
                    path,
                    file.getLanguage(),
                    isFolder
            );
            nodeMap.put(path, node);
        }

        for (FileTreeNode node : nodeMap.values()) {
            String path = node.getPath();
            
            if (path.equals("/") || path.equals("")) {
                rootNodes.add(node);
                continue;
            }
            
            String parentPath = getParentPath(path);
            
            if (parentPath.isEmpty() || parentPath.equals("/")) {
                rootNodes.add(node);
                continue;
            }
            
            FileTreeNode parent = nodeMap.get(parentPath);
            if (parent != null && parent.isFolder()) {
                parent.addChild(node);
            } else {
                rootNodes.add(node);
            }
        }

        rootNodes.sort((a, b) -> {
            if (a.isFolder() && !b.isFolder()) return -1;
            if (!a.isFolder() && b.isFolder()) return 1;
            return a.getName().compareTo(b.getName());
        });
        
        for (FileTreeNode node : nodeMap.values()) {
            node.getChildren().sort((a, b) -> {
                if (a.isFolder() && !b.isFolder()) return -1;
                if (!a.isFolder() && b.isFolder()) return 1;
                return a.getName().compareTo(b.getName());
            });
        }

        return rootNodes;
    }

    private String getParentPath(String path) {
        if (path == null || path.isEmpty()) {
          return "/";
        }
        if (path.equals("/")) {
          return "/";
        }
        
        String normalizedPath = path;
        boolean endsWithSlash = normalizedPath.endsWith("/");
        if (endsWithSlash) {
          normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
        }
        
        int lastSlash = normalizedPath.lastIndexOf('/');
        if (lastSlash <= 0) {
          return "/";
        }
        
        return normalizedPath.substring(0, lastSlash) + "/";
    }

    @Override
    @Transactional
    public void copyProjectFiles(Long sourceProjectId, Long targetProjectId) {
        logger.info("Copying files from project {} to project {}", sourceProjectId, targetProjectId);
        
        List<CodeFile> sourceFiles = fileRepository.findByProjectIdAndIsDeleted(sourceProjectId, false);
        
        Map<String, String> oldToNewPathMap = new HashMap<>();
        
        for (CodeFile sourceFile : sourceFiles) {
            CodeFile newFile = new CodeFile();
            newFile.setProjectId(targetProjectId);
            newFile.setName(sourceFile.getName());
            newFile.setPath(sourceFile.getPath());
            newFile.setLanguage(sourceFile.getLanguage());
            newFile.setContent(sourceFile.getContent());
            newFile.setSize(sourceFile.getSize());
            newFile.setCreatedById(targetProjectId);
            newFile.setLastEditedBy(targetProjectId);
            newFile.setDeleted(false);
            newFile.setFolder(sourceFile.isFolder());
            
            CodeFile saved = fileRepository.save(newFile);
            
            oldToNewPathMap.put(sourceFile.getPath(), saved.getPath());
        }
        
        for (CodeFile sourceFile : sourceFiles) {
            if (sourceFile.isFolder()) {
                String newPath = oldToNewPathMap.get(sourceFile.getPath());
                if (newPath != null) {
                    List<CodeFile> children = fileRepository.findByProjectIdAndIsDeleted(sourceProjectId, false);
                    for (CodeFile child : children) {
                        if (child.getPath().startsWith(sourceFile.getPath()) && !child.getPath().equals(sourceFile.getPath())) {
                            String relativePath = child.getPath().substring(sourceFile.getPath().length());
                            String newChildPath = newPath + relativePath;
                            String mappedPath = oldToNewPathMap.get(child.getPath());
                            if (mappedPath != null) {
                                child.setPath(mappedPath);
                                fileRepository.save(child);
                            }
                        }
                    }
                }
            }
        }
        
        logger.info("Copied {} files from project {} to project {}", sourceFiles.size(), sourceProjectId, targetProjectId);
    }
}