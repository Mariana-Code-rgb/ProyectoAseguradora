package org.example.EmisionSuscripcionPolizas.Delegates.Notificaciones;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.EmisionSuscripcionPolizas.Service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;

@Component("notificarRechazoPorNoPagoInicialDelegate")
public class NotificarRechazoPorNoPagoInicialDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificarRechazoPorNoPagoInicialDelegate.class);
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");

    @Autowired(required = false)
    private EmailService emailService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        String nombreCliente = (String) execution.getVariable("nombreCliente");
        String emailCliente  = (String) execution.getVariable("emailCliente");
        String tipoProducto  = (String) execution.getVariable("tipoProducto");
        Double montoInicial  = (Double) execution.getVariable("montoInicial");
        if (montoInicial == null) montoInicial = 0.0;
        String processId     = execution.getProcessInstanceId();

        LOGGER.info("══════════════════════════════════════════════════════");
        LOGGER.info("❌ NOTIFICACIÓN DE RECHAZO POR NO PAGO");
        LOGGER.info("Cliente: {}", nombreCliente);
        LOGGER.info("Email: {}", emailCliente);
        LOGGER.info("Producto: {}", tipoProducto);
        LOGGER.info("Monto inicial esperado: ${}", MONEY.format(montoInicial));
        LOGGER.info("Process ID: {}", processId);
        LOGGER.info("Motivo: No se confirmó el pago dentro del plazo definido.");
        LOGGER.info("══════════════════════════════════════════════════════");

        String subject = "Solicitud rechazada por falta de pago";
        String html = """
            <html><body style='font-family:Arial, sans-serif; color:#333'>
              <h2>Hola %s,</h2>
              <p>La solicitud para el producto <strong>%s</strong> ha sido rechazada porque no se confirmó el pago inicial de <strong>$%s</strong> dentro del plazo establecido.</p>
              <p>Si fue un error o deseas retomar el proceso, por favor comunícate con nosotros.</p>
              <p style='margin-top:24px'>Saludos cordiales,<br/>Equipo de Seguros Camunda</p>
            </body></html>
            """.formatted(
                nombreCliente != null ? nombreCliente : "",
                tipoProducto != null ? tipoProducto : "seguro",
                MONEY.format(montoInicial)
        );

        try {
            if (emailService != null && emailCliente != null && !emailCliente.isBlank()) {
                emailService.sendHtmlMail(emailCliente, subject, html);
            } else {
                LOGGER.warn("Correo NO enviado (EmailService nulo o emailCliente vacío).");
                LOGGER.info("PREVIEW EMAIL HTML:\n{}", html);
            }
        } catch (Exception e) {
            LOGGER.error("❌ Error enviando notificación por no pago: {}", e.getMessage(), e);
        }

        execution.setVariable("estadoFinal", "RECHAZO_NO_PAGO");
        execution.setVariable(
                "mensajePortalFinal",
                "Tu solicitud fue rechazada porque no se confirmó el pago inicial dentro del plazo."
        );
    }
}
