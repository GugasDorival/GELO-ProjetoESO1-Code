package project.gelo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.gelo.model.TipoUsuario;
import project.gelo.model.Usuario;
import project.gelo.repository.UsuarioRepository;

@Component
@ConditionalOnProperty(name = "gelo.bootstrap-admin.enabled", havingValue = "true", matchIfMissing = true)
public class AdministradorInicial implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String nome;
    private final String email;
    private final String senha;

    public AdministradorInicial(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${gelo.bootstrap-admin.nome:Administrador GELO}") String nome,
            @Value("${gelo.bootstrap-admin.email:admin@gelo.local}") String email,
            @Value("${gelo.bootstrap-admin.senha:Admin123!}") String senha) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
            return;
        }
        Usuario administrador = new Usuario(
                nome,
                email,
                passwordEncoder.encode(senha),
                TipoUsuario.ADMINISTRADOR
        );
        usuarioRepository.save(administrador);
    }
}
