package de.thws.libraryapi.api.controller;

import de.thws.libraryapi.domain.model.Role;
import de.thws.libraryapi.persistence.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import de.thws.libraryapi.domain.model.Book;
import de.thws.libraryapi.domain.model.User;
import de.thws.libraryapi.domain.service.BookService;
import de.thws.libraryapi.domain.service.UserService;
import de.thws.libraryapi.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController
{
    private final UserService userService;
    private final BookService bookService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    public UserController(UserService userService, BookService bookService, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.userService = userService;
        this.bookService = bookService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

@GetMapping("/{userId}")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId, Authentication authentication) {
    Optional<User> userOpt = userService.getUserById(userId);

    if (userOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    User user = userOpt.get();

    //  Prüfen, ob der angemeldete User der gleiche ist oder ein Admin
    if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
            && !authentication.getName().equals(user.getUsername())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    List<Book> reservedBooks = userService.getReservedBooksByUser(user);
    return ResponseEntity.ok(new UserDTO(user, reservedBooks));
}



  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<UserDTO>> getAllUsers(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "5") int size,
          @RequestParam(defaultValue = "id:asc") String sort) {

      Pageable pageable = PageRequest.of(page, size, Sort.by(parseSortOrders(sort)));
      Page<User> usersPage = userService.getAllUsers(pageable);
      Page<UserDTO> userDTOPage = usersPage.map(user -> new UserDTO(user, getReservedBooksForUser(user)));

      return ResponseEntity.ok(userDTOPage);
  }


    private List<Sort.Order> parseSortOrders(String sort) {
        return Arrays.stream(sort.split(","))
                .map(s -> {
                    String[] sortParams = s.split(":");
                    String property = sortParams[0].trim();
                    Sort.Direction direction = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc"))
                            ? Sort.Direction.DESC
                            : Sort.Direction.ASC;
                    return new Sort.Order(direction, property);
                })
                .collect(Collectors.toList());
    }




    @PostMapping("/register")
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        //  Überprüfen, ob der Benutzername bereits existiert
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username is already taken.");
        }

        //  Setzt die Standardwerte für neue Nutzer
        user.setRole(Role.USER); // Jeder neue User wird automatisch "USER"
        user.setBorrowLimit(5);  // Standard-Borrow-Limit ist 5

        //  Passwort verschlüsseln
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        //  User speichern
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully.");
    }

@PostMapping("/{userId}/borrowBook/{bookId}")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public ResponseEntity<String> borrowBook(@PathVariable Long userId, @PathVariable Long bookId, Authentication authentication) {

    //  Prüfen, ob der User seine eigene ID nutzt oder ein Admin ist
    if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
            && !authentication.getName().equals(userService.getUserById(userId).map(User::getUsername).orElse(""))) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Users can only borrow books for themselves.");
    }

    String message = userService.borrowBook(bookId, userId);

    // Fehlerüberprüfung
    if (message.toLowerCase().contains("not found") ||
            message.toLowerCase().contains("limit exceeded") ||
            message.toLowerCase().contains("already borrowed")) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    return ResponseEntity.ok(message); // Erfolgreiche Ausleihe -> 200 OK
}

  @PostMapping("/{userId}/returnBook/{bookId}")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  public ResponseEntity<String> returnBook(@PathVariable Long userId, @PathVariable Long bookId, Authentication authentication) {

      //  Prüfen, ob der User seine eigene ID nutzt oder ein Admin ist
      if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
              && !authentication.getName().equals(userService.getUserById(userId).map(User::getUsername).orElse(""))) {
          return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Users can only return their own books.");
      }

      String message = userService.returnBook(bookId, userId);

      // Falls das Buch nicht ausgeliehen war -> 400 Bad Request
      if (message.contains("not borrowed")) {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
      }

      return ResponseEntity.ok(message); // Erfolgreiche Rückgabe -> 200 OK
  }




   @PostMapping("/{userId}/reserveBook/{bookId}")
   @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
   public ResponseEntity<String> reserveBook(@PathVariable Long userId, @PathVariable Long bookId, Authentication authentication) {

       // Prüfen, ob der User seine eigene ID nutzt oder ein Admin ist
       if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
               && !authentication.getName().equals(userService.getUserById(userId).map(User::getUsername).orElse(""))) {
           return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Users can only reserve books for themselves.");
       }

       String message = userService.addUserToReservationQueue(bookId, userId);

       if (message.contains("not found")) {
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
       }

       return ResponseEntity.ok(message);
   }



  @DeleteMapping("/{userId}/reserveBook/{bookId}")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  public ResponseEntity<String> cancelReservation(@PathVariable Long userId, @PathVariable Long bookId, Authentication authentication) {

      //  Prüfen, ob der User seine eigene ID nutzt oder ein Admin ist
      if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
              && !authentication.getName().equals(userService.getUserById(userId).map(User::getUsername).orElse(""))) {
          return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Users can only cancel their own reservations.");
      }

      String message = userService.removeUserFromReservationQueue(bookId, userId);

      if (message.contains("not found") || message.contains("not in reservation")) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
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


@PutMapping("/{userId}")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public ResponseEntity<UserDTO> updateUser(@PathVariable Long userId, @RequestBody User updatedUser, Authentication authentication) {
    Optional<User> existingUserOpt = userService.getUserById(userId);

    if (existingUserOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    User existingUser = existingUserOpt.get();

    //  Prüfen, ob der User seine eigene ID nutzt oder ein Admin ist
    boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    boolean isSelf = authentication.getName().equals(existingUser.getUsername());

    if (!isAdmin && !isSelf) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    //  Username darf NICHT geändert werden!
    if (updatedUser.getUsername() != null && !updatedUser.getUsername().equals(existingUser.getUsername())) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    //  Benutzer kann nur seinen eigenen Namen und Passwort ändern
    if (isSelf) {
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(userService.encodePassword(updatedUser.getPassword())); // Hashen!
        }
        if (updatedUser.getName() != null && !updatedUser.getName().isEmpty()) {
            existingUser.setName(updatedUser.getName());
        }
    }

    //  Admin kann ALLES ändern (außer Username)
    if (isAdmin) {
        if (updatedUser.getBorrowLimit() != null) {
            existingUser.setBorrowLimit(updatedUser.getBorrowLimit());
        }
        if (updatedUser.getRole() != null) {
            existingUser.setRole(updatedUser.getRole());
        }
    }

    //  Benutzer speichern & neue Daten aus der DB abrufen
    userService.updateUser(existingUser);
    User updatedUserFromDb = userService.getUserById(userId).orElseThrow(); // DB reload fix!

    return ResponseEntity.ok(new UserDTO(updatedUserFromDb));
}



    private List<Book> getReservedBooksForUser(User user) {
        return bookService.getAllBooks().stream()
                .filter(book -> book.getReservationQueue().contains(user)) //  User ist in Warteschlange?
                .collect(Collectors.toList());
    }

    //eventuell paging noch hinzufügen, sodass die liste

}


