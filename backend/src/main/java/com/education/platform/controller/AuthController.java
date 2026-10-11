package com.education.platform.controller;

import com.education.platform.model.User;
import com.education.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class AuthController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            String firstName = request.get("firstName");
            String lastName = request.get("lastName");
            String role = request.get("role");
            
            if (userRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body("Email already exists");
            }
            
            User user = new User(email, passwordEncoder.encode(password), 
                               firstName, lastName, User.Role.valueOf(role.toUpperCase()));
            user = userRepository.save(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", user.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            System.out.println("Login attempt for email: " + email);
            
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                System.out.println("User found: " + user.getEmail() + ", Role: " + user.getRole());
                System.out.println("Password match: " + passwordEncoder.matches(password, user.getPassword()));
                
                if (passwordEncoder.matches(password, user.getPassword())) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Login successful");
                    
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("email", user.getEmail());
                    userMap.put("firstName", user.getFirstName());
                    userMap.put("lastName", user.getLastName());
                    userMap.put("role", user.getRole());
                    userMap.put("learningStyle", user.getLearningStyle());
                    userMap.put("totalPoints", user.getTotalPoints());
                    userMap.put("averageScore", user.getAverageScore());
                    
                    response.put("user", userMap);
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.badRequest().body("Password does not match");
                }
            } else {
                System.out.println("User not found for email: " + email);
                return ResponseEntity.badRequest().body("User not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }
}