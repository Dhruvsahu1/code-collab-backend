package com.codesync.version.dto;

import java.time.LocalDateTime;
import java.util.List;

public class DiffResponse {
    private Long snapshot1Id;
    private Long snapshot2Id;
    private String snapshot1Hash;
    private String snapshot2Hash;
    private LocalDateTime snapshot1Time;
    private LocalDateTime snapshot2Time;
    private List<DiffLine> lines;
    private int additions;
    private int deletions;

    public DiffResponse() {}

    public Long getSnapshot1Id() { return snapshot1Id; }
    public void setSnapshot1Id(Long snapshot1Id) { this.snapshot1Id = snapshot1Id; }
    public Long getSnapshot2Id() { return snapshot2Id; }
    public void setSnapshot2Id(Long snapshot2Id) { this.snapshot2Id = snapshot2Id; }
    public String getSnapshot1Hash() { return snapshot1Hash; }
    public void setSnapshot1Hash(String snapshot1Hash) { this.snapshot1Hash = snapshot1Hash; }
    public String getSnapshot2Hash() { return snapshot2Hash; }
    public void setSnapshot2Hash(String snapshot2Hash) { this.snapshot2Hash = snapshot2Hash; }
    public LocalDateTime getSnapshot1Time() { return snapshot1Time; }
    public void setSnapshot1Time(LocalDateTime snapshot1Time) { this.snapshot1Time = snapshot1Time; }
    public LocalDateTime getSnapshot2Time() { return snapshot2Time; }
    public void setSnapshot2Time(LocalDateTime snapshot2Time) { this.snapshot2Time = snapshot2Time; }
    public List<DiffLine> getLines() { return lines; }
    public void setLines(List<DiffLine> lines) { this.lines = lines; }
    public int getAdditions() { return additions; }
    public void setAdditions(int additions) { this.additions = additions; }
    public int getDeletions() { return deletions; }
    public void setDeletions(int deletions) { this.deletions = deletions; }
}