
package co.edu.autonoma.prestamos_api.service;

import co.edu.autonoma.prestamos_api.dto.PrestamoRequest;
import co.edu.autonoma.prestamos_api.dto.PrestamoResponse;
import co.edu.autonoma.prestamos_api.entity.EquipoEntity;
import co.edu.autonoma.prestamos_api.entity.PrestamoEntity;
import co.edu.autonoma.prestamos_api.entity.TrazabilidadEntity;
import co.edu.autonoma.prestamos_api.entity.UsuarioEntity;
import co.edu.autonoma.prestamos_api.enums.EstadoEquipo;
import co.edu.autonoma.prestamos_api.enums.EstadoPrestamo;
import co.edu.autonoma.prestamos_api.enums.OperacionTrazabilidad;
import co.edu.autonoma.prestamos_api.repository.EquipoRepository;
import co.edu.autonoma.prestamos_api.repository.PrestamoRepository;
import co.edu.autonoma.prestamos_api.repository.TrazabilidadRepository;
import co.edu.autonoma.prestamos_api.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EquipoRepository equipoRepository;
    private final TrazabilidadRepository trazabilidadRepository;

    public PrestamoService(
            PrestamoRepository prestamoRepository,
            UsuarioRepository usuarioRepository,
            EquipoRepository equipoRepository,
            TrazabilidadRepository trazabilidadRepository) {
        this.prestamoRepository = prestamoRepository;
        this.usuarioRepository = usuarioRepository;
        this.equipoRepository = equipoRepository;
        this.trazabilidadRepository = trazabilidadRepository;
    }

    public List<PrestamoResponse> listar() {
        return prestamoRepository.findAll()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    public PrestamoResponse buscarPorId(Integer id) {
        PrestamoEntity prestamo = obtenerPrestamo(id);
        return convertirRespuesta(prestamo);
    }

    @Transactional
    public PrestamoResponse crear(PrestamoRequest request) {
        validarDatos(request);

        UsuarioEntity usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un usuario con el ID " + request.usuarioId()
                ));

        EquipoEntity equipo = equipoRepository.findById(request.equipoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un equipo con el ID " + request.equipoId()
                ));

        if (equipo.getEstado() != EstadoEquipo.DISPONIBLE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El equipo no está disponible para préstamo"
            );
        }

        if (prestamoRepository.existsByEquipoIdAndEstado(
                equipo.getId(), EstadoPrestamo.ACTIVO)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El equipo ya tiene un préstamo activo"
            );
        }

        LocalDateTime fechaPrestamo = LocalDateTime.now();

        if (!request.fechaLimite().isAfter(fechaPrestamo)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha límite debe ser posterior a la fecha del préstamo"
            );
        }

        PrestamoEntity prestamo = new PrestamoEntity();

        prestamo.setUsuarioId(usuario.getId());
        prestamo.setEquipoId(equipo.getId());
        prestamo.setFechaPrestamo(fechaPrestamo);
        prestamo.setFechaLimite(request.fechaLimite());
        prestamo.setFechaDevolucion(null);
        prestamo.setEstado(EstadoPrestamo.ACTIVO);

        PrestamoEntity guardado = prestamoRepository.save(prestamo);

        equipo.setEstado(EstadoEquipo.PRESTADO);
        equipoRepository.save(equipo);

        registrarTrazabilidad(
                guardado,
                OperacionTrazabilidad.PRESTAMO,
                "Se registró un préstamo para el equipo con ID " + equipo.getId()
        );

        return convertirRespuesta(guardado);
    }

    @Transactional
    public PrestamoResponse devolver(Integer id) {
        PrestamoEntity prestamo = obtenerPrestamo(id);

        if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El préstamo ya fue devuelto"
            );
        }

        EquipoEntity equipo = equipoRepository.findById(prestamo.getEquipoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe el equipo asociado al préstamo"
                ));

        LocalDateTime fechaDevolucion = LocalDateTime.now();

        prestamo.setFechaDevolucion(fechaDevolucion);
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);

        PrestamoEntity actualizado = prestamoRepository.save(prestamo);

        equipo.setEstado(EstadoEquipo.DISPONIBLE);
        equipoRepository.save(equipo);

        registrarTrazabilidad(
                actualizado,
                OperacionTrazabilidad.DEVOLUCION,
                "Se registró la devolución del equipo con ID " + equipo.getId()
        );

        return convertirRespuesta(actualizado);
    }

    private PrestamoEntity obtenerPrestamo(Integer id) {
        return prestamoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el préstamo con ID " + id
                ));
    }

    private void validarDatos(PrestamoRequest request) {
        if (request == null
                || request.usuarioId() == null
                || request.equipoId() == null
                || request.fechaLimite() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Todos los campos son obligatorios"
            );
        }

        if (request.usuarioId() <= 0 || request.equipoId() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los IDs de usuario y equipo deben ser válidos"
            );
        }
    }

    private void registrarTrazabilidad(
            PrestamoEntity prestamo,
            OperacionTrazabilidad operacion,
            String descripcion) {

        TrazabilidadEntity trazabilidad = new TrazabilidadEntity();

        trazabilidad.setPrestamoId(prestamo.getId());
        trazabilidad.setUsuarioId(prestamo.getUsuarioId());
        trazabilidad.setOperacion(operacion);
        trazabilidad.setFechaHora(LocalDateTime.now());
        trazabilidad.setDescripcion(descripcion);

        trazabilidadRepository.save(trazabilidad);
    }

    private PrestamoResponse convertirRespuesta(PrestamoEntity prestamo) {
        return new PrestamoResponse(
                prestamo.getId(),
                prestamo.getUsuarioId(),
                prestamo.getEquipoId(),
                prestamo.getFechaPrestamo(),
                prestamo.getFechaLimite(),
                prestamo.getFechaDevolucion(),
                prestamo.getEstado()
        );
    }
}
