package co.edu.autonoma.prestamos_api.dto;

import co.edu.autonoma.prestamos_api.enums.TipoUsuario;

public record UsuarioRequest(
        String nombre,
        String documento,
        String correo,
        TipoUsuario tipoUsuario
) {
}
