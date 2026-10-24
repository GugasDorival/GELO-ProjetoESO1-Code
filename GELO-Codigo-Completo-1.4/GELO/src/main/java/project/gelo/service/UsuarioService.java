package project.gelo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.gelo.dto.UsuarioDTO;
import project.gelo.model.TipoUsuario;
import project.gelo.model.Usuario;
import project.gelo.repository.UsuarioRepository;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class UsuarioService {

    private static final int SENHA_MINIMA = 6;
    private static final int SENHA_MAXIMA = 20;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Cadastro público cria apenas leitores, conforme a observação do modelo Astah. */
    public UsuarioDTO criarLeitor(String nome, String email, String senha) {
        validarNome(nome);
        String emailNormalizado = validarEmail(email);
        validarSenha(senha);

        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new IllegalArgumentException("Já existe uma conta cadastrada com esse e-mail.");
        }

        Usuario usuario = new Usuario(
                nome.trim(),
                emailNormalizado,
                passwordEncoder.encode(senha),
                TipoUsuario.LEITOR
        );
        return UsuarioDTO.from(usuarioRepository.save(usuario));
    }

    /** Autentica pelo e-mail e senha e retorna um DTO sem o hash de senha. */
    @Transactional(readOnly = true)
    public UsuarioDTO autenticar(String email, String senha) {
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail e a senha.");
        }

        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
                .orElseThrow(() -> new IllegalArgumentException("E-mail ou senha inválidos."));

        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            throw new IllegalArgumentException("E-mail ou senha inválidos.");
        }
        return UsuarioDTO.from(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAllByOrderByNomeAsc()
                .stream()
                .map(UsuarioDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioDTO buscarPorId(Integer id) {
        return UsuarioDTO.from(obterUsuario(id));
    }

    /**
     * O leitor só pode alterar os próprios dados. O administrador pode alterar contas.
     * A troca de perfil não é oferecida por esta operação: cadastros normais são LEITOR;
     * contas ADMINISTRADOR são criadas pela configuração inicial do sistema.
     */
    public UsuarioDTO atualizar(Integer id, String nome, String email, String novaSenha,
                                Integer idSolicitante, boolean solicitanteAdministrador) {
        if (id == null) {
            throw new IllegalArgumentException("Selecione uma conta válida.");
        }
        if (!solicitanteAdministrador && (idSolicitante == null || !id.equals(idSolicitante))) {
            throw new IllegalArgumentException("Você só pode editar a própria conta.");
        }

        validarNome(nome);
        String emailNormalizado = validarEmail(email);
        Usuario usuario = obterUsuario(id);

        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(emailNormalizado, id)) {
            throw new IllegalArgumentException("Outro usuário já utiliza esse e-mail.");
        }

        usuario.setNome(nome.trim());
        usuario.setEmail(emailNormalizado);

        if (novaSenha != null && !novaSenha.isBlank()) {
            validarSenha(novaSenha);
            usuario.setSenha(passwordEncoder.encode(novaSenha));
        }

        return UsuarioDTO.from(usuarioRepository.save(usuario));
    }

    /** O leitor só exclui a própria conta; o administrador pode excluir outras contas. */
    public void excluir(Integer id, Integer idSolicitante, boolean solicitanteAdministrador) {
        if (id == null) {
            throw new IllegalArgumentException("Selecione uma conta válida.");
        }
        if (!solicitanteAdministrador && (idSolicitante == null || !id.equals(idSolicitante))) {
            throw new IllegalArgumentException("Você só pode excluir a própria conta.");
        }
        if (solicitanteAdministrador && id.equals(idSolicitante)) {
            throw new IllegalArgumentException("Para excluir a própria conta, utilize a opção Minha conta.");
        }

        Usuario usuario = obterUsuario(id);
        if (usuario.getTipoUsuario() == TipoUsuario.ADMINISTRADOR
                && usuarioRepository.countByTipoUsuario(TipoUsuario.ADMINISTRADOR) <= 1) {
            throw new IllegalArgumentException("Não é possível excluir o último administrador do sistema.");
        }
        usuarioRepository.delete(usuario);
    }

    private Usuario obterUsuario(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("A conta selecionada não foi encontrada."));
    }

    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Informe o nome.");
        }
        if (nome.trim().length() > 100) {
            throw new IllegalArgumentException("O nome deve ter no máximo 100 caracteres.");
        }
    }

    private String validarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail.");
        }
        String valor = email.trim().toLowerCase(Locale.ROOT);
        if (valor.length() > 160 || !valor.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }
        return valor;
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.length() < SENHA_MINIMA || senha.length() > SENHA_MAXIMA) {
            throw new IllegalArgumentException("A senha deve ter entre 6 e 20 caracteres.");
        }
    }
}
