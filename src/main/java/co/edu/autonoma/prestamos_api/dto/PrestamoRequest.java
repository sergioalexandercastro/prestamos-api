package co.edu.autonoma.prestamos_api.dto;

import java.time.LocalDateTime;

public record PrestamoRequest(
        Integer usuarioId,
        Integer equipoId,
        LocalDateTime fechaLimite
) {
}
