package project.gelo.controller;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.context.annotation.ScopedProxyMode;
import project.gelo.dto.UsuarioDTO;
import project.gelo.model.TipoUsuario;
import project.gelo.service.UsuarioService;

import java.io.IOException;

@Component("loginBean")
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LoginBean {

    private final UsuarioService usuarioService;

    private String email;
    private String senha;
    private UsuarioDTO usuarioLogado;

    public LoginBean(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public String entrar() {
        try {
            usuarioLogado = usuarioService.autenticar(email, senha);
            Object requisicao = FacesContext.getCurrentInstance().getExternalContext().getRequest();
            if (requisicao instanceof HttpServletRequest httpServletRequest) {
                httpServletRequest.changeSessionId();
            }
            senha = null;
            return "/home.xhtml?faces-redirect=true";
        } catch (IllegalArgumentException ex) {
            senha = null;
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, ex.getMessage());
            return null;
        }
    }

    public String sair() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }

    public void exigirAutenticacao(ComponentSystemEvent event) {
        if (usuarioLogado == null) {
            redirecionar("/login.xhtml");
        }
    }

    public void exigirAdministrador(ComponentSystemEvent event) {
        if (usuarioLogado == null) {
            redirecionar("/login.xhtml");
        } else if (!isAdministrador()) {
            redirecionar("/home.xhtml");
        }
    }

    private void redirecionar(String caminho) {
        FacesContext contexto = FacesContext.getCurrentInstance();
        String destino = contexto.getExternalContext().getRequestContextPath() + caminho;
        try {
            contexto.getExternalContext().redirect(destino);
            contexto.responseComplete();
        } catch (IOException ex) {
            throw new IllegalStateException("Não foi possível redirecionar a página.", ex);
        }
    }

    public boolean isAdministrador() {
        return usuarioLogado != null && usuarioLogado.tipoUsuario() == TipoUsuario.ADMINISTRADOR;
    }

    public boolean isLeitor() {
        return usuarioLogado != null && usuarioLogado.tipoUsuario() == TipoUsuario.LEITOR;
    }

    public Integer getIdUsuarioLogado() {
        return usuarioLogado == null ? null : usuarioLogado.id();
    }

    public void atualizarUsuarioLogado(UsuarioDTO usuarioAtualizado) {
        if (usuarioLogado != null && usuarioAtualizado != null
                && usuarioLogado.id().equals(usuarioAtualizado.id())) {
            this.usuarioLogado = usuarioAtualizado;
        }
    }

    private void adicionarMensagem(FacesMessage.Severity severidade, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidade, texto, null));
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public UsuarioDTO getUsuarioLogado() { return usuarioLogado; }
}
