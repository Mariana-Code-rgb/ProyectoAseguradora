package org.example.GestionarRenovacionFidelizacion.Delegates.Envios;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.logging.Logger;

@Component("enviarInstruccionesPagoDelegate")
public class EnviarInstruccionesPagoDelegate implements JavaDelegate {

    private static final Logger LOGGER = Logger.getLogger(EnviarInstruccionesPagoDelegate.class.getName());

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("=== Enviando Instrucciones de Pago de Renovación ===");

        String numeroPoliza  = (String) execution.getVariable("numeroPoliza");
        String nombreCliente = (String) execution.getVariable("nombreCliente");
        String emailCliente  = (String) execution.getVariable("emailCliente");
        Double primaRenovada = (Double) execution.getVariable("primaRenovada");

        // ID de instancia de proceso para correlacionar el pago
        String processInstanceId = execution.getProcessInstanceId();  // ✅

        LocalDate fechaLimitePago = LocalDate.now().plusDays(10);

        String mensaje = String.format(
                "Estimado/a %s,\n\n" +
                        "Para completar la renovación de su póliza %s, por favor realice el pago de $%.2f\n" +
                        "antes del %s.\n\n",
                nombreCliente, numeroPoliza, primaRenovada, fechaLimitePago
        );

        String linkPago = "http://localhost:8080/api/renovacion/formulario-pago"
                + "?poliza=" + numeroPoliza
                + "&monto=" + primaRenovada
                + "&processInstanceId=" + processInstanceId;

        mensaje += "Para pagar ahora (demo), haga clic aquí:\n" + linkPago;

        LOGGER.info("Link de pago (demo): " + linkPago);
        LOGGER.info("Enviando instrucciones de pago a: " + emailCliente);
        LOGGER.info("Referencia de pago: REF-REN-" + numeroPoliza + "-" + System.currentTimeMillis());
        LOGGER.info("Fecha límite: " + fechaLimitePago);

        execution.setVariable("fechaLimitePago", fechaLimitePago.toString());

        LOGGER.info("Instrucciones de pago enviadas exitosamente");
    }
}
