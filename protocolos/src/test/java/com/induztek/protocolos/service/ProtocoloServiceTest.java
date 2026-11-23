package com.induztek.protocolos.service;

import com.induztek.protocolos.client.EquipoClient;
import com.induztek.protocolos.client.UsuarioClient;
import com.induztek.protocolos.dto.EquipoExternoDTO;
import com.induztek.protocolos.dto.ProtocoloDetalleResponse;
import com.induztek.protocolos.dto.UsuarioExternoDTO;
import com.induztek.protocolos.model.Protocolo;
import com.induztek.protocolos.repository.ProtocoloRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProtocoloServiceTest {

    @Mock
    private ProtocoloRepository repository;

    @Mock
    private EquipoClient equipoClient;

    @Mock
    private UsuarioClient usuarioClient;

    @InjectMocks
    private ProtocoloService service;

    private Protocolo crearProtocoloEjemplo() {
        return new Protocolo(1L, 10L, 20L, 1732400000L, "AISLACION",
                1200.0, 950.0, 880.0, 2.5, "APROBADO", "Sin novedades", "BORRADOR", null);
    }

    private EquipoExternoDTO crearEquipoDto(Long id, String codigo) {
        EquipoExternoDTO dto = new EquipoExternoDTO();
        dto.setIdEquipo(id);
        dto.setCodigo(codigo);
        dto.setTipo("Transformador");
        dto.setInstalacion("Subestación Norte");
        return dto;
    }

    private UsuarioExternoDTO crearUsuarioDto(String nombre) {
        UsuarioExternoDTO dto = new UsuarioExternoDTO();
        dto.setNombre(nombre);
        return dto;
    }

    @Test
    void enriquecerDetalle_conDatosExternosYConNulos() {
        Protocolo p = crearProtocoloEjemplo();

        // Caso 1: clientes responden con datos
        when(equipoClient.obtenerPorId(10L)).thenReturn(crearEquipoDto(10L, "TR-100"));
        when(usuarioClient.obtenerPorId(20L)).thenReturn(crearUsuarioDto("Carlos Pérez"));

        ProtocoloDetalleResponse lleno = service.enriquecerDetalle(p);
        assertEquals("TR-100", lleno.getCodigoEquipo());
        assertEquals("Transformador", lleno.getTipoEquipo());
        assertEquals("Subestación Norte", lleno.getInstalacion());
        assertEquals("Carlos Pérez", lleno.getNombreTecnico());

        // Caso 2: clientes retornan null (valores por defecto)
        when(equipoClient.obtenerPorId(10L)).thenReturn(null);
        when(usuarioClient.obtenerPorId(20L)).thenReturn(null);

        ProtocoloDetalleResponse fallback = service.enriquecerDetalle(p);
        assertEquals("Equipo #10", fallback.getCodigoEquipo());
        assertEquals("Desconocido", fallback.getTipoEquipo());
        assertEquals("Sin instalación", fallback.getInstalacion());
        assertEquals("Técnico #20", fallback.getNombreTecnico());
    }

    @Test
    void listarTodosYBuscarPorId_retornanDetalles() {
        Protocolo p = crearProtocoloEjemplo();
        when(repository.findAll()).thenReturn(List.of(p));
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        when(repository.findById(99L)).thenReturn(Optional.empty());
        when(equipoClient.obtenerPorId(10L)).thenReturn(crearEquipoDto(10L, "TR-100"));
        when(usuarioClient.obtenerPorId(20L)).thenReturn(crearUsuarioDto("Carlos Pérez"));

        assertEquals(1, service.listarTodos().size());
        assertTrue(service.buscarPorId(1L).isPresent());
        assertTrue(service.buscarPorId(99L).isEmpty());
    }

    @Test
    void listarPorEquipo_retornaProtocolosDelEquipo() {
        Protocolo p = crearProtocoloEjemplo();
        when(repository.findByIdEquipoOrderByIdProtocoloDesc(10L)).thenReturn(List.of(p));
        when(equipoClient.obtenerPorId(10L)).thenReturn(crearEquipoDto(10L, "TR-100"));
        when(usuarioClient.obtenerPorId(20L)).thenReturn(crearUsuarioDto("Carlos Pérez"));

        List<ProtocoloDetalleResponse> resultado = service.listarPorEquipo(10L);

        assertEquals(1, resultado.size());
        assertEquals("TR-100", resultado.get(0).getCodigoEquipo());
    }

    @Test
    void listarPorCliente_conEquiposYSinEquipos() {
        // Caso sin equipos
        when(equipoClient.listarPorCliente(5L)).thenReturn(Collections.emptyList());
        assertTrue(service.listarPorCliente(5L).isEmpty());

        // Caso equipos null
        when(equipoClient.listarPorCliente(6L)).thenReturn(null);
        assertTrue(service.listarPorCliente(6L).isEmpty());

        // Caso con equipos
        Protocolo p = crearProtocoloEjemplo();
        when(equipoClient.listarPorCliente(7L)).thenReturn(List.of(crearEquipoDto(10L, "TR-100")));
        when(repository.findByIdEquipoInOrderByIdProtocoloDesc(List.of(10L))).thenReturn(List.of(p));
        when(equipoClient.obtenerPorId(10L)).thenReturn(crearEquipoDto(10L, "TR-100"));
        when(usuarioClient.obtenerPorId(20L)).thenReturn(crearUsuarioDto("Carlos Pérez"));

        List<ProtocoloDetalleResponse> resultado = service.listarPorCliente(7L);
        assertEquals(1, resultado.size());
    }

    @Test
    void guardar_asignaEstadoPorDefectoSiEsNuloOVacio() {
        Protocolo sinEstado = crearProtocoloEjemplo();
        sinEstado.setEstado(" ");
        when(repository.save(sinEstado)).thenReturn(sinEstado);

        Protocolo guardado = service.guardar(sinEstado);
        assertEquals("REGISTRADO_EN_TERRENO", guardado.getEstado());

        Protocolo conEstado = crearProtocoloEjemplo();
        conEstado.setEstado("BORRADOR");
        when(repository.save(conEstado)).thenReturn(conEstado);

        assertEquals("BORRADOR", service.guardar(conEstado).getEstado());
    }

    @Test
    void formalizar_existenteYNoExistente() {
        Protocolo p = crearProtocoloEjemplo();
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        when(repository.findById(99L)).thenReturn(Optional.empty());
        when(repository.save(p)).thenReturn(p);

        Optional<Protocolo> formalizado = service.formalizar(1L);
        assertTrue(formalizado.isPresent());
        assertEquals("FORMALIZADO", formalizado.get().getEstado());

        assertTrue(service.formalizar(99L).isEmpty());
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