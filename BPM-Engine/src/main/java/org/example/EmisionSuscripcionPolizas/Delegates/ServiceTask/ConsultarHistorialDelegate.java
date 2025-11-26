package org.example.EmisionSuscripcionPolizas.Delegates.ServiceTask;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("consultarHistorialDelegate")
public class ConsultarHistorialDelegate implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(ConsultarHistorialDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String nombreCliente = (String) execution.getVariable("nombreCliente");

        // Simula consulta a BD interna
        Thread.sleep(500);

        // Simula diferentes escenarios según el cliente
        int antiguedad = (int) (Math.random() * 10); // 0-9 años
        String[] historialOpciones = {"Limpio", "Bajo", "Alto"};
        String historial = historialOpciones[(int) (Math.random() * 3)];

        LOG.info("📊 CONSULTA HISTORIAL INTERNO - Cliente: {}", nombreCliente);
        LOG.info("   Antigüedad: {} años", antiguedad);
        LOG.info("   Historial Siniestralidad: {}", historial);

        execution.setVariable("antiguedadCliente", antiguedad);
        execution.setVariable("historialSiniestralidad", historial);
    }
}
