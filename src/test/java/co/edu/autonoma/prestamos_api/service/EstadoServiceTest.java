package co.edu.autonoma.prestamos_api.service;

import co.edu.autonoma.prestamos_api.dto.EstadoResponse;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EstadoServiceTest {

    @Test
    void consultarEstado_sinDependencias_devuelveServicioDisponible() {

        // Arrange
        EstadoService service = new EstadoService();

        // Act
        EstadoResponse respuesta =
                service.consultarEstado();

        // Assert
        assertThat(respuesta).isNotNull();

        assertThat(respuesta.servicio())
                .isEqualTo("prestamos-api");

        assertThat(respuesta.estado())
                .isEqualTo("disponible");
    }
}
