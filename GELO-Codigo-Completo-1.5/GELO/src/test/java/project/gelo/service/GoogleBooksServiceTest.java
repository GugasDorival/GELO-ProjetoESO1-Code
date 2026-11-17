package project.gelo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import project.gelo.dto.LivroBuscaDTO;
import project.gelo.dto.TipoBusca;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import org.springframework.http.HttpMethod;

/** Testa a integração com o Google Books sem acessar a rede (resposta simulada). */
class GoogleBooksServiceTest {

    private static final String URL = "https://www.googleapis.com/books/v1/volumes";

    private static final String RESPOSTA = """
            {"kind":"books#volumes","totalItems":4,"items":[
              {"id":"abc1","volumeInfo":{"title":"Dom Casmurro","subtitle":"Romance",
                "authors":["Machado de Assis"],"publishedDate":"1899-01-01",
                "imageLinks":{"smallThumbnail":"http://books.google.com/small.jpg","thumbnail":"http://books.google.com/big.jpg"}}},
              {"id":"abc2","volumeInfo":{"title":"Sem dados","publishedDate":"2005"}},
              {"id":"abc1","volumeInfo":{"title":"Duplicado"}},
              {"id":"abc3","volumeInfo":{"authors":["Sem título"]}}
            ]}""";

    private MockRestServiceServer servidor;
    private GoogleBooksService servico;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
        servico = new GoogleBooksService(builder, new ObjectMapper(), URL, 20, "");
    }

    @Test
    void buscaPorTituloUsaOperadorIntitleEConverteResultado() {
        servidor.expect(method(HttpMethod.GET))
                .andExpect(requestTo(org.hamcrest.Matchers.startsWith(URL)))
                .andExpect(queryParam("q", "intitle:dom intitle:casmurro"))
                .andExpect(queryParam("maxResults", "20"))
                .andRespond(withSuccess(RESPOSTA, MediaType.APPLICATION_JSON));

        List<LivroBuscaDTO> livros = servico.buscar("  Dom   Casmurro ", TipoBusca.TITULO);

        servidor.verify();
        assertEquals(2, livros.size());
        LivroBuscaDTO primeiro = livros.get(0);
        assertEquals("Dom Casmurro: Romance", primeiro.titulo());
        assertEquals("Machado de Assis", primeiro.autores());
        assertEquals("1899", primeiro.anoPublicacao());
        assertEquals("https://books.google.com/small.jpg", primeiro.urlCapa());
        assertTrue(primeiro.isPossuiCapa());

        LivroBuscaDTO segundo = livros.get(1);
        assertEquals("Autor desconhecido", segundo.autores());
        assertEquals("2005", segundo.anoPublicacao());
        assertNull(segundo.urlCapa());
    }

    @Test
    void buscaPorAutorUsaOperadorInauthor() {
        servidor.expect(queryParam("q", "inauthor:machado inauthor:de inauthor:assis"))
                .andRespond(withSuccess("{\"totalItems\":0}", MediaType.APPLICATION_JSON));

        assertTrue(servico.buscar("machado de assis", TipoBusca.AUTOR).isEmpty());
        servidor.verify();
    }

    @Test
    void buscaGeralNaoUsaOperador() {
        assertEquals("harry potter", servico.montarConsulta("harry potter", TipoBusca.GERAL));
    }

    @Test
    void termoVazioEIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> servico.buscar("   ", TipoBusca.TITULO));
        assertThrows(IllegalArgumentException.class, () -> servico.buscar(null, TipoBusca.TITULO));
    }

    @Test
    void falhaDaApiViraIllegalState() {
        servidor.expect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> servico.buscar("qualquer", TipoBusca.GERAL));
        assertTrue(ex.getMessage().contains("limite"));
    }

    @Test
    void erroDoGoogleMostraCodigoEMensagem() {
        servidor.expect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\":{\"code\":403,\"message\":\"Books API has not been used in project 1 before or it is disabled.\"}}"));
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> servico.buscar("qualquer", TipoBusca.GERAL));
        assertTrue(ex.getMessage().contains("HTTP 403"));
        assertTrue(ex.getMessage().contains("Books API has not been used"));
    }
}
