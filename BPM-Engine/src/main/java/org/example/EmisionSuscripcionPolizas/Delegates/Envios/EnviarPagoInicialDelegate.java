package org.example.EmisionSuscripcionPolizas.Delegates.Envios;


import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.EmisionSuscripcionPolizas.Service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;

@Component("enviarPagoInicialDelegate")
public class EnviarPagoInicialDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnviarPagoInicialDelegate.class);
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");

    @Autowired(required = false)
    private EmailService emailService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        // Variables de proceso
        String nombreCliente = (String) execution.getVariable("nombreCliente");
        String emailCliente  = (String) execution.getVariable("emailCliente");
        Double montoInicial  = (Double) execution.getVariable("montoInicial");   // si no tienes, usa otra var p.ej. montoAPagar
        if (montoInicial == null) montoInicial = 0.0;

        String processId     = execution.getProcessInstanceId();

        // Resolver baseUrl
        Object baseUrlVar = execution.getVariable("baseUrl");
        String baseUrl = baseUrlVar != null ? baseUrlVar.toString() : "http://localhost:8080";

        // Enlaces para confirmación de pago
        String urlPagado    = "%s/api/confirmacion-pago?processId=%s&pagado=true".formatted(baseUrl, processId);
        String urlNoPagado  = "%s/api/confirmacion-pago?processId=%s&pagado=false".formatted(baseUrl, processId);

        // Logs con enlaces (misma línea de estilo)
        LOGGER.info("═══════════════════════════════════════════════════");
        LOGGER.info("💳 SOLICITUD DE CONFIRMACIÓN DE PAGO");
        LOGGER.info("Cliente: {}", nombreCliente);
        LOGGER.info("Monto inicial: ${}", MONEY.format(montoInicial));
        LOGGER.info("Process ID: {}", processId);
        LOGGER.info("");
        LOGGER.info("⚠️ Para confirmar el pago, copia uno de estos enlaces en tu navegador:");
        LOGGER.info("");
        LOGGER.info("✅ PAGADO:    {}", urlPagado);
        LOGGER.info("");
        LOGGER.info("❌ NO PAGADO: {}", urlNoPagado);
        LOGGER.info("");
        LOGGER.info("═══════════════════════════════════════════════════");

        // Email opcional y tolerante (no detiene el proceso si falla)
        String subject = "Confirmación de pago requerida";
        String htmlBody = """
        <html>
          <body style='font-family:Arial'>
            <h2>Hola %s,</h2>
            <p>Por favor confirma el pago inicial por <strong>$%s</strong>.</p>
            <p>
              <a href="%s" style="background:#4CAF50;color:white;padding:12px 22px;text-decoration:none;border-radius:4px">
                ✅ Confirmar Pago
              </a>
              &nbsp;&nbsp;
              <a href="%s" style="background:#f44336;color:white;padding:12px 22px;text-decoration:none;border-radius:4px">
                ❌ No se realizó el pago
              </a>
            </p>
            <p style='margin-top:40px;font-size:12px;color:#666'>
              Si los botones no funcionan, copia y pega estas URL:<br>%s<br>%s
            </p>
          </body>
        </html>
        """.formatted(nombreCliente, MONEY.format(montoInicial), urlPagado, urlNoPagado, urlPagado, urlNoPagado);

        try {
            if (emailService != null && emailCliente != null && !emailCliente.isBlank()) {
                emailService.sendHtmlMail(emailCliente, subject, htmlBody);
            } else {
                LOGGER.warn("Correo NO enviado (EmailService nulo o emailCliente vacío).");
                LOGGER.info("PREVIEW EMAIL HTML:\n{}", htmlBody);
            }
        } catch (Exception e) {
            LOGGER.error("❌ Error enviando correo de confirmación de pago (se continúa el proceso): {}", e.getMessage(), e);
            LOGGER.info("PREVIEW EMAIL HTML:\n{}", htmlBody);
        }

        // Variable de seguimiento
        execution.setVariable("solicitudPagoEnviada", true);
    }
}
