package madstodolist.service;

import madstodolist.dto.EquipoData;
import madstodolist.dto.ResumenUsuarioData;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class ResumenServiceTest {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    TareaService tareaService;

    @Autowired
    EquipoService equipoService;

    @Autowired
    ResumenService resumenService;

    // Registra en la BD el usuario de prueba y lo devuelve
    private UsuarioData registrarUsuario() {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        return usuarioService.registrar(usuario);
    }

    @Test
    public void testResumenTotalesTareas() {
        // GIVEN: un usuario con 3 tareas, una de ellas completada
        UsuarioData usuarioNuevo = registrarUsuario();

        TareaData tarea1 = tareaService.nuevaTareaUsuario(usuarioNuevo.getId(), "Lavar coche");
        tareaService.nuevaTareaUsuario(usuarioNuevo.getId(), "Renovar DNI");
        tareaService.nuevaTareaUsuario(usuarioNuevo.getId(), "Estudiar MADS");
        tareaService.cambiarEstadoCompletada(tarea1.getId());

        // WHEN
        ResumenUsuarioData resumen = resumenService.resumenUsuario(usuarioNuevo.getId());

        // THEN
        assertThat(resumen.getTotalTareas()).isEqualTo(3);
        assertThat(resumen.getTareasCompletadas()).isEqualTo(1);
        assertThat(resumen.getTareasPendientes()).isEqualTo(2);
    }

    @Test
    public void testResumenPorcentajeCompletado() {
        // GIVEN: un usuario con 3 tareas, dos de ellas completadas
        UsuarioData usuarioNuevo = registrarUsuario();

        TareaData tarea1 = tareaService.nuevaTareaUsuario(usuarioNuevo.getId(), "Lavar coche");
        TareaData tarea2 = tareaService.nuevaTareaUsuario(usuarioNuevo.getId(), "Renovar DNI");
        tareaService.nuevaTareaUsuario(usuarioNuevo.getId(), "Estudiar MADS");
        tareaService.cambiarEstadoCompletada(tarea1.getId());
        tareaService.cambiarEstadoCompletada(tarea2.getId());

        // WHEN
        ResumenUsuarioData resumen = resumenService.resumenUsuario(usuarioNuevo.getId());

        // THEN: 2 de 3 = 66,67 %, redondeado a 67
        assertThat(resumen.getPorcentajeCompletado()).isEqualTo(67);
    }

    @Test
    public void testResumenSinTareas() {
        // GIVEN: un usuario sin tareas
        UsuarioData usuarioNuevo = registrarUsuario();

        // WHEN
        ResumenUsuarioData resumen = resumenService.resumenUsuario(usuarioNuevo.getId());

        // THEN: sin tareas, el porcentaje es 0 (y no hay error de división)
        assertThat(resumen.getTotalTareas()).isEqualTo(0);
        assertThat(resumen.getTareasCompletadas()).isEqualTo(0);
        assertThat(resumen.getTareasPendientes()).isEqualTo(0);
        assertThat(resumen.getPorcentajeCompletado()).isEqualTo(0);
    }

    @Test
    public void testResumenEquiposUsuario() {
        // GIVEN: un usuario que pertenece a un equipo
        UsuarioData usuarioNuevo = registrarUsuario();

        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuarioNuevo.getId());

        // WHEN
        ResumenUsuarioData resumen = resumenService.resumenUsuario(usuarioNuevo.getId());

        // THEN: el resumen contiene ese equipo
        assertThat(resumen.getEquipos()).hasSize(1);
        assertThat(resumen.getEquipos().get(0).getNombre()).isEqualTo("Proyecto 1");
    }
}