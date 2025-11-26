package org.example.EmisionSuscripcionPolizas.Controller;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.example.EmisionSuscripcionPolizas.DTO.SolicitudCotizacionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/cotizaciones")
public class SolicitudCotizacionController {

    private final RuntimeService runtimeService;

    public SolicitudCotizacionController(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @PostMapping("/solicitud")
    public ResponseEntity<Map<String, Object>> recibirSolicitud(@RequestBody SolicitudCotizacionDto dto) {

        // Variables que se mandan al proceso
        Map<String, Object> variables = new HashMap<>();
        variables.put("nombreCliente", dto.getNombreCliente());
        variables.put("emailCliente", dto.getEmailCliente());
        variables.put("montoSolicitado", dto.getMontoSolicitado());
        variables.put("producto", dto.getProducto());

        // Business key = número de solicitud (puedes usar otro identificador)
        String businessKey = dto.getNumeroSolicitud();

        // IMPORTANTE: este nombre debe ser el mismo que el message name del Message Start Event
        String messageName = "MSG_SOLICITUD_COTIZACION";

        ProcessInstance instancia = runtimeService
                .startProcessInstanceByMessage(
                        messageName,
                        businessKey,
                        variables
                );

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("processInstanceId", instancia.getId());
        respuesta.put("businessKey", instancia.getBusinessKey());
        respuesta.put("definitionId", instancia.getProcessDefinitionId());

        return ResponseEntity.ok(respuesta);
    }
}
