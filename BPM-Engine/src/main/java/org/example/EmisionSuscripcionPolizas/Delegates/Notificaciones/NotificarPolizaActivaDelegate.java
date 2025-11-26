package org.example.EmisionSuscripcionPolizas.Delegates.Notificaciones;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.EmisionSuscripcionPolizas.Service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificarPolizaActivaDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificarPolizaActivaDelegate.class);

    @Autowired(required = false)
    private EmailService emailService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String emailCliente   = (String) execution.getVariable("emailCliente");
        String nombreCliente  = (String) execution.getVariable("nombreCliente");
        String tipoProducto   = (String) execution.getVariable("tipoProducto");
        Double primaAnual     = (Double) execution.getVariable("primaAnual");
        Double montoAsegurado = (Double) execution.getVariable("montoAsegurado");

        LOGGER.info("══════════════════════════════════════════════════════");
        LOGGER.info("✅ NOTIFICACIÓN DE PÓLIZA ACTIVA");
        LOGGER.info("Cliente: {}", nombreCliente);
        LOGGER.info("Email: {}", emailCliente);
        LOGGER.info("Producto: {}", tipoProducto);
        LOGGER.info("Prima anual: {}", primaAnual);
        LOGGER.info("Monto asegurado: {}", montoAsegurado);
        LOGGER.info("══════════════════════════════════════════════════════");

        String subject = "¡Tu póliza ya está activa!";
        String html = """
            <html><body style='font-family:Arial, sans-serif; color:#333'>
              <h2>Hola %s,</h2>
              <p>¡Buenas noticias! Tu póliza de <strong>%s</strong> ha sido activada exitosamente.</p>
              <ul>
                <li><strong>Prima anual:</strong> %s</li>
                <li><strong>Monto asegurado:</strong> %s</li>
              </ul>
              <p>A partir de este momento cuentas con la protección contratada.</p>
              <p style='margin-top:24px'>Gracias por confiar en Seguros Camunda.</p>
            </body></html>
            """.formatted(
                nombreCliente != null ? nombreCliente : "",
                tipoProducto != null ? tipoProducto : "tu póliza",
                primaAnual != null ? primaAnual : "-",
                montoAsegurado != null ? montoAsegurado : "-"
        );

        try {
            if (emailService != null && emailCliente != null && !emailCliente.isBlank()) {
                emailService.sendHtmlMail(emailCliente, subject, html);
            } else {
                LOGGER.warn("Correo NO enviado (EmailService nulo o emailCliente vacío).");
                LOGGER.info("PREVIEW EMAIL HTML:\n{}", html);
            }
        } catch (Exception e) {
            LOGGER.error("❌ Error enviando notificación de póliza activa: {}", e.getMessage(), e);
        }

        execution.setVariable("estadoFinal", "POLIZA_ACTIVA");
        execution.setVariable(
                "mensajePortalFinal",
                "¡Tu póliza ha sido activada exitosamente! Gracias por confiar en nosotros."
        );
    }
}
