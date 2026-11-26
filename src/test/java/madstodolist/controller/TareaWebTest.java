package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.TareaService;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/clean-db.sql")
public class TareaWebTest {

    @Autowired
    private MockMvc mockMvc;

    // Declaramos los servicios como Autowired
    @Autowired
    private TareaService tareaService;

    @Autowired
    private UsuarioService usuarioService;

    // Moqueamos el managerUserSession para poder moquear el usuario logeado
    @MockBean
    private ManagerUserSession managerUserSession;

    // Método para inicializar los datos de prueba en la BD
    // Devuelve un mapa con los identificadores del usuario y de la primera tarea añadida

    Map<String, Long> addUsuarioTareasBD() {
        // Añadimos un usuario a la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);

        // Y añadimos dos tareas asociadas a ese usuario
        TareaData tarea1 = tareaService.nuevaTareaUsuario(usuario.getId(), "Lavar coche");
        tareaService.nuevaTareaUsuario(usuario.getId(), "Renovar DNI");

        // Devolvemos los ids del usuario y de la primera tarea añadida
        Map<String, Long> ids = new HashMap<>();
        ids.put("usuarioId", usuario.getId());
        ids.put("tareaId", tarea1.getId());
        return ids;

    }

    @Test
    public void listaTareas() throws Exception {
        // GIVEN
        // Un usuario con dos tareas en la BD
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");

        // Moqueamos el método usuarioLogeado para que devuelva el usuario 1L,
        // el mismo que se está usando en la petición. De esta forma evitamos
        // que salte la excepción de que el usuario que está haciendo la
        // petición no está logeado.
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // se realiza la petición GET al listado de tareas del usuario,
        // el HTML devuelto contiene las descripciones de sus tareas.

        String url = "/usuarios/" + usuarioId.toString() + "/tareas";

        this.mockMvc.perform(get(url))
                .andExpect((content().string(allOf(
                        containsString("Lavar coche"),
                        containsString("Renovar DNI")
                ))));
    }

    @Test
    public void getNuevaTareaDevuelveForm() throws Exception {
        // GIVEN
        // Un usuario con dos tareas en la BD
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");

        // Ver el comentario en el primer test
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // si ejecutamos una petición GET para crear una nueva tarea de un usuario,
        // el HTML resultante contiene un formulario y la ruta con
        // la acción para crear la nueva tarea.

        String urlPeticion = "/usuarios/" + usuarioId.toString() + "/tareas/nueva";
        String urlAction = "action=\"/usuarios/" + usuarioId.toString() + "/tareas/nueva\"";

        this.mockMvc.perform(get(urlPeticion))
                .andExpect((content().string(allOf(
                        containsString("form method=\"post\""),
                        containsString(urlAction)
                ))));
    }

