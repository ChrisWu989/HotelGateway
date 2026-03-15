package com.synex.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.synex.service.PDFEmbeddingService;

import java.util.Map;

@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "*")
public class PDFController {
    
    @Autowired
    private PDFEmbeddingService pdfEmbeddingService;
    
    /**
     * Embed all PDFs from resources/pdfs folder
     * POST /api/pdf/embed
     */
    @PostMapping("/embed")
    public ResponseEntity<Map<String, Object>> embedPDFs() {
        Map<String, Object> result = pdfEmbeddingService.processAllPDFs();
        
        if ("success".equals(result.get("status"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.internalServerError().body(result);
        }
    }
}
