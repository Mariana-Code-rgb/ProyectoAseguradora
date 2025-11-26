package org.example.GestionarReclamaciones.Controller;

import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/reclamaciones")
public class ReclamacionesController {

    private static final Logger LOGGER =
            Logger.getLogger(ReclamacionesController.class.getName());

    @Autowired
    private RuntimeService runtimeService;

    /**
     * Página HTML para que el cliente pague el deducible.
     * Ejemplo de link (lo arma el delegate):
     * http://localhost:8080/api/reclamaciones/formulario-deducible?poliza=POL-2024-001&monto=300&processInstanceId=...
     */
    @GetMapping("/formulario-deducible")
    public String mostrarFormularioDeducible(
            @RequestParam String poliza,
            @RequestParam Double monto,
            @RequestParam String processInstanceId) {

        return "<!DOCTYPE html>" +
                "<html lang='es'><head><meta charset='UTF-8'><title>Pagar Deducible</title>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1'/>" +
                "<style>" +
                "body{margin:0;font-family:system-ui,-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;" +
                "min-height:100vh;display:flex;align-items:center;justify-content:center;" +
                "background:radial-gradient(circle at top,#0f172a 0,#020617 45%,#000 100%);}" +
                ".card{background:#0b1120;color:#e5e7eb;max-width:520px;width:100%;padding:28px 24px;" +
                "border-radius:18px;box-shadow:0 24px 60px rgba(15,23,42,0.9);position:relative;overflow:hidden;}" +
                ".chip{display:inline-flex;align-items:center;gap:6px;padding:4px 10px;border-radius:999px;" +
                "background:rgba(15,23,42,.9);border:1px solid rgba(148,163,184,.6);font-size:.75rem;margin-bottom:16px;}" +
                ".chip span.icon{width:18px;height:18px;border-radius:999px;background:linear-gradient(135deg,#38bdf8,#0ea5e9);" +
                "display:inline-flex;align-items:center;justify-content:center;font-size:.8rem;color:#0b1120;font-weight:700;}" +
                "h1{font-size:1.4rem;margin:0 0 6px;color:#f9fafb;}" +
                "p.sub{margin:0 0 14px;font-size:.9rem;color:#cbd5f5;}" +
                ".field-row{display:flex;justify-content:space-between;margin:6px 0;font-size:.9rem;}" +
                ".label{color:#9ca3af;}.value{font-weight:600;color:#e5e7eb;}" +
                ".value.monto{font-size:1.15rem;color:#38bdf8;}" +
                ".actions{margin-top:20px;display:flex;flex-wrap:wrap;gap:12px;}" +
                "button{border:none;border-radius:999px;padding:11px 22px;font-size:.95rem;font-weight:600;" +
                "cursor:pointer;transition:all .18s ease-out;display:inline-flex;align-items:center;gap:8px;}" +
                ".btn-primary{background:linear-gradient(135deg,#38bdf8,#0ea5e9);color:#0b1120;" +
                "box-shadow:0 18px 45px rgba(56,189,248,.6);}" +
                ".btn-primary:hover{transform:translateY(-1px);box-shadow:0 22px 55px rgba(56,189,248,.8);}" +
                ".btn-ghost{background:transparent;color:#e5e7eb;border:1px solid rgba(148,163,184,.7);}" +
                ".btn-ghost:hover{background:rgba(15,23,42,.9);}" +
                ".status{margin-top:18px;padding:12px 14px;border-radius:12px;font-size:.9rem;display:none;}" +
                ".status.ok{background:rgba(22,163,74,.15);color:#bbf7d0;border:1px solid rgba(34,197,94,.7);}" +
                ".status.ko{background:rgba(248,113,113,.1);color:#fecaca;border:1px solid rgba(248,113,113,.8);}" +
                "@media(max-width:600px){.card{margin:16px;padding:22px 18px;border-radius:16px;}}" +
                "</style></head><body>" +
                "<div class='card'>" +
                " <div class='chip'><span class='icon'>₿</span><span>Pago de deducible</span></div>" +
                " <h1>Pagar deducible de reclamación</h1>" +
                " <p class='sub'>Confirma el pago para que podamos continuar con la gestión de tu reclamación.</p>" +
                " <div class='field-row'><span class='label'>Póliza</span><span class='value'>" + poliza + "</span></div>" +
                " <div class='field-row'><span class='label'>Monto del deducible</span>" +
                "      <span class='value monto'>$" + monto + "</span></div>" +
                " <div class='actions'>" +
                "   <button class='btn-primary' onclick=\"pagar(true)\">Pagar ahora</button>" +
                "   <button class='btn-ghost' onclick=\"pagar(false)\">No pagar</button>" +
                " </div>" +
                " <div id='statusBox' class='status'></div>" +
                "</div>" +
                "<script>" +
                "function pagar(acepta){" +
                "  const box=document.getElementById('statusBox');" +
                "  if(!acepta){" +
                "    box.style.display='block';" +
                "    box.className='status ko';" +
                "    box.innerHTML='<strong>Has decidido no pagar el deducible. La reclamación podrá ser rechazada por vencimiento.</strong>';"+
                "    return;" +
                "  }" +
                "  fetch('/api/reclamaciones/simular-pago-deducible?poliza=" + poliza +
                "&monto=" + monto + "&processInstanceId=" + processInstanceId + "')" +
                "    .then(r=>r.json())" +
                "    .then(d=>{" +
                "      box.style.display='block';" +
                "      box.className='status ok';" +
                "      box.innerHTML='<strong>'+d.mensaje+'</strong>';"+
                "    }).catch(e=>{alert('Error: '+e);});" +
                "}" +
                "</script></body></html>";
    }

    /**
     * Endpoint de demo que confirma el pago de deducible y
     * correlaciona el mensaje Message_ConfirmacionPagoDeducible
     * con el evento intermedio Event_0k717jy.
     */
    @GetMapping("/simular-pago-deducible")
    public ResponseEntity<Map<String, String>> simularPagoDeducible(
            @RequestParam String poliza,
            @RequestParam Double monto,
            @RequestParam String processInstanceId) {

        LOGGER.info("=== Confirmación de pago de deducible recibida ===");
        LOGGER.info("Póliza=" + poliza + ", monto=" + monto +
                ", processInstanceId=" + processInstanceId);

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("deduciblePagado", true);
            variables.put("montoDeduciblePagado", monto);
            variables.put("fechaPagoDeducible", java.time.LocalDateTime.now().toString());
            variables.put("referenciaPagoDeducible", "DED-" + System.currentTimeMillis());

            runtimeService.createMessageCorrelation("Message_ConfirmacionPagoDeducible")
                    .processInstanceId(processInstanceId)
                    .setVariables(variables)
                    .correlate();

            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Pago de deducible registrado correctamente. Continuaremos con la gestión de su reclamación.");
            response.put("poliza", poliza);
            response.put("estado", "DEDUCIBLE_PAGADO");

            LOGGER.info("Mensaje Message_ConfirmacionPagoDeducible correlacionado correctamente");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LOGGER.severe("Error al correlacionar mensaje de pago de deducible: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "No se pudo registrar el pago de deducible");
            error.put("detalle", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
