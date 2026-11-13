package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void testVerCuentaConUsuarioLogueado() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);
        usuario.setEmail("test@ua.es");
        usuario.setNombre("Test User");
        usuario.setDescripcion("Mi biografía de prueba");

        when(usuarioService.findById(1L)).thenReturn(usuario);

        mockMvc.perform(get("/cuenta"))
                .andExpect(status().isOk())
                .andExpect(view().name("cuenta"))
                .andExpect(model().attributeExists("usuario"));
    }

    @Test
    public void testVerCuentaSinLoguearRedirigeLogin() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(get("/cuenta"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testFormEditarCuenta() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);
        usuario.setEmail("test@ua.es");

        when(usuarioService.findById(1L)).thenReturn(usuario);

        mockMvc.perform(get("/cuenta/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("editarCuenta"))
                .andExpect(model().attributeExists("usuario"));
    }

    @Test
    public void testGuardarCuentaModificada() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        UsuarioData usuarioModificado = new UsuarioData();
        usuarioModificado.setNombre("Nuevo Nombre");
        usuarioModificado.setDescripcion("Nueva Descripción");

        when(usuarioService.modifica(eq(1L), any(UsuarioData.class))).thenReturn(usuarioModificado);

        mockMvc.perform(post("/cuenta/editar")
                        .param("nombre", "Nuevo Nombre")
                        .param("descripcion", "Nueva Descripción"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cuenta"));
    }
    @Test
    public void testFormCambiarPassword() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        mockMvc.perform(get("/cuenta/password"))
                .andExpect(status().isOk())
                .andExpect(view().name("cambiarPassword"));
    }

    @Test
    public void testGuardarPasswordCorrecta() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.cambiarPassword(eq(1L), eq("123456"), eq("nueva123"))).thenReturn(true);

        mockMvc.perform(post("/cuenta/password")
                        .param("passwordActual", "123456")
                        .param("passwordNueva", "nueva123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cuenta"));
    }

    @Test
    public void testGuardarPasswordIncorrecta() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.cambiarPassword(eq(1L), eq("mal"), eq("nueva123"))).thenReturn(false);

        mockMvc.perform(post("/cuenta/password")
                        .param("passwordActual", "mal")
                        .param("passwordNueva", "nueva123"))
                .andExpect(status().isOk())
                .andExpect(view().name("cambiarPassword"))
                .andExpect(model().attributeExists("error"));
    }
}