package co.edu.autonoma.prestamos_api.service;

import co.edu.autonoma.prestamos_api.dto.TrazabilidadResponse;
import co.edu.autonoma.prestamos_api.entity.TrazabilidadEntity;
import co.edu.autonoma.prestamos_api.enums.OperacionTrazabilidad;
import co.edu.autonoma.prestamos_api.repository.TrazabilidadRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrazabilidadServiceTest {

    @Mock
    private TrazabilidadRepository trazabilidadRepository;

    @InjectMocks
    private TrazabilidadService service;

    // Método auxiliar

    private TrazabilidadEntity crearTrazabilidadPrueba() {

        TrazabilidadEntity trazabilidad =
                new TrazabilidadEntity();

        trazabilidad.setId(1);
        trazabilidad.setPrestamoId(3);
        trazabilidad.setUsuarioId(2);

        trazabilidad.setOperacion(
                OperacionTrazabilidad.PRESTAMO
        );

        trazabilidad.setFechaHora(
                LocalDateTime.of(2026, 11, 26, 10, 0)
        );

        trazabilidad.setDescripcion(
                "Registro de préstamo de equipo"
        );

        return trazabilidad;
    }

    // 1. Listar registros existentes

    @Test
    void listar_registrosExistentes_devuelveListaDeTrazabilidad() {

        // Arrange
        TrazabilidadEntity prestamo =
                crearTrazabilidadPrueba();

        TrazabilidadEntity devolucion =
                crearTrazabilidadPrueba();

        devolucion.setId(2);

        devolucion.setOperacion(
                OperacionTrazabilidad.DEVOLUCION
        );

        devolucion.setFechaHora(
                prestamo.getFechaHora().plusDays(1)
        );

        devolucion.setDescripcion(
                "Registro de devolución de equipo"
        );

        when(trazabilidadRepository.findAll())
                .thenReturn(List.of(prestamo, devolucion));

        // Act
        List<TrazabilidadResponse> respuesta =
                service.listar();

        // Assert
        assertThat(respuesta).hasSize(2);

        assertThat(respuesta.get(0).id())
                .isEqualTo(1);

        assertThat(respuesta.get(0).operacion())
                .isEqualTo(OperacionTrazabilidad.PRESTAMO);

        assertThat(respuesta.get(1).id())
                .isEqualTo(2);

        assertThat(respuesta.get(1).operacion())
                .isEqualTo(OperacionTrazabilidad.DEVOLUCION);

        assertThat(respuesta.get(1).prestamoId())
                .isEqualTo(3);

        verify(trazabilidadRepository).findAll();
    }

    // 2. Listar sin registros

    @Test
    void listar_sinRegistros_devuelveListaVacia() {

        // Arrange
        when(trazabilidadRepository.findAll())
                .thenReturn(List.of());

        // Act
        List<TrazabilidadResponse> respuesta =
                service.listar();

        // Assert
        assertThat(respuesta)
                .isNotNull()
                .isEmpty();

        verify(trazabilidadRepository).findAll();
    }

    // 3. Buscar registro existente

    @Test
    void buscarPorId_existente_devuelveLaTrazabilidad() {

        // Arrange
        TrazabilidadEntity trazabilidad =
                crearTrazabilidadPrueba();

        when(trazabilidadRepository.findById(1))
                .thenReturn(Optional.of(trazabilidad));

        // Act
        TrazabilidadResponse respuesta =
                service.buscarPorId(1);

        // Assert
        assertThat(respuesta).isNotNull();

        assertThat(respuesta.id())
                .isEqualTo(1);

        assertThat(respuesta.prestamoId())
                .isEqualTo(3);

        assertThat(respuesta.usuarioId())
                .isEqualTo(2);

        assertThat(respuesta.operacion())
                .isEqualTo(OperacionTrazabilidad.PRESTAMO);

        assertThat(respuesta.fechaHora())
                .isEqualTo(
                        LocalDateTime.of(2026, 11, 26, 10, 0)
                );

        assertThat(respuesta.descripcion())
                .isEqualTo("Registro de préstamo de equipo");

        verify(trazabilidadRepository).findById(1);
    }

    // 4. Buscar registro inexistente

    @Test
    void buscarPorId_inexistente_lanzaExcepcionNotFound() {

        // Arrange
        when(trazabilidadRepository.findById(999))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.buscarPorId(999)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(excepcion.getReason())
                .isEqualTo(
                        "No existe un registro de trazabilidad con el ID 999"
                );

        verify(trazabilidadRepository).findById(999);
    }

    // 5. Buscar con identificador nulo

    @Test
    void buscarPorId_idNulo_lanzaExcepcionBadRequest() {

        // Arrange
        Integer id = null;

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.buscarPorId(id)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(excepcion.getReason())
                .isEqualTo(
                        "El ID de trazabilidad debe ser válido"
                );

        verifyNoInteractions(trazabilidadRepository);
    }

    // 6. Buscar con identificadores no positivos

    @Test
    void buscarPorId_idNoPositivo_lanzaExcepcionBadRequest() {

        // Arrange
        Integer idCero = 0;
        Integer idNegativo = -1;

        // Act
        ResponseStatusException excepcionCero = assertThrows(
                ResponseStatusException.class,
                () -> service.buscarPorId(idCero)
        );

        ResponseStatusException excepcionNegativo = assertThrows(
                ResponseStatusException.class,
                () -> service.buscarPorId(idNegativo)
        );

        // Assert
        assertThat(excepcionCero.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(excepcionNegativo.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(excepcionCero.getReason())
                .isEqualTo(
                        "El ID de trazabilidad debe ser válido"
                );

        assertThat(excepcionNegativo.getReason())
                .isEqualTo(
                        "El ID de trazabilidad debe ser válido"
                );

        verifyNoInteractions(trazabilidadRepository);
    }
}
