package com.example.demo.repository;

import com.example.demo.model.Genero;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneroRepository extends JpaRepository<Genero, Long> {
    Genero findByNombre(String nombre);
}
