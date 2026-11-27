package co.edu.autonoma.prestamos_api.service;

import co.edu.autonoma.prestamos_api.dto.EquipoRequest;
import co.edu.autonoma.prestamos_api.dto.EquipoResponse;
import co.edu.autonoma.prestamos_api.entity.EquipoEntity;
import co.edu.autonoma.prestamos_api.enums.EstadoEquipo;
import co.edu.autonoma.prestamos_api.repository.EquipoRepository;
import co.edu.autonoma.prestamos_api.repository.MantenimientoRepository;
import co.edu.autonoma.prestamos_api.repository.PrestamoRepository;

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
class EquipoServiceTest {

    @Mock
    private EquipoRepository equipoRepository;

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private MantenimientoRepository mantenimientoRepository;

    @InjectMocks
    private EquipoService service;

    // Métodos auxiliares

    private EquipoEntity crearEquipoPrueba() {

        EquipoEntity equipo = new EquipoEntity();

        equipo.setId(1);
        equipo.setCodigoInventario("EQ-001");
        equipo.setNombre("Computador portátil");
        equipo.setTipo("Portátil");
        equipo.setMarca("Lenovo");
        equipo.setModelo("ThinkPad");
        equipo.setEstado(EstadoEquipo.DISPONIBLE);

        return equipo;
    }

    private EquipoRequest crearRequestValido() {

        return new EquipoRequest(
                "EQ-001",
                "Computador portátil",
                "Portátil",
                "Lenovo",
                "ThinkPad",
                EstadoEquipo.DISPONIBLE
        );
    }

    // 1. Listar todos los equipos

    @Test
    void listar_sinFiltro_devuelveTodosLosEquipos() {

        // Arrange
        EquipoEntity equipo = crearEquipoPrueba();

        when(equipoRepository.findAll())
                .thenReturn(List.of(equipo));

        // Act
        List<EquipoResponse> respuesta = service.listar(null);

        // Assert
        assertThat(respuesta).hasSize(1);

        assertThat(respuesta.get(0).id()).isEqualTo(1);

        assertThat(respuesta.get(0).codigoInventario())
                .isEqualTo("EQ-001");

        verify(equipoRepository).findAll();

        verify(equipoRepository, never())
                .findByEstado(any(EstadoEquipo.class));
    }

    // 2. Listar equipos por estado

    @Test
    void listar_conEstadoDisponible_devuelveEquiposDisponibles() {

        // Arrange
        EquipoEntity equipo = crearEquipoPrueba();

        when(equipoRepository.findByEstado(
                EstadoEquipo.DISPONIBLE))
                .thenReturn(List.of(equipo));

        // Act
        List<EquipoResponse> respuesta =
                service.listar(EstadoEquipo.DISPONIBLE);

        // Assert
        assertThat(respuesta).hasSize(1);

        assertThat(respuesta.get(0).estado())
                .isEqualTo(EstadoEquipo.DISPONIBLE);

        verify(equipoRepository)
                .findByEstado(EstadoEquipo.DISPONIBLE);

        verify(equipoRepository, never()).findAll();
    }

    // 3. Buscar equipo existente

    @Test
    void buscarPorId_existente_devuelveElEquipo() {

        // Arrange
        EquipoEntity equipo = crearEquipoPrueba();

        when(equipoRepository.findById(1))
                .thenReturn(Optional.of(equipo));

        // Act
        EquipoResponse respuesta = service.buscarPorId(1);

        // Assert
        assertThat(respuesta).isNotNull();
        assertThat(respuesta.id()).isEqualTo(1);
        assertThat(respuesta.codigoInventario()).isEqualTo("EQ-001");
        assertThat(respuesta.nombre()).isEqualTo("Computador portátil");
        assertThat(respuesta.tipo()).isEqualTo("Portátil");
        assertThat(respuesta.marca()).isEqualTo("Lenovo");
        assertThat(respuesta.modelo()).isEqualTo("ThinkPad");

        assertThat(respuesta.estado())
                .isEqualTo(EstadoEquipo.DISPONIBLE);
    }

    // 4. Buscar equipo inexistente

    @Test
    void buscarPorId_inexistente_lanzaExcepcionNotFound() {

        // Arrange
        when(equipoRepository.findById(999))
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
                .isEqualTo("No se encontró el equipo con ID 999");
    }

    // 5. Crear equipo válido

    @Test
    void crear_datosValidos_registraElEquipo() {

        // Arrange
        EquipoRequest request = new EquipoRequest(
                " EQ-001 ",
                " Computador portátil ",
                " Portátil ",
                " Lenovo ",
                "   ",
                EstadoEquipo.DISPONIBLE
        );

        when(equipoRepository.existsByCodigoInventario("EQ-001"))
                .thenReturn(false);

        when(equipoRepository.save(any(EquipoEntity.class)))
                .thenAnswer(invocacion -> {

                    EquipoEntity equipo = invocacion.getArgument(0);

                    equipo.setId(1);

                    return equipo;
                });

        // Act
        EquipoResponse respuesta = service.crear(request);

        // Assert
        assertThat(respuesta.id()).isEqualTo(1);
        assertThat(respuesta.codigoInventario()).isEqualTo("EQ-001");
        assertThat(respuesta.nombre()).isEqualTo("Computador portátil");
        assertThat(respuesta.tipo()).isEqualTo("Portátil");
        assertThat(respuesta.marca()).isEqualTo("Lenovo");
        assertThat(respuesta.modelo()).isNull();

        assertThat(respuesta.estado())
                .isEqualTo(EstadoEquipo.DISPONIBLE);

        verify(equipoRepository)
                .save(any(EquipoEntity.class));
    }

    // 6. Crear equipo con código duplicado

    @Test
    void crear_codigoDuplicado_lanzaExcepcionConflict() {

        // Arrange
        EquipoRequest request = crearRequestValido();

        when(equipoRepository.existsByCodigoInventario("EQ-001"))
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
                        "Ya existe un equipo con ese código de inventario"
                );

