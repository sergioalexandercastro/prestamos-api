package co.edu.autonoma.prestamos_api.repository;

import co.edu.autonoma.prestamos_api.entity.MantenimientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MantenimientoRepository
        extends JpaRepository<MantenimientoEntity, Integer> {

}