package com.fernando.bookservice.services;

import com.fernando.bookservice.dto.BookMetadataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookMetadataService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Google Books API base URL
    private static final String GOOGLE_BOOKS_API_URL = "https://www.googleapis.com/books/v1/volumes?q=isbn:";
    
    // Open Library API base URL
    private static final String OPEN_LIBRARY_API_URL = "https://openlibrary.org/api/books?bibkeys=ISBN:";
    
    public BookMetadataResponse fetchBookMetadata(String isbn) {
        log.info("Fetching metadata for ISBN: {}", isbn);
        
        // Clean ISBN (remove hyphens and spaces)
        String cleanIsbn = isbn.replaceAll("[\\s-]", "");
        
        // Try Google Books API first
        BookMetadataResponse googleResponse = fetchFromGoogleBooks(cleanIsbn);
        if (googleResponse.isSuccess()) {
            return googleResponse;
        }
        
        // Fallback to Open Library API
        BookMetadataResponse openLibraryResponse = fetchFromOpenLibrary(cleanIsbn);
        if (openLibraryResponse.isSuccess()) {
            return openLibraryResponse;
        }
        
        // If both fail, return unsuccessful response
        return BookMetadataResponse.builder()
                .success(false)
                .message("Book metadata not found for ISBN: " + isbn)
                .build();
    }
    
    private BookMetadataResponse fetchFromGoogleBooks(String isbn) {
        try {
            log.info("Fetching from Google Books API for ISBN: {}", isbn);
            String url = GOOGLE_BOOKS_API_URL + isbn;
            String response = restTemplate.getForObject(url, String.class);
            
            if (response != null) {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode itemsNode = rootNode.get("items");
                
                if (itemsNode != null && itemsNode.isArray() && itemsNode.size() > 0) {
                    JsonNode bookNode = itemsNode.get(0);
                    JsonNode volumeInfo = bookNode.get("volumeInfo");
                    
                    if (volumeInfo != null) {
                        BookMetadataResponse.BookMetadata metadata = parseGoogleBooksData(volumeInfo, isbn);
                        log.info("Successfully fetched metadata from Google Books for: {}", metadata.getTitle());
                        
                        return BookMetadataResponse.builder()
                                .success(true)
                                .message("Metadata retrieved from Google Books API")
                                .data(metadata)
                                .build();
                    }
                }
            }
        } catch (RestClientException e) {
            log.warn("Google Books API request failed for ISBN: {}", isbn, e);
        } catch (Exception e) {
            log.error("Error parsing Google Books API response for ISBN: {}", isbn, e);
        }
        
        return BookMetadataResponse.builder()
                .success(false)
                .message("Google Books API lookup failed")
                .build();
    }
    
    private BookMetadataResponse fetchFromOpenLibrary(String isbn) {
        try {
            log.info("Fetching from Open Library API for ISBN: {}", isbn);
            String url = OPEN_LIBRARY_API_URL + isbn + "&format=json&jscmd=data";
            String response = restTemplate.getForObject(url, String.class);
            
            if (response != null) {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode bookNode = rootNode.get("ISBN:" + isbn);
                
                if (bookNode != null) {
                    BookMetadataResponse.BookMetadata metadata = parseOpenLibraryData(bookNode, isbn);
                    log.info("Successfully fetched metadata from Open Library for: {}", metadata.getTitle());
                    
                    return BookMetadataResponse.builder()
                            .success(true)
                            .message("Metadata retrieved from Open Library API")
                            .data(metadata)
                            .build();
                }
            }
        } catch (RestClientException e) {
            log.warn("Open Library API request failed for ISBN: {}", isbn, e);
        } catch (Exception e) {
            log.error("Error parsing Open Library API response for ISBN: {}", isbn, e);
        }
        
        return BookMetadataResponse.builder()
                .success(false)
                .message("Open Library API lookup failed")
                .build();
    }
    
    private BookMetadataResponse.BookMetadata parseGoogleBooksData(JsonNode volumeInfo, String isbn) {
        return BookMetadataResponse.BookMetadata.builder()
                .isbn(isbn)
                .title(getTextValue(volumeInfo, "title"))
                .author(getAuthorsAsString(volumeInfo.get("authors")))
                .publisher(getTextValue(volumeInfo, "publisher"))
                .publicationYear(parsePublicationYear(getTextValue(volumeInfo, "publishedDate")))
                .genre(getCategoriesAsString(volumeInfo.get("categories")))
                .description(getTextValue(volumeInfo, "description"))
                .language(getTextValue(volumeInfo, "language"))
                .pages(getIntValue(volumeInfo, "pageCount"))
                .coverImageUrl(getImageUrl(volumeInfo.get("imageLinks")))
                .source("Google Books API")
                .build();
    }
    
    private BookMetadataResponse.BookMetadata parseOpenLibraryData(JsonNode bookNode, String isbn) {
        return BookMetadataResponse.BookMetadata.builder()
                .isbn(isbn)
                .title(getTextValue(bookNode, "title"))
                .author(getAuthorsFromOpenLibrary(bookNode.get("authors")))
                .publisher(getPublishersFromOpenLibrary(bookNode.get("publishers")))
                .publicationYear(parsePublicationYear(getTextValue(bookNode, "publish_date")))
                .genre(getSubjectsAsString(bookNode.get("subjects")))
                .pages(getIntValue(bookNode, "number_of_pages"))
                .source("Open Library API")
                .build();
    }
    
    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : null;
    }
    
    private Integer getIntValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asInt() : null;
    }
    
    private String getAuthorsAsString(JsonNode authorsNode) {
        if (authorsNode != null && authorsNode.isArray()) {
            StringBuilder authors = new StringBuilder();
            for (JsonNode author : authorsNode) {
                if (authors.length() > 0) {
                    authors.append(", ");
                }
                authors.append(author.asText());
            }
            return authors.toString();
        }
        return null;
    }
    
    private String getCategoriesAsString(JsonNode categoriesNode) {
        if (categoriesNode != null && categoriesNode.isArray() && categoriesNode.size() > 0) {
            return categoriesNode.get(0).asText(); // Return first category as genre
        }
        return null;
    }
    
    private String getSubjectsAsString(JsonNode subjectsNode) {
        if (subjectsNode != null && subjectsNode.isArray() && subjectsNode.size() > 0) {
            JsonNode firstSubject = subjectsNode.get(0);
            return firstSubject.has("name") ? firstSubject.get("name").asText() : firstSubject.asText();
        }
        return null;
    }
    
    private String getAuthorsFromOpenLibrary(JsonNode authorsNode) {
        if (authorsNode != null && authorsNode.isArray()) {
            StringBuilder authors = new StringBuilder();
            for (JsonNode author : authorsNode) {
                if (authors.length() > 0) {
                    authors.append(", ");
                }
                String name = author.has("name") ? author.get("name").asText() : author.asText();
                authors.append(name);
            }
            return authors.toString();
        }
        return null;
    }
    
    private String getPublishersFromOpenLibrary(JsonNode publishersNode) {
        if (publishersNode != null && publishersNode.isArray() && publishersNode.size() > 0) {
            JsonNode firstPublisher = publishersNode.get(0);
            return firstPublisher.has("name") ? firstPublisher.get("name").asText() : firstPublisher.asText();
        }
        return null;
    }
    
    private String getImageUrl(JsonNode imageLinksNode) {
        if (imageLinksNode != null) {
            // Try thumbnail first, then smallThumbnail
            JsonNode thumbnail = imageLinksNode.get("thumbnail");
            if (thumbnail != null) {
                return thumbnail.asText();
            }
            JsonNode smallThumbnail = imageLinksNode.get("smallThumbnail");
            if (smallThumbnail != null) {
                return smallThumbnail.asText();
            }
        }
        return null;
    }
    
    private Integer parsePublicationYear(String publishDate) {
        if (publishDate != null && !publishDate.isEmpty()) {
            try {
                // Extract year from various date formats (YYYY, YYYY-MM-DD, etc.)
                String yearStr = publishDate.split("-")[0];
                return Integer.parseInt(yearStr);
            } catch (NumberFormatException e) {
                log.warn("Could not parse publication year from: {}", publishDate);
            }
        }
        return null;
    }
}