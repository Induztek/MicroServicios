package com.induztek.auth.service;

import com.induztek.auth.client.UsuarioClient;
import com.induztek.auth.dto.DatosCambioContrasena;
import com.induztek.auth.dto.DatosCredencial;
import com.induztek.auth.dto.DatosLogin;
import com.induztek.auth.dto.DatosOlvide;
import com.induztek.auth.dto.DatosRestablecer;
import com.induztek.auth.dto.RespuestaLogin;
import com.induztek.auth.dto.UsuarioDto;
import com.induztek.auth.model.Credencial;
import com.induztek.auth.model.TokenRestablecimiento;
import com.induztek.auth.repository.CredencialRepository;
import com.induztek.auth.repository.TokenRestablecimientoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CredencialRepository credencialRepository;

    @Mock
    private TokenRestablecimientoRepository tokenRepository;

    @Mock
    private PasswordEncoder cifradorContrasenas;

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioClient usuarioClient;

    @InjectMocks
    private AuthService authService;

    @Test
    void iniciarSesion_exitoso_retornaRespuestaLogin() {
        Credencial cred = Credencial.builder()
                .usuarioId(1L)
                .correo("admin@induztek.cl")
                .contrasena("hash")
                .primerIngreso(true)
                .build();
        UsuarioDto usuario = new UsuarioDto(1L, 3L);

        when(credencialRepository.findByCorreo("admin@induztek.cl")).thenReturn(Optional.of(cred));
        when(cifradorContrasenas.matches("123456", "hash")).thenReturn(true);
        when(usuarioClient.obtener(1L)).thenReturn(usuario);
        when(jwtService.crearToken(1L, 3L)).thenReturn("jwt-token");

        RespuestaLogin resp = authService.iniciarSesion(new DatosLogin("admin@induztek.cl", "123456"));

        assertEquals("jwt-token", resp.token());
        assertEquals(1L, resp.usuarioId());
        assertEquals(3L, resp.rolId());
        assertTrue(resp.primerIngreso());
    }

    @Test
    void iniciarSesion_contrasenaIncorrecta_lanzaUnauthorized() {
        Credencial cred = Credencial.builder().correo("admin@induztek.cl").contrasena("hash").build();
        when(credencialRepository.findByCorreo("admin@induztek.cl")).thenReturn(Optional.of(cred));
        when(cifradorContrasenas.matches("mala", "hash")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.iniciarSesion(new DatosLogin("admin@induztek.cl", "mala")));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void iniciarSesion_usuarioNoExisteEnMicroservicio_lanzaUnauthorized() {
        Credencial cred = Credencial.builder().usuarioId(99L).correo("x@induztek.cl").contrasena("hash").build();
        when(credencialRepository.findByCorreo("x@induztek.cl")).thenReturn(Optional.of(cred));
        when(cifradorContrasenas.matches("123456", "hash")).thenReturn(true);
        when(usuarioClient.obtener(99L)).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.iniciarSesion(new DatosLogin("x@induztek.cl", "123456")));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void crearCredencial_exitoso_guardaConPrimerIngresoTrue() {
        DatosCredencial datos = new DatosCredencial(5L, "nuevo@induztek.cl", "clave123");
        when(credencialRepository.existsByCorreo("nuevo@induztek.cl")).thenReturn(false);
        when(credencialRepository.existsByUsuarioId(5L)).thenReturn(false);
        when(usuarioClient.obtener(5L)).thenReturn(new UsuarioDto(5L, 1L));
        when(cifradorContrasenas.encode("clave123")).thenReturn("hash-clave");

        authService.crearCredencial(datos);

        verify(credencialRepository).save(argThat(c ->
                c.getUsuarioId().equals(5L) &&
                c.getCorreo().equals("nuevo@induztek.cl") &&
                c.getContrasena().equals("hash-clave") &&
                c.isPrimerIngreso()
        ));
    }

    @Test
    void crearCredencial_datosVaciosOContrasenaCorta_lanzaBadRequest() {
        assertThrows(ResponseStatusException.class,
                () -> authService.crearCredencial(new DatosCredencial(null, "a@b.cl", "123456")));
        assertThrows(ResponseStatusException.class,
                () -> authService.crearCredencial(new DatosCredencial(1L, " ", "123456")));
        assertThrows(ResponseStatusException.class,
                () -> authService.crearCredencial(new DatosCredencial(1L, "a@b.cl", "123")));
    }

    @Test
    void crearCredencial_correoOUsuarioDuplicado_lanzaConflict() {
        DatosCredencial datos = new DatosCredencial(5L, "dup@induztek.cl", "clave123");

        when(credencialRepository.existsByCorreo("dup@induztek.cl")).thenReturn(true);
        ResponseStatusException exCorreo = assertThrows(ResponseStatusException.class,
                () -> authService.crearCredencial(datos));
        assertEquals(HttpStatus.CONFLICT, exCorreo.getStatusCode());

        when(credencialRepository.existsByCorreo("dup@induztek.cl")).thenReturn(false);
        when(credencialRepository.existsByUsuarioId(5L)).thenReturn(true);
        ResponseStatusException exUsuario = assertThrows(ResponseStatusException.class,
                () -> authService.crearCredencial(datos));
        assertEquals(HttpStatus.CONFLICT, exUsuario.getStatusCode());
    }

    @Test
    void crearCredencial_usuarioInexistente_lanzaBadRequest() {
        DatosCredencial datos = new DatosCredencial(88L, "ok@induztek.cl", "clave123");
        when(credencialRepository.existsByCorreo("ok@induztek.cl")).thenReturn(false);
        when(credencialRepository.existsByUsuarioId(88L)).thenReturn(false);
        when(usuarioClient.obtener(88L)).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.crearCredencial(datos));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void configurar_exitoso_actualizaContrasenaYDesactivaPrimerIngreso() {
        Credencial cred = Credencial.builder()
                .correo("tec@induztek.cl")
                .contrasena("hash-temp")
                .primerIngreso(true)
                .build();

        when(credencialRepository.findByCorreo("tec@induztek.cl")).thenReturn(Optional.of(cred));
        when(cifradorContrasenas.matches("temp12", "hash-temp")).thenReturn(true);
        when(cifradorContrasenas.encode("nueva123")).thenReturn("hash-nueva");

        authService.configurar(new DatosCambioContrasena("tec@induztek.cl", "temp12", "nueva123"));

        assertEquals("hash-nueva", cred.getContrasena());
        assertFalse(cred.isPrimerIngreso());
        verify(credencialRepository).save(cred);
    }

    @Test
    void configurar_contrasenaActualIncorrecta_lanzaUnauthorized() {
        when(credencialRepository.findByCorreo("tec@induztek.cl")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.configurar(new DatosCambioContrasena("tec@induztek.cl", "mal", "nueva123")));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void olvide_cuandoCorreoExiste_generaYGuardaToken() {
        Credencial cred = Credencial.builder().usuarioId(3L).correo("user@induztek.cl").build();
        when(credencialRepository.findByCorreo("user@induztek.cl")).thenReturn(Optional.of(cred));
        when(cifradorContrasenas.encode(anyString())).thenReturn("hash-codigo");

        authService.olvide(new DatosOlvide("user@induztek.cl"));

        verify(tokenRepository).save(any(TokenRestablecimiento.class));
    }

    @Test
    void olvide_cuandoCorreoNoExiste_noGuardaToken() {
        when(credencialRepository.findByCorreo("no@induztek.cl")).thenReturn(Optional.empty());

        authService.olvide(new DatosOlvide("no@induztek.cl"));

        verify(tokenRepository, never()).save(any());
    }

    @Test
    void restablecer_codigoValido_marcaTokenUsadoYActualizaClave() {
        Credencial cred = Credencial.builder()
                .usuarioId(3L)
                .correo("user@induztek.cl")
                .contrasena("vieja")
                .primerIngreso(true)
                .build();
        TokenRestablecimiento token = TokenRestablecimiento.builder()
                .usuarioId(3L)
                .codigo("hash-123456")
                .venceEn(LocalDateTime.now().plusMinutes(10))
                .usado(false)
                .build();

        when(credencialRepository.findByCorreo("user@induztek.cl")).thenReturn(Optional.of(cred));
        when(tokenRepository.findByUsuarioIdAndUsadoFalse(3L)).thenReturn(List.of(token));
        when(cifradorContrasenas.matches("123456", "hash-123456")).thenReturn(true);
        when(cifradorContrasenas.encode("claveNueva1")).thenReturn("hash-claveNueva1");

        authService.restablecer(new DatosRestablecer("user@induztek.cl", "123456", "claveNueva1"));

        assertTrue(token.isUsado());
        assertFalse(cred.isPrimerIngreso());
        assertEquals("hash-claveNueva1", cred.getContrasena());
        verify(tokenRepository).save(token);
        verify(credencialRepository).save(cred);
    }

    @Test
    void restablecer_codigoVencidoOInvalido_lanzaBadRequest() {
        Credencial cred = Credencial.builder().usuarioId(3L).correo("user@induztek.cl").build();
        TokenRestablecimiento tokenVencido = TokenRestablecimiento.builder()
                .usuarioId(3L)
                .codigo("hash-123456")
                .venceEn(LocalDateTime.now().minusMinutes(5))
                .usado(false)
                .build();

        when(credencialRepository.findByCorreo("user@induztek.cl")).thenReturn(Optional.of(cred));
        when(tokenRepository.findByUsuarioIdAndUsadoFalse(3L)).thenReturn(List.of(tokenVencido));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.restablecer(new DatosRestablecer("user@induztek.cl", "123456", "claveNueva1")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}