package co.edu.autonoma.prestamos_api.service;

import co.edu.autonoma.prestamos_api.dto.UsuarioRequest;
import co.edu.autonoma.prestamos_api.dto.UsuarioResponse;
import co.edu.autonoma.prestamos_api.entity.UsuarioEntity;
import co.edu.autonoma.prestamos_api.enums.TipoUsuario;
import co.edu.autonoma.prestamos_api.repository.PrestamoRepository;
import co.edu.autonoma.prestamos_api.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PrestamoRepository prestamoRepository;

    @InjectMocks
    private UsuarioService service;

    // Métodos auxiliares

    private UsuarioEntity crearUsuarioPrueba() {

        UsuarioEntity usuario = new UsuarioEntity();

        usuario.setId(1);
        usuario.setNombre("Usuario de prueba");
        usuario.setDocumento("100000001");
        usuario.setCorreo("usuario@example.com");
        usuario.setTipoUsuario(TipoUsuario.ESTUDIANTE);

        return usuario;
    }

    private UsuarioRequest crearRequestValido() {

        return new UsuarioRequest(
                "Usuario de prueba",
                "100000001",
                "usuario@example.com",
                TipoUsuario.ESTUDIANTE
        );
    }

    private UsuarioRequest crearRequestActualizacion() {

        return new UsuarioRequest(
                "Usuario actualizado",
                "100000002",
                "actualizado@example.com",
                TipoUsuario.DOCENTE
        );
    }

    // 1. Listar usuarios

    @Test
    void listar_usuariosExistentes_devuelveListaDeUsuarios() {

        // Arrange
        UsuarioEntity usuario = crearUsuarioPrueba();

        when(usuarioRepository.findAll())
                .thenReturn(List.of(usuario));

        // Act
        List<UsuarioResponse> respuesta = service.listar();

        // Assert
        assertThat(respuesta).hasSize(1);

        assertThat(respuesta.get(0).id()).isEqualTo(1);

        assertThat(respuesta.get(0).nombre())
                .isEqualTo("Usuario de prueba");

        assertThat(respuesta.get(0).documento())
                .isEqualTo("100000001");

        assertThat(respuesta.get(0).correo())
                .isEqualTo("usuario@example.com");

        assertThat(respuesta.get(0).tipoUsuario())
                .isEqualTo(TipoUsuario.ESTUDIANTE);

        verify(usuarioRepository).findAll();
    }

    // 2. Buscar usuario existente

    @Test
    void buscarPorId_existente_devuelveElUsuario() {

        // Arrange
        UsuarioEntity usuario = crearUsuarioPrueba();

        when(usuarioRepository.findById(1))
                .thenReturn(Optional.of(usuario));

        // Act
        UsuarioResponse respuesta = service.buscarPorId(1);

        // Assert
        assertThat(respuesta).isNotNull();
        assertThat(respuesta.id()).isEqualTo(1);

        assertThat(respuesta.nombre())
                .isEqualTo("Usuario de prueba");

        assertThat(respuesta.documento())
                .isEqualTo("100000001");

        assertThat(respuesta.correo())
                .isEqualTo("usuario@example.com");

        assertThat(respuesta.tipoUsuario())
                .isEqualTo(TipoUsuario.ESTUDIANTE);

        verify(usuarioRepository).findById(1);
    }

    // 3. Buscar usuario inexistente

    @Test
    void buscarPorId_inexistente_lanzaExcepcionNotFound() {

        // Arrange
        when(usuarioRepository.findById(999))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.buscarPorId(999)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(excepcion.getReason())
                .isEqualTo("No se encontró el usuario con ID 999");
    }

    // 4. Crear usuario válido

    @Test
    void crear_datosValidos_registraElUsuario() {

        // Arrange
        UsuarioRequest request = new UsuarioRequest(
                " Usuario de prueba ",
                " 100000001 ",
                " usuario@example.com ",
                TipoUsuario.ESTUDIANTE
        );

        when(usuarioRepository.existsByDocumento("100000001"))
                .thenReturn(false);

        when(usuarioRepository.existsByCorreo("usuario@example.com"))
                .thenReturn(false);

        when(usuarioRepository.save(any(UsuarioEntity.class)))
                .thenAnswer(invocacion -> {

                    UsuarioEntity usuario =
                            invocacion.getArgument(0);

                    usuario.setId(1);

                    return usuario;
                });

        // Act
        UsuarioResponse respuesta = service.crear(request);

        // Assert
        assertThat(respuesta.id()).isEqualTo(1);

        assertThat(respuesta.nombre())
                .isEqualTo("Usuario de prueba");

        assertThat(respuesta.documento())
                .isEqualTo("100000001");

        assertThat(respuesta.correo())
                .isEqualTo("usuario@example.com");

        assertThat(respuesta.tipoUsuario())
                .isEqualTo(TipoUsuario.ESTUDIANTE);

        verify(usuarioRepository)
                .save(any(UsuarioEntity.class));
    }

    // 5. Crear usuario con documento duplicado

    @Test
    void crear_documentoDuplicado_lanzaExcepcionConflict() {

        // Arrange
        UsuarioRequest request = crearRequestValido();

        when(usuarioRepository.existsByDocumento("100000001"))
                .thenReturn(true);

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(request)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(excepcion.getReason())
                .isEqualTo(
                        "Ya existe un usuario con ese documento"
                );

        verify(usuarioRepository, never())
                .save(any(UsuarioEntity.class));

        verify(usuarioRepository, never())
                .existsByCorreo(anyString());
    }

    // 6. Crear usuario con correo duplicado

    @Test
    void crear_correoDuplicado_lanzaExcepcionConflict() {

        // Arrange
        UsuarioRequest request = crearRequestValido();

        when(usuarioRepository.existsByDocumento("100000001"))
                .thenReturn(false);

        when(usuarioRepository.existsByCorreo("usuario@example.com"))
                .thenReturn(true);

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(request)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(excepcion.getReason())
                .isEqualTo(
                        "Ya existe un usuario con ese correo"
                );

        verify(usuarioRepository, never())
                .save(any(UsuarioEntity.class));
    }

    // 7. Crear usuario con campos obligatorios vacíos

    @Test
    void crear_camposObligatoriosVacios_lanzaExcepcionBadRequest() {

        // Arrange
        UsuarioRequest request = new UsuarioRequest(
                "   ",
                "100000001",
                "usuario@example.com",
                TipoUsuario.ESTUDIANTE
        );

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(request)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(excepcion.getReason())
                .isEqualTo("Todos los campos son obligatorios");

        verifyNoInteractions(usuarioRepository);
    }

    // 8. Crear usuario con correo inválido

    @Test
    void crear_correoInvalido_lanzaExcepcionBadRequest() {

        // Arrange
        UsuarioRequest request = new UsuarioRequest(
                "Usuario de prueba",
                "100000001",
                "correo-invalido",
                TipoUsuario.ESTUDIANTE
        );

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(request)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(excepcion.getReason())
                .isEqualTo("El formato del correo no es válido");

        verifyNoInteractions(usuarioRepository);
    }

    // 9. Actualizar usuario existente

    @Test
    void actualizar_datosValidos_actualizaElUsuario() {

        // Arrange
        UsuarioEntity usuario = crearUsuarioPrueba();

        UsuarioRequest request = crearRequestActualizacion();

        when(usuarioRepository.findById(1))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.existsByDocumentoAndIdNot(
                "100000002", 1))
                .thenReturn(false);

        when(usuarioRepository.existsByCorreoAndIdNot(
                "actualizado@example.com", 1))
                .thenReturn(false);

        when(usuarioRepository.save(usuario))
                .thenReturn(usuario);

        // Act
        UsuarioResponse respuesta = service.actualizar(1, request);

        // Assert
        assertThat(respuesta.id()).isEqualTo(1);

        assertThat(respuesta.nombre())
                .isEqualTo("Usuario actualizado");

        assertThat(respuesta.documento())
                .isEqualTo("100000002");

        assertThat(respuesta.correo())
                .isEqualTo("actualizado@example.com");

        assertThat(respuesta.tipoUsuario())
                .isEqualTo(TipoUsuario.DOCENTE);

        verify(usuarioRepository).save(usuario);
    }

    // 10. Actualizar con documento duplicado

    @Test
    void actualizar_documentoDeOtroUsuario_lanzaExcepcionConflict() {

        // Arrange
        UsuarioEntity usuario = crearUsuarioPrueba();

        UsuarioRequest request = crearRequestActualizacion();

        when(usuarioRepository.findById(1))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.existsByDocumentoAndIdNot(
                "100000002", 1))
                .thenReturn(true);

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.actualizar(1, request)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(excepcion.getReason())
                .isEqualTo(
                        "El documento ya pertenece a otro usuario"
                );

        assertThat(usuario.getDocumento())
                .isEqualTo("100000001");

        verify(usuarioRepository, never())
                .save(any(UsuarioEntity.class));

        verify(usuarioRepository, never())
                .existsByCorreoAndIdNot(anyString(), anyInt());
    }

    // 11. Actualizar con correo duplicado

    @Test
    void actualizar_correoDeOtroUsuario_lanzaExcepcionConflict() {

        // Arrange
        UsuarioEntity usuario = crearUsuarioPrueba();

        UsuarioRequest request = crearRequestActualizacion();

        when(usuarioRepository.findById(1))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.existsByDocumentoAndIdNot(
                "100000002", 1))
                .thenReturn(false);

        when(usuarioRepository.existsByCorreoAndIdNot(
                "actualizado@example.com", 1))
                .thenReturn(true);

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.actualizar(1, request)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(excepcion.getReason())
                .isEqualTo(
                        "El correo ya pertenece a otro usuario"
                );

        assertThat(usuario.getCorreo())
                .isEqualTo("usuario@example.com");

        assertThat(usuario.getDocumento())
                .isEqualTo("100000001");

        verify(usuarioRepository, never())
                .save(any(UsuarioEntity.class));
    }

    // 12. Actualización parcial

    @Test
    void actualizarParcial_soloNombre_conservaLosDemasCampos() {

        // Arrange
        UsuarioEntity usuario = crearUsuarioPrueba();

        UsuarioRequest request = new UsuarioRequest(
                "Nuevo nombre",
                null,
                null,
                null
        );

        when(usuarioRepository.findById(1))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.save(usuario))
                .thenReturn(usuario);

        // Act
        UsuarioResponse respuesta =
                service.actualizarParcial(1, request);

        // Assert
        assertThat(respuesta.id()).isEqualTo(1);

        assertThat(respuesta.nombre())
                .isEqualTo("Nuevo nombre");

        assertThat(respuesta.documento())
                .isEqualTo("100000001");

        assertThat(respuesta.correo())
                .isEqualTo("usuario@example.com");

        assertThat(respuesta.tipoUsuario())
                .isEqualTo(TipoUsuario.ESTUDIANTE);

        verify(usuarioRepository).save(usuario);

        verify(usuarioRepository, never())
                .existsByDocumentoAndIdNot(anyString(), anyInt());

        verify(usuarioRepository, never())
                .existsByCorreoAndIdNot(anyString(), anyInt());
    }

    // 13. Actualización parcial con correo duplicado

    @Test
    void actualizarParcial_correoDuplicado_lanzaExcepcionConflict() {

        // Arrange
        UsuarioEntity usuario = crearUsuarioPrueba();

        UsuarioRequest request = new UsuarioRequest(
                null,
                null,
                "duplicado@example.com",
                null
        );

        when(usuarioRepository.findById(1))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.existsByCorreoAndIdNot(
                "duplicado@example.com", 1))
                .thenReturn(true);

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.actualizarParcial(1, request)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(excepcion.getReason())
                .isEqualTo(
                        "Ya existe otro usuario con ese correo"
                );

        assertThat(usuario.getCorreo())
                .isEqualTo("usuario@example.com");

        verify(usuarioRepository, never())
                .save(any(UsuarioEntity.class));
    }

    // 14. Eliminar usuario sin préstamos asociados

    @Test
    void eliminar_usuarioSinPrestamos_borraElUsuario() {

        // Arrange
        UsuarioEntity usuario = crearUsuarioPrueba();

        when(usuarioRepository.findById(1))
                .thenReturn(Optional.of(usuario));

        when(prestamoRepository.existsByUsuarioId(1))
                .thenReturn(false);

        // Act
        service.eliminar(1);

        // Assert
        verify(prestamoRepository).existsByUsuarioId(1);

        verify(usuarioRepository).delete(usuario);
    }

    // 15. Eliminar usuario con préstamos asociados

    @Test
    void eliminar_usuarioConPrestamos_lanzaExcepcionConflict() {

        // Arrange
        UsuarioEntity usuario = crearUsuarioPrueba();

        when(usuarioRepository.findById(1))
                .thenReturn(Optional.of(usuario));

        when(prestamoRepository.existsByUsuarioId(1))
                .thenReturn(true);

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.eliminar(1)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(excepcion.getReason())
                .isEqualTo(
                        "No se puede eliminar el usuario porque tiene préstamos asociados"
                );

        verify(prestamoRepository).existsByUsuarioId(1);

        verify(usuarioRepository, never())
                .delete(any(UsuarioEntity.class));
    }
}
