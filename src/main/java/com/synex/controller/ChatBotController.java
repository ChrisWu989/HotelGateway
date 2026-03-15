package com.synex.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.synex.service.ChatbotService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*")
public class ChatBotController {
    
    @Autowired
    private ChatbotService chatbotService;
    
    // Chat endpoint - send message, get AI response
    // POST /api/chatbot/chat
    // Body: { "message": "I need a luxury hotel in New York" }
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        String sessionId = request.get("sessionId");
        String message = request.get("message");
        
        // Generate sessionId if not provided
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = "session-" + System.currentTimeMillis();
        }
        
        String botResponse = chatbotService.chat(sessionId, message);  // Pass sessionId!
        
        Map<String, String> response = new HashMap<>();
        response.put("sessionId", sessionId);  // Return sessionId to frontend
        response.put("userMessage", message);
        response.put("botResponse", botResponse);
        
        return ResponseEntity.ok(response);
    }
    
    // clears chat history
    @PostMapping("/clear/{sessionId}")
    public ResponseEntity<String> clearHistory(@PathVariable String sessionId) {
        chatbotService.clearHistory(sessionId);
        return ResponseEntity.ok("History cleared for session: " + sessionId);
    }
}