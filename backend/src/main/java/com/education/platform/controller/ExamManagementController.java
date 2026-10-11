package com.education.platform.controller;

import com.education.platform.model.*;
import com.education.platform.service.ExamService;
import com.education.platform.repository.UserRepository;
import com.education.platform.config.BedrockAIConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

@RestController
@RequestMapping("/exam-management")
@CrossOrigin
public class ExamManagementController {
    
    @Autowired
    private ExamService examService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BedrockAIConfig.AIService aiService;
    
    @PostMapping("/create")
    public ResponseEntity<?> createExam(@RequestBody Map<String, Object> request) {
        System.out.println("Received exam creation request: " + request);
        try {
            String title = (String) request.get("title");
            String description = (String) request.get("description");
            Integer duration = Integer.valueOf(request.get("duration").toString());
            Long createdById = Long.valueOf(request.get("createdBy").toString());
            
            User creator = userRepository.findById(createdById)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Exam exam = new Exam(title, description, duration, 0, creator);
            
            // Process questions from request
            List<Map<String, Object>> questionsData = (List<Map<String, Object>>) request.get("questions");
            List<Question> questions = new ArrayList<>();
            
            System.out.println("Processing " + (questionsData != null ? questionsData.size() : 0) + " questions");
            
            if (questionsData != null) {
                for (int i = 0; i < questionsData.size(); i++) {
                    Map<String, Object> qData = questionsData.get(i);
                    System.out.println("Processing question " + (i+1) + ": " + qData);
                    String questionText = (String) qData.get("questionText");
                    String type = (String) qData.get("type");
                    Integer marks = Integer.valueOf(qData.get("marks").toString());
                    String correctAnswer = (String) qData.get("correctAnswer");
                    String topic = (String) qData.get("topic");
                    
                    Question question = new Question(questionText, 
                        Question.QuestionType.valueOf(type), marks, exam);
                    question.setCorrectAnswer(correctAnswer);
                    question.setTopic(topic);
                    
                    if ("MULTIPLE_CHOICE".equals(type)) {
                        List<String> options = (List<String>) qData.get("options");
                        if (options != null && !options.isEmpty()) {
                            question.setOptions(options);
                        }
                    }
                    
                    // Handle coding question fields
                    if ("CODING".equals(type)) {
                        String language = (String) qData.get("language");
                        String starterCode = (String) qData.get("starterCode");
                        String testCases = (String) qData.get("testCases");
                        
                        question.setLanguage(language);
                        question.setStarterCode(starterCode);
                        question.setTestCases(testCases);
                        
                        System.out.println("Saving coding question with test cases: " + testCases);
                    }
                    
                    questions.add(question);
                }
                
                exam.setQuestions(questions);
                exam.setTotalMarks(questions.size() * 10);
            }
            
            Exam savedExam = examService.createExam(exam);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Exam created successfully");
            response.put("examId", savedExam.getId());
            response.put("exam", savedExam);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error creating exam: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create exam: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<?> getTeacherExams(@PathVariable Long teacherId) {
        System.out.println("Getting exams for teacher ID: " + teacherId);
        try {
            List<Exam> exams = examService.getExamsByTeacher(teacherId);
            System.out.println("Found " + exams.size() + " exams for teacher");
            return ResponseEntity.ok(exams);
        } catch (Exception e) {
            System.err.println("Error getting teacher exams: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get teacher exams: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PostMapping("/generate-questions")
    public ResponseEntity<?> generateAIQuestions(@RequestBody Map<String, Object> request) {
        try {
            String topic = (String) request.get("topic");
            String difficulty = (String) request.get("difficulty");
            Integer count = Integer.valueOf(request.get("count").toString());
            
            String aiQuestions = aiService.generateQuestions(topic, difficulty, count);
            
            Map<String, Object> response = new HashMap<>();
            response.put("topic", topic);
            response.put("difficulty", difficulty);
            response.put("generatedQuestions", aiQuestions);
            response.put("message", "Questions generated successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to generate questions: " + e.getMessage());
        }
    }
    
    @GetMapping("/templates")
    public ResponseEntity<?> getExamTemplates() {
        Map<String, Object> templates = new HashMap<>();
        
        Map<String, Object> javaTemplate = new HashMap<>();
        javaTemplate.put("title", "Java Programming Basics");
        javaTemplate.put("description", "Test your knowledge of Java fundamentals");
        javaTemplate.put("duration", 60);
        javaTemplate.put("totalMarks", 100);
        javaTemplate.put("topics", Arrays.asList("OOP", "Variables", "Methods", "Classes"));
        
        Map<String, Object> pythonTemplate = new HashMap<>();
        pythonTemplate.put("title", "Python Programming");
        pythonTemplate.put("description", "Python syntax and concepts");
        pythonTemplate.put("duration", 45);
        pythonTemplate.put("totalMarks", 75);
        pythonTemplate.put("topics", Arrays.asList("Syntax", "Data Types", "Functions", "Modules"));
        
        templates.put("java", javaTemplate);
        templates.put("python", pythonTemplate);
        
        return ResponseEntity.ok(templates);
    }
}