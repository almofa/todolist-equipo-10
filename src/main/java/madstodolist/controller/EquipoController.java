package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.EquipoControllerException;
import madstodolist.controller.exception.UnauthorizedException;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.EquipoServiceException;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ManagerUserSession managerUserSession;

    @GetMapping("/equipos")
    public String listadoEquipos(Model model, HttpSession session) {
        Long idUsuarioLogeado = managerUserSession.comprobarUsuarioLogeado();
        UsuarioData usuario = usuarioService.findById(idUsuarioLogeado);
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();

        model.addAttribute("usuario", usuario);
        model.addAttribute("equipos", equipos);
        return "listadoEquipos";
    }

    @GetMapping("/equipos/{id}")
    public String detalleEquipo(@PathVariable("id") Long idEquipo, Model model, RedirectAttributes flash) {
        Long idUsuario = managerUserSession.comprobarUsuarioLogeado();
        UsuarioData usuario = usuarioService.findById(idUsuario);

        try {
            EquipoData equipo = equipoService.recuperarEquipo(idEquipo);
            model.addAttribute("usuario", usuario);
            model.addAttribute("equipo", equipo);
            model.addAttribute("usuarios", equipoService.usuariosEquipo(idEquipo));
            return "detalleEquipo";
        } catch (EquipoServiceException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/equipos";
        }
    }

    @PostMapping("/equipos/nuevo")
    public String crearEquipo(@RequestParam("nombre") String nombre,
                              RedirectAttributes flash) {
        Long idUsuario = managerUserSession.comprobarUsuarioLogeado();

        try {
            equipoService.crearEquipo(nombre);
            flash.addFlashAttribute("mensaje", "Equipo creado correctamente");
        } catch (EquipoControllerException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/equipos";
    }

    @PostMapping("/equipos/{id}/apply")
    public String anadirUsuarioAEquipo(@PathVariable("id") Long idEquipo,
                                       RedirectAttributes flash) {
        Long idUsuario = managerUserSession.comprobarUsuarioLogeado();

        try {
            equipoService.añadirUsuarioAEquipo(idEquipo, idUsuario);
            flash.addFlashAttribute("mensaje", "Te has unido al equipo correctamente");
        } catch (EquipoServiceException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/equipos";
    }

    @PostMapping("/equipos/{id}/exit")
    public String salirDeEquipo(@PathVariable("id") Long idEquipo,
                                RedirectAttributes flash) {
        Long idUsuario = managerUserSession.comprobarUsuarioLogeado();

        try {
            equipoService.eliminarUsuarioDeEquipo(idEquipo, idUsuario);
            flash.addFlashAttribute("mensaje", "Has salido del equipo correctamente");
        } catch (EquipoServiceException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/equipos";
    }

    @GetMapping("/equipos/{id}/editar")
    public String formEditarEquipo(@PathVariable("id") Long idEquipo,
                                   Model model,
                                   RedirectAttributes flash) {
        managerUserSession.comprobarAdmin();

        try {
            EquipoData equipo = equipoService.recuperarEquipo(idEquipo);
            model.addAttribute("equipo", equipo);
            return "formEditarEquipo";
        } catch (EquipoServiceException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/equipos";
        }
    }

    @PostMapping("/equipos/{id}/editar")
    public String editarEquipo(@PathVariable("id") Long idEquipo,
                               @RequestParam("nombre") String nuevoNombre,
                               RedirectAttributes flash) {
        managerUserSession.comprobarAdmin();

        try {
            equipoService.modificarEquipo(idEquipo, nuevoNombre);
            flash.addFlashAttribute("mensaje", "Equipo modificado correctamente");
        } catch (EquipoServiceException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/equipos";
    }

    @PostMapping("/equipos/{id}/borrar")
    public String borrarEquipo(@PathVariable("id") Long idEquipo,
                               RedirectAttributes flash) {
        // Lanza UnauthorizedException si no es admin, la cual es capturada por GlobalExceptionHandler
        managerUserSession.comprobarAdmin();

        try {
            equipoService.borrarEquipo(idEquipo);
            flash.addFlashAttribute("mensaje", "Equipo eliminado correctamente");
        } catch (EquipoServiceException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/equipos";
    }
}