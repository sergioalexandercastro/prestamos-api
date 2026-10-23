package co.edu.autonoma.prestamos_api.dto;

import co.edu.autonoma.prestamos_api.enums.EstadoEquipo;

public record EquipoRequest(
        String codigoInventario,
        String nombre,
        String tipo,
        String marca,
        String modelo,
        EstadoEquipo estado
) {
}