        verify(equipoRepository, never())
                .save(any(EquipoEntity.class));
    }

    // 7. Crear equipo con datos incompletos

    @Test
    void crear_camposObligatoriosVacios_lanzaExcepcionBadRequest() {

        // Arrange
        EquipoRequest request = new EquipoRequest(
                "",
                "Computador portátil",
                "Portátil",
                null,
                null,
                EstadoEquipo.DISPONIBLE
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
                .isEqualTo(
                        "Todos los campos obligatorios deben estar completos"
                );

        verifyNoInteractions(equipoRepository);
    }

    // 8. Actualizar equipo existente

    @Test
    void actualizar_datosValidos_actualizaElEquipo() {

        // Arrange
        EquipoEntity equipo = crearEquipoPrueba();

        EquipoRequest request = new EquipoRequest(
                "EQ-002",
                "Computador actualizado",
                "Portátil",
                "Dell",
                "Latitude",
                EstadoEquipo.DISPONIBLE
        );

        when(equipoRepository.findById(1))
                .thenReturn(Optional.of(equipo));

        when(equipoRepository.existsByCodigoInventarioAndIdNot(
                "EQ-002", 1))
                .thenReturn(false);

        when(equipoRepository.save(equipo))
                .thenReturn(equipo);

        // Act
        EquipoResponse respuesta = service.actualizar(1, request);

        // Assert
        assertThat(respuesta.id()).isEqualTo(1);
        assertThat(respuesta.codigoInventario()).isEqualTo("EQ-002");
        assertThat(respuesta.nombre()).isEqualTo("Computador actualizado");
        assertThat(respuesta.tipo()).isEqualTo("Portátil");
        assertThat(respuesta.marca()).isEqualTo("Dell");
        assertThat(respuesta.modelo()).isEqualTo("Latitude");

        assertThat(respuesta.estado())
                .isEqualTo(EstadoEquipo.DISPONIBLE);

        verify(equipoRepository).save(equipo);
    }

    // 9. Actualizar con código duplicado

    @Test
    void actualizar_codigoDeOtroEquipo_lanzaExcepcionConflict() {

        // Arrange
        EquipoEntity equipo = crearEquipoPrueba();

        EquipoRequest request = new EquipoRequest(
                "EQ-002",
                "Computador actualizado",
                "Portátil",
                "Dell",
                "Latitude",
                EstadoEquipo.DISPONIBLE
        );

        when(equipoRepository.findById(1))
                .thenReturn(Optional.of(equipo));

        when(equipoRepository.existsByCodigoInventarioAndIdNot(
                "EQ-002", 1))
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
                        "El código de inventario ya pertenece a otro equipo"
                );

        assertThat(equipo.getCodigoInventario())
                .isEqualTo("EQ-001");

        verify(equipoRepository, never())
                .save(any(EquipoEntity.class));
    }

    // 10. Actualización parcial

    @Test
    void actualizarParcial_soloNombre_conservaLosDemasCampos() {

        // Arrange
        EquipoEntity equipo = crearEquipoPrueba();

        EquipoRequest request = new EquipoRequest(
                null,
                " Nuevo nombre ",
                null,
                null,
                null,
                null
        );

        when(equipoRepository.findById(1))
                .thenReturn(Optional.of(equipo));

        when(equipoRepository.save(equipo))
                .thenReturn(equipo);

        // Act
        EquipoResponse respuesta =
                service.actualizarParcial(1, request);

        // Assert
        assertThat(respuesta.id()).isEqualTo(1);
        assertThat(respuesta.nombre()).isEqualTo("Nuevo nombre");
        assertThat(respuesta.codigoInventario()).isEqualTo("EQ-001");
        assertThat(respuesta.tipo()).isEqualTo("Portátil");
        assertThat(respuesta.marca()).isEqualTo("Lenovo");
        assertThat(respuesta.modelo()).isEqualTo("ThinkPad");

        assertThat(respuesta.estado())
                .isEqualTo(EstadoEquipo.DISPONIBLE);

        verify(equipoRepository).save(equipo);
    }

    // 11. Actualización parcial inválida

    @Test
    void actualizarParcial_nombreVacio_lanzaExcepcionBadRequest() {

        // Arrange
        EquipoEntity equipo = crearEquipoPrueba();

        EquipoRequest request = new EquipoRequest(
                null,
                "   ",
                null,
                null,
                null,
                null
        );

        when(equipoRepository.findById(1))
                .thenReturn(Optional.of(equipo));

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.actualizarParcial(1, request)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(excepcion.getReason())
                .isEqualTo("El nombre no puede estar vacío");

        assertThat(equipo.getNombre())
                .isEqualTo("Computador portátil");

        verify(equipoRepository, never())
                .save(any(EquipoEntity.class));
    }

    // 12. Eliminar equipo sin asociaciones

    @Test
    void eliminar_equipoSinAsociaciones_borraElEquipo() {

        // Arrange
        EquipoEntity equipo = crearEquipoPrueba();

        when(equipoRepository.findById(1))
                .thenReturn(Optional.of(equipo));

        when(prestamoRepository.existsByEquipoId(1))
                .thenReturn(false);

        when(mantenimientoRepository.existsByEquipoId(1))
                .thenReturn(false);

        // Act
        service.eliminar(1);

        // Assert
        verify(equipoRepository).delete(equipo);
    }

    // 13. Eliminar equipo con préstamos asociados

    @Test
    void eliminar_equipoConPrestamos_lanzaExcepcionConflict() {

        // Arrange
        EquipoEntity equipo = crearEquipoPrueba();

        when(equipoRepository.findById(1))
                .thenReturn(Optional.of(equipo));

        when(prestamoRepository.existsByEquipoId(1))
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
                        "No se puede eliminar el equipo porque tiene préstamos asociados"
                );

        verify(equipoRepository, never())
                .delete(any(EquipoEntity.class));

        verifyNoInteractions(mantenimientoRepository);
    }

    // 14. Eliminar equipo con mantenimientos asociados

    @Test
    void eliminar_equipoConMantenimientos_lanzaExcepcionConflict() {

        // Arrange
        EquipoEntity equipo = crearEquipoPrueba();

        when(equipoRepository.findById(1))
                .thenReturn(Optional.of(equipo));

        when(prestamoRepository.existsByEquipoId(1))
                .thenReturn(false);

        when(mantenimientoRepository.existsByEquipoId(1))
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
                        "No se puede eliminar el equipo porque tiene mantenimientos asociados"
                );

        verify(equipoRepository, never())
                .delete(any(EquipoEntity.class));
    }

    // 15. Eliminar equipo inexistente

    @Test
    void eliminar_equipoInexistente_lanzaExcepcionNotFound() {

        // Arrange
        when(equipoRepository.findById(999))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.eliminar(999)
        );

        // Assert
        assertThat(excepcion.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(excepcion.getReason())
                .isEqualTo("No se encontró el equipo con ID 999");

        verify(equipoRepository, never())
                .delete(any(EquipoEntity.class));

        verifyNoInteractions(
                prestamoRepository,
                mantenimientoRepository
        );
    }
}
