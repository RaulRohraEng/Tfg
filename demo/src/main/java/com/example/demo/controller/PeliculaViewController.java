package com.example.demo.controller;

import com.example.demo.model.Genero;
import com.example.demo.model.Pelicula;
import com.example.demo.repository.GeneroRepository;
import com.example.demo.repository.PeliculaRepository;
import com.example.demo.service.PeliculaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class PeliculaViewController {

    @Autowired
    private PeliculaService peliculaService;

    @Autowired
    private GeneroRepository generoRepository;

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

    @PostMapping("/peliculas/buscar-filmaffinity")
    public String buscarYGuardarDesdeWeb(@RequestParam("tituloBusqueda") String titulo, RedirectAttributes redirectAttributes) {
        try {
            // Llamada al endpoint REST que ya existe
            String url = "http://localhost:8080/api/peliculas/buscar/" + URLEncoder.encode(titulo, StandardCharsets.UTF_8);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                redirectAttributes.addFlashAttribute("mensaje", "Película añadida desde FilmAffinity.");
            } else {
                redirectAttributes.addFlashAttribute("error", "No se encontró ninguna película con ese título.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al buscar película: " + e.getMessage());
        }

        return "redirect:/peliculas";
    }


    @PostMapping("/peliculas/eliminar/{id}")
    public String eliminarPelicula(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            peliculaService.eliminarPeliculaConRelaciones(id);
            redirectAttributes.addFlashAttribute("mensaje", "Película eliminada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar la película.");
        }
        return "redirect:/peliculas";
    }

    @GetMapping("/peliculas/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        Pelicula pelicula = peliculaService.buscarPorId(id).orElseThrow();
        List<Genero> todosGeneros = generoRepository.findAll();

        model.addAttribute("pelicula", pelicula);
        model.addAttribute("todosGeneros", todosGeneros);
        return "editar_pelicula";
    }

    @PostMapping("/peliculas/editar/{id}")
    public String actualizarPelicula(@PathVariable Long id,
                                     @RequestParam String titulo,
                                     @RequestParam Integer anio,
                                     @RequestParam Integer duracion,
                                     @RequestParam Double puntuacion,
                                     @RequestParam String sinopsis,
                                     @RequestParam String director,
                                     @RequestParam(value = "generos", required = false) List<String> generosSeleccionados,
                                     RedirectAttributes redirectAttributes) {
        Pelicula pelicula = peliculaService.buscarPorId(id).orElseThrow();

        pelicula.setTitulo(titulo);
        pelicula.setAnio(anio);
        pelicula.setDuracion(duracion);
        pelicula.setPuntuacion(puntuacion);
        pelicula.setSinopsis(sinopsis);

        // Actualización de director (simplificada aquí o puedes delegarla a un servicio)
        pelicula.getDirector().setNombre(director);

        // Actualización de géneros
        Set<Genero> generos = new HashSet<>();
        if (generosSeleccionados != null) {
            for (String nombre : generosSeleccionados) {
                Genero genero = generoRepository.findByNombre(nombre);
                if (genero != null) generos.add(genero);
            }
        }
        pelicula.setGeneros(generos);

        peliculaService.guardar(pelicula);
        redirectAttributes.addFlashAttribute("mensaje", "Película actualizada correctamente.");
        return "redirect:/peliculas";
    }
    @GetMapping("/peliculas/search")
    public String buscarPeliculas(@RequestParam(value = "titulo", required = false) String titulo,
                                  @RequestParam(value = "director", required = false) String director,
                                  @RequestParam(value = "anio", required = false) Integer anio,
                                  @RequestParam(value = "puntuacionMin", required = false) Double puntuacionMin,
                                  @RequestParam(value = "genero", required = false) String genero,
                                  Model model) {
        List<Pelicula> peliculas = peliculaRepository.findAll();

        if (titulo != null && !titulo.isEmpty()) {
            peliculas = peliculas.stream()
                .filter(p -> p.getTitulo().toLowerCase().contains(titulo.toLowerCase()))
                .collect(Collectors.toList());
        }

        if (director != null && !director.isEmpty()) {
            peliculas = peliculas.stream()
                .filter(p -> p.getDirector() != null &&
                             p.getDirector().getNombre().toLowerCase().contains(director.toLowerCase()))
                .collect(Collectors.toList());
        }

        if (anio != null) {
            peliculas = peliculas.stream()
                .filter(p -> p.getAnio() == anio)
                .collect(Collectors.toList());
        }

        if (puntuacionMin != null) {
            peliculas = peliculas.stream()
                .filter(p -> p.getPuntuacion() >= puntuacionMin)
                .collect(Collectors.toList());
        }

        if (genero != null && !genero.isEmpty()) {
            peliculas = peliculas.stream()
                .filter(p -> p.getGeneros().stream().anyMatch(g -> g.getNombre().equalsIgnoreCase(genero)))
                .collect(Collectors.toList());
        }

        model.addAttribute("peliculas", peliculas);
        model.addAttribute("todosGeneros", generoRepository.findAll());
        return "peliculas";
    }
    @GetMapping("/peliculas/estadisticas")
    public String verEstadisticas(Model model) {
        List<Pelicula> peliculas = peliculaRepository.findAll();

        Map<String, Long> datosGenero = peliculas.stream()
            .flatMap(p -> p.getGeneros().stream())
            .collect(Collectors.groupingBy(Genero::getNombre, Collectors.counting()));

        Map<Integer, Long> datosAnio = peliculas.stream()
            .collect(Collectors.groupingBy(Pelicula::getAnio, Collectors.counting()));

        Map<Integer, Double> datosPuntuacion = peliculas.stream()
            .collect(Collectors.groupingBy(
                Pelicula::getAnio,
                Collectors.averagingDouble(Pelicula::getPuntuacion)
            ));

        model.addAttribute("datosGenero", datosGenero);
        model.addAttribute("datosAnio", datosAnio);
        model.addAttribute("datosPuntuacion", datosPuntuacion);

        return "estadisticas";
    }


}