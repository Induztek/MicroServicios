package com.induztek.auth.controller;

import com.induztek.auth.dto.DatosCambioContrasena;
import com.induztek.auth.dto.DatosCredencial;
import com.induztek.auth.dto.DatosLogin;
import com.induztek.auth.dto.DatosOlvide;
import com.induztek.auth.dto.DatosRestablecer;
import com.induztek.auth.dto.RespuestaLogin;
import com.induztek.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Gestión de inicio de sesión JWT, credenciales y recuperación de cuenta")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica al usuario mediante correo y contraseña, retornando el token JWT y sus datos."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso"),
            @ApiResponse(responseCode = "401", description = "Correo o contraseña incorrectos")
    })
    @PostMapping("/login")
    public RespuestaLogin iniciarSesion(@RequestBody DatosLogin datos) {
        return authService.iniciarSesion(datos);
    }

    @Operation(
            summary = "Crear credencial de acceso",
            description = "Registra una credencial inicial (generada por Administrador o Supervisor) para un usuario nuevo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Credencial creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o credencial ya existente")
    })
    @PostMapping("/credenciales")
    @ResponseStatus(HttpStatus.CREATED)
    public void crearCredencial(@RequestBody DatosCredencial datos) {
        authService.crearCredencial(datos);
    }

    @Operation(
            summary = "Configurar cuenta en primer ingreso",
            description = "Actualiza la contraseña temporal por una definitiva y guarda la pregunta/respuesta de seguridad."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cuenta configurada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o contraseña actual incorrecta")
    })
    @PostMapping("/configurar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void configurar(@RequestBody DatosCambioContrasena datos) {
        authService.configurar(datos);
    }

    @Operation(
            summary = "Solicitar recuperación de contraseña",
            description = "Inicia el flujo de recuperación de acceso a partir del correo electrónico del usuario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Solicitud procesada correctamente"),
            @ApiResponse(responseCode = "404", description = "Correo no registrado")
    })
    @PostMapping("/olvide")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void olvide(@RequestBody DatosOlvide datos) {
        authService.olvide(datos);
    }

    @Operation(
            summary = "Restablecer contraseña",
            description = "Asigna una nueva contraseña tras validar la respuesta de seguridad o el token de restablecimiento."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contraseña restablecida correctamente"),
            @ApiResponse(responseCode = "400", description = "Validación de seguridad fallida o token inválido")
    })
    @PostMapping("/restablecer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restablecer(@RequestBody DatosRestablecer datos) {
        authService.restablecer(datos);
    }
}