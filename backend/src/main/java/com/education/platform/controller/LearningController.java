package com.education.platform.controller;

import com.education.platform.model.User;
import com.education.platform.service.AdaptiveLearningService;
import com.education.platform.service.GamificationService;
import com.education.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/learning")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class LearningController {
    
    @Autowired
    private AdaptiveLearningService adaptiveLearningService;
    
    @Autowired
    private GamificationService gamificationService;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/recommendations/{userId}")
    public ResponseEntity<?> getRecommendations(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Map<String, Object> recommendations = adaptiveLearningService.generatePersonalizedRecommendations(user);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get recommendations: " + e.getMessage());
        }
    }
    
    @GetMapping("/progress/{userId}")
    public ResponseEntity<?> getUserProgress(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Map<String, Object> progress = gamificationService.getUserProgress(user);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get progress: " + e.getMessage());
        }
    }
    
    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard() {
        try {
            Map<String, Object> leaderboard = gamificationService.getLeaderboard();
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get leaderboard: " + e.getMessage());
        }
    }
    
    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateLearningProfile(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            String learningStyle = request.get("learningStyle");
            if (learningStyle != null) {
                user.setLearningStyle(User.LearningStyle.valueOf(learningStyle.toUpperCase()));
                userRepository.save(user);
            }
            
            return ResponseEntity.ok("Profile updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update profile: " + e.getMessage());
        }
    }
}