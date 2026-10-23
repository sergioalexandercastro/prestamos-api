package co.edu.autonoma.prestamos_api.service;

import co.edu.autonoma.prestamos_api.dto.UsuarioRequest;
import co.edu.autonoma.prestamos_api.dto.UsuarioResponse;
import co.edu.autonoma.prestamos_api.entity.UsuarioEntity;
import co.edu.autonoma.prestamos_api.repository.PrestamoRepository;
import co.edu.autonoma.prestamos_api.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PrestamoRepository prestamoRepository;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PrestamoRepository prestamoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.prestamoRepository = prestamoRepository;
    }

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    public UsuarioResponse buscarPorId(Integer id) {
        UsuarioEntity usuario = obtenerUsuario(id);
        return convertirRespuesta(usuario);
    }

    public UsuarioResponse crear(UsuarioRequest request) {
        validarDatos(request);

        if (usuarioRepository.existsByDocumento(request.documento().trim())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un usuario con ese documento"
            );
        }

        if (usuarioRepository.existsByCorreo(request.correo().trim())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un usuario con ese correo"
            );
        }

        UsuarioEntity usuario = new UsuarioEntity();
        asignarDatos(usuario, request);

        return convertirRespuesta(usuarioRepository.save(usuario));
    }

    public UsuarioResponse actualizar(Integer id, UsuarioRequest request) {
        UsuarioEntity usuario = obtenerUsuario(id);
        validarDatos(request);

        String documento = request.documento().trim();
        String correo = request.correo().trim();

        if (usuarioRepository.existsByDocumentoAndIdNot(documento, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El documento ya pertenece a otro usuario"
            );
        }

        if (usuarioRepository.existsByCorreoAndIdNot(correo, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo ya pertenece a otro usuario"
            );
        }

        asignarDatos(usuario, request);

        return convertirRespuesta(usuarioRepository.save(usuario));
    }

    public UsuarioResponse actualizarParcial(
            Integer id,
            UsuarioRequest request) {

        UsuarioEntity usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un usuario con el ID " + id
                ));

        if (request.nombre() != null) {
            usuario.setNombre(request.nombre());
        }

        if (request.documento() != null) {
            if (usuarioRepository.existsByDocumentoAndIdNot(
                    request.documento(), id)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Ya existe otro usuario con ese documento"
                );
            }
            usuario.setDocumento(request.documento());
        }

        if (request.correo() != null) {
            if (usuarioRepository.existsByCorreoAndIdNot(
                    request.correo(), id)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Ya existe otro usuario con ese correo"
                );
            }
            usuario.setCorreo(request.correo());
        }

        if (request.tipoUsuario() != null) {
            usuario.setTipoUsuario(request.tipoUsuario());
        }

        UsuarioEntity actualizado = usuarioRepository.save(usuario);

        return new UsuarioResponse(
                actualizado.getId(),
                actualizado.getNombre(),
                actualizado.getDocumento(),
                actualizado.getCorreo(),
                actualizado.getTipoUsuario()
        );
    }

    public void eliminar(Integer id) {
        UsuarioEntity usuario = obtenerUsuario(id);

        if (prestamoRepository.existsByUsuarioId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el usuario porque tiene préstamos asociados"
            );
        }

        usuarioRepository.delete(usuario);
    }

    private UsuarioEntity obtenerUsuario(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el usuario con ID " + id
                ));
    }

    private void validarDatos(UsuarioRequest request) {
        if (request == null
                || estaVacio(request.nombre())
                || estaVacio(request.documento())
                || estaVacio(request.correo())
                || request.tipoUsuario() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Todos los campos son obligatorios"
            );
        }

        if (!request.correo().trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El formato del correo no es válido"
            );
        }
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private void asignarDatos(
            UsuarioEntity usuario,
            UsuarioRequest request) {
        usuario.setNombre(request.nombre().trim());
        usuario.setDocumento(request.documento().trim());
        usuario.setCorreo(request.correo().trim());
        usuario.setTipoUsuario(request.tipoUsuario());
    }

    private UsuarioResponse convertirRespuesta(UsuarioEntity usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getDocumento(),
                usuario.getCorreo(),
                usuario.getTipoUsuario()
        );
    }
}
