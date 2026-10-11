package com.education.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_progress")
public class UserProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "learning_path_id")
    private LearningPath learningPath;
    
    private Double completionPercentage = 0.0;
    private Integer timeSpentMinutes = 0;
    private LocalDateTime lastAccessed = LocalDateTime.now();
    private LocalDateTime startedAt = LocalDateTime.now();
    private LocalDateTime completedAt;
    
    @Enumerated(EnumType.STRING)
    private Status status = Status.IN_PROGRESS;

    public enum Status {
        NOT_STARTED, IN_PROGRESS, COMPLETED, PAUSED
    }

    // Constructors
    public UserProgress() {}
    
    public UserProgress(User user, LearningPath learningPath) {
        this.user = user;
        this.learningPath = learningPath;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public LearningPath getLearningPath() { return learningPath; }
    public void setLearningPath(LearningPath learningPath) { this.learningPath = learningPath; }
    
    public Double getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(Double completionPercentage) { this.completionPercentage = completionPercentage; }
    
    public Integer getTimeSpentMinutes() { return timeSpentMinutes; }
    public void setTimeSpentMinutes(Integer timeSpentMinutes) { this.timeSpentMinutes = timeSpentMinutes; }
    
    public LocalDateTime getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(LocalDateTime lastAccessed) { this.lastAccessed = lastAccessed; }
    
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}