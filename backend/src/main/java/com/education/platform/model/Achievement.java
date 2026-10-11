package com.education.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "achievements")
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    private String description;
    private String badgeIcon;
    private Integer pointsAwarded;
    private LocalDateTime earnedAt = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Achievement() {}
    
    public Achievement(String title, String description, String badgeIcon, Integer pointsAwarded, User user) {
        this.title = title;
        this.description = description;
        this.badgeIcon = badgeIcon;
        this.pointsAwarded = pointsAwarded;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getBadgeIcon() { return badgeIcon; }
    public void setBadgeIcon(String badgeIcon) { this.badgeIcon = badgeIcon; }
    
    public Integer getPointsAwarded() { return pointsAwarded; }
    public void setPointsAwarded(Integer pointsAwarded) { this.pointsAwarded = pointsAwarded; }
    
    public LocalDateTime getEarnedAt() { return earnedAt; }
    public void setEarnedAt(LocalDateTime earnedAt) { this.earnedAt = earnedAt; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}