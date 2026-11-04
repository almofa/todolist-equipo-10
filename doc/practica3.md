# Documentación Técnica - Práctica 3: MADS ToDoList

## 1. Visión General de la Práctica
En esta tercera parte de la práctica se ha desarrollado la versión **1.2.0** de la aplicación ToDoList aplicando la metodología TDD (Test-Driven Development). Además, se ha integrado la aplicación con bases de datos PostgreSQL tanto en el perfil de desarrollo como en la suite de integración continua con GitHub Actions.

---

## 2. Historias de Usuario Desarrolladas

### 2.1. HU 008: Listado de Equipos
Permite a cualquier usuario autenticado consultar el listado global de equipos registrados en la organización, ordenados alfabéticamente, así como acceder al detalle de sus miembros.

#### Endpoints Definidos
* `GET /equipos`: Recupera y muestra la lista completa de equipos existentes ordenados alfabéticamente por nombre.
* `GET /equipos/{id}`: Renderiza la vista de detalle del equipo especificado, mostrando la información básica del equipo y el listado de usuarios que lo integran.

#### Clases, Métodos y Vistas Involucrados
* **`EquipoController`**: Expone los métodos `listadoEquipos` y `detalleEquipo`. Utiliza `ManagerUserSession` para verificar que el usuario está logueado en la sesión.
* **`EquipoService`**:
    * `findAllOrdenadoPorNombre()`: Consulta el repositorio y devuelve el listado de DTOs (`EquipoData`) ordenados alfabéticamente.
    * `recuperarEquipo(Long id)`: Recupera la información de un equipo específico por su identificador.
    * `usuariosEquipo(Long id)`: Devuelve la lista de usuarios (`UsuarioData`) asociados al equipo especificado.
* **Plantillas Thymeleaf**: `listadoEquipos.html` (para el listado) y `detalleEquipo.html` (para la vista detallada de miembros).

---

### 2.2. HU 009: Gestionar Pertenencia al Equipo
Permite a los usuarios crear nuevos equipos de trabajo, unirse voluntariamente a cualquier equipo o salir de los equipos a los que pertenece.

#### Endpoints Definidos
* `POST /equipos/nuevo`: Procesa la creación de un nuevo equipo en el sistema a partir del nombre facilitado.
* `POST /equipos/{id}/apply`: Registra al usuario autenticado como miembro del equipo especificado.
* `POST /equipos/{id}/exit`: Desvincula al usuario autenticado del equipo correspondiente.

#### Clases, Métodos y Vistas Involucrados
* **`EquipoController`**: Expone los métodos `crearEquipo`, `anadirUsuarioAEquipo` y `salirDeEquipo`.
* **`EquipoService`**:
    * `crearEquipo(String nombre)`: Persiste una nueva entidad `Equipo` en la base de datos.
    * `añadirUsuarioAEquipo(Long idEquipo, Long idUsuario)`: Vincula al usuario con el equipo sincronizando ambos lados de la relación bidireccional.
    * `eliminarUsuarioDeEquipo(Long idEquipo, Long idUsuario)`: Desvincula al usuario del equipo actualizando ambas colecciones en memoria y BD.
* **Plantillas Thymeleaf**: `listadoEquipos.html` y `detalleEquipo.html` incluyen los formularios y botones dinámicos para solicitar el ingreso o la salida del equipo.

---

### 2.3. HU 010: Gestión de Equipos (Administración)
Concede permisos exclusivos a los usuarios con rol de **Administrador** para renombrar o eliminar equipos de la organización.

#### Endpoints Definidos
* `GET /equipos/{id}/editar`: Carga y muestra el formulario de edición de nombre para el equipo especificado.
* `POST /equipos/{id}/editar`: Procesa la modificación del nombre del equipo en la base de datos.
* `POST /equipos/{id}/borrar`: Elimina permanentemente el equipo del sistema.

#### Clases, Métodos y Vistas Involucrados
* **`EquipoController`**: Expone los métodos `formEditarEquipo`, `editarEquipo` y `borrarEquipo`.
* **`ManagerUserSession`**: Invoca el método `comprobarAdmin()`. Si el usuario no cuenta con rol de administrador, lanza la excepción `UnauthorizedException`.
* **`GlobalExceptionHandler`**: Captura la excepción `UnauthorizedException`, asigna el código de estado HTTP `401 UNAUTHORIZED` y renderiza la vista de error.
* **`EquipoService`**:
    * `modificarEquipo(Long idEquipo, String nuevoNombre)`: Actualiza el nombre del equipo persistido.
    * `borrarEquipo(Long idEquipo)`: Elimina la entidad `Equipo` de la base de datos.
* **Plantillas Thymeleaf**: `formEditarEquipo.html` para la edición y `error/401.html` para el acceso no autorizado.

---

## 3. Pruebas Automatizadas
Todas las capas del sistema han sido verificadas mediante tests automatizados:
* **Pruebas de Servicio y Repositorio (`EquipoTest` y `EquipoServiceTest`)**: Desarrolladas siguiendo el ciclo TDD (Red-Green-Refactor) para validar el comportamiento de las entidades, relaciones `@ManyToMany` y lógica de negocio.
* **Pruebas de Controlador (`EquipoWebTest`)**: Integradas mediante `MockMvc` para verificar la respuesta HTTP, redirecciones, atributos del modelo/flash y control de acceso por roles (respuestas `401 Unauthorized`).