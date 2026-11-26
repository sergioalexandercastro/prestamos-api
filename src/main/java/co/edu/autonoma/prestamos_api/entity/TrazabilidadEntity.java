package co.edu.autonoma.prestamos_api.entity;

import co.edu.autonoma.prestamos_api.enums.OperacionTrazabilidad;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "trazabilidad")
public class TrazabilidadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "prestamo_id", nullable = false)
    private Integer prestamoId;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OperacionTrazabilidad operacion;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(length = 255)
    private String descripcion;

}
