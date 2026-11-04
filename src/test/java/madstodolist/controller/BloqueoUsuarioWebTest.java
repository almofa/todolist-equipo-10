package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UnauthorizedException;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BloqueoUsuarioWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void adminPuedeBloquearUsuario() throws Exception {
        // GIVEN: Administrador logueado en la sesión
        UsuarioData admin = new UsuarioData();
        admin.setId(1L);
        admin.setAdmin(true);

        UsuarioData usuarioNormal = new UsuarioData();
        usuarioNormal.setId(2L);
        usuarioNormal.setAdmin(false);
        usuarioNormal.setBloqueado(false);

        // Simulamos que la comprobación de admin pasa con éxito y devuelve el usuario admin
        when(managerUserSession.comprobarAdmin()).thenReturn(admin);
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.findById(1L)).thenReturn(admin);
        when(usuarioService.findById(2L)).thenReturn(usuarioNormal);

        // WHEN & THEN: El admin hace POST a /registrados/2/bloqueo y redirige a /registrados
        this.mockMvc.perform(post("/registrados/2/bloqueo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrados"));

        // Verificamos que se invocó al servicio con el nuevo estado true (bloqueado)
        verify(usuarioService).setBloqueoUsuario(2L, true);
    }

    @Test
    public void usuarioNoAdminNoPuedeBloquearUsuario() throws Exception {
        // GIVEN: El manager de sesión detecta que no es admin o no está logueado y lanza UnauthorizedException
        doThrow(new UnauthorizedException()).when(managerUserSession).comprobarAdmin();

        // WHEN & THEN: Intenta bloquear a otro usuario y obtiene 401 Unauthorized
        this.mockMvc.perform(post("/registrados/2/bloqueo"))
                .andExpect(status().isUnauthorized());
    }
}