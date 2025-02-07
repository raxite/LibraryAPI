package de.thws.libraryapi.persistence.repository;

import de.thws.libraryapi.domain.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>
{
    List<Book> findByTitleStartingWith(String prefix);
    List<Book> findByTitleContaining(String title);
    List<Book> findByAuthorName(String authorName);
    List<Book> findByGenreName(String genreName);
    List<Book> findByAvailability(Boolean availability);
}
