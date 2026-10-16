package co.edu.autonoma.prestamos_api.entity;

import co.edu.autonoma.prestamos_api.enums.EstadoMantenimiento;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mantenimientos")
public class MantenimientoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "equipo_id", nullable = false)
    private Integer equipoId;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(nullable = false, length = 255)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoMantenimiento estado;

}
