package project.gelo.dto;

import project.gelo.model.TipoUsuario;
import project.gelo.model.Usuario;

/** DTO público da conta: não contém a senha nem o hash. */
public record UsuarioDTO(Integer id, String nome, String email, TipoUsuario tipoUsuario) {

    // Getters JavaBeans para compatibilidade com expressões EL nas páginas JSF.
    public Integer getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public TipoUsuario getTipoUsuario() { return tipoUsuario; }

    public static UsuarioDTO from(Usuario usuario) {
        return new UsuarioDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTipoUsuario());
    }
}
