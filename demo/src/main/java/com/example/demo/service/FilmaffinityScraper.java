package com.example.demo.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmaffinityScraper {

    private static final String SEARCH_URL = "https://www.filmaffinity.com/es/search.php?stext=";

    public String getMovieUrl(String titulo) throws IOException {
        String encodedTitle = URLEncoder.encode(titulo, StandardCharsets.UTF_8);
        String searchUrl = SEARCH_URL + encodedTitle;

        Document doc = Jsoup.connect(searchUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .get();

        Element firstResult = doc.select("div.mc-title a").first();
        return (firstResult != null) ? firstResult.absUrl("href") : null;
    }

    public MovieData getMovieData(String titulo) throws IOException {
        String movieUrl = getMovieUrl(titulo);
        if (movieUrl == null) {
            return null;
        }

        Document doc = Jsoup.connect(movieUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .get();


     // Título original
        Element titleElement = doc.select("dl.movie-info dd").first();
        String title = (titleElement != null) ? titleElement.text() : "";


        // Extraer el director correctamente
        Element directorElement = doc.select("dd.directors span[itemprop=name]").first();
        String director = (directorElement != null) ? directorElement.text() : null;

        // Extraer año
        String year = doc.select("dd[itemprop=datePublished]").text();

        // Extraer duración
        Element durationElement = doc.select("dd[itemprop=duration]").first();
        String duration = (durationElement != null) ? durationElement.text().replace(" min.", "") : "0";

        // Extraer puntuación
        Element ratingElement = doc.select("#movie-rat-avg").first();
        String rating = (ratingElement != null) ? ratingElement.text().replace(",", ".") : "0";
        
        Element sinopsisElement = doc.select("dd[itemprop=description]").first();
        String sinopsis = (sinopsisElement != null) ? sinopsisElement.text() : "";

        Element generoContainer = doc.selectFirst("dd.card-genres");
        List<String> generos = generoContainer != null
                ? generoContainer.select("a").stream()
                    .map(Element::text)
                    .collect(Collectors.toList())
                : List.of();


        // Imprimir valores en consola para depuración
        System.out.println("Título: " + title);
        System.out.println("Director: " + director);
        System.out.println("Año: " + year);
        System.out.println("Duración: " + duration);
        System.out.println("Puntuación: " + rating);
        System.out.println("URL: " + movieUrl);
        System.out.println("Genero: " + generos);

        return new MovieData(title, director, year, duration, rating, movieUrl, sinopsis, generos);
    }

    public static class MovieData {
        public String titulo;
        public String director;
        public String anio;
        public String duracion;
        public String puntuacion;
        public String url;
        public String sinopsis; // <--- Nuevo campo
        public List<String> generos;

        public MovieData(String titulo, String director, String anio, String duracion, String puntuacion, String url, String sinopsis, List<String> generos) {
            this.titulo = titulo;
            this.director = director;
            this.anio = anio;
            this.duracion = duracion;
            this.puntuacion = puntuacion;
            this.url = url;
            this.sinopsis = sinopsis;
            this.generos = generos;
        }
    }
}
