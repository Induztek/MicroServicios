package com.induztek.protocolos.controller;

import com.induztek.protocolos.dto.ProtocoloDetalleResponse;
import com.induztek.protocolos.model.Protocolo;
import com.induztek.protocolos.service.ProtocoloService;
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
@RequestMapping("/api/protocolos")
@RequiredArgsConstructor
@Tag(name = "Protocolos", description = "Gestión de protocolos de pruebas eléctricas en terreno y formalización")
public class ProtocoloController {

    private final ProtocoloService service;

    @Operation(
            summary = "Listar todos los protocolos",
            description = "Retorna todos los protocolos enriquecidos con los datos del equipo y del técnico."
    )
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    @GetMapping
    public ResponseEntity<List<ProtocoloDetalleResponse>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @Operation(
            summary = "Obtener protocolo por ID",
            description = "Retorna el detalle completo de un protocolo específico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Protocolo encontrado"),
            @ApiResponse(responseCode = "404", description = "Protocolo no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProtocoloDetalleResponse> obtenerPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Listar protocolos por equipo",
            description = "Retorna el historial de protocolos asociados a un equipo ordenado del más reciente al más antiguo."
    )
    @ApiResponse(responseCode = "200", description = "Historial obtenido correctamente")
    @GetMapping("/equipo/{idEquipo}")
    public ResponseEntity<List<ProtocoloDetalleResponse>> listarPorEquipo(@PathVariable Long idEquipo) {
        return ResponseEntity.ok(service.listarPorEquipo(idEquipo));
    }

    @Operation(
            summary = "Listar protocolos por cliente",
            description = "Consulta los equipos pertenecientes al cliente y retorna sus protocolos asociados."
    )
    @ApiResponse(responseCode = "200", description = "Protocolos del cliente obtenidos correctamente")
    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<ProtocoloDetalleResponse>> listarPorCliente(@PathVariable Long idCliente) {
        return ResponseEntity.ok(service.listarPorCliente(idCliente));
    }

    @Operation(
            summary = "Registrar nuevo protocolo",
            description = "Guarda un nuevo protocolo de prueba eléctrica (asigna estado REGISTRADO_EN_TERRENO por defecto si no se especifica)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Protocolo creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o incompletos")
    })
    @PostMapping
    public ResponseEntity<Protocolo> crear(@Valid @RequestBody Protocolo protocolo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(protocolo));
    }

    @Operation(
            summary = "Formalizar protocolo",
            description = "Cambia el estado de un protocolo existente a FORMALIZADO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Protocolo formalizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Protocolo no encontrado")
    })
    @PatchMapping("/{id}/formalizar")
    public ResponseEntity<Protocolo> formalizar(@PathVariable Long id) {
        return service.formalizar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Eliminar protocolo",
            description = "Elimina un protocolo del sistema según su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Protocolo eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Protocolo no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!service.eliminar(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}