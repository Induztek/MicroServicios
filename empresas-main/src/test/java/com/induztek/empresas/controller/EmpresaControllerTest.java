package com.induztek.empresas.controller;

import com.induztek.empresas.dto.DatosEmpresa;
import com.induztek.empresas.model.Empresa;
import com.induztek.empresas.service.EmpresaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpresaControllerTest {

    @Mock
    private EmpresaService empresaService;

    @InjectMocks
    private EmpresaController controller;

    private Empresa ejemplo() {
        return Empresa.builder().id(1L).nombre("Induztek").direccion("Av. Industrial 500").build();
    }

    @Test
    void listar_delegaEnServicio() {
        List<Empresa> esperado = List.of(ejemplo());
        when(empresaService.listar()).thenReturn(esperado);

        assertEquals(esperado, controller.listar());
    }

    @Test
    void obtener_delegaEnServicio() {
        Empresa esperado = ejemplo();
        when(empresaService.obtener(1L)).thenReturn(esperado);

        assertEquals(esperado, controller.obtener(1L));
    }

    @Test
    void crear_delegaEnServicio() {
        DatosEmpresa datos = new DatosEmpresa("Induztek", "Av. Industrial 500");
        Empresa esperado = ejemplo();
        when(empresaService.crear(datos)).thenReturn(esperado);

        assertEquals(esperado, controller.crear(datos));
    }

    @Test
    void actualizar_delegaEnServicio() {
        DatosEmpresa datos = new DatosEmpresa("Induztek SA", "Av. Industrial 600");
        Empresa esperado = ejemplo();
        when(empresaService.actualizar(1L, datos)).thenReturn(esperado);

        assertEquals(esperado, controller.actualizar(1L, datos));
    }
}