package org.example.EmisionSuscripcionPolizas.Delegates.Envios;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.EmisionSuscripcionPolizas.Service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component("enviarCotizacionDelegate")
public class EnviarCotizacionDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnviarCotizacionDelegate.class);

    @Autowired
    private EmailService emailService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Obtener variables del proceso
        String nombreCliente = (String) execution.getVariable("nombreCliente");
        String emailCliente = (String) execution.getVariable("emailCliente");
        String tipoProducto = (String) execution.getVariable("tipoProducto");
        Double primaAnual = (Double) execution.getVariable("primaAnual");
        Double montoAsegurado = (Double) execution.getVariable("montoAsegurado");

        String processInstanceId = execution.getProcessInstanceId();

        LOGGER.info("═══════════════════════════════════════════════════");
        LOGGER.info("📧 COTIZACIÓN PREPARADA");
        LOGGER.info("Cliente: {}", nombreCliente);
        LOGGER.info("Email: {}", emailCliente);
        LOGGER.info("Producto: {}", tipoProducto);
        LOGGER.info("Prima Anual: ${}", primaAnual);
        LOGGER.info("Monto Asegurado: ${}", montoAsegurado);
        LOGGER.info("Process ID: {}", processInstanceId);
        LOGGER.info("");
        LOGGER.info("⚠️ Para continuar el proceso, copia uno de estos enlaces en tu navegador:");
        LOGGER.info("");
        LOGGER.info("✅ ACEPTAR: http://localhost:8080/api/respuesta-cliente?processId={}&acepta=true", processInstanceId);
        LOGGER.info("");
        LOGGER.info("❌ RECHAZAR: http://localhost:8080/api/respuesta-cliente?processId={}&acepta=false", processInstanceId);
        LOGGER.info("");
        LOGGER.info("═══════════════════════════════════════════════════");
    }
}
