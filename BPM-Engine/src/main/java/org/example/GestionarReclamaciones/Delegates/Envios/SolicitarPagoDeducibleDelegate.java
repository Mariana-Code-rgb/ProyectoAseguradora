package org.example.GestionarReclamaciones.Delegates.Envios;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.logging.Logger;

@Component("solicitarPagoDeducibleDelegate")
public class SolicitarPagoDeducibleDelegate implements JavaDelegate {

    private static final Logger LOGGER =
            Logger.getLogger(SolicitarPagoDeducibleDelegate.class.getName());

    @Override
    public void execute(DelegateExecution execution) {
        String numeroPoliza    = (String) execution.getVariable("numeroPoliza");
        String nombreAsegurado = (String) execution.getVariable("nombreAsegurado");
        String emailCliente    = (String) execution.getVariable("emailCliente");
        Double montoDeducible  = (Double) execution.getVariable("montoDeducible");

        if (montoDeducible == null) {
            montoDeducible = 0.0;
        }

        String processInstanceId = execution.getProcessInstanceId();

        LocalDate fechaLimite = LocalDate.now().plusDays(5);
        execution.setVariable("fechaLimiteDeducible", fechaLimite.toString());

        String linkPago = "http://localhost:8080/api/reclamaciones/formulario-deducible"
                + "?poliza=" + numeroPoliza
                + "&monto=" + montoDeducible
                + "&processInstanceId=" + processInstanceId;

        String mensaje = String.format(
                "Estimado/a %s,%n%n" +
                        "Para continuar con la gestión de su reclamación de la póliza %s,%n" +
                        "por favor realice el pago del deducible por un monto de $%.2f%n" +
                        "antes del %s.%n%n" +
                        "Para pagar ahora (demo), haga clic en el siguiente enlace:%n%s%n%n" +
                        "Saludos,%nDepartamento de Finanzas",
                nombreAsegurado, numeroPoliza, montoDeducible, fechaLimite, linkPago
        );

        LOGGER.info("=== Solicitar Pago de Deducible al Cliente ===");
        LOGGER.info("Link de pago deducible: " + linkPago);
        LOGGER.info("Enviando solicitud de pago a: " + emailCliente);
        LOGGER.info("Contenido:\n" + mensaje);
    }
}

