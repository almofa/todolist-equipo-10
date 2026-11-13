package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
public class AccountController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ManagerUserSession managerUserSession;

    // 1. Mostrar los datos de la cuenta del usuario logueado actualmente
    @GetMapping("/cuenta")
    public String verCuenta(Model model) {
        Long idUsuarioLogueado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogueado == null) {
            return "redirect:/login";
        }

        UsuarioData usuario = usuarioService.findById(idUsuarioLogueado);
        model.addAttribute("usuario", usuario);
        return "cuenta"; // Vista HTML para ver la cuenta
    }

    // 2. Formulario para editar la cuenta (GET)
    @GetMapping("/cuenta/editar")
    public String formEditarCuenta(Model model) {
        Long idUsuarioLogueado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogueado == null) {
            return "redirect:/login";
        }

        UsuarioData usuario = usuarioService.findById(idUsuarioLogueado);
        model.addAttribute("usuario", usuario);
        return "editarCuenta"; // Vista HTML con el formulario de edición
    }

    // 3. Procesar la modificación de los datos de la cuenta (POST)
    @PostMapping("/cuenta/editar")
    public String guardarCuenta(@ModelAttribute("usuario") @Valid UsuarioData usuarioData,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Long idUsuarioLogueado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogueado == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            return "editarCuenta";
        }

        try {
            usuarioService.modifica(idUsuarioLogueado, usuarioData);
            redirectAttributes.addFlashAttribute("mensaje", "Cuenta actualizada correctamente");
        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar la cuenta: " + e.getMessage());
            return "editarCuenta";
        }

        return "redirect:/cuenta";
    }
}