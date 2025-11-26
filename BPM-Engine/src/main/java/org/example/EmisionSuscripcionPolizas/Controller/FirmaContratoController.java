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
public class FirmaContratoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FirmaContratoController.class);

    @Autowired
    private RuntimeService runtimeService;

    // GET /api/firmar-contrato?processId=...&firmado=true
    @GetMapping(value = "/firmar-contrato", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> firmarContrato(
            @RequestParam String processId,
            @RequestParam(defaultValue = "true") boolean firmado) {

        try {
            LOGGER.info("═══════════════════════════════════════════════════");
            LOGGER.info("✍️ FIRMA DE CONTRATO RECIBIDA");
            LOGGER.info("Process ID: {}", processId);
            LOGGER.info("Firmado: {}", firmado ? "FIRMADO ✅" : "NO FIRMADO ❌");

            Map<String, Object> vars = new HashMap<>();
            vars.put("contratoFirmado", firmado);
            vars.put("fechaFirmaContrato", Instant.now().toString());
            vars.put("medioFirma", "link");

            // IMPORTANTE: usar correlateAllWithResult() (no correlateAll())
            List<MessageCorrelationResult> results =
                    runtimeService.createMessageCorrelation("Message_ContratoFirmado")
                            .processInstanceId(processId)
                            .setVariables(vars)
                            .correlateAllWithResult();

            if (results == null || results.isEmpty()) {
                String pending = """
          <html><body style='font-family:Arial;text-align:center;padding:50px'>
            <h1>⏳ Procesando</h1>
            <p>Tu firma ha sido registrada y se aplicará en cuanto el sistema esté listo.</p>
          </body></html>
        """;
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(pending); // 202
            }

            LOGGER.info("✅ Mensaje 'Message_ContratoFirmado' correlacionado");
            LOGGER.info("═══════════════════════════════════════════════════");

            String estado = firmado ? "FIRMADO" : "NO FIRMADO";
            String emoji  = firmado ? "✅" : "❌";
            String ok = """
        <html><body style='font-family:Arial;text-align:center;padding:50px'>
          <h1>%s Contrato %s</h1>
          <p>Tu respuesta ha sido registrada correctamente.</p>
        </body></html>
      """.formatted(emoji, estado);

            return ResponseEntity.ok(ok);

        } catch (Exception e) {
            LOGGER.error("❌ Error al procesar firma: {}", e.getMessage(), e);
            String err = """
        <html><body style='font-family:Arial;text-align:center;padding:50px'>
          <h1>❌ Error</h1>
          <p>No se pudo registrar la firma. Intenta más tarde.</p>
        </body></html>
      """;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }
}
