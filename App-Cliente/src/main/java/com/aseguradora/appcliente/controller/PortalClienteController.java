package com.aseguradora.appcliente.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PortalClienteController {

    private final String ENGINE_URL = "http://localhost:9000/engine-rest";

    // 1) Pantalla de acceso al portal de cliente (login por email)
    @GetMapping("/portal/acceso")
    public String accesoPortal(@RequestParam(required = false) String email, Model model) {
        if (email != null && !email.isBlank()) {
            model.addAttribute("email", email);
            return "forward:/portal/mis-tramites";
        }
        return "portal/acceso";
    }

    @GetMapping("/portal/mis-tramites")
    public String verTramitesGet(@RequestParam String email, Model model) {
        return verTramites(email, model);
    }

    // 2) Bandeja de trámites del cliente
    @PostMapping("/portal/mis-tramites")
    public String verTramites(@RequestParam String email, Model model) {
        RestTemplate restTemplate = new RestTemplate();
        List<Map<String, Object>> listaFinal = new ArrayList<>();
        Map<String, Object> ultimaNotificacion = null;

        try {
            // 2.1) User tasks asignadas al email
            String urlTasks = ENGINE_URL + "/task?assignee=" + email;
            List<Map<String, Object>> tasks = restTemplate.getForObject(urlTasks, List.class);
            if (tasks != null) {
                for (Map<String, Object> t : tasks) {
                    t.put("esMensaje", false);
                    listaFinal.add(t);
                }
            }

            // 2.2) Mensaje: Respuesta a Cotización
            String urlRespCoti = ENGINE_URL
                    + "/execution?processVariables=emailCliente_eq_" + email
                    + "&messageEventSubscriptionName=Message_RespuestaCliente";

            List<Map<String, Object>> ejecRespCoti =
                    restTemplate.getForObject(urlRespCoti, List.class);

            if (ejecRespCoti != null) {
                for (Map<String, Object> exe : ejecRespCoti) {
                    Map<String, Object> tareaFalsa = new HashMap<>();
                    tareaFalsa.put("id", exe.get("id"));           // executionId
                    tareaFalsa.put("name", "Responder a Cotización");
                    tareaFalsa.put("created", "En espera de tu respuesta");
                    tareaFalsa.put("esMensaje", true);
                    tareaFalsa.put("tipoMensaje", "RESPUESTA_COTI");
                    listaFinal.add(tareaFalsa);
                }
            }

            // 2.3) Mensaje: Contrato Firmado Recibido
            String urlContrato = ENGINE_URL
                    + "/execution?processVariables=emailCliente_eq_" + email
                    + "&messageEventSubscriptionName=Message_ContratoFirmado";

            List<Map<String, Object>> ejecContrato =
                    restTemplate.getForObject(urlContrato, List.class);

            if (ejecContrato != null) {
                for (Map<String, Object> exe : ejecContrato) {
                    Map<String, Object> tareaFalsa = new HashMap<>();
                    tareaFalsa.put("id", exe.get("id"));
                    tareaFalsa.put("name", "Confirmar Firma de Contrato");
                    tareaFalsa.put("created", "Contrato enviado para firma");
                    tareaFalsa.put("esMensaje", true);
                    tareaFalsa.put("tipoMensaje", "CONTRATO_FIRMA");
                    listaFinal.add(tareaFalsa);
                }
            }

            // 2.4) Mensaje: Confirmación de Pago (emisión)
            String urlPago = ENGINE_URL
                    + "/execution?processVariables=emailCliente_eq_" + email
                    + "&messageEventSubscriptionName=Message_ConfirmacionPago";

            List<Map<String, Object>> ejecPago =
                    restTemplate.getForObject(urlPago, List.class);

            if (ejecPago != null) {
                for (Map<String, Object> exe : ejecPago) {
                    Map<String, Object> tareaFalsa = new HashMap<>();
                    tareaFalsa.put("id", exe.get("id"));
                    tareaFalsa.put("name", "Confirmar Pago de la Póliza");
                    tareaFalsa.put("created", "Pago pendiente de confirmación");
                    tareaFalsa.put("esMensaje", true);
                    tareaFalsa.put("tipoMensaje", "CONFIRMACION_PAGO");
                    listaFinal.add(tareaFalsa);
                }
            }

            // 2.5) Mensaje: Confirmación de Pago de Deducible (reclamaciones)
            String urlPagoDeducible = ENGINE_URL
                    + "/execution?processVariables=emailCliente_eq_" + email
                    + "&messageEventSubscriptionName=Message_ConfirmacionPagoDeducible";

            List<Map<String, Object>> ejecPagoDed =
                    restTemplate.getForObject(urlPagoDeducible, List.class);

            if (ejecPagoDed != null) {
                for (Map<String, Object> exe : ejecPagoDed) {
                    Map<String, Object> tareaFalsa = new HashMap<>();
                    tareaFalsa.put("id", exe.get("id"));  // executionId
                    tareaFalsa.put("processInstanceId", exe.get("processInstanceId"));
                    tareaFalsa.put("name", "Pagar deducible de la reclamación");
                    tareaFalsa.put("created", "Pago de deducible pendiente");
                    tareaFalsa.put("esMensaje", true);
                    tareaFalsa.put("tipoMensaje", "PAGO_DEDUCIBLE");
                    listaFinal.add(tareaFalsa);
                }
            }

            // 2.6) Mensajes de reclamaciones

            // Rechazo por póliza no válida
            String urlRechazoPolizaInv = ENGINE_URL
                    + "/execution?processVariables=emailCliente_eq_" + email
                    + "&messageEventSubscriptionName=Message_RechazoPolizaInvalida";

            List<Map<String, Object>> ejecRechazoPolizaInv =
                    restTemplate.getForObject(urlRechazoPolizaInv, List.class);

            if (ejecRechazoPolizaInv != null) {
                for (Map<String, Object> exe : ejecRechazoPolizaInv) {
                    Map<String, Object> tareaFalsa = new HashMap<>();
                    tareaFalsa.put("id", exe.get("id"));
                    tareaFalsa.put("name", "Reclamación rechazada por póliza no válida");
                    tareaFalsa.put("created", "Tu reclamación fue rechazada por póliza no válida");
                    tareaFalsa.put("esMensaje", true);
                    tareaFalsa.put("tipoMensaje", "RECHAZO_POLIZA_INVALIDA");
                    listaFinal.add(tareaFalsa);
                }
            }

            // Rechazo de reclamación
            String urlRechazoReclam = ENGINE_URL
                    + "/execution?processVariables=emailCliente_eq_" + email
                    + "&messageEventSubscriptionName=Message_RechazoReclamacion";

            List<Map<String, Object>> ejecRechazoReclam =
                    restTemplate.getForObject(urlRechazoReclam, List.class);

            if (ejecRechazoReclam != null) {
                for (Map<String, Object> exe : ejecRechazoReclam) {
                    Map<String, Object> tareaFalsa = new HashMap<>();
                    tareaFalsa.put("id", exe.get("id"));
                    tareaFalsa.put("name", "Reclamación rechazada");
                    tareaFalsa.put("created", "Tu reclamación fue rechazada");
                    tareaFalsa.put("esMensaje", true);
                    tareaFalsa.put("tipoMensaje", "RECHAZO_RECLAMACION");
                    listaFinal.add(tareaFalsa);
                }
            }

            // Rechazo por vencimiento
            String urlRechazoVenc = ENGINE_URL
                    + "/execution?processVariables=emailCliente_eq_" + email
                    + "&messageEventSubscriptionName=Message_RechazoPorVencimiento";

            List<Map<String, Object>> ejecRechazoVenc =
                    restTemplate.getForObject(urlRechazoVenc, List.class);

            if (ejecRechazoVenc != null) {
                for (Map<String, Object> exe : ejecRechazoVenc) {
                    Map<String, Object> tareaFalsa = new HashMap<>();
                    tareaFalsa.put("id", exe.get("id"));
                    tareaFalsa.put("name", "Reclamación rechazada por vencimiento");
                    tareaFalsa.put("created", "Tu reclamación venció por no pago a tiempo");
                    tareaFalsa.put("esMensaje", true);
                    tareaFalsa.put("tipoMensaje", "RECHAZO_VENCIMIENTO");
                    listaFinal.add(tareaFalsa);
                }
            }

            // Resolución de reclamación aprobada
            String urlResolucion = ENGINE_URL
                    + "/execution?processVariables=emailCliente_eq_" + email
                    + "&messageEventSubscriptionName=Message_ResolucionReclamacion";

            List<Map<String, Object>> ejecResolucion =
                    restTemplate.getForObject(urlResolucion, List.class);

            if (ejecResolucion != null) {
                for (Map<String, Object> exe : ejecResolucion) {
                    Map<String, Object> tareaFalsa = new HashMap<>();
                    tareaFalsa.put("id", exe.get("id"));
                    tareaFalsa.put("name", "Resolución de tu reclamación");
                    tareaFalsa.put("created", "Tu reclamación fue resuelta");
                    tareaFalsa.put("esMensaje", true);
                    tareaFalsa.put("tipoMensaje", "RESOLUCION_RECLAMACION");
                    listaFinal.add(tareaFalsa);
                }
            }

            // Renovación: respuesta del cliente a la oferta
            String urlRespRenov = ENGINE_URL
                    + "/execution?processVariables=emailCliente_eq_" + email
                    + "&messageEventSubscriptionName=Message_RespuestaClienteRecibida";

            List<Map<String, Object>> ejecRespRenov =
                    restTemplate.getForObject(urlRespRenov, List.class);

            if (ejecRespRenov != null) {
                for (Map<String, Object> exe : ejecRespRenov) {
                    Map<String, Object> tareaFalsa = new HashMap<>();
                    tareaFalsa.put("id", exe.get("id"));                // executionId
                    tareaFalsa.put("name", "Responder oferta de renovación");
                    tareaFalsa.put("created", "Tienes una oferta de renovación pendiente");
                    tareaFalsa.put("esMensaje", true);
                    tareaFalsa.put("tipoMensaje", "RESPUESTA_RENOVACION");
                    listaFinal.add(tareaFalsa);
                }
            }

            // Renovación: confirmación de pago
            String urlPagoRenov = ENGINE_URL
                    + "/execution?processVariables=emailCliente_eq_" + email
                    + "&messageEventSubscriptionName=Message_PagoClienteRenovacion";

            List<Map<String, Object>> ejecPagoRenov =
                    restTemplate.getForObject(urlPagoRenov, List.class);

            if (ejecPagoRenov != null) {
                for (Map<String, Object> exe : ejecPagoRenov) {
                    Map<String, Object> tareaFalsa = new HashMap<>();
                    tareaFalsa.put("id", exe.get("id")); // lo puedes dejar, pero ya no lo usaremos
                    tareaFalsa.put("processInstanceId", exe.get("processInstanceId"));
                    tareaFalsa.put("name", "Confirmar pago de renovación");
                    tareaFalsa.put("created", "Pago de renovación pendiente de confirmación");
                    tareaFalsa.put("esMensaje", true);
                    tareaFalsa.put("tipoMensaje", "PAGO_RENOVACION");
                    listaFinal.add(tareaFalsa);
                }
            }



            // 2.7) Si ya no hay trámites pendientes, mostrar el resultado del último proceso
            try {
                if (listaFinal.isEmpty()) {
                    String urlUltimaInstancia = ENGINE_URL
                            + "/history/process-instance"
                            + "?variables=emailCliente_eq_" + email
                            + "&finished=true"
                            + "&sortBy=startTime&sortOrder=desc"
                            + "&maxResults=1";

                    List<Map<String, Object>> instancias =
                            restTemplate.getForObject(urlUltimaInstancia, List.class);

                    if (instancias != null && !instancias.isEmpty()) {
                        String processInstanceId = (String) instancias.get(0).get("id");

                        String urlVars = ENGINE_URL
                                + "/history/variable-instance"
                                + "?processInstanceId=" + processInstanceId
                                + "&variableNameIn=estadoFinal,mensajePortalFinal";

                        List<Map<String, Object>> vars =
                                restTemplate.getForObject(urlVars, List.class);

                        if (vars != null && !vars.isEmpty()) {
                            ultimaNotificacion = new HashMap<>();
                            for (Map<String, Object> var : vars) {
                                String name = (String) var.get("name");
                                Object value = var.get("value");
                                ultimaNotificacion.put(name, value);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            model.addAttribute("tareas", listaFinal);
            model.addAttribute("email", email);
            model.addAttribute("ultimaNotificacion", ultimaNotificacion);
            return "portal/bandeja";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar tus trámites: " + e.getMessage());
            return "error";
        }
    }

    // 3) Formulario para responder una cotización (Aceptar / Rechazar)
    @GetMapping("/portal/responder-cotizacion/{executionId}")
    public String formRespuesta(@PathVariable String executionId, Model model) {
        model.addAttribute("executionId", executionId);
        return "portal/responder-cotizacion";
    }

    // 4) Enviar la respuesta del cliente al motor (mensaje de cotización)
    @PostMapping("/portal/enviar-respuesta")
    public String enviarRespuesta(@RequestParam String executionId,
                                  @RequestParam Boolean decision) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            Map<String, Object> vars = new HashMap<>();
            Map<String, Object> valor = new HashMap<>();
            valor.put("value", decision);
            valor.put("type", "Boolean");
            vars.put("clienteAcepta", valor);  // nombre usado en el gateway del BPMN

            Map<String, Object> body = new HashMap<>();
            body.put("variables", vars);

            String url = ENGINE_URL
                    + "/execution/" + executionId
                    + "/messageSubscriptions/Message_RespuestaCliente/trigger";

            restTemplate.postForEntity(url, body, Void.class);
            return "redirect:/portal/acceso";

        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    // 5) Formulario para confirmar la firma del contrato
    @GetMapping("/portal/confirmar-contrato/{executionId}")
    public String formConfirmarContrato(@PathVariable String executionId, Model model) {
        model.addAttribute("executionId", executionId);
        return "portal/confirmar-contrato";
    }

    // 6) Enviar confirmación de contrato firmado al motor
    @PostMapping("/portal/enviar-confirmacion-contrato")
    public String enviarConfirmacionContrato(@RequestParam String executionId) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            Map<String, Object> vars = new HashMap<>();
            Map<String, Object> valor = new HashMap<>();
            valor.put("value", true);
            valor.put("type", "Boolean");
            vars.put("contratoFirmado", valor);

            Map<String, Object> body = new HashMap<>();
            body.put("variables", vars);

            String url = ENGINE_URL
                    + "/execution/" + executionId
                    + "/messageSubscriptions/Message_ContratoFirmado/trigger";

            restTemplate.postForEntity(url, body, Void.class);
            return "redirect:/portal/acceso";

        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    // 7) Formulario para confirmar pago (emisión)
    @GetMapping("/portal/confirmar-pago/{executionId}")
    public String formConfirmarPago(@PathVariable String executionId, Model model) {
        model.addAttribute("executionId", executionId);
        return "portal/confirmar-pago";
    }

    // 8) Enviar confirmación de pago al motor (emisión)
    @PostMapping("/portal/enviar-confirmacion-pago")
    public String enviarConfirmacionPago(@RequestParam String executionId) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            Map<String, Object> vars = new HashMap<>();
            Map<String, Object> valor = new HashMap<>();
            valor.put("value", true);
            valor.put("type", "Boolean");
            vars.put("pagoConfirmado", valor);

            Map<String, Object> body = new HashMap<>();
            body.put("variables", vars);

            String url = ENGINE_URL
                    + "/execution/" + executionId
                    + "/messageSubscriptions/Message_ConfirmacionPago/trigger";

            restTemplate.postForEntity(url, body, Void.class);
            return "redirect:/portal/acceso";

        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    // 9) Formulario para responder oferta de renovación
    @GetMapping("/portal/renovaciones/responder-oferta/{executionId}")
    public String formResponderOfertaRenovacion(@PathVariable String executionId, Model model) {
        model.addAttribute("executionId", executionId);
        return "portal/responder-oferta-renovacion";
    }

    // 10) Enviar respuesta del cliente a la oferta de renovación
    @PostMapping("/portal/renovaciones/enviar-respuesta-oferta")
    public String enviarRespuestaOfertaRenovacion(@RequestParam String executionId,
                                                  @RequestParam Boolean decision) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            Map<String, Object> vars = new HashMap<>();
            Map<String, Object> valor = new HashMap<>();
            valor.put("value", decision);
            valor.put("type", "Boolean");
            // variable usada en el gateway ¿Cliente Acepta Renovar?
            vars.put("clienteAceptaRenovar", valor);

            Map<String, Object> body = new HashMap<>();
            body.put("variables", vars);

            String url = ENGINE_URL
                    + "/execution/" + executionId
                    + "/messageSubscriptions/Message_RespuestaClienteRecibida/trigger";

            restTemplate.postForEntity(url, body, Void.class);
            return "redirect:/portal/acceso";

        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    // 11) Formulario para confirmar pago de renovación
    @GetMapping("/portal/renovaciones/confirmar-pago")
    public String formConfirmarPagoRenovacion(@RequestParam String processInstanceId, Model model) {
        model.addAttribute("processInstanceId", processInstanceId);
        return "portal/confirmar-pago-renovacion";
    }

    // 12) Enviar confirmación de pago de renovación al motor
    @PostMapping("/portal/renovaciones/enviar-confirmacion-pago")
    public String enviarConfirmacionPagoRenovacion(@RequestParam String processInstanceId) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            Map<String, Object> vars = new HashMap<>();
            Map<String, Object> valor = new HashMap<>();
            valor.put("value", true);
            valor.put("type", "Boolean");
            vars.put("pagoRenovacionConfirmado", valor);

            Map<String, Object> body = new HashMap<>();
            body.put("messageName", "Message_PagoClienteRenovacion");
            body.put("processInstanceId", processInstanceId);
            body.put("processVariables", vars);

            restTemplate.postForEntity(ENGINE_URL + "/message", body, Void.class);
            return "redirect:/portal/acceso";

        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }


}
