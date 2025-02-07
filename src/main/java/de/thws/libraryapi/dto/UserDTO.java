package de.thws.libraryapi.dto;

import de.thws.libraryapi.domain.model.Book;
import de.thws.libraryapi.domain.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserDTO {
    private Long id;
    private String name;
    private String username;
    private String role;
    private List<BorrowedBookDTO> borrowedBooks;
    private List<ReservedBookDTO> reservedBooks;
    private Integer borrowLimit;


    public UserDTO(User user){
        this.id = user.getId();
        this.name = user.getName();
        this.username = user.getUsername();
        this.role = (user.getRole() != null) ? user.getRole().name() : "UNKNOWN";
        this.borrowedBooks = user.getBorrowedBooks().stream()
                .map(BorrowedBookDTO::new)  // Map Book → BorrowedBookDTO
                .collect(Collectors.toList());

        /*this.reservedBooks = user.getReservationQueue().stream()
                .map(book -> new ReservedBookDTO(book, book.getQueuePosition(user)))
                .collect(Collectors.toList());*/
        this.reservedBooks = user.getBorrowedBooks().stream()
                .filter(book -> book.getReservationQueue().contains(user)) // Nur Bücher, wo der User in der Queue ist
                .map(book -> new ReservedBookDTO(book, book.getQueuePosition(user)))
                .collect(Collectors.toList());
        this.borrowLimit = user.getBorrowLimit();
    }

    public UserDTO(User user, List<Book> reservedBooks) {
        this.id = user.getId();
        this.name = user.getName();
        this.username = user.getUsername();
        this.role = (user.getRole() != null) ? user.getRole().name() : "UNKNOWN";

        // 📖 Geliehene Bücher
        this.borrowedBooks = user.getBorrowedBooks().stream()
                .map(BorrowedBookDTO::new)
                .collect(Collectors.toList());

        // 🏷 Reservierte Bücher über `reservedBooks` oder `Book.reservationQueue`
        if (reservedBooks != null) {
            this.reservedBooks = reservedBooks.stream()
                    .map(book -> new ReservedBookDTO(book, getQueuePosition(user, book)))
                    .collect(Collectors.toList());
        } else {
            this.reservedBooks = user.getBorrowedBooks().stream()
                    .filter(book -> book.getReservationQueue().contains(user)) // User ist in Warteschlange
                    .map(book -> new ReservedBookDTO(book, getQueuePosition(user, book)))
                    .collect(Collectors.toList());
        }

        this.borrowLimit = user.getBorrowLimit();
    }


    // Getter & Setter
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getRole() { return role; }

    public List<BorrowedBookDTO> getBorrowedBooks() {
        return borrowedBooks;
    }

    public List<ReservedBookDTO> getReservedBooks() {
        return reservedBooks;
    }

    public Integer getBorrowLimit()
    {
        return borrowLimit;
    }
   /* private int getQueuePosition(User user, Book book) {
        int index = book.getReservationQueue().indexOf(user);
        return (index >= 0) ? index + 1 : -1;
    }*/
   private int getQueuePosition(User user, Book book) {
       return book.getReservationQueue().indexOf(user) + 1; // 1-basiert
   }

}
