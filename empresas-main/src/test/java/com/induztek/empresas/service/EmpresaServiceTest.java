package com.induztek.empresas.service;

import com.induztek.empresas.dto.DatosEmpresa;
import com.induztek.empresas.model.Empresa;
import com.induztek.empresas.repository.EmpresaRepository;
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
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private EmpresaService empresaService;

    // --- listar ---

    @Test
    void listar_retornaEmpresasOrdenadasPorNombre() {
        List<Empresa> esperado = List.of(
                Empresa.builder().id(1L).nombre("Alpha").direccion("Av. Siempre Viva 123").build(),
                Empresa.builder().id(2L).nombre("Beta").direccion("Calle Falsa 456").build()
        );
        when(empresaRepository.findAllByOrderByNombreAsc()).thenReturn(esperado);

        List<Empresa> resultado = empresaService.listar();

        assertEquals(2, resultado.size());
        assertEquals("Alpha", resultado.get(0).getNombre());
    }

    // --- obtener ---

    @Test
    void obtener_existente_retornaEmpresa() {
        Empresa empresa = Empresa.builder().id(1L).nombre("Induztek").direccion("Av. Industrial 500").build();
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        Empresa resultado = empresaService.obtener(1L);

        assertEquals("Induztek", resultado.getNombre());
    }

    @Test
    void obtener_inexistente_lanzaNotFound() {
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> empresaService.obtener(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // --- crear ---

    @Test
    void crear_exitoso_recortaEspaciosYGuarda() {
        DatosEmpresa datos = new DatosEmpresa("  Minera Norte  ", "  Ruta 5 Norte Km 10  ");
        Empresa guardada = Empresa.builder().id(10L).nombre("Minera Norte").direccion("Ruta 5 Norte Km 10").build();

        when(empresaRepository.existsByNombreIgnoreCase("Minera Norte")).thenReturn(false);
        when(empresaRepository.save(any(Empresa.class))).thenReturn(guardada);

        Empresa resultado = empresaService.crear(datos);

        assertEquals(10L, resultado.getId());
        assertEquals("Minera Norte", resultado.getNombre());
        assertEquals("Ruta 5 Norte Km 10", resultado.getDireccion());
    }

    @Test
    void crear_nombreDuplicado_lanzaConflict() {
        DatosEmpresa datos = new DatosEmpresa("Induztek", "Av. Central 123");
        when(empresaRepository.existsByNombreIgnoreCase("Induztek")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> empresaService.crear(datos));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void crear_datosVaciosOLongitudInvalida_lanzaBadRequest() {
        // Vacíos o nulos
        assertThrows(ResponseStatusException.class, () -> empresaService.crear(new DatosEmpresa(" ", "Direccion 123")));
        assertThrows(ResponseStatusException.class, () -> empresaService.crear(new DatosEmpresa("Empresa", null)));

        // Nombre < 3 o > 60 caracteres
        assertThrows(ResponseStatusException.class, () -> empresaService.crear(new DatosEmpresa("AB", "Direccion 123")));
        assertThrows(ResponseStatusException.class, () -> empresaService.crear(new DatosEmpresa("A".repeat(61), "Direccion 123")));

        // Dirección < 5 o > 100 caracteres
        assertThrows(ResponseStatusException.class, () -> empresaService.crear(new DatosEmpresa("Empresa", "1234")));
        assertThrows(ResponseStatusException.class, () -> empresaService.crear(new DatosEmpresa("Empresa", "D".repeat(101))));
    }

    // --- actualizar ---

    @Test
    void actualizar_exitoso_modificaYGuarda() {
        Empresa existente = Empresa.builder().id(1L).nombre("Vieja").direccion("Direccion Vieja 1").build();
        DatosEmpresa datos = new DatosEmpresa("  Nueva Empresa  ", "  Nueva Direccion 200  ");

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(empresaRepository.existsByNombreIgnoreCaseAndIdNot("Nueva Empresa", 1L)).thenReturn(false);
        when(empresaRepository.save(existente)).thenReturn(existente);

        Empresa resultado = empresaService.actualizar(1L, datos);

        assertEquals("Nueva Empresa", resultado.getNombre());
        assertEquals("Nueva Direccion 200", resultado.getDireccion());
    }

    @Test
    void actualizar_nombreDeOtraEmpresa_lanzaConflict() {
        Empresa existente = Empresa.builder().id(1L).nombre("Original").direccion("Direccion 123").build();
        DatosEmpresa datos = new DatosEmpresa("Duplicada", "Direccion 456");

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(empresaRepository.existsByNombreIgnoreCaseAndIdNot("Duplicada", 1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> empresaService.actualizar(1L, datos));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }
}