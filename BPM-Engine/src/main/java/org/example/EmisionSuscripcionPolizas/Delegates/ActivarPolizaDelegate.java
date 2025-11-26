package org.example.EmisionSuscripcionPolizas.Delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("activarPolizaDelegate")
public class ActivarPolizaDelegate implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(ActivarPolizaDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String numeroContrato = (String) execution.getVariable("numeroContrato");
        String nombreCliente = (String) execution.getVariable("nombreCliente");
        String numeroPoliza = "POL-" + System.currentTimeMillis();

        LOG.info("═══════════════════════════════════════════════════════");
        LOG.info("✅ PÓLIZA ACTIVADA EN EL SISTEMA");
        LOG.info("Cliente: {}", nombreCliente);
        LOG.info("Contrato: {}", numeroContrato);
        LOG.info("Número de Póliza: {}", numeroPoliza);
        LOG.info("Estado: ACTIVA");
        LOG.info("═══════════════════════════════════════════════════════");

        execution.setVariable("numeroPoliza", numeroPoliza);
        execution.setVariable("estadoPoliza", "ACTIVA");
        execution.setVariable("fechaActivacion", java.time.LocalDateTime.now().toString());
    }
}
