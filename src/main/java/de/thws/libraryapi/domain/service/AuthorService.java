package de.thws.libraryapi.domain.service;

import de.thws.libraryapi.domain.model.Author;
import de.thws.libraryapi.persistence.repository.AuthorRepository;
import de.thws.libraryapi.persistence.repository.BookRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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


    @CachePut(value = "authors", key = "#author.id")
    public Author updateAuthor(Author author) {
        return authorRepository.save(author);
    }


    @CacheEvict(value = {"authors", "allAuthors"}, allEntries = true) // eventuell fehleranfällig
    @Transactional
    public Author createAuthor(Author author) {
        // Speichert den Autor und stellt sicher, dass die Bücher auch gespeichert werden
        Author savedAuthor = authorRepository.save(author);

        if (author.getBooks() != null) {
            bookRepository.saveAll(author.getBooks());
        }

        return savedAuthor;
    }



    @CacheEvict(value = "authors", key = "#id")
    public void deleteAuthor(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Author with ID " + id + " not found"));

        if (!author.getBooks().isEmpty()) {
            throw new IllegalStateException("Cannot delete author with ID " + id + " because they have associated books.");
        }

        authorRepository.deleteById(id);
        System.out.println("Author with ID " + id + " deleted successfully.");
    }

    @Cacheable(value = "authorSearchResults", key = "#name + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Author> searchAuthorsByName(String name, Pageable pageable)
    {
        return authorRepository.findByNameContainingIgnoreCase(name, pageable);
  }


   // @Cacheable(value = "allAuthors", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
   @Cacheable(value = "allAuthors", key = "'page-' + #pageable.pageNumber + '-size-' + #pageable.pageSize")
   public Page<Author> getAllAuthors(Pageable pageable) {
        return authorRepository.findAll(pageable);
    }
    @Cacheable(value = "authors", key = "#id")
    public Optional<Author> getAuthorById(Long id) {
        return authorRepository.findById(id);
    }
}
