package org.example.GestionarReclamaciones.Delegates.Envios;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component("notificarResolucionReclamacionDelegate")
public class NotificarResolucionReclamacionDelegate implements JavaDelegate {

    private static final Logger LOGGER =
            Logger.getLogger(NotificarResolucionReclamacionDelegate.class.getName());

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        String numeroPoliza    = (String) execution.getVariable("numeroPoliza");
        String nombreAsegurado = (String) execution.getVariable("nombreAsegurado");
        String emailCliente    = (String) execution.getVariable("emailCliente");
        Boolean aprobada       = (Boolean) execution.getVariable("reclamacionAprobada");

        // Leer monto como Number para evitar ClassCastException
        Number montoNumber     = (Number) execution.getVariable("montoIndemnizacion");
        Double montoIndemnizacion = montoNumber != null ? montoNumber.doubleValue() : 0.0;

        String estado = Boolean.TRUE.equals(aprobada) ? "APROBADA" : "RECHAZADA";

        String detalle;
        if (Boolean.TRUE.equals(aprobada)) {
            // Usar montoIndemnizacion, no montoAprobado
            detalle = String.format("Monto aprobado para indemnización: $%.2f.", montoIndemnizacion);
        } else {
            detalle = "Para más información sobre el motivo del rechazo, por favor contacte a nuestro centro de atención.";
        }

        String mensaje = String.format(
                "Estimado/a %s,%n%n" +
                        "La reclamación asociada a su póliza %s ha sido %s.%n%n" +
                        "%s%n%n" +
                        "Saludos,%nServicio al Cliente",
                nombreAsegurado, numeroPoliza, estado, detalle
        );

        LOGGER.info("=== Notificar Resolución al Cliente ===");
        LOGGER.info("Enviando resolución a: " + emailCliente);
        LOGGER.info("Contenido:\n" + mensaje);

        execution.setVariable("estadoFinal", "APROBADA_PAGADA");
        execution.setVariable("mensajePortalFinal",
                "Tu reclamación fue aprobada y el pago al taller se ha realizado.");

    }
}
