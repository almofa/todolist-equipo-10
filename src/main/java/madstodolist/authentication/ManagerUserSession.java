package madstodolist.authentication;

import madstodolist.controller.exception.UnauthorizedException;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;

@Component
public class ManagerUserSession {

    @Autowired
    private HttpSession session;

    @Autowired
    private UsuarioService usuarioService;

    public void logearUsuario(Long idUsuario) {
        session.setAttribute("idUsuarioLogeado", idUsuario);
    }

    public Long usuarioLogeado() {
        return (Long) session.getAttribute("idUsuarioLogeado");
    }

    public void logout() {
        session.setAttribute("idUsuarioLogeado", null);
    }

    public Long comprobarUsuarioLogeado() {
        Long idUsuarioLogeado = usuarioLogeado();
        if (idUsuarioLogeado == null) {
            throw new UsuarioNoLogeadoException();
        }
        return idUsuarioLogeado;
    }

    public void comprobarUsuarioLogeado(Long idUsuarioEsperado) {
        Long idUsuarioLogeado = comprobarUsuarioLogeado();
        if (!idUsuarioLogeado.equals(idUsuarioEsperado)) {
            throw new UsuarioNoLogeadoException();
        }
    }

    /**
     * Comprueba si el usuario logueado actual tiene rol de Administrador.
     * Si no hay sesión o no es admin, lanza UnauthorizedException.
     * Devuelve el objeto UsuarioData correspondiente.
     */
    public UsuarioData comprobarAdmin() {
        Long idUsuarioLogeado = usuarioLogeado();
        if (idUsuarioLogeado == null) {
            throw new UnauthorizedException();
        }

        UsuarioData usuario = usuarioService.findById(idUsuarioLogeado);
        if (usuario == null || !Boolean.TRUE.equals(usuario.getAdmin())) {
            throw new UnauthorizedException();
        }

        return usuario;
    }
}