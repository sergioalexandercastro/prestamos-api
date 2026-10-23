package co.edu.autonoma.prestamos_api.dto;

import co.edu.autonoma.prestamos_api.enums.TipoUsuario;

public record UsuarioResponse(
        Integer id,
        String nombre,
        String documento,
        String correo,
        TipoUsuario tipoUsuario
) {
}
