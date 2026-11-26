package co.edu.autonoma.prestamos_api.repository;

import co.edu.autonoma.prestamos_api.entity.MantenimientoEntity;
import co.edu.autonoma.prestamos_api.enums.EstadoMantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MantenimientoRepository extends JpaRepository<MantenimientoEntity, Integer> {

    boolean existsByEquipoId(Integer equipoId);

    boolean existsByEquipoIdAndEstadoIn(
            Integer equipoId,
            List<EstadoMantenimiento> estados
    );
}