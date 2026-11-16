package project.gelo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity 
public class Livro {

    @Id 
    private String idGoogle;
 
    private String titulo;
    private String autor;
    private String anoPublicacao;
    private String urlCapa;
    
    public String getIdGoogle() {
        return idGoogle;
    }
    public void setIdGoogle(String idGoogle) {
        this.idGoogle = idGoogle;
    }

    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }
    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getAnoPublicacao() {
        return anoPublicacao;
    }
    public void setAnoPublicacao(String anoPublicacao) {
        this.anoPublicacao = anoPublicacao;
    }

    public String getUrlCapa() {
        return urlCapa;
    }
    public void setUrlCapa(String urlCapa) {
        this.urlCapa = urlCapa;
    }
}
