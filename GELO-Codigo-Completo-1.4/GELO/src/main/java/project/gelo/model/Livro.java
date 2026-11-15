package project.gelo.model;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity 
public class Livro {

    @Id 
    private String idGoogle;

    private String titulo;
    private String autor;
    private List<String> genero;

}
