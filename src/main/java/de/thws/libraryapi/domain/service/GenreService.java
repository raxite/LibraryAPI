package de.thws.libraryapi.domain.service;


import de.thws.libraryapi.domain.model.Genre;
import de.thws.libraryapi.persistence.repository.GenreRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GenreService
{
    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository)
    {
        this.genreRepository = genreRepository;
    }

  /*  public List<Genre> getAllGenres()
    {
        return genreRepository.findAll();
    }*/
  public Page<Genre> getAllGenres(Pageable pageable) {
      return genreRepository.findAll(pageable);
  }
    public Optional<Genre> getGenreById(Long id) {
        return genreRepository.findById(id);
    }

    public Genre updateGenre(Long id, Genre updatedGenre) {
        Genre existingGenre = genreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Genre with ID " + id + " not found"));

        if (updatedGenre.getName() != null) {
            existingGenre.setName(updatedGenre.getName());
        }
        if (updatedGenre.getDescription() != null) {
            existingGenre.setDescription(updatedGenre.getDescription());
        }

        return genreRepository.save(existingGenre);
    }


    public void deleteGenre(Long id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Genre with ID " + id + " not found"));

        // Prüfen, ob Bücher mit diesem Genre existieren
        if (!genre.getBooks().isEmpty()) {
            throw new IllegalStateException("Cannot delete Genre with ID " + id + " as it is associated with books.");
        }

        genreRepository.delete(genre);
    }

    public Genre addGenre(Genre genre)
    {
        return genreRepository.save(genre);
    }
}
