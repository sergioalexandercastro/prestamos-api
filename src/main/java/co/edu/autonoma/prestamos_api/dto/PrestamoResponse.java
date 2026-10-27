package co.edu.autonoma.prestamos_api.dto;

import co.edu.autonoma.prestamos_api.enums.EstadoPrestamo;
import java.time.LocalDateTime;

public record PrestamoResponse(
        Integer id,
        Integer usuarioId,
        Integer equipoId,
        LocalDateTime fechaPrestamo,
        LocalDateTime fechaLimite,
        LocalDateTime fechaDevolucion,
        EstadoPrestamo estado
) {
}
