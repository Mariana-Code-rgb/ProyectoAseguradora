package org.example.EmisionSuscripcionPolizas.Controller;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ConfirmacionPagoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfirmacionPagoController.class);

    @Autowired
    private RuntimeService runtimeService;

    // GET /api/confirmacion-pago?processId=...&pagado=true
    @GetMapping(value = "/confirmacion-pago", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> confirmarPago(
            @RequestParam String processId,
            @RequestParam boolean pagado) {

        try {
            LOGGER.info("═══════════════════════════════════════════════════");
            LOGGER.info("💳 CONFIRMACIÓN DE PAGO RECIBIDA");
            LOGGER.info("Process ID: {}", processId);
            LOGGER.info("Pago: {}", pagado ? "PAGADO ✅" : "NO PAGADO ❌");

            Map<String, Object> vars = new HashMap<>();
            vars.put("pagoConfirmado", pagado);
            vars.put("fechaConfirmacionPago", Instant.now().toString());
            vars.put("medioConfirmacionPago", "link");

            // IMPORTANTE: usar correlateAllWithResult()
            List<MessageCorrelationResult> results =
                    runtimeService.createMessageCorrelation("Message_ConfirmacionPago")
                            .processInstanceId(processId)
                            .setVariables(vars)
                            .correlateAllWithResult();

            if (results == null || results.isEmpty()) {
                String pending = """
          <html><body style='font-family:Arial;text-align:center;padding:50px'>
            <h1>⏳ Procesando</h1>
            <p>Tu confirmación ha sido registrada y se aplicará en cuanto el sistema esté listo.</p>
          </body></html>
        """;
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(pending); // 202
            }

            LOGGER.info("✅ Mensaje 'Message_ConfirmacionPago' correlacionado");
            LOGGER.info("═══════════════════════════════════════════════════");

            String estado = pagado ? "PAGADO" : "NO PAGADO";
            String emoji  = pagado ? "✅" : "❌";
            String ok = """
        <html><body style='font-family:Arial;text-align:center;padding:50px'>
          <h1>%s Pago %s</h1>
          <p>Tu respuesta ha sido registrada correctamente.</p>
        </body></html>
      """.formatted(emoji, estado);

            return ResponseEntity.ok(ok);

        } catch (Exception e) {
            LOGGER.error("❌ Error al procesar confirmación de pago: {}", e.getMessage(), e);
            String err = """
        <html><body style='font-family:Arial;text-align:center;padding:50px'>
          <h1>❌ Error</h1>
          <p>No se pudo registrar la confirmación. Intenta más tarde.</p>
        </body></html>
      """;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }
}
