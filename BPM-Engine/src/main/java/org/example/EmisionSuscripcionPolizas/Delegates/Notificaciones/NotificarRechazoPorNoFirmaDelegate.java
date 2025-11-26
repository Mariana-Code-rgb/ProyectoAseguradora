package org.example.EmisionSuscripcionPolizas.Delegates.Notificaciones;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.EmisionSuscripcionPolizas.Service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("notificarRechazoPorNoFirmaDelegate")
public class NotificarRechazoPorNoFirmaDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificarRechazoPorNoFirmaDelegate.class);

    @Autowired(required = false)
    private EmailService emailService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        String nombreCliente  = (String) execution.getVariable("nombreCliente");
        String emailCliente   = (String) execution.getVariable("emailCliente");
        String tipoProducto   = (String) execution.getVariable("tipoProducto");
        String numeroContrato = (String) execution.getVariable("numeroContrato");
        String processId      = execution.getProcessInstanceId();

        LOGGER.info("══════════════════════════════════════════════════════");
        LOGGER.info("❌ NOTIFICACIÓN DE RECHAZO POR NO FIRMA DE CONTRATO");
        LOGGER.info("Cliente: {}", nombreCliente);
        LOGGER.info("Email: {}", emailCliente);
        LOGGER.info("Producto: {}", tipoProducto);
        LOGGER.info("Número de Contrato: {}", numeroContrato);
        LOGGER.info("Process ID: {}", processId);
        LOGGER.info("Motivo: No se registró la firma del contrato dentro del plazo definido.");
        LOGGER.info("══════════════════════════════════════════════════════");

        String subject = "Solicitud rechazada por no firma del contrato";
        String html = """
            <html><body style='font-family:Arial, sans-serif; color:#333'>
              <h2>Hola %s,</h2>
              <p>La solicitud para el producto <strong>%s</strong> y contrato <strong>%s</strong> ha sido rechazada porque no se registró la firma dentro del tiempo establecido.</p>
              <p>Si necesitas más información o deseas iniciar un nuevo proceso, por favor contacta a nuestro equipo comercial.</p>
              <p style='margin-top:24px'>Atentamente,<br/>Equipo de Seguros Camunda</p>
            </body></html>
            """.formatted(
                nombreCliente != null ? nombreCliente : "",
                tipoProducto != null ? tipoProducto : "seguro",
                numeroContrato != null ? numeroContrato : "-"
        );

        try {
            if (emailService != null && emailCliente != null && !emailCliente.isBlank()) {
                emailService.sendHtmlMail(emailCliente, subject, html);
            } else {
                LOGGER.warn("Correo NO enviado (EmailService nulo o emailCliente vacío).");
                LOGGER.info("PREVIEW EMAIL HTML:\n{}", html);
            }
        } catch (Exception e) {
            LOGGER.error("❌ Error enviando notificación por no firma: {}", e.getMessage(), e);
        }

        execution.setVariable("estadoFinal", "RECHAZO_NO_FIRMA");
        execution.setVariable(
                "mensajePortalFinal",
                "Tu solicitud fue rechazada porque no se registró la firma del contrato a tiempo."
        );
    }
}
