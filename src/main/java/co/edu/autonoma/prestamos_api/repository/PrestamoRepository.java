package co.edu.autonoma.prestamos_api.repository;

import co.edu.autonoma.prestamos_api.entity.PrestamoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrestamoRepository
        extends JpaRepository<PrestamoEntity, Integer> {

    boolean existsByUsuarioId(Integer usuarioId);
}
