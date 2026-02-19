package com.manoj.matchIQ.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class DocumentParsingService {
    private static final Logger log = LoggerFactory.getLogger(DocumentParsingService.class);
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    /**
     * Extracts text from uploaded resume file (PDF, DOCX, or TXT)
     */
    public String extractTextFromResume(MultipartFile file) throws IOException {
        log.info(">>> Extracting text from resume file: {}", file.getOriginalFilename());
        log.info("    File size: {} bytes, Content type: {}", file.getSize(), file.getContentType());
        
        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 10MB");
        }
        
        // Validate file is not empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("File name is null");
        }
        
        String extension = getFileExtension(filename).toLowerCase();
        log.info("    File extension: {}", extension);
        
        String extractedText;
        
        switch (extension) {
            case "pdf":
                extractedText = extractTextFromPDF(file.getInputStream());
                break;
            case "doc":
            case "docx":
                extractedText = extractTextFromWord(file.getInputStream());
                break;
            case "txt":
                extractedText = new String(file.getBytes());
                break;
            default:
                throw new IllegalArgumentException(
                    "Unsupported file type: " + extension + ". Please upload PDF, DOCX, or TXT file."
                );
        }
        
        log.info("<<< Successfully extracted {} characters from resume", extractedText.length());
        log.debug("    First 100 chars: {}", extractedText.substring(0, Math.min(100, extractedText.length())));
        
        return extractedText.trim();
    }
    
    /**
     * Extracts text from PDF file
     */
    private String extractTextFromPDF(InputStream inputStream) throws IOException {
        log.debug("    Parsing PDF document...");
        
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            log.debug("    PDF parsed successfully - {} pages, {} characters", 
                    document.getNumberOfPages(), text.length());
            
            return text;
        }
    }
    
    /**
     * Extracts text from Word document (.doc or .docx)
     */
    private String extractTextFromWord(InputStream inputStream) throws IOException {
        log.debug("    Parsing Word document...");
        
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            
            String text = extractor.getText();
            
            log.debug("    Word document parsed successfully - {} characters", text.length());
            
            return text;
        }
    }
    
    /**
     * Gets file extension from filename
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return filename.substring(lastDot + 1);
    }
}
