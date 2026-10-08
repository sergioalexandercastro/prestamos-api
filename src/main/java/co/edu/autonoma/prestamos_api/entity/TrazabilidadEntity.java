package co.edu.autonoma.prestamos_api.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Entity
@Table(name = "trazabilidad")
public class TrazabilidadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long prestamoId;

    @Column(nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperacionTrazabilidad operacion;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    private String descripcion;
}
