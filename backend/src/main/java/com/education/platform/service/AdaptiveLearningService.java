package com.education.platform.service;

import com.education.platform.model.User;
import com.education.platform.model.ExamResult;
import com.education.platform.repository.ExamResultRepository;
import com.education.platform.config.BedrockAIConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class AdaptiveLearningService {
    
    @Autowired
    private ExamResultRepository examResultRepository;
    
    @Autowired
    private BedrockAIConfig.AIService aiService;
    
    public Map<String, Object> generatePersonalizedRecommendations(User user) {
        Map<String, Object> recommendations = new HashMap<>();
        
        List<ExamResult> results = examResultRepository.findByUserId(user.getId());
        Double averageScore = examResultRepository.findAverageScoreByUserId(user.getId());
        
        String learningAnalysis = analyzeLearningPatterns(user, results, averageScore);
        String aiRecommendations = aiService.generateRecommendations(learningAnalysis);
        
        recommendations.put("averageScore", averageScore != null ? averageScore : 0.0);
        recommendations.put("totalExams", results.size());
        recommendations.put("learningStyle", user.getLearningStyle());
        recommendations.put("recommendations", aiRecommendations);
        recommendations.put("suggestedTopics", getSuggestedTopics(user, averageScore));
        
        return recommendations;
    }
    
    private String analyzeLearningPatterns(User user, List<ExamResult> results, Double averageScore) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("User Learning Profile: ");
        analysis.append("Learning Style: ").append(user.getLearningStyle()).append(", ");
        analysis.append("Average Score: ").append(averageScore != null ? averageScore : 0).append("%, ");
        analysis.append("Total Exams: ").append(results.size());
        
        if (averageScore != null) {
            if (averageScore >= 80) {
                analysis.append(". Strong performer, ready for advanced topics.");
            } else if (averageScore >= 60) {
                analysis.append(". Moderate performance, needs reinforcement in weak areas.");
            } else {
                analysis.append(". Needs significant improvement, focus on fundamentals.");
            }
        }
        
        return analysis.toString();
    }
    
    private List<String> getSuggestedTopics(User user, Double averageScore) {
        if (averageScore == null || averageScore < 60) {
            return List.of("Basic Concepts", "Fundamentals Review", "Practice Exercises");
        } else if (averageScore < 80) {
            return List.of("Intermediate Topics", "Problem Solving", "Application Exercises");
        } else {
            return List.of("Advanced Topics", "Complex Problems", "Research Projects");
        }
    }
}