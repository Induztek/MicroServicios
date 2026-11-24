package com.induztek.equipos.controller;

import com.induztek.equipos.dto.AsignarTecnicoRequest;
import com.induztek.equipos.dto.UbicacionRequest;
import com.induztek.equipos.model.Equipo;
import com.induztek.equipos.service.EquipoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipos")
@RequiredArgsConstructor
@Tag(name = "Equipos", description = "Gestión de equipos industriales, asignación por cliente y ubicación")
public class EquipoController {

    private final EquipoService service;

    @Operation(
            summary = "Listar todos los equipos",
            description = "Retorna el catálogo completo de equipos registrados en el sistema."
    )
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    @GetMapping
    public ResponseEntity<List<Equipo>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @Operation(
            summary = "Listar equipos por cliente",
            description = "Retorna todos los equipos asociados a un identificador de cliente o empresa."
    )
    @ApiResponse(responseCode = "200", description = "Listado filtrado obtenido correctamente")
    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<Equipo>> listarPorCliente(@PathVariable Long idCliente) {
        return ResponseEntity.ok(service.listarPorCliente(idCliente));
    }

    @Operation(
            summary = "Listar equipos por técnico",
            description = "Retorna los equipos asignados a un técnico."
    )
    @ApiResponse(responseCode = "200", description = "Listado filtrado obtenido correctamente")
    @GetMapping("/tecnico/{idTecnico}")
    public ResponseEntity<List<Equipo>> listarPorTecnico(@PathVariable Long idTecnico) {
        return ResponseEntity.ok(service.listarPorTecnico(idTecnico));
    }

    @Operation(
            summary = "Obtener equipo por ID",
            description = "Busca y retorna el detalle de un equipo según su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipo encontrado"),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Equipo> obtenerPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Registrar nuevo equipo",
            description = "Crea un nuevo equipo validando que no existan conflictos de serie o código."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Equipo registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Conflicto: equipo o número de serie ya registrado")
    })
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Equipo equipo) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(equipo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @Operation(
            summary = "Actualizar equipo",
            description = "Actualiza los datos generales de un equipo existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipo actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Equipo> actualizar(@PathVariable Long id, @Valid @RequestBody Equipo datos) {
        return service.actualizar(id, datos)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Actualizar ubicación de equipo",
            description = "Actualiza parcialmente las coordenadas o ubicación física de un equipo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ubicación actualizada correctamente"),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado")
    })
    @PatchMapping("/{id}/ubicacion")
    public ResponseEntity<Equipo> actualizarUbicacion(@PathVariable Long id, @RequestBody UbicacionRequest req) {
        return service.actualizarUbicacion(id, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Asignar técnico a equipo",
            description = "El supervisor asigna o quita (idTecnico = null) el técnico responsable del equipo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Técnico asignado correctamente"),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado")
    })
    @PatchMapping("/{id}/tecnico")
    public ResponseEntity<Equipo> asignarTecnico(@PathVariable Long id, @RequestBody AsignarTecnicoRequest req) {
        return service.asignarTecnico(id, req.getIdTecnico())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Eliminar equipo",
            description = "Elimina un equipo del inventario según su ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Equipo eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!service.eliminar(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}