package co.edu.autonoma.prestamos_api.dto;

import co.edu.autonoma.prestamos_api.enums.EstadoMantenimiento;
import java.time.LocalDateTime;

public record MantenimientoResponse(
        Integer id,
        Integer equipoId,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        String motivo,
        EstadoMantenimiento estado
) {
}
