package org.example.GestionarReclamaciones.Delegates.Envios;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component("notificarRechazoPolizaInvalidaDelegate")
public class NotificarRechazoPolizaInvalidaDelegate implements JavaDelegate {

    private static final Logger LOGGER =
            Logger.getLogger(NotificarRechazoPolizaInvalidaDelegate.class.getName());

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String numeroPoliza   = (String) execution.getVariable("numeroPoliza");
        String nombreAsegurado = (String) execution.getVariable("nombreAsegurado");
        String emailCliente   = (String) execution.getVariable("emailCliente");

        String mensaje = String.format(
                "Estimado/a %s,%n%n" +
                        "Hemos recibido su notificación de siniestro para la póliza %s.%n" +
                        "Sin embargo, la póliza no se encuentra vigente o presenta inconsistencias,%n" +
                        "por lo que la reclamación ha sido rechazada.%n%n" +
                        "Saludos,%nDepartamento de Reclamaciones",
                nombreAsegurado, numeroPoliza
        );

        LOGGER.info("=== Notificar Rechazo por Póliza no Válida ===");
        LOGGER.info("Enviando notificación a: " + emailCliente);
        LOGGER.info("Contenido:\n" + mensaje);

        execution.setVariable("estadoFinal", "RECHAZADA_POLIZA_INVALIDA");
        execution.setVariable("mensajePortalFinal",
                "Tu reclamación fue rechazada porque la póliza no es válida o no está vigente.");

    }


}
