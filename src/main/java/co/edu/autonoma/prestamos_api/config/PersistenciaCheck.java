package co.edu.autonoma.prestamos_api.config;

import co.edu.autonoma.prestamos_api.repository.EquipoRepository;
import co.edu.autonoma.prestamos_api.repository.MantenimientoRepository;
import co.edu.autonoma.prestamos_api.repository.PrestamoRepository;
import co.edu.autonoma.prestamos_api.repository.TrazabilidadRepository;
import co.edu.autonoma.prestamos_api.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PersistenciaCheck implements CommandLineRunner {

    private final EquipoRepository equipoRepository;
    private final MantenimientoRepository mantenimientoRepository;
    private final PrestamoRepository prestamoRepository;
    private final TrazabilidadRepository trazabilidadRepository;
    private final UsuarioRepository usuarioRepository;

    public PersistenciaCheck(
            EquipoRepository equipoRepository,
            MantenimientoRepository mantenimientoRepository,
            PrestamoRepository prestamoRepository,
            TrazabilidadRepository trazabilidadRepository,
            UsuarioRepository usuarioRepository) {
        this.equipoRepository = equipoRepository;
        this.mantenimientoRepository = mantenimientoRepository;
        this.prestamoRepository = prestamoRepository;
        this.trazabilidadRepository = trazabilidadRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Verificacion de persistencia ===");

        System.out.println("Usuarios: " + usuarioRepository.count());
        System.out.println("Equipos: " + equipoRepository.count());
        System.out.println("Prestamos: " + prestamoRepository.count());
        System.out.println("Mantenimientos: " + mantenimientoRepository.count());
        System.out.println("Trazabilidad: " + trazabilidadRepository.count());

        System.out.println("=== Fin de la verificacion ===");
    }
}