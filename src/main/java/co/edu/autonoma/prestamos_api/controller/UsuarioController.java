
package co.edu.autonoma.prestamos_api.controller;

import co.edu.autonoma.prestamos_api.dto.UsuarioRequest;
import co.edu.autonoma.prestamos_api.dto.UsuarioResponse;
import co.edu.autonoma.prestamos_api.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable Integer id) {
        return usuarioService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@RequestBody UsuarioRequest request) {
        return usuarioService.crear(request);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(
            @PathVariable Integer id,
            @RequestBody UsuarioRequest request) {
        return usuarioService.actualizar(id, request);
    }

    @PatchMapping("/{id}")
    public UsuarioResponse actualizarParcial(
            @PathVariable Integer id,
            @RequestBody UsuarioRequest request) {
        return usuarioService.actualizarParcial(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        usuarioService.eliminar(id);
    }
}
