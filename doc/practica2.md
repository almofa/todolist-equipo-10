# Documentación Técnica: Evolución del Sistema y Nuevas Funcionalidades (Práctica 2)

## 1. Visión General

Esta documentación está dirigida al equipo de desarrollo y resume los cambios arquitectónicos, las nuevas funcionalidades y la estrategia de pruebas incorporada en la versión actual de la aplicación **MadsTodolist**.

El objetivo principal de esta iteración ha sido implementar la gestión de roles de administración, la visualización y detalle de usuarios registrados, el mecanismo de bloqueo de acceso para usuarios y la protección centralizada de rutas sensibles mediante control de acceso basado en roles.

---

## 2. Listado de Nuevas Clases y Métodos Implementados

### Capa de Servicio (`UsuarioService`)

* **`existeAdmin()`**: Comprueba mediante el repositorio si ya existe al menos un usuario con rol de administrador registrado en la aplicación.
* **`setBloqueoUsuario(Long id, boolean bloqueado)`**: Actualiza el estado de bloqueo de un usuario específico en la base de datos.
* **`login(String email, String password)`** *(modificado)*: Ampliado para retornar el nuevo estado `USER_BLOCKED` dentro del enumerado `LoginStatus` cuando un usuario con restricción de acceso intenta autenticarse.

### Capa de Control (`HomeController` y controladores web)

* **`comprobarAdmin()`**: Método auxiliar privado de seguridad que valida si el usuario que realiza la petición HTTP tiene una sesión activa y posee el rol de administrador. En caso contrario, interrumpe el flujo lanzando `UnauthorizedException`.
* **`listadoUsuarios(Model model)`** (`GET /registrados`): Renderiza el listado global de usuarios del sistema si la verificación de administración es satisfactoria.
* **`detalleUsuario(@PathVariable Long id, Model model)`** (`GET /registrados/{id}`): Muestra la ficha individual con los detalles de un usuario. Redirige a `/registrados` si el ID proporcionado no existe.
* **`bloquearUsuario(@PathVariable Long id)`** (`POST /registrados/{id}/bloqueo`): Permite al administrador alternar el estado de bloqueo de un usuario de la aplicación.

### Capa de Excepciones

* **`UnauthorizedException`**: Excepción personalizada anotada con `@ResponseStatus(HttpStatus.UNAUTHORIZED)` para forzar la devolución de un código de estado HTTP 401 en accesos no autorizados.

---

## 3. Plantillas Thymeleaf Añadidas y Modificadas

* **`src/main/resources/templates/listadoUsuarios.html`**: Presenta la tabla de usuarios registrados con sus datos principales y el botón dinámico de bloqueo/desbloqueo.
* **`src/main/resources/templates/detalleUsuario.html`**: Ficha individual con la información detallada del perfil de un usuario.
* **`src/main/resources/templates/error/401.html`**: Vista de error personalizada para notificar el acceso no autorizado.
* **`src/main/resources/templates/fragments.html`** *(modificada)*: Incorporación de la barra de navegación dinámica mediante condicionales `th:if` basados en el estado de la sesión y el rol del usuario.

---

## 4. Fragmentos de Código Fuente Relevantes

### 4.1. Verificación Centralizada de Autorización

Para mantener el principio DRY (*Don't Repeat Yourself*), la lógica de comprobación del rol de administrador se ha centralizado en el controlador mediante el método `comprobarAdmin()`:

```java
private void comprobarAdmin() {
    Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
    if (idUsuarioLogeado == null) {
        throw new UnauthorizedException();
    }
    UsuarioData usuario = usuarioService.findById(idUsuarioLogeado);
    if (usuario == null || !Boolean.TRUE.equals(usuario.getAdmin())) {
        throw new UnauthorizedException();
    }
}
```

**Explicación técnica:** Este método aplica defensividad en dos niveles. En primer lugar, verifica si existe una clave de sesión válida mediante `ManagerUserSession`. Posteriormente, valida contra la base de datos que la entidad no sea nula y que la propiedad `admin` esté establecida explícitamente en `true`. Si alguna condición falla, lanza `UnauthorizedException`, desencadenando de forma inmediata la respuesta HTTP 401.

### 4.2. Sanitización y Privacidad en DTOs

En las operaciones de lectura de la capa de servicio (`findById`, `findAll`), la conversión mediante `ModelMapper` se asegura de no exponer información sensible:

```java
@Transactional(readOnly = true)
public UsuarioData findById(Long usuarioId) {
    Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
    if (usuario == null) return null;
    
    UsuarioData usuarioData = modelMapper.map(usuario, UsuarioData.class);
    usuarioData.setPassword(null); // Desacoplamiento y privacidad
    return usuarioData;
}
```

**Explicación técnica:** Aunque `UsuarioData` se reutiliza en la captura de formularios de registro, en las operaciones de lectura se establece la propiedad `password` a `null` antes de retornar el objeto al controlador. Esto garantiza que ningún hash ni contraseña viaje hacia la plantilla HTML de Thymeleaf.

---

## 5. Estrategia de Testing Implementada

La suite de pruebas se ha estructurado siguiendo el patrón **GIVEN-WHEN-THEN** y cubriendo las tres capas principales de la arquitectura:

1. **Capa de Repositorio y Modelo (`UsuarioTest`)**: Pruebas unitarias para validar la lógica de los métodos `equals` y `hashCode`, así como pruebas de integración con `@Transactional` para comprobar las consultas derivadas de Spring Data JPA (`findByEmail`, `save`).
2. **Capa de Servicio (`UsuarioServiceTest`)**: Validación de reglas de negocio aisladas mediante Mocks (Mockito), probando la lógica de administrador único (`existeAdmin()`) y la denegación de login para cuentas restringidas (`USER_BLOCKED`).
3. **Capa Web / Controlador (`MockMvc`)**: Pruebas de integración de la capa web utilizando `@AutoConfigureMockMvc` para simular peticiones HTTP sin desplegar el servidor web completo:
    * **`ProteccionRutasWebTest`**: Verifica que las rutas administrativas devuelvan un estado HTTP 401 ante usuarios anónimos o no administradores.
    * **`ListadoUsuariosWebTest` y `DetalleUsuarioWebTest`**: Verifican que el código de respuesta sea HTTP 200 OK y que el HTML renderizado contenga la información esperada (garantizando además la ausencia de contraseñas en el DOM).
    * **`BloqueoUsuarioWebTest`**: Comprueba el envío de peticiones `POST` para la acción de bloqueo y la adecuada verificación de permisos.