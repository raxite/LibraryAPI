package de.thws.libraryapi.dto;

import de.thws.libraryapi.domain.model.Book;

public class ReservedBookDTO
{
    private Long id;
    private String title;
    private int queuePosition;

    public ReservedBookDTO(Book book, int position) {
        this.id = book.getId();
        this.title = book.getTitle();
        this.queuePosition = position;
    }

    // Getter
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public int getQueuePosition() { return queuePosition; }

}
