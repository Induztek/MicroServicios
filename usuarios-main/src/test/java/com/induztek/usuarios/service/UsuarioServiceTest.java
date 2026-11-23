package com.induztek.usuarios.service;

import com.induztek.usuarios.client.EmpresaClient;
import com.induztek.usuarios.dto.DatosActualizacion;
import com.induztek.usuarios.dto.DatosRegistro;
import com.induztek.usuarios.dto.EmpresaDto;
import com.induztek.usuarios.dto.UsuarioRespuesta;
import com.induztek.usuarios.model.Usuario;
import com.induztek.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmpresaClient empresaClient;

    @InjectMocks
    private UsuarioService usuarioService;

    // --- listar ---

    @Test
    void listar_sinFiltro_retornaTodosConNombreEmpresaYRol() {
        Usuario u1 = Usuario.builder().id(1L).nombre("Ana").apellido("Soto").rut("11-1").telefono("999").empresaId(10L).rolId(1L).build();
        Usuario u2 = Usuario.builder().id(2L).nombre("Luis").apellido("Paz").rut("22-2").telefono("888").empresaId(20L).rolId(3L).build();

        when(usuarioRepository.findAllByOrderByNombreAsc()).thenReturn(List.of(u1, u2));
        when(empresaClient.listar()).thenReturn(List.of(new EmpresaDto(10L, "Induztek"), new EmpresaDto(20L, "Minera Norte")));

        List<UsuarioRespuesta> resultado = usuarioService.listar(null);

        assertEquals(2, resultado.size());
        assertEquals("Induztek", resultado.get(0).empresaNombre());
        assertEquals("Técnico", resultado.get(0).rolNombre());
        assertEquals("Minera Norte", resultado.get(1).empresaNombre());
        assertEquals("Administrador", resultado.get(1).rolNombre());
    }

    @Test
    void listar_conFiltro_buscaPorNombreORut() {
        Usuario u = Usuario.builder().id(3L).nombre("Carlos").apellido("Diaz").rut("33-3").telefono("777").empresaId(99L).rolId(2L).build();
        when(usuarioRepository.findByNombreContainingIgnoreCaseOrRutContainingIgnoreCaseOrderByNombreAsc("Car", "Car"))
                .thenReturn(List.of(u));
        when(empresaClient.listar()).thenReturn(List.of());

        List<UsuarioRespuesta> resultado = usuarioService.listar("Car");

        assertEquals(1, resultado.size());
        assertEquals("", resultado.get(0).empresaNombre());
        assertEquals("Cliente", resultado.get(0).rolNombre());
    }

    // --- obtener ---

    @Test
    void obtener_existente_retornaUsuarioRespuesta() {
        Usuario u = Usuario.builder().id(1L).nombre("Ana").apellido("Soto").rut("11-1").telefono("999").empresaId(10L).rolId(1L).build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(empresaClient.obtener(10L)).thenReturn(new EmpresaDto(10L, "Induztek"));

        UsuarioRespuesta resp = usuarioService.obtener(1L);

        assertEquals(1L, resp.id());
        assertEquals("Ana", resp.nombre());
        assertEquals("Induztek", resp.empresaNombre());
    }

    @Test
    void obtener_inexistente_lanzaNotFound() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> usuarioService.obtener(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // --- registrar ---

    @Test
    void registrar_exitoso_guardaYRetornaRespuesta() {
        DatosRegistro datos = new DatosRegistro("Mario", "Rojas", "12.345.678-9", "912345678", 10L, 1L);
        EmpresaDto empresa = new EmpresaDto(10L, "Induztek");
        Usuario guardado = Usuario.builder().id(5L).nombre("Mario").apellido("Rojas").rut("12.345.678-9").telefono("912345678").empresaId(10L).rolId(1L).build();

        when(empresaClient.obtener(10L)).thenReturn(empresa);
        when(usuarioRepository.existsByRut("12.345.678-9")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(guardado);

        UsuarioRespuesta resp = usuarioService.registrar(datos);

        assertEquals(5L, resp.id());
        assertEquals("Técnico", resp.rolNombre());
        assertEquals("Induztek", resp.empresaNombre());
    }

    @Test
    void registrar_datosInvalidosORolInexistente_lanzaBadRequest() {
        assertThrows(ResponseStatusException.class,
                () -> usuarioService.registrar(new DatosRegistro(" ", "Rojas", "11-1", "999", 10L, 1L)));
        assertThrows(ResponseStatusException.class,
                () -> usuarioService.registrar(new DatosRegistro("Mario", "Rojas", "11-1", "999", 10L, 99L)));
        assertThrows(ResponseStatusException.class,
                () -> usuarioService.registrar(new DatosRegistro("Mario", "Rojas", "11-1", "999", null, 1L)));
    }

    @Test
    void registrar_empresaNoExiste_lanzaBadRequest() {
        DatosRegistro datos = new DatosRegistro("Mario", "Rojas", "11-1", "999", 77L, 1L);
        when(empresaClient.obtener(77L)).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> usuarioService.registrar(datos));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void registrar_rutDuplicado_lanzaConflict() {
        DatosRegistro datos = new DatosRegistro("Mario", "Rojas", "11-1", "999", 10L, 2L);
        when(empresaClient.obtener(10L)).thenReturn(new EmpresaDto(10L, "Induztek"));
        when(usuarioRepository.existsByRut("11-1")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> usuarioService.registrar(datos));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    // --- actualizar ---

    @Test
    void actualizar_exitoso_modificaYGuarda() {
        Usuario existente = Usuario.builder().id(1L).nombre("Viejo").apellido("Ape").rut("11-1").telefono("111").empresaId(10L).rolId(1L).build();
        DatosActualizacion datos = new DatosActualizacion("Nuevo", "Apellido", "11-1", "222", 20L, 3L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(empresaClient.obtener(20L)).thenReturn(new EmpresaDto(20L, "Planta Sur"));
        when(usuarioRepository.existsByRutAndIdNot("11-1", 1L)).thenReturn(false);
        when(usuarioRepository.save(existente)).thenReturn(existente);

        UsuarioRespuesta resp = usuarioService.actualizar(1L, datos);

        assertEquals("Nuevo", resp.nombre());
        assertEquals("Administrador", resp.rolNombre());
        assertEquals("Planta Sur", resp.empresaNombre());
    }

    @Test
    void actualizar_rutDeOtroUsuario_lanzaConflict() {
        Usuario existente = Usuario.builder().id(1L).rut("11-1").build();
        DatosActualizacion datos = new DatosActualizacion("Nuevo", "Apellido", "22-2", "222", 10L, 1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(empresaClient.obtener(10L)).thenReturn(new EmpresaDto(10L, "Induztek"));
        when(usuarioRepository.existsByRutAndIdNot("22-2", 1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> usuarioService.actualizar(1L, datos));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    // --- eliminar ---

    @Test
    void eliminar_existente_eliminaUsuario() {
        Usuario existente = Usuario.builder().id(4L).build();
        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(existente));

        usuarioService.eliminar(4L);

        verify(usuarioRepository).delete(existente);
    }
}