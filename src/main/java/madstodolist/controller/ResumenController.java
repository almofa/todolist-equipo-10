package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.ResumenService;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ResumenController {

    @Autowired
    private ResumenService resumenService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ManagerUserSession managerUserSession;

    @GetMapping("/resumen")
    public String resumen(Model model) {
        Long idUsuario = managerUserSession.comprobarUsuarioLogeado();
        UsuarioData usuario = usuarioService.findById(idUsuario);

        model.addAttribute("usuario", usuario);
        model.addAttribute("resumen", resumenService.resumenUsuario(idUsuario));
        return "resumen";
    }
}