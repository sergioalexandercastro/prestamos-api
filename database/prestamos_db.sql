-- ============================================
-- BASE DE DATOS: prestamos_db
-- PROYECTO: Préstamo de equipos tecnológicos
-- ============================================


-- ============================================
-- 1. TABLA USUARIOS
-- ============================================

CREATE TABLE usuarios (
                          id BIGSERIAL PRIMARY KEY,
                          nombre VARCHAR(100) NOT NULL,
                          documento VARCHAR(30) NOT NULL UNIQUE,
                          correo VARCHAR(150) NOT NULL UNIQUE,
                          tipo_usuario VARCHAR(20) NOT NULL,

                          CONSTRAINT chk_usuario_tipo
                              CHECK (tipo_usuario IN ('ESTUDIANTE', 'DOCENTE', 'ENCARGADO'))
);


-- ============================================
-- 2. TABLA EQUIPOS
-- ============================================

CREATE TABLE equipos (
                         id BIGSERIAL PRIMARY KEY,
                         codigo_inventario VARCHAR(50) NOT NULL UNIQUE,
                         nombre VARCHAR(100) NOT NULL,
                         tipo VARCHAR(50) NOT NULL,
                         marca VARCHAR(50),
                         modelo VARCHAR(50),
                         estado VARCHAR(30) NOT NULL,

                         CONSTRAINT chk_equipo_estado
                             CHECK (estado IN ('DISPONIBLE', 'PRESTADO', 'EN_MANTENIMIENTO'))
);


-- ============================================
-- 3. TABLA PRESTAMOS
-- ============================================

CREATE TABLE prestamos (
                           id BIGSERIAL PRIMARY KEY,

                           usuario_id BIGINT NOT NULL,
                           equipo_id BIGINT NOT NULL,

                           fecha_prestamo TIMESTAMP NOT NULL,
                           fecha_limite TIMESTAMP NOT NULL,
                           fecha_devolucion TIMESTAMP,

                           estado VARCHAR(30) NOT NULL,

                           CONSTRAINT fk_prestamo_usuario
                               FOREIGN KEY (usuario_id)
                                   REFERENCES usuarios(id),

                           CONSTRAINT fk_prestamo_equipo
                               FOREIGN KEY (equipo_id)
                                   REFERENCES equipos(id),

                           CONSTRAINT chk_prestamo_estado
                               CHECK (estado IN ('ACTIVO', 'DEVUELTO')),

                           CONSTRAINT chk_fecha_limite
                               CHECK (fecha_limite > fecha_prestamo),

                           CONSTRAINT chk_fecha_devolucion
                               CHECK (
                                   fecha_devolucion IS NULL
                                       OR fecha_devolucion >= fecha_prestamo
                                   )
);


-- ============================================
-- REGLA: UN EQUIPO SOLO PUEDE TENER
-- UN PRESTAMO ACTIVO A LA VEZ
-- ============================================

CREATE UNIQUE INDEX uq_prestamo_equipo_activo
    ON prestamos (equipo_id)
    WHERE estado = 'ACTIVO';


-- ============================================
-- 4. TABLA MANTENIMIENTOS
-- ============================================

CREATE TABLE mantenimientos (
                                id BIGSERIAL PRIMARY KEY,

                                equipo_id BIGINT NOT NULL,

                                fecha_inicio TIMESTAMP NOT NULL,
                                fecha_fin TIMESTAMP,

                                motivo VARCHAR(255) NOT NULL,
                                estado VARCHAR(30) NOT NULL,

                                CONSTRAINT fk_mantenimiento_equipo
                                    FOREIGN KEY (equipo_id)
                                        REFERENCES equipos(id),

                                CONSTRAINT chk_mantenimiento_estado
                                    CHECK (
                                        estado IN ('PROGRAMADO', 'EN_PROCESO', 'FINALIZADO')
                                        ),

                                CONSTRAINT chk_fecha_mantenimiento
                                    CHECK (
                                        fecha_fin IS NULL
                                            OR fecha_fin >= fecha_inicio
                                        )
);


-- ============================================
-- 5. TABLA TRAZABILIDAD
-- ============================================

CREATE TABLE trazabilidad (
                              id BIGSERIAL PRIMARY KEY,

                              prestamo_id BIGINT NOT NULL,
                              usuario_id BIGINT NOT NULL,

                              operacion VARCHAR(30) NOT NULL,
                              fecha_hora TIMESTAMP NOT NULL,
                              descripcion VARCHAR(255),

                              CONSTRAINT fk_trazabilidad_prestamo
                                  FOREIGN KEY (prestamo_id)
                                      REFERENCES prestamos(id),

                              CONSTRAINT fk_trazabilidad_usuario
                                  FOREIGN KEY (usuario_id)
                                      REFERENCES usuarios(id),

                              CONSTRAINT chk_trazabilidad_operacion
                                  CHECK (
                                      operacion IN ('PRESTAMO', 'DEVOLUCION')
                                      )
);