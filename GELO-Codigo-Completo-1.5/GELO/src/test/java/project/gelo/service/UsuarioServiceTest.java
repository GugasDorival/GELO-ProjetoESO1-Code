package project.gelo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import project.gelo.dto.UsuarioDTO;
import project.gelo.model.TipoUsuario;
import project.gelo.model.Usuario;
import project.gelo.repository.UsuarioRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:gelotest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "gelo.bootstrap-admin.enabled=false"
})
@Transactional
class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limparUsuarios() {
        usuarioRepository.deleteAll();
    }

    @Test
    void cadastroCriaLeitorEGravaSenhaComoHash() {
        UsuarioDTO criado = usuarioService.criarLeitor("Ana Leitura", "ANA@example.com", "senha123");
        Usuario persistido = usuarioRepository.findById(criado.id()).orElseThrow();

        assertEquals(TipoUsuario.LEITOR, criado.tipoUsuario());
        assertEquals("ana@example.com", criado.email());
        assertNotEquals("senha123", persistido.getSenha());
        assertTrue(passwordEncoder.matches("senha123", persistido.getSenha()));
    }

    @Test
    void loginAceitaEmailSemDiferenciarMaiusculas() {
        usuarioService.criarLeitor("Ana Leitura", "ana@example.com", "senha123");

        UsuarioDTO autenticado = usuarioService.autenticar("ANA@EXAMPLE.COM", "senha123");

        assertEquals("Ana Leitura", autenticado.nome());
        assertEquals(TipoUsuario.LEITOR, autenticado.tipoUsuario());
    }

    @Test
    void impedeCadastroComEmailDuplicado() {
        usuarioService.criarLeitor("Ana", "ana@example.com", "senha123");

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.criarLeitor("Outra pessoa", "ANA@example.com", "senha456"));
    }

    @Test
    void loginRecusaSenhaIncorreta() {
        usuarioService.criarLeitor("Ana", "ana@example.com", "senha123");

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.autenticar("ana@example.com", "errada123"));
    }
}
