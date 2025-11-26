package org.example.GestionarReclamaciones.Delegates.Envios;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component("enviarConfirmacionPagoTallerDelegate")
public class EnviarConfirmacionPagoTallerDelegate implements JavaDelegate {

    private static final Logger LOGGER =
            Logger.getLogger(EnviarConfirmacionPagoTallerDelegate.class.getName());

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String nombreTaller        = (String) execution.getVariable("nombreTaller");
        String numeroPoliza        = (String) execution.getVariable("numeroPoliza");
        Number montoNumber          = (Number) execution.getVariable("montoPagoTaller");
        Double montoPagoTaller      = montoNumber != null ? montoNumber.doubleValue() : 0.0;
        String referenciaPagoTaller = (String) execution.getVariable("referenciaPagoTaller");

        if (montoPagoTaller == null) {
            montoPagoTaller = 0.0;
        }

        LOGGER.info("=== Enviar Confirmación de Pago al Taller ===");
        LOGGER.info("Taller: " + nombreTaller);
        LOGGER.info("Póliza: " + numeroPoliza);
        LOGGER.info("Monto pagado: $" + montoPagoTaller);
        LOGGER.info("Referencia: " + referenciaPagoTaller);

        String mensaje = String.format(
                "Estimado taller %s,%n%n" +
                        "Se confirma el pago de la reparación asociada a la póliza %s.%n%n" +
                        "- Monto pagado: $%.2f%n" +
                        "- Referencia de pago: %s%n%n" +
                        "Saludos,%nDepartamento de Finanzas",
                nombreTaller, numeroPoliza, montoPagoTaller, referenciaPagoTaller
        );

        LOGGER.info("Contenido enviado al taller:\n" + mensaje);
    }
}
