package com.example.demo.model;

import jakarta.persistence.*;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "generos")
public class Genero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    
    @ManyToMany(mappedBy = "generos")
    @JsonBackReference
    private Set<Pelicula> peliculas;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Set<Pelicula> getPeliculas() { return peliculas; }
    public void setPeliculas(Set<Pelicula> peliculas) { this.peliculas = peliculas; }
}
