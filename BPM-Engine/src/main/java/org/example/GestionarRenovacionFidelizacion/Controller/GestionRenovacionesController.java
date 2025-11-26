package org.example.GestionarRenovacionFidelizacion.Controller;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/gestion-renovaciones")
public class GestionRenovacionesController {

    private static final Logger LOGGER = Logger.getLogger(GestionRenovacionesController.class.getName());

    @Autowired
    private RuntimeService runtimeService;

    /**
     * Endpoint para iniciar manualmente el proceso de gestión de renovaciones
     * GET: http://localhost:8080/api/gestion-renovaciones/iniciar
     */
    @GetMapping("/iniciar")
    public ResponseEntity<Map<String, Object>> iniciarGestionRenovaciones() {

        LOGGER.info("=== Iniciando Proceso de Gestión de Renovaciones y Fidelización ===");

        try {
            // Variables iniciales (opcionales, el delegate consultará las pólizas)
            Map<String, Object> variables = new HashMap<>();
            variables.put("fechaInicio", java.time.LocalDateTime.now().toString());
            variables.put("usuarioInicio", "SISTEMA_AUTOMATICO");

            // Iniciar el proceso padre por su Process Definition Key
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                    "Process_1fthirz", // Este es el ID del proceso padre
                    variables
            );

            LOGGER.info("Proceso iniciado - Instance ID: " + processInstance.getId());
            LOGGER.info("Process Definition ID: " + processInstance.getProcessDefinitionId());

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Proceso de Gestión de Renovaciones iniciado exitosamente");
            response.put("processInstanceId", processInstance.getId());
            response.put("processDefinitionId", processInstance.getProcessDefinitionId());
            response.put("businessKey", processInstance.getBusinessKey());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LOGGER.severe("Error al iniciar proceso: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("error", "No se pudo iniciar el proceso");
            error.put("detalle", e.getMessage());

            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Endpoint para consultar el estado de una instancia del proceso
     * GET: http://localhost:8080/api/gestion-renovaciones/estado/{processInstanceId}
     */
    @GetMapping("/estado/{processInstanceId}")
    public ResponseEntity<Map<String, Object>> consultarEstado(@PathVariable String processInstanceId) {

        LOGGER.info("Consultando estado del proceso: " + processInstanceId);

        try {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();

            Map<String, Object> response = new HashMap<>();

            if (processInstance != null) {
                response.put("estado", "ACTIVO");
                response.put("processInstanceId", processInstance.getId());
                response.put("processDefinitionId", processInstance.getProcessDefinitionId());
                response.put("suspended", processInstance.isSuspended());

                // Obtener variables actuales
                Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
                response.put("variables", variables);

            } else {
                // Proceso terminado o no existe
                response.put("estado", "FINALIZADO_O_NO_EXISTE");
                response.put("mensaje", "El proceso no está activo. Puede haber terminado.");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LOGGER.severe("Error al consultar estado: " + e.getMessage());

            Map<String, Object> error = new HashMap<>();
            error.put("error", "No se pudo consultar el estado");
            error.put("detalle", e.getMessage());

            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Endpoint para listar todas las instancias activas de renovaciones
     * GET: http://localhost:8080/api/gestion-renovaciones/listar-activas
     */
    @GetMapping("/listar-activas")
    public ResponseEntity<Map<String, Object>> listarProcesosActivos() {

        LOGGER.info("Listando procesos de renovación activos");

        try {
            long count = runtimeService.createProcessInstanceQuery()
                    .processDefinitionKey("Process_1fthirz")
                    .count();

            Map<String, Object> response = new HashMap<>();
            response.put("totalProcesosActivos", count);
            response.put("processDefinitionKey", "Process_1fthirz");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LOGGER.severe("Error al listar procesos: " + e.getMessage());

            Map<String, Object> error = new HashMap<>();
            error.put("error", "No se pudo listar los procesos");
            error.put("detalle", e.getMessage());

            return ResponseEntity.badRequest().body(error);
        }
    }
}
