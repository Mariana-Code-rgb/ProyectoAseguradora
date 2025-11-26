package org.example.GestionarReclamaciones.Delegates.Envios;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component("notificarRechazoPorVencimientoDelegate")
public class NotificarRechazoPorVencimientoDelegate implements JavaDelegate {

    private static final Logger LOGGER =
            Logger.getLogger(NotificarRechazoPorVencimientoDelegate.class.getName());

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String numeroPoliza    = (String) execution.getVariable("numeroPoliza");
        String nombreAsegurado = (String) execution.getVariable("nombreAsegurado");
        String emailCliente    = (String) execution.getVariable("emailCliente");
        String fechaLimite     = (String) execution.getVariable("fechaLimiteDeducible");

        String mensaje = String.format(
                "Estimado/a %s,%n%n" +
                        "Para continuar con la gestión de su reclamación de la póliza %s,%n" +
                        "era necesario realizar el pago del deducible antes del %s.%n" +
                        "Al no haberse registrado el pago en el plazo establecido,%n" +
                        "la reclamación ha sido rechazada por vencimiento.%n%n" +
                        "Saludos,%nDepartamento de Finanzas",
                nombreAsegurado, numeroPoliza, fechaLimite
        );

        LOGGER.info("=== Notificar Rechazo por Vencimiento ===");
        LOGGER.info("Enviando notificación a: " + emailCliente);
        LOGGER.info("Contenido:\n" + mensaje);

        execution.setVariable("estadoFinal", "RECHAZADA_VENCIMIENTO");
        execution.setVariable("mensajePortalFinal",
                "Tu reclamación venció porque no se pagó el deducible dentro del plazo.");

    }
}
