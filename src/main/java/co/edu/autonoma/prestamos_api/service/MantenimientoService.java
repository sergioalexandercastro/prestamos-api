package co.edu.autonoma.prestamos_api.service;

import co.edu.autonoma.prestamos_api.dto.MantenimientoRequest;
import co.edu.autonoma.prestamos_api.dto.MantenimientoResponse;
import co.edu.autonoma.prestamos_api.entity.EquipoEntity;
import co.edu.autonoma.prestamos_api.entity.MantenimientoEntity;
import co.edu.autonoma.prestamos_api.enums.EstadoEquipo;
import co.edu.autonoma.prestamos_api.enums.EstadoMantenimiento;
import co.edu.autonoma.prestamos_api.enums.EstadoPrestamo;
import co.edu.autonoma.prestamos_api.repository.EquipoRepository;
import co.edu.autonoma.prestamos_api.repository.MantenimientoRepository;
import co.edu.autonoma.prestamos_api.repository.PrestamoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MantenimientoService {

    private final MantenimientoRepository mantenimientoRepository;
    private final EquipoRepository equipoRepository;
    private final PrestamoRepository prestamoRepository;

    public MantenimientoService(
            MantenimientoRepository mantenimientoRepository,
            EquipoRepository equipoRepository,
            PrestamoRepository prestamoRepository
    ) {
        this.mantenimientoRepository = mantenimientoRepository;
        this.equipoRepository = equipoRepository;
        this.prestamoRepository = prestamoRepository;
    }

    public List<MantenimientoResponse> listar() {
        return mantenimientoRepository.findAll()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    public MantenimientoResponse buscarPorId(Integer id) {
        MantenimientoEntity mantenimiento = obtenerMantenimiento(id);
        return convertirRespuesta(mantenimiento);
    }

    @Transactional
    public MantenimientoResponse crear(MantenimientoRequest request) {

        validarDatos(request);

        EquipoEntity equipo = obtenerEquipo(request.equipoId());

        if (equipo.getEstado() != EstadoEquipo.DISPONIBLE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El equipo no se encuentra disponible para mantenimiento"
            );
        }

        if (prestamoRepository.existsByEquipoIdAndEstado(
                equipo.getId(), EstadoPrestamo.ACTIVO)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede iniciar el mantenimiento porque el equipo tiene un préstamo activo"
            );
        }

        if (mantenimientoRepository.existsByEquipoIdAndEstadoIn(
                equipo.getId(),
                List.of(
                        EstadoMantenimiento.PROGRAMADO,
                        EstadoMantenimiento.EN_PROCESO
                )
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El equipo ya tiene un mantenimiento pendiente o en proceso"
            );
        }

        MantenimientoEntity mantenimiento = new MantenimientoEntity();

        mantenimiento.setEquipoId(equipo.getId());
        mantenimiento.setFechaInicio(request.fechaInicio());
        mantenimiento.setFechaFin(null);
        mantenimiento.setMotivo(request.motivo().trim());
        mantenimiento.setEstado(EstadoMantenimiento.EN_PROCESO);

        MantenimientoEntity guardado =
                mantenimientoRepository.save(mantenimiento);

        equipo.setEstado(EstadoEquipo.EN_MANTENIMIENTO);
        equipoRepository.save(equipo);

        return convertirRespuesta(guardado);
    }

    @Transactional
    public MantenimientoResponse actualizar(
            Integer id,
            MantenimientoRequest request
    ) {

        validarDatos(request);

        MantenimientoEntity mantenimiento = obtenerMantenimiento(id);

        validarMantenimientoEnProceso(mantenimiento);

        EquipoEntity equipoAnterior = obtenerEquipo(mantenimiento.getEquipoId());
        EquipoEntity equipoNuevo = obtenerEquipo(request.equipoId());

        if (!equipoAnterior.getId().equals(equipoNuevo.getId())) {

            validarEquipoDisponible(equipoNuevo);

            if (prestamoRepository.existsByEquipoIdAndEstado(
                    equipoNuevo.getId(), EstadoPrestamo.ACTIVO)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "El nuevo equipo tiene un préstamo activo"
                );
            }

            if (mantenimientoRepository.existsByEquipoIdAndEstadoIn(
                    equipoNuevo.getId(),
                    List.of(
                            EstadoMantenimiento.PROGRAMADO,
                            EstadoMantenimiento.EN_PROCESO
                    )
            )) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "El nuevo equipo ya tiene un mantenimiento pendiente o en proceso"
                );
            }

            equipoAnterior.setEstado(EstadoEquipo.DISPONIBLE);
            equipoRepository.save(equipoAnterior);

            equipoNuevo.setEstado(EstadoEquipo.EN_MANTENIMIENTO);
            equipoRepository.save(equipoNuevo);

            mantenimiento.setEquipoId(equipoNuevo.getId());
        }

        mantenimiento.setFechaInicio(request.fechaInicio());
        mantenimiento.setMotivo(request.motivo().trim());

        MantenimientoEntity actualizado =
                mantenimientoRepository.save(mantenimiento);

        return convertirRespuesta(actualizado);
    }

    @Transactional
    public MantenimientoResponse actualizarParcial(
            Integer id,
            MantenimientoRequest request
    ) {

        MantenimientoEntity mantenimiento = obtenerMantenimiento(id);

        validarMantenimientoEnProceso(mantenimiento);

        if (request.equipoId() != null
                && !request.equipoId().equals(mantenimiento.getEquipoId())) {

            EquipoEntity equipoAnterior = obtenerEquipo(mantenimiento.getEquipoId());
            EquipoEntity equipoNuevo = obtenerEquipo(request.equipoId());

            validarEquipoDisponible(equipoNuevo);

            if (prestamoRepository.existsByEquipoIdAndEstado(
                    equipoNuevo.getId(), EstadoPrestamo.ACTIVO)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "El nuevo equipo tiene un préstamo activo"
                );
            }

            if (mantenimientoRepository.existsByEquipoIdAndEstadoIn(
                    equipoNuevo.getId(),
                    List.of(
                            EstadoMantenimiento.PROGRAMADO,
                            EstadoMantenimiento.EN_PROCESO
                    )
            )) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "El nuevo equipo ya tiene otro mantenimiento pendiente o en proceso"
                );
            }

            equipoAnterior.setEstado(EstadoEquipo.DISPONIBLE);
            equipoRepository.save(equipoAnterior);

            equipoNuevo.setEstado(EstadoEquipo.EN_MANTENIMIENTO);
            equipoRepository.save(equipoNuevo);

            mantenimiento.setEquipoId(equipoNuevo.getId());
        }

        if (request.fechaInicio() != null) {
            validarFechaInicio(request.fechaInicio());
            mantenimiento.setFechaInicio(request.fechaInicio());
        }

        if (request.motivo() != null) {
            if (request.motivo().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El motivo no puede estar vacío"
                );
            }

            mantenimiento.setMotivo(request.motivo().trim());
        }

        MantenimientoEntity actualizado =
                mantenimientoRepository.save(mantenimiento);

        return convertirRespuesta(actualizado);
    }

    @Transactional
    public MantenimientoResponse finalizar(Integer id) {

        MantenimientoEntity mantenimiento = obtenerMantenimiento(id);

        if (mantenimiento.getEstado() != EstadoMantenimiento.EN_PROCESO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se pueden finalizar mantenimientos que estén en proceso"
            );
        }

        EquipoEntity equipo = obtenerEquipo(mantenimiento.getEquipoId());

        mantenimiento.setFechaFin(LocalDateTime.now());
        mantenimiento.setEstado(EstadoMantenimiento.FINALIZADO);

        MantenimientoEntity actualizado =
                mantenimientoRepository.save(mantenimiento);

        equipo.setEstado(EstadoEquipo.DISPONIBLE);
        equipoRepository.save(equipo);

        return convertirRespuesta(actualizado);
    }

    @Transactional
    public void eliminar(Integer id) {

        MantenimientoEntity mantenimiento = obtenerMantenimiento(id);

        if (mantenimiento.getEstado() == EstadoMantenimiento.EN_PROCESO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar un mantenimiento que está en proceso. Primero debe finalizarlo"
            );
        }

        mantenimientoRepository.delete(mantenimiento);
    }

    private MantenimientoEntity obtenerMantenimiento(Integer id) {
        return mantenimientoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un mantenimiento con el ID " + id
                ));
    }

    private EquipoEntity obtenerEquipo(Integer id) {
        return equipoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un equipo con el ID " + id
                ));
    }

    private void validarDatos(MantenimientoRequest request) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los datos del mantenimiento son obligatorios"
            );
        }

        if (request.equipoId() == null || request.equipoId() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID del equipo es obligatorio y debe ser válido"
            );
        }

        if (request.fechaInicio() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha de inicio es obligatoria"
            );
        }

        validarFechaInicio(request.fechaInicio());

        if (request.motivo() == null || request.motivo().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El motivo del mantenimiento es obligatorio"
            );
        }

        if (request.motivo().trim().length() > 255) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El motivo no puede superar los 255 caracteres"
            );
        }
    }

    private void validarFechaInicio(LocalDateTime fechaInicio) {
        if (fechaInicio.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha de inicio no puede ser futura"
            );
        }
    }

    private void validarEquipoDisponible(EquipoEntity equipo) {
        if (equipo.getEstado() != EstadoEquipo.DISPONIBLE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El equipo no se encuentra disponible"
            );
        }
    }

    private void validarMantenimientoEnProceso(
            MantenimientoEntity mantenimiento
    ) {
        if (mantenimiento.getEstado() != EstadoMantenimiento.EN_PROCESO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se pueden modificar mantenimientos que estén en proceso"
            );
        }
    }

    private MantenimientoResponse convertirRespuesta(
            MantenimientoEntity mantenimiento
    ) {
        return new MantenimientoResponse(
                mantenimiento.getId(),
                mantenimiento.getEquipoId(),
                mantenimiento.getFechaInicio(),
                mantenimiento.getFechaFin(),
                mantenimiento.getMotivo(),
                mantenimiento.getEstado()
        );
    }
}