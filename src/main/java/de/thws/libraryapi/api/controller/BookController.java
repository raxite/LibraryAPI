package de.thws.libraryapi.api.controller;

import de.thws.libraryapi.domain.model.Author;
import de.thws.libraryapi.domain.model.Book;
import de.thws.libraryapi.domain.model.Genre;
import de.thws.libraryapi.domain.service.AuthorService;
import de.thws.libraryapi.domain.service.BookService;
import de.thws.libraryapi.domain.service.GenreService;
import de.thws.libraryapi.dto.BookCreationDTO;
import de.thws.libraryapi.dto.BookDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


@RestController
@RequestMapping("/books")

public class BookController
{

    private final BookService bookService;
    private final AuthorService authorService;
    private final GenreService genreService;

    @Autowired
    public BookController(BookService bookService, AuthorService authorService, GenreService genreService)
    {
        this.bookService = bookService;
        this.authorService = authorService;
        this.genreService = genreService;
    }

    //  ADMIN darf Bücher bearbeiten
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookDTO> updateBook(@PathVariable Long id, @RequestBody Book updatedBook) {
        Optional<Book> existingBookOpt = bookService.getBookById(id);

        if (existingBookOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Book existingBook = existingBookOpt.get();

        // Nur nicht-null Werte aktualisieren
        if (updatedBook.getTitle() != null) {
            existingBook.setTitle(updatedBook.getTitle());
        }
        if (updatedBook.getIsbn() != null) {
            existingBook.setIsbn(updatedBook.getIsbn());
        }
        if (updatedBook.getAvailability() != null) {
            existingBook.setAvailability(updatedBook.getAvailability());
        }
        if (updatedBook.getDueDate() != null) {
            existingBook.setDueDate(updatedBook.getDueDate());
        }
        if (updatedBook.getAuthor() != null) {
            existingBook.setAuthor(updatedBook.getAuthor());
        }
        if (updatedBook.getGenre() != null) {
            existingBook.setGenre(updatedBook.getGenre());
        }

        Book savedBook = bookService.updateBook(existingBook);
        return ResponseEntity.ok(new BookDTO(savedBook));
    }




@GetMapping
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public ResponseEntity<Page<BookDTO>> getAllBooks(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size,
        @RequestParam(defaultValue = "id,asc") String sort) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(parseSortOrders(sort)));
    Page<Book> booksPage = bookService.getAllBooks(pageable);
    Page<BookDTO> bookDTOPage = booksPage.map(BookDTO::new);

    return ResponseEntity.ok(bookDTOPage);
}


    // Beide Rollen können ein Buch nach ID abrufen
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        return bookService.getBookById(id)
                .map(book -> ResponseEntity.ok(new BookDTO(book))) // Hier das BookDTO erzeugen
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }



    //  ADMIN darf Bücher hinzufügen
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createBook(@RequestBody BookCreationDTO bookDTO) {
        try {
            Book createdBook = bookService.createBook(bookDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body("Book with title '" + createdBook.getTitle() + "' created successfully.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }


    // ADMIN darf Bücher löschen
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {
        Optional<Book> book = bookService.getBookById(id);

        if (book.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Book with ID " + id + " not found.");
        }

        bookService.deleteBook(id);
        return ResponseEntity.ok("Book with ID " + id + " deleted successfully.");
    }



   @GetMapping("/search")
   @PreAuthorize("permitAll()")
   public ResponseEntity<Page<BookDTO>> searchBooks(
           @RequestParam(required = false) String title,
           @RequestParam(required = false) String author,
           @RequestParam(required = false) String genre,
           @RequestParam(required = false) Boolean availability,
           @RequestParam(defaultValue = "0") int page,
           @RequestParam(defaultValue = "10") int size,
           @RequestParam(defaultValue = "id,asc") String sort) {

       Pageable pageable = PageRequest.of(page, size, Sort.by(parseSortOrders(sort)));

       Page<Book> booksPage = bookService.searchBooks(title, author, genre, availability, pageable);
       Page<BookDTO> bookDTOPage = booksPage.map(BookDTO::new);

       return ResponseEntity.ok(bookDTOPage);
   }

 private List<Sort.Order> parseSortOrders(String sort) {
     String[] sortParams = sort.split(",");
     String property = sortParams[0].trim();
     Sort.Direction direction = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc"))
             ? Sort.Direction.DESC
             : Sort.Direction.ASC;
     return List.of(new Sort.Order(direction, property));
 }

}
