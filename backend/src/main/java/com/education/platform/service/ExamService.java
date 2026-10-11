package com.education.platform.service;

import com.education.platform.model.*;
import com.education.platform.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ExamService {
    
    @Autowired
    private ExamRepository examRepository;
    
    @Autowired
    private ExamResultRepository examResultRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GamificationService gamificationService;
    
    public List<Exam> getActiveExams() {
        System.out.println("[SERVICE] ExamService.getActiveExams() - Fetching active exams");
        List<Exam> exams = examRepository.findByIsActiveTrue();
        System.out.println("[SERVICE] ExamService.getActiveExams() - Found " + exams.size() + " active exams");
        return exams;
    }
    
    public Exam getExamById(Long examId) {
        System.out.println("[SERVICE] ExamService.getExamById() - Fetching exam with ID: " + examId);
        Optional<Exam> examOpt = examRepository.findById(examId);
        if (examOpt.isPresent()) {
            Exam exam = examOpt.get();
            // Force load questions
            if (exam.getQuestions() != null) {
                exam.getQuestions().size();
            }
            System.out.println("[SERVICE] ExamService.getExamById() - Found exam: " + exam.getTitle() + " with " + (exam.getQuestions() != null ? exam.getQuestions().size() : 0) + " questions");
            return exam;
        } else {
            System.out.println("[SERVICE] ExamService.getExamById() - Exam not found with ID: " + examId);
            throw new RuntimeException("Exam not found");
        }
    }
    
    public Exam createExam(Exam exam) {
        System.out.println("[SERVICE] ExamService.createExam() - Creating exam: " + exam.getTitle());
        if (exam.getQuestions() != null && !exam.getQuestions().isEmpty()) {
            int totalMarks = exam.getQuestions().stream()
                .mapToInt(q -> q.getMarks() != null ? q.getMarks() : 0)
                .sum();
            exam.setTotalMarks(totalMarks);
        }
        Exam savedExam = examRepository.save(exam);
        System.out.println("[SERVICE] ExamService.createExam() - Created exam with ID: " + savedExam.getId());
        return savedExam;
    }
    
    public ExamResult startExam(Long examId, Long userId) {
        System.out.println("[SERVICE] ExamService.startExam() - Starting exam ID: " + examId + " for user ID: " + userId);
        Optional<Exam> examOpt = examRepository.findById(examId);
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (examOpt.isPresent() && userOpt.isPresent()) {
            Exam exam = examOpt.get();
            User user = userOpt.get();
            
            Optional<ExamResult> existingResult = examResultRepository.findByUserIdAndExamIdAndIsCompleted(userId, examId, false);
            if (existingResult.isPresent()) {
                System.out.println("[SERVICE] ExamService.startExam() - Returning existing incomplete result ID: " + existingResult.get().getId());
                return existingResult.get();
            }
            
            ExamResult result = new ExamResult(user, exam);
            ExamResult savedResult = examResultRepository.save(result);
            System.out.println("[SERVICE] ExamService.startExam() - Created new exam result ID: " + savedResult.getId());
            return savedResult;
        }
        
        System.out.println("[SERVICE] ExamService.startExam() - Exam or User not found");
        throw new RuntimeException("Exam or User not found");
    }
    
    public ExamResult submitExam(Long resultId, Double score) {
        System.out.println("[SERVICE] ExamService.submitExam() - Submitting exam result ID: " + resultId + " with score: " + score);
        Optional<ExamResult> resultOpt = examResultRepository.findById(resultId);
        
        if (resultOpt.isPresent()) {
            ExamResult result = resultOpt.get();
            result.setScore(score);
            result.setSubmittedAt(LocalDateTime.now());
            result.setIsCompleted(true);
            
            ExamResult savedResult = examResultRepository.save(result);
            System.out.println("[SERVICE] ExamService.submitExam() - Exam submitted successfully");
            
            updateUserAverageScore(result.getUser().getId());
            gamificationService.awardPointsForExam(result.getUser(), score);
            
            return savedResult;
        }
        
        System.out.println("[SERVICE] ExamService.submitExam() - Exam result not found with ID: " + resultId);
        throw new RuntimeException("Exam result not found");
    }
    
    private void updateUserAverageScore(Long userId) {
        Double averageScore = examResultRepository.findAverageScoreByUserId(userId);
        if (averageScore != null) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setAverageScore(averageScore);
                userRepository.save(user);
            }
        }
    }
    
    public List<ExamResult> getUserExamResults(Long userId) {
        System.out.println("[SERVICE] ExamService.getUserExamResults() - Fetching results for user ID: " + userId);
        List<ExamResult> results = examResultRepository.findByUserId(userId);
        System.out.println("[SERVICE] ExamService.getUserExamResults() - Found " + results.size() + " results");
        return results;
    }
    
    public List<Exam> getExamsByTeacher(Long teacherId) {
        System.out.println("[SERVICE] ExamService.getExamsByTeacher() - Fetching exams for teacher ID: " + teacherId);
        List<Exam> exams = examRepository.findByCreatedById(teacherId);
        System.out.println("[SERVICE] ExamService.getExamsByTeacher() - Found " + exams.size() + " exams");
        return exams;
    }
    
    public ExamResult saveExamResult(ExamResult examResult) {
        System.out.println("[SERVICE] ExamService.saveExamResult() - Saving exam result ID: " + examResult.getId());
        ExamResult savedResult = examResultRepository.save(examResult);
        System.out.println("[SERVICE] ExamService.saveExamResult() - Exam result saved successfully");
        return savedResult;
    }
}