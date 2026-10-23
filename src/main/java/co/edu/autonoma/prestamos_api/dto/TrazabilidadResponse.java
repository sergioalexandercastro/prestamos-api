package co.edu.autonoma.prestamos_api.dto;

import co.edu.autonoma.prestamos_api.enums.OperacionTrazabilidad;
import java.time.LocalDateTime;

public record TrazabilidadResponse(
        Integer id,
        Integer prestamoId,
        Integer usuarioId,
        OperacionTrazabilidad operacion,
        LocalDateTime fechaHora,
        String descripcion
) {
}
