package org.example.GestionarRenovacionFidelizacion.Controller;

import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/renovacion")
public class RenovacionController {

    private static final Logger LOGGER = Logger.getLogger(RenovacionController.class.getName());

    @Autowired
    private RuntimeService runtimeService;

    // ==========================
    //  API TÉCNICAS (JSON)
    // ==========================

    /**
     * Endpoint para que el cliente responda a la oferta de renovación
     * GET: /api/renovacion/responder?poliza=POL-2024-001&acepta=true&processInstanceId=xxx
     */
    @GetMapping("/responder")
    public ResponseEntity<Map<String, String>> responderOferta(
            @RequestParam String poliza,
            @RequestParam Boolean acepta,
            @RequestParam String processInstanceId) {

        LOGGER.info("=== Cliente responde oferta de renovación ===");
        LOGGER.info("Póliza: " + poliza + " - Acepta: " + acepta);
        LOGGER.info("Process Instance ID: " + processInstanceId);

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("clienteAceptaRenovar", acepta);
            variables.put("fechaRespuesta", java.time.LocalDateTime.now().toString());

            runtimeService.createMessageCorrelation("Message_RespuestaClienteRecibida")
                    .processInstanceId(processInstanceId)
                    .setVariables(variables)
                    .correlate();

            Map<String, String> response = new HashMap<>();
            response.put("mensaje", acepta
                    ? "¡Gracias! Tu decisión ha sido registrada. En breve recibirás las instrucciones de pago."
                    : "Hemos registrado tu decisión. Un ejecutivo de retención se pondrá en contacto contigo.");
            response.put("poliza", poliza);
            response.put("estado", "PROCESADO");

            LOGGER.info("Mensaje correlacionado exitosamente");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LOGGER.severe("Error al correlacionar mensaje: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "No se pudo procesar la respuesta");
            error.put("detalle", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Endpoint para confirmar pago de renovación
     * POST (simula webhook de pasarela de pago)
     */
    @PostMapping("/confirmar-pago")
    public ResponseEntity<Map<String, String>> confirmarPago(@RequestBody Map<String, Object> pagoData) {

        LOGGER.info("=== Confirmación de pago recibida ===");
        LOGGER.info("Datos: " + pagoData);

        try {
            String numeroPoliza = (String) pagoData.get("numeroPoliza");
            String processInstanceId = (String) pagoData.get("processInstanceId");
            String referenciaPago = (String) pagoData.get("referenciaPago");
            Double monto = Double.parseDouble(pagoData.get("monto").toString());

            Map<String, Object> variables = new HashMap<>();
            variables.put("pagoConfirmado", true);
            variables.put("montoRecibido", monto);
            variables.put("fechaPagoRecibido", java.time.LocalDateTime.now().toString());
            variables.put("referenciaPagoConfirmada", referenciaPago);

            runtimeService.createMessageCorrelation("Message_PagoClienteRenovacion")
                    .processInstanceId(processInstanceId)
                    .setVariables(variables)
                    .correlate();

            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Pago confirmado exitosamente");
            response.put("numeroPoliza", numeroPoliza);
            response.put("estado", "PAGO_PROCESADO");

            LOGGER.info("Pago confirmado y mensaje correlacionado");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LOGGER.severe("Error al confirmar pago: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "No se pudo confirmar el pago");
            error.put("detalle", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Endpoint técnico para simular pago (lo invoca la página bonita)
     * GET: /api/renovacion/simular-pago?poliza=...&monto=...&processInstanceId=...
     */
    @GetMapping("/simular-pago")
    public ResponseEntity<Map<String, String>> simularPago(
            @RequestParam String poliza,
            @RequestParam Double monto,
            @RequestParam String processInstanceId) {

        Map<String, Object> pagoData = new HashMap<>();
        pagoData.put("numeroPoliza", poliza);
        pagoData.put("referenciaPago", "SIM-" + System.currentTimeMillis());
        pagoData.put("monto", monto);
        pagoData.put("processInstanceId", processInstanceId);

        return confirmarPago(pagoData);
    }

    // ==========================
    //  PÁGINAS HTML BONITAS
    // ==========================

    /**
     * Página HTML para que el cliente acepte o rechace la oferta.
     * GET: /api/renovacion/formulario-respuesta?poliza=...&processInstanceId=...
     */
    @GetMapping("/formulario-respuesta")
    public String mostrarFormularioRespuesta(
            @RequestParam String poliza,
            @RequestParam String processInstanceId) {

        return "<!DOCTYPE html>" +
                "<html lang='es'>" +
                "<head>" +
                "  <meta charset='UTF-8' />" +
                "  <meta name='viewport' content='width=device-width, initial-scale=1' />" +
                "  <title>Oferta de Renovación</title>" +
                "  <style>" +
                "    *{box-sizing:border-box;margin:0;padding:0;}" +
                "    body{font-family:system-ui,-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;" +
                "         min-height:100vh;display:flex;align-items:center;justify-content:center;" +
                "         background:radial-gradient(circle at top,#0d6efd 0,#020824 40%,#000 100%);color:#111;}" +
                "    .card{background:#fff;border-radius:18px;max-width:520px;width:100%;padding:32px 28px;" +
                "          box-shadow:0 24px 60px rgba(0,0,0,0.45);position:relative;overflow:hidden;}" +
                "    .card::before{content:'';position:absolute;inset:-40%;background:conic-gradient(from 210deg," +
                "           rgba(13,110,253,0.25),rgba(111,66,193,0.25),rgba(13,202,240,0.25),transparent 60%);" +
                "           opacity:0.9;filter:blur(40px);z-index:-1;}" +
                "    h1{font-size:1.6rem;margin-bottom:8px;color:#0b1437;}" +
                "    h2{font-size:0.95rem;font-weight:500;margin-bottom:20px;color:#6c757d;text-transform:uppercase;letter-spacing:.12em;}" +
                "    .badge{display:inline-flex;align-items:center;gap:6px;padding:4px 10px;border-radius:999px;" +
                "           background:#e7f1ff;color:#0d6efd;font-size:0.75rem;font-weight:600;margin-bottom:18px;}" +
                "    .badge span.icon{width:18px;height:18px;border-radius:999px;background:#0d6efd;color:#fff;" +
                "           display:inline-flex;align-items:center;justify-content:center;font-size:0.8rem;}" +
                "    .poliza{font-weight:600;color:#111827;margin-bottom:8px;font-size:0.95rem;}" +
                "    .texto{font-size:0.95rem;color:#4b5563;line-height:1.6;margin-bottom:20px;}" +
                "    .resumen{background:#f8fafc;border-radius:14px;padding:14px 16px;margin-bottom:22px;font-size:0.9rem;}" +
                "    .resumen-row{display:flex;justify-content:space-between;margin-bottom:6px;}" +
                "    .label{color:#6b7280;}" +
                "    .value{font-weight:600;color:#111827;}" +
                "    .value.monto{font-size:1.05rem;color:#0d6efd;}" +
                "    .acciones{display:flex;flex-wrap:wrap;gap:12px;margin-top:10px;}" +
                "    button{border:none;cursor:pointer;border-radius:999px;padding:12px 22px;font-size:0.95rem;" +
                "           font-weight:600;transition:all .18s ease-out;display:inline-flex;align-items:center;gap:8px;}" +
                "    .btn-primary{background:#0d6efd;color:#fff;box-shadow:0 10px 25px rgba(13,110,253,0.45);}" +
                "    .btn-primary:hover{transform:translateY(-1px);box-shadow:0 16px 40px rgba(13,110,253,0.6);}" +
                "    .btn-ghost{background:transparent;color:#111827;border:1px solid #d1d5db;}" +
                "    .btn-ghost:hover{background:#f3f4f6;}" +
                "    .estado{margin-top:20px;padding:12px 14px;border-radius:12px;font-size:0.9rem;display:none;}" +
                "    .estado.ok{background:#ecfdf3;color:#166534;border:1px solid #bbf7d0;}" +
                "    .estado.ko{background:#fef2f2;color:#b91c1c;border:1px solid #fecaca;}" +
                "    @media(max-width:600px){body{padding:16px;} .card{padding:24px 20px;border-radius:16px;}}" +
                "  </style>" +
                "</head>" +
                "<body>" +
                "<div class='card'>" +
                "  <div class='badge'><span class='icon'>★</span><span>Renovación preferencial</span></div>" +
                "  <h2>Confirmación de oferta</h2>" +
                "  <h1>¿Renovamos tu póliza?</h1>" +
                "  <p class='poliza'>Póliza: " + poliza + "</p>" +
                "  <p class='texto'>Hemos preparado una oferta exclusiva de renovación para mantener tu cobertura activa sin interrupciones. " +
                "     Elige una de las siguientes opciones para continuar.</p>" +
                "  <div class='resumen'>" +
                "     <div class='resumen-row'><span class='label'>Estado actual</span><span class='value'>Pendiente de tu decisión</span></div>" +
                "     <div class='resumen-row'><span class='label'>Próximo paso</span><span class='value'>Aceptar o rechazar oferta</span></div>" +
                "  </div>" +
                "  <div class='acciones'>" +
                "     <button class='btn-primary' onclick=\"responder(true)\">" +
                "        <span>Quiero renovar ahora</span>" +
                "     </button>" +
                "     <button class='btn-ghost' onclick=\"responder(false)\">" +
                "        <span>No deseo renovar</span>" +
                "     </button>" +
                "  </div>" +
                "  <div id='estado' class='estado'></div>" +
                "</div>" +
                "<script>" +
                "function responder(acepta){" +
                "  fetch('/api/renovacion/responder?poliza=" + poliza +
                "&acepta='+acepta+'&processInstanceId=" + processInstanceId + "')" +
                "    .then(r=>r.json())" +
                "    .then(d=>{" +
                "      const box = document.getElementById('estado');" +
                "      box.style.display='block';" +
                "      box.className = 'estado ' + (acepta ? 'ok' : 'ko');" +
                "      box.innerHTML = '<strong>'+d.mensaje+'</strong>';"+
                "    }).catch(e=>{alert('Error: '+e);});" +
                "}" +
                "</script>" +
                "</body></html>";
    }

    /**
     * Página HTML para simular pago del cliente
     * GET: /api/renovacion/formulario-pago?poliza=...&monto=...&processInstanceId=...
     */
    @GetMapping("/formulario-pago")
    public String mostrarFormularioPago(
            @RequestParam String poliza,
            @RequestParam Double monto,
            @RequestParam String processInstanceId) {

        return "<!DOCTYPE html>" +
                "<html lang='es'>" +
                "<head>" +
                "  <meta charset='UTF-8' />" +
                "  <meta name='viewport' content='width=device-width, initial-scale=1' />" +
                "  <title>Pago de Renovación</title>" +
                "  <style>" +
                "    *{box-sizing:border-box;margin:0;padding:0;}" +
                "    body{font-family:system-ui,-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;" +
                "         min-height:100vh;display:flex;align-items:center;justify-content:center;" +
                "         background:linear-gradient(135deg,#020617 0,#0f172a 40%,#1e293b 100%);color:#111;}" +
                "    .card{background:#0b1120;border-radius:20px;max-width:520px;width:100%;padding:30px 26px;" +
                "          box-shadow:0 24px 60px rgba(15,23,42,0.9);position:relative;overflow:hidden;color:#e5e7eb;}" +
                "    .card::before{content:'';position:absolute;inset:-40%;background:radial-gradient(circle at top," +
                "           rgba(56,189,248,0.25),rgba(59,130,246,0.3),transparent 60%);opacity:.9;z-index:-1;}" +
                "    h1{font-size:1.6rem;margin-bottom:10px;color:#f9fafb;}" +
                "    h2{font-size:0.95rem;font-weight:500;margin-bottom:20px;color:#9ca3af;text-transform:uppercase;letter-spacing:.12em;}" +
                "    .chip{display:inline-flex;align-items:center;gap:6px;padding:4px 10px;border-radius:999px;" +
                "          background:rgba(15,23,42,0.85);border:1px solid rgba(148,163,184,0.4);font-size:0.75rem;margin-bottom:18px;}" +
                "    .chip span.icon{width:18px;height:18px;border-radius:999px;background:linear-gradient(135deg,#38bdf8,#0ea5e9);" +
                "          color:#0b1120;display:inline-flex;align-items:center;justify-content:center;font-size:0.8rem;font-weight:700;}" +
                "    .poliza{font-weight:600;color:#e5e7eb;margin-bottom:6px;font-size:0.95rem;}" +
                "    .texto{font-size:0.95rem;color:#cbd5f5;line-height:1.6;margin-bottom:18px;}" +
                "    .resumen{background:rgba(15,23,42,0.9);border-radius:16px;padding:14px 16px;margin-bottom:22px;" +
                "             border:1px solid rgba(148,163,184,0.35);font-size:0.9rem;}" +
                "    .resumen-row{display:flex;justify-content:space-between;margin-bottom:8px;}" +
                "    .label{color:#9ca3af;}" +
                "    .value{font-weight:600;color:#e5e7eb;}" +
                "    .value.monto{font-size:1.15rem;color:#38bdf8;}" +
                "    .acciones{display:flex;flex-wrap:wrap;gap:12px;margin-top:10px;}" +
                "    button{border:none;cursor:pointer;border-radius:999px;padding:11px 22px;font-size:0.95rem;" +
                "           font-weight:600;transition:all .18s ease-out;display:inline-flex;align-items:center;gap:8px;}" +
                "    .btn-primary{background:linear-gradient(135deg,#38bdf8,#0ea5e9);color:#0b1120;box-shadow:0 18px 45px rgba(56,189,248,0.6);}" +
                "    .btn-primary:hover{transform:translateY(-1px);box-shadow:0 22px 55px rgba(56,189,248,0.8);}" +
                "    .btn-ghost{background:transparent;color:#e5e7eb;border:1px solid rgba(148,163,184,0.6);}" +
                "    .btn-ghost:hover{background:rgba(15,23,42,0.9);}" +
                "    .estado{margin-top:20px;padding:12px 14px;border-radius:12px;font-size:0.9rem;display:none;}" +
                "    .estado.ok{background:rgba(22,163,74,0.15);color:#bbf7d0;border:1px solid rgba(34,197,94,0.7);}" +
                "    .estado.ko{background:rgba(248,113,113,0.1);color:#fecaca;border:1px solid rgba(248,113,113,0.8);}" +
                "    @media(max-width:600px){body{padding:16px;} .card{padding:24px 20px;border-radius:18px;}}" +
                "  </style>" +
                "</head>" +
                "<body>" +
                "<div class='card'>" +
                "  <div class='chip'><span class='icon'>₿</span><span>Pasarela de pago segura</span></div>" +
                "  <h2>Confirmación de pago</h2>" +
                "  <h1>Completa tu renovación</h1>" +
                "  <p class='poliza'>Póliza: " + poliza + "</p>" +
                "  <p class='texto'>Estás a un solo paso de mantener tu cobertura activa por un año más. " +
                "     Revisa el monto a pagar y confirma tu pago para finalizar la renovación.</p>" +
                "  <div class='resumen'>" +
                "     <div class='resumen-row'><span class='label'>Monto a pagar</span><span class='value monto'>$" + monto + "</span></div>" +
                "     <div class='resumen-row'><span class='label'>Método</span><span class='value'>Pago online simulado</span></div>" +
                "  </div>" +
                "  <div class='acciones'>" +
                "     <button class='btn-primary' onclick=\"pagar(true)\">Confirmar pago ahora</button>" +
                "     <button class='btn-ghost' onclick=\"pagar(false)\">Cancelar</button>" +
                "  </div>" +
                "  <div id='estadoPago' class='estado'></div>" +
                "</div>" +
                "<script>" +
                "function pagar(acepta){" +
                "  const box = document.getElementById('estadoPago');" +
                "  if(!acepta){" +
                "     box.style.display='block';" +
                "     box.className='estado ko';" +
                "     box.innerHTML='<strong>El pago ha sido cancelado. La póliza seguirá pendiente de renovación.</strong>';"+
                "     return;" +
                "  }" +
                "  fetch('/api/renovacion/simular-pago?poliza=" + poliza +
                "&monto=" + monto + "&processInstanceId=" + processInstanceId + "')" +
                "    .then(r=>r.json())" +
                "    .then(d=>{" +
                "       box.style.display='block';" +
                "       box.className='estado ok';" +
                "       box.innerHTML='<strong>'+d.mensaje+'</strong>';"+
                "    }).catch(e=>{alert('Error: '+e);});" +
                "}" +
                "</script>" +
                "</body></html>";
    }
}
