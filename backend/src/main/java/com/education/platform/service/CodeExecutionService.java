package com.education.platform.service;

import com.education.platform.dto.*;
import com.education.platform.model.Question;
import com.education.platform.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.net.ssl.*;
//import java.net.HttpsURLConnection;
import java.security.cert.X509Certificate;
import java.util.*;

@Service
public class CodeExecutionService {
    
    @Autowired
    private QuestionRepository questionRepository;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public CodeExecutionService() {
        this.restTemplate = createRestTemplate();
    }
    
    private RestTemplate createRestTemplate() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };
            
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            
            return new RestTemplate();
        } catch (Exception e) {
            return new RestTemplate();
        }
    }
    private final String PISTON_API_URL = "https://emkc.org/api/v2/piston/execute";
    
    private final Map<String, String> languageVersions = Map.of(
        "java", "15.0.2",
        "python", "3.10.0",
        "cpp", "10.2.0",
        "c", "10.2.0",
        "javascript", "18.15.0"
    );

    public CodeResponse executeCode(CodeRequest request) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("language", request.getLanguage());
            payload.put("version", languageVersions.get(request.getLanguage()));
            
            Map<String, String> file = new HashMap<>();
            file.put("name", getFileName(request.getLanguage()));
            file.put("content", request.getCode());
            
            payload.put("files", List.of(file));
            
            if (request.getInput() != null && !request.getInput().isEmpty()) {
                payload.put("stdin", request.getInput());
            }
            
            System.out.println("Piston API Payload: " + payload);
            Map response = restTemplate.postForObject(PISTON_API_URL, payload, Map.class);
            System.out.println("Piston API Response: " + response);
            Map run = (Map) response.get("run");
            
            return new CodeResponse(
                (String) run.get("stdout"),
                (String) run.get("stderr"),
                (Integer) run.get("code"),
                (Integer) run.get("code") == 0
            );
        } catch (Exception e) {
            return new CodeResponse("", "Execution failed: " + e.getMessage(), 1, false);
        }
    }
    
    public EvaluationResult evaluateTestCases(Long questionId, CodeRequest request) {
        try {
            System.out.println("Evaluating question ID: " + questionId);
            Question question = questionRepository.findById(questionId).orElse(null);
            if (question == null) {
                System.out.println("Question not found for ID: " + questionId);
                return new EvaluationResult(0, 0, new ArrayList<>());
            }
            if (question.getTestCases() == null) {
                System.out.println("No test cases found for question: " + questionId);
                return new EvaluationResult(0, 0, new ArrayList<>());
            }
            System.out.println("Test cases JSON: " + question.getTestCases());
            
            List<Map<String, String>> testCases = objectMapper.readValue(
                question.getTestCases(), 
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            
            List<TestCaseResult> results = new ArrayList<>();
            int passedCount = 0;
            
            for (Map<String, String> testCase : testCases) {
                String input = testCase.get("input");
                String expectedOutput = testCase.get("output");
                
                CodeRequest testRequest = new CodeRequest();
                testRequest.setCode(request.getCode());
                testRequest.setLanguage(request.getLanguage());
                testRequest.setInput(input);
                
                CodeResponse response = executeCode(testRequest);
                String actualOutput = response.getStdout().trim();
                boolean passed = actualOutput.equals(expectedOutput.trim());
                
                if (passed) passedCount++;
                
                results.add(new TestCaseResult(input, expectedOutput, actualOutput, passed));
            }
            
            return new EvaluationResult(testCases.size(), passedCount, results);
        } catch (Exception e) {
            return new EvaluationResult(0, 0, new ArrayList<>());
        }
    }
    
    private String getFileName(String language) {
        return switch (language) {
            case "java" -> "Main.java";
            case "python" -> "main.py";
            case "cpp" -> "main.cpp";
            case "c" -> "main.c";
            case "javascript" -> "main.js";
            default -> "main.txt";
        };
    }
}