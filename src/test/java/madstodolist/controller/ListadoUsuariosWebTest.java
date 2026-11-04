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

import java.util.Arrays;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ListadoUsuariosWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void getRegistradosDevuelveListaDeUsuarios() throws Exception {
        // GIVEN: Usuario administrador autenticado
        UsuarioData admin = new UsuarioData();
        admin.setId(100L);
        admin.setAdmin(true);

        when(managerUserSession.usuarioLogeado()).thenReturn(100L);
        when(usuarioService.findById(100L)).thenReturn(admin);

        // Usuarios simulados para la lista
        UsuarioData u1 = new UsuarioData();
        u1.setId(1L);
        u1.setEmail("ana@gmail.com");

        UsuarioData u2 = new UsuarioData();
        u2.setId(2L);
        u2.setEmail("pedro@gmail.com");

        when(usuarioService.findAll()).thenReturn(Arrays.asList(u1, u2));

        // WHEN & THEN: Accedemos a /registrados
        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Usuarios Registrados")))
                .andExpect(content().string(containsString("ana@gmail.com")))
                .andExpect(content().string(containsString("pedro@gmail.com")));
    }
}