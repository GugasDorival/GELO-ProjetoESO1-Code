package project.gelo.dto;

import project.gelo.model.TipoUsuario;
import project.gelo.model.Usuario;

/** Dados de conta apropriados para telas; por segurança, não inclui a senha/hash. */
public record UsuarioDTO(Integer id, String nome, String email, TipoUsuario tipoUsuario) {

    // Getters JavaBeans são necessários para as expressões EL das páginas JSF.
    public Integer getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public TipoUsuario getTipoUsuario() { return tipoUsuario; }

    public static UsuarioDTO from(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTipoUsuario()
        );
    }
}
