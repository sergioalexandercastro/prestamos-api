
package co.edu.autonoma.prestamos_api.service;

import co.edu.autonoma.prestamos_api.dto.EquipoRequest;
import co.edu.autonoma.prestamos_api.dto.EquipoResponse;
import co.edu.autonoma.prestamos_api.entity.EquipoEntity;
import co.edu.autonoma.prestamos_api.enums.EstadoEquipo;
import co.edu.autonoma.prestamos_api.repository.EquipoRepository;
import co.edu.autonoma.prestamos_api.repository.MantenimientoRepository;
import co.edu.autonoma.prestamos_api.repository.PrestamoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final PrestamoRepository prestamoRepository;
    private final MantenimientoRepository mantenimientoRepository;

    public EquipoService(
            EquipoRepository equipoRepository,
            PrestamoRepository prestamoRepository,
            MantenimientoRepository mantenimientoRepository) {
        this.equipoRepository = equipoRepository;
        this.prestamoRepository = prestamoRepository;
        this.mantenimientoRepository = mantenimientoRepository;
    }

    public List<EquipoResponse> listar(EstadoEquipo estado) {
        List<EquipoEntity> equipos;

        if (estado != null) {
            equipos = equipoRepository.findByEstado(estado);
        } else {
            equipos = equipoRepository.findAll();
        }

        return equipos.stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    public EquipoResponse buscarPorId(Integer id) {
        EquipoEntity equipo = obtenerEquipo(id);
        return convertirRespuesta(equipo);
    }

    public EquipoResponse crear(EquipoRequest request) {
        validarDatos(request);

        String codigoInventario = request.codigoInventario().trim();

        if (equipoRepository.existsByCodigoInventario(codigoInventario)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un equipo con ese código de inventario"
            );
        }

        EquipoEntity equipo = new EquipoEntity();
        asignarDatos(equipo, request);

        return convertirRespuesta(equipoRepository.save(equipo));
    }

    public EquipoResponse actualizar(Integer id, EquipoRequest request) {
        EquipoEntity equipo = obtenerEquipo(id);
        validarDatos(request);

        String codigoInventario = request.codigoInventario().trim();

        if (equipoRepository.existsByCodigoInventarioAndIdNot(
                codigoInventario, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El código de inventario ya pertenece a otro equipo"
            );
        }

        asignarDatos(equipo, request);

        return convertirRespuesta(equipoRepository.save(equipo));
    }

    public EquipoResponse actualizarParcial(
            Integer id,
            EquipoRequest request) {

        EquipoEntity equipo = obtenerEquipo(id);

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe proporcionar al menos un dato para actualizar"
            );
        }

        if (request.codigoInventario() != null) {
            if (estaVacio(request.codigoInventario())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El código de inventario no puede estar vacío"
                );
            }

            String codigoInventario = request.codigoInventario().trim();

            if (equipoRepository.existsByCodigoInventarioAndIdNot(
                    codigoInventario, id)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Ya existe otro equipo con ese código de inventario"
                );
            }

            equipo.setCodigoInventario(codigoInventario);
        }

        if (request.nombre() != null) {
            if (estaVacio(request.nombre())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El nombre no puede estar vacío"
                );
            }

            equipo.setNombre(request.nombre().trim());
        }

        if (request.tipo() != null) {
            if (estaVacio(request.tipo())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El tipo no puede estar vacío"
                );
            }

            equipo.setTipo(request.tipo().trim());
        }

        if (request.marca() != null) {
            equipo.setMarca(limpiarOpcional(request.marca()));
        }

        if (request.modelo() != null) {
            equipo.setModelo(limpiarOpcional(request.modelo()));
        }

        if (request.estado() != null) {
            equipo.setEstado(request.estado());
        }

        return convertirRespuesta(equipoRepository.save(equipo));
    }

    public void eliminar(Integer id) {
        EquipoEntity equipo = obtenerEquipo(id);

        if (prestamoRepository.existsByEquipoId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el equipo porque tiene préstamos asociados"
            );
        }

        if (mantenimientoRepository.existsByEquipoId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el equipo porque tiene mantenimientos asociados"
            );
        }

        equipoRepository.delete(equipo);
    }

    private EquipoEntity obtenerEquipo(Integer id) {
        return equipoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el equipo con ID " + id
                ));
    }

    private void validarDatos(EquipoRequest request) {
        if (request == null
                || estaVacio(request.codigoInventario())
                || estaVacio(request.nombre())
                || estaVacio(request.tipo())
                || request.estado() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Todos los campos obligatorios deben estar completos"
            );
        }
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private String limpiarOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }

    private void asignarDatos(
            EquipoEntity equipo,
            EquipoRequest request) {

        equipo.setCodigoInventario(request.codigoInventario().trim());
        equipo.setNombre(request.nombre().trim());
        equipo.setTipo(request.tipo().trim());
        equipo.setMarca(limpiarOpcional(request.marca()));
        equipo.setModelo(limpiarOpcional(request.modelo()));
        equipo.setEstado(request.estado());
    }

    private EquipoResponse convertirRespuesta(EquipoEntity equipo) {
        return new EquipoResponse(
                equipo.getId(),
                equipo.getCodigoInventario(),
                equipo.getNombre(),
                equipo.getTipo(),
                equipo.getMarca(),
                equipo.getModelo(),
                equipo.getEstado()
        );
    }
}
