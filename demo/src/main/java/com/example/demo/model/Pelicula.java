package com.example.demo.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;

@Entity
@Table(name = "peliculas")
public class Pelicula {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    
    @ManyToOne
    @JoinColumn(name = "director_id", nullable = true)
    private Director director;

    private int anio;
    
    @Column(columnDefinition = "TEXT")
    private String sinopsis;

    private int duracion;
    private double puntuacion;
    private String imagen;
    
    @Enumerated(EnumType.STRING)
    private FuenteDatos fuenteDatos;

    // Getters y Setters
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public Director getDirector() {
		return director;
	}

	public void setDirector(Director director) {
		this.director = director;
	}

	public int getAnio() {
		return anio;
	}

	public void setAnio(int anio) {
		this.anio = anio;
	}

	public String getSinopsis() {
		return sinopsis;
	}

	public void setSinopsis(String sinopsis) {
		this.sinopsis = sinopsis;
	}

	public int getDuracion() {
		return duracion;
	}

	public void setDuracion(int duracion) {
		this.duracion = duracion;
	}

	public double getPuntuacion() {
		return puntuacion;
	}

	public void setPuntuacion(double puntuacion) {
		this.puntuacion = puntuacion;
	}

	public String getImagen() {
		return imagen;
	}

	public void setImagen(String imagen) {
		this.imagen = imagen;
	}

	public FuenteDatos getFuenteDatos() {
		return fuenteDatos;
	}

	public void setFuenteDatos(FuenteDatos fuenteDatos) {
		this.fuenteDatos = fuenteDatos;
	}
    
    public enum FuenteDatos {
        Manual, Filmaffinity
    }
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
        name = "pelicula_genero",
        joinColumns = @JoinColumn(name = "pelicula_id"),
        inverseJoinColumns = @JoinColumn(name = "genero_id")
    )
    @JsonManagedReference
    private Set<Genero> generos = new java.util.HashSet<>();

    public Set<Genero> getGeneros() { return generos; }
    public void setGeneros(Set<Genero> generos) { this.generos = generos; }
}