    @Test
    public void postNuevaTareaDevuelveRedirectYAñadeTarea() throws Exception {
        // GIVEN
        // Un usuario con dos tareas en la BD
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");

        // Ver el comentario en el primer test
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // realizamos la petición POST para añadir una nueva tarea,
        // el estado HTTP que se devuelve es un REDIRECT al listado
        // de tareas.

        String urlPost = "/usuarios/" + usuarioId.toString() + "/tareas/nueva";
        String urlRedirect = "/usuarios/" + usuarioId.toString() + "/tareas";

        this.mockMvc.perform(post(urlPost)
                        .param("titulo", "Estudiar examen MADS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        // y si después consultamos el listado de tareas con una petición
        // GET el HTML contiene la tarea añadida.

        this.mockMvc.perform(get(urlRedirect))
                .andExpect((content().string(containsString("Estudiar examen MADS"))));
    }

    @Test
    public void deleteTareaDevuelveOKyBorraTarea() throws Exception {
        // GIVEN
        // Un usuario con dos tareas en la BD
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaLavarCocheId = ids.get("tareaId");

        // Ver el comentario en el primer test
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // realizamos la petición DELETE para borrar una tarea,
        // se devuelve el estado HTTP que se devuelve es OK,

        String urlDelete = "/tareas/" + tareaLavarCocheId.toString();

        this.mockMvc.perform(delete(urlDelete))
                .andExpect(status().isOk());

        // y cuando se pide un listado de tareas del usuario, la tarea borrada ya no aparece.

        String urlListado = "/usuarios/" + usuarioId + "/tareas";

        this.mockMvc.perform(get(urlListado))
                .andExpect(content().string(
                        allOf(not(containsString("Lavar coche")),
                                containsString("Renovar DNI"))));
    }

    @Test
    public void editarTareaActualizaLaTarea() throws Exception {
        // GIVEN
        // Un usuario con dos tareas en la BD
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaLavarCocheId = ids.get("tareaId");

        // Ver el comentario en el primer test
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // realizamos una petición POST al endpoint para editar una tarea

        String urlEditar = "/tareas/" + tareaLavarCocheId + "/editar";
        String urlRedirect = "/usuarios/" + usuarioId + "/tareas";

        this.mockMvc.perform(post(urlEditar)
                        .param("titulo", "Limpiar cristales coche"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        // Y si realizamos un listado de las tareas del usuario
        // ha cambiado el título de la tarea modificada

        String urlListado = "/usuarios/" + usuarioId + "/tareas";

        this.mockMvc.perform(get(urlListado))
                .andExpect(content().string(containsString("Limpiar cristales coche")));
    }

    @Test
    public void cambiarEstadoCompletadaTareaRedirigeYActualizaEstado() throws Exception {
        // GIVEN
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN: marcamos la tarea como completada
        String urlCompletar = "/tareas/" + tareaId + "/completar";
        String urlRedirect = "/usuarios/" + usuarioId + "/tareas?estado=todas";

        this.mockMvc.perform(post(urlCompletar))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect))
                .andExpect(flash().attribute("mensaje", "Estado de la tarea actualizado"));

        // THEN: en BD la tarea aparece como completada
        assertThat(tareaService.findById(tareaId).isCompletada()).isTrue();
    }

    @Test
    public void filtrarTareasPorEstadoMuestraSoloLasCorrespondientes() throws Exception {
        // GIVEN: usuario con "Lavar coche" (completada) y "Renovar DNI" (pendiente)
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaLavarCocheId = ids.get("tareaId");

        tareaService.cambiarEstadoCompletada(tareaLavarCocheId);
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlBase = "/usuarios/" + usuarioId + "/tareas";

        // WHEN & THEN: filtro "pendientes"
        this.mockMvc.perform(get(urlBase).param("estado", "pendientes"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Renovar DNI"),
                        not(containsString("Lavar coche"))
                )));

        // WHEN & THEN: filtro "completadas"
        this.mockMvc.perform(get(urlBase).param("estado", "completadas"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Lavar coche"),
                        not(containsString("Renovar DNI"))
                )));

        // WHEN & THEN: filtro "todas"
        this.mockMvc.perform(get(urlBase).param("estado", "todas"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Lavar coche"),
                        containsString("Renovar DNI")
                )));
    }

    @Test
    public void usuarioNoPropietarioNoPuedeCambiarEstadoTarea() throws Exception {
        // GIVEN
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        // Logeado otro usuario distinto al dueño de la tarea
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId + 99);

        // WHEN & THEN
        this.mockMvc.perform(post("/tareas/" + tareaId + "/completar"))
                .andExpect(status().is3xxRedirection());

        // La tarea sigue sin completarse
        assertThat(tareaService.findById(tareaId).isCompletada()).isFalse();
    }

    @Test
    public void listaTareasMuestraContadorPendientesYCompletadas() throws Exception {
        // GIVEN: usuario logeado con 2 tareas, de las que completamos una
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        tareaService.cambiarEstadoCompletada(ids.get("tareaId"));
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN: las pestañas muestran el número de tareas de cada estado
        this.mockMvc.perform(get("/usuarios/" + usuarioId + "/tareas"))
                .andExpect(content().string(allOf(
                        containsString("Todas (2)"),
                        containsString("Pendientes (1)"),
                        containsString("Completadas (1)"))));
    }

    @Test
    public void crearYEditarTareaConPrioridadDesdeWeb() throws Exception {
        // GIVEN
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // 1. El formulario de nueva tarea incluye el selector de prioridad
        this.mockMvc.perform(get("/usuarios/" + usuarioId + "/tareas/nueva"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("name=\"prioridad\""),
                        containsString("value=\"ALTA\""),
                        containsString("value=\"MEDIA\""),
                        containsString("value=\"BAJA\"")
                )));

        // 2. Al enviar POST con prioridad ALTA, se crea con prioridad ALTA
        this.mockMvc.perform(post("/usuarios/" + usuarioId + "/tareas/nueva")
                        .param("titulo", "Preparar demo")
                        .param("prioridad", "ALTA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/" + usuarioId + "/tareas"));

        TareaData tareaCreada = tareaService.allTareasUsuario(usuarioId).stream()
                .filter(t -> "Preparar demo".equals(t.getTitulo()))
                .findFirst()
                .orElse(null);
        assertThat(tareaCreada).isNotNull();
        assertThat(tareaCreada.getPrioridad()).isEqualTo("ALTA");

        // 3. El formulario de edición preselecciona la prioridad actual (ALTA)
        this.mockMvc.perform(get("/tareas/" + tareaCreada.getId() + "/editar"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"ALTA\" selected=\"selected\"")));

        // 4. Al editar y cambiar la prioridad a BAJA, se actualiza en BD
        this.mockMvc.perform(post("/tareas/" + tareaCreada.getId() + "/editar")
                        .param("titulo", "Preparar demo final")
                        .param("prioridad", "BAJA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/" + usuarioId + "/tareas"));

        TareaData tareaEditada = tareaService.findById(tareaCreada.getId());
        assertThat(tareaEditada.getTitulo()).isEqualTo("Preparar demo final");
        assertThat(tareaEditada.getPrioridad()).isEqualTo("BAJA");
    }

    @Test
    public void filtrarTareasPorPrioridadYEstadoEnListadoWeb() throws Exception {
        // GIVEN: "Lavar coche" (MEDIA, pendiente), "Renovar DNI" (MEDIA, pendiente),
        // "Tarea urgente" (ALTA, pendiente) y "Tarea urgente hecha" (ALTA, completada)
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");

        tareaService.nuevaTareaUsuario(usuarioId, "Tarea urgente", "ALTA");
        TareaData altaHecha = tareaService.nuevaTareaUsuario(usuarioId, "Tarea urgente hecha", "ALTA");
        tareaService.cambiarEstadoCompletada(altaHecha.getId());

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);
        String urlBase = "/usuarios/" + usuarioId + "/tareas";

        // WHEN & THEN: filtrar solo por prioridad=ALTA
        this.mockMvc.perform(get(urlBase).param("prioridad", "ALTA"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Tarea urgente"),
                        containsString("Tarea urgente hecha"),
                        not(containsString("Lavar coche")),
                        not(containsString("Renovar DNI"))
                )));

        // WHEN & THEN: combinar estado=pendientes y prioridad=ALTA
        this.mockMvc.perform(get(urlBase)
                        .param("estado", "pendientes")
                        .param("prioridad", "ALTA"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Tarea urgente"),
                        not(containsString("Tarea urgente hecha")),
                        not(containsString("Lavar coche"))
                )));
    }

    @Test
    public void cambiarEstadoMantieneFiltroDePrioridadActivo() throws Exception {
        // GIVEN
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        TareaData tareaAlta = tareaService.nuevaTareaUsuario(usuarioId, "Revisar PR", "ALTA");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN & THEN
        this.mockMvc.perform(post("/tareas/" + tareaAlta.getId() + "/completar")
                        .param("estado", "pendientes")
                        .param("prioridad", "ALTA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/" + usuarioId + "/tareas?estado=pendientes&prioridad=ALTA"));
    }
}
