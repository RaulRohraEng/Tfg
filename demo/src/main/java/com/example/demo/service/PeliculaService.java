package com.example.demo.service;

import com.example.demo.model.Pelicula;
import com.example.demo.repository.PeliculaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PeliculaService {

    @Autowired
    private PeliculaRepository peliculaRepository;

    public void eliminarPeliculaConRelaciones(Long id) {
        Pelicula pelicula = peliculaRepository.findById(id).orElseThrow();
        // Si tienes entidades relacionadas como ArchivoVideo, elimina también allí.
        peliculaRepository.delete(pelicula);
    }

    public Optional<Pelicula> buscarPorId(Long id) {
        return peliculaRepository.findById(id);
    }

    public Pelicula guardar(Pelicula pelicula) {
        return peliculaRepository.save(pelicula);
    }

}
