package com.aseguradora.appcliente.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Controller
public class ReclamacionesClienteController {

    private final String ENGINE_URL = "http://localhost:9000/engine-rest";

    // 1) Formulario para notificar un siniestro
    @GetMapping("/portal/reclamaciones/nueva")
    public String nuevaReclamacion(
            @RequestParam(required = false) String email,
            Model model) {

        // Si llega el email por query string, lo dejamos precargado
        if (email != null && !email.isBlank()) {
            model.addAttribute("email", email);
        }
        return "portal/reclamacion-nueva";
    }

    // 2) Enviar la notificación de siniestro al motor (mensaje de inicio)
    @PostMapping("/portal/reclamaciones/enviar")
    public String enviarReclamacion(
            @RequestParam String emailCliente,
            @RequestParam String nombreCliente,
            @RequestParam String telefonoContacto,
            @RequestParam String numeroPoliza,
            @RequestParam String tipoSiniestro,
            @RequestParam String fechaSiniestro,
            @RequestParam String descripcionSiniestro) {   // SIN montoReclamado aquí

        RestTemplate restTemplate = new RestTemplate();

        try {
            Map<String, Object> vars = new HashMap<>();

            vars.put("numeroPoliza", Map.of(
                    "value", numeroPoliza,
                    "type", "String"));

            vars.put("nombreAsegurado", Map.of(
                    "value", nombreCliente,
                    "type", "String"));

            vars.put("fechaSiniestro", Map.of(
                    "value", fechaSiniestro,
                    "type", "String"));

            vars.put("tipoSiniestro", Map.of(
                    "value", tipoSiniestro,
                    "type", "String"));

            // Como en tu formulario no hay monto, lo dejamos en 0 para el proceso
            vars.put("montoReclamado", Map.of(
                    "value", 0.0,
                    "type", "Double"));

            vars.put("descripcionSiniestro", Map.of(
                    "value", descripcionSiniestro,
                    "type", "String"));

            vars.put("emailCliente", Map.of(
                    "value", emailCliente,
                    "type", "String"));

            vars.put("telefonoContacto", Map.of(
                    "value", telefonoContacto,
                    "type", "String"));

            Map<String, Object> body = new HashMap<>();
            body.put("messageName", "Message_NotificacionSiniestro");
            body.put("processVariables", vars);

            restTemplate.postForEntity(ENGINE_URL + "/message", body, Void.class); // [web:361][web:256]
            return "redirect:/portal/acceso?email=" + emailCliente;

        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    // 3) Confirmar pago de deducible (dispara el mensaje intermedio)
    @PostMapping("/portal/reclamaciones/confirmar-pago-deducible")
    public String confirmarPagoDeducible(
            @RequestParam String processInstanceId,
            @RequestParam String emailCliente) {

        RestTemplate restTemplate = new RestTemplate();

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("messageName", "Message_ConfirmacionPagoDeducible");
            body.put("processInstanceId", processInstanceId);

            restTemplate.postForEntity(ENGINE_URL + "/message", body, Void.class);

            return "redirect:/portal/acceso?email=" + emailCliente;

        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    // 4) Confirmar pago de deducible (dispara el mensaje intermedio)
    @GetMapping("/portal/reclamaciones/formulario-deducible")
    public String mostrarFormularioDeducible(@RequestParam String processInstanceId,
                                             @RequestParam String email,
                                             Model model) {
        model.addAttribute("processInstanceId", processInstanceId);
        model.addAttribute("emailCliente", email);
        return "portal/pago-deducible";  // nombre del HTML en templates/portal/
    }

}
