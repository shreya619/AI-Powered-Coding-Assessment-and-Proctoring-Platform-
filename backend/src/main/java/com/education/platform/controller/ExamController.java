package com.education.platform.controller;

import com.education.platform.model.*;
import com.education.platform.service.ExamService;
import com.education.platform.service.AIRecommendationService;
import com.education.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/exams")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class ExamController {
    @Autowired
    private ExamService examService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AIRecommendationService aiRecommendationService;

    @GetMapping
    public ResponseEntity<List<Exam>> getActiveExams() {
        System.out.println("[CONTROLLER] ExamController.getActiveExams() - GET /api/exams");
        List<Exam> exams = examService.getActiveExams();
        System.out.println("[CONTROLLER] ExamController.getActiveExams() - Returning " + exams.size() + " exams");
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/{examId}")
    public ResponseEntity<Exam> getExamById(@PathVariable Long examId) {
        System.out.println("[CONTROLLER] ExamController.getExamById() - GET /api/exams/" + examId);
        try {
            Exam exam = examService.getExamById(examId);
            System.out.println("[CONTROLLER] ExamController.getExamById() - Returning exam: " + exam.getTitle());
            return ResponseEntity.ok(exam);
        } catch (Exception e) {
            System.out.println("[CONTROLLER] ExamController.getExamById() - Exam not found: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createExam(@RequestBody Map<String, Object> request) {
        System.out.println("[CONTROLLER] ExamController.createExam() - POST /api/exams");
        try {
            String title = (String) request.get("title");
            String description = (String) request.get("description");
            Integer duration = (Integer) request.get("duration");
            Integer totalMarks = (Integer) request.get("totalMarks");
            Long createdById = Long.valueOf(request.get("createdBy").toString());
            System.out.println("[CONTROLLER] ExamController.createExam() - Creating exam: " + title);
            User creator = userRepository.findById(createdById)
                .orElseThrow(() -> new RuntimeException("User not found"));
            Exam exam = new Exam(title, description, duration, totalMarks, creator);
            exam = examService.createExam(exam);
            System.out.println("[CONTROLLER] ExamController.createExam() - Exam created successfully with ID: " + exam.getId());
            return ResponseEntity.ok(exam);
        } catch (Exception e) {
            System.out.println("[CONTROLLER] ExamController.createExam() - Error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to create exam: " + e.getMessage());
        }
    }

    @PostMapping("/{examId}/start")
    public ResponseEntity<?> startExam(@PathVariable Long examId, @RequestBody Map<String, Long> request) {
        System.out.println("[CONTROLLER] ExamController.startExam() - POST /api/exams/" + examId + "/start");
        try {
            Long userId = request.get("userId");
            System.out.println("[CONTROLLER] ExamController.startExam() - Starting exam for user: " + userId);
            ExamResult result = examService.startExam(examId, userId);
            System.out.println("[CONTROLLER] ExamController.startExam() - Exam started, result ID: " + result.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("id", result.getId());
            response.put("startedAt", result.getStartedAt());
            response.put("isCompleted", result.getIsCompleted());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("[CONTROLLER] ExamController.startExam() - Error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to start exam: " + e.getMessage());
        }
    }

    @PostMapping("/results/{resultId}/submit")
    public ResponseEntity<?> submitExam(@PathVariable Long resultId, @RequestBody Map<String, Double> request) {
        try {
            Double score = request.get("score");
            ExamResult result = examService.submitExam(resultId, score);
            Map<String, Object> response = new HashMap<>();
            response.put("id", result.getId());
            response.put("score", result.getScore());
            response.put("submittedAt", result.getSubmittedAt());
            response.put("isCompleted", result.getIsCompleted());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to submit exam: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}/results")
    public ResponseEntity<List<ExamResult>> getUserExamResults(@PathVariable Long userId) {
        return ResponseEntity.ok(examService.getUserExamResults(userId));
    }

    @PostMapping("/results/{resultId}/submit-coding")
    public ResponseEntity<?> submitCodingExam(@PathVariable Long resultId, @RequestBody Map<String, Object> request) {
        System.out.println("[CONTROLLER] ExamController.submitCodingExam() - POST /api/exams/results/" + resultId + "/submit-coding");
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> codingAnswers = (List<Map<String, Object>>) request.get("codingAnswers");
            
            double totalScore = 0;
            if (codingAnswers != null) {
                for (Map<String, Object> answer : codingAnswers) {
                    Integer passedTests = (Integer) answer.get("passedTests");
                    Integer totalTests = (Integer) answer.get("totalTests");
                    Integer questionMarks = (Integer) answer.get("marks");
                    
                    if (totalTests != null && totalTests > 0) {
                        double questionScore = ((double) passedTests / totalTests) * questionMarks;
                        totalScore += questionScore;
                    }
                }
            }
            
            ExamResult result = examService.submitExam(resultId, totalScore);
            
            Map<String, Object> response = new HashMap<>();
            response.put("score", result.getScore());
            response.put("totalMarks", result.getExam().getTotalMarks());
            response.put("submittedAt", result.getSubmittedAt());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("[CONTROLLER] ExamController.submitCodingExam() - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to submit coding exam: " + e.getMessage());
        }
    }
    
    @PostMapping("/results/{resultId}/submit-detailed")
    public ResponseEntity<?> submitExamDetailed(@PathVariable Long resultId, @RequestBody Map<String, Object> request) {
        System.out.println("[CONTROLLER] ExamController.submitExamDetailed() - POST /api/exams/results/" + resultId + "/submit-detailed");
        try {
            Object scoreObj = request.get("score");
            if (scoreObj == null) {
                return ResponseEntity.badRequest().body("Score is required");
            }
            Double score = Double.valueOf(scoreObj.toString());
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> answers = (List<Map<String, Object>>) request.get("answers");
            if (answers == null) {
                answers = new ArrayList<>();
            }
            
            @SuppressWarnings("unchecked")
            List<String> suspiciousActivities = (List<String>) request.get("suspiciousActivities");

            System.out.println("[CONTROLLER] ExamController.submitExamDetailed() - Submitting with score: " + score + ", answers: " + answers.size());

            ExamResult result = examService.submitExam(resultId, score);
            Integer totalMarks = result.getExam().getTotalMarks();

            String aiRecommendations;
            try {
                System.out.println("[CONTROLLER] ExamController.submitExamDetailed() - Generating AI exam insights");
                aiRecommendations = aiRecommendationService.generatePersonalizedRecommendations(
                    answers,
                    score,
                    totalMarks,
                    result.getExam().getTitle()
                );
            } catch (Exception aiError) {
                System.err.println("[CONTROLLER] AI service failed: " + aiError.getMessage());
                aiError.printStackTrace();
                aiRecommendations = generateBasicRecommendations(answers, score, totalMarks);
            }

            result.setProctoringReport(aiRecommendations);
            examService.saveExamResult(result);

            System.out.println("[CONTROLLER] ExamController.submitExamDetailed() - Exam submitted with recommendations");
            Map<String, Object> response = new HashMap<>();
            response.put("score", result.getScore());
            response.put("totalMarks", totalMarks);
            response.put("aiRecommendations", aiRecommendations);
            response.put("submittedAt", result.getSubmittedAt());
            response.put("suspiciousActivityCount", suspiciousActivities != null ? suspiciousActivities.size() : 0);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("[CONTROLLER] ExamController.submitExamDetailed() - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to submit exam: " + e.getMessage());
        }
    }

    private String generateBasicRecommendations(List<Map<String, Object>> answers, Double score, Integer totalMarks) {
        StringBuilder recommendations = new StringBuilder();
        double percentage = (score / totalMarks) * 100;
        
        recommendations.append("📊 **Exam Performance Summary**\n\n");
        recommendations.append(String.format("Score: %.0f/%.0f (%.1f%%)\n\n", score, (double)totalMarks, percentage));
        
        if (answers != null && !answers.isEmpty()) {
            List<Map<String, Object>> wrongAnswers = answers.stream()
                .filter(answer -> answer.get("isCorrect") != null && !(Boolean) answer.get("isCorrect"))
                .collect(java.util.stream.Collectors.toList());
            
            if (!wrongAnswers.isEmpty()) {
                recommendations.append("🔍 **Areas for Improvement:**\n\n");
                for (int i = 0; i < Math.min(wrongAnswers.size(), 5); i++) {
                    Map<String, Object> answer = wrongAnswers.get(i);
                    recommendations.append(String.format("**Question %d:** %s\n", i + 1, answer.get("questionText")));
                    recommendations.append(String.format("❌ Your answer: \"%s\"\n", answer.get("userAnswer")));
                    recommendations.append(String.format("✅ Correct answer: \"%s\"\n", answer.get("correctAnswer")));
                    recommendations.append("💡 **Tip:** Review this topic and practice similar questions.\n\n");
                }
            }
        }
        
        recommendations.append("\n🎓 **Study Recommendations:**\n");
        if (percentage >= 80) {
            recommendations.append("🎉 **Great job!** Keep up the excellent work and continue practicing.");
        } else if (percentage >= 60) {
            recommendations.append("👍 **Good effort!** Focus on the areas you missed and try again.");
        } else {
            recommendations.append("📚 **Keep learning!** Review the fundamentals and practice more questions.");
        }
        return recommendations.toString();
    }

}