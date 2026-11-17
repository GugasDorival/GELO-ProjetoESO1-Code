package project.gelo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import project.gelo.dto.LivroBuscaDTO;
import project.gelo.dto.TipoBusca;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Consulta livros na API pública Google Books (volumes). */
@Service
public class GoogleBooksService {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleBooksService.class);
    private static final Pattern ANO = Pattern.compile("^(\\d{4})");
    private static final int LIMITE_API = 40;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String urlBase;
    private final int maxResultados;
    private final String chaveApi;

    public GoogleBooksService(RestClient.Builder restClientBuilder,
                              ObjectMapper objectMapper,
                              @Value("${gelo.google-books.url:https://www.googleapis.com/books/v1/volumes}") String urlBase,
                              @Value("${gelo.google-books.max-resultados:20}") int maxResultados,
                              @Value("${gelo.google-books.api-key:}") String chaveApi) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.urlBase = urlBase;
        this.maxResultados = Math.max(1, Math.min(maxResultados, LIMITE_API));
        this.chaveApi = chaveApi;
    }

    /**
     * Busca livros pelo termo informado.
     *
     * @throws IllegalArgumentException se o termo estiver vazio
     * @throws IllegalStateException    se o Google Books não puder ser consultado
     */
    public List<LivroBuscaDTO> buscar(String termo, TipoBusca tipo) {
        if (termo == null || termo.isBlank()) {
            throw new IllegalArgumentException("Informe o nome de um livro ou de um autor para pesquisar.");
        }
        URI uri = montarUri(termo, tipo);
        try {
            String json = restClient.get().uri(uri).retrieve().body(String.class);
            return converter(json);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 429) {
                throw new IllegalStateException(
                        "O limite de consultas ao Google Books foi atingido. Tente novamente em alguns instantes.", ex);
            }
            // A chave nunca é registrada: só o código HTTP e a mensagem devolvida pelo Google.
            String detalhe = detalheErro(ex);
            int status = ex.getStatusCode().value();
            LOG.warn("Google Books respondeu HTTP {}: {}", status, detalhe);
            throw new IllegalStateException("O Google Books recusou a consulta (HTTP " + status + ")"
                    + (detalhe == null ? "." : ": " + detalhe), ex);
        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Não foi possível consultar o Google Books agora. Verifique a conexão e tente novamente.", ex);
        }
    }

    private String detalheErro(RestClientResponseException ex) {
        try {
            return texto(objectMapper.readTree(ex.getResponseBodyAsString()).path("error"), "message");
        } catch (Exception e) {
            return null;
        }
    }

    /** Monta a URL da consulta, aplicando o operador de busca conforme o tipo escolhido. */
    URI montarUri(String termo, TipoBusca tipo) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(urlBase)
                .queryParam("q", "{q}")
                .queryParam("maxResults", maxResultados)
                .queryParam("printType", "books")
                .queryParam("key", "{key}");
                
        return builder.encode().build(montarConsulta(termo, tipo), chaveApi.trim());
    }

    /**
     * Um operador por palavra (intitle:dom intitle:casmurro), pois o Google Books só aplica
     * o operador à palavra imediatamente seguinte.
     */
    String montarConsulta(String termo, TipoBusca tipo) {
        String limpo = termo.trim().replaceAll("\\s+", " ");
        TipoBusca efetivo = tipo == null ? TipoBusca.GERAL : tipo;
        String operador = switch (efetivo) {
            case TITULO -> "intitle:";
            case AUTOR -> "inauthor:";
            case GERAL -> "";
        };
        if (operador.isEmpty()) {
            return limpo;
        }
        StringBuilder consulta = new StringBuilder();
        for (String palavra : limpo.split(" ")) {
            if (consulta.length() > 0) {
                consulta.append(' ');
            }
            consulta.append(operador).append(palavra);
        }
        return consulta.toString();
    }

    /** Converte o JSON da API em livros para exibição; itens sem título são ignorados. */
    List<LivroBuscaDTO> converter(String json) {
        List<LivroBuscaDTO> livros = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return livros;
        }
        JsonNode itens;
        try {
            itens = objectMapper.readTree(json).path("items");
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("A resposta do Google Books não pôde ser interpretada.", ex);
        }
        Set<String> idsVistos = new LinkedHashSet<>();
        for (JsonNode item : itens) {
            JsonNode info = item.path("volumeInfo");
            String titulo = texto(info, "title");
            String id = texto(item, "id");
            if (titulo == null || (id != null && !idsVistos.add(id))) {
                continue;
            }
            String subtitulo = texto(info, "subtitle");
            if (subtitulo != null) {
                titulo = titulo + ": " + subtitulo;
            }
            livros.add(new LivroBuscaDTO(id, titulo, autores(info.path("authors")),
                    ano(texto(info, "publishedDate")), capa(info.path("imageLinks"))));
        }
        return livros;
    }

    private static String autores(JsonNode autores) {
        List<String> nomes = new ArrayList<>();
        for (JsonNode autor : autores) {
            if (autor.isTextual() && !autor.asText().isBlank()) {
                nomes.add(autor.asText().trim());
            }
        }
        return nomes.isEmpty() ? "Autor desconhecido" : String.join(", ", nomes);
    }

    private static String ano(String dataPublicacao) {
        if (dataPublicacao != null) {
            Matcher m = ANO.matcher(dataPublicacao);
            if (m.find()) {
                return m.group(1);
            }
        }
        return "—";
    }

    /** Prefere a miniatura pequena e força HTTPS (a API costuma devolver links http). */
    private static String capa(JsonNode links) {
        String url = texto(links, "smallThumbnail");
        if (url == null) {
            url = texto(links, "thumbnail");
        }
        if (url == null) {
            return null;
        }
        return url.startsWith("http://") ? "https://" + url.substring("http://".length()) : url;
    }

    private static String texto(JsonNode no, String campo) {
        JsonNode valor = no.path(campo);
        if (valor.isTextual() && !valor.asText().isBlank()) {
            return valor.asText().trim();
        }
        return null;
    }
}
