package de.thws.libraryapi.api.controller;


import de.thws.libraryapi.domain.model.Genre;
import de.thws.libraryapi.domain.service.GenreService;
import de.thws.libraryapi.dto.AuthorDTO;
import de.thws.libraryapi.dto.GenreDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/genres")
public class GenreController
{
    private final GenreService genreService;

    public GenreController(GenreService genreService)
    {
        this.genreService = genreService;
    }


    //  ADMIN darf alle Genres sehen
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<GenreDTO>> getAllGenres() {
        List<GenreDTO> genres = genreService.getAllGenres()
                .stream()
                .map(GenreDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(genres);
    }

    // ADMIN kann Genres bearbeiten
   @PutMapping("/{id}")
   @PreAuthorize("hasRole('ADMIN')")
   public ResponseEntity<GenreDTO> updateGenre(@PathVariable Long id, @RequestBody Genre updatedGenre) {
       try {
           Genre savedGenre = genreService.updateGenre(id, updatedGenre);
           return ResponseEntity.ok(new GenreDTO(savedGenre));
       } catch (EntityNotFoundException e) {
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
       }
   }


    //  ADMIN kann Genres löschen
   @DeleteMapping("/{id}")
   @PreAuthorize("hasRole('ADMIN')")
   public ResponseEntity<String> deleteGenre(@PathVariable Long id) {
       try {
           genreService.deleteGenre(id);
           return ResponseEntity.ok("Genre with ID " + id + " deleted successfully.");
       } catch (IllegalStateException e) {
           System.err.println("ERROR: " + e.getMessage());
           return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
       } catch (EntityNotFoundException e) {
           System.err.println("ERROR: " + e.getMessage());
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
       }
   }

    //  ADMIN kann Genres hinzufügen
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addGenre(@RequestBody Genre newGenre) {
        if (newGenre.getName() == null || newGenre.getName().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Genre name is required.");
        }
        Genre savedGenre = genreService.addGenre(newGenre);
        return ResponseEntity.status(HttpStatus.CREATED).body(new GenreDTO(savedGenre));
    }
}
