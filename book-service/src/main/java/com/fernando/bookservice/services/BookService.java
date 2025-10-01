package com.fernando.bookservice.services;

import com.fernando.bookservice.dto.*;
import com.fernando.bookservice.entities.Book;
import com.fernando.bookservice.repositories.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {
    
    private final BookRepository bookRepository;
    private final BookMetadataService bookMetadataService;
    
    // Enhanced book creation with metadata auto-fill
    @Transactional
    public BookResponse createBookWithMetadata(CreateBookWithMetadataRequest request) {
        log.info("Creating new book with ISBN: {} (auto-fill: {})", 
                request.getIsbn(), !request.getSkipMetadataFetch());
        
        // Check if book already exists
        if (bookRepository.existsById(request.getIsbn())) {
            throw new IllegalArgumentException("Book with ISBN " + request.getIsbn() + " already exists");
        }
        
        // Auto-fill metadata if not skipped and not manual entry
        if (!Boolean.TRUE.equals(request.getSkipMetadataFetch()) && 
            !Boolean.TRUE.equals(request.getIsManualEntry())) {
            
            BookMetadataResponse metadataResponse = bookMetadataService.fetchBookMetadata(request.getIsbn());
            
            if (metadataResponse.isSuccess()) {
                log.info("Auto-filled metadata from {}", metadataResponse.getData().getSource());
                mergeMetadataWithRequest(request, metadataResponse.getData());
            } else {
                log.warn("Metadata auto-fill failed: {}", metadataResponse.getMessage());
            }
        }
        
        // Validate required fields after metadata merge
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required. Please provide manually if auto-fill failed.");
        }
        if (request.getAuthor() == null || request.getAuthor().trim().isEmpty()) {
            throw new IllegalArgumentException("Author is required. Please provide manually if auto-fill failed.");
        }
        
        Book book = Book.builder()
                .isbn(request.getIsbn())
                .title(request.getTitle())
                .author(request.getAuthor())
                .publisher(request.getPublisher())
                .publicationYear(request.getPublicationYear())
                .genre(request.getGenre())
                .description(request.getDescription())
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getTotalCopies())
                .shelfLocation(request.getShelfLocation())
                .language(request.getLanguage())
                .pages(request.getPages())
                .status(Book.BookStatus.AVAILABLE)
                .build();
        
        Book savedBook = bookRepository.save(book);
        log.info("Successfully created book: {}", savedBook.getTitle());
        
        return mapToBookResponse(savedBook);
    }
    
    // Legacy method for backward compatibility
    @Transactional
    public BookResponse createBook(CreateBookRequest request) {
        log.info("Creating new book with ISBN: {}", request.getIsbn());
        
        // Check if book already exists
        if (bookRepository.existsById(request.getIsbn())) {
            throw new IllegalArgumentException("Book with ISBN " + request.getIsbn() + " already exists");
        }
        
        Book book = Book.builder()
                .isbn(request.getIsbn())
                .title(request.getTitle())
                .author(request.getAuthor())
                .publisher(request.getPublisher())
                .publicationYear(request.getPublicationYear())
                .genre(request.getGenre())
                .description(request.getDescription())
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getTotalCopies())
                .shelfLocation(request.getShelfLocation())
                .language(request.getLanguage())
                .pages(request.getPages())
                .status(Book.BookStatus.AVAILABLE)
                .build();
        
        Book savedBook = bookRepository.save(book);
        log.info("Successfully created book: {}", savedBook.getTitle());
        
        return mapToBookResponse(savedBook);
    }
    
    // Fetch metadata for ISBN without creating book
    public BookMetadataResponse fetchBookMetadata(String isbn) {
        log.info("Fetching metadata for ISBN: {}", isbn);
        
        // Check if book already exists
        if (bookRepository.existsById(isbn)) {
            return BookMetadataResponse.builder()
                    .success(false)
                    .message("Book with ISBN " + isbn + " already exists in the catalog")
                    .build();
        }
        
        return bookMetadataService.fetchBookMetadata(isbn);
    }
    
    private void mergeMetadataWithRequest(CreateBookWithMetadataRequest request, 
                                        BookMetadataResponse.BookMetadata metadata) {
        // Only fill empty fields, don't override user input
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            request.setTitle(metadata.getTitle());
        }
        if (request.getAuthor() == null || request.getAuthor().trim().isEmpty()) {
            request.setAuthor(metadata.getAuthor());
        }
        if (request.getPublisher() == null || request.getPublisher().trim().isEmpty()) {
            request.setPublisher(metadata.getPublisher());
        }
        if (request.getPublicationYear() == null) {
            request.setPublicationYear(metadata.getPublicationYear());
        }
        if (request.getGenre() == null || request.getGenre().trim().isEmpty()) {
            request.setGenre(metadata.getGenre());
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            request.setDescription(metadata.getDescription());
        }
        if (request.getLanguage() == null || request.getLanguage().trim().isEmpty()) {
            request.setLanguage(metadata.getLanguage());
        }
        if (request.getPages() == null) {
            request.setPages(metadata.getPages());
        }
    }
    
    public Page<BookResponse> getAllBooks(Pageable pageable) {
        log.info("Fetching all books with pagination");
        return bookRepository.findAll(pageable).map(this::mapToBookResponse);
    }
    
    public BookResponse getBookByIsbn(String isbn) {
        log.info("Fetching book with ISBN: {}", isbn);
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ISBN: " + isbn));
        return mapToBookResponse(book);
    }
    
    @Transactional
    public BookResponse updateBook(String isbn, UpdateBookRequest request) {
        log.info("Updating book with ISBN: {}", isbn);
        
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ISBN: " + isbn));
        
        // Update only provided fields
        if (request.getTitle() != null) {
            book.setTitle(request.getTitle());
        }
        if (request.getAuthor() != null) {
            book.setAuthor(request.getAuthor());
        }
        if (request.getPublisher() != null) {
            book.setPublisher(request.getPublisher());
        }
        if (request.getPublicationYear() != null) {
            book.setPublicationYear(request.getPublicationYear());
        }
        if (request.getGenre() != null) {
            book.setGenre(request.getGenre());
        }
        if (request.getDescription() != null) {
            book.setDescription(request.getDescription());
        }
        if (request.getTotalCopies() != null) {
            // Adjust available copies based on change in total copies
            int difference = request.getTotalCopies() - book.getTotalCopies();
            book.setTotalCopies(request.getTotalCopies());
            book.setAvailableCopies(book.getAvailableCopies() + difference);
            
            // Ensure available copies doesn't go negative
            if (book.getAvailableCopies() < 0) {
                book.setAvailableCopies(0);
            }
        }
        if (request.getShelfLocation() != null) {
            book.setShelfLocation(request.getShelfLocation());
        }
        if (request.getLanguage() != null) {
            book.setLanguage(request.getLanguage());
        }
        if (request.getPages() != null) {
            book.setPages(request.getPages());
        }
        if (request.getStatus() != null) {
            book.setStatus(Book.BookStatus.valueOf(request.getStatus().toUpperCase()));
        }
        
        Book updatedBook = bookRepository.save(book);
        log.info("Successfully updated book: {}", updatedBook.getTitle());
        
        return mapToBookResponse(updatedBook);
    }
    
    @Transactional
    public void deleteBook(String isbn) {
        log.info("Deleting book with ISBN: {}", isbn);
        
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ISBN: " + isbn));
        
        // Check if book has active borrows (available copies should equal total copies if no active borrows)
        if (book.getAvailableCopies() < book.getTotalCopies()) {
            throw new IllegalStateException("Cannot delete book with active borrows. Current available copies: " 
                    + book.getAvailableCopies() + ", Total copies: " + book.getTotalCopies());
        }
        
        bookRepository.delete(book);
        log.info("Successfully deleted book with ISBN: {}", isbn);
    }
    
    @Transactional
    public BookResponse markBookAsLost(String isbn, MarkBookLostRequest request) {
        log.info("Marking {} copies as lost for book with ISBN: {}", request.getCopiesToMarkLost(), isbn);
        
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ISBN: " + isbn));
        
        // Validate that we don't mark more copies as lost than available
        if (request.getCopiesToMarkLost() > book.getAvailableCopies()) {
            throw new IllegalStateException(
                "Cannot mark " + request.getCopiesToMarkLost() + " copies as lost. " +
                "Only " + book.getAvailableCopies() + " copies are available."
            );
        }
        
        // Reduce available copies and total copies
        book.setAvailableCopies(book.getAvailableCopies() - request.getCopiesToMarkLost());
        book.setTotalCopies(book.getTotalCopies() - request.getCopiesToMarkLost());
        
        // If no copies left, mark the book status as LOST
        if (book.getTotalCopies() == 0) {
            book.setStatus(Book.BookStatus.LOST);
        } else if (book.getAvailableCopies() == 0) {
            book.setStatus(Book.BookStatus.UNAVAILABLE);
        }
        
        Book updatedBook = bookRepository.save(book);
        
        log.info("Successfully marked {} copies as lost for book: {}. New totals - Available: {}, Total: {}", 
                request.getCopiesToMarkLost(), book.getTitle(), 
                updatedBook.getAvailableCopies(), updatedBook.getTotalCopies());
        
        return mapToBookResponse(updatedBook);
    }
    
    @Transactional
    public BookResponse addCopies(String isbn, AddCopiesRequest request) {
        log.info("Adding {} copies to book with ISBN: {} (reason: {}, added by: {})", 
                request.getCopiesToAdd(), isbn, request.getReason(), request.getAddedBy());
        
        if (request.getCopiesToAdd() <= 0) {
            throw new IllegalArgumentException("Additional copies must be positive");
        }
        
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ISBN: " + isbn));
        
        book.setTotalCopies(book.getTotalCopies() + request.getCopiesToAdd());
        book.setAvailableCopies(book.getAvailableCopies() + request.getCopiesToAdd());
        
        // Update status if book was previously unavailable/lost
        if (book.getStatus() == Book.BookStatus.LOST || book.getStatus() == Book.BookStatus.UNAVAILABLE) {
            book.setStatus(Book.BookStatus.AVAILABLE);
        }
        
        Book updatedBook = bookRepository.save(book);
        
        log.info("Successfully added {} copies to book: {}. New totals - Available: {}, Total: {}",
                request.getCopiesToAdd(), book.getTitle(),
                updatedBook.getAvailableCopies(), updatedBook.getTotalCopies());
        
        return mapToBookResponse(updatedBook);
    }
    
    public Page<BookResponse> searchBooks(String title, String author, String genre, String status, Pageable pageable) {
        log.info("Searching books with criteria - Title: {}, Author: {}, Genre: {}, Status: {}", 
                title, author, genre, status);
        
        Book.BookStatus bookStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                bookStatus = Book.BookStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid book status: " + status);
            }
        }
        
        return bookRepository.searchBooks(title, author, genre, bookStatus, pageable)
                .map(this::mapToBookResponse);
    }
    
    public Page<BookResponse> getAvailableBooks(Pageable pageable) {
        log.info("Fetching available books");
        return bookRepository.findAvailableBooks(pageable).map(this::mapToBookResponse);
    }
    
    public List<String> getAllGenres() {
        log.info("Fetching all book genres");
        return bookRepository.findAllGenres();
    }
    
    public List<BookResponse> getLowStockBooks(Integer threshold) {
        log.info("Fetching books with low stock (threshold: {})", threshold);
        return bookRepository.findLowStockBooks(threshold != null ? threshold : 5)
                .stream()
                .map(this::mapToBookResponse)
                .toList();
    }
    
    @Transactional
    public void decreaseAvailableCopies(String isbn) {
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ISBN: " + isbn));
        
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No available copies for book: " + book.getTitle());
        }
        
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);
        log.info("Decreased available copies for book: {}. New available copies: {}", 
                book.getTitle(), book.getAvailableCopies());
    }
    
    @Transactional
    public void increaseAvailableCopies(String isbn) {
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ISBN: " + isbn));
        
        if (book.getAvailableCopies() >= book.getTotalCopies()) {
            throw new IllegalStateException("Available copies cannot exceed total copies for book: " + book.getTitle());
        }
        
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);
        log.info("Increased available copies for book: {}. New available copies: {}", 
                book.getTitle(), book.getAvailableCopies());
    }
    
    private BookResponse mapToBookResponse(Book book) {
        return BookResponse.builder()
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .publicationYear(book.getPublicationYear())
                .genre(book.getGenre())
                .description(book.getDescription())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .status(book.getStatus().name())
                .shelfLocation(book.getShelfLocation())
                .language(book.getLanguage())
                .pages(book.getPages())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}