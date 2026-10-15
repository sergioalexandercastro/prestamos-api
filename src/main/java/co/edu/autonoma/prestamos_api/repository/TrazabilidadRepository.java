package co.edu.autonoma.prestamos_api.repository;

import co.edu.autonoma.prestamos_api.entity.TrazabilidadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrazabilidadRepository
        extends JpaRepository<TrazabilidadEntity, Integer> {

}
