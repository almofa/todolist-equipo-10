package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.EquipoData;
import madstodolist.dto.ResumenUsuarioData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.ResumenService;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.hamcrest.Matchers.not;

@SpringBootTest
@AutoConfigureMockMvc
public class ResumenWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManagerUserSession managerUserSession;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ResumenService resumenService;

    @Test
    public void resumenSinUsuarioLogeadoRedirigeALogin() throws Exception {
        doThrow(new UsuarioNoLogeadoException()).when(managerUserSession).comprobarUsuarioLogeado();

        this.mockMvc.perform(get("/resumen"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void resumenMuestraTareasYEquiposDelUsuario() throws Exception {
        Long idUsuario = 1L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(idUsuario);
        usuario.setNombre("Juan");

        EquipoData equipo = new EquipoData();
        equipo.setId(1L);
        equipo.setNombre("Proyecto Alpha");

        when(managerUserSession.comprobarUsuarioLogeado()).thenReturn(idUsuario);
        when(managerUserSession.usuarioLogeado()).thenReturn(idUsuario);
        when(usuarioService.findById(idUsuario)).thenReturn(usuario);
        when(resumenService.resumenUsuario(idUsuario))
                .thenReturn(new ResumenUsuarioData(3, 1, 2, 33, Collections.singletonList(equipo)));

        this.mockMvc.perform(get("/resumen"))
                .andExpect(status().isOk())
                .andExpect(view().name("resumen"))
                .andExpect(model().attributeExists("resumen"))
                .andExpect(content().string(containsString("33%")))
                .andExpect(content().string(containsString("Proyecto Alpha")))
                .andExpect(content().string(not(containsString("Todavía no tienes tareas"))));
    }

    @Test
    public void resumenSinTareasMuestraMensajeEnLugarDeLaBarra() throws Exception {
        Long idUsuario = 1L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(idUsuario);
        usuario.setNombre("Juan");

        when(managerUserSession.comprobarUsuarioLogeado()).thenReturn(idUsuario);
        when(managerUserSession.usuarioLogeado()).thenReturn(idUsuario);
        when(usuarioService.findById(idUsuario)).thenReturn(usuario);
        when(resumenService.resumenUsuario(idUsuario))
                .thenReturn(new ResumenUsuarioData(0, 0, 0, 0, Collections.emptyList()));

        this.mockMvc.perform(get("/resumen"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Todavía no tienes tareas")))
                .andExpect(content().string(not(containsString("progress-bar"))));
    }
}