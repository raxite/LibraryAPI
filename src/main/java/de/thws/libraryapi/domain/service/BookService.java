package de.thws.libraryapi.domain.service;

import de.thws.libraryapi.domain.model.Author;
import de.thws.libraryapi.domain.model.Book;
import de.thws.libraryapi.domain.model.Genre;
import de.thws.libraryapi.domain.model.User;
import de.thws.libraryapi.dto.BookCreationDTO;
import de.thws.libraryapi.persistence.repository.AuthorRepository;
import de.thws.libraryapi.persistence.repository.BookRepository;
import de.thws.libraryapi.persistence.repository.GenreRepository;
import de.thws.libraryapi.persistence.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class BookService
{
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;


    public BookService(BookRepository bookRepository, AuthorRepository authorRepository, GenreRepository genreRepository)
    {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
    }

    public Optional<Book> getBookById(Long id)
    {
        return bookRepository.findById(id);
    }

    public List<Book> getAllBooks()
    {
        return bookRepository.findAll();
    }

    public Book addBook(Book book)
    {
        return bookRepository.save(book);
    }
    public void deleteBook(Long id)
    {
        bookRepository.deleteById(id);
    }
    public List<Book> searchBooks(String title, String author, String genre, Boolean availability, String startsWith) {
        if (title != null) {
            return bookRepository.findByTitleContaining(title);
        } else if (startsWith != null) {
            return bookRepository.findByTitleStartingWith(startsWith);
        } else if (author != null) {
            return bookRepository.findByAuthorName(author);
        } else if (genre != null) {
            return bookRepository.findByGenreName(genre);
        } else if (availability != null) {
            return bookRepository.findByAvailability(availability);
        } else {
            return bookRepository.findAll();
        }
    }
    public Book updateBook(Book book) {
        return bookRepository.save(book);
    }
    public List<Book> getBooksByIds(List<Long> bookIds) {
        return bookRepository.findAllById(bookIds);
    }
    public Book createBook(BookCreationDTO bookDTO) {
        // Author suchen
        Author author = authorRepository.findById(bookDTO.getAuthorId())
                .orElseThrow(() -> new EntityNotFoundException("Author with ID " + bookDTO.getAuthorId() + " not found"));

        // Genre suchen
        Genre genre = genreRepository.findById(bookDTO.getGenreId())
                .orElseThrow(() -> new EntityNotFoundException("Genre with ID " + bookDTO.getGenreId() + " not found"));

        // Neues Buch erstellen
        Book book = new Book();
        book.setTitle(bookDTO.getTitle());
        book.setIsbn(bookDTO.getIsbn());  // Sicherstellen, dass ISBN gesetzt wird
        book.setAvailability(bookDTO.getAvailability());  // Sicherstellen, dass Availability gesetzt wird
        book.setAuthor(author);
        book.setGenre(genre);

        // Buch speichern
        Book savedBook = bookRepository.save(book);

        // Buch zur Liste des Autors hinzufügen
        author.getBooks().add(savedBook);
        authorRepository.save(author);

        return savedBook;
    }










}
