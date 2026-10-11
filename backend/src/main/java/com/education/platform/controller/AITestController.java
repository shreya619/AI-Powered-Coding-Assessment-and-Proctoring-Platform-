package com.education.platform.controller;

import com.education.platform.config.BedrockAIConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/ai-test")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class AITestController {
    
    @Autowired
    private BedrockAIConfig.AIService aiService;
    
    @PostMapping("/recommendations")
    public ResponseEntity<?> testRecommendations(@RequestBody Map<String, String> request) {
        try {
            String analysis = request.getOrDefault("analysis", 
                "Student has 75% average score, prefers visual learning, completed 5 exams");
            
            String recommendations = aiService.generateRecommendations(analysis);
            
            Map<String, Object> response = new HashMap<>();
            response.put("input", analysis);
            response.put("aiRecommendations", recommendations);
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "failed");
            return ResponseEntity.ok(error);
        }
    }
    
    @PostMapping("/generate-questions")
    public ResponseEntity<?> testQuestionGeneration(@RequestBody Map<String, Object> request) {
        try {
            String topic = (String) request.getOrDefault("topic", "Java Programming");
            String difficulty = (String) request.getOrDefault("difficulty", "intermediate");
            Integer count = (Integer) request.getOrDefault("count", 3);
            
            String questions = aiService.generateQuestions(topic, difficulty, count);
            
            Map<String, Object> response = new HashMap<>();
            response.put("topic", topic);
            response.put("difficulty", difficulty);
            response.put("count", count);
            response.put("generatedQuestions", questions);
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "failed");
            return ResponseEntity.ok(error);
        }
    }
    
    @PostMapping("/analyze-performance")
    public ResponseEntity<?> testPerformanceAnalysis(@RequestBody Map<String, String> request) {
        try {
            String performanceData = request.getOrDefault("performanceData", 
                "Student completed 10 exams, average score 82%, time spent 45 hours");
            
            String analysis = aiService.analyzePerformance(performanceData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("performanceData", performanceData);
            response.put("aiAnalysis", analysis);
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("e	rror", e.getMessage());
            error.put("status", "failed");
            return ResponseEntity.ok(error);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<?> testAIHealth() {
        Map<String, Object> response = new HashMap<>();
        try {
            String testResponse = aiService.generateRecommendations("Test connection");
            response.put("aiConnected", true);
            response.put("testResponse", testResponse);
            response.put("status", "AI service is working");
        } catch (Exception e) {
            response.put("aiConnected", false);
            response.put("error", e.getMessage());
            response.put("status", "AI service failed");
        }
        return ResponseEntity.ok(response);
    }
}