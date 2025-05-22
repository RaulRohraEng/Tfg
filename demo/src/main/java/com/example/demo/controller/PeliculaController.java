package com.example.demo.controller;

import com.example.demo.model.Director;
import com.example.demo.model.Pelicula;
import com.example.demo.dto.PeliculaResponse;
import com.example.demo.repository.DirectorRepository;
import com.example.demo.repository.PeliculaRepository;
import com.example.demo.service.FilmaffinityScraper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/peliculas")
public class PeliculaController {

    private final PeliculaRepository peliculaRepository;
    private final FilmaffinityScraper filmaffinityScraper;
    private final DirectorRepository directorRepository;

    public PeliculaController(PeliculaRepository peliculaRepository,
                              FilmaffinityScraper filmaffinityScraper,
                              DirectorRepository directorRepository) {
        this.peliculaRepository = peliculaRepository;
        this.filmaffinityScraper = filmaffinityScraper;
        this.directorRepository = directorRepository;
    }

    // Obtener todas las películas
    @GetMapping
    public List<Pelicula> getAllPeliculas() {
        return peliculaRepository.findAll();
    }

    // Obtener una película por ID
    @GetMapping("/{id}")
    public ResponseEntity<Pelicula> getPeliculaById(@PathVariable Long id) {
        Optional<Pelicula> pelicula = peliculaRepository.findById(id);
        return pelicula.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Agregar una nueva película manualmente
    @PostMapping
    public ResponseEntity<PeliculaResponse> createPelicula(@RequestBody PeliculaResponse peliculaRequest) {
        Pelicula nuevaPelicula = new Pelicula();
        nuevaPelicula.setTitulo(peliculaRequest.getTitulo());
        nuevaPelicula.setAnio(peliculaRequest.getAnio());
        nuevaPelicula.setDuracion(peliculaRequest.getDuracion());
        nuevaPelicula.setPuntuacion(peliculaRequest.getPuntuacion());
        nuevaPelicula.setSinopsis(peliculaRequest.getSinopsis());
        nuevaPelicula.setImagen(peliculaRequest.getImagen());
        nuevaPelicula.setFuenteDatos(Pelicula.FuenteDatos.Manual); // Se guarda como "Manual"

        // Manejo del director
        Director directorEntity = directorRepository.findByNombre(peliculaRequest.getDirector());
        if (directorEntity == null) {
            directorEntity = new Director();
            directorEntity.setNombre(peliculaRequest.getDirector());
            directorEntity = directorRepository.save(directorEntity);
        }
        nuevaPelicula.setDirector(directorEntity);

        Pelicula peliculaGuardada = peliculaRepository.save(nuevaPelicula);

        // Convertir a DTO de respuesta
        PeliculaResponse response = new PeliculaResponse();
        response.setId(peliculaGuardada.getId());
        response.setTitulo(peliculaGuardada.getTitulo());
        response.setDirector(peliculaGuardada.getDirector().getNombre());
        response.setAnio(peliculaGuardada.getAnio());
        response.setDuracion(peliculaGuardada.getDuracion());
        response.setPuntuacion(peliculaGuardada.getPuntuacion());
        response.setSinopsis(peliculaGuardada.getSinopsis());
        response.setImagen(peliculaGuardada.getImagen());
        response.setFuenteDatos(peliculaGuardada.getFuenteDatos().toString());

        return ResponseEntity.ok(response);
    }


    // Buscar una película en Filmaffinity y guardarla
    @PostMapping("/buscar/{titulo}")
    public ResponseEntity<PeliculaResponse> buscarYGuardarPelicula(@PathVariable String titulo) {
        try {
            FilmaffinityScraper.MovieData movieData = filmaffinityScraper.getMovieData(titulo);
            if (movieData == null) {
                return ResponseEntity.notFound().build();
            }

            Pelicula nuevaPelicula = new Pelicula();
            nuevaPelicula.setTitulo(movieData.titulo);
            nuevaPelicula.setAnio(Integer.parseInt(movieData.anio));
            nuevaPelicula.setDuracion(Integer.parseInt(movieData.duracion.replace(" min.", "").trim()));
            nuevaPelicula.setPuntuacion(Double.parseDouble(movieData.puntuacion.replace(",", ".")));
            nuevaPelicula.setFuenteDatos(Pelicula.FuenteDatos.Filmaffinity);
            nuevaPelicula.setSinopsis(movieData.sinopsis);

            // Manejo del director (suponiendo que director es una entidad)
            Director directorEntity = directorRepository.findByNombre(movieData.director);
            if (directorEntity == null) {
                directorEntity = new Director();
                directorEntity.setNombre(movieData.director);
                directorEntity = directorRepository.save(directorEntity);
            }
            nuevaPelicula.setDirector(directorEntity);

            Pelicula peliculaGuardada = peliculaRepository.save(nuevaPelicula);

            // Mapeo a DTO
            PeliculaResponse response = new PeliculaResponse();
            response.setId(peliculaGuardada.getId());
            response.setTitulo(peliculaGuardada.getTitulo());
            response.setDirector(peliculaGuardada.getDirector().getNombre()); // Solo el nombre
            response.setAnio(peliculaGuardada.getAnio());
            response.setSinopsis(peliculaGuardada.getSinopsis());
            response.setDuracion(peliculaGuardada.getDuracion());
            response.setPuntuacion(peliculaGuardada.getPuntuacion());
            response.setImagen(peliculaGuardada.getImagen());
            response.setFuenteDatos(peliculaGuardada.getFuenteDatos().toString());

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


    // Actualizar una película por ID
    @PutMapping("/{id}")
    public ResponseEntity<PeliculaResponse> updatePelicula(@PathVariable Long id, @RequestBody PeliculaResponse peliculaRequest) {
        return peliculaRepository.findById(id).map(pelicula -> {
            pelicula.setTitulo(peliculaRequest.getTitulo());
            pelicula.setAnio(peliculaRequest.getAnio());
            pelicula.setDuracion(peliculaRequest.getDuracion());
            pelicula.setPuntuacion(peliculaRequest.getPuntuacion());
            pelicula.setSinopsis(peliculaRequest.getSinopsis());
            pelicula.setImagen(peliculaRequest.getImagen());

            // Actualizar director
            Director directorEntity = directorRepository.findByNombre(peliculaRequest.getDirector());
            if (directorEntity == null) {
                directorEntity = new Director();
                directorEntity.setNombre(peliculaRequest.getDirector());
                directorEntity = directorRepository.save(directorEntity);
            }
            pelicula.setDirector(directorEntity);

            Pelicula updatedPelicula = peliculaRepository.save(pelicula);

            // Convertir a DTO de respuesta
            PeliculaResponse response = new PeliculaResponse();
            response.setId(updatedPelicula.getId());
            response.setTitulo(updatedPelicula.getTitulo());
            response.setDirector(updatedPelicula.getDirector().getNombre());
            response.setAnio(updatedPelicula.getAnio());
            response.setDuracion(updatedPelicula.getDuracion());
            response.setPuntuacion(updatedPelicula.getPuntuacion());
            response.setSinopsis(updatedPelicula.getSinopsis());
            response.setImagen(updatedPelicula.getImagen());
            response.setFuenteDatos(updatedPelicula.getFuenteDatos().toString());

            return ResponseEntity.ok(response);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }


    // Eliminar una película
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePelicula(@PathVariable Long id) {
        if (peliculaRepository.existsById(id)) {
            peliculaRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    

}
