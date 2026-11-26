package co.edu.autonoma.prestamos_api.repository;

import co.edu.autonoma.prestamos_api.entity.EquipoEntity;
import co.edu.autonoma.prestamos_api.enums.EstadoEquipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipoRepository extends JpaRepository<EquipoEntity, Integer> {

    List<EquipoEntity> findByEstado(EstadoEquipo estado);

    boolean existsByCodigoInventario(String codigoInventario);

    boolean existsByCodigoInventarioAndIdNot(String codigoInventario, Integer id);
}
