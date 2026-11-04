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
public class BarraMenuWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManagerUserSession managerUserSession;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    public void navbarSinUsuarioLogeadoMuestraLoginYRegistro() throws Exception {
        // Dado que no hay usuario en sesión
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        // Al acceder a /about
        this.mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                // Se muestra la marca ToDoList, Login y Registro
                .andExpect(content().string(containsString("ToDoList")))
                .andExpect(content().string(containsString("Login")))
                .andExpect(content().string(containsString("Registro")))
                // Y NO se muestran las opciones de usuario logeado
                .andExpect(content().string(not(containsString("Tareas"))))
                .andExpect(content().string(not(containsString("Cerrar sesión"))));
    }

    @Test
    public void navbarConUsuarioLogeadoMuestraTareasYDesplegable() throws Exception {
        // Dado un usuario logeado
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);
        usuario.setEmail("juan@gmail.com");
        usuario.setNombre("Juan");

        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.findById(1L)).thenReturn(usuario);

        // Al acceder a /about
        this.mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                // Se muestran Tareas, la ruta de logout y el nombre del usuario
                .andExpect(content().string(containsString("ToDoList")))
                .andExpect(content().string(containsString("Tareas")))
                .andExpect(content().string(containsString("Juan")))
                .andExpect(content().string(containsString("/logout")))
                .andExpect(content().string(containsString("Cerrar sesión")))
                // Y NO se muestran Login ni Registro
                .andExpect(content().string(not(containsString("Login"))))
                .andExpect(content().string(not(containsString("Registro"))));
    }
}