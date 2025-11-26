package org.example.EmisionSuscripcionPolizas.Delegates.Notificaciones;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.EmisionSuscripcionPolizas.Service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("notificarRechazoPorVencimientoCotizacionDelegate")
public class NotificarRechazoPorVencimientoCotizacionDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificarRechazoPorVencimientoCotizacionDelegate.class);

    @Autowired(required = false)
    private EmailService emailService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        String nombreCliente = (String) execution.getVariable("nombreCliente");
        String emailCliente  = (String) execution.getVariable("emailCliente");
        String tipoProducto  = (String) execution.getVariable("tipoProducto");
        String processId     = execution.getProcessInstanceId();

        LOGGER.info("══════════════════════════════════════════════════════");
        LOGGER.info("⌛ NOTIFICACIÓN DE RECHAZO POR VENCIMIENTO DE COTIZACIÓN");
        LOGGER.info("Cliente: {}", nombreCliente);
        LOGGER.info("Email: {}", emailCliente);
        LOGGER.info("Producto: {}", tipoProducto);
        LOGGER.info("Process ID: {}", processId);
        LOGGER.info("Motivo: No se recibió respuesta del cliente dentro del plazo definido.");
        LOGGER.info("══════════════════════════════════════════════════════");

        String subject = "Tu cotización ha vencido";
        String html = """
            <html><body style='font-family:Arial, sans-serif; color:#333'>
              <h2>Hola %s,</h2>
              <p>La cotización para el producto <strong>%s</strong> ha vencido porque no recibimos tu respuesta dentro del tiempo establecido.</p>
              <p>Si aún estás interesada en adquirir tu póliza, puedes solicitar una nueva cotización cuando lo desees.</p>
              <p style='margin-top:24px'>Gracias por considerar a Seguros Camunda.</p>
            </body></html>
            """.formatted(
                nombreCliente != null ? nombreCliente : "",
                tipoProducto != null ? tipoProducto : "seguro"
        );

        try {
            if (emailService != null && emailCliente != null && !emailCliente.isBlank()) {
                emailService.sendHtmlMail(emailCliente, subject, html);
            } else {
                LOGGER.warn("Correo NO enviado (EmailService nulo o emailCliente vacío).");
                LOGGER.info("PREVIEW EMAIL HTML:\n{}", html);
            }
        } catch (Exception e) {
            LOGGER.error("❌ Error enviando notificación de vencimiento de cotización: {}", e.getMessage(), e);
        }

        execution.setVariable("estadoFinal", "COTIZACION_VENCIDA");
        execution.setVariable(
                "mensajePortalFinal",
                "La cotización venció porque no recibimos tu respuesta dentro del tiempo establecido."
        );
    }
}
