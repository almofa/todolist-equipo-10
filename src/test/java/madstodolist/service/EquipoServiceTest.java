package madstodolist.service;

import madstodolist.dto.UsuarioData;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import madstodolist.dto.EquipoData;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class EquipoServiceTest {

    @Autowired
    EquipoService equipoService;

    @Autowired
    UsuarioService usuarioService;

    @Test
    public void crearRecuperarEquipo() {
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        assertThat(equipo.getId()).isNotNull();

        EquipoData equipoBd = equipoService.recuperarEquipo(equipo.getId());
        assertThat(equipoBd).isNotNull();
        assertThat(equipoBd.getNombre()).isEqualTo("Proyecto 1");
    }

    @Test
    public void listadoEquiposOrdenAlfabetico() {
        // GIVEN
        // Dos equipos en la base de datos
        equipoService.crearEquipo("Proyecto BBB");
        equipoService.crearEquipo("Proyecto AAA");

        // WHEN
        // Recuperamos los equipos
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();

        // THEN
        // Los equipos están ordenados por nombre
        assertThat(equipos).hasSize(2);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Proyecto AAA");
        assertThat(equipos.get(1).getNombre()).isEqualTo("Proyecto BBB");
    }

    @Test
    public void añadirUsuarioAEquipo() {
        // GIVEN
        // Un usuario y un equipo en la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");

        // WHEN
        // Añadimos el usuario al equipo
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // THEN
        // El usuario pertenece al equipo
        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuarios).hasSize(1);
        assertThat(usuarios.get(0).getEmail()).isEqualTo("user@ua");
    }

    @Test
    public void recuperarEquiposDeUsuario() {
        // GIVEN
        // Un usuario y dos equipos en la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipo1 = equipoService.crearEquipo("Proyecto 1");
        EquipoData equipo2 = equipoService.crearEquipo("Proyecto 2");
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), usuario.getId());
        equipoService.añadirUsuarioAEquipo(equipo2.getId(), usuario.getId());

        // WHEN
        // Recuperamos los equipos del usuario
        List<EquipoData> equipos = equipoService.equiposUsuario(usuario.getId());

        // THEN
        // El usuario pertenece a los dos equipos
        assertThat(equipos).hasSize(2);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Proyecto 1");
        assertThat(equipos.get(1).getNombre()).isEqualTo("Proyecto 2");
    }

    @Test
    public void comprobarExcepciones() {
        // Comprobamos las excepciones lanzadas por los métodos
        // recuperarEquipo, añadirUsuarioAEquipo, usuariosEquipo y equiposUsuario
        assertThatThrownBy(() -> equipoService.recuperarEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(1L, 1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.usuariosEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.equiposUsuario(1L))
                .isInstanceOf(EquipoServiceException.class);

        // Creamos un equipo pero no un usuario y comprobamos que también se lanza una excepción
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(equipo.getId(), 1L))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void eliminarUsuarioDeEquipo() {
        // GIVEN: Un usuario y un equipo creados en la BD y relacionados
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user.eliminar@ua.es");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);

        EquipoData equipo = equipoService.crearEquipo("Proyecto Eliminar");
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // Verificamos que inicialmente el usuario pertenece al equipo
        List<UsuarioData> usuariosIniciales = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuariosIniciales).hasSize(1);

        // WHEN: Eliminamos al usuario del equipo a través del servicio
        equipoService.eliminarUsuarioDeEquipo(equipo.getId(), usuario.getId());

        // THEN: El equipo ya no tiene usuarios y el usuario ya no tiene equipos
        List<UsuarioData> usuariosFinales = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuariosFinales).isEmpty();

        List<EquipoData> equiposUsuario = equipoService.equiposUsuario(usuario.getId());
        assertThat(equiposUsuario).isEmpty();
    }

    @Test
    public void crearEquipoNombreVacioLanzaExcepcion() {
        assertThatThrownBy(() -> equipoService.crearEquipo(""))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.crearEquipo("   "))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void añadirUsuarioYaPertenecienteLanzaExcepcion() {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user.duplicado@ua.es");
        usuario.setPassword("123");
        UsuarioData usuarioRegistrado = usuarioService.registrar(usuario); // Usamos una nueva variable

        EquipoData equipo = equipoService.crearEquipo("Proyecto Duplicado");
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuarioRegistrado.getId());

        // Intentar añadirlo de nuevo debe lanzar excepción (usando la variable efectivamente final)
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(equipo.getId(), usuarioRegistrado.getId()))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void eliminarUsuarioDeEquipoInexistenteOnoPertenecienteLanzaExcepcion() {
        // 1. Equipo o usuario inexistente
        assertThatThrownBy(() -> equipoService.eliminarUsuarioDeEquipo(999L, 1L))
                .isInstanceOf(EquipoServiceException.class);

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user.fuera@ua.es");
        usuario.setPassword("123");
        UsuarioData usuarioRegistrado = usuarioService.registrar(usuario); // Variable nueva
        EquipoData equipo = equipoService.crearEquipo("Proyecto Fuera");

        assertThatThrownBy(() -> equipoService.eliminarUsuarioDeEquipo(equipo.getId(), 999L))
                .isInstanceOf(EquipoServiceException.class);

        // 2. Usuario que no pertenece al equipo (usando usuarioRegistrado)
        assertThatThrownBy(() -> equipoService.eliminarUsuarioDeEquipo(equipo.getId(), usuarioRegistrado.getId()))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void modificarNombreEquipo() {
        // GIVEN: Un equipo creado en el sistema
        EquipoData equipo = equipoService.crearEquipo("Proyecto Alfa");
        Long idEquipo = equipo.getId();

        // WHEN: Modificamos el nombre del equipo
        equipoService.modificarEquipo(idEquipo, "Proyecto Alfa Renombrado");

        // THEN: Al recuperar el equipo, el nombre se ha actualizado correctamente
        EquipoData equipoModificado = equipoService.recuperarEquipo(idEquipo);
        assertThat(equipoModificado.getNombre()).isEqualTo("Proyecto Alfa Renombrado");
    }

    @Test
    public void modificarEquipoInexistenteLanzaExcepcion() {
        // WHEN & THEN: Intentar modificar un equipo que no existe lanza EquipoServiceException
        assertThatThrownBy(() -> equipoService.modificarEquipo(999L, "Proyecto Inexistente"))
                .isInstanceOf(EquipoServiceException.class)
                .hasMessage("Equipo no encontrado");
    }

    @Test
    public void borrarEquipo() {
        // GIVEN: Un equipo creado en el sistema
        EquipoData equipo = equipoService.crearEquipo("Proyecto a Borrar");
        Long idEquipo = equipo.getId();

        // WHEN: Borramos el equipo
        equipoService.borrarEquipo(idEquipo);

        // THEN: Al intentar recuperarlo, lanza la excepción de que no existe
        assertThatThrownBy(() -> equipoService.recuperarEquipo(idEquipo))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void borrarEquipoInexistenteLanzaExcepcion() {
        // WHEN & THEN: Intentar borrar un equipo que no existe lanza EquipoServiceException
        assertThatThrownBy(() -> equipoService.borrarEquipo(999L))
                .isInstanceOf(EquipoServiceException.class)
                .hasMessage("Equipo no encontrado");
    }

    @Test
    public void borrarEquipoConUsuarios() {
        // GIVEN: Un equipo y un usuario en el sistema, y el usuario añadido al equipo
        EquipoData equipo = equipoService.crearEquipo("Proyecto con Miembros");
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user-equipo-borrar@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);

        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // WHEN: Borramos el equipo
        equipoService.borrarEquipo(equipo.getId());

        // THEN: El equipo ya no existe y el usuario sigue existiendo pero ya no tiene ese equipo
        assertThatThrownBy(() -> equipoService.recuperarEquipo(equipo.getId()))
                .isInstanceOf(EquipoServiceException.class);

        List<EquipoData> equiposUsuario = equipoService.equiposUsuario(usuario.getId());
        assertThat(equiposUsuario).isEmpty();
    }

}