package madstodolist.service;

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
    ResumenService resumenService;

    @Test
    public void testResumenTotalesTareas() {
        // GIVEN: un usuario con 3 tareas, una de ellas completada
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        UsuarioData usuarioNuevo = usuarioService.registrar(usuario);

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
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        UsuarioData usuarioNuevo = usuarioService.registrar(usuario);

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
}