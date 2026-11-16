package madstodolist.service;

import madstodolist.dto.UsuarioData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    // Método para inicializar los datos de prueba en la BD
    // Devuelve el identificador del usuario de la BD
    Long addUsuarioBD() {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setNombre("Usuario Ejemplo");
        usuario.setPassword("123");
        UsuarioData nuevoUsuario = usuarioService.registrar(usuario);
        return nuevoUsuario.getId();
    }

    @Test
    public void servicioLoginUsuario() {
        // GIVEN
        // Un usuario en la BD

        addUsuarioBD();

        // WHEN
        // intentamos logear un usuario y contraseña correctos
        UsuarioService.LoginStatus loginStatus1 = usuarioService.login("user@ua", "123");

        // intentamos logear un usuario correcto, con una contraseña incorrecta
        UsuarioService.LoginStatus loginStatus2 = usuarioService.login("user@ua", "000");

        // intentamos logear un usuario que no existe,
        UsuarioService.LoginStatus loginStatus3 = usuarioService.login("pepito.perez@gmail.com", "12345678");

        // THEN

        // el valor devuelto por el primer login es LOGIN_OK,
        assertThat(loginStatus1).isEqualTo(UsuarioService.LoginStatus.LOGIN_OK);

        // el valor devuelto por el segundo login es ERROR_PASSWORD,
        assertThat(loginStatus2).isEqualTo(UsuarioService.LoginStatus.ERROR_PASSWORD);

        // y el valor devuelto por el tercer login es USER_NOT_FOUND.
        assertThat(loginStatus3).isEqualTo(UsuarioService.LoginStatus.USER_NOT_FOUND);
    }

    @Test
    public void servicioRegistroUsuario() {
        // WHEN
        // Registramos un usuario con un e-mail no existente en la base de datos,

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("usuario.prueba2@gmail.com");
        usuario.setPassword("12345678");

        usuarioService.registrar(usuario);

        // THEN
        // el usuario se añade correctamente al sistema.

        UsuarioData usuarioBaseDatos = usuarioService.findByEmail("usuario.prueba2@gmail.com");
        assertThat(usuarioBaseDatos).isNotNull();
        assertThat(usuarioBaseDatos.getEmail()).isEqualTo("usuario.prueba2@gmail.com");
    }

    @Test
    public void servicioRegistroUsuarioExcepcionConNullPassword() {
        // WHEN, THEN
        // Si intentamos registrar un usuario con un password null,
        // se produce una excepción de tipo UsuarioServiceException

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("usuario.prueba@gmail.com");

        Assertions.assertThrows(UsuarioServiceException.class, () -> {
            usuarioService.registrar(usuario);
        });
    }


    @Test
    public void servicioRegistroUsuarioExcepcionConEmailRepetido() {
        // GIVEN
        // Un usuario en la BD

        addUsuarioBD();

        // THEN
        // Si registramos un usuario con un e-mail ya existente en la base de datos,
        // , se produce una excepción de tipo UsuarioServiceException

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("12345678");

        Assertions.assertThrows(UsuarioServiceException.class, () -> {
            usuarioService.registrar(usuario);
        });
    }

    @Test
    public void servicioRegistroUsuarioDevuelveUsuarioConId() {

        // WHEN
        // Si registramos en el sistema un usuario con un e-mail no existente en la base de datos,
        // y un password no nulo,

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("usuario.prueba@gmail.com");
        usuario.setPassword("12345678");

        UsuarioData usuarioNuevo = usuarioService.registrar(usuario);

        // THEN
        // se actualiza el identificador del usuario

        assertThat(usuarioNuevo.getId()).isNotNull();

        // con el identificador que se ha guardado en la BD.

        UsuarioData usuarioBD = usuarioService.findById(usuarioNuevo.getId());
        assertThat(usuarioBD).isEqualTo(usuarioNuevo);
    }

    @Test
    public void servicioConsultaUsuarioDevuelveUsuario() {
        // GIVEN
        // Un usuario en la BD

        Long usuarioId = addUsuarioBD();

        // WHEN
        // recuperamos un usuario usando su e-mail,

        UsuarioData usuario = usuarioService.findByEmail("user@ua");

        // THEN
        // el usuario obtenido es el correcto.

        assertThat(usuario.getId()).isEqualTo(usuarioId);
        assertThat(usuario.getEmail()).isEqualTo("user@ua");
        assertThat(usuario.getNombre()).isEqualTo("Usuario Ejemplo");
    }

    @Test
    public void obtenerTodosLosUsuariosDevuelveLista() {
        // GIVEN: Creamos y registramos un par de usuarios
        UsuarioData u1 = new UsuarioData();
        u1.setEmail("usuario1@gmail.com");
        u1.setPassword("1234");
        usuarioService.registrar(u1);

        UsuarioData u2 = new UsuarioData();
        u2.setEmail("usuario2@gmail.com");
        u2.setPassword("1234");
        usuarioService.registrar(u2);

        // WHEN: Llamamos a findAll()
        List<UsuarioData> usuarios = usuarioService.findAll();

        // THEN: Comprobamos que devuelve al menos los usuarios creados
        assertThat(usuarios).isNotEmpty();
        assertThat(usuarios.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    public void comprobarExisteAdminYRegistrarAdmin() {
        // 1. Al inicio no debe haber administradores
        assertThat(usuarioService.existeAdmin()).isFalse();

        // 2. Registramos un usuario como administrador
        UsuarioData admin = new UsuarioData();
        admin.setEmail("admin@gmail.com");
        admin.setPassword("1234");
        admin.setAdmin(true);
        usuarioService.registrar(admin);

        // 3. Ahora el método existeAdmin() debe devolver true
        assertThat(usuarioService.existeAdmin()).isTrue();
    }

    @Test
    public void bloquearUsuarioYComprobarLogin() {
        // 1. GIVEN: Un usuario registrado y activo
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("bloqueo@gmail.com");
        usuario.setPassword("1234");
        UsuarioData usuarioCreado = usuarioService.registrar(usuario);

        // Comprobamos que inicialmente puede hacer login
        assertThat(usuarioService.login("bloqueo@gmail.com", "1234"))
                .isEqualTo(UsuarioService.LoginStatus.LOGIN_OK);

        // 2. WHEN: El administrador lo bloquea
        usuarioService.setBloqueoUsuario(usuarioCreado.getId(), true);

        // 3. THEN: El login para este usuario devuelve USER_BLOCKED
        assertThat(usuarioService.login("bloqueo@gmail.com", "1234"))
                .isEqualTo(UsuarioService.LoginStatus.USER_BLOCKED);
    }

    @Test
    public void testModificarDescripcionUsuario() {
        // 1. Registramos un usuario de prueba
        UsuarioData usuarioData = new UsuarioData();
        usuarioData.setEmail("usuario.test@ua.es");
        usuarioData.setNombre("Usuario Test");
        usuarioData.setPassword("123456");
        UsuarioData usuarioRegistrado = usuarioService.registrar(usuarioData);

        // 2. Modificamos sus datos añadiendo una descripción
        usuarioRegistrado.setNombre("Nombre Modificado");
        usuarioRegistrado.setDescripcion("Esta es mi nueva descripción o biografía en el perfil.");

        usuarioService.modifica(usuarioRegistrado.getId(), usuarioRegistrado);

        // 3. Recuperamos el usuario de la base de datos y comprobamos que se ha guardado la descripción
        UsuarioData usuarioModificado = usuarioService.findById(usuarioRegistrado.getId());
        assertThat(usuarioModificado.getNombre()).isEqualTo("Nombre Modificado");
        assertThat(usuarioModificado.getDescripcion()).isEqualTo("Esta es mi nueva descripción o biografía en el perfil.");
    }
}