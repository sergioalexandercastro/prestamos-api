package co.edu.autonoma.prestamos_api.controller;

import co.edu.autonoma.prestamos_api.dto.MantenimientoRequest;
import co.edu.autonoma.prestamos_api.dto.MantenimientoResponse;
import co.edu.autonoma.prestamos_api.service.MantenimientoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mantenimientos")
public class MantenimientoController {

    private final MantenimientoService mantenimientoService;

    public MantenimientoController(
            MantenimientoService mantenimientoService
    ) {
        this.mantenimientoService = mantenimientoService;
    }

    @GetMapping
    public List<MantenimientoResponse> listar() {
        return mantenimientoService.listar();
    }

    @GetMapping("/{id}")
    public MantenimientoResponse buscarPorId(@PathVariable Integer id) {
        return mantenimientoService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MantenimientoResponse crear(
            @RequestBody MantenimientoRequest request
    ) {
        return mantenimientoService.crear(request);
    }

    @PutMapping("/{id}")
    public MantenimientoResponse actualizar(
            @PathVariable Integer id,
            @RequestBody MantenimientoRequest request
    ) {
        return mantenimientoService.actualizar(id, request);
    }

    @PatchMapping("/{id}")
    public MantenimientoResponse actualizarParcial(
            @PathVariable Integer id,
            @RequestBody MantenimientoRequest request
    ) {
        return mantenimientoService.actualizarParcial(id, request);
    }

    @PatchMapping("/{id}/finalizacion")
    public MantenimientoResponse finalizar(@PathVariable Integer id) {
        return mantenimientoService.finalizar(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        mantenimientoService.eliminar(id);
    }
}