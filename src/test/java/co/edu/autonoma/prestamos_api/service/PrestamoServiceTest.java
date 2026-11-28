package co.edu.autonoma.prestamos_api.service;

import co.edu.autonoma.prestamos_api.dto.PrestamoResponse;
import co.edu.autonoma.prestamos_api.entity.PrestamoEntity;
import co.edu.autonoma.prestamos_api.enums.EstadoPrestamo;
import co.edu.autonoma.prestamos_api.repository.EquipoRepository;
import co.edu.autonoma.prestamos_api.repository.PrestamoRepository;
import co.edu.autonoma.prestamos_api.repository.TrazabilidadRepository;
import co.edu.autonoma.prestamos_api.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import co.edu.autonoma.prestamos_api.dto.PrestamoRequest;
import co.edu.autonoma.prestamos_api.entity.EquipoEntity;
import co.edu.autonoma.prestamos_api.entity.UsuarioEntity;
import co.edu.autonoma.prestamos_api.entity.TrazabilidadEntity;
import co.edu.autonoma.prestamos_api.enums.EstadoEquipo;
import co.edu.autonoma.prestamos_api.enums.OperacionTrazabilidad;

import java.time.temporal.ChronoUnit;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class PrestamoServiceTest {

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EquipoRepository equipoRepository;

    @Mock
    private TrazabilidadRepository trazabilidadRepository;

    @InjectMocks
    private PrestamoService service;

    @Test
    void buscarPorId_existente_devuelveElPrestamo() {

        // Arrange
        LocalDateTime fechaPrestamo =
                LocalDateTime.of(2026, 11, 26, 10, 0);

        LocalDateTime fechaLimite =
                fechaPrestamo.plusDays(3);

        PrestamoEntity prestamo = new PrestamoEntity();

        prestamo.setId(3);
        prestamo.setUsuarioId(1);
        prestamo.setEquipoId(2);
        prestamo.setFechaPrestamo(fechaPrestamo);
        prestamo.setFechaLimite(fechaLimite);
        prestamo.setFechaDevolucion(null);
        prestamo.setEstado(EstadoPrestamo.ACTIVO);

        when(prestamoRepository.findById(3))
                .thenReturn(Optional.of(prestamo));

        // Act
        PrestamoResponse respuesta = service.buscarPorId(3);

        // Assert
        assertThat(respuesta).isNotNull();
        assertThat(respuesta.id()).isEqualTo(3);
        assertThat(respuesta.usuarioId()).isEqualTo(1);
        assertThat(respuesta.equipoId()).isEqualTo(2);
        assertThat(respuesta.fechaPrestamo())
                .isEqualTo(fechaPrestamo);
        assertThat(respuesta.fechaLimite())
                .isEqualTo(fechaLimite);
        assertThat(respuesta.fechaDevolucion()).isNull();
        assertThat(respuesta.estado())
                .isEqualTo(EstadoPrestamo.ACTIVO);
    }


    @Test
    void buscarPorId_inexistente_lanzaExcepcionNotFound() {

        // Arrange
        Integer idInexistente = 999;

        when(prestamoRepository.findById(idInexistente))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.buscarPorId(idInexistente)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(excepcion.getReason())
                .isEqualTo(
                        "No se encontró el préstamo con ID 999"
                );
    }

    @Test
    void crear_prestamoNuevo_quedaActivoYConFechaActual() {

        // Arrange
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(1);

        EquipoEntity equipo = new EquipoEntity();
        equipo.setId(2);
        equipo.setEstado(EstadoEquipo.DISPONIBLE);

        LocalDateTime fechaLimite =
                LocalDateTime.now().plusDays(3);

        PrestamoRequest request =
                new PrestamoRequest(1, 2, fechaLimite);

        when(usuarioRepository.findById(1))
                .thenReturn(Optional.of(usuario));

        when(equipoRepository.findById(2))
                .thenReturn(Optional.of(equipo));

        when(prestamoRepository.existsByEquipoIdAndEstado(
                2, EstadoPrestamo.ACTIVO))
                .thenReturn(false);

        when(prestamoRepository.save(any(PrestamoEntity.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        // Act
        PrestamoResponse respuesta = service.crear(request);

        // Assert
        assertThat(respuesta).isNotNull();
        assertThat(respuesta.usuarioId()).isEqualTo(1);
        assertThat(respuesta.equipoId()).isEqualTo(2);

        assertThat(respuesta.estado())
                .isEqualTo(EstadoPrestamo.ACTIVO);

        assertThat(respuesta.fechaPrestamo())
                .isCloseTo(
                        LocalDateTime.now(),
                        within(5, ChronoUnit.SECONDS)
                );

        assertThat(respuesta.fechaLimite())
                .isEqualTo(fechaLimite);

        assertThat(respuesta.fechaDevolucion()).isNull();

        assertThat(equipo.getEstado())
                .isEqualTo(EstadoEquipo.PRESTADO);

        verify(prestamoRepository)
                .save(any(PrestamoEntity.class));

        verify(equipoRepository).save(equipo);

        verify(trazabilidadRepository)
                .save(argThat(trazabilidad ->
                        trazabilidad.getOperacion()
                                == OperacionTrazabilidad.PRESTAMO
                                && Integer.valueOf(1)
                                .equals(trazabilidad.getUsuarioId())
                ));
    }

    @Test
    void crear_equipoPrestado_lanzaExcepcionConflict() {

        // Arrange
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(1);

        EquipoEntity equipo = new EquipoEntity();
        equipo.setId(2);
        equipo.setEstado(EstadoEquipo.PRESTADO);

        PrestamoRequest request = new PrestamoRequest(
                1, 2, LocalDateTime.now().plusDays(3)
        );

        when(usuarioRepository.findById(1))
                .thenReturn(Optional.of(usuario));

        when(equipoRepository.findById(2))
                .thenReturn(Optional.of(equipo));

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(request)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(excepcion.getReason())
                .isEqualTo(
                        "El equipo no está disponible para préstamo"
                );

        assertThat(equipo.getEstado())
                .isEqualTo(EstadoEquipo.PRESTADO);

        verify(prestamoRepository, never())
                .save(any(PrestamoEntity.class));

        verify(equipoRepository, never())
                .save(any(EquipoEntity.class));

        verify(trazabilidadRepository, never())
                .save(any(TrazabilidadEntity.class));
    }

    @Test
    void crear_equipoEnMantenimiento_lanzaExcepcionConflict() {

        // Arrange
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(1);

        EquipoEntity equipo = new EquipoEntity();
        equipo.setId(2);
        equipo.setEstado(EstadoEquipo.EN_MANTENIMIENTO);

        PrestamoRequest request = new PrestamoRequest(
                1, 2, LocalDateTime.now().plusDays(3)
        );

        when(usuarioRepository.findById(1))
                .thenReturn(Optional.of(usuario));

        when(equipoRepository.findById(2))
                .thenReturn(Optional.of(equipo));

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(request)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(excepcion.getReason())
                .isEqualTo(
                        "El equipo no está disponible para préstamo"
                );

        assertThat(equipo.getEstado())
                .isEqualTo(EstadoEquipo.EN_MANTENIMIENTO);

        verify(prestamoRepository, never())
                .save(any(PrestamoEntity.class));

        verify(equipoRepository, never())
                .save(any(EquipoEntity.class));

        verify(trazabilidadRepository, never())
                .save(any(TrazabilidadEntity.class));
    }

    @Test
    void crear_equipoConPrestamoActivo_lanzaExcepcionConflict() {

        // Arrange
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(1);

        EquipoEntity equipo = new EquipoEntity();
        equipo.setId(2);
        equipo.setEstado(EstadoEquipo.DISPONIBLE);

        PrestamoRequest request = new PrestamoRequest(
                1, 2, LocalDateTime.now().plusDays(3)
        );

        when(usuarioRepository.findById(1))
                .thenReturn(Optional.of(usuario));

        when(equipoRepository.findById(2))
                .thenReturn(Optional.of(equipo));

        when(prestamoRepository.existsByEquipoIdAndEstado(
                2, EstadoPrestamo.ACTIVO))
                .thenReturn(true);

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(request)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(excepcion.getReason())
                .isEqualTo(
                        "El equipo ya tiene un préstamo activo"
                );

        verify(prestamoRepository, never())
                .save(any(PrestamoEntity.class));

        verify(equipoRepository, never())
                .save(any(EquipoEntity.class));

        verify(trazabilidadRepository, never())
                .save(any(TrazabilidadEntity.class));
    }

    @Test
    void crear_fechaLimiteAnterior_lanzaExcepcionBadRequest() {

        // Arrange
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(1);

        EquipoEntity equipo = new EquipoEntity();
        equipo.setId(2);
        equipo.setEstado(EstadoEquipo.DISPONIBLE);

        LocalDateTime fechaIncorrecta =
                LocalDateTime.now().minusDays(1);

        PrestamoRequest request =
                new PrestamoRequest(1, 2, fechaIncorrecta);

        when(usuarioRepository.findById(1))
                .thenReturn(Optional.of(usuario));

        when(equipoRepository.findById(2))
                .thenReturn(Optional.of(equipo));

        when(prestamoRepository.existsByEquipoIdAndEstado(
                2, EstadoPrestamo.ACTIVO))
                .thenReturn(false);

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(request)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(excepcion.getReason())
                .isEqualTo(
                        "La fecha límite debe ser posterior a la fecha del préstamo"
                );

        verify(prestamoRepository, never())
                .save(any(PrestamoEntity.class));

        verify(equipoRepository, never())
                .save(any(EquipoEntity.class));

        verify(trazabilidadRepository, never())
                .save(any(TrazabilidadEntity.class));
    }

    @Test
    void devolver_prestamoActivo_actualizaEstadoYLiberaEquipo() {

        // Arrange
        PrestamoEntity prestamo = new PrestamoEntity();
        prestamo.setId(3);
        prestamo.setUsuarioId(1);
        prestamo.setEquipoId(2);
        prestamo.setFechaPrestamo(
                LocalDateTime.now().minusDays(1)
        );
        prestamo.setFechaLimite(
                LocalDateTime.now().plusDays(2)
        );
        prestamo.setEstado(EstadoPrestamo.ACTIVO);

        EquipoEntity equipo = new EquipoEntity();
        equipo.setId(2);
        equipo.setEstado(EstadoEquipo.PRESTADO);

        when(prestamoRepository.findById(3))
                .thenReturn(Optional.of(prestamo));

        when(equipoRepository.findById(2))
                .thenReturn(Optional.of(equipo));

        when(prestamoRepository.save(any(PrestamoEntity.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        // Act
        PrestamoResponse respuesta = service.devolver(3);

        // Assert
        assertThat(respuesta.id()).isEqualTo(3);

        assertThat(respuesta.estado())
                .isEqualTo(EstadoPrestamo.DEVUELTO);

        assertThat(respuesta.fechaDevolucion())
                .isNotNull()
                .isCloseTo(
                        LocalDateTime.now(),
                        within(5, ChronoUnit.SECONDS)
                );

        assertThat(equipo.getEstado())
                .isEqualTo(EstadoEquipo.DISPONIBLE);

        verify(prestamoRepository).save(prestamo);

        verify(equipoRepository).save(equipo);

        verify(trazabilidadRepository)
                .save(argThat(trazabilidad ->
                        trazabilidad.getOperacion()
                                == OperacionTrazabilidad.DEVOLUCION
                                && Integer.valueOf(3)
                                .equals(trazabilidad.getPrestamoId())
                                && Integer.valueOf(1)
                                .equals(trazabilidad.getUsuarioId())
                ));
    }

    @Test
    void devolver_prestamoYaDevuelto_noRealizaCambios() {

        // Arrange
        PrestamoEntity prestamo = new PrestamoEntity();
        prestamo.setId(3);
        prestamo.setUsuarioId(1);
        prestamo.setEquipoId(2);
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);

        LocalDateTime fechaDevolucionOriginal =
                LocalDateTime.now().minusDays(1);

        prestamo.setFechaDevolucion(fechaDevolucionOriginal);

        when(prestamoRepository.findById(3))
                .thenReturn(Optional.of(prestamo));

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.devolver(3)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(excepcion.getReason())
                .isEqualTo("El préstamo ya fue devuelto");

        assertThat(prestamo.getEstado())
                .isEqualTo(EstadoPrestamo.DEVUELTO);

        assertThat(prestamo.getFechaDevolucion())
                .isEqualTo(fechaDevolucionOriginal);

        verify(prestamoRepository, never())
                .save(any(PrestamoEntity.class));

        verify(equipoRepository, never())
                .save(any(EquipoEntity.class));

        verify(trazabilidadRepository, never())
                .save(any(TrazabilidadEntity.class));
    }
    private PrestamoEntity crearPrestamoActivoPrueba() {

        PrestamoEntity prestamo = new PrestamoEntity();

        prestamo.setId(3);
        prestamo.setUsuarioId(1);
        prestamo.setEquipoId(2);

        prestamo.setFechaPrestamo(
                LocalDateTime.now().minusDays(1)
        );

        prestamo.setFechaLimite(
                LocalDateTime.now().plusDays(2)
        );

        prestamo.setFechaDevolucion(null);
        prestamo.setEstado(EstadoPrestamo.ACTIVO);

        return prestamo;
    }
    @Test
    void listar_conPrestamos_devuelveListaDePrestamos() {

        // Arrange
        PrestamoEntity activo =
                crearPrestamoActivoPrueba();

        PrestamoEntity devuelto =
                crearPrestamoActivoPrueba();

        devuelto.setId(4);
        devuelto.setEquipoId(5);
        devuelto.setEstado(EstadoPrestamo.DEVUELTO);
        devuelto.setFechaDevolucion(LocalDateTime.now());

        when(prestamoRepository.findAll())
                .thenReturn(List.of(activo, devuelto));

        // Act
        List<PrestamoResponse> respuesta =
                service.listar();

        // Assert
        assertThat(respuesta).hasSize(2);

        assertThat(respuesta.get(0).id())
                .isEqualTo(3);

        assertThat(respuesta.get(0).usuarioId())
                .isEqualTo(1);

        assertThat(respuesta.get(0).equipoId())
                .isEqualTo(2);

        assertThat(respuesta.get(0).estado())
                .isEqualTo(EstadoPrestamo.ACTIVO);

        assertThat(respuesta.get(0).fechaDevolucion())
                .isNull();

        assertThat(respuesta.get(1).id())
                .isEqualTo(4);

        assertThat(respuesta.get(1).equipoId())
                .isEqualTo(5);

        assertThat(respuesta.get(1).estado())
                .isEqualTo(EstadoPrestamo.DEVUELTO);

        assertThat(respuesta.get(1).fechaDevolucion())
                .isEqualTo(devuelto.getFechaDevolucion());

        verify(prestamoRepository).findAll();
    }
    @Test
    void listar_sinPrestamos_devuelveListaVacia() {

        // Arrange
        when(prestamoRepository.findAll())
                .thenReturn(List.of());

        // Act
        List<PrestamoResponse> respuesta =
                service.listar();

        // Assert
        assertThat(respuesta)
                .isNotNull()
                .isEmpty();

        verify(prestamoRepository).findAll();
    }
    @Test
    void crear_usuarioInexistente_lanzaExcepcionNotFound() {

        // Arrange
        PrestamoRequest request = new PrestamoRequest(
                999,
                2,
                LocalDateTime.now().plusDays(3)
        );

        when(usuarioRepository.findById(999))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(request)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(excepcion.getReason())
                .isEqualTo(
                        "No existe un usuario con el ID 999"
                );

        verify(usuarioRepository).findById(999);

        verifyNoInteractions(
                equipoRepository,
                prestamoRepository,
                trazabilidadRepository
        );
    }
    @Test
    void crear_equipoInexistente_lanzaExcepcionNotFound() {

        // Arrange
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(1);

        PrestamoRequest request = new PrestamoRequest(
                1,
                999,
                LocalDateTime.now().plusDays(3)
        );

        when(usuarioRepository.findById(1))
                .thenReturn(Optional.of(usuario));

        when(equipoRepository.findById(999))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(request)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(excepcion.getReason())
                .isEqualTo(
                        "No existe un equipo con el ID 999"
                );

        verify(usuarioRepository).findById(1);
        verify(equipoRepository).findById(999);

        verify(equipoRepository, never())
                .save(any(EquipoEntity.class));

        verifyNoInteractions(
                prestamoRepository,
                trazabilidadRepository
        );
    }
    @Test
    void crear_datosObligatoriosIncompletos_lanzaExcepcionBadRequest() {

        // Arrange
        LocalDateTime fechaLimite =
                LocalDateTime.now().plusDays(3);

        PrestamoRequest[] solicitudesInvalidas = {
                null,
                new PrestamoRequest(null, 2, fechaLimite),
                new PrestamoRequest(1, null, fechaLimite),
                new PrestamoRequest(1, 2, null)
        };

        // Act y Assert
        for (PrestamoRequest request : solicitudesInvalidas) {

            ResponseStatusException excepcion = assertThrows(
                    ResponseStatusException.class,
                    () -> service.crear(request)
            );

            assertThat(excepcion.getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);

            assertThat(excepcion.getReason())
                    .isEqualTo(
                            "Todos los campos son obligatorios"
                    );
        }

        verifyNoInteractions(
                usuarioRepository,
                equipoRepository,
                prestamoRepository,
                trazabilidadRepository
        );
    }
    @Test
    void crear_identificadoresInvalidos_lanzaExcepcionBadRequest() {

        // Arrange
        LocalDateTime fechaLimite =
                LocalDateTime.now().plusDays(3);

        List<PrestamoRequest> solicitudesInvalidas = List.of(
                new PrestamoRequest(0, 2, fechaLimite),
                new PrestamoRequest(-1, 2, fechaLimite),
                new PrestamoRequest(1, 0, fechaLimite),
                new PrestamoRequest(1, -1, fechaLimite)
        );

        // Act y Assert
        for (PrestamoRequest request : solicitudesInvalidas) {

            ResponseStatusException excepcion = assertThrows(
                    ResponseStatusException.class,
                    () -> service.crear(request)
            );

            assertThat(excepcion.getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);

            assertThat(excepcion.getReason())
                    .isEqualTo(
                            "Los IDs de usuario y equipo deben ser válidos"
                    );
        }

        verifyNoInteractions(
                usuarioRepository,
                equipoRepository,
                prestamoRepository,
                trazabilidadRepository
        );
    }
    @Test
    void devolver_prestamoInexistente_lanzaExcepcionNotFound() {

        // Arrange
        when(prestamoRepository.findById(999))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.devolver(999)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(excepcion.getReason())
                .isEqualTo(
                        "No se encontró el préstamo con ID 999"
                );

        verify(prestamoRepository).findById(999);

        verify(prestamoRepository, never())
                .save(any(PrestamoEntity.class));

        verifyNoInteractions(
                equipoRepository,
                usuarioRepository,
                trazabilidadRepository
        );
    }
    @Test
    void devolver_equipoAsociadoInexistente_noRealizaCambios() {

        // Arrange
        PrestamoEntity prestamo =
                crearPrestamoActivoPrueba();

        when(prestamoRepository.findById(3))
                .thenReturn(Optional.of(prestamo));

        when(equipoRepository.findById(2))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.devolver(3)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(excepcion.getReason())
                .isEqualTo(
                        "No existe el equipo asociado al préstamo"
                );

        assertThat(prestamo.getEstado())
                .isEqualTo(EstadoPrestamo.ACTIVO);

        assertThat(prestamo.getFechaDevolucion())
                .isNull();

        verify(prestamoRepository).findById(3);
        verify(equipoRepository).findById(2);

        verify(prestamoRepository, never())
                .save(any(PrestamoEntity.class));

        verify(equipoRepository, never())
                .save(any(EquipoEntity.class));

        verifyNoInteractions(
                usuarioRepository,
                trazabilidadRepository
        );
    }
}
