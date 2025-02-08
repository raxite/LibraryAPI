package de.thws.libraryapi.domain.service;

import de.thws.libraryapi.domain.model.Book;
import de.thws.libraryapi.domain.model.Role;
import de.thws.libraryapi.domain.model.User;
import de.thws.libraryapi.persistence.repository.BookRepository;
import de.thws.libraryapi.persistence.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,BookRepository bookRepository, PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.passwordEncoder = passwordEncoder;
    }

  /*  public List<User> getAllUsers() {
        return userRepository.findAll();
    }*/
  public Page<User> getAllUsers(Pageable pageable) {
      return userRepository.findAll(pageable);
  }




    public User registerUser(User user) {
    // Standardwerte setzen, damit Nutzer sich nicht selbst Admin-Rechte gibt
    user.setRole(Role.USER);
    user.setBorrowLimit(5);

    // 🔥 Passwort verschlüsseln, bevor es gespeichert wird
    user.setPassword(passwordEncoder.encode(user.getPassword()));

    return userRepository.save(user);
}


    public String addUserToReservationQueue(Long bookId, Long userId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (bookOpt.isEmpty() || userOpt.isEmpty()) {
            return "Book or User not found.";
        }

        Book book = bookOpt.get();
        User user = userOpt.get();

        //  Benutzer kann das Buch nicht reservieren, wenn es verfügbar ist
        if (Boolean.TRUE.equals(book.getAvailability())) {
            return "Book is currently available. Please borrow it instead of reserving.";
        }

        //  Benutzer kann das Buch nicht reservieren, wenn er es bereits ausgeliehen hat
        if (user.getBorrowedBooks().contains(book)) {
            return "User has already borrowed this book.";
        }

        // Prüfen, ob die Warteschlange das Limit erreicht hat (z. B. 10 Personen)
        if (book.getReservationQueue().size() >= 10) {
            return "Reservation limit for this book has been reached.";
        }

        //  Benutzer zur Warteschlange hinzufügen, falls er nicht schon drin ist
        if (!book.getReservationQueue().contains(user)) {
            book.getReservationQueue().add(user);
            bookRepository.save(book);
            return "User added to reservation queue.";
        } else {
            return "User is already in the reservation queue.";
        }
    }

    public String removeUserFromReservationQueue(Long bookId, Long userId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (bookOpt.isEmpty() || userOpt.isEmpty()) {
            return "Book or User not found!";
        }

        Book book = bookOpt.get();
        User user = userOpt.get();

        if (!book.getReservationQueue().contains(user)) {
            return "User is not in the reservation queue!";
        }

        book.getReservationQueue().remove(user);
        bookRepository.save(book);

        return "User removed from reservation queue.";
    }
    @Transactional
    public String borrowBook(Long bookId, Long userId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (bookOpt.isEmpty()) {
            return "Book not found!";
        }
        if (userOpt.isEmpty()) {
            return "User not found!";
        }

        Book book = bookOpt.get();
        User user = userOpt.get();

        if (!book.getAvailability()) {
            return "Book is already borrowed!";
        }

        if (user.getBorrowedBooks().size() >= user.getBorrowLimit()) {
            return "User has reached borrow limit!";
        }

        // Buch ausleihen
        book.setAvailability(false);
        book.setDueDate(new Date(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000))); // 7 Tage ausleihen
        user.getBorrowedBooks().add(book);

        bookRepository.save(book);
        userRepository.save(user);

        return "Book borrowed successfully!";
    }

    @Transactional
    public String returnBook(Long bookId, Long userId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (bookOpt.isEmpty() || userOpt.isEmpty()) {
            return "Book or User not found";
        }

        Book book = bookOpt.get();
        User user = userOpt.get();

        if (!user.getBorrowedBooks().contains(book)) {
            return "This book was not borrowed by this user.";
        }

        user.getBorrowedBooks().remove(book);
        userRepository.save(user);

        book.setAvailability(true);
        bookRepository.save(book);

        return "Book returned successfully!";
    }
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    public List<Book> getReservedBooksByUser(User user) {
        return bookRepository.findAll().stream()
                .filter(book -> book.getReservationQueue().contains(user))
                .collect(Collectors.toList());
    }



}
