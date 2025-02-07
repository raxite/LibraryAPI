package de.thws.libraryapi.persistence.repository;

import de.thws.libraryapi.domain.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long>
{
}
