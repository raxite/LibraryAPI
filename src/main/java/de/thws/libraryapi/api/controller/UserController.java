package de.thws.libraryapi.api.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import de.thws.libraryapi.domain.model.Book;
import de.thws.libraryapi.domain.model.User;
import de.thws.libraryapi.domain.service.BookService;
import de.thws.libraryapi.domain.service.UserService;
import de.thws.libraryapi.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController
{
    private final UserService userService;
    private final BookService bookService;

    @Autowired
    public UserController(UserService userService, BookService bookService) {
        this.userService = userService;
        this.bookService = bookService;
    }

    //  USER darf nur eigene Daten abrufen
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = userOpt.get();
        List<Book> reservedBooks = userService.getReservedBooksByUser(user);

        return ResponseEntity.ok(new UserDTO(user, reservedBooks));
    }

    //  ADMIN darf ALLE Benutzer sehen
   @GetMapping
   @PreAuthorize("hasRole('ADMIN')")
   public ResponseEntity<List<UserDTO>> getAllUsers() {
       List<UserDTO> users = userService.getAllUsers()
               .stream()
               .map(user -> new UserDTO(user, getReservedBooksForUser(user))) //  Neuer Konstruktor mit Reservierungen
               .collect(Collectors.toList());
       return ResponseEntity.ok(users);
   }


    //  USER kann sich registrieren (Public)
    @PostMapping("/register")
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        if (user.getRole() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Role must not be null");
        }

        userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("User successfully created");
    }


    //  USER kann Bücher reservieren
    @PostMapping("/{userId}/reserveBook/{bookId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> reserveBook(@PathVariable Long userId, @PathVariable Long bookId) {
        String message = userService.addUserToReservationQueue(bookId, userId);
        if (message.contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
        return ResponseEntity.ok(message);
    }

    //  USER kann Reservierungen stornieren
    @DeleteMapping("/{userId}/reserveBook/{bookId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> cancelReservation(@PathVariable Long userId, @PathVariable Long bookId) {
        String message = userService.removeUserFromReservationQueue(bookId, userId);
        if (message.contains("not found") || message.contains("not in reservation")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
        return ResponseEntity.ok(message);
    }

//  USER kann Bücher ausleihen
@PostMapping("/{userId}/borrowBook/{bookId}")
@PreAuthorize("hasRole('USER')")
public ResponseEntity<String> borrowBook(@PathVariable Long userId, @PathVariable Long bookId) {
    String message = userService.borrowBook(bookId, userId);

    // Überprüfen, ob die Nachricht einen Fehler enthält
    if (message.toLowerCase().contains("not found") ||
            message.toLowerCase().contains("limit exceeded") ||
            message.toLowerCase().contains("already borrowed")) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    return ResponseEntity.ok(message); // Erfolgreiche Ausleihe -> 200 OK
}



    //  USER kann Bücher zurückgeben
    @PostMapping("/{userId}/returnBook/{bookId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> returnBook(@PathVariable Long userId, @PathVariable Long bookId) {
        String message = userService.returnBook(bookId, userId);
        if (message.contains("not borrowed")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        }
        return ResponseEntity.ok(message);
    }

    //  ADMIN darf Benutzer löschen
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        Optional<User> user = userService.getUserById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    //  ADMIN kann Nutzer aktualisieren (Rolle, Borrow-Limit)
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long userId, @RequestBody User updatedUser) {
        Optional<User> existingUserOpt = userService.getUserById(userId);

        if (existingUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User existingUser = existingUserOpt.get();

        //  Passwort ändern (nur wenn übermittelt)
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(userService.encodePassword(updatedUser.getPassword())); // Hashen!
        }

        //  Borrow-Limit ändern (nur Bibliothekar/Admin)
        if (updatedUser.getBorrowLimit() != null) {
            existingUser.setBorrowLimit(updatedUser.getBorrowLimit());
        }

        //  Benutzer-Rolle ändern (nur Bibliothekar/Admin)
        if (updatedUser.getRole() != null) {
            existingUser.setRole(updatedUser.getRole());
        }

        User savedUser = userService.updateUser(existingUser);
        return ResponseEntity.ok(new UserDTO(savedUser));
    }
    private List<Book> getReservedBooksForUser(User user) {
        return bookService.getAllBooks().stream()
                .filter(book -> book.getReservationQueue().contains(user)) //  User ist in Warteschlange?
                .collect(Collectors.toList());
    }

}


