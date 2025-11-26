package com.aseguradora.appcliente.controller;

import org.example.dto.SolicitudCotizacionDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Controller
public class SolicitudController {

    // Apunta a tu BPM-Engine (Puerto 9000)
    private final String ENGINE_URL = "http://localhost:9000/engine-rest";

    // ID exacto del "Message Name" que definiste en tu BPMN (Ver tu última imagen)
    private final String MESSAGE_NAME = "MSG_SOLICITUD_COTIZACION";

    @GetMapping("/")
    public String index() {
        return "index"; // Muestra src/main/resources/templates/index.html
    }

    @GetMapping("/solicitar")
    public String mostrarFormulario(Model model) {
        // Inicializamos el DTO vacío para que Thymeleaf pueda enlazar los campos
        model.addAttribute("solicitud", new SolicitudCotizacionDTO());
        return "solicitar-cotizacion"; // Muestra src/main/resources/templates/solicitar-cotizacion.html
    }

    @PostMapping("/iniciar-tramite")
    public String iniciarTramite(@ModelAttribute SolicitudCotizacionDTO solicitud, Model model) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // 1. Preparar el cuerpo del mensaje para Camunda
            // https://docs.camunda.org/manual/7.21/reference/rest/message/post-message/
            Map<String, Object> messageBody = new HashMap<>();
            messageBody.put("messageName", MESSAGE_NAME);

            // Opcional: businessKey para rastrear (usamos el email por ejemplo)
            messageBody.put("businessKey", solicitud.getEmailCliente());

            // 2. Convertir los datos del DTO a variables de proceso Camunda
            Map<String, Object> processVariables = new HashMap<>();

            processVariables.put("nombreCliente", Map.of("value", solicitud.getNombreCliente(), "type", "String"));
            processVariables.put("emailCliente", Map.of("value", solicitud.getEmailCliente(), "type", "String"));
            processVariables.put("tipoProducto", Map.of("value", solicitud.getTipoProducto(), "type", "String"));

            // Manejo seguro de Double (Monto Asegurado)
            if (solicitud.getMontoAsegurado() != null) {
                processVariables.put("montoAsegurado", Map.of("value", solicitud.getMontoAsegurado(), "type", "Double"));
            } else {
                processVariables.put("montoAsegurado", Map.of("value", 0.0, "type", "Double"));
            }

            // Agregamos las variables al cuerpo del mensaje
            messageBody.put("processVariables", processVariables);

            // 3. Enviar la petición POST al BPM-Engine
            String url = ENGINE_URL + "/message";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(messageBody, headers);

            // Usamos Void.class porque Camunda devuelve 204 No Content (vacío)
            restTemplate.postForEntity(url, entity, Void.class);


            // 4. Mostrar éxito
            model.addAttribute("mensaje", "¡Solicitud recibida correctamente! Te contactaremos pronto.");
            return "exito"; // Muestra src/main/resources/templates/exito.html

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Hubo un error al procesar tu solicitud: " + e.getMessage());
            return "error"; // Muestra src/main/resources/templates/error.html (créalo si no existe)
        }
    }
}
