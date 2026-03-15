package com.synex.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class ChatbotService {
    
    @Autowired
    private ChatClient chatClient;
    
    @Autowired
    private PDFEmbeddingService pdfEmbeddingService; // hotel pdfs
    
    @Autowired
    private EmbeddingService embeddingService; // hotel tables
    
    //conversation history
    // Store conversation history AND context per session
    private Map<String, ConversationContext> sessionContexts = new HashMap<>();
    
    // Inner class to store conversation state
    private static class ConversationContext {
        List<Message> messages = new ArrayList<>();
        String lastHotelMentioned = null;  // Track which hotel user is asking about
        List<String> recentHotels = new ArrayList<>();  // Track recent hotels discussed
    }
    
    /**
     * Main chat method with conversation memory
     */
    public String chat(String sessionId, String userMessage) {
        
        // Get or create conversation context
        ConversationContext context = sessionContexts.computeIfAbsent(
            sessionId, 
            k -> new ConversationContext()
        );
        
        // Search hotel database
        List<Document> hotelResults = embeddingService.searchSimilarHotels(userMessage, 3);
        
        // Search PDF documents
        List<Document> pdfResults = pdfEmbeddingService.searchPDFs(userMessage, 3);
        
        // Build context from both sources
        String searchContext = buildContext(hotelResults, pdfResults, context);
        
        // Check if user is asking about "the hotel" or "this hotel" - use context
        String enhancedMessage = enhanceMessageWithContext(userMessage, context);
        
        // Create system prompt with conversation awareness
        String systemPrompt = """
            You are a helpful hotel assistant with access to hotel database and policy documents.
            
            IMPORTANT CONTEXT RULES:
            1. ONLY provide information about hotels that appear in the "AVAILABLE INFORMATION" section below.
            2. NEVER make up, invent, or assume information about hotels that are not listed.
            3. NEVER mention hotel names that don't appear in the provided data.
            4. If you don't have information, say "I don't have information about that" - DO NOT guess.
            5. If asked about a location with no matching hotels, say "I don't have any hotels in that area in my database."
            6. DO NOT use any markdown formatting like **, ##, or bullet points - use plain text only.
            7. Use simple paragraphs and natural language without special formatting.
            8. When the user asks a follow-up question (like "what are the pet policies" after discussing a specific hotel),
               they are asking about THE LAST HOTEL MENTIONED, not all hotels.
            9. If the user says "this hotel", "that hotel", "it", or asks a question without specifying which hotel,
               refer to the most recently discussed hotel.
            10. Only provide information about multiple hotels if the user explicitly asks for comparisons or doesn't have
               a hotel in context.
            11. Keep track of which hotel you're discussing and stay focused on it unless the user changes the topic.
            
            CURRENT CONVERSATION CONTEXT:
            """ + (context.lastHotelMentioned != null ? 
                "Currently discussing: " + context.lastHotelMentioned : 
                "No specific hotel in context yet") + """
            
            
            Remember: If a hotel is not listed above, it does not exist in your knowledge base. Do not mention it.
            AVAILABLE INFORMATION:
            """ + searchContext;
        
        // Add system message
        SystemMessage systemMsg = new SystemMessage(systemPrompt);
        
        // Add current user message
        UserMessage userMsg = new UserMessage(enhancedMessage);
        context.messages.add(userMsg);
        
        // Build full message list (system + history)
        List<Message> fullMessages = new ArrayList<>();
        fullMessages.add(systemMsg);
        fullMessages.addAll(context.messages);
        
        // Get response from ChatClient with full history
        String botResponse = chatClient.prompt()
            .messages(fullMessages)
            .call()
            .content();
        
        // remove markdown
        botResponse = removeMarkdownFormatting(botResponse);
        
        // Clean up response
        botResponse = botResponse.replaceAll("\\n\\n", " ");
        botResponse = botResponse.replaceAll("\\n", " ");
        botResponse = botResponse.replaceAll("\\s+", " ").trim();
        
        // Add assistant response to history
        AssistantMessage assistantMsg = new AssistantMessage(botResponse);
        context.messages.add(assistantMsg);
        
        // Update context - track which hotel is being discussed
        updateConversationContext(context, userMessage, botResponse, hotelResults);
        
        // Keep history manageable (last 20 messages = 10 exchanges)
        if (context.messages.size() > 20) {
            context.messages.subList(0, context.messages.size() - 20).clear();
        }
        
        return botResponse;
    }
    
    /**
     * Enhance user message with context (replace "this hotel", "it", etc. with actual hotel name)
     */
    private String enhanceMessageWithContext(String userMessage, ConversationContext context) {
        String lower = userMessage.toLowerCase();
        
        // If user asks about "the hotel", "this hotel", "that hotel", "it"
        // AND we have a hotel in context, make it explicit
        if (context.lastHotelMentioned != null) {
            if (lower.contains("this hotel") || lower.contains("that hotel") || 
                lower.contains("the hotel") || lower.matches(".*\\bit\\b.*") ||
                (!lower.contains("hotel") && (lower.contains("policy") || lower.contains("policies") || 
                 lower.contains("amenities") || lower.contains("check-in") || lower.contains("price")))) {
                
                // Make the context explicit
                return userMessage + " (asking about " + context.lastHotelMentioned + ")";
            }
        }
        
        return userMessage;
    }
    
    /**
     * Update conversation context - track which hotel is being discussed
     */
    private void updateConversationContext(ConversationContext context, String userMessage, 
                                          String botResponse, List<Document> hotelResults) {
        
        // Extract hotel names mentioned in the response
        String lowerResponse = botResponse.toLowerCase();
        
        // Check if a specific hotel was mentioned in the results
        if (!hotelResults.isEmpty()) {
            for (Document doc : hotelResults) {
                String content = doc.getContent().toLowerCase();
                
                // Try to extract hotel name from content
                // Format is usually "Hotel Name is a X-star hotel located..."
                if (content.contains("hotel")) {
                    String[] parts = content.split(" is a ");
                    if (parts.length > 0) {
                        String possibleHotelName = parts[0].trim();
                        
                        // If this hotel name appears in the bot response, it's being discussed
                        if (lowerResponse.contains(possibleHotelName.toLowerCase())) {
                            context.lastHotelMentioned = capitalize(possibleHotelName);
                            
                            // Add to recent hotels if not already there
                            if (!context.recentHotels.contains(possibleHotelName)) {
                                context.recentHotels.add(0, possibleHotelName);
                                
                                // Keep only last 3 hotels
                                if (context.recentHotels.size() > 3) {
                                    context.recentHotels.remove(context.recentHotels.size() - 1);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        
        // Also check for explicit hotel mentions in bot response
        if (lowerResponse.contains("grand plaza hotel")) {
            context.lastHotelMentioned = "Grand Plaza Hotel";
        } else if (lowerResponse.contains("sunset beach resort")) {
            context.lastHotelMentioned = "Sunset Beach Resort";
        } else if (lowerResponse.contains("mountain view lodge")) {
            context.lastHotelMentioned = "Mountain View Lodge";
        } else if (lowerResponse.contains("downtown business hotel")) {
            context.lastHotelMentioned = "Downtown Business Hotel";
        } else if (lowerResponse.contains("seaside inn")) {
            context.lastHotelMentioned = "Seaside Inn";
        } else if (lowerResponse.contains("grand central suites")) {
            context.lastHotelMentioned = "Grand Central Suites";
        } else if (lowerResponse.contains("plaza hotel and spa")) {
            context.lastHotelMentioned = "Plaza Hotel and Spa";
        } else if (lowerResponse.contains("airport express hotel")) {
            context.lastHotelMentioned = "Airport Express Hotel";
        }
    }
    
    /**
     * Capitalize first letter of each word
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        
        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1));
                }
                result.append(" ");
            }
        }
        
        return result.toString().trim();
    }
    
    /**
     * Build context from hotel and PDF search results
     */
    private String buildContext(List<Document> hotelResults, List<Document> pdfResults, 
                                ConversationContext context) {
        StringBuilder contextBuilder = new StringBuilder();
        
        // Add hotel information
        if (!hotelResults.isEmpty()) {
            contextBuilder.append("=== HOTEL DATABASE ===\n");
            for (int i = 0; i < hotelResults.size(); i++) {
                Document doc = hotelResults.get(i);
                contextBuilder.append("\nHotel ").append(i + 1).append(":\n");
                contextBuilder.append(doc.getContent()).append("\n");
            }
        }
        
        // Add PDF document information
        if (!pdfResults.isEmpty()) {
            contextBuilder.append("\n=== HOTEL POLICIES & DOCUMENTS ===\n");
            
            // If we have a hotel in context, prioritize PDFs for that hotel
            if (context.lastHotelMentioned != null) {
                contextBuilder.append("(Focusing on: ").append(context.lastHotelMentioned).append(")\n");
            }
            
            for (int i = 0; i < pdfResults.size(); i++) {
                Document doc = pdfResults.get(i);
                Map<String, Object> metadata = doc.getMetadata();
                String fileName = (String) metadata.get("fileName");
                
                contextBuilder.append("\nDocument: ").append(fileName).append("\n");
                contextBuilder.append(doc.getContent()).append("\n");
            }
        }
        
        return contextBuilder.toString();
    }
    
    private String removeMarkdownFormatting(String text) {
        if (text == null) return text;
        
        // Remove bold/italic markers
        text = text.replaceAll("\\*\\*([^*]+)\\*\\*", "$1");  // **bold** → bold
        text = text.replaceAll("\\*([^*]+)\\*", "$1");        // *italic* → italic
        text = text.replaceAll("__([^_]+)__", "$1");          // __bold__ → bold
        text = text.replaceAll("_([^_]+)_", "$1");            // _italic_ → italic
        
        // Remove headers
        text = text.replaceAll("#{1,6}\\s+", "");             // ### Header → Header
        
        // Remove bullet points and numbered lists
        text = text.replaceAll("^\\s*[-*+]\\s+", "" );       // - item → item
        text = text.replaceAll("^\\s*\\d+\\.\\s+", "");       // 1. item → item
        
        // Remove inline code markers
        text = text.replaceAll("`([^`]+)`", "$1");            // `code` → code
        
        // Remove links but keep text
        text = text.replaceAll("\\[([^]]+)\\]\\([^)]+\\)", "$1");  // [text](url) → text
        
        return text;
    }
    
    /**
     * Clear conversation history for a session
     */
    public void clearHistory(String sessionId) {
        sessionContexts.remove(sessionId);
    }
}