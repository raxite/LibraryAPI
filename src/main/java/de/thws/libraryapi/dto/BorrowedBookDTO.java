package de.thws.libraryapi.dto;


import de.thws.libraryapi.domain.model.Book;

import java.util.Date;

public class BorrowedBookDTO
{
    private Long id;
    private String title;
    private String isbn;
    private Date dueDate;

    public BorrowedBookDTO(Book book) {
        this.id = book.getId();
        this.title = book.getTitle();
        this.isbn = book.getIsbn();
        this.dueDate = book.getDueDate();
    }

    // Getter
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getIsbn() { return isbn; }
    public Date getDueDate() { return dueDate; }
}

