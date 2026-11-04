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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UsuarioAdminWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void formularioRegistroMuestraCheckboxSiNoHayAdmin() throws Exception {
        when(usuarioService.existeAdmin()).thenReturn(false);

        this.mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Registrarse como Administrador")));
    }

    @Test
    public void formularioRegistroOcultaCheckboxSiYaHayAdmin() throws Exception {
        when(usuarioService.existeAdmin()).thenReturn(true);

        this.mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Registrarse como Administrador"))));
    }

    @Test
    public void loginAdminRedirigeARegistrados() throws Exception {
        UsuarioData admin = new UsuarioData();
        admin.setId(1L);
        admin.setEmail("admin@gmail.com");
        admin.setAdmin(true);

        when(usuarioService.login("admin@gmail.com", "1234"))
                .thenReturn(UsuarioService.LoginStatus.LOGIN_OK);
        when(usuarioService.findByEmail("admin@gmail.com")).thenReturn(admin);

        this.mockMvc.perform(post("/login")
                        .param("eMail", "admin@gmail.com")
                        .param("password", "1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrados"));
    }
}