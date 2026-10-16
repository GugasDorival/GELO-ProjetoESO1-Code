package project.gelo.controller;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import project.gelo.dto.UsuarioDTO;
import project.gelo.service.UsuarioService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component("usuarioBean")
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UsuarioBean {

    private final UsuarioService usuarioService;
    private final LoginBean loginBean;

    private String novoNome;
    private String novoEmail;
    private String novaSenha;

    // Campos para editar outro usuário na área administrativa.
    private String nomeEdicao;
    private String emailEdicao;
    private String senhaEdicao;
    private Integer usuarioEditandoId;

    // Campos separados para "Minha conta", para não interferir na edição administrativa.
    private String nomeConta;
    private String emailConta;
    private String senhaConta;
    private Integer contaCarregadaPara;

    private List<UsuarioDTO> usuarios = new ArrayList<>();

    public UsuarioBean(UsuarioService usuarioService, LoginBean loginBean) {
        this.usuarioService = usuarioService;
        this.loginBean = loginBean;
    }

    /** Cadastro público: sempre cria uma conta do tipo LEITOR. */
    public String registrarLeitor() {
        try {
            usuarioService.criarLeitor(novoNome, novoEmail, novaSenha);
            limparNovoUsuario();
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            mensagem(FacesMessage.SEVERITY_INFO, "Conta criada com sucesso. Entre com seu e-mail e senha.");
            return "/login.xhtml?faces-redirect=true";
        } catch (IllegalArgumentException ex) {
            novaSenha = null;
            mensagem(FacesMessage.SEVERITY_ERROR, ex.getMessage());
            return null;
        }
    }

    public String criarLeitorPeloAdministrador() {
        if (!loginBean.isAdministrador()) {
            mensagem(FacesMessage.SEVERITY_ERROR, "Somente um administrador pode cadastrar usuários nesta tela.");
            return null;
        }
        try {
            usuarioService.criarLeitor(novoNome, novoEmail, novaSenha);
            limparNovoUsuario();
            carregarListaInterna();
            mensagem(FacesMessage.SEVERITY_INFO, "Conta de leitor criada com sucesso.");
        } catch (IllegalArgumentException ex) {
            novaSenha = null;
            mensagem(FacesMessage.SEVERITY_ERROR, ex.getMessage());
        }
        return null;
    }

    public void carregarUsuarios(ComponentSystemEvent event) {
        if (FacesContext.getCurrentInstance().getResponseComplete()) {
            return;
        }
        if (!loginBean.isAdministrador()) {
            redirecionar(loginBean.getUsuarioLogado() == null ? "/login.xhtml" : "/home.xhtml");
            return;
        }
        carregarListaInterna();
    }

    private void carregarListaInterna() {
        usuarios = usuarioService.listarTodos();
    }

    public String selecionarParaEditar(Integer id) {
        if (!loginBean.isAdministrador()) {
            mensagem(FacesMessage.SEVERITY_ERROR, "Acesso permitido somente a administradores.");
            return null;
        }
        if (id != null && id.equals(loginBean.getIdUsuarioLogado())) {
            mensagem(FacesMessage.SEVERITY_WARN, "Para editar sua própria conta, utilize a opção Minha conta.");
            return null;
        }
        try {
            UsuarioDTO usuario = usuarioService.buscarPorId(id);
            usuarioEditandoId = usuario.id();
            nomeEdicao = usuario.nome();
            emailEdicao = usuario.email();
            senhaEdicao = "";
        } catch (IllegalArgumentException ex) {
            mensagem(FacesMessage.SEVERITY_ERROR, ex.getMessage());
        }
        return null;
    }

    public String salvarEdicaoAdministrativa() {
        if (!loginBean.isAdministrador()) {
            mensagem(FacesMessage.SEVERITY_ERROR, "Acesso permitido somente a administradores.");
            return null;
        }
        if (usuarioEditandoId == null) {
            mensagem(FacesMessage.SEVERITY_ERROR, "Selecione uma conta antes de salvar.");
            return null;
        }
        try {
            usuarioService.atualizar(usuarioEditandoId, nomeEdicao, emailEdicao, senhaEdicao,
                    loginBean.getIdUsuarioLogado(), true);
            limparEdicaoAdministrativa();
            carregarListaInterna();
            mensagem(FacesMessage.SEVERITY_INFO, "Conta atualizada com sucesso.");
        } catch (IllegalArgumentException ex) {
            senhaEdicao = "";
            mensagem(FacesMessage.SEVERITY_ERROR, ex.getMessage());
        }
        return null;
    }

    public String cancelarEdicaoAdministrativa() {
        limparEdicaoAdministrativa();
        return null;
    }

    private void limparEdicaoAdministrativa() {
        usuarioEditandoId = null;
        nomeEdicao = null;
        emailEdicao = null;
        senhaEdicao = null;
    }

    public String excluirUsuario(Integer id) {
        if (!loginBean.isAdministrador()) {
            mensagem(FacesMessage.SEVERITY_ERROR, "Acesso permitido somente a administradores.");
            return null;
        }
        try {
            usuarioService.excluir(id, loginBean.getIdUsuarioLogado(), true);
            if (id != null && id.equals(usuarioEditandoId)) {
                limparEdicaoAdministrativa();
            }
            carregarListaInterna();
            mensagem(FacesMessage.SEVERITY_INFO, "Conta excluída com sucesso.");
        } catch (IllegalArgumentException ex) {
            mensagem(FacesMessage.SEVERITY_ERROR, ex.getMessage());
        }
        return null;
    }

    public void carregarMinhaConta(ComponentSystemEvent event) {
        if (FacesContext.getCurrentInstance().getResponseComplete()) {
            return;
        }
        UsuarioDTO atual = loginBean.getUsuarioLogado();
        if (atual == null) {
            redirecionar("/login.xhtml");
            return;
        }
        if (!atual.id().equals(contaCarregadaPara)) {
            UsuarioDTO dados = usuarioService.buscarPorId(atual.id());
            nomeConta = dados.nome();
            emailConta = dados.email();
            senhaConta = "";
            contaCarregadaPara = dados.id();
        }
    }

    public String salvarMinhaConta() {
        UsuarioDTO atual = loginBean.getUsuarioLogado();
        if (atual == null) {
            return "/login.xhtml?faces-redirect=true";
        }
        try {
            UsuarioDTO atualizado = usuarioService.atualizar(atual.id(), nomeConta, emailConta, senhaConta,
                    atual.id(), false);
            loginBean.atualizarUsuarioLogado(atualizado);
            nomeConta = atualizado.nome();
            emailConta = atualizado.email();
            senhaConta = "";
            mensagem(FacesMessage.SEVERITY_INFO, "Sua conta foi atualizada com sucesso.");
            return null;
        } catch (IllegalArgumentException ex) {
            senhaConta = "";
            mensagem(FacesMessage.SEVERITY_ERROR, ex.getMessage());
            return null;
        }
    }

    public String excluirMinhaConta() {
        UsuarioDTO atual = loginBean.getUsuarioLogado();
        if (atual == null) {
            return "/login.xhtml?faces-redirect=true";
        }
        try {
            usuarioService.excluir(atual.id(), atual.id(), false);
            return loginBean.sair();
        } catch (IllegalArgumentException ex) {
            mensagem(FacesMessage.SEVERITY_ERROR, ex.getMessage());
            return null;
        }
    }

    private void limparNovoUsuario() {
        novoNome = null;
        novoEmail = null;
        novaSenha = null;
    }

    private void mensagem(FacesMessage.Severity severidade, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidade, texto, null));
    }

    private void redirecionar(String caminho) {
        FacesContext contexto = FacesContext.getCurrentInstance();
        try {
            contexto.getExternalContext().redirect(contexto.getExternalContext().getRequestContextPath() + caminho);
            contexto.responseComplete();
        } catch (IOException ex) {
            throw new IllegalStateException("Não foi possível redirecionar a página.", ex);
        }
    }

    public String getNovoNome() { return novoNome; }
    public void setNovoNome(String novoNome) { this.novoNome = novoNome; }
    public String getNovoEmail() { return novoEmail; }
    public void setNovoEmail(String novoEmail) { this.novoEmail = novoEmail; }
    public String getNovaSenha() { return novaSenha; }
    public void setNovaSenha(String novaSenha) { this.novaSenha = novaSenha; }
    public String getNomeEdicao() { return nomeEdicao; }
    public void setNomeEdicao(String nomeEdicao) { this.nomeEdicao = nomeEdicao; }
    public String getEmailEdicao() { return emailEdicao; }
    public void setEmailEdicao(String emailEdicao) { this.emailEdicao = emailEdicao; }
    public String getSenhaEdicao() { return senhaEdicao; }
    public void setSenhaEdicao(String senhaEdicao) { this.senhaEdicao = senhaEdicao; }
    public Integer getUsuarioEditandoId() { return usuarioEditandoId; }
    public String getNomeConta() { return nomeConta; }
    public void setNomeConta(String nomeConta) { this.nomeConta = nomeConta; }
    public String getEmailConta() { return emailConta; }
    public void setEmailConta(String emailConta) { this.emailConta = emailConta; }
    public String getSenhaConta() { return senhaConta; }
    public void setSenhaConta(String senhaConta) { this.senhaConta = senhaConta; }
    public List<UsuarioDTO> getUsuarios() { return usuarios; }
}
