package co.edu.autonoma.prestamos_api.repository;

import co.edu.autonoma.prestamos_api.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository
        extends JpaRepository<UsuarioEntity, Integer> {

}
