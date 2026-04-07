package com.codesync.file.dto;

import java.util.ArrayList;
import java.util.List;

public class FileTreeNode {
    private Long fileId;
    private String name;
    private String path;
    private boolean isFolder;
    private List<FileTreeNode> children = new ArrayList<>();

    public FileTreeNode() {}

    public FileTreeNode(Long fileId, String name, String path, boolean isFolder) {
        this.fileId = fileId;
        this.name = name;
        this.path = path;
        this.isFolder = isFolder;
    }

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public boolean isFolder() { return isFolder; }
    public void setFolder(boolean folder) { isFolder = folder; }

    public List<FileTreeNode> getChildren() { return children; }
    public void setChildren(List<FileTreeNode> children) { this.children = children; }

    public void addChild(FileTreeNode child) {
        this.children.add(child);
    }
}
