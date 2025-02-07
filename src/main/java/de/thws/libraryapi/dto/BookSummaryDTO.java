package de.thws.libraryapi.dto;

import de.thws.libraryapi.domain.model.Book;

public class BookSummaryDTO
{
    private Long id;
    private String title;
    private String isbn;
    private String genre;

    public BookSummaryDTO(Book book) {
        this.id = book.getId();
        this.title = book.getTitle();
        this.isbn = book.getIsbn();
        this.genre = book.getGenre().getName(); // Nur der Genre-Name
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
    // Getter & Setter
}

