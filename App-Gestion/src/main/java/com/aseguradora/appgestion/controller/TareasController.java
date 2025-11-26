package com.aseguradora.appgestion.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class TareasController {

    private static final String ENGINE_URL = "http://localhost:9000/engine-rest";

    // IDs de los procesos en el BPMN (ajusta si cambian)
    private static final String PROC_EMISION   = "Process_0xmqxvr"; // Emisión y Suscripción
    private static final String PROC_RECLAMOS  = "Process_0gif700"; // Gestión de Reclamaciones
    private static final String PROC_RENOVACION = "GeReIn";        // Gestión de Renovación Individual

    // Home con el panel de procesos
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // =======================
    //  EMISIÓN Y SUSCRIPCIÓN
    // =======================

    @GetMapping("/emision/tareas")
    public String listarTareasEmision(Model model) {
        RestTemplate restTemplate = new RestTemplate();

        String url = ENGINE_URL + "/task?processDefinitionKey=" + PROC_EMISION;
        List<Map<String, Object>> tareas = restTemplate.getForObject(url, List.class);

        model.addAttribute("tareas", tareas);
        model.addAttribute("tituloBandeja", "Tareas de Emisión y Suscripción");
        model.addAttribute("urlBase", "/emision");
        return "emision/bandeja";
    }

    @GetMapping("/emision/tarea/{taskId}")
    public String abrirTareaEmision(@PathVariable String taskId, Model model) {
        return abrirTareaGenerica(taskId, model);
    }

    @PostMapping("/emision/tarea/{taskId}/complete")
    public String completarTareaEmision(@PathVariable String taskId, HttpServletRequest request) {
        completarTareaGenerica(taskId, request);
        return "redirect:/emision/tareas";
    }

    @PostMapping("/emision/tarea/{taskId}/eliminar")
    public String eliminarTareaEmision(@PathVariable String taskId) {
        eliminarTareaGenerica(taskId);
        return "redirect:/emision/tareas";
    }

    // ========================
    //  GESTIÓN DE RECLAMACIONES
    // ========================

    @GetMapping("/reclamaciones/tareas")
    public String listarTareasReclamaciones(Model model) {
        RestTemplate restTemplate = new RestTemplate();

        String url = ENGINE_URL + "/task?processDefinitionKey=" + PROC_RECLAMOS;
        List<Map<String, Object>> tareas = restTemplate.getForObject(url, List.class);

        model.addAttribute("tareas", tareas);
        model.addAttribute("tituloBandeja", "Tareas de Gestión de Reclamaciones");
        model.addAttribute("urlBase", "/reclamaciones");
        return "reclamaciones/bandeja";
    }

    @GetMapping("/reclamaciones/tarea/{taskId}")
    public String abrirTareaReclamacion(@PathVariable String taskId, Model model) {
        return abrirTareaGenerica(taskId, model);
    }

    @PostMapping("/reclamaciones/tarea/{taskId}/complete")
    public String completarTareaReclamacion(@PathVariable String taskId, HttpServletRequest request) {
        completarTareaGenerica(taskId, request);
        return "redirect:/reclamaciones/tareas";
    }

    @PostMapping("/reclamaciones/tarea/{taskId}/eliminar")
    public String eliminarTareaReclamacion(@PathVariable String taskId) {
        eliminarTareaGenerica(taskId);
        return "redirect:/reclamaciones/tareas";
    }

    @PostMapping("/reclamaciones/confirmar-pago-deducible")
    public String confirmarPagoDeducible(HttpServletRequest request) {

        String processInstanceId = request.getParameter("processInstanceId");
        if (processInstanceId == null || processInstanceId.isBlank()) {
            return "redirect:/reclamaciones/tareas";
        }

        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> body = new HashMap<>();
        body.put("messageName", "Message_ConfirmacionPagoDeducible");
        body.put("processInstanceId", processInstanceId);

        restTemplate.postForEntity(ENGINE_URL + "/message", body, Void.class);

        return "redirect:/reclamaciones/tareas";
    }

    // =========================
    //  RENOVACIONES Y FIDELIZACIÓN
    // =========================

    @GetMapping("/renovaciones/tareas")
    public String listarTareasRenovaciones(Model model) {
        RestTemplate restTemplate = new RestTemplate();

        // Tareas del proceso de Gestión de Renovación Individual (GeReIn)
        String url = ENGINE_URL + "/task?processDefinitionKey=" + PROC_RENOVACION;
        List<Map<String, Object>> tareas = restTemplate.getForObject(url, List.class);

        model.addAttribute("tareas", tareas);
        model.addAttribute("tituloBandeja", "Tareas de Renovaciones y Fidelización");
        model.addAttribute("urlBase", "/renovaciones");
        return "renovaciones/bandeja";
    }

    @GetMapping("/renovaciones/tarea/{taskId}")
    public String abrirTareaRenovacion(@PathVariable String taskId, Model model) {
        return abrirTareaGenerica(taskId, model);
    }

    @PostMapping("/renovaciones/tarea/{taskId}/complete")
    public String completarTareaRenovacion(@PathVariable String taskId, HttpServletRequest request) {
        completarTareaGenerica(taskId, request);
        return "redirect:/renovaciones/tareas";
    }

    @PostMapping("/renovaciones/tarea/{taskId}/eliminar")
    public String eliminarTareaRenovacion(@PathVariable String taskId) {
        eliminarTareaGenerica(taskId);
        return "redirect:/renovaciones/tareas";
    }

    // =======================
    //  MÉTODOS COMPARTIDOS
    // =======================

    private String abrirTareaGenerica(String taskId, Model model) {
        RestTemplate restTemplate = new RestTemplate();

        String urlTask = ENGINE_URL + "/task/" + taskId;
        Map<String, Object> task = restTemplate.getForObject(urlTask, Map.class);
        System.out.println("==== TAREA DESDE CAMUNDA ====");
        System.out.println(task);

        String urlVars = ENGINE_URL + "/task/" + taskId + "/form-variables";
        Map<String, Object> variables = restTemplate.getForObject(urlVars, Map.class);
        if (variables == null) {
            variables = new HashMap<>();
        }

        model.addAttribute("taskId", taskId);
        model.addAttribute("variables", variables);

        String formKey = task != null ? (String) task.get("formKey") : null;
        System.out.println("formKey = " + formKey);

        if (formKey != null && formKey.startsWith("app:")) {
            String viewName = formKey.replace("app:", "");
            System.out.println("View a renderizar = " + viewName);
            return viewName;
        }

        // Fallback temporal
        System.out.println("Usando fallback: forms/elaborarCotizacion");
        return "forms/elaborarCotizacion";
    }

    private void completarTareaGenerica(String taskId, HttpServletRequest request) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String[]> params = request.getParameterMap();
        Map<String, Object> variables = new HashMap<>();

        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String name = entry.getKey();
            if ("_csrf".equals(name)) continue;

            String value = entry.getValue()[0];
            Map<String, Object> v = new HashMap<>();

            if ("montoAsegurado".equals(name) || "primaAnual".equals(name) ||
                    "montoAprobado".equals(name) || "deducible".equals(name) ||
                    "montoEstimado".equals(name) || "montoReclamado".equals(name) ||
                    "montoPagoTaller".equals(name)) {

                Double numero = null;
                if (value != null && !value.isBlank()) {
                    numero = Double.parseDouble(value);
                }
                v.put("value", numero);
                v.put("type", "Double");

            } else if ("polizaValida".equals(name) ||
                    "reclamacionAprobada".equals(name) ||
                    "requiereSupervisor".equals(name) ||
                    "seLogroRetener".equals(name)) {   // ← aquí se agrega

                Boolean bool = null;
                if (value != null && !value.isBlank()) {
                    bool = Boolean.parseBoolean(value);
                }
                v.put("value", bool);
                v.put("type", "Boolean");

            } else {
                v.put("value", value);
                v.put("type", "String");
            }

            variables.put(name, v);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("variables", variables);

        String urlComplete = ENGINE_URL + "/task/" + taskId + "/complete";
        restTemplate.postForEntity(urlComplete, body, Void.class);
    }

    private void eliminarTareaGenerica(String taskId) {
        RestTemplate restTemplate = new RestTemplate();

        String urlTask = ENGINE_URL + "/task/" + taskId;
        Map<String, Object> task = restTemplate.getForObject(urlTask, Map.class);

        if (task != null && task.get("processInstanceId") != null) {
            String processInstanceId = (String) task.get("processInstanceId");
            String urlDelete = ENGINE_URL + "/process-instance/" + processInstanceId
                    + "?skipCustomListeners=true&skipIoMappings=true";
            restTemplate.delete(urlDelete);
        }
    }
}
