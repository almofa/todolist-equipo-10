package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ManagerUserSession managerUserSession;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/about")
    public String about(Model model) {
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario != null) {
            UsuarioData usuario = usuarioService.findById(idUsuario);
            model.addAttribute("usuario", usuario);
        }
        return "about";
    }

    @GetMapping("/registrados")
    public String listadoUsuarios(Model model) {
        // Valida admin y obtiene el usuario autenticado en un solo paso
        UsuarioData usuarioLogeado = managerUserSession.comprobarAdmin();

        List<UsuarioData> usuarios = usuarioService.findAll();

        model.addAttribute("usuario", usuarioLogeado);
        model.addAttribute("usuarios", usuarios);

        return "listadoUsuarios";
    }

    @GetMapping("/registrados/{id}")
    public String detalleUsuario(@PathVariable(value="id") Long id, Model model) {
        // Valida admin y obtiene el usuario autenticado en un solo paso
        UsuarioData usuarioLogeado = managerUserSession.comprobarAdmin();

        UsuarioData usuarioDetalle = usuarioService.findById(id);
        if (usuarioDetalle == null) {
            return "redirect:/registrados";
        }

        model.addAttribute("usuario", usuarioLogeado);
        model.addAttribute("usuarioDetalle", usuarioDetalle);
        return "detalleUsuario";
    }

    @PostMapping("/registrados/{id}/bloqueo")
    public String cambiarBloqueoUsuario(@PathVariable(value="id") Long id) {
        // Valida permisos de administrador antes de hacer nada
        managerUserSession.comprobarAdmin();

        UsuarioData usuario = usuarioService.findById(id);
        if (usuario != null && !Boolean.TRUE.equals(usuario.getAdmin())) {
            boolean nuevoEstado = !Boolean.TRUE.equals(usuario.getBloqueado());
            usuarioService.setBloqueoUsuario(id, nuevoEstado);
        }

        return "redirect:/registrados";
    }
}