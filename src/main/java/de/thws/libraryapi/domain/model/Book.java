package de.thws.libraryapi.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.annotations.ManyToAny;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;

@Entity
@Table(name = "books")
public class Book
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String isbn;
    private Boolean availability;

    @Temporal(TemporalType.DATE)
    private Date dueDate;


    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;



    @ManyToOne
    @JoinColumn(name = "genre_id")
    private Genre genre;

    @ManyToMany
    @JoinTable(
            name = "book_reservations",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    List<User> reservationQueue = new ArrayList<>();


    public Book(){}

    public Book(String title, String isbn, Boolean availability, Author author, Genre genre)
    {
        this.title = title;
        this.isbn = isbn;
        this.availability = availability;
        this.author = author;
        this.genre = genre;
    }


    public List<User> getReservationQueue() {
        return reservationQueue;
    }

    public void setReservationQueue(List<User> reservationQueue) {
        this.reservationQueue = reservationQueue;
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

    public Boolean getAvailability() {
        return availability;
    }

    public void setAvailability(Boolean availability) {
        this.availability = availability;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public int getQueuePosition(User user)
    {
        int index = reservationQueue.indexOf(user);
        return (index >= 0) ? index + 1 : -1;
    }
}
