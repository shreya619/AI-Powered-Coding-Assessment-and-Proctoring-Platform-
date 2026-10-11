package com.education.platform.controller;

import com.education.platform.model.ProctoringSession;
import com.education.platform.service.ProctoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/proctoring")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class ProctoringController {
    
    @Autowired
    private ProctoringService proctoringService;
    
    @PostMapping("/sessions/{sessionId}/alert")
    public ResponseEntity<?> reportSuspiciousActivity(
            @PathVariable Long sessionId, 
            @RequestBody Map<String, String> request) {
        try {
            String activity = request.get("activity");
            // In real implementation, fetch session from database
            // proctoringService.recordSuspiciousActivity(session, activity);
            return ResponseEntity.ok("Alert recorded");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to record alert: " + e.getMessage());
        }
    }
    
    @GetMapping("/sessions/{sessionId}/analysis")
    public ResponseEntity<?> getProctoringAnalysis(@PathVariable Long sessionId) {
        try {
            // In real implementation, fetch session from database
            // Map<String, Object> analysis = proctoringService.analyzeProctoringData(session);
            return ResponseEntity.ok("Analysis data");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get analysis: " + e.getMessage());
        }
    }
    
    @PostMapping("/sessions/{sessionId}/end")
    public ResponseEntity<?> endProctoringSession(@PathVariable Long sessionId) {
        try {
            // In real implementation, fetch session from database and end it
            return ResponseEntity.ok("Session ended");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to end session: " + e.getMessage());
        }
    }
}