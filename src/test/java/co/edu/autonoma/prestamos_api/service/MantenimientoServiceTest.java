package co.edu.autonoma.prestamos_api.service;

import co.edu.autonoma.prestamos_api.entity.MantenimientoEntity;
import co.edu.autonoma.prestamos_api.enums.EstadoMantenimiento;
import co.edu.autonoma.prestamos_api.repository.EquipoRepository;
import co.edu.autonoma.prestamos_api.repository.MantenimientoRepository;
import co.edu.autonoma.prestamos_api.repository.PrestamoRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import co.edu.autonoma.prestamos_api.dto.MantenimientoRequest;
import co.edu.autonoma.prestamos_api.dto.MantenimientoResponse;
import co.edu.autonoma.prestamos_api.entity.EquipoEntity;
import co.edu.autonoma.prestamos_api.enums.EstadoEquipo;
import co.edu.autonoma.prestamos_api.enums.EstadoPrestamo;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.within;

@ExtendWith(MockitoExtension.class)
class MantenimientoServiceTest {

    @Mock
    private MantenimientoRepository mantenimientoRepository;

    @Mock
    private EquipoRepository equipoRepository;

    @Mock
    private PrestamoRepository prestamoRepository;

    @InjectMocks
    private MantenimientoService service;

    @Test
    void eliminar_mantenimientoProgramado_borraElMantenimiento() {

        // Arrange
        MantenimientoEntity mantenimiento =
                new MantenimientoEntity();

        mantenimiento.setId(3);
        mantenimiento.setEquipoId(2);
        mantenimiento.setMotivo("Mantenimiento preventivo");

        mantenimiento.setFechaInicio(
                LocalDateTime.now().plusDays(2)
        );

        mantenimiento.setEstado(
                EstadoMantenimiento.PROGRAMADO
        );

        when(mantenimientoRepository.findById(3))
                .thenReturn(Optional.of(mantenimiento));

        // Act
        service.eliminar(3);

        // Assert
        verify(mantenimientoRepository)
                .delete(mantenimiento);
    }

    @Test
    void eliminar_mantenimientoInexistente_noBorraNada() {

        // Arrange
        Integer idInexistente = 999;

        when(mantenimientoRepository.findById(idInexistente))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.eliminar(idInexistente)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(excepcion.getReason())
                .isEqualTo(
                        "No existe un mantenimiento con el ID 999"
                );

        verify(mantenimientoRepository, never())
                .delete(any(MantenimientoEntity.class));
    }

    @Test
    void eliminar_mantenimientoEnProceso_noBorraNada() {

        // Arrange
        MantenimientoEntity mantenimiento =
                new MantenimientoEntity();

        mantenimiento.setId(3);
        mantenimiento.setEquipoId(2);

        mantenimiento.setEstado(
                EstadoMantenimiento.EN_PROCESO
        );

        when(mantenimientoRepository.findById(3))
                .thenReturn(Optional.of(mantenimiento));

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.eliminar(3)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(excepcion.getReason())
                .isEqualTo(
                        "No se puede eliminar un mantenimiento que está en proceso. Primero debe finalizarlo"
                );

        verify(mantenimientoRepository, never())
                .delete(any(MantenimientoEntity.class));
    }
    private EquipoEntity crearEquipoParaMantenimiento(
            Integer id,
            EstadoEquipo estado
    ) {
        EquipoEntity equipo = new EquipoEntity();

        equipo.setId(id);
        equipo.setEstado(estado);

        return equipo;
    }

    private MantenimientoEntity crearMantenimientoEnProceso() {

        MantenimientoEntity mantenimiento =
                new MantenimientoEntity();

        mantenimiento.setId(1);
        mantenimiento.setEquipoId(2);
        mantenimiento.setFechaInicio(
                LocalDateTime.now().minusDays(1)
        );
        mantenimiento.setFechaFin(null);
        mantenimiento.setMotivo("Mantenimiento preventivo");
        mantenimiento.setEstado(EstadoMantenimiento.EN_PROCESO);

        return mantenimiento;
    }

    private List<EstadoMantenimiento> estadosMantenimientoPendiente() {

        return List.of(
                EstadoMantenimiento.PROGRAMADO,
                EstadoMantenimiento.EN_PROCESO
        );
    }

// 4. Listar mantenimientos existentes

    @Test
    void listar_conRegistros_devuelveMantenimientos() {

        // Arrange
        MantenimientoEntity mantenimiento =
                crearMantenimientoEnProceso();

        when(mantenimientoRepository.findAll())
                .thenReturn(List.of(mantenimiento));

        // Act
        List<MantenimientoResponse> respuesta =
                service.listar();

        // Assert
        assertThat(respuesta).hasSize(1);

        assertThat(respuesta.get(0).id())
                .isEqualTo(1);

        assertThat(respuesta.get(0).equipoId())
                .isEqualTo(2);

        assertThat(respuesta.get(0).estado())
                .isEqualTo(EstadoMantenimiento.EN_PROCESO);

        verify(mantenimientoRepository).findAll();
    }

// 5. Listar sin mantenimientos

    @Test
    void listar_sinRegistros_devuelveListaVacia() {

        // Arrange
        when(mantenimientoRepository.findAll())
                .thenReturn(List.of());

        // Act
        List<MantenimientoResponse> respuesta =
                service.listar();

        // Assert
        assertThat(respuesta).isNotNull().isEmpty();

        verify(mantenimientoRepository).findAll();
    }

// 6. Buscar mantenimiento existente

    @Test
    void buscarPorId_existente_devuelveMantenimiento() {

        // Arrange
        MantenimientoEntity mantenimiento =
                crearMantenimientoEnProceso();

        when(mantenimientoRepository.findById(1))
                .thenReturn(Optional.of(mantenimiento));

        // Act
        MantenimientoResponse respuesta =
                service.buscarPorId(1);

        // Assert
        assertThat(respuesta.id()).isEqualTo(1);
        assertThat(respuesta.equipoId()).isEqualTo(2);

        assertThat(respuesta.fechaInicio())
                .isEqualTo(mantenimiento.getFechaInicio());

        assertThat(respuesta.fechaFin()).isNull();

        assertThat(respuesta.motivo())
                .isEqualTo("Mantenimiento preventivo");

        assertThat(respuesta.estado())
                .isEqualTo(EstadoMantenimiento.EN_PROCESO);

        verify(mantenimientoRepository).findById(1);
    }

// 7. Buscar mantenimiento inexistente

    @Test
    void buscarPorId_inexistente_lanzaExcepcionNotFound() {

        // Arrange
        when(mantenimientoRepository.findById(999))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.buscarPorId(999)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        verify(mantenimientoRepository).findById(999);
    }
    @Test
    void crear_equipoDisponible_iniciaMantenimiento() {

        // Arrange
        EquipoEntity equipo = crearEquipoParaMantenimiento(
                2, EstadoEquipo.DISPONIBLE
        );

        LocalDateTime fechaInicio =
                LocalDateTime.now().minusMinutes(10);

        MantenimientoRequest request =
                new MantenimientoRequest(
                        2,
                        fechaInicio,
                        " Mantenimiento preventivo "
                );

        when(equipoRepository.findById(2))
                .thenReturn(Optional.of(equipo));

        when(prestamoRepository.existsByEquipoIdAndEstado(
                2, EstadoPrestamo.ACTIVO))
                .thenReturn(false);

        when(mantenimientoRepository.existsByEquipoIdAndEstadoIn(
                2, estadosMantenimientoPendiente()))
                .thenReturn(false);

        when(mantenimientoRepository.save(
                any(MantenimientoEntity.class)))
                .thenAnswer(invocacion -> {

                    MantenimientoEntity mantenimiento =
                            invocacion.getArgument(0);

                    mantenimiento.setId(1);

                    return mantenimiento;
                });

        // Act
        MantenimientoResponse respuesta =
                service.crear(request);

        // Assert
        assertThat(respuesta.id()).isEqualTo(1);
        assertThat(respuesta.equipoId()).isEqualTo(2);

        assertThat(respuesta.fechaInicio())
                .isEqualTo(fechaInicio);

        assertThat(respuesta.fechaFin()).isNull();

        assertThat(respuesta.motivo())
                .isEqualTo("Mantenimiento preventivo");

        assertThat(respuesta.estado())
                .isEqualTo(EstadoMantenimiento.EN_PROCESO);

        assertThat(equipo.getEstado())
                .isEqualTo(EstadoEquipo.EN_MANTENIMIENTO);

        verify(mantenimientoRepository)
                .save(any(MantenimientoEntity.class));

        verify(equipoRepository).save(equipo);
    }
    @Test
    void crear_equipoPrestado_lanzaExcepcionConflict() {

        // Arrange
        EquipoEntity equipo = crearEquipoParaMantenimiento(
                2, EstadoEquipo.PRESTADO
        );

        MantenimientoRequest request =
                new MantenimientoRequest(
                        2,
                        LocalDateTime.now().minusMinutes(10),
                        "Revisión técnica"
                );

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
                        "El equipo no se encuentra disponible para mantenimiento"
                );

        assertThat(equipo.getEstado())
                .isEqualTo(EstadoEquipo.PRESTADO);

        verify(mantenimientoRepository, never())
                .save(any(MantenimientoEntity.class));

        verify(equipoRepository, never())
                .save(any(EquipoEntity.class));
    }
    @Test
    void crear_equipoConPrestamoActivo_lanzaExcepcionConflict() {

        // Arrange
        EquipoEntity equipo = crearEquipoParaMantenimiento(
                2, EstadoEquipo.DISPONIBLE
        );

        MantenimientoRequest request =
                new MantenimientoRequest(
                        2,
                        LocalDateTime.now().minusMinutes(10),
                        "Revisión técnica"
                );

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
                        "No se puede iniciar el mantenimiento porque el equipo tiene un préstamo activo"
                );

        verify(mantenimientoRepository, never())
                .save(any(MantenimientoEntity.class));

        verify(equipoRepository, never())
                .save(any(EquipoEntity.class));
    }
    @Test
    void crear_equipoConMantenimientoPendiente_lanzaExcepcionConflict() {

        // Arrange
        EquipoEntity equipo = crearEquipoParaMantenimiento(
                2, EstadoEquipo.DISPONIBLE
        );

        MantenimientoRequest request =
                new MantenimientoRequest(
                        2,
                        LocalDateTime.now().minusMinutes(10),
                        "Revisión técnica"
                );

        when(equipoRepository.findById(2))
                .thenReturn(Optional.of(equipo));

        when(prestamoRepository.existsByEquipoIdAndEstado(
                2, EstadoPrestamo.ACTIVO))
                .thenReturn(false);

        when(mantenimientoRepository.existsByEquipoIdAndEstadoIn(
                2, estadosMantenimientoPendiente()))
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
                        "El equipo ya tiene un mantenimiento pendiente o en proceso"
                );

        verify(mantenimientoRepository, never())
                .save(any(MantenimientoEntity.class));

        verify(equipoRepository, never())
                .save(any(EquipoEntity.class));
    }
    @Test
    void crear_datosIncompletos_lanzaExcepcionBadRequest() {

        // Arrange
        MantenimientoRequest request =
                new MantenimientoRequest(
                        null,
                        LocalDateTime.now().minusMinutes(10),
                        "Revisión técnica"
                );

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(request)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verifyNoInteractions(
                mantenimientoRepository,
                equipoRepository,
                prestamoRepository
        );
    }
    @Test
    void actualizar_cambioDeEquipo_actualizaAmbosEstados() {

        // Arrange
        MantenimientoEntity mantenimiento =
                crearMantenimientoEnProceso();

        EquipoEntity equipoAnterior =
                crearEquipoParaMantenimiento(
                        2, EstadoEquipo.EN_MANTENIMIENTO
                );

        EquipoEntity equipoNuevo =
                crearEquipoParaMantenimiento(
                        3, EstadoEquipo.DISPONIBLE
                );

        LocalDateTime fechaInicio =
                LocalDateTime.now().minusHours(2);

        MantenimientoRequest request =
                new MantenimientoRequest(
                        3,
                        fechaInicio,
                        " Nuevo mantenimiento "
                );

        when(mantenimientoRepository.findById(1))
                .thenReturn(Optional.of(mantenimiento));

        when(equipoRepository.findById(2))
                .thenReturn(Optional.of(equipoAnterior));

        when(equipoRepository.findById(3))
                .thenReturn(Optional.of(equipoNuevo));

        when(prestamoRepository.existsByEquipoIdAndEstado(
                3, EstadoPrestamo.ACTIVO))
                .thenReturn(false);

        when(mantenimientoRepository.existsByEquipoIdAndEstadoIn(
                3, estadosMantenimientoPendiente()))
                .thenReturn(false);

        when(mantenimientoRepository.save(mantenimiento))
                .thenReturn(mantenimiento);

        // Act
        MantenimientoResponse respuesta =
                service.actualizar(1, request);

        // Assert
        assertThat(respuesta.id()).isEqualTo(1);
        assertThat(respuesta.equipoId()).isEqualTo(3);

        assertThat(respuesta.fechaInicio())
                .isEqualTo(fechaInicio);

        assertThat(respuesta.motivo())
                .isEqualTo("Nuevo mantenimiento");

        assertThat(respuesta.estado())
                .isEqualTo(EstadoMantenimiento.EN_PROCESO);

        assertThat(equipoAnterior.getEstado())
                .isEqualTo(EstadoEquipo.DISPONIBLE);

        assertThat(equipoNuevo.getEstado())
                .isEqualTo(EstadoEquipo.EN_MANTENIMIENTO);

        verify(equipoRepository).save(equipoAnterior);
        verify(equipoRepository).save(equipoNuevo);

        verify(mantenimientoRepository)
                .save(mantenimiento);
    }
    @Test
    void actualizarParcial_soloMotivo_conservaLosDemasCampos() {

        // Arrange
        MantenimientoEntity mantenimiento =
                crearMantenimientoEnProceso();

        LocalDateTime fechaOriginal =
                mantenimiento.getFechaInicio();

        MantenimientoRequest request =
                new MantenimientoRequest(
                        null,
                        null,
                        " Cambio de batería "
                );

        when(mantenimientoRepository.findById(1))
                .thenReturn(Optional.of(mantenimiento));

        when(mantenimientoRepository.save(mantenimiento))
                .thenReturn(mantenimiento);

        // Act
        MantenimientoResponse respuesta =
                service.actualizarParcial(1, request);

        // Assert
        assertThat(respuesta.id()).isEqualTo(1);
        assertThat(respuesta.equipoId()).isEqualTo(2);

        assertThat(respuesta.fechaInicio())
                .isEqualTo(fechaOriginal);

        assertThat(respuesta.fechaFin()).isNull();

        assertThat(respuesta.motivo())
                .isEqualTo("Cambio de batería");

        assertThat(respuesta.estado())
                .isEqualTo(EstadoMantenimiento.EN_PROCESO);

        verify(mantenimientoRepository)
                .save(mantenimiento);

        verifyNoInteractions(
                equipoRepository,
                prestamoRepository
        );
    }
    @Test
    void actualizarParcial_mantenimientoFinalizado_lanzaExcepcionConflict() {

        // Arrange
        MantenimientoEntity mantenimiento =
                crearMantenimientoEnProceso();

        mantenimiento.setEstado(
                EstadoMantenimiento.FINALIZADO
        );

        mantenimiento.setFechaFin(LocalDateTime.now());

        MantenimientoRequest request =
                new MantenimientoRequest(
                        null,
                        null,
                        "Nuevo motivo"
                );

        when(mantenimientoRepository.findById(1))
                .thenReturn(Optional.of(mantenimiento));

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.actualizarParcial(1, request)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(excepcion.getReason())
                .isEqualTo(
                        "Solo se pueden modificar mantenimientos que estén en proceso"
                );

        assertThat(mantenimiento.getMotivo())
                .isEqualTo("Mantenimiento preventivo");

        verify(mantenimientoRepository, never())
                .save(any(MantenimientoEntity.class));

        verifyNoInteractions(
                equipoRepository,
                prestamoRepository
        );
    }
    @Test
    void finalizar_mantenimientoEnProceso_liberaElEquipo() {

        // Arrange
        MantenimientoEntity mantenimiento =
                crearMantenimientoEnProceso();

        EquipoEntity equipo =
                crearEquipoParaMantenimiento(
                        2, EstadoEquipo.EN_MANTENIMIENTO
                );

        when(mantenimientoRepository.findById(1))
                .thenReturn(Optional.of(mantenimiento));

        when(equipoRepository.findById(2))
                .thenReturn(Optional.of(equipo));

        when(mantenimientoRepository.save(mantenimiento))
                .thenReturn(mantenimiento);

        // Act
        MantenimientoResponse respuesta =
                service.finalizar(1);

        // Assert
        assertThat(respuesta.id()).isEqualTo(1);

        assertThat(respuesta.estado())
                .isEqualTo(EstadoMantenimiento.FINALIZADO);

        assertThat(respuesta.fechaFin())
                .isNotNull()
                .isCloseTo(
                        LocalDateTime.now(),
                        within(5, ChronoUnit.SECONDS)
                );

        assertThat(equipo.getEstado())
                .isEqualTo(EstadoEquipo.DISPONIBLE);

        verify(mantenimientoRepository)
                .save(mantenimiento);

        verify(equipoRepository).save(equipo);
    }
    @Test
    void finalizar_mantenimientoYaFinalizado_lanzaExcepcionConflict() {

        // Arrange
        MantenimientoEntity mantenimiento =
                crearMantenimientoEnProceso();

        mantenimiento.setEstado(
                EstadoMantenimiento.FINALIZADO
        );

        LocalDateTime fechaFinOriginal =
                LocalDateTime.now().minusHours(1);

        mantenimiento.setFechaFin(fechaFinOriginal);

        when(mantenimientoRepository.findById(1))
                .thenReturn(Optional.of(mantenimiento));

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.finalizar(1)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(excepcion.getReason())
                .isEqualTo(
                        "Solo se pueden finalizar mantenimientos que estén en proceso"
                );

        assertThat(mantenimiento.getFechaFin())
                .isEqualTo(fechaFinOriginal);

        verify(mantenimientoRepository, never())
                .save(any(MantenimientoEntity.class));

        verifyNoInteractions(equipoRepository);
    }
}
