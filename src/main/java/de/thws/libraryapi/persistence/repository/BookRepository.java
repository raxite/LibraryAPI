package de.thws.libraryapi.persistence.repository;

import de.thws.libraryapi.domain.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>
{
    @Query("SELECT b FROM Book b JOIN b.author a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :authorName, '%'))")
    Page<Book> findByAuthorName(@Param("authorName") String authorName, Pageable pageable);
    Page<Book> findByTitleStartingWith(String prefix, Pageable pageable);
    Page<Book> findByTitleContaining(String title, Pageable pageable);
 //   Page<Book> findByAuthorNameContainingIgnoreCase(String authorName, Pageable pageable);
    Page<Book> findByGenreName(String genreName, Pageable pageable);
    Page<Book> findByAvailability(Boolean availability, Pageable pageable);
}
