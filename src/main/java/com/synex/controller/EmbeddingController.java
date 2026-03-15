package com.synex.controller;

import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.synex.service.EmbeddingService;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/embeddings")
@CrossOrigin(origins = "*")
public class EmbeddingController {
    
    @Autowired
    private EmbeddingService embeddingService;
    
    // Generate embeddings for all hotels in console (POST)
    @PostMapping("/generate-all")
    public ResponseEntity<Map<String, String>> generateAllEmbeddingsPost() {
        return generateAllEmbeddings();
    }
    
    // Generate embeddings for all hotels in browser (GET)
    @GetMapping("/generate-all")
    public ResponseEntity<Map<String, String>> generateAllEmbeddingsGet() {
        return generateAllEmbeddings();
    }
    
    private ResponseEntity<Map<String, String>> generateAllEmbeddings() {
        try {
            embeddingService.generateHotelEmbeddings();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Successfully generated embeddings for all hotels");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Generate embedding for a specific hotel in console (POST)
    @PostMapping("/generate/{hotelId}")
    public ResponseEntity<Map<String, String>> generateHotelEmbeddingPost(@PathVariable int hotelId) {
        return generateHotelEmbedding(hotelId);
    }
    
    // Generate embedding for a specific hotel in browser (GET)
    @GetMapping("/generate/{hotelId}")
    public ResponseEntity<Map<String, String>> generateHotelEmbeddingGet(@PathVariable int hotelId) {
        return generateHotelEmbedding(hotelId);
    }
    
    private ResponseEntity<Map<String, String>> generateHotelEmbedding(int hotelId) {
        try {
            embeddingService.generateHotelEmbedding(hotelId);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Successfully generated embedding for hotel " + hotelId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Search hotels using semantic search (GET)
    @GetMapping("/search")
    public ResponseEntity<List<Document>> searchSimilarHotels(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int limit) {
        
        List<Document> results = embeddingService.searchSimilarHotels(query, limit);
        return ResponseEntity.ok(results);
    }
    
    // Delete embedding for a hotel (DELETE)
    @DeleteMapping("/{hotelId}")
    public ResponseEntity<Map<String, String>> deleteEmbedding(@PathVariable int hotelId) {
        try {
            embeddingService.deleteHotelEmbedding(hotelId);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Successfully deleted embedding for hotel " + hotelId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}