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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
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


@GetMapping
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Page<GenreDTO>> getAllGenres(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size,
        @RequestParam(defaultValue = "id:asc") String sort) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(parseSortOrders(sort)));
    Page<Genre> genresPage = genreService.getAllGenres(pageable);
    Page<GenreDTO> genreDTOPage = genresPage.map(GenreDTO::new);

    return ResponseEntity.ok(genreDTOPage);
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
    private List<Sort.Order> parseSortOrders(String sort) {
        return Arrays.stream(sort.split(","))
                .map(s -> {
                    String[] sortParams = s.split(":");
                    String property = sortParams[0].trim();
                    Sort.Direction direction = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc"))
                            ? Sort.Direction.DESC
                            : Sort.Direction.ASC;
                    return new Sort.Order(direction, property);
                })
                .collect(Collectors.toList());
    }

    //eventuell paging anpassen das es komma statt : unterstützt oder andersherum einheitlich machen
}
