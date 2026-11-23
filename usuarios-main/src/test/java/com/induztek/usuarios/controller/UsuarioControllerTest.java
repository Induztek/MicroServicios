package com.induztek.usuarios.controller;

import com.induztek.usuarios.dto.DatosActualizacion;
import com.induztek.usuarios.dto.DatosRegistro;
import com.induztek.usuarios.dto.UsuarioRespuesta;
import com.induztek.usuarios.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController controller;

    private UsuarioRespuesta ejemplo() {
        return new UsuarioRespuesta(1L, "Ana", "Soto", "11-1", "999", 10L, "Induztek", 1L, "Técnico", null);
    }

    @Test
    void listar_delegaEnServicio() {
        List<UsuarioRespuesta> esperado = List.of(ejemplo());
        when(usuarioService.listar("Ana")).thenReturn(esperado);

        assertEquals(esperado, controller.listar("Ana"));
    }

    @Test
    void obtener_delegaEnServicio() {
        UsuarioRespuesta esperado = ejemplo();
        when(usuarioService.obtener(1L)).thenReturn(esperado);

        assertEquals(esperado, controller.obtener(1L));
    }

    @Test
    void registrar_delegaEnServicio() {
        DatosRegistro datos = new DatosRegistro("Ana", "Soto", "11-1", "999", 10L, 1L);
        UsuarioRespuesta esperado = ejemplo();
        when(usuarioService.registrar(datos)).thenReturn(esperado);

        assertEquals(esperado, controller.registrar(datos));
    }

    @Test
    void actualizar_delegaEnServicio() {
        DatosActualizacion datos = new DatosActualizacion("Ana", "Soto", "11-1", "999", 10L, 1L);
        UsuarioRespuesta esperado = ejemplo();
        when(usuarioService.actualizar(1L, datos)).thenReturn(esperado);

        assertEquals(esperado, controller.actualizar(1L, datos));
    }

    @Test
    void eliminar_delegaEnServicio() {
        controller.eliminar(1L);
        verify(usuarioService).eliminar(1L);
    }
}