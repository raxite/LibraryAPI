package de.thws.libraryapi.dto;

import de.thws.libraryapi.domain.model.Genre;


public class GenreDTO
{
    private Long id;
    private String name;
    private String description;

    public GenreDTO(Genre genre)
    {
        this.id = genre.getId();
        this.name = genre.getName();
        this.description = genre.getDescription();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
