package com.education.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "learning_paths")
public class LearningPath {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    private String description;
    
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;
    
    @ElementCollection
    @CollectionTable(name = "learning_path_topics")
    private List<String> topics;
    
    private Integer estimatedHours;
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @OneToMany(mappedBy = "learningPath", cascade = CascadeType.ALL)
    private List<UserProgress> userProgresses;

    public enum Difficulty {
        BEGINNER, INTERMEDIATE, ADVANCED
    }

    // Constructors
    public LearningPath() {}
    
    public LearningPath(String title, String description, Difficulty difficulty, User createdBy) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }
    
    public List<String> getTopics() { return topics; }
    public void setTopics(List<String> topics) { this.topics = topics; }
    
    public Integer getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Integer estimatedHours) { this.estimatedHours = estimatedHours; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    
    public List<UserProgress> getUserProgresses() { return userProgresses; }
    public void setUserProgresses(List<UserProgress> userProgresses) { this.userProgresses = userProgresses; }
}