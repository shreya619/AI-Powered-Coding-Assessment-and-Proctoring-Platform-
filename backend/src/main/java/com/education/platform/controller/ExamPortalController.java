package com.education.platform.controller;

import com.education.platform.model.*;
import com.education.platform.service.*;
import com.education.platform.repository.*;
import com.education.platform.config.BedrockAIConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/exam-portal")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class ExamPortalController {
    
    @Autowired
    private ExamService examService;
    
    @Autowired
    private ProctoringService proctoringService;
    
    @Autowired
    private BedrockAIConfig.AIService aiService;
    
    @GetMapping("/{examId}")
    public ResponseEntity<?> getExamForPortal(@PathVariable Long examId) {
        try {
            Map<String, Object> examData = new HashMap<>();
            
            // Mock exam data
            Map<String, Object> exam = new HashMap<>();
            exam.put("id", examId);
            exam.put("title", "Java Programming Assessment");
            exam.put("description", "Comprehensive test of Java programming concepts");
            exam.put("duration", 90);
            exam.put("totalMarks", 100);
            exam.put("proctoringEnabled", true);
            
            List<Map<String, Object>> questions = Arrays.asList(
                Map.of(
                    "id", 1,
                    "questionText", "What is the correct syntax for the main method in Java?",
                    "type", "MULTIPLE_CHOICE",
                    "marks", 10,
                    "options", Arrays.asList(
                        "public static void main(String[] args)",
                        "public void main(String[] args)",
                        "static void main(String[] args)",
                        "public static main(String[] args)"
                    )
                ),
                Map.of(
                    "id", 2,
                    "questionText", "Java is platform independent. True or False?",
                    "type", "TRUE_FALSE",
                    "marks", 5,
                    "options", Arrays.asList("True", "False")
                ),
                Map.of(
                    "id", 3,
                    "questionText", "Explain the concept of inheritance in Java with an example.",
                    "type", "SHORT_ANSWER",
                    "marks", 15
                ),
                Map.of(
                    "id", 4,
                    "questionText", "Which of the following are valid Java data types?",
                    "type", "MULTIPLE_CHOICE",
                    "marks", 10,
                    "options", Arrays.asList("int", "string", "boolean", "char")
                ),
                Map.of(
                    "id", 5,
                    "questionText", "What is polymorphism in Java? Provide examples.",
                    "type", "SHORT_ANSWER",
                    "marks", 20
                )
            );
            
            examData.put("exam", exam);
            examData.put("questions", questions);
            examData.put("instructions", Arrays.asList(
                "Ensure stable internet connection throughout the exam",
                "Do not switch tabs, minimize window, or use external applications",
                "Keep your face visible to the camera at all times",
                "No external help, books, or materials are allowed",
                "The exam will auto-submit when time expires",
                "Any suspicious activity will be recorded and flagged"
            ));
            
            return ResponseEntity.ok(examData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to load exam: " + e.getMessage());
        }
    }
    
    @PostMapping("/{examId}/start")
    public ResponseEntity<?> startExamSession(@PathVariable Long examId, @RequestBody Map<String, Long> request) {
        try {
            Long userId = request.get("userId");
            
            Map<String, Object> session = new HashMap<>();
            session.put("sessionId", System.currentTimeMillis());
            session.put("examId", examId);
            session.put("userId", userId);
            session.put("startTime", LocalDateTime.now());
            session.put("proctoringEnabled", true);
            session.put("status", "ACTIVE");
            
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to start exam session: " + e.getMessage());
        }
    }
    
    @PostMapping("/sessions/{sessionId}/proctoring/alert")
    public ResponseEntity<?> reportProctoringAlert(@PathVariable Long sessionId, @RequestBody Map<String, String> request) {
        try {
            String activity = request.get("activity");
            String timestamp = request.get("timestamp");
            
            // AI analysis of suspicious activity
            String aiAnalysis = aiService.analyzePerformance(
                "Suspicious activity detected: " + activity + " at " + timestamp
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("alertRecorded", true);
            response.put("aiAnalysis", aiAnalysis);
            response.put("riskLevel", calculateRiskLevel(activity));
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to record alert: " + e.getMessage());
        }
    }
    
    @PostMapping("/sessions/{sessionId}/submit")
    public ResponseEntity<?> submitExam(@PathVariable Long sessionId, @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> answers = (Map<String, String>) request.get("answers");
            @SuppressWarnings("unchecked")
            List<String> suspiciousActivities = (List<String>) request.get("suspiciousActivities");
            
            // Calculate score
            double score = calculateExamScore(answers);
            
            // Generate AI-powered exam report
            String examReport = generateExamReport(answers, suspiciousActivities, score);
            
            Map<String, Object> result = new HashMap<>();
            result.put("score", score);
            result.put("totalMarks", 100);
            result.put("percentage", (score / 100) * 100);
            result.put("submittedAt", LocalDateTime.now());
            result.put("proctoringReport", examReport);
            result.put("suspiciousActivityCount", suspiciousActivities != null ? suspiciousActivities.size() : 0);
            result.put("status", "COMPLETED");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to submit exam: " + e.getMessage());
        }
    }
    
    @GetMapping("/sessions/{sessionId}/report")
    public ResponseEntity<?> getExamReport(@PathVariable Long sessionId) {
        try {
            Map<String, Object> report = new HashMap<>();
            
            // Mock detailed report
            report.put("sessionId", sessionId);
            report.put("examTitle", "Java Programming Assessment");
            report.put("studentName", "John Doe");
            report.put("score", 85);
            report.put("totalMarks", 100);
            report.put("percentage", 85.0);
            report.put("duration", "87 minutes");
            report.put("submittedAt", LocalDateTime.now().minusHours(1));
            
            // Proctoring analysis
            Map<String, Object> proctoringAnalysis = new HashMap<>();
            proctoringAnalysis.put("totalAlerts", 2);
            proctoringAnalysis.put("riskLevel", "LOW");
            proctoringAnalysis.put("faceVisibilityScore", 95);
            proctoringAnalysis.put("tabSwitchCount", 1);
            proctoringAnalysis.put("suspiciousMovements", 0);
            proctoringAnalysis.put("aiConfidenceScore", 92);
            
            report.put("proctoringAnalysis", proctoringAnalysis);
            
            // Question-wise performance
            List<Map<String, Object>> questionAnalysis = Arrays.asList(
                Map.of("questionId", 1, "answered", true, "correct", true, "timeSpent", "2:30"),
                Map.of("questionId", 2, "answered", true, "correct", true, "timeSpent", "1:15"),
                Map.of("questionId", 3, "answered", true, "correct", false, "timeSpent", "8:45"),
                Map.of("questionId", 4, "answered", true, "correct", true, "timeSpent", "3:20"),
                Map.of("questionId", 5, "answered", true, "correct", true, "timeSpent", "12:10")
            );
            
            report.put("questionAnalysis", questionAnalysis);
            
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to generate report: " + e.getMessage());
        }
    }
    
    private String calculateRiskLevel(String activity) {
        if (activity.contains("tab switch") || activity.contains("window")) {
            return "HIGH";
        } else if (activity.contains("key combination") || activity.contains("mouse")) {
            return "MEDIUM";
        }
        return "LOW";
    }
    
    private double calculateExamScore(Map<String, String> answers) {
        // Mock scoring logic
        Map<String, String> correctAnswers = Map.of(
            "1", "public static void main(String[] args)",
            "2", "True",
            "4", "int"
        );
        
        double score = 0;
        for (Map.Entry<String, String> entry : answers.entrySet()) {
            if (correctAnswers.containsKey(entry.getKey()) && 
                correctAnswers.get(entry.getKey()).equals(entry.getValue())) {
                score += entry.getKey().equals("1") || entry.getKey().equals("4") ? 10 : 5;
            }
        }
        
        // Add partial marks for short answers
        if (answers.containsKey("3") && answers.get("3").length() > 50) {
            score += 10; // Partial marks for attempt
        }
        if (answers.containsKey("5") && answers.get("5").length() > 100) {
            score += 15; // Partial marks for attempt
        }
        
        return score;
    }
    
    private String generateExamReport(Map<String, String> answers, List<String> activities, double score) {
        String performanceLevel = score >= 80 ? "Excellent" : score >= 60 ? "Good" : "Needs Improvement";
        int activityCount = activities != null ? activities.size() : 0;
        String integrityLevel = activityCount == 0 ? "High" : activityCount <= 2 ? "Medium" : "Low";
        
        return String.format(
            "📊 Exam Performance: %s (%.1f/100)\n" +
            "🔒 Academic Integrity: %s (%d alerts)\n" +
            "⏱️ Completion Status: Submitted on time\n" +
            "🎯 Recommendation: %s",
            performanceLevel, score, integrityLevel, activityCount,
            score >= 80 ? "Continue with advanced topics" : "Review fundamental concepts"
        );
    }
}