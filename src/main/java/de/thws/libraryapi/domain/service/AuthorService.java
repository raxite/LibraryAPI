package de.thws.libraryapi.domain.service;

import de.thws.libraryapi.domain.model.Author;
import de.thws.libraryapi.persistence.repository.AuthorRepository;
import de.thws.libraryapi.persistence.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthorService
{
    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    @Autowired
    public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository)
    {
        this.authorRepository = authorRepository;
        this.bookRepository =bookRepository;
    }


    public Author updateAuthor(Author author) {
        return authorRepository.save(author);
    }


    @Transactional
    public Author createAuthor(Author author) {
        // Speichert den Autor und stellt sicher, dass die Bücher auch gespeichert werden
        Author savedAuthor = authorRepository.save(author);

        if (author.getBooks() != null) {
            bookRepository.saveAll(author.getBooks());
        }

        return savedAuthor;
    }



    public void deleteAuthor(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Author with ID " + id + " not found"));

        if (!author.getBooks().isEmpty()) {
            throw new IllegalStateException("Cannot delete author with ID " + id + " because they have associated books.");
        }

        authorRepository.deleteById(id);
        System.out.println("Author with ID " + id + " deleted successfully.");
    }
    public List<Author> searchAuthorsByName(String name) {
        return authorRepository.findByNameContainingIgnoreCase(name);
    }


    public List<Author> getAllAuthors()
    {
        return authorRepository.findAll();
    }
    public Optional<Author> getAuthorById(Long id) {
        return authorRepository.findById(id);
    }
}
