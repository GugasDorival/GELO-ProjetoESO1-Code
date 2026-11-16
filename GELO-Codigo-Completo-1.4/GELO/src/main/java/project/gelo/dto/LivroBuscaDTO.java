package project.gelo.dto;

/**
 * Dados de um livro retornado pela busca no Google Books, prontos para exibição.
 *
 * @param idGoogle      identificador do volume no Google Books
 * @param titulo        título (com subtítulo, quando houver)
 * @param autores       autores separados por vírgula, ou "Autor desconhecido"
 * @param anoPublicacao ano de publicação, ou "—" quando não informado
 * @param urlCapa       URL (HTTPS) da miniatura da capa; nulo quando o livro não tem capa
 */
public record LivroBuscaDTO(String idGoogle, String titulo, String autores, String anoPublicacao, String urlCapa) {

    public String getIdGoogle() { return idGoogle; }
    public String getTitulo() { return titulo; }
    public String getAutores() { return autores; }
    public String getAnoPublicacao() { return anoPublicacao; }
    public String getUrlCapa() { return urlCapa; }

    public boolean isPossuiCapa() {
        return urlCapa != null && !urlCapa.isBlank();
    }
} 
 