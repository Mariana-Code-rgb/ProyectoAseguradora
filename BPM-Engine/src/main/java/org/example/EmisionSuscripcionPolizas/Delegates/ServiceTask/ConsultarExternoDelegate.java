package org.example.EmisionSuscripcionPolizas.Delegates.ServiceTask;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("consultarExternoDelegate")
public class ConsultarExternoDelegate implements JavaDelegate {
    private static final Logger LOG = LoggerFactory.getLogger(ConsultarExternoDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String nombreCliente = (String) execution.getVariable("nombreCliente");
        Thread.sleep(400);

        int score = 780;                 // fijo alto para aprobar
        String reporte = "Positivo";     // evitar "Negativo" durante pruebas

        LOG.info("🌐 CONSULTA BASE DE DATOS EXTERNA - Cliente: {}", nombreCliente);
        LOG.info("   Score Crediticio: {}", score);
        LOG.info("   Reporte de Riesgo: {}", reporte);

        execution.setVariable("scoreCrediticio", score);
        execution.setVariable("reporteCRiesgo", reporte);
    }
}

