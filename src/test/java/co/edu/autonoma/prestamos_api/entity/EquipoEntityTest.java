package co.edu.autonoma.prestamos_api.entity;

import co.edu.autonoma.prestamos_api.enums.EstadoEquipo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EquipoEntityTest {

    @Test
    void setEstado_conEquipoDisponible_cambiaEstadoAPrestado() {

        // Arrange
        EquipoEntity equipo = new EquipoEntity();
        equipo.setId(1);
        equipo.setCodigoInventario("EQ-001");
        equipo.setNombre("Computador portátil");
        equipo.setTipo("Portátil");
        equipo.setEstado(EstadoEquipo.DISPONIBLE);

        // Act
        equipo.setEstado(EstadoEquipo.PRESTADO);

        // Assert
        assertThat(equipo.getEstado())
                .isEqualTo(EstadoEquipo.PRESTADO);

        assertThat(equipo.getCodigoInventario())
                .isEqualTo("EQ-001");
    }
}
