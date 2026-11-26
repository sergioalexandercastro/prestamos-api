package co.edu.autonoma.prestamos_api.service;

import co.edu.autonoma.prestamos_api.dto.TrazabilidadResponse;
import co.edu.autonoma.prestamos_api.entity.TrazabilidadEntity;
import co.edu.autonoma.prestamos_api.repository.TrazabilidadRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TrazabilidadService {

    private final TrazabilidadRepository trazabilidadRepository;

    public TrazabilidadService(
            TrazabilidadRepository trazabilidadRepository
    ) {
        this.trazabilidadRepository = trazabilidadRepository;
    }

    public List<TrazabilidadResponse> listar() {
        return trazabilidadRepository.findAll()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    public TrazabilidadResponse buscarPorId(Integer id) {
        TrazabilidadEntity trazabilidad = obtenerTrazabilidad(id);

        return convertirRespuesta(trazabilidad);
    }

    private TrazabilidadEntity obtenerTrazabilidad(Integer id) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID de trazabilidad debe ser válido"
            );
        }

        return trazabilidadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un registro de trazabilidad con el ID " + id
                ));
    }

    private TrazabilidadResponse convertirRespuesta(
            TrazabilidadEntity trazabilidad
    ) {
        return new TrazabilidadResponse(
                trazabilidad.getId(),
                trazabilidad.getPrestamoId(),
                trazabilidad.getUsuarioId(),
                trazabilidad.getOperacion(),
                trazabilidad.getFechaHora(),
                trazabilidad.getDescripcion()
        );
    }
}