package madstodolist.controller.exception;

import madstodolist.controller.exception.UnauthorizedException;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. SI NO ESTÁ LOGUEADO: Redirige a /login
    @ExceptionHandler(UsuarioNoLogeadoException.class)
    public String handleUsuarioNoLogeado(RedirectAttributes flash) {
        return "redirect:/login";
    }

    // 2. SI NO TIENE PERMISOS DE ADMIN: Muestra tu plantilla error/401.html con estado 401
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleUnauthorizedException(UnauthorizedException ex, Model model) {
        return "error/401";
    }
}