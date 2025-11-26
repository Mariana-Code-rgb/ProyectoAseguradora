package org.example.EmisionSuscripcionPolizas.Delegates.Envios;


import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.EmisionSuscripcionPolizas.Service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("enviarContratoDelegate")
public class EnviarContratoDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnviarContratoDelegate.class);

    @Autowired(required = false)
    private EmailService emailService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        // Variables de proceso
        String nombreCliente  = (String) execution.getVariable("nombreCliente");
        String emailCliente   = (String) execution.getVariable("emailCliente");
        String numeroContrato = (String) execution.getVariable("numeroContrato");
        String processId      = execution.getProcessInstanceId();

        // Resolver baseUrl
        Object baseUrlVar = execution.getVariable("baseUrl");
        String baseUrl = baseUrlVar != null ? baseUrlVar.toString() : "http://localhost:8080";

        // Enlaces para firmar/no firmar
        String urlFirmar    = "%s/api/firmar-contrato?processId=%s&firmado=true".formatted(baseUrl, processId);
        String urlNoFirmar  = "%s/api/firmar-contrato?processId=%s&firmado=false".formatted(baseUrl, processId);

        // Logs con enlaces (estilo solicitado)
        LOGGER.info("═══════════════════════════════════════════════════");
        LOGGER.info("📄 CONTRATO ENVIADO PARA FIRMA");
        LOGGER.info("Cliente: {}", nombreCliente);
        LOGGER.info("Número de Contrato: {}", numeroContrato);
        LOGGER.info("Process ID: {}", processId);
        LOGGER.info("");
        LOGGER.info("⚠️ Para firmar el contrato, copia uno de estos enlaces en tu navegador:");
        LOGGER.info("");
        LOGGER.info("✍️ FIRMAR:    {}", urlFirmar);
        LOGGER.info("");
        LOGGER.info("❌ NO FIRMAR:  {}", urlNoFirmar);
        LOGGER.info("");
        LOGGER.info("═══════════════════════════════════════════════════");

        // Email opcional y tolerante (no detiene el proceso si falla)
        String subject = "Contrato disponible para firma";
        String htmlBody = """
        <html>
          <body style='font-family:Arial'>
            <h2>Hola %s,</h2>
            <p>Tu contrato <strong>%s</strong> está listo para firma.</p>
            <p>
              <a href="%s" style="background:#4CAF50;color:white;padding:12px 22px;text-decoration:none;border-radius:4px">
                ✍️ Firmar Contrato
              </a>
              &nbsp;&nbsp;
              <a href="%s" style="background:#f44336;color:white;padding:12px 22px;text-decoration:none;border-radius:4px">
                ❌ No firmar
              </a>
            </p>
            <p style='margin-top:40px;font-size:12px;color:#666'>
              Si los botones no funcionan, copia y pega estas URL:<br>%s<br>%s
            </p>
          </body>
        </html>
        """.formatted(nombreCliente, numeroContrato, urlFirmar, urlNoFirmar, urlFirmar, urlNoFirmar);

        try {
            if (emailService != null && emailCliente != null && !emailCliente.isBlank()) {
                emailService.sendHtmlMail(emailCliente, subject, htmlBody);
            } else {
                LOGGER.warn("Correo NO enviado (EmailService nulo o emailCliente vacío).");
                LOGGER.info("PREVIEW EMAIL HTML:\n{}", htmlBody);
            }
        } catch (Exception e) {
            LOGGER.error("❌ Error enviando correo de contrato (se continúa el proceso): {}", e.getMessage(), e);
            LOGGER.info("PREVIEW EMAIL HTML:\n{}", htmlBody);
        }

        // Variable de seguimiento
        execution.setVariable("contratoEnviado", true);
    }
}
