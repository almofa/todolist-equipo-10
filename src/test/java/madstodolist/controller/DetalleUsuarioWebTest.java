package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DetalleUsuarioWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void getDetalleUsuarioMuestraDatosSinContrasena() throws Exception {
        // GIVEN: Usuario administrador logueado en la sesión
        UsuarioData admin = new UsuarioData();
        admin.setId(100L);
        admin.setAdmin(true);

        when(managerUserSession.usuarioLogeado()).thenReturn(100L);
        when(usuarioService.findById(100L)).thenReturn(admin);

        // Usuario consultado
        UsuarioData usuario = new UsuarioData();
        usuario.setId(10L);
        usuario.setEmail("maria@gmail.com");
        usuario.setNombre("María López");
        usuario.setPassword("secreto123");

        when(usuarioService.findById(10L)).thenReturn(usuario);

        // WHEN & THEN: Accedemos a /registrados/10
        this.mockMvc.perform(get("/registrados/10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("María López")))
                .andExpect(content().string(containsString("maria@gmail.com")))
                .andExpect(content().string(containsString("10")))
                .andExpect(content().string(not(containsString("secreto123"))));
    }
}