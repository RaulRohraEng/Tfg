package com.example.demo.controller;


import com.example.demo.model.Pelicula;
import com.example.demo.repository.PeliculaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PeliculaViewController {

    private final PeliculaRepository peliculaRepository;

    public PeliculaViewController(PeliculaRepository peliculaRepository) {
        this.peliculaRepository = peliculaRepository;
    }

    @GetMapping("/peliculas")
    public String listarPeliculas(Model model) {
        List<Pelicula> peliculas = peliculaRepository.findAll();
        model.addAttribute("peliculas", peliculas);
        return "peliculas";
    }

    @GetMapping("/peliculas/search")
    public String buscarPeliculas(@RequestParam(value = "titulo", required = false) String titulo,
                                  @RequestParam(value = "anio", required = false) Integer anio,
                                  Model model) {
        List<Pelicula> peliculas = peliculaRepository.findAll();

        // Filtrar por título si se proporciona
        if (titulo != null && !titulo.isEmpty()) {
            peliculas = peliculas.stream()
                .filter(p -> p.getTitulo().toLowerCase().contains(titulo.toLowerCase()))
                .collect(Collectors.toList());
        }

        // Filtrar por año si se proporciona
        if (anio != null) {
            peliculas = peliculas.stream()
                .filter(p -> p.getAnio() == anio)
                .collect(Collectors.toList());
        }

        model.addAttribute("peliculas", peliculas);
        return "peliculas";
    }
}
