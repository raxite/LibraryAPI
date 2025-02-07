package de.thws.libraryapi.dto;

public class BookCreationDTO
{
    private String title;
    private String isbn;
    private Boolean availability;
    private Long authorId;
    private Long genreId;

    // Konstruktoren
    public BookCreationDTO() {}

    public BookCreationDTO(String title,String isbn, Boolean availability, Long authorId, Long genreId) {
            this.title = title;
            this.isbn = isbn;
            this.availability = availability;
            this.authorId = authorId;
            this.genreId = genreId;
    }

    // Getter und Setter
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public Long getGenreId() { return genreId; }
    public void setGenreId(Long genreId) { this.genreId = genreId; }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Boolean getAvailability() {
        return availability;
    }

    public void setAvailability(Boolean availability) {
        this.availability = availability;
    }
}


