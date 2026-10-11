package com.education.platform.service;

import com.education.platform.model.*;
import com.education.platform.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Arrays;

@Service
public class DataInitializationService implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ExamRepository examRepository;
    
    @Autowired
    private ExamResultRepository examResultRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        initializeDefaultData();
    }
    
    private void initializeDefaultData() {
        if (userRepository.count() == 0) {
            User student = new User("student@example.com", 
                                  passwordEncoder.encode("password123"), 
                                  "John", "Doe", User.Role.STUDENT);
            student.setLearningStyle(User.LearningStyle.VISUAL);
            student.setTotalPoints(150);
            student.setAverageScore(85.5);
            userRepository.save(student);
            
            User teacher = new User("teacher@example.com", 
                                  passwordEncoder.encode("password123"), 
                                  "Jane", "Smith", User.Role.TEACHER);
            userRepository.save(teacher);
            
            User student2 = new User("alice@example.com", 
                                   passwordEncoder.encode("password123"), 
                                   "Alice", "Johnson", User.Role.STUDENT);
            student2.setTotalPoints(200);
            student2.setAverageScore(92.0);
            userRepository.save(student2);
            
            User student3 = new User("bob@example.com", 
                                   passwordEncoder.encode("password123"), 
                                   "Bob", "Wilson", User.Role.STUDENT);
            student3.setTotalPoints(120);
            student3.setAverageScore(78.5);
            userRepository.save(student3);
            
            User student4 = new User("charlie@example.com", 
                                   passwordEncoder.encode("password123"), 
                                   "Charlie", "Brown", User.Role.STUDENT);
            student4.setTotalPoints(180);
            student4.setAverageScore(88.0);
            userRepository.save(student4);
            
            User student5 = new User("diana@example.com", 
                                   passwordEncoder.encode("password123"), 
                                   "Diana", "Davis", User.Role.STUDENT);
            student5.setTotalPoints(95);
            student5.setAverageScore(72.3);
            userRepository.save(student5);
            
            User student6 = new User("eve@example.com", 
                                   passwordEncoder.encode("password123"), 
                                   "Eve", "Miller", User.Role.STUDENT);
            student6.setTotalPoints(220);
            student6.setAverageScore(94.5);
            userRepository.save(student6);
            
            // Using existing exams from database
            
            System.out.println("Default data initialized successfully!");
        }
    }
    

}