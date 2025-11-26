package com.aseguradora.appgestion;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class VerificarResultadoReclamacionDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Leer variables que vienen del flujo
        Boolean polizaValida = (Boolean) execution.getVariable("polizaValida");
        String nivelDanios = (String) execution.getVariable("nivelDanios");        // BAJO / MEDIO / ALTO
        Double montoEstimado = (Double) execution.getVariable("montoEstimado");    // del ajustador
        Double montoReclamado = (Double) execution.getVariable("montoReclamado");  // monto aprobado por gerencia

        // Lógica muy básica de ejemplo: decidir si se aprueba o se rechaza
        boolean aprobada = true;
        String comentario = "Reclamación aprobada automáticamente.";

        if (polizaValida == null || !polizaValida) {
            aprobada = false;
            comentario = "Póliza no válida para este siniestro.";
        } else if (montoReclamado == null || montoReclamado <= 0) {
            aprobada = false;
            comentario = "Monto reclamado inválido.";
        } else if ("ALTO".equalsIgnoreCase(nivelDanios) && montoReclamado > 2 * (montoEstimado != null ? montoEstimado : 0.0)) {
            aprobada = false;
            comentario = "Monto reclamado excede el umbral para el nivel de daños.";
        }

        // Variables de salida que usará el gateway / pasos siguientes
        execution.setVariable("reclamacionAprobada", aprobada);
        execution.setVariable("comentarioSistemaReclamacion", comentario);
    }
}