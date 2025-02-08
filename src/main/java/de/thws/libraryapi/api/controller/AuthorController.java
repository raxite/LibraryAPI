package de.thws.libraryapi.api.controller;

import de.thws.libraryapi.domain.model.Author;
import de.thws.libraryapi.domain.model.Book;
import de.thws.libraryapi.domain.service.AuthorService;
import de.thws.libraryapi.domain.service.BookService;
import de.thws.libraryapi.dto.AuthorCreateDTO;
import de.thws.libraryapi.dto.AuthorDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/authors")

public class AuthorController
{
    private final AuthorService authorService;
    private final BookService bookService;


    @Autowired
    public AuthorController(AuthorService authorService, BookService bookService)
    {
        this.authorService = authorService;
        this.bookService = bookService;
    }

    //  ADMIN darf Autoren erstellen
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthorDTO> createAuthor(@RequestBody AuthorCreateDTO authorCreateDTO) {
        // Neuen Autor erstellen
        Author newAuthor = new Author();
        newAuthor.setName(authorCreateDTO.getName());

        // Autor in der Datenbank speichern
        Author savedAuthor = authorService.createAuthor(newAuthor);

        // Rückgabe als DTO
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthorDTO(savedAuthor));
    }

    //  ADMIN kann Bücher zu Autoren hinzufügen
    @PutMapping("/{authorId}/books")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthorDTO> addBooksToAuthor(@PathVariable Long authorId, @RequestBody List<Long> bookIds) {
        Optional<Author> authorOpt = authorService.getAuthorById(authorId);
        if (authorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Author author = authorOpt.get();
        List<Book> books = bookService.getBooksByIds(bookIds);

        // Bücher dem Autor zuweisen
        for (Book book : books) {
            book.setAuthor(author);
        }

        // Autor speichern (automatisch in JPA gespeichert)
        Author updatedAuthor = authorService.updateAuthor(author);
        return ResponseEntity.ok(new AuthorDTO(updatedAuthor));
    }



    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<AuthorDTO>> getAllAuthors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id:asc") String sort) {
        List<Sort.Order> orders = parseSortOrders(sort);

        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));

        Page<Author> authorsPage = authorService.getAllAuthors(pageable);
        Page<AuthorDTO> authorDTOPage = authorsPage.map(AuthorDTO::new);

        return ResponseEntity.ok(authorDTOPage);
    }




    //  ADMIN kann Autoren aktualisieren
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthorDTO> updateAuthor(@PathVariable Long id, @RequestBody Author updatedAuthor) {
        Optional<Author> existingAuthorOpt = authorService.getAuthorById(id);

        if (existingAuthorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Author existingAuthor = existingAuthorOpt.get();

        // Aktualisierung nur für vorhandene Werte
        if (updatedAuthor.getName() != null) {
            existingAuthor.setName(updatedAuthor.getName());
        }

        Author savedAuthor = authorService.updateAuthor(existingAuthor);
        return ResponseEntity.ok(new AuthorDTO(savedAuthor));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<AuthorDTO>> searchAuthors(@RequestParam String name, Pageable pageable) {
        Page<Author> authorsPage = authorService.searchAuthorsByName(name, pageable);
        Page<AuthorDTO> authorDTOPage = authorsPage.map(AuthorDTO::new);  // Mapping von Author zu AuthorDTO
        return ResponseEntity.ok(authorDTOPage);
    }




    //  ADMIN kann Autoren löschen
   @DeleteMapping("/{id}")
   @PreAuthorize("hasRole('ADMIN')")
   public ResponseEntity<String> deleteAuthor(@PathVariable Long id) {
       try {
           authorService.deleteAuthor(id);
           return ResponseEntity.ok("Author with ID " + id + " deleted successfully.");
       } catch (IllegalStateException e) {
           System.err.println("ERROR: " + e.getMessage());
           return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
       } catch (EntityNotFoundException e) {
           System.err.println("ERROR: " + e.getMessage());
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
       }
   }


    private List<Sort.Order> parseSortOrders(String sort) {
        if (sort == null || sort.isBlank()) {
            return List.of(new Sort.Order(Sort.Direction.ASC, "id"));  // Standard-Sortierung nach "id" aufsteigend
        }

        return Arrays.stream(sort.split(","))
                .map(s -> {
                    String[] sortParams = s.split(":");
                    String property = sortParams[0].trim();
                    Sort.Direction direction = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc"))
                            ? Sort.Direction.DESC
                            : Sort.Direction.ASC;
                    return new Sort.Order(direction, property);
                })
                .filter(order -> isValidProperty(order.getProperty()))  // Filter für gültige Properties
                .collect(Collectors.toList());
    }

    private boolean isValidProperty(String property) {
        List<String> validProperties = List.of("id", "name");  // Liste der erlaubten Sortierfelder
        return validProperties.contains(property);
    }


    //eventuell Fallback auf default paging einbauen, problem es könnte zu viele daten zurückgegeben werden
    //eventuell einschränken wie viele Bücher eines Authors zurückgegeben werden können, könnte zu viel sein



}
