package com.education.platform.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "coding_questions")
public class CodingQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String language;
    
    @Column(columnDefinition = "JSON")
    private String testCases;
    
    @Column(columnDefinition = "TEXT")
    private String starterCode;
    
    private Integer points = 10;
    
    @ManyToOne
    @JoinColumn(name = "exam_id")
    private Exam exam;

    // Constructors
    public CodingQuestion() {}
    
    public CodingQuestion(String title, String description, String language, String testCases) {
        this.title = title;
        this.description = description;
        this.language = language;
        this.testCases = testCases;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getTestCases() { return testCases; }
    public void setTestCases(String testCases) { this.testCases = testCases; }
    
    public String getStarterCode() { return starterCode; }
    public void setStarterCode(String starterCode) { this.starterCode = starterCode; }
    
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    
    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }
}