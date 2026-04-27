package com.codesync.execution.service;

public class ExecutionOutput {
    private String status;
    private String output;
    private String type;

    public ExecutionOutput() {}

    public ExecutionOutput(String status, String output) {
        this.status = status;
        this.output = output;
        this.type = "stream";
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}