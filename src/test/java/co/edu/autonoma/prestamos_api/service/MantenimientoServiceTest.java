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
}
