package com.education.platform.dto;

import java.util.List;

public class EvaluationResult {
    private int totalTests;
    private int passedTests;
    private List<TestCaseResult> testResults;
    private boolean allPassed;

    public EvaluationResult() {}

    public EvaluationResult(int totalTests, int passedTests, List<TestCaseResult> testResults) {
        this.totalTests = totalTests;
        this.passedTests = passedTests;
        this.testResults = testResults;
        this.allPassed = totalTests == passedTests;
    }

    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }
    
    public int getPassedTests() { return passedTests; }
    public void setPassedTests(int passedTests) { this.passedTests = passedTests; }
    
    public List<TestCaseResult> getTestResults() { return testResults; }
    public void setTestResults(List<TestCaseResult> testResults) { this.testResults = testResults; }
    
    public boolean isAllPassed() { return allPassed; }
    public void setAllPassed(boolean allPassed) { this.allPassed = allPassed; }
}