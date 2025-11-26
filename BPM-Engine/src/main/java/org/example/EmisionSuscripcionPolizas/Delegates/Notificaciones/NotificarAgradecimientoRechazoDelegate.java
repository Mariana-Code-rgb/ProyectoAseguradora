package org.example.EmisionSuscripcionPolizas.Delegates.Notificaciones;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.EmisionSuscripcionPolizas.Service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("notificarAgradecimientoRechazoDelegate")
public class NotificarAgradecimientoRechazoDelegate implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(NotificarAgradecimientoRechazoDelegate.class);

    @Autowired(required = false)
    private EmailService emailService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String nombreCliente = (String) execution.getVariable("nombreCliente");
        String emailCliente  = (String) execution.getVariable("emailCliente");
        String tipoProducto  = (String) execution.getVariable("tipoProducto");

        LOG.info("═══════════════════════════════════════════════════════");
        LOG.info("💌 AGRADECIMIENTO Y RECHAZO (CLIENTE NO ACEPTÓ)");
        LOG.info("Cliente: {}", nombreCliente);
        LOG.info("Email: {}", emailCliente);
        LOG.info("Producto: {}", tipoProducto);
        LOG.info("Motivo: Cliente no aceptó la cotización");
        LOG.info("═══════════════════════════════════════════════════════");

        String subject = "Gracias por considerar nuestra cotización";
        String html = """
            <html><body style='font-family:Arial, sans-serif; color:#333'>
              <h2>Hola %s,</h2>
              <p>Gracias por tomarte el tiempo de revisar la cotización de tu póliza de <strong>%s</strong>.</p>
              <p>Hemos registrado que decidiste no continuar con el proceso, por lo que tu solicitud ha sido cerrada.</p>
              <p>Si en el futuro deseas una nueva cotización, estaremos encantados de ayudarte nuevamente.</p>
              <p style='margin-top:24px'>Un cordial saludo,<br/>Equipo de Seguros Camunda</p>
            </body></html>
            """.formatted(
                nombreCliente != null ? nombreCliente : "",
                tipoProducto != null ? tipoProducto : "seguro"
        );

        try {
            if (emailService != null && emailCliente != null && !emailCliente.isBlank()) {
                emailService.sendHtmlMail(emailCliente, subject, html);
            } else {
                LOG.warn("Correo NO enviado (EmailService nulo o emailCliente vacío).");
                LOG.info("PREVIEW EMAIL HTML:\n{}", html);
            }
        } catch (Exception e) {
            LOG.error("❌ Error enviando notificación de agradecimiento y rechazo: {}", e.getMessage(), e);
        }

        execution.setVariable("estadoFinal", "RECHAZO_POR_CLIENTE");
        execution.setVariable(
                "mensajePortalFinal",
                "Tu solicitud fue cerrada porque decidiste no aceptar la cotización."
        );
    }
}
