package com.induztek.protocolos.controller;

import com.induztek.protocolos.dto.ProtocoloDetalleResponse;
import com.induztek.protocolos.model.Protocolo;
import com.induztek.protocolos.service.ProtocoloService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProtocoloControllerTest {

    @Mock
    private ProtocoloService service;

    @InjectMocks
    private ProtocoloController controller;

    private Protocolo protocoloEjemplo() {
        return new Protocolo(1L, 10L, 20L, 1732400000L, "AISLACION",
                1200.0, 950.0, 880.0, 2.5, "APROBADO", "OK", "BORRADOR", null);
    }

    private ProtocoloDetalleResponse detalleEjemplo() {
        return new ProtocoloDetalleResponse(protocoloEjemplo(), "TR-100", "Transformador", "Planta Norte", "Carlos");
    }

    @Test
    void listados_delegaEnServicio() {
        List<ProtocoloDetalleResponse> lista = List.of(detalleEjemplo());
        when(service.listarTodos()).thenReturn(lista);
        when(service.listarPorEquipo(10L)).thenReturn(lista);
        when(service.listarPorCliente(5L)).thenReturn(lista);

        assertEquals(lista, controller.listarTodos().getBody());
        assertEquals(lista, controller.listarPorEquipo(10L).getBody());
        assertEquals(lista, controller.listarPorCliente(5L).getBody());
    }

    @Test
    void obtenerPorId_encontradoYNoEncontrado() {
        ProtocoloDetalleResponse detalle = detalleEjemplo();
        when(service.buscarPorId(1L)).thenReturn(Optional.of(detalle));
        when(service.buscarPorId(99L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.OK, controller.obtenerPorId(1L).getStatusCode());
        assertEquals(detalle, controller.obtenerPorId(1L).getBody());
        assertEquals(HttpStatus.NOT_FOUND, controller.obtenerPorId(99L).getStatusCode());
    }

    @Test
    void crearYFormalizar_retornanEstadosEsperados() {
        Protocolo p = protocoloEjemplo();
        when(service.guardar(p)).thenReturn(p);
        when(service.formalizar(1L)).thenReturn(Optional.of(p));
        when(service.formalizar(99L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.CREATED, controller.crear(p).getStatusCode());
        assertEquals(HttpStatus.OK, controller.formalizar(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.formalizar(99L).getStatusCode());
    }

    @Test
    void eliminar_exitosoYNoEncontrado() {
        when(service.eliminar(1L)).thenReturn(true);
        when(service.eliminar(99L)).thenReturn(false);

        assertEquals(HttpStatus.NO_CONTENT, controller.eliminar(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.eliminar(99L).getStatusCode());
    }
}