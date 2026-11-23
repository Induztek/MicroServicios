package com.induztek.auth.controller;

import com.induztek.auth.dto.DatosCambioContrasena;
import com.induztek.auth.dto.DatosCredencial;
import com.induztek.auth.dto.DatosLogin;
import com.induztek.auth.dto.DatosOlvide;
import com.induztek.auth.dto.DatosRestablecer;
import com.induztek.auth.dto.RespuestaLogin;
import com.induztek.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController controller;

    @Test
    void iniciarSesion_delegaEnServicio() {
        DatosLogin datos = new DatosLogin("a@b.cl", "123456");
        RespuestaLogin esperado = new RespuestaLogin("tok", 1L, 2L, false);
        when(authService.iniciarSesion(datos)).thenReturn(esperado);

        RespuestaLogin actual = controller.iniciarSesion(datos);

        assertEquals(esperado, actual);
    }

    @Test
    void crearCredencial_delegaEnServicio() {
        DatosCredencial datos = new DatosCredencial(1L, "a@b.cl", "123456");
        controller.crearCredencial(datos);
        verify(authService).crearCredencial(datos);
    }

    @Test
    void configurar_delegaEnServicio() {
        DatosCambioContrasena datos = new DatosCambioContrasena("a@b.cl", "123456", "654321");
        controller.configurar(datos);
        verify(authService).configurar(datos);
    }

    @Test
    void olvide_delegaEnServicio() {
        DatosOlvide datos = new DatosOlvide("a@b.cl");
        controller.olvide(datos);
        verify(authService).olvide(datos);
    }

    @Test
    void restablecer_delegaEnServicio() {
        DatosRestablecer datos = new DatosRestablecer("a@b.cl", "111222", "654321");
        controller.restablecer(datos);
        verify(authService).restablecer(datos);
    }
}