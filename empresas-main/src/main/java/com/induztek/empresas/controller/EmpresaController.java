package com.induztek.empresas.controller;

import com.induztek.empresas.dto.DatosEmpresa;
import com.induztek.empresas.model.Empresa;
import com.induztek.empresas.service.EmpresaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
@Tag(name = "Empresas", description = "Administración de empresas clientes y organizaciones")
public class EmpresaController {

    private final EmpresaService empresaService;

    @Operation(
            summary = "Listar empresas",
            description = "Retorna el listado completo de empresas registradas."
    )
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    @GetMapping
    public List<Empresa> listar() {
        return empresaService.listar();
    }

    @Operation(
            summary = "Obtener empresa por ID",
            description = "Consulta el detalle de una empresa específica según su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
            @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    @GetMapping("/{id}")
    public Empresa obtener(@PathVariable Long id) {
        return empresaService.obtener(id);
    }

    @Operation(
            summary = "Crear nueva empresa",
            description = "Registra una nueva empresa en el sistema."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Empresa creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o incompletos"),
            @ApiResponse(responseCode = "409", description = "Ya existe una empresa con el mismo RUT o nombre")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Empresa crear(@RequestBody DatosEmpresa datos) {
        return empresaService.crear(datos);
    }

    @Operation(
            summary = "Actualizar empresa",
            description = "Modifica los datos de una empresa existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Empresa no encontrada"),
            @ApiResponse(responseCode = "409", description = "RUT o nombre duplicado")
    })
    @PutMapping("/{id}")
    public Empresa actualizar(@PathVariable Long id, @RequestBody DatosEmpresa datos) {
        return empresaService.actualizar(id, datos);
    }
}