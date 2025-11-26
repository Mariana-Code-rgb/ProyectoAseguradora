package org.example.EmisionSuscripcionPolizas.Controller;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;          // <-- added
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RespuestaClienteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RespuestaClienteController.class);

    @Autowired
    private RuntimeService runtimeService;

    @GetMapping("/respuesta-cliente")
    public ResponseEntity<String> recibirRespuesta(            // changed return type
                                                               @RequestParam String processId,
                                                               @RequestParam boolean acepta) {

        try {
            LOGGER.info("═══════════════════════════════════════════════════");
            LOGGER.info("📨 RESPUESTA DEL CLIENTE RECIBIDA");
            LOGGER.info("Process ID: {}", processId);
            LOGGER.info("Respuesta: {}", acepta ? "ACEPTÓ ✅" : "RECHAZÓ ❌");

            // 1. ¿Existe una ejecución esperando por el mensaje?
            Execution waitingExecution = runtimeService
                    .createExecutionQuery()
                    .processInstanceId(processId)
                    .messageEventSubscriptionName("Message_RespuestaCliente")
                    .singleResult();

            if (waitingExecution == null) {
                LOGGER.warn("⚠️  No hay ejecución a la espera de Message_RespuestaCliente para el proceso {}", processId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("<html><body style='font-family:Arial;text-align:center;padding:50px'>"
                                + "<h1>⚠️ Proceso no encontrado</h1>"
                                + "<p>No se encontró un proceso esperando su respuesta.</p>"
                                + "</body></html>");
            }

            // 2. Preparar variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("clienteAcepta", acepta);

            // 3. Correlacionar mensaje
            runtimeService.createMessageCorrelation("Message_RespuestaCliente")
                    .processInstanceId(processId)
                    .setVariables(variables)
                    .correlate();

            LOGGER.info("✅ Mensaje correlacionado exitosamente");
            LOGGER.info("═══════════════════════════════════════════════════");

            // 4. Retornar página HTML de confirmación
            String respuesta = acepta ? "aceptada" : "rechazada";
            String emoji = acepta ? "✅" : "❌";

            String html = String.format(
                    "<html><head><meta charset='UTF-8'></head><body style='font-family:Arial;text-align:center;padding:50px'>"
                            + "<h1>%s Respuesta Recibida</h1>"
                            + "<p>Su respuesta ha sido <strong>%s</strong> exitosamente.</p>"
                            + "<p>Nos pondremos en contacto con usted a la brevedad.</p>"
                            + "<p style='color:#666;font-size:12px;margin-top:50px'>Puede cerrar esta ventana.</p>"
                            + "</body></html>",
                    emoji, respuesta
            );
            return ResponseEntity.ok(html);

        } catch (Exception e) {
            LOGGER.error("❌ Error al procesar respuesta: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("<html><body style='font-family:Arial;text-align:center;padding:50px'>"
                            + "<h1>❌ Error</h1>"
                            + "<p>No se pudo procesar su respuesta. Por favor, contacte con nosotros.</p>"
                            + "</body></html>");
        }
    }
}