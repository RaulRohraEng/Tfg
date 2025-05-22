package com.example.demo.controller;

import com.example.demo.model.Director;
import com.example.demo.model.Genero;
import com.example.demo.model.Pelicula;
import com.example.demo.dto.PeliculaResponse;
import com.example.demo.repository.DirectorRepository;
import com.example.demo.repository.GeneroRepository;
import com.example.demo.repository.PeliculaRepository;
import com.example.demo.service.FilmaffinityScraper;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/peliculas")
public class PeliculaController {

    private final PeliculaRepository peliculaRepository;
    private final FilmaffinityScraper filmaffinityScraper;
    private final DirectorRepository directorRepository;
    private final GeneroRepository generoRepository;

    public PeliculaController(PeliculaRepository peliculaRepository,
                              FilmaffinityScraper filmaffinityScraper,
                              DirectorRepository directorRepository,
                              GeneroRepository generoRepository) {
        this.peliculaRepository = peliculaRepository;
        this.filmaffinityScraper = filmaffinityScraper;
        this.directorRepository = directorRepository;
        this.generoRepository = generoRepository;
    }

    @GetMapping
    public List<Pelicula> getAllPeliculas() {
        return peliculaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pelicula> getPeliculaById(@PathVariable Long id) {
        return peliculaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PeliculaResponse> createPelicula(@RequestBody PeliculaResponse peliculaRequest) {
        Pelicula nuevaPelicula = new Pelicula();
        nuevaPelicula.setTitulo(peliculaRequest.getTitulo());
        nuevaPelicula.setAnio(peliculaRequest.getAnio());
        nuevaPelicula.setDuracion(peliculaRequest.getDuracion());
        nuevaPelicula.setPuntuacion(peliculaRequest.getPuntuacion());
        nuevaPelicula.setSinopsis(peliculaRequest.getSinopsis());
        nuevaPelicula.setImagen(peliculaRequest.getImagen());
        nuevaPelicula.setFuenteDatos(Pelicula.FuenteDatos.Manual);

        // Director
        Director director = directorRepository.findByNombre(peliculaRequest.getDirector());
        if (director == null) {
            director = new Director();
            director.setNombre(peliculaRequest.getDirector());
            director = directorRepository.save(director);
        }
        nuevaPelicula.setDirector(director);

        // Géneros
        Set<Genero> generos = peliculaRequest.getGeneros().stream().map(nombre -> {
            Genero genero = generoRepository.findByNombre(nombre);
            if (genero == null) {
                genero = new Genero();
                genero.setNombre(nombre);
                genero = generoRepository.save(genero);
            }
            return genero;
        }).collect(Collectors.toSet());
        nuevaPelicula.setGeneros(generos);

        Pelicula guardada = peliculaRepository.save(nuevaPelicula);

        return ResponseEntity.ok(mapToResponse(guardada));
    }

    @PostMapping("/buscar/{titulo}")
    public ResponseEntity<PeliculaResponse> buscarYGuardarPelicula(@PathVariable String titulo) {
        try {
            FilmaffinityScraper.MovieData movieData = filmaffinityScraper.getMovieData(titulo);
            if (movieData == null) return ResponseEntity.notFound().build();

            Pelicula nuevaPelicula = new Pelicula();
            nuevaPelicula.setTitulo(movieData.titulo);
            nuevaPelicula.setAnio(Integer.parseInt(movieData.anio));
            nuevaPelicula.setDuracion(Integer.parseInt(movieData.duracion));
            nuevaPelicula.setPuntuacion(Double.parseDouble(movieData.puntuacion));
            nuevaPelicula.setFuenteDatos(Pelicula.FuenteDatos.Filmaffinity);
            nuevaPelicula.setSinopsis(movieData.sinopsis);

            Director director = directorRepository.findByNombre(movieData.director);
            if (director == null) {
                director = new Director();
                director.setNombre(movieData.director);
                director = directorRepository.save(director);
            }
            nuevaPelicula.setDirector(director);

            Set<Genero> generos = movieData.generos.stream().map(nombre -> {
                Genero genero = generoRepository.findByNombre(nombre);
                if (genero == null) {
                    genero = new Genero();
                    genero.setNombre(nombre);
                    genero = generoRepository.save(genero);
                }
                return genero;
            }).collect(Collectors.toSet());
            nuevaPelicula.setGeneros(generos);

            Pelicula guardada = peliculaRepository.save(nuevaPelicula);

            return ResponseEntity.ok(mapToResponse(guardada));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PeliculaResponse> updatePelicula(@PathVariable Long id, @RequestBody PeliculaResponse peliculaRequest) {
        return peliculaRepository.findById(id).map(pelicula -> {
            pelicula.setTitulo(peliculaRequest.getTitulo());
            pelicula.setAnio(peliculaRequest.getAnio());
            pelicula.setDuracion(peliculaRequest.getDuracion());
            pelicula.setPuntuacion(peliculaRequest.getPuntuacion());
            pelicula.setSinopsis(peliculaRequest.getSinopsis());
            pelicula.setImagen(peliculaRequest.getImagen());

            Director director = directorRepository.findByNombre(peliculaRequest.getDirector());
            if (director == null) {
                director = new Director();
                director.setNombre(peliculaRequest.getDirector());
                director = directorRepository.save(director);
            }
            pelicula.setDirector(director);

            Set<Genero> generos = peliculaRequest.getGeneros().stream().map(nombre -> {
                Genero genero = generoRepository.findByNombre(nombre);
                if (genero == null) {
                    genero = new Genero();
                    genero.setNombre(nombre);
                    genero = generoRepository.save(genero);
                }
                return genero;
            }).collect(Collectors.toSet());
            pelicula.setGeneros(generos);

            Pelicula actualizada = peliculaRepository.save(pelicula);

            return ResponseEntity.ok(mapToResponse(actualizada));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePelicula(@PathVariable Long id) {
        if (peliculaRepository.existsById(id)) {
            peliculaRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private PeliculaResponse mapToResponse(Pelicula pelicula) {
        PeliculaResponse response = new PeliculaResponse();
        response.setId(pelicula.getId());
        response.setTitulo(pelicula.getTitulo());
        response.setDirector(pelicula.getDirector().getNombre());
        response.setAnio(pelicula.getAnio());
        response.setDuracion(pelicula.getDuracion());
        response.setPuntuacion(pelicula.getPuntuacion());
        response.setSinopsis(pelicula.getSinopsis());
        response.setImagen(pelicula.getImagen());
        response.setFuenteDatos(pelicula.getFuenteDatos().toString());
        response.setGeneros(pelicula.getGeneros().stream().map(Genero::getNombre).collect(Collectors.toList()));
        return response;
    }
}
