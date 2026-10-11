package com.education.platform.controller;

import com.education.platform.dto.CodeRequest;
import com.education.platform.dto.CodeResponse;
import com.education.platform.service.CodeExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/code")
@CrossOrigin(origins = "http://localhost:4200")
public class CodeExecutionController {

    @Autowired
    private CodeExecutionService codeExecutionService;

    @PostMapping("/run")
    public ResponseEntity<CodeResponse> runCode(@RequestBody CodeRequest request) {
        CodeResponse response = codeExecutionService.executeCode(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/languages")
    public ResponseEntity<String[]> getSupportedLanguages() {
        String[] languages = {"java", "python", "cpp", "c", "javascript"};
        return ResponseEntity.ok(languages);
    }
    
    @PostMapping("/evaluate/{questionId}")
    public ResponseEntity<com.education.platform.dto.EvaluationResult> evaluateCode(
            @PathVariable Long questionId, 
            @RequestBody CodeRequest request) {
        com.education.platform.dto.EvaluationResult result = codeExecutionService.evaluateTestCases(questionId, request);
        return ResponseEntity.ok(result);
    }
}