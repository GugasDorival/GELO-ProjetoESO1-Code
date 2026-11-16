package project.gelo.controller;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import project.gelo.dto.LivroBuscaDTO;
import project.gelo.dto.TipoBusca;
import project.gelo.service.GoogleBooksService;

import java.util.ArrayList;
import java.util.List;

/**
 * Pesquisa de livros no Google Books. O termo e o tipo viajam na URL (buscar.xhtml?termo=...&tipo=...),
 * então a página de resultados pode ser atualizada ou compartilhada sem perder a pesquisa.
 */
@Component("livroBean")
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LivroBean {

    private final GoogleBooksService googleBooksService;

    private String termo;
    private String tipo = TipoBusca.TITULO.name();
    private List<LivroBuscaDTO> resultados = new ArrayList<>();
    private boolean pesquisaRealizada;

    public LivroBean(GoogleBooksService googleBooksService) {
        this.googleBooksService = googleBooksService;
    }

    /** Ação do formulário: redireciona para a página de resultados levando termo e tipo na URL. */
    public String pesquisar() {
        return "/view/html/livro/buscar.xhtml?faces-redirect=true&includeViewParams=true";
    }

    /** Executada ao abrir buscar.xhtml (preRenderView). */
    public void carregarResultados(ComponentSystemEvent event) {
        FacesContext contexto = FacesContext.getCurrentInstance();
        if (contexto.getResponseComplete() || contexto.isPostback() || termo == null || termo.isBlank()) {
            return;
        }
        try {
            resultados = googleBooksService.buscar(termo, TipoBusca.de(tipo));
            pesquisaRealizada = true;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), null));
        }
    }

    public String getTermo() { return termo; }
    public void setTermo(String termo) { this.termo = termo == null ? null : termo.trim(); }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = TipoBusca.de(tipo).name(); }
    public TipoBusca[] getTipos() { return TipoBusca.values(); }
    public List<LivroBuscaDTO> getResultados() { return resultados; }
    public boolean isPesquisaRealizada() { return pesquisaRealizada; }
    public boolean isSemResultados() { return pesquisaRealizada && resultados.isEmpty(); }
}
