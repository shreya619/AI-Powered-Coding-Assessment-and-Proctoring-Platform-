package com.education.platform.service;

import com.education.platform.model.*;
import com.education.platform.repository.*;
import com.education.platform.config.BedrockAIConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Service
public class ProctoringService {
    
    @Autowired
    private BedrockAIConfig.AIService aiService;
    
    public ProctoringSession startProctoringSession(ExamResult examResult) {
        ProctoringSession session = new ProctoringSession(examResult);
        session.setAlerts(new ArrayList<>());
        return session;
    }
    
    public void recordSuspiciousActivity(ProctoringSession session, String activity) {
        session.getAlerts().add(LocalDateTime.now() + ": " + activity);
        session.setSuspiciousActivityCount(session.getSuspiciousActivityCount() + 1);
        
        if (session.getSuspiciousActivityCount() >= 3) {
            session.setStatus(ProctoringSession.Status.FLAGGED);
        }
    }
    
    public Map<String, Object> analyzeProctoringData(ProctoringSession session) {
        Map<String, Object> analysis = new HashMap<>();
        
        // AI-powered analysis
        String analysisPrompt = String.format(
            "Analyze proctoring session: %d alerts, %d suspicious activities. Alerts: %s",
            session.getAlerts().size(),
            session.getSuspiciousActivityCount(),
            String.join(", ", session.getAlerts())
        );
        
        String aiAnalysis = aiService.analyzePerformance(analysisPrompt);
        session.setAiAnalysisReport(aiAnalysis);
        
        analysis.put("suspiciousActivityCount", session.getSuspiciousActivityCount());
        analysis.put("alerts", session.getAlerts());
        analysis.put("status", session.getStatus());
        analysis.put("aiAnalysis", aiAnalysis);
        analysis.put("riskLevel", calculateRiskLevel(session));
        
        return analysis;
    }
    
    private String calculateRiskLevel(ProctoringSession session) {
        int count = session.getSuspiciousActivityCount();
        if (count == 0) return "LOW";
        if (count <= 2) return "MEDIUM";
        return "HIGH";
    }
    
    public void endProctoringSession(ProctoringSession session) {
        session.setEndTime(LocalDateTime.now());
        session.setStatus(ProctoringSession.Status.COMPLETED);
    }
    
    public List<String> detectCheatingPatterns(List<String> activities) {
        List<String> patterns = new ArrayList<>();
        
        // Simple pattern detection
        long tabSwitches = activities.stream()
            .filter(activity -> activity.contains("tab switch"))
            .count();
        
        if (tabSwitches > 5) {
            patterns.add("Excessive tab switching detected");
        }
        
        long faceNotVisible = activities.stream()
            .filter(activity -> activity.contains("face not visible"))
            .count();
        
        if (faceNotVisible > 3) {
            patterns.add("Face frequently not visible");
        }
        
        return patterns;
    }
}