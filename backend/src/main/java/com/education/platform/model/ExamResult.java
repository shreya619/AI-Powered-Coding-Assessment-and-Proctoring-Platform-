package com.education.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_results")
public class ExamResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "exam_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Exam exam;
    
    private Double score;
    private Integer totalMarks;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private Boolean isCompleted = false;
    
    @Column(columnDefinition = "TEXT")
    private String proctoringReport;

    public ExamResult() {}
    
    public ExamResult(User user, Exam exam) {
        this.user = user;
        this.exam = exam;
        this.startedAt = LocalDateTime.now();
        this.totalMarks = exam.getTotalMarks();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }
    
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    
    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }
    
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    
    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }
    
    public String getProctoringReport() { return proctoringReport; }
    public void setProctoringReport(String proctoringReport) { this.proctoringReport = proctoringReport; }
}