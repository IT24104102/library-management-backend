package com.fernando.bookservice.repositories;

import com.fernando.bookservice.entities.Book;
import com.fernando.bookservice.entities.Book.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {
    
    // Find by title containing (case insensitive)
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    // Find by author containing (case insensitive)
    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);
    
    // Find by genre
    Page<Book> findByGenreIgnoreCase(String genre, Pageable pageable);
    
    // Find by status
    Page<Book> findByStatus(BookStatus status, Pageable pageable);
    
    // Find available books
    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0 AND b.status = 'AVAILABLE'")
    Page<Book> findAvailableBooks(Pageable pageable);
    
    // Search books by multiple criteria
    @Query("SELECT b FROM Book b WHERE " +
           "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
           "(:genre IS NULL OR LOWER(b.genre) = LOWER(:genre)) AND " +
           "(:status IS NULL OR b.status = :status)")
    Page<Book> searchBooks(@Param("title") String title, 
                          @Param("author") String author, 
                          @Param("genre") String genre, 
                          @Param("status") BookStatus status, 
                          Pageable pageable);
    
    // Find books by publisher
    Page<Book> findByPublisherContainingIgnoreCase(String publisher, Pageable pageable);
    
    // Find books by publication year range
    Page<Book> findByPublicationYearBetween(Integer startYear, Integer endYear, Pageable pageable);
    
    // Find books with low stock (available copies less than threshold)
    @Query("SELECT b FROM Book b WHERE b.availableCopies < :threshold AND b.status = 'AVAILABLE'")
    List<Book> findLowStockBooks(@Param("threshold") Integer threshold);
    
    // Get all distinct genres
    @Query("SELECT DISTINCT b.genre FROM Book b WHERE b.genre IS NOT NULL ORDER BY b.genre")
    List<String> findAllGenres();
}