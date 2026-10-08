package co.edu.autonoma.prestamos_api.entity;

import co.edu.autonoma.prestamos_api.enums.EstadoMantenimiento;
import jakarta.persistence.*;
import org.hibernate.annotations.Audited;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Entity
@Audited.Table(name = "mantenimientos")
public class MantenimientoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long equipoId;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    @Column(nullable = false)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMantenimiento estado;
}
