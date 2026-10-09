package co.edu.autonoma.prestamos_api.entity;

import co.edu.autonoma.prestamos_api.enums.EstadoEquipo;
import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

@Entity
@Table(name = "equipos")
public class EquipoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String codigoInventario;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String tipo;

    private String marca;

    private String modelo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEquipo estado;
}
