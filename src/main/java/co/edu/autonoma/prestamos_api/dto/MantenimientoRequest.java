package co.edu.autonoma.prestamos_api.dto;

import java.time.LocalDateTime;

public record MantenimientoRequest(
        Integer equipoId,
        LocalDateTime fechaInicio,
        String motivo
) {
}
