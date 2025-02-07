package de.thws.libraryapi.dto;

import de.thws.libraryapi.domain.model.Book;
import de.thws.libraryapi.domain.model.User;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class BookDTO
{

        private Long id;
        private String title;
        private String isbn;
        private Boolean availability;
        private String authorName;
        private String genreName;
        private Date dueDate;
        private List<Long> reservationQueue;

        public BookDTO(Book book) {
            this.id = book.getId();
            this.title = book.getTitle();
            this.isbn = book.getIsbn();
            this.availability = book.getAvailability();
            this.authorName = book.getAuthor() != null ? book.getAuthor().getName() : "Unknown";
            this.genreName = book.getGenre() != null ? book.getGenre().getName() : "Unknown";
            this.dueDate = book.getDueDate();
            this.reservationQueue = book.getReservationQueue().stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
        }

        // Getter & Setter
        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getIsbn() { return isbn; }
        public Boolean getAvailability() { return availability; }
        public String getAuthorName() { return authorName; }
        public String getGenreName() { return genreName; }

    public Date getDueDate() {
        return dueDate;
    }
    public List<Long> getReservationQueue()
    {
        return reservationQueue;
    }
}


