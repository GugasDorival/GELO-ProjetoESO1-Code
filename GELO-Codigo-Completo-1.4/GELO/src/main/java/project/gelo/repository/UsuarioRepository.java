package project.gelo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.gelo.model.TipoUsuario;
import project.gelo.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Integer id);
    long countByTipoUsuario(TipoUsuario tipoUsuario);
    List<Usuario> findAllByOrderByNomeAsc();
}
