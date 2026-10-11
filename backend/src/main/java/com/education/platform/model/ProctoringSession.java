package com.education.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "proctoring_sessions")
public class ProctoringSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "exam_result_id")
    private ExamResult examResult;
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    @ElementCollection
    @CollectionTable(name = "proctoring_alerts")
    private List<String> alerts;
    
    private String videoRecordingPath;
    private String screenRecordingPath;
    
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;
    
    private Integer suspiciousActivityCount = 0;
    private String aiAnalysisReport;

    public enum Status {
        ACTIVE, COMPLETED, TERMINATED, FLAGGED
    }

    // Constructors
    public ProctoringSession() {}
    
    public ProctoringSession(ExamResult examResult) {
        this.examResult = examResult;
        this.startTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public ExamResult getExamResult() { return examResult; }
    public void setExamResult(ExamResult examResult) { this.examResult = examResult; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public List<String> getAlerts() { return alerts; }
    public void setAlerts(List<String> alerts) { this.alerts = alerts; }
    
    public String getVideoRecordingPath() { return videoRecordingPath; }
    public void setVideoRecordingPath(String videoRecordingPath) { this.videoRecordingPath = videoRecordingPath; }
    
    public String getScreenRecordingPath() { return screenRecordingPath; }
    public void setScreenRecordingPath(String screenRecordingPath) { this.screenRecordingPath = screenRecordingPath; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
    public Integer getSuspiciousActivityCount() { return suspiciousActivityCount; }
    public void setSuspiciousActivityCount(Integer suspiciousActivityCount) { this.suspiciousActivityCount = suspiciousActivityCount; }
    
    public String getAiAnalysisReport() { return aiAnalysisReport; }
    public void setAiAnalysisReport(String aiAnalysisReport) { this.aiAnalysisReport = aiAnalysisReport; }
}