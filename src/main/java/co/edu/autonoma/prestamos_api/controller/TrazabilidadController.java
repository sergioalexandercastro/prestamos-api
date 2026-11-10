package co.edu.autonoma.prestamos_api.controller;

import co.edu.autonoma.prestamos_api.dto.TrazabilidadResponse;
import co.edu.autonoma.prestamos_api.service.TrazabilidadService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trazabilidad")
public class TrazabilidadController {

    private final TrazabilidadService trazabilidadService;

    public TrazabilidadController(
            TrazabilidadService trazabilidadService
    ) {
        this.trazabilidadService = trazabilidadService;
    }

    @GetMapping
    public List<TrazabilidadResponse> listar() {
        return trazabilidadService.listar();
    }

    @GetMapping("/{id}")
    public TrazabilidadResponse buscarPorId(
            @PathVariable Integer id
    ) {
        return trazabilidadService.buscarPorId(id);
    }
}