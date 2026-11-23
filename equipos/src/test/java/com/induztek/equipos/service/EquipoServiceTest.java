package com.induztek.equipos.service;

import com.induztek.equipos.dto.UbicacionRequest;
import com.induztek.equipos.model.Equipo;
import com.induztek.equipos.repository.EquipoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipoServiceTest {

    @Mock
    private EquipoRepository repository;

    @InjectMocks
    private EquipoService service;

    private Equipo crearEquipoEjemplo() {
        return new Equipo(1L, "EQ-001", "Compresor", "Planta Norte", "Av. Industrial 100", -33.45, -70.66, 10L);
    }

    @Test
    void listarTodos_retornaListaCompleta() {
        List<Equipo> esperado = List.of(crearEquipoEjemplo());
        when(repository.findAll()).thenReturn(esperado);

        List<Equipo> resultado = service.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("EQ-001", resultado.get(0).getCodigo());
    }

    @Test
    void listarPorCliente_retornaEquiposDelCliente() {
        List<Equipo> esperado = List.of(crearEquipoEjemplo());
        when(repository.findByIdCliente(10L)).thenReturn(esperado);

        List<Equipo> resultado = service.listarPorCliente(10L);

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).getIdCliente());
    }

    @Test
    void buscarPorId_existenteYNoExistente() {
        Equipo equipo = crearEquipoEjemplo();
        when(repository.findById(1L)).thenReturn(Optional.of(equipo));
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(service.buscarPorId(1L).isPresent());
        assertTrue(service.buscarPorId(99L).isEmpty());
    }

    @Test
    void guardar_exitoso_cuandoCodigoNoExiste() {
        Equipo nuevo = crearEquipoEjemplo();
        when(repository.existsByCodigo("EQ-001")).thenReturn(false);
        when(repository.save(nuevo)).thenReturn(nuevo);

        Equipo guardado = service.guardar(nuevo);

        assertEquals("EQ-001", guardado.getCodigo());
        verify(repository).save(nuevo);
    }

    @Test
    void guardar_codigoDuplicado_lanzaIllegalArgumentException() {
        Equipo duplicado = crearEquipoEjemplo();
        when(repository.existsByCodigo("EQ-001")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.guardar(duplicado));
        assertTrue(ex.getMessage().contains("EQ-001"));
        verify(repository, never()).save(any());
    }

    @Test
    void actualizar_existente_modificaTodosLosCamposYGuarda() {
        Equipo existente = crearEquipoEjemplo();
        Equipo datosNuevos = new Equipo(null, "EQ-002", "Caldera", "Planta Sur", "Ruta 5 Km 20", -36.82, -73.05, 20L);

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        Optional<Equipo> resultado = service.actualizar(1L, datosNuevos);

        assertTrue(resultado.isPresent());
        assertEquals("EQ-002", resultado.get().getCodigo());
        assertEquals("Caldera", resultado.get().getTipo());
        assertEquals("Planta Sur", resultado.get().getInstalacion());
        assertEquals("Ruta 5 Km 20", resultado.get().getDireccion());
        assertEquals(-36.82, resultado.get().getLatitud());
        assertEquals(-73.05, resultado.get().getLongitud());
        assertEquals(20L, resultado.get().getIdCliente());
    }

    @Test
    void actualizar_inexistente_retornaEmpty() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        Optional<Equipo> resultado = service.actualizar(99L, crearEquipoEjemplo());

        assertTrue(resultado.isEmpty());
    }

    @Test
    void actualizarUbicacion_existente_actualizaDireccionYCoordenadas() {
        Equipo existente = crearEquipoEjemplo();
        UbicacionRequest req = new UbicacionRequest();
        req.setDireccion("Nueva Ubicacion 450");
        req.setLatitud(-33.50);
        req.setLongitud(-70.70);

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        Optional<Equipo> resultado = service.actualizarUbicacion(1L, req);

        assertTrue(resultado.isPresent());
        assertEquals("Nueva Ubicacion 450", resultado.get().getDireccion());
        assertEquals(-33.50, resultado.get().getLatitud());
        assertEquals(-70.70, resultado.get().getLongitud());
    }

    @Test
    void eliminar_existenteYNoExistente() {
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.existsById(99L)).thenReturn(false);

        assertTrue(service.eliminar(1L));
        verify(repository).deleteById(1L);

        assertFalse(service.eliminar(99L));
        verify(repository, never()).deleteById(99L);
    }
}