package co.edu.autonoma.prestamos_api.entity;

import co.edu.autonoma.prestamos_api.enums.OperacionTrazabilidad;
import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Entity
@Table(name = "trazabilidad")
public class TrazabilidadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer prestamoId;

    @Column(nullable = false)
    private Integer usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperacionTrazabilidad operacion;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    private String descripcion;
}
