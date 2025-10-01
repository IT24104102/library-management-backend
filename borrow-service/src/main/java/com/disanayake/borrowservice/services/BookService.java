package com.disanayake.borrowservice.services;

import com.disanayake.borrowservice.dto.BookResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {
    
    private final RestTemplate restTemplate;
    
    @Value("${services.book-service.url}")
    private String bookServiceUrl;
    
    public BookResponse getBookByIsbn(String isbn) {
        try {
            String url = bookServiceUrl + "/api/books/" + isbn;
            log.info("Fetching book details for ISBN: {} from URL: {}", isbn, url);
            
            // The book-service returns an ApiResponse wrapper, so we need to extract the data
            ResponseEntity<java.util.Map> response = restTemplate.getForEntity(url, java.util.Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> apiResponse = (java.util.Map<String, Object>) response.getBody();
                Boolean success = (Boolean) apiResponse.get("success");
                
                if (success != null && success) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> bookData = (java.util.Map<String, Object>) apiResponse.get("data");
                    
                    if (bookData != null) {
                        // Convert the map to BookResponse
                        BookResponse bookResponse = BookResponse.builder()
                                .isbn((String) bookData.get("isbn"))
                                .title((String) bookData.get("title"))
                                .author((String) bookData.get("author"))
                                .publisher((String) bookData.get("publisher"))
                                .genre((String) bookData.get("genre"))
                                .publicationYear((Integer) bookData.get("publicationYear"))
                                .totalCopies((Integer) bookData.get("totalCopies"))
                                .availableCopies((Integer) bookData.get("availableCopies"))
                                .shelfLocation((String) bookData.get("shelfLocation"))
                                .description((String) bookData.get("description"))
                                .status((String) bookData.get("status"))
                                .build();
                        
                        log.info("Successfully fetched book details for ISBN: {}", isbn);
                        return bookResponse;
                    }
                }
                
                log.warn("Book not found for ISBN: {}", isbn);
                return null;
            } else {
                log.warn("Book not found for ISBN: {}", isbn);
                return null;
            }
        } catch (RestClientException e) {
            log.error("Error calling book service for ISBN: {}", isbn, e);
            return null;
        }
    }
    
    public boolean isBookAvailable(String isbn) {
        BookResponse book = getBookByIsbn(isbn);
        return book != null && 
               "AVAILABLE".equalsIgnoreCase(book.getStatus()) && 
               book.getAvailableCopies() > 0;
    }
    
    public void decreaseAvailableCopies(String isbn) {
        try {
            String url = bookServiceUrl + "/api/books/" + isbn + "/decrease-copies";
            log.info("Decreasing available copies for ISBN: {}", isbn);
            
            restTemplate.exchange(url, HttpMethod.PUT, null, Void.class);
            log.info("Successfully decreased available copies for ISBN: {}", isbn);
        } catch (RestClientException e) {
            log.error("Error decreasing available copies for ISBN: {}", isbn, e);
            throw new RuntimeException("Failed to update book availability", e);
        }
    }
    
    public void increaseAvailableCopies(String isbn) {
        try {
            String url = bookServiceUrl + "/api/books/" + isbn + "/increase-copies";
            log.info("Increasing available copies for ISBN: {}", isbn);
            
            restTemplate.exchange(url, HttpMethod.PUT, null, Void.class);
            log.info("Successfully increased available copies for ISBN: {}", isbn);
        } catch (RestClientException e) {
            log.error("Error increasing available copies for ISBN: {}", isbn, e);
            throw new RuntimeException("Failed to update book availability", e);
        }
    }
}