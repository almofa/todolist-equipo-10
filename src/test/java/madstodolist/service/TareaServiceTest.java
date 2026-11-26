package madstodolist.service;

import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

// Hemos eliminado todos los @Transactional de los tests
// y usado un script para limpiar la BD de test después de
// cada test
// https://dev.to/henrykeys/don-t-use-transactional-in-tests-40eb

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class TareaServiceTest {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    TareaService tareaService;

    // Método para inicializar los datos de prueba en la BD
    // Devuelve un mapa con los identificadores del usuario y de la primera tarea añadida
    Map<String, Long> addUsuarioTareasBD() {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");

        // Añadimos un usuario a la base de datos
        UsuarioData usuarioNuevo = usuarioService.registrar(usuario);

        // Y añadimos dos tareas asociadas a ese usuario
        TareaData tarea1 = tareaService.nuevaTareaUsuario(usuarioNuevo.getId(), "Lavar coche");
        tareaService.nuevaTareaUsuario(usuarioNuevo.getId(), "Renovar DNI");

        // Devolvemos los ids del usuario y de la primera tarea añadida
        Map<String, Long> ids = new HashMap<>();
        ids.put("usuarioId", usuarioNuevo.getId());
        ids.put("tareaId", tarea1.getId());
        return ids;
    }

    @Test
    public void testNuevaTareaUsuario() {
        // GIVEN
        // Un usuario en la BD

        Long usuarioId = addUsuarioTareasBD().get("usuarioId");

        // WHEN
        // creamos una nueva tarea asociada al usuario,
        TareaData nuevaTarea = tareaService.nuevaTareaUsuario(usuarioId, "Práctica 1 de MADS");

        // THEN
        // al recuperar la lista de tareas del usuario, la nueva tarea
        // está en la lista de tareas del usuario.

        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);

        assertThat(tareas).hasSize(3);
        assertThat(tareas).contains(nuevaTarea);
    }

    @Test
    public void testBuscarTarea() {
        // GIVEN
        // Una tarea en la BD

        Long tareaId = addUsuarioTareasBD().get("tareaId");

        // WHEN
        // recuperamos una tarea de la base de datos a partir de su ID,

        TareaData lavarCoche = tareaService.findById(tareaId);

        // THEN
        // los datos de la tarea recuperada son correctos.

        assertThat(lavarCoche).isNotNull();
        assertThat(lavarCoche.getTitulo()).isEqualTo("Lavar coche");
    }

    @Test
    public void testModificarTarea() {
        // GIVEN
        // Un usuario y una tarea en la BD

        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        // WHEN
        // modificamos la tarea correspondiente al identificador,

        tareaService.modificaTarea(tareaId, "Limpiar los cristales del coche");

        // THEN
        // al buscar por el identificador en la base de datos se devuelve la tarea modificada

        TareaData tareaBD = tareaService.findById(tareaId);
        assertThat(tareaBD.getTitulo()).isEqualTo("Limpiar los cristales del coche");

        // y el usuario tiene también esa tarea modificada.
        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);
        assertThat(tareas).contains(tareaBD);
    }

    @Test
    public void testBorrarTarea() {
        // GIVEN
        // Un usuario y una tarea en la BD

        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        // WHEN
        // borramos la tarea correspondiente al identificador,

        tareaService.borraTarea(tareaId);

        // THEN
        // la tarea ya no está en la base de datos ni en las tareas del usuario.

        assertThat(tareaService.findById(tareaId)).isNull();

        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);
        assertThat(tareas).hasSize(1);
    }

    @Test
    public void asignarEtiquetaATarea(){

        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        assertThat(tareaService.usuarioContieneTarea(usuarioId,tareaId)).isTrue();
    }

    @Test
    public void testCambiarEstadoCompletadaTarea() {
        // GIVEN
        Map<String, Long> ids = addUsuarioTareasBD();
        Long tareaId = ids.get("tareaId");

        // Estado inicial debe ser false
        TareaData tareaInicial = tareaService.findById(tareaId);
        assertThat(tareaInicial.isCompletada()).isFalse();

        // WHEN: marcamos como completada
        TareaData tareaCompletada = tareaService.cambiarEstadoCompletada(tareaId);

        // THEN
        assertThat(tareaCompletada.isCompletada()).isTrue();
        assertThat(tareaService.findById(tareaId).isCompletada()).isTrue();

        // WHEN: volvemos a cambiar estado (desmarcar)
        TareaData tareaPendiente = tareaService.cambiarEstadoCompletada(tareaId);

        // THEN
        assertThat(tareaPendiente.isCompletada()).isFalse();
        assertThat(tareaService.findById(tareaId).isCompletada()).isFalse();
    }

    @Test
    public void testCambiarEstadoTareaInexistenteLanzaExcepcion() {
        assertThatThrownBy(() -> tareaService.cambiarEstadoCompletada(999L))
                .isInstanceOf(TareaServiceException.class);
    }

    @Test
    public void testFiltrarTareasUsuarioPorEstado() {
        // GIVEN: usuario con 2 tareas ("Lavar coche" y "Renovar DNI")
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tarea1Id = ids.get("tareaId");

        // Completamos la primera tarea ("Lavar coche")
        tareaService.cambiarEstadoCompletada(tarea1Id);

        // WHEN
        List<TareaData> todas = tareaService.allTareasUsuarioPorEstado(usuarioId, "todas");
        List<TareaData> pendientes = tareaService.allTareasUsuarioPorEstado(usuarioId, "pendientes");
        List<TareaData> completadas = tareaService.allTareasUsuarioPorEstado(usuarioId, "completadas");

        // THEN
        assertThat(todas).hasSize(2);

        assertThat(pendientes).hasSize(1);
        assertThat(pendientes.get(0).getTitulo()).isEqualTo("Renovar DNI");

        assertThat(completadas).hasSize(1);
        assertThat(completadas.get(0).getTitulo()).isEqualTo("Lavar coche");
    }

    @Test
    public void testContarTareasUsuarioPorEstado() {
        // GIVEN: usuario con 2 tareas, de las que completamos una
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        tareaService.cambiarEstadoCompletada(ids.get("tareaId"));

        // WHEN
        long pendientes = tareaService.contarTareasPorEstado(usuarioId, false);
        long completadas = tareaService.contarTareasPorEstado(usuarioId, true);

        // THEN: hay 1 pendiente y 1 completada
        assertThat(pendientes).isEqualTo(1);
        assertThat(completadas).isEqualTo(1);
    }

    @Test
    public void testCrearYModificarTareaConPrioridad() {
        // GIVEN
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaPorDefectoId = ids.get("tareaId");

        // Una tarea creada sin especificar prioridad debe tener "MEDIA"
        TareaData tareaPorDefecto = tareaService.findById(tareaPorDefectoId);
        assertThat(tareaPorDefecto.getPrioridad()).isEqualTo("MEDIA");

        // WHEN: creamos una tarea indicando prioridad "ALTA"
        TareaData tareaAlta = tareaService.nuevaTareaUsuario(usuarioId, "Entregar memoria", "ALTA");

        // THEN
        assertThat(tareaAlta.getPrioridad()).isEqualTo("ALTA");
        assertThat(tareaService.findById(tareaAlta.getId()).getPrioridad()).isEqualTo("ALTA");

        // WHEN: modificamos título y prioridad a "BAJA"
        TareaData tareaModificada = tareaService.modificaTarea(tareaAlta.getId(), "Entregar memoria final", "BAJA");

        // THEN
        assertThat(tareaModificada.getTitulo()).isEqualTo("Entregar memoria final");
        assertThat(tareaModificada.getPrioridad()).isEqualTo("BAJA");
        assertThat(tareaService.findById(tareaAlta.getId()).getPrioridad()).isEqualTo("BAJA");
    }

    @Test
    public void testFiltrarTareasPorEstadoYPrioridad() {
        // GIVEN: usuario con tareas de distintas prioridades y estados
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        // addUsuarioTareasBD crea "Lavar coche" (MEDIA, pendiente) y "Renovar DNI" (MEDIA, pendiente)

        TareaData altaPendiente = tareaService.nuevaTareaUsuario(usuarioId, "Examen MADS", "ALTA");
        TareaData altaCompletada = tareaService.nuevaTareaUsuario(usuarioId, "Enviar practica", "ALTA");
        tareaService.cambiarEstadoCompletada(altaCompletada.getId());

        // WHEN
        List<TareaData> todasAltas = tareaService.allTareasUsuarioPorEstadoYPrioridad(usuarioId, "todas", "ALTA");
        List<TareaData> pendientesAltas = tareaService.allTareasUsuarioPorEstadoYPrioridad(usuarioId, "pendientes", "ALTA");
        List<TareaData> completadasAltas = tareaService.allTareasUsuarioPorEstadoYPrioridad(usuarioId, "completadas", "ALTA");
        List<TareaData> pendientesTodasPrioridades = tareaService.allTareasUsuarioPorEstadoYPrioridad(usuarioId, "pendientes", "TODAS");

        // THEN
        assertThat(todasAltas).hasSize(2);
        assertThat(todasAltas).extracting(TareaData::getTitulo).containsExactly("Examen MADS", "Enviar practica");

        assertThat(pendientesAltas).hasSize(1);
        assertThat(pendientesAltas.get(0).getTitulo()).isEqualTo("Examen MADS");

        assertThat(completadasAltas).hasSize(1);
        assertThat(completadasAltas.get(0).getTitulo()).isEqualTo("Enviar practica");

        assertThat(pendientesTodasPrioridades).hasSize(3);
    }

    @Test
    public void testModificarTareaConPrioridadInexistenteLanzaExcepcion() {
        assertThatThrownBy(() -> tareaService.modificaTarea(999L, "Titulo", "ALTA"))
                .isInstanceOf(TareaServiceException.class);
    }

    @Test
    public void testCrearTareaConPrioridadNoValidaLanzaExcepcion() {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");

        assertThatThrownBy(() -> tareaService.nuevaTareaUsuario(usuarioId, "Tarea rara", "URGENTE"))
                .isInstanceOf(TareaServiceException.class);
    }

    @Test
    public void testModificarTareaConPrioridadNoValidaLanzaExcepcion() {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long tareaId = ids.get("tareaId");

        assertThatThrownBy(() -> tareaService.modificaTarea(tareaId, "Nuevo titulo", "CRITICA"))
                .isInstanceOf(TareaServiceException.class);
    }

    @Test
    public void testPrioridadConEspaciosYMinusculasSeNormaliza() {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");

        TareaData tarea = tareaService.nuevaTareaUsuario(usuarioId, "Tarea con espacios", "  alta  ");
        assertThat(tarea.getPrioridad()).isEqualTo("ALTA");

        List<TareaData> filtradas = tareaService.allTareasUsuarioPorEstadoYPrioridad(usuarioId, " todas ", " alta ");
        assertThat(filtradas).hasSize(1);
        assertThat(filtradas.get(0).getId()).isEqualTo(tarea.getId());
    }
}
