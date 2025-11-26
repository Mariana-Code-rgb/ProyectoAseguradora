package org.example.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class EvaluacionRiesgoDTO implements Serializable {
    // Campos de solo lectura (que vienen del proceso)
    private String nombreCliente;
    private String tipoProducto;
    private Double primaAnual;
    private Double montoAsegurado;

    // Campos del análisis
    private Integer antiguedadCliente;
    private String historialSiniestralidad;
    private String reporteRiesgo;
    private Integer scoreCrediticio;

    // Campos de decisión (los que llena el usuario)
    private Boolean suscripcionAprobada; // select: true/false
    private Double ajustePrimaFinal;
    private String observaciones;
}
