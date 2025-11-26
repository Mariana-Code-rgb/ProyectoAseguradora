package org.example.EmisionSuscripcionPolizas.Controller;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/polizas")
public class PolizaController {

    @Autowired
    private RuntimeService runtimeService;

    @PostMapping("/iniciar")
    public ResponseEntity<Map<String, Object>> iniciarEmision(@RequestBody Map<String, Object> variables) {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("emisionSuscripcionPolizas", variables);

        Map<String, Object> response = new HashMap<>();
        response.put("processInstanceId", processInstance.getId());
        response.put("processDefinitionId", processInstance.getProcessDefinitionId());
        response.put("mensaje", "Proceso de Emisión y Suscripción de Pólizas iniciado exitosamente");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/respuesta-cliente/{processInstanceId}")
    public ResponseEntity<Map<String, String>> enviarRespuestaCliente(
            @PathVariable String processInstanceId,
            @RequestBody Map<String, Object> variables) {

        runtimeService.createMessageCorrelation("MSG_RESPUESTA_CLIENTE")
                .processInstanceId(processInstanceId)
                .setVariables(variables)
                .correlate();

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Respuesta del cliente correlacionada exitosamente");
        response.put("processInstanceId", processInstanceId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/contrato-firmado/{processInstanceId}")
    public ResponseEntity<Map<String, String>> enviarContratoFirmado(@PathVariable String processInstanceId) {
        runtimeService.createMessageCorrelation("MSG_CONTRATO_FIRMADO")
                .processInstanceId(processInstanceId)
                .correlate();

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Contrato firmado correlacionado exitosamente");
        response.put("processInstanceId", processInstanceId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirmacion-pago/{processInstanceId}")
    public ResponseEntity<Map<String, String>> enviarConfirmacionPago(@PathVariable String processInstanceId) {
        runtimeService.createMessageCorrelation("MSG_CONFIRMACION_PAGO")
                .processInstanceId(processInstanceId)
                .correlate();

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Confirmación de pago correlacionada exitosamente");
        response.put("processInstanceId", processInstanceId);

        return ResponseEntity.ok(response);
    }
}
