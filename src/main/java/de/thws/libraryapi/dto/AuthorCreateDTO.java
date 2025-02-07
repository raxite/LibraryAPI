package de.thws.libraryapi.dto;

import java.util.List;

public class AuthorCreateDTO
{
    private String name;
    private List<BookDTO> books; // Vollständige Buchinfos zur Erstellung

    // Getter und Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<BookDTO> getBooks() { return books; }
    public void setBooks(List<BookDTO> books) { this.books = books; }
}
