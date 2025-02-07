package de.thws.libraryapi.domain.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "genres")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Genre
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    private String name;
    private String description;

    @OneToMany(mappedBy = "genre")
    @JsonIgnore
    private List<Book> books = new ArrayList<>(); // brauche ich das wirklich? könnte man zu zuordnung von büchern zu einem Genre verwenden, aber eventuell einfach argumentieren, dass man nur eine übersicht von genren haben möchte

    public Genre(){}

    public Genre(String name, String description)
    {
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }
}
