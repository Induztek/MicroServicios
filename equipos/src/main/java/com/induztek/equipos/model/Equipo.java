package com.induztek.equipos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "equipos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEquipo;

    @NotBlank(message = "El código del equipo es obligatorio")
    @Column(unique = true, nullable = false)
    private String codigo;

    @NotBlank(message = "El tipo de equipo es obligatorio")
    private String tipo;

    private String instalacion;
    private String direccion;
    private Double latitud;
    private Double longitud;

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long idCliente;

    private Long idEmpresa;
    private Long idTecnico;

    // Equipo sin empresa ni técnico asignado
    public Equipo(Long idEquipo, String codigo, String tipo, String instalacion, String direccion,
                  Double latitud, Double longitud, Long idCliente) {
        this(idEquipo, codigo, tipo, instalacion, direccion, latitud, longitud, idCliente, null, null);
    }
}