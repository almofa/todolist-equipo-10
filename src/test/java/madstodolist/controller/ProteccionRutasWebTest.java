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

import java.util.Collections;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
public class ProteccionRutasWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void usuarioSinLoguearObtieneUnauthorizedEnRegistrados() throws Exception {
        doThrow(new UnauthorizedException()).when(managerUserSession).comprobarAdmin();

        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void usuarioNoAdminObtieneUnauthorizedEnRegistrados() throws Exception {
        doThrow(new UnauthorizedException()).when(managerUserSession).comprobarAdmin();

        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void usuarioAdminPuedeAccederARegistrados() throws Exception {
        UsuarioData usuarioAdmin = new UsuarioData();
        usuarioAdmin.setId(2L);
        usuarioAdmin.setAdmin(true);

        when(managerUserSession.comprobarAdmin()).thenReturn(usuarioAdmin);
        when(managerUserSession.usuarioLogeado()).thenReturn(2L);
        when(usuarioService.findById(2L)).thenReturn(usuarioAdmin);
        when(usuarioService.findAll()).thenReturn(Collections.emptyList());

        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk())
                .andExpect(view().name("listadoUsuarios"));
    }
}