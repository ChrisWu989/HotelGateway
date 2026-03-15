package com.synex.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.synex.entity.Hotel;
import com.synex.repository.HotelRepository;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

@Service
@Transactional
public class EmbeddingService {
    
    @Autowired
    private HotelRepository hotelRepository;
    
    @Autowired
    private EmbeddingModel embeddingModel;
    
    @Autowired
    private VectorStore vectorStore;
    
    
    // Generate and store embeddings for all hotels
    public void generateHotelEmbeddings() {
        System.out.println("Starting embedding generation for all hotels...");
        
        List<Hotel> hotels = hotelRepository.findAll();
        List<Document> documents = new ArrayList<>();
        
        for (Hotel hotel : hotels) {
            // Create text content from hotel data
            String content = buildHotelContent(hotel);
            
            // Create metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("hotelId", hotel.getHotelId());
            metadata.put("hotelName", hotel.getHotelName());
            metadata.put("city", hotel.getCity());
            metadata.put("state", hotel.getState());
            metadata.put("starRating", hotel.getStarRating());
            metadata.put("averagePrice", hotel.getAveragePrice());
            
            // Create document
            String documentId = UUID.randomUUID().toString();
            Document document = new Document(
                documentId, 
                content, 
                metadata
            );
            
            documents.add(document);
        }
        
        // Store all documents with embeddings in vector database
        vectorStore.add(documents);
        
        System.out.println("Successfully generated embeddings for " + hotels.size() + " hotels!");
    }
    
    
    // Generate embedding for a single hotel
    public void generateHotelEmbedding(int hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
            .orElseThrow(() -> new RuntimeException("Hotel not found: " + hotelId));
        
        String content = buildHotelContent(hotel);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("hotelId", hotel.getHotelId());
        metadata.put("hotelName", hotel.getHotelName());
        metadata.put("city", hotel.getCity());
        metadata.put("state", hotel.getState());
        metadata.put("starRating", hotel.getStarRating());
        metadata.put("averagePrice", hotel.getAveragePrice());
        
        String documentId = UUID.randomUUID().toString();
        Document document = new Document(
            documentId,
            content, 
            metadata
        );
        
        vectorStore.add(List.of(document));
        
        System.out.println("Generated embedding for: " + hotel.getHotelName());
    }
    
    // Build searchable text content from hotel data
    private String buildHotelContent(Hotel hotel) {
        StringBuilder content = new StringBuilder();
        
        // Add hotel name
        content.append("Hotel Name: ").append(hotel.getHotelName()).append(". ");
        
        // Add description
        if (hotel.getDescription() != null && !hotel.getDescription().isEmpty()) {
            content.append(hotel.getDescription()).append(" ");
        }
        
        // Add location
        content.append("Located in ").append(hotel.getCity()).append(", ")
               .append(hotel.getState()).append(". ");
        
        // Add star rating
        content.append(hotel.getStarRating()).append("-star hotel. ");
        
        // Add price range
        content.append("Average price: $").append(hotel.getAveragePrice()).append(" per night. ");
        
        // Add amenities
        if (!hotel.getAmenities().isEmpty()) {
            content.append("Amenities include: ");
            hotel.getAmenities().forEach(amenity -> 
                content.append(amenity.getName()).append(", ")
            );
        }
        
        return content.toString();
    }
    
    // Search similar hotels using semantic search
    public List<Document> searchSimilarHotels(String query, int limit) {
        SearchRequest request = SearchRequest.query(query).withTopK(limit);
        return vectorStore.similaritySearch(request);
    }
    
    // Delete embedding for a hotel
    public void deleteHotelEmbedding(int hotelId) {
        vectorStore.delete(List.of(String.valueOf(hotelId)));
        System.out.println("Deleted embedding for hotel ID: " + hotelId);
    }
}