package com.education.platform.controller;

import com.education.platform.model.Exam;
import com.education.platform.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class TestController {
    
    @Autowired
    private ExamService examService;
    
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Backend is running");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/exams-count")
    public ResponseEntity<?> getExamsCount() {
        try {
            List<Exam> exams = examService.getActiveExams();
            Map<String, Object> response = new HashMap<>();
            response.put("count", exams.size());
            response.put("exams", exams);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}