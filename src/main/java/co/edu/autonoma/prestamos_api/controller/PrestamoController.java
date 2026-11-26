
package co.edu.autonoma.prestamos_api.controller;

import co.edu.autonoma.prestamos_api.dto.PrestamoRequest;
import co.edu.autonoma.prestamos_api.dto.PrestamoResponse;
import co.edu.autonoma.prestamos_api.service.PrestamoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @GetMapping
    public List<PrestamoResponse> listar() {
        return prestamoService.listar();
    }

    @GetMapping("/{id}")
    public PrestamoResponse buscarPorId(@PathVariable Integer id) {
        return prestamoService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PrestamoResponse crear(@RequestBody PrestamoRequest request) {
        return prestamoService.crear(request);
    }

    @PatchMapping("/{id}/devolucion")
    public PrestamoResponse devolver(@PathVariable Integer id) {
        return prestamoService.devolver(id);
    }
}
