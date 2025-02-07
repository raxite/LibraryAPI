package de.thws.libraryapi.dto;

import de.thws.libraryapi.domain.model.Author;
import de.thws.libraryapi.domain.model.Book;

import java.util.List;
import java.util.stream.Collectors;

public class AuthorDTO
{
    private Long id;
    private String name;
    private List<BookSummaryDTO> books;

    public AuthorDTO(Author author)
    {
        this.id = author.getId();
        this.name = author.getName();
        this.books = author.getBooks().stream()
                .map(BookSummaryDTO::new)
                .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<BookSummaryDTO> getBooks() {
        return books;
    }

    public void setBooks(List<BookSummaryDTO> books) {
        this.books = books;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
