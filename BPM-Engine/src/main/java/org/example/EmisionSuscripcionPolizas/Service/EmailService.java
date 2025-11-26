package org.example.EmisionSuscripcionPolizas.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private Environment env;

    private final DecimalFormat money = new DecimalFormat("#,##0.00");

    /**
     * Carga un template HTML desde src/main/resources/templates y reemplaza {{clave}} por valor.
     */
    private String loadTemplate(String templateName, Map<String, String> variables) throws Exception {
        Path path = Path.of("src/main/resources/templates", templateName);
        String html = Files.readString(path, StandardCharsets.UTF_8);
        for (Map.Entry<String, String> e : variables.entrySet()) {
            html = html.replace("{{" + e.getKey() + "}}", e.getValue());
        }
        return html;
    }

    /**
     * Base URL del motor (por si la necesitas para otras cosas).
     */
    private String engineBaseUrl() {
        return env.getProperty("app.engine-url", "http://localhost:9000");
    }

    /**
     * Base URL del portal de clientes (App-Cliente).
     * En application.properties del motor puedes definir:
     *   app.cliente-base-url=http://localhost:9001
     */
    private String clienteBaseUrl() {
        return env.getProperty("app.cliente-base-url", "http://localhost:9001");
    }

    /**
     * Enviar correo de cotización al cliente con link al Portal de Cliente.
     * El processInstanceId se mantiene en la firma por compatibilidad, aunque
     * la correlación ahora se hace por email desde el portal.
     */
    public void sendCotizacionEmail(
            String to,
            String nombreCliente,
            String tipoProducto,
            Double primaAnual,
            Double montoAsegurado,
            String processInstanceId) {

        try {
            // Link al portal del cliente, con el email precargado
            String emailEncoded = URLEncoder.encode(to, StandardCharsets.UTF_8);
            String portalUrl = "%s/portal/acceso?email=%s"
                    .formatted(clienteBaseUrl(), emailEncoded);

            String subject = "Tu cotización está lista ✔";

            String html = """
                <html>
                  <body style='font-family:Arial, sans-serif; background-color:#f4f7f6; padding:20px;'>
                    <table align='center' width='600' cellpadding='0' cellspacing='0' 
                           style='background:#ffffff;border-radius:8px;box-shadow:0 4px 12px rgba(0,0,0,0.1);padding:30px;'>
                      <tr>
                        <td>
                          <h2 style='color:#0d6efd;margin-top:0;'>Hola %s,</h2>
                          <p style='font-size:14px;color:#555;'>
                            Hemos elaborado la cotización que solicitaste. A continuación encontrarás un resumen:
                          </p>
                          <ul style='font-size:14px;color:#333;'>
                            <li><b>Producto:</b> %s</li>
                            <li><b>Prima anual:</b> $%s</li>
                            <li><b>Monto asegurado:</b> $%s</li>
                          </ul>
                          <p style='font-size:14px;color:#555;'>
                            Para ver los detalles completos y <b>aceptar o rechazar</b> la oferta, ingresa a tu portal de cliente:
                          </p>
                          <p style='text-align:center;margin:30px 0;'>
                            <a href="%s"
                               style="background:#0d6efd;color:white;padding:12px 28px;
                                      text-decoration:none;border-radius:25px;
                                      font-weight:bold;font-size:14px;display:inline-block;">
                              Ir a mi portal
                            </a>
                          </p>
                          <p style='font-size:12px;color:#999;margin-top:30px;'>
                            Si el botón no funciona, copia y pega este enlace en tu navegador:<br/>
                            <span style='color:#555;'>%s</span>
                          </p>
                          <p style='font-size:11px;color:#aaa;margin-top:20px;'>
                            Este correo es solo informativo. No respondas a esta dirección.
                          </p>
                        </td>
                      </tr>
                    </table>
                  </body>
                </html>
                """.formatted(
                    nombreCliente,
                    tipoProducto,
                    money.format(primaAnual),
                    money.format(montoAsegurado),
                    portalUrl,
                    portalUrl
            );

            if (mailSender == null) {
                log.warn("Email no configurado (JavaMailSender es null). Omitiendo envío. Subject='{}' To={}", subject, to);
                log.info("PREVIEW EMAIL HTML:\n{}", html);
                return;
            }

            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, "utf-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true); // true => HTML
            mailSender.send(mime);

            log.info("✅ Email de cotización enviado a {}", to);

        } catch (Exception e) {
            // No romper el flujo del proceso si falla el correo
            log.error("❌ Error enviando email de cotización (se ignora para no fallar la tarea): {}", e.getMessage(), e);
        }
    }

    /**
     * Utilidad genérica por si quieres enviar otros HTML.
     */
    public void sendHtmlMail(String to, String subject, String html) {
        try {
            if (mailSender == null) {
                log.warn("Email no configurado. Omitiendo envío. Subject='{}' To={}", subject, to);
                log.info("PREVIEW EMAIL HTML:\n{}", html);
                return;
            }
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, "utf-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(mime);
            log.info("✅ Email enviado a {}", to);
        } catch (Exception e) {
            log.error("❌ Error enviando email: {}", e.getMessage(), e);
        }
    }
}
