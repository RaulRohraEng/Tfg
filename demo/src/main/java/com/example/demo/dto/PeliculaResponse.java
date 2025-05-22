package com.example.demo.dto;

import java.util.List;

public class PeliculaResponse {
    private Long id;
    private String titulo;
    private String director; // solo el nombre
    private Integer anio;
    private String sinopsis;
    private Integer duracion;
    private Double puntuacion;
    private String imagen;
    
    private String fuenteDatos;
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
	public String getDirector() {
		return director;
	}
	public void setDirector(String director) {
		this.director = director;
	}
	public Integer getAnio() {
		return anio;
	}
	public void setAnio(Integer anio) {
		this.anio = anio;
	}
	public String getSinopsis() {
		return sinopsis;
	}
	public void setSinopsis(String sinopsis) {
		this.sinopsis = sinopsis;
	}
	public Integer getDuracion() {
		return duracion;
	}
	public void setDuracion(Integer duracion) {
		this.duracion = duracion;
	}
	public Double getPuntuacion() {
		return puntuacion;
	}
	public void setPuntuacion(Double puntuacion) {
		this.puntuacion = puntuacion;
	}
	public String getImagen() {
		return imagen;
	}
	public void setImagen(String imagen) {
		this.imagen = imagen;
	}
	public String getFuenteDatos() {
		return fuenteDatos;
	}
	public void setFuenteDatos(String fuenteDatos) {
		this.fuenteDatos = fuenteDatos;
	}
	private List<String> generos;

	public List<String> getGeneros() { return generos; }
	public void setGeneros(List<String> generos) { this.generos = generos; }

}

    // Getters y setters
