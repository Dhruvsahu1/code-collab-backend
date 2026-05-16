package com.codesync.version.dto;

public class DiffLine {
    private int lineNumber;
    private String content;
    private String type;

    public DiffLine() {}

    public DiffLine(int lineNumber, String content, String type) {
        this.lineNumber = lineNumber;
        this.content = content;
        this.type = type;
    }

    public int getLineNumber() { return lineNumber; }
    public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}