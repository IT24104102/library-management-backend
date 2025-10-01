package com.kuruneru.latereminders.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {
    private String isbn;
    private String title;
    private String author;
    private String genre;
    private Integer totalCopies;
    private Integer availableCopies;
}