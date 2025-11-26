package org.example.GestionarReclamaciones.Delegates.Envios;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component("notificarRechazoReclamacionDelegate")
public class NotificarRechazoDelegate implements JavaDelegate {

    private static final Logger LOGGER =
            Logger.getLogger(NotificarRechazoDelegate.class.getName());

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String numeroPoliza   = (String) execution.getVariable("numeroPoliza");
        String nombreAsegurado = (String) execution.getVariable("nombreAsegurado");
        String emailCliente   = (String) execution.getVariable("emailCliente");
        String motivo         = (String) execution.getVariable("motivoRechazo");

        if (motivo == null || motivo.isBlank()) {
            motivo = "El resultado de la evaluación de la reclamación no cumple con los criterios de aprobación.";
        }

        String mensaje = String.format(
                "Estimado/a %s,%n%n" +
                        "Luego de analizar su reclamación asociada a la póliza %s,%n" +
                        "lamentamos informarle que la misma ha sido rechazada.%n%n" +
                        "Motivo:%n%s%n%n" +
                        "Si requiere más información, por favor contacte a nuestro centro de atención.%n%n" +
                        "Saludos,%nGerencia de Reclamaciones",
                nombreAsegurado, numeroPoliza, motivo
        );

        LOGGER.info("=== Notificar Rechazo de Reclamación ===");
        LOGGER.info("Enviando notificación a: " + emailCliente);
        LOGGER.info("Contenido:\n" + mensaje);

        execution.setVariable("estadoFinal", "RECHAZADA");
        execution.setVariable("mensajePortalFinal",
                "Tu reclamación fue rechazada según la evaluación de la aseguradora.");

    }
}
