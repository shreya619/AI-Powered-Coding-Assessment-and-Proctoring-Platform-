package com.education.platform.service;

import com.education.platform.model.User;
import com.education.platform.model.Achievement;
import com.education.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class GamificationService {
    
    @Autowired
    private UserRepository userRepository;
    
    public void awardPointsForExam(User user, Double score) {
        int points = calculatePoints(score);
        user.setTotalPoints(user.getTotalPoints() + points);
        userRepository.save(user);
        
        checkAndAwardAchievements(user);
    }
    
    private int calculatePoints(Double score) {
        if (score >= 90) return 100;
        if (score >= 80) return 80;
        if (score >= 70) return 60;
        if (score >= 60) return 40;
        return 20;
    }
    
    private void checkAndAwardAchievements(User user) {
        if (user.getExamResults().size() == 1) {
            Achievement achievement = new Achievement(
                "First Steps", 
                "Completed your first exam", 
                "🎯", 
                50, 
                user
            );
            user.getAchievements().add(achievement);
        }
        
        if (user.getAverageScore() >= 90) {
            Achievement achievement = new Achievement(
                "Excellence", 
                "Maintained 90%+ average score", 
                "🏆", 
                200, 
                user
            );
            user.getAchievements().add(achievement);
        }
        
        if (user.getTotalPoints() >= 1000) {
            Achievement achievement = new Achievement(
                "Point Master", 
                "Earned 1000+ points", 
                "⭐", 
                100, 
                user
            );
            user.getAchievements().add(achievement);
        }
        
        userRepository.save(user);
    }
    
    public Map<String, Object> getLeaderboard() {
        Map<String, Object> leaderboard = new HashMap<>();
        
        List<User> topByPoints = userRepository.findTopStudentsByPoints();
        List<User> topByScore = userRepository.findTopStudentsByScore();
        
        leaderboard.put("topByPoints", topByPoints.stream().limit(10).toList());
        leaderboard.put("topByScore", topByScore.stream().limit(10).toList());
        
        return leaderboard;
    }
    
    public Map<String, Object> getUserProgress(User user) {
        Map<String, Object> progress = new HashMap<>();
        
        progress.put("totalPoints", user.getTotalPoints());
        progress.put("averageScore", user.getAverageScore());
        progress.put("achievements", user.getAchievements());
        progress.put("totalExams", user.getExamResults().size());
        progress.put("rank", calculateUserRank(user));
        
        return progress;
    }
    
    private int calculateUserRank(User user) {
        List<User> allStudents = userRepository.findTopStudentsByPoints();
        for (int i = 0; i < allStudents.size(); i++) {
            if (allStudents.get(i).getId().equals(user.getId())) {
                return i + 1;
            }
        }
        return allStudents.size();
    }
}