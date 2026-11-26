package co.edu.autonoma.prestamos_api.repository;

import co.edu.autonoma.prestamos_api.entity.PrestamoEntity;
import co.edu.autonoma.prestamos_api.enums.EstadoPrestamo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrestamoRepository
        extends JpaRepository<PrestamoEntity, Integer> {

    boolean existsByUsuarioId(Integer usuarioId);

    boolean existsByEquipoId(Integer equipoId);

    boolean existsByEquipoIdAndEstado(
            Integer equipoId,
            EstadoPrestamo estado);
}
