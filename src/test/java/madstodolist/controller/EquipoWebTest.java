package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UnauthorizedException;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.EquipoServiceException;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EquipoWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EquipoService equipoService;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void listadoEquiposMuestraListaDeEquipos() throws Exception {
        Long idUsuario = 1L;
        when(managerUserSession.comprobarUsuarioLogeado()).thenReturn(idUsuario);
        when(usuarioService.findById(idUsuario)).thenReturn(new UsuarioData());

        EquipoData equipo1 = new EquipoData();
        equipo1.setId(1L);
        equipo1.setNombre("Proyecto Alpha");

        EquipoData equipo2 = new EquipoData();
        equipo2.setId(2L);
        equipo2.setNombre("Proyecto Beta");

        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Arrays.asList(equipo1, equipo2));

        this.mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(view().name("listadoEquipos"))
                .andExpect(model().attributeExists("equipos"))
                .andExpect(model().attributeExists("usuario"))
                .andExpect(content().string(containsString("Proyecto Alpha")))
                .andExpect(content().string(containsString("Proyecto Beta")));
    }

    @Test
    public void detalleEquipoMuestraMiembrosDelEquipo() throws Exception {
        Long idUsuario = 1L;
        when(managerUserSession.comprobarUsuarioLogeado()).thenReturn(idUsuario);
        when(usuarioService.findById(idUsuario)).thenReturn(new UsuarioData());

        EquipoData equipo = new EquipoData();
        equipo.setId(1L);
        equipo.setNombre("Proyecto Alpha");

        UsuarioData usuario1 = new UsuarioData();
        usuario1.setId(10L);
        usuario1.setEmail("ana@ua.es");

        UsuarioData usuario2 = new UsuarioData();
        usuario2.setId(20L);
        usuario2.setEmail("carlos@ua.es");

        List<UsuarioData> miembros = Arrays.asList(usuario1, usuario2);

        when(equipoService.recuperarEquipo(1L)).thenReturn(equipo);
        when(equipoService.usuariosEquipo(1L)).thenReturn(miembros);

        this.mockMvc.perform(get("/equipos/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("detalleEquipo"))
                .andExpect(model().attributeExists("equipo"))
                .andExpect(model().attributeExists("usuarios"))
                .andExpect(content().string(containsString("Proyecto Alpha")))
                .andExpect(content().string(containsString("ana@ua.es")))
                .andExpect(content().string(containsString("carlos@ua.es")));
    }

    @Test
    public void detalleEquipoInexistenteRedirigeAListado() throws Exception {
        when(equipoService.recuperarEquipo(99L)).thenThrow(new EquipoServiceException("Equipo no encontrado"));

        this.mockMvc.perform(get("/equipos/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));
    }

    @Test
    public void crearEquipoRedirigeYMuestraMensajeFlash() throws Exception {
        Long idUsuario = 1L;
        when(managerUserSession.comprobarUsuarioLogeado()).thenReturn(idUsuario);

        this.mockMvc.perform(post("/equipos/nuevo")
                        .param("nombre", "Proyecto Gamma"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"))
                .andExpect(flash().attribute("mensaje", "Equipo creado correctamente"));

        verify(equipoService).crearEquipo("Proyecto Gamma");
    }

    @Test
    public void anadirUsuarioAEquipoRedirigeYMuestraMensaje() throws Exception {
        Long idUsuario = 1L;
        when(managerUserSession.comprobarUsuarioLogeado()).thenReturn(idUsuario);

        this.mockMvc.perform(post("/equipos/1/apply"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"))
                .andExpect(flash().attribute("mensaje", "Te has unido al equipo correctamente"));

        verify(equipoService).añadirUsuarioAEquipo(1L, idUsuario);
    }

    @Test
    public void salirDeEquipoRedirigeYMuestraMensaje() throws Exception {
        Long idUsuario = 1L;
        when(managerUserSession.comprobarUsuarioLogeado()).thenReturn(idUsuario);

        this.mockMvc.perform(post("/equipos/1/exit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"))
                .andExpect(flash().attribute("mensaje", "Has salido del equipo correctamente"));

        verify(equipoService).eliminarUsuarioDeEquipo(1L, idUsuario);
    }

    @Test
    public void listadoEquiposMuestraBotonEditarSoloAAdmin() throws Exception {
        Long idUsuario = 1L;
        UsuarioData admin = new UsuarioData();
        admin.setId(idUsuario);
        admin.setAdmin(true);

        EquipoData equipo = new EquipoData();
        equipo.setId(10L);
        equipo.setNombre("Equipo Beta");

        when(managerUserSession.comprobarUsuarioLogeado()).thenReturn(idUsuario);
        when(usuarioService.findById(idUsuario)).thenReturn(admin);
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Arrays.asList(equipo));

        this.mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/equipos/10/editar")));
    }

    @Test
    public void adminPuedeVerFormularioEditarEquipo() throws Exception {
        UsuarioData admin = new UsuarioData();
        admin.setAdmin(true);

        EquipoData equipo = new EquipoData();
        equipo.setId(10L);
        equipo.setNombre("Equipo Alfa");

        when(managerUserSession.comprobarAdmin()).thenReturn(admin);
        when(equipoService.recuperarEquipo(10L)).thenReturn(equipo);

        this.mockMvc.perform(get("/equipos/10/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("formEditarEquipo"))
                .andExpect(model().attributeExists("equipo"))
                .andExpect(content().string(containsString("Equipo Alfa")));
    }

    @Test
    public void usuarioNoAdminNoPuedeVerFormularioEditarEquipo() throws Exception {
        doThrow(new UnauthorizedException()).when(managerUserSession).comprobarAdmin();

        this.mockMvc.perform(get("/equipos/10/editar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void editarEquipoInexistenteRedirigeAListado() throws Exception {
        UsuarioData admin = new UsuarioData();
        admin.setAdmin(true);

        when(managerUserSession.comprobarAdmin()).thenReturn(admin);
        when(equipoService.recuperarEquipo(99L)).thenThrow(new EquipoServiceException("Equipo no encontrado"));

        this.mockMvc.perform(get("/equipos/99/editar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"))
                .andExpect(flash().attribute("error", "Equipo no encontrado"));
    }

    @Test
    public void adminPuedeEditarEquipoYMuestraMensajeFlash() throws Exception {
        UsuarioData admin = new UsuarioData();
        admin.setAdmin(true);

        when(managerUserSession.comprobarAdmin()).thenReturn(admin);

        this.mockMvc.perform(post("/equipos/10/editar")
                        .param("nombre", "Proyecto Alpha Editado"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"))
                .andExpect(flash().attribute("mensaje", "Equipo modificado correctamente"));

        verify(equipoService).modificarEquipo(10L, "Proyecto Alpha Editado");
    }

    @Test
    public void usuarioNoAdminNoPuedeEditarEquipo() throws Exception {
        doThrow(new UnauthorizedException()).when(managerUserSession).comprobarAdmin();

        this.mockMvc.perform(post("/equipos/10/editar")
                        .param("nombre", "Proyecto Hackeado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void editarEquipoInexistenteMuestraMensajeDeError() throws Exception {
        UsuarioData admin = new UsuarioData();
        admin.setAdmin(true);

        when(managerUserSession.comprobarAdmin()).thenReturn(admin);

        doThrow(new EquipoServiceException("Equipo no encontrado"))
                .when(equipoService).modificarEquipo(99L, "Nombre Nuevo");

        this.mockMvc.perform(post("/equipos/99/editar")
                        .param("nombre", "Nombre Nuevo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"))
                .andExpect(flash().attribute("error", "Equipo no encontrado"));
    }

    @Test
    public void adminPuedeBorrarEquipoYMuestraMensajeFlash() throws Exception {
        UsuarioData admin = new UsuarioData();
        admin.setAdmin(true);

        when(managerUserSession.comprobarAdmin()).thenReturn(admin);

        this.mockMvc.perform(post("/equipos/10/borrar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"))
                .andExpect(flash().attribute("mensaje", "Equipo eliminado correctamente"));

        verify(equipoService).borrarEquipo(10L);
    }

    @Test
    public void usuarioNoAdminNoPuedeBorrarEquipo() throws Exception {
        doThrow(new UnauthorizedException()).when(managerUserSession).comprobarAdmin();

        this.mockMvc.perform(post("/equipos/10/borrar"))
                .andExpect(status().isUnauthorized());
    }
}