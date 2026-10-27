# API de Préstamo de Equipos Tecnológicos

## 1. Descripción del proyecto

`prestamos-api` es una API REST desarrollada con Spring Boot y Java para gestionar el préstamo de equipos tecnológicos. El sistema contempla la administración de usuarios, equipos, préstamos, mantenimientos y registros de trazabilidad.

La información se almacena en una base de datos PostgreSQL y se utiliza Spring Data JPA para la persistencia.

## 2. Tecnologías utilizadas

- Java
- Spring Boot
- Spring Data JPA
- Hibernate
- PostgreSQL
- Maven
- Git y GitHub

## 3. Dirección local

La API se ejecuta localmente en:

`http://localhost:8080`

## 4. Catálogo de endpoints

Los siguientes endpoints corresponden al diseño previsto para la API. Su estado debe actualizarse a medida que se implementen y prueben.

### 4.1. Estado del servicio

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/estado` | Verifica que el servicio esté disponible. |

### 4.2. Usuarios

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/usuarios` | Consulta todos los usuarios. |
| GET | `/api/usuarios/{id}` | Consulta un usuario por su identificador. |
| POST | `/api/usuarios` | Registra un usuario. |
| PUT | `/api/usuarios/{id}` | Actualiza los datos de un usuario. |
| PATCH | `/api/usuarios/{id}` | Actualiza parcialmente los datos de un usuario. |
| DELETE | `/api/usuarios/{id}` | Elimina un usuario cuando las reglas del sistema lo permiten. |

### 4.3. Equipos

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/equipos` | Consulta los equipos registrados. |
| GET | `/api/equipos?estado=DISPONIBLE` | Consulta equipos filtrados por estado. |
| GET | `/api/equipos/{id}` | Consulta un equipo por su identificador. |
| POST | `/api/equipos` | Registra un equipo. |
| PUT | `/api/equipos/{id}` | Actualiza los datos de un equipo. |
| PATCH | `/api/equipos/{id}` | Actualiza parcialmente los datos de un equipo. |
| DELETE | `/api/equipos/{id}` | Solicita la eliminación de un equipo, sujeta a las reglas del sistema. |

### 4.4. Préstamos

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/prestamos` | Consulta los préstamos registrados. |
| GET | `/api/prestamos/{id}` | Consulta un préstamo por su identificador. |
| POST | `/api/prestamos` | Registra un préstamo. |
| PATCH | `/api/prestamos/{id}/devolucion` | Registra la devolución de un préstamo. |

### 4.5. Mantenimientos

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/mantenimientos` | Consulta los mantenimientos registrados. |
| GET | `/api/mantenimientos/{id}` | Consulta un mantenimiento por su identificador. |
| POST | `/api/mantenimientos` | Registra un mantenimiento. |
| PUT | `/api/mantenimientos/{id}` | Actualiza los datos de un mantenimiento. |
| PATCH | `/api/mantenimientos/{id}` | Actualiza parcialmente un mantenimiento. |
| DELETE | `/api/mantenimientos/{id}` | Solicita la eliminación de un mantenimiento, según las reglas del sistema. |

### 4.6. Trazabilidad

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/trazabilidad` | Consulta los registros de trazabilidad. |
| GET | `/api/trazabilidad/{id}` | Consulta un registro de trazabilidad por su identificador. |

Los registros de trazabilidad tienen como finalidad conservar el historial de las operaciones relevantes del sistema, como los préstamos y las devoluciones.

## 5. Reglas generales del sistema

- Solo se deben prestar equipos disponibles.
- Un equipo no debe tener más de un préstamo activo al mismo tiempo.
- La fecha de devolución prevista debe ser posterior a la fecha de inicio del préstamo.
- Las devoluciones deben actualizar el estado del préstamo y del equipo de acuerdo con las reglas de negocio.
- Las operaciones relevantes deben conservar su registro de trazabilidad.
- Las restricciones de eliminación deben proteger los registros que necesiten conservarse como historial.

## 6. Pruebas de la API

Los endpoints se verificarán mediante solicitudes HTTP utilizando Bruno. Se comprobarán los métodos, las rutas, los datos enviados, las respuestas y los códigos de estado HTTP.

## 7. Estado de implementación

El catálogo anterior describe los endpoints previstos para el proyecto. La implementación y las pruebas de cada recurso se documentarán conforme avance el desarrollo.
