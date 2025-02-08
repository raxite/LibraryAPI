package de.thws.libraryapi.persistence.repository;

import de.thws.libraryapi.domain.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long>
{
    List<Author> findByNameContainingIgnoreCase(String name);
}
