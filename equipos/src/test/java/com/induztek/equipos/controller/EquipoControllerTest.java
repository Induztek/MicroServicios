package com.induztek.equipos.controller;

import com.induztek.equipos.dto.UbicacionRequest;
import com.induztek.equipos.model.Equipo;
import com.induztek.equipos.service.EquipoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipoControllerTest {

    @Mock
    private EquipoService service;

    @InjectMocks
    private EquipoController controller;

    private Equipo ejemplo() {
        return new Equipo(1L, "EQ-001", "Compresor", "Planta Norte", "Av. Industrial 100", -33.45, -70.66, 10L);
    }

    @Test
    void listarTodosYPorCliente_retornanOk() {
        List<Equipo> lista = List.of(ejemplo());
        when(service.listarTodos()).thenReturn(lista);
        when(service.listarPorCliente(10L)).thenReturn(lista);

        assertEquals(HttpStatus.OK, controller.listarTodos().getStatusCode());
        assertEquals(lista, controller.listarTodos().getBody());
        assertEquals(lista, controller.listarPorCliente(10L).getBody());
    }

    @Test
    void obtenerPorId_encontradoYNoEncontrado() {
        Equipo eq = ejemplo();
        when(service.buscarPorId(1L)).thenReturn(Optional.of(eq));
        when(service.buscarPorId(99L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.OK, controller.obtenerPorId(1L).getStatusCode());
        assertEquals(eq, controller.obtenerPorId(1L).getBody());
        assertEquals(HttpStatus.NOT_FOUND, controller.obtenerPorId(99L).getStatusCode());
    }

    @Test
    void crear_exitosoYConflicto() {
        Equipo eq = ejemplo();
        when(service.guardar(eq)).thenReturn(eq);

        ResponseEntity<?> respOk = controller.crear(eq);
        assertEquals(HttpStatus.CREATED, respOk.getStatusCode());
        assertEquals(eq, respOk.getBody());

        when(service.guardar(eq)).thenThrow(new IllegalArgumentException("Código duplicado"));
        ResponseEntity<?> respConflict = controller.crear(eq);
        assertEquals(HttpStatus.CONFLICT, respConflict.getStatusCode());
        assertEquals("Código duplicado", respConflict.getBody());
    }

    @Test
    void actualizar_encontradoYNoEncontrado() {
        Equipo eq = ejemplo();
        when(service.actualizar(1L, eq)).thenReturn(Optional.of(eq));
        when(service.actualizar(99L, eq)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.OK, controller.actualizar(1L, eq).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.actualizar(99L, eq).getStatusCode());
    }

    @Test
    void actualizarUbicacion_encontradoYNoEncontrado() {
        Equipo eq = ejemplo();
        UbicacionRequest req = new UbicacionRequest();
        when(service.actualizarUbicacion(1L, req)).thenReturn(Optional.of(eq));
        when(service.actualizarUbicacion(99L, req)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.OK, controller.actualizarUbicacion(1L, req).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.actualizarUbicacion(99L, req).getStatusCode());
    }

    @Test
    void eliminar_exitosoYNoEncontrado() {
        when(service.eliminar(1L)).thenReturn(true);
        when(service.eliminar(99L)).thenReturn(false);

        assertEquals(HttpStatus.NO_CONTENT, controller.eliminar(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.eliminar(99L).getStatusCode());
    }
}