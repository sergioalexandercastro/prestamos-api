
package co.edu.autonoma.prestamos_api.controller;

import co.edu.autonoma.prestamos_api.dto.EquipoRequest;
import co.edu.autonoma.prestamos_api.dto.EquipoResponse;
import co.edu.autonoma.prestamos_api.enums.EstadoEquipo;
import co.edu.autonoma.prestamos_api.service.EquipoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipos")
public class EquipoController {

    private final EquipoService equipoService;

    public EquipoController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    @GetMapping
    public List<EquipoResponse> listar(
            @RequestParam(required = false) EstadoEquipo estado) {
        return equipoService.listar(estado);
    }

    @GetMapping("/{id}")
    public EquipoResponse buscarPorId(@PathVariable Integer id) {
        return equipoService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipoResponse crear(@RequestBody EquipoRequest request) {
        return equipoService.crear(request);
    }

    @PutMapping("/{id}")
    public EquipoResponse actualizar(
            @PathVariable Integer id,
            @RequestBody EquipoRequest request) {
        return equipoService.actualizar(id, request);
    }

    @PatchMapping("/{id}")
    public EquipoResponse actualizarParcial(
            @PathVariable Integer id,
            @RequestBody EquipoRequest request) {
        return equipoService.actualizarParcial(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        equipoService.eliminar(id);
    }
}
