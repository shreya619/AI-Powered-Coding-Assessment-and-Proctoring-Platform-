package com.education.platform.service;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class AIRecommendationService {
    
    private BedrockRuntimeClient bedrockClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public AIRecommendationService(@Value("${spring.ai.bedrock.aws.access-key}") String accessKey,
                                 @Value("${spring.ai.bedrock.aws.secret-key}") String secretKey,
                                 @Value("${spring.ai.bedrock.aws.region}") String region) {
        try {
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
            
            this.bedrockClient = BedrockRuntimeClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
            
            System.out.println("✅ AWS Bedrock client initialized with region: " + region);
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize Bedrock client: " + e.getMessage());
            this.bedrockClient = null;
            System.out.println("⚠️ Running in fallback mode without AI");
        }
    }
    
    public String generatePersonalizedRecommendations(List<Map<String, Object>> answers, Double score, Integer totalMarks, String examTitle) {
        System.out.println("🚀 Generating recommendations for " + examTitle);
        double percentage = (score / totalMarks) * 100;
        
        if (percentage >= 100) {
            return generatePerfectScoreFeedback(examTitle, score, totalMarks);
        }
        
        List<Map<String, Object>> wrongAnswers = answers.stream()
            .filter(answer -> !(Boolean) answer.get("isCorrect"))
            .collect(Collectors.toList());
        
        if (bedrockClient != null && !wrongAnswers.isEmpty()) {
            try {
                return generateRealAIRecommendations(wrongAnswers, percentage, examTitle);
            } catch (Exception e) {
                System.err.println("❌ AI generation failed, using fallback: " + e.getMessage());
            }
        }
        
        return generateBasicFallback(answers, score, totalMarks, examTitle);
    }
    
    private String generateBasicFallback(List<Map<String, Object>> answers, Double score, Integer totalMarks, String examTitle) {
        StringBuilder recommendations = new StringBuilder();
        double percentage = (score / totalMarks) * 100;
        
        recommendations.append(String.format("🎯 **%s - Performance Analysis**\n\n", examTitle));
        recommendations.append(String.format("📊 **Your Score:** %.0f out of %.0f marks (%.1f%%)\n\n", score, (double)totalMarks, percentage));
        
        List<Map<String, Object>> wrongAnswers = answers.stream()
            .filter(answer -> !(Boolean) answer.get("isCorrect"))
            .collect(Collectors.toList());
        
        if (!wrongAnswers.isEmpty()) {
            recommendations.append("🔍 **Questions to Review:**\n\n");
            for (int i = 0; i < wrongAnswers.size(); i++) {
                Map<String, Object> answer = wrongAnswers.get(i);
                recommendations.append(String.format("**Question %d:** %s\n", i + 1, answer.get("questionText")));
                recommendations.append(String.format("❌ **Your Answer:** %s\n", answer.get("userAnswer")));
                recommendations.append(String.format("✅ **Correct Answer:** %s\n", answer.get("correctAnswer")));
                recommendations.append(String.format("💡 **Learning Tip:** Review the concept of %s and practice similar questions.\n\n", answer.get("topic")));
            }
        } else {
            recommendations.append("✅ **Perfect Score!** You answered all questions correctly.\n\n");
        }
        
        recommendations.append("🎓 **Study Recommendations:**\n");
        if (percentage >= 90) {
            recommendations.append("• 🎆 Outstanding performance! You've mastered the material.\n");
            recommendations.append("• 🚀 Consider exploring advanced topics in this subject.\n");
            recommendations.append("• 👥 Help other students to reinforce your learning.\n");
        } else if (percentage >= 80) {
            recommendations.append("• 🎉 Great job! You have a solid understanding.\n");
            recommendations.append("• 📝 Review the questions you missed for complete mastery.\n");
            recommendations.append("• 📚 Practice more challenging problems in weak areas.\n");
        } else if (percentage >= 60) {
            recommendations.append("• 👍 Good effort! You're on the right track.\n");
            recommendations.append("• 📚 Focus extra time on the topics you missed.\n");
            recommendations.append("• 🔄 Retake practice tests to improve your score.\n");
        } else {
            recommendations.append("• 📚 Review the fundamental concepts thoroughly.\n");
            recommendations.append("• 📝 Take notes while studying and create summaries.\n");
            recommendations.append("• 👥 Consider getting help from teachers or study groups.\n");
        }
        
        if (percentage < 100) {
            recommendations.append("\n📖 **Topic-Specific Improvement Areas:**\n");
            recommendations.append(generateTopicImprovements(wrongAnswers, examTitle));
        }
        
        recommendations.append("\n🎯 **Keep practicing and you'll improve! Every attempt is a step toward mastery.**");
        
        return recommendations.toString();
    }
    
    private String generatePerfectScoreFeedback(String examTitle, Double score, Integer totalMarks) {
        return String.format("🎯 **%s - Performance Analysis**\n\n" +
            "📊 **Your Score:** %.0f out of %.0f marks (100%%)\n\n" +
            "✅ **Perfect Score!** You answered all questions correctly.\n\n" +
            "🎓 **Study Recommendations:**\n" +
            "• 🎆 Outstanding performance! You've mastered the material.\n" +
            "• 🚀 Consider exploring advanced topics in this subject.\n" +
            "• 👥 Help other students to reinforce your learning.\n" +
            "• 📚 Challenge yourself with more complex problems.\n\n" +
            "🎯 **Excellent work! Keep up the great performance!**",
            examTitle, score, (double)totalMarks);
    }
    
    private String generateTopicImprovements(List<Map<String, Object>> wrongAnswers, String examTitle) {
        StringBuilder improvements = new StringBuilder();
        
        Map<String, Long> topicCounts = wrongAnswers.stream()
            .collect(Collectors.groupingBy(
                answer -> (String) answer.get("topic"),
                Collectors.counting()
            ));
        
        topicCounts.forEach((topic, count) -> {
            improvements.append(String.format("• **%s**: %d question(s) incorrect - ", topic, count));
            improvements.append(getTopicSpecificTips(topic, examTitle));
            improvements.append("\n");
        });
        
        return improvements.toString();
    }
    
    private String getTopicSpecificTips(String topic, String examTitle) {
        String subject = examTitle.toLowerCase();
        
        if (subject.contains("java")) {
            return getJavaTips(topic);
        } else if (subject.contains("python")) {
            return getPythonTips(topic);
        } else if (subject.contains("javascript") || subject.contains("js")) {
            return getJavaScriptTips(topic);
        } else if (subject.contains("sql") || subject.contains("database")) {
            return getSQLTips(topic);
        }
        
        return "Review core concepts and practice more examples.";
    }
    
    private String getJavaTips(String topic) {
        String topicLower = topic.toLowerCase();
        if (topicLower.contains("oop") || topicLower.contains("object")) {
            return "Study classes, objects, inheritance, polymorphism. Practice creating class hierarchies.";
        } else if (topicLower.contains("collection") || topicLower.contains("list") || topicLower.contains("map")) {
            return "Master ArrayList, HashMap, HashSet. Understand when to use each collection type.";
        } else if (topicLower.contains("exception")) {
            return "Learn try-catch blocks, custom exceptions, and exception hierarchy.";
        } else if (topicLower.contains("thread") || topicLower.contains("concurrent")) {
            return "Study Thread class, Runnable interface, synchronization, and ExecutorService.";
        } else if (topicLower.contains("stream") || topicLower.contains("lambda")) {
            return "Practice lambda expressions, Stream API operations (map, filter, reduce).";
        }
        return "Review Java fundamentals and practice coding exercises.";
    }
    
    private String getPythonTips(String topic) {
        String topicLower = topic.toLowerCase();
        if (topicLower.contains("list") || topicLower.contains("dict")) {
            return "Master list comprehensions, dictionary operations, and built-in methods.";
        } else if (topicLower.contains("function") || topicLower.contains("lambda")) {
            return "Practice defining functions, lambda expressions, and decorators.";
        } else if (topicLower.contains("class") || topicLower.contains("oop")) {
            return "Study class definitions, inheritance, magic methods (__init__, __str__).";
        }
        return "Review Python syntax and practice coding problems.";
    }
    
    private String getJavaScriptTips(String topic) {
        String topicLower = topic.toLowerCase();
        if (topicLower.contains("async") || topicLower.contains("promise")) {
            return "Study Promises, async/await, and callback functions.";
        } else if (topicLower.contains("dom")) {
            return "Practice DOM manipulation, event listeners, and querySelector methods.";
        } else if (topicLower.contains("array") || topicLower.contains("object")) {
            return "Master array methods (map, filter, reduce) and object destructuring.";
        }
        return "Review JavaScript fundamentals and ES6+ features.";
    }
    
    private String getSQLTips(String topic) {
        String topicLower = topic.toLowerCase();
        if (topicLower.contains("join")) {
            return "Practice INNER JOIN, LEFT JOIN, RIGHT JOIN with multiple tables.";
        } else if (topicLower.contains("aggregate") || topicLower.contains("group")) {
            return "Study GROUP BY, HAVING, COUNT, SUM, AVG functions.";
        } else if (topicLower.contains("subquery")) {
            return "Practice nested queries and correlated subqueries.";
        }
        return "Review SQL syntax and practice query writing.";
    }
    

    
    private String generateRealAIRecommendations(List<Map<String, Object>> wrongAnswers, double percentage, String examTitle) {
        String aiPrompt = buildAIPrompt(wrongAnswers, percentage, examTitle);
        
        try {
            // Prepare Claude 3 request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("anthropic_version", "bedrock-2023-05-31");
            requestBody.put("max_tokens", 1000);
            
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", aiPrompt);
            requestBody.put("messages", List.of(message));
            
            String jsonRequest = objectMapper.writeValueAsString(requestBody);
            
            InvokeModelRequest request = InvokeModelRequest.builder()
                .modelId("anthropic.claude-3-sonnet-20240229-v1:0")
                .body(SdkBytes.fromUtf8String(jsonRequest))
                .build();
            
            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String responseBody = response.body().asUtf8String();
            
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            return jsonResponse.path("content").get(0).path("text").asText();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Bedrock AI", e);
        }
    }
    
    private String buildAIPrompt(List<Map<String, Object>> wrongAnswers, double percentage, String examTitle) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an expert educational AI tutor specializing in personalized learning recommendations. ");
        prompt.append("Analyze the following student exam performance and provide detailed, encouraging, and actionable feedback.\n\n");
        
        prompt.append("EXAM DETAILS:\n");
        prompt.append("- Exam Title: ").append(examTitle).append("\n");
        prompt.append("- Final Score: ").append(String.format("%.1f%%", percentage)).append("\n");
        prompt.append("- Number of Incorrect Answers: ").append(wrongAnswers.size()).append("\n\n");
        
        if (!wrongAnswers.isEmpty()) {
            prompt.append("INCORRECT ANSWERS ANALYSIS:\n");
            for (int i = 0; i < wrongAnswers.size(); i++) {
                Map<String, Object> answer = wrongAnswers.get(i);
                prompt.append(String.format("%d. QUESTION: %s\n", i + 1, answer.get("questionText")));
                prompt.append(String.format("   STUDENT'S ANSWER: %s\n", answer.get("userAnswer")));
                prompt.append(String.format("   CORRECT ANSWER: %s\n", answer.get("correctAnswer")));
                prompt.append(String.format("   TOPIC AREA: %s\n\n", answer.get("topic")));
            }
        }
        
        prompt.append("REQUIREMENTS FOR YOUR RESPONSE:\n");
        prompt.append("1. Start with an encouraging performance assessment\n");
        prompt.append("2. For each incorrect answer, provide:\n");
        prompt.append("   - Clear explanation of why the correct answer is right\n");
        prompt.append("   - Specific learning tip to remember this concept\n");
        prompt.append("   - Study strategy for this topic\n");
        prompt.append("3. Give overall study recommendations based on patterns\n");
        prompt.append("4. End with motivational encouragement\n\n");
        
        prompt.append("FORMAT REQUIREMENTS:\n");
        prompt.append("- Use emojis and **bold** formatting for better readability\n");
        prompt.append("- Structure each wrong answer analysis clearly\n");
        prompt.append("- Be specific and actionable in your recommendations\n");
        prompt.append("- Maintain an encouraging and supportive tone throughout\n");
        prompt.append("- Focus on understanding concepts, not just memorization\n");
        
        return prompt.toString();
    }
    
    private String generateAIOnlyFallback(List<Map<String, Object>> wrongAnswers, double percentage, String examTitle) {
        StringBuilder recommendations = new StringBuilder();
        
        if (!wrongAnswers.isEmpty()) {
            for (int i = 0; i < wrongAnswers.size(); i++) {
                Map<String, Object> answer = wrongAnswers.get(i);
                recommendations.append(String.format("**Question %d:** %s\n", i + 1, answer.get("questionText")));
                recommendations.append(String.format("❌ Your answer: \"%s\"\n", answer.get("userAnswer")));
                recommendations.append(String.format("✅ Correct answer: \"%s\"\n", answer.get("correctAnswer")));
                
                String aiTip = getAITip((String) answer.get("topic"), (String) answer.get("questionText"));
                if (!aiTip.isEmpty()) {
                    recommendations.append(String.format("💡 **AI Tip:** %s\n\n", aiTip));
                } else {
                    recommendations.append("\n");
                }
            }
        }
        
        return recommendations.toString();
    }
    
    private String getAITip(String topic, String question) {
        try {
            System.out.println("🔍 Generating AI tip for: " + question.substring(0, Math.min(50, question.length())) + "...");
            String tip = generateAITip(topic, question);
            System.out.println("✅ AI tip generated: " + tip.substring(0, Math.min(100, tip.length())) + "...");
            return tip;
        } catch (Exception e) {
            System.err.println("❌ AI tip failed for question: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }
    
    private String generateAITip(String topic, String question) {
        String prompt = "You are an expert educational tutor. For this question: '" + question + 
                       "' in topic '" + topic + 
                       "', provide a clear explanation of the correct answer and why it's correct. " +
                       "Include a practical learning tip to help remember this concept. " +
                       "Keep it concise but informative (2-3 sentences max).";
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("anthropic_version", "bedrock-2023-05-31");
            requestBody.put("max_tokens", 200);
            
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            requestBody.put("messages", List.of(message));
            
            String jsonRequest = objectMapper.writeValueAsString(requestBody);
            
            InvokeModelRequest request = InvokeModelRequest.builder()
                .modelId("anthropic.claude-3-sonnet-20240229-v1:0")
                .body(SdkBytes.fromUtf8String(jsonRequest))
                .build();
            
            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String responseBody = response.body().asUtf8String();
            
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            return jsonResponse.path("content").get(0).path("text").asText().trim();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate AI tip", e);
        }
    }
    
    
    
    public String generateExamInsights(String examTitle, List<Map<String, Object>> answers, Double score) {
        try {
            double percentage = score;
           
            // 1. Extract all the questions student saw
            List<String> questions = answers.stream()
                .map(a -> (String) a.get("questionText"))
                .toList();

            // 2. Extract wrong answers for topic insights
            List<Map<String, Object>> wrong = answers.stream()
                .filter(a -> !(Boolean) a.get("isCorrect"))
                .toList();

            // 3. Prepare prompt for Bedrock
            Map<String, Object> promptBody = Map.of(
                "prompt",
                """
                You are an AI education expert.
                Analyze the following exam:

                Exam Title: %s

                Questions:
                %s

                Wrong Answers:
                %s

                Student Score: %.2f

                Your tasks:
                1. Identify what subject the exam belongs to (e.g., Java, Python, DBMS, Cybersecurity).
                2. Extract main concepts/topics asked in the exam.
                3. Identify the student's weak topics based on wrong answers.
                4. If the score is below 90%% → give actionable tips to improve.
                5. Keep it simple and student-friendly.

                Respond in this JSON format:
                {
                   "examType": "",
                   "detectedTopics": [],
                   "weakTopics": [],
                   "studyTips": ""
                }
                """.formatted(
                    examTitle,
                    questions.toString(),
                    wrong.toString(),
                    percentage
                )
            );

            String bodyJson = objectMapper.writeValueAsString(promptBody);

            InvokeModelRequest request = InvokeModelRequest.builder()
                .modelId("anthropic.claude-3-5-sonnet") // Bedrock model
                .contentType("application/json")
                .accept("application/json")
                .body(SdkBytes.fromUtf8String(bodyJson))
                .build();

            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String responseBody = response.body().asUtf8String();

            JsonNode jsonResponse = objectMapper.readTree(responseBody);

            return jsonResponse.toPrettyString();

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"AI processing failed, using fallback.\"}";
        }
    }

}