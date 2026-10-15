package co.edu.autonoma.prestamos_api.repository;

import co.edu.autonoma.prestamos_api.entity.EquipoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipoRepository
        extends JpaRepository<EquipoEntity, Integer> {

}
