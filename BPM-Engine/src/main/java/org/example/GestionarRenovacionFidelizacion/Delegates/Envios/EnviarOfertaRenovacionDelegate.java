package org.example.GestionarRenovacionFidelizacion.Delegates.Envios;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.logging.Logger;

@Component("enviarOfertaRenovacionDelegate")
public class EnviarOfertaRenovacionDelegate implements JavaDelegate {

    private static final Logger LOGGER = Logger.getLogger(EnviarOfertaRenovacionDelegate.class.getName());

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("=== Enviando Oferta de Renovación al Cliente ===");

        String numeroPoliza       = (String) execution.getVariable("numeroPoliza");
        String nombreCliente      = (String) execution.getVariable("nombreCliente");
        String emailCliente       = (String) execution.getVariable("emailCliente");
        Double primaRenovada      = (Double) execution.getVariable("primaRenovada");
        String fechaVigenciaDesde = (String) execution.getVariable("fechaVigenciaDesde");
        String fechaVigenciaHasta = (String) execution.getVariable("fechaVigenciaHasta");

        // ID de la instancia de proceso para correlación posterior
        String processInstanceId = execution.getProcessInstanceId();

        String mensaje = String.format(
                "Estimado/a %s,\n\n" +
                        "Nos complace presentarle nuestra oferta de renovación para su póliza %s.\n\n" +
                        "Prima renovada: $%.2f\n" +
                        "Vigencia: %s al %s\n\n" +
                        "Por favor, confirme su aceptación dentro de los próximos 30 días.\n\n" +
                        "Saludos,\nEquipo de Renovaciones",
                nombreCliente, numeroPoliza, primaRenovada, fechaVigenciaDesde, fechaVigenciaHasta
        );

        // ✅ Incluir también el processInstanceId
        String linkRespuesta =
                "http://localhost:8080/api/renovacion/formulario-respuesta"
                        + "?poliza=" + numeroPoliza
                        + "&processInstanceId=" + processInstanceId;

        mensaje += "\n\nPara responder, haga clic aquí:\n" + linkRespuesta;

        LOGGER.info("Link de respuesta: " + linkRespuesta);
        LOGGER.info("Enviando oferta a: " + emailCliente);
        LOGGER.info("Contenido:\n" + mensaje);

        execution.setVariable("fechaEnvioOferta", LocalDateTime.now().toString());
        execution.setVariable("ofertaEnviada", true);

        LOGGER.info("Oferta de renovación enviada exitosamente");
    }
}
