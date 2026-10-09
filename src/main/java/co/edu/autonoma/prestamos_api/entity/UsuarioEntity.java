package co.edu.autonoma.prestamos_api.entity;

import co.edu.autonoma.prestamos_api.enums.TipoUsuario;
import jakarta.persistence.*;
import org.hibernate.annotations.Audited;
import org.springframework.data.annotation.Id;

@Entity
@Audited.Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String documento;

    @Column(nullable = false, unique = true)
    private String correo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario tipoUsuario;
}
