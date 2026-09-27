package co.edu.autonoma.prestamos_api.service;

import co.edu.autonoma.prestamos_api.dto.EstadoResponse;
import org.springframework.stereotype.Service;

@Service
public class EstadoService {
    public EstadoResponse consultarEstado() {
        return new EstadoResponse(
                "prestamos-api",
                "disponible"
        );
    }
}
