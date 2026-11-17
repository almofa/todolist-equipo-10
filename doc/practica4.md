# Práctica 4 - Trabajo en equipo con GitFlow y despliegue en producción

Equipo 10: Àlex, Nicole, Óscar e Iker.

## 1. Cambios en la aplicación (versión 1.3.0)

### Página *Acerca de*

- Muestra a todos los miembros del equipo, la versión `1.3.0` y la fecha de release.

### Descripción de equipos

- Nuevo campo `descripcion` en la entidad `Equipo` y en el DTO `EquipoData`.
- `EquipoService` permite crear y modificar equipos con descripción.
- `EquipoController` recibe el parámetro opcional `descripcion` en `POST /equipos/nuevo` y `POST /equipos/{id}/editar`.
- La descripción se muestra en `listadoEquipos.html` y `detalleEquipo.html` y se edita en `formEditarEquipo.html`.

### Número de miembros de los equipos

- El listado de equipos muestra cuántos miembros tiene cada equipo.
- Se corrige que el nombre del equipo apareciera duplicado en la tarjeta del listado.

### Cuenta de usuario

- Página `GET /cuenta` con los datos del usuario logeado.
- Edición de los datos y de la descripción en `/cuenta/editar` (nuevo campo `descripcion` en `Usuario` y `UsuarioData`).
- Cambio de contraseña en `/cuenta/password`.

### Tareas completadas

- Nuevo campo `completada` en la entidad `Tarea` y en `TareaData`.
- `POST /tareas/{id}/completar` marca o desmarca una tarea.
- El listado de tareas se puede filtrar por estado (`?estado=todas|pendientes|completadas`).
- El listado muestra un contador de tareas pendientes y completadas. `TareaService.contarTareasPorEstado` usa una consulta `count` en `TareaRepository`.

### Resumen del usuario

- `ResumenService` calcula el total de tareas, las completadas, el porcentaje completado y los equipos del usuario.
- `ResumenController` (`GET /resumen`) redirige al login si no hay sesión y muestra la plantilla `resumen.html`.
- Si el usuario no tiene tareas, se muestra un mensaje en lugar de la barra de progreso.
- Hay un enlace al resumen en el menú.

Las capas de servicio y repositorio se han desarrollado con TDD (tests de servicio sin `@Transactional`, con `clean-db.sql`). Los controllers y las vistas tienen sus tests web con `MockMvc`.

## 2. Imagen Docker

El `ENTRYPOINT` del `Dockerfile` usa `sh -c` para pasar a `java` los parámetros añadidos al final de `docker run`:

```dockerfile
ENTRYPOINT ["sh","-c","java -Djava.security.egd=file:/dev/urandom -jar /app.jar ${0} ${@}"]
```

`application-postgres.properties` lee `POSTGRES_HOST`, `POSTGRES_PORT`, `DB_USER` y `DB_PASSWD`, con valores por defecto para trabajar en local.

URL de la imagen: **PENDIENTE: poner la URL de Docker Hub de la imagen 1.3.0 cuando esté publicada**

## 3. Despliegue en producción

La base de datos y la aplicación se ejecutan en dos contenedores conectados por la red `network-equipo`:

```bash
docker network create network-equipo
docker run -d --network network-equipo --network-alias postgres -v ${PWD}:/mi-host --name db-equipo \
  -e POSTGRES_USER=mads -e POSTGRES_PASSWORD=mads -e POSTGRES_DB=mads postgres:13
docker run --network network-equipo -p8080:8080 <usuario>/<imagen>:1.3.0 \
  --spring.profiles.active=postgres-prod --POSTGRES_HOST=postgres
```

En producción se usa el perfil `postgres-prod` (`application-postgres-prod.properties`). Es igual que `postgres`, pero con `spring.jpa.hibernate.ddl-auto=validate`: Hibernate no modifica el esquema y la aplicación no arranca si las tablas no coinciden con las entidades.

## 4. Esquemas de datos y migración

Los ficheros están en la carpeta `sql/`:

| Fichero | Contenido |
|---|---|
| `schema-1.2.0.sql` | Esquema de la versión 1.2.0 |
| `schema-1.3.0.sql` | Esquema de la versión 1.3.0 |
| `schema-1.2.0-1.3.0.sql` | Script de migración de 1.2.0 a 1.3.0 |
| `backup16112026.sql` | Copia de seguridad de producción con la versión 1.2.0 |
| `backup17112026.sql` | Copia de seguridad tras migrar a 1.3.0 y añadir datos nuevos |

### Diferencias entre 1.2.0 y 1.3.0

| Tabla | 1.2.0 | 1.3.0 |
|---|---|---|
| `equipos` | `id`, `nombre` | se añade `descripcion varchar(255)` |
| `usuarios` | `id`, `admin`, `bloqueado`, `email`, `fecha_nacimiento`, `nombre`, `password` | se añade `descripcion varchar(255)` |
| `tareas` | `id`, `titulo`, `usuario_id` | se añade `completada boolean NOT NULL` |
| `equipo_usuario` | `fk_equipo`, `fk_usuario` | sin cambios |

Tablas de la versión 1.3.0 (extraídas de `schema-1.3.0.sql`):

```sql
CREATE TABLE public.equipos (
    id bigint NOT NULL,
    descripcion character varying(255),
    nombre character varying(255)
);
CREATE TABLE public.tareas (
    id bigint NOT NULL,
    completada boolean NOT NULL,
    titulo character varying(255) NOT NULL,
    usuario_id bigint NOT NULL
);
CREATE TABLE public.usuarios (
    id bigint NOT NULL,
    admin boolean,
    bloqueado boolean,
    descripcion character varying(255),
    email character varying(255) NOT NULL,
    fecha_nacimiento date,
    nombre character varying(255),
    password character varying(255)
);
```

### Script de migración (`schema-1.2.0-1.3.0.sql`)

```sql
ALTER TABLE public.usuarios ADD COLUMN descripcion character varying(255);
ALTER TABLE public.equipos ADD COLUMN descripcion character varying(255);
ALTER TABLE public.tareas ADD COLUMN completada boolean NOT NULL DEFAULT false;
```

Como `completada` es `NOT NULL`, la columna se crea con `DEFAULT false` para que las tareas existentes queden como pendientes.

### Prueba de la migración

1. Se lanza un contenedor de Postgres 13 con la base de datos vacía.
2. Se restaura la copia de la versión 1.2.0:
   `docker exec db-equipo psql -U mads -d mads -f /mi-host/sql/backup16112026.sql`
3. Se aplica la migración:
   `docker exec db-equipo psql -U mads -d mads -f /mi-host/sql/schema-1.2.0-1.3.0.sql`
4. Se arranca la versión 1.3.0 con el perfil `postgres-prod`. La validación del esquema pasa y la aplicación arranca.
5. Se comprueba que los datos anteriores se conservan: el usuario y sus tareas siguen ahí, y las tareas antiguas aparecen como pendientes.
6. Se añaden datos nuevos desde la aplicación: tareas, un equipo con descripción y una tarea completada.
7. Se hace la copia de seguridad de la nueva versión:
   `docker exec db-equipo sh -c "pg_dump --clean -U mads mads > /mi-host/sql/backup17112026.sql"`
