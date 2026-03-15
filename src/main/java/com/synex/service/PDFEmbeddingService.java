package com.synex.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class PDFEmbeddingService {
    
    @Autowired
    private VectorStore vectorStore;
    
    @Autowired
    private ResourceLoader resourceLoader;
    
    // Configuration
    private static final int CHUNK_SIZE = 1000;
    private static final int CHUNK_OVERLAP = 200;
    
    /**
     * Process all PDFs in the resources/pdfs folder
     * OPTIMIZED: Process one at a time
     */
    public Map<String, Object> processAllPDFs() {
        Map<String, Object> result = new HashMap<>();
        
        // Check memory at start
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        
        System.out.println("=================================");
        System.out.println("MEMORY CHECK:");
        System.out.println("Max Memory: " + maxMemory + " MB");
        System.out.println("Total Memory: " + totalMemory + " MB");
        System.out.println("Free Memory: " + freeMemory + " MB");
        System.out.println("=================================");
        
        try {
            Resource[] resources = ResourcePatternUtils
                .getResourcePatternResolver(resourceLoader)
                .getResources("classpath:pdfs/*.pdf");
            
            System.out.println("Found " + resources.length + " PDF files");
            
            int totalChunks = 0;
            List<String> processedFiles = new ArrayList<>();
            List<String> failedFiles = new ArrayList<>();
            
            for (Resource resource : resources) {
                try {
                    String fileName = resource.getFilename();
                    System.out.println("\n Processing: " + fileName);
                    
                    // Step 1: Extract
                    System.out.println("[1/4] Extracting text...");
                    String text = extractTextFromPDF(resource.getInputStream());
                    System.out.println("Extracted " + text.length() + " characters");
                    
                    // Step 2: Chunk
                    System.out.println("[2/4] Chunking text...");
                    List<String> chunks = chunkText(text);
                    System.out.println("Created " + chunks.size() + " chunks");
                    
                    // Clear text to free memory
                    text = null;
                    
                    // Step 3: Create documents
                    System.out.println("[3/4] Creating documents...");
                    List<Document> documents = new ArrayList<>();
                    for (int i = 0; i < chunks.size(); i++) {
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("source", "pdf");
                        metadata.put("fileName", fileName);
                        metadata.put("chunkIndex", i);
                        metadata.put("totalChunks", chunks.size());
                        
                        Document doc = new Document(UUID.randomUUID().toString(), chunks.get(i), metadata);
                        documents.add(doc);
                    }
                    System.out.println("Created " + documents.size() + " document objects");
                    
                    // Clear chunks
                    chunks.clear();
                    chunks = null;
                    
                    // Step 4: Embed
                    System.out.println("[4/4] Calling OpenAI to create embeddings...");
                    System.out.println("(This may take 10-30 seconds...)");
                    
                    try {
                        vectorStore.add(documents);
                        System.out.println("Successfully embedded " + documents.size() + " chunks");
                        
                        totalChunks += documents.size();
                        processedFiles.add(fileName);
                        
                    } catch (Exception embedError) {
                        System.err.println("EMBEDDING FAILED!");
                        System.err.println("Error type: " + embedError.getClass().getName());
                        System.err.println("Error message: " + embedError.getMessage());
                        
                        if (embedError.getCause() != null) {
                            System.err.println("Caused by: " + embedError.getCause().getMessage());
                        }
                        
                        failedFiles.add(fileName + " (embedding failed)");
                        embedError.printStackTrace();
                    }
                    
                    // Cleanup
                    documents.clear();
                    documents = null;
                    System.gc();
                    
                } catch (Exception fileError) {
                    System.err.println("❌ Failed to process: " + resource.getFilename());
                    System.err.println("Error: " + fileError.getMessage());
                    failedFiles.add(resource.getFilename() + " (" + fileError.getClass().getSimpleName() + ")");
                    fileError.printStackTrace();
                }
            }
            
            // Final memory check
            runtime = Runtime.getRuntime();
            maxMemory = runtime.maxMemory() / (1024 * 1024);
            totalMemory = runtime.totalMemory() / (1024 * 1024);
            freeMemory = runtime.freeMemory() / (1024 * 1024);
            
            System.out.println("\n=================================");
            System.out.println("FINAL MEMORY:");
            System.out.println("Max Memory: " + maxMemory + " MB");
            System.out.println("Total Memory: " + totalMemory + " MB");
            System.out.println("Free Memory: " + freeMemory + " MB");
            System.out.println("=================================");
            
            result.put("status", failedFiles.isEmpty() ? "success" : "partial");
            result.put("filesProcessed", processedFiles.size());
            result.put("totalChunks", totalChunks);
            result.put("processedFiles", processedFiles);
            result.put("failedFiles", failedFiles);
            result.put("maxMemoryMB", maxMemory);
            result.put("message", "Processed " + processedFiles.size() + "/" + resources.length + " PDFs");
            
            System.out.println("\n✅ Completed: " + processedFiles.size() + " success, " + failedFiles.size() + " failed");
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
            result.put("errorType", e.getClass().getName());
            e.printStackTrace();
        }
        
        return result;
    }
    
    
    /**
     * Extract text from PDF using PDFBox 3.0 Loader class
     */
    private String extractTextFromPDF(InputStream inputStream) throws IOException {
        byte[] pdfBytes = null;
        PDDocument document = null;
        
        try {
            // Read bytes
            pdfBytes = inputStream.readAllBytes();
            inputStream.close(); // Close stream immediately
            
            // Load PDF
            document = Loader.loadPDF(pdfBytes);
            
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            
            String text = stripper.getText(document);
            
            // Clean up text
            text = text.replaceAll("\\s+", " ").trim();
            
            return text;
            
        } finally {
            // MEMORY OPTIMIZATION: Always close document
            if (document != null) {
                document.close();
            }
            // Clear byte array
            pdfBytes = null;
        }
    }
    
    /**
     * Chunk text with overlap
     */
    private List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        
        // Safety check
        if (text == null || text.isEmpty()) {
            return chunks;
        }
        
        // If text is smaller than chunk size, return as single chunk
        if (text.length() <= CHUNK_SIZE) {
            chunks.add(text);
            return chunks;
        }
        
        int start = 0;
        int iterations = 0;
        int maxIterations = (text.length() / (CHUNK_SIZE - CHUNK_OVERLAP)) + 10; // Safety limit
        
        while (start < text.length() && iterations < maxIterations) {
            iterations++;
            
            // Calculate end position
            int end = Math.min(start + CHUNK_SIZE, text.length());
            
            // Try to break at sentence boundary (only if not at end of text)
            if (end < text.length()) {
                // Look for sentence endings
                int lastPeriod = text.lastIndexOf('.', end);
                int lastQuestion = text.lastIndexOf('?', end);
                int lastExclamation = text.lastIndexOf('!', end);
                
                int sentenceEnd = Math.max(lastPeriod, Math.max(lastQuestion, lastExclamation));
                
                // Only use sentence boundary if it's not too far back
                // AND it's after the start position
                if (sentenceEnd > start && sentenceEnd > start + (CHUNK_SIZE / 2)) {
                    end = sentenceEnd + 1;
                }
            }
            
            // Extract chunk
            String chunk = text.substring(start, end).trim();
            
            // Only add non-empty chunks
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
            
            // CRITICAL FIX: Ensure we always move forward
            int nextStart = end - CHUNK_OVERLAP;
            
            // If overlap would put us back before current position, just move to end
            if (nextStart <= start) {
                nextStart = end;
            }
            
            // If we're not making progress, force move forward
            if (nextStart == start) {
                nextStart = start + 1;
            }
            
            start = nextStart;
        }
        return chunks;
    }
    
    // searches pdfs
    public List<Document> searchPDFs(String query, int limit) {
        return vectorStore.similaritySearch(query);
    }
}
