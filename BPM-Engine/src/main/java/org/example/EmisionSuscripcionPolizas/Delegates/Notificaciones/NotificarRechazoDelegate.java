package org.example.EmisionSuscripcionPolizas.Delegates.Notificaciones;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.EmisionSuscripcionPolizas.Service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("notificarRechazoDelegate")
public class NotificarRechazoDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificarRechazoDelegate.class);

    @Autowired(required = false)
    private EmailService emailService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String emailCliente = (String) execution.getVariable("emailCliente");
        String nombreCliente = (String) execution.getVariable("nombreCliente");
        String motivo = (String) execution.getVariable("motivoRechazo");

        LOGGER.info("══════════════════════════════════════════════════════");
        LOGGER.info("❌ NOTIFICACIÓN DE RECHAZO GENERAL");
        LOGGER.info("Cliente: {}", nombreCliente);
        LOGGER.info("Email: {}", emailCliente);
        LOGGER.info("Motivo: {}", motivo);
        LOGGER.info("══════════════════════════════════════════════════════");

        String subject = "Notificación de rechazo de solicitud";
        String html = """
            <html><body style='font-family:Arial, sans-serif; color:#333'>
              <h2>Hola %s,</h2>
              <p>Lamentamos informarte que tu solicitud de póliza ha sido rechazada.</p>
              <p><strong>Motivo:</strong> %s</p>
              <p>Si lo deseas, puedes contactar a tu agente para obtener más detalles.</p>
              <p style='margin-top:24px'>Atentamente,<br/>Equipo de Seguros Camunda</p>
            </body></html>
            """.formatted(
                nombreCliente != null ? nombreCliente : "",
                motivo != null ? motivo : "No especificado"
        );

        try {
            if (emailService != null && emailCliente != null && !emailCliente.isBlank()) {
                emailService.sendHtmlMail(emailCliente, subject, html);
            } else {
                LOGGER.warn("Correo NO enviado (EmailService nulo o emailCliente vacío).");
                LOGGER.info("PREVIEW EMAIL HTML:\n{}", html);
            }
        } catch (Exception e) {
            LOGGER.error("❌ Error enviando notificación de rechazo general: {}", e.getMessage(), e);
        }

        execution.setVariable("estadoFinal", "RECHAZO_MANUAL");
        execution.setVariable(
                "mensajePortalFinal",
                "Tu solicitud fue rechazada por la aseguradora. Motivo: "
                        + (motivo != null ? motivo : "No especificado")
        );
    }
}
