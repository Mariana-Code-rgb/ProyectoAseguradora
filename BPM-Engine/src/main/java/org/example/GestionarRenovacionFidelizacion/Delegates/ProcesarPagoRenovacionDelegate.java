package org.example.GestionarRenovacionFidelizacion.Delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.logging.Logger;

@Component("procesarPagoRenovacionDelegate")
public class ProcesarPagoRenovacionDelegate implements JavaDelegate {

    private static final Logger LOGGER = Logger.getLogger(ProcesarPagoRenovacionDelegate.class.getName());

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("=== Procesando Pago y Actualizando Póliza ===");

        String numeroPoliza = (String) execution.getVariable("numeroPoliza");
        String referenciaPago = (String) execution.getVariable("referenciaPago");
        Double primaRenovada = (Double) execution.getVariable("primaRenovada");
        String fechaVigenciaDesde = (String) execution.getVariable("fechaVigenciaDesde");
        String fechaVigenciaHasta = (String) execution.getVariable("fechaVigenciaHasta");

        // Simular procesamiento de pago
        String numeroTransaccion = "TRX-" + System.currentTimeMillis();
        LocalDateTime fechaPago = LocalDateTime.now();

        LOGGER.info("Procesando pago para póliza: " + numeroPoliza);
        LOGGER.info("Referencia: " + referenciaPago);
        LOGGER.info("Monto: $" + primaRenovada);
        LOGGER.info("Número de transacción: " + numeroTransaccion);

        // Simular actualización en base de datos
        // polizaRepository.actualizarVigencia(numeroPoliza, fechaVigenciaDesde, fechaVigenciaHasta);
        // pagoRepository.registrarPago(numeroTransaccion, primaRenovada, fechaPago);

        execution.setVariable("numeroTransaccion", numeroTransaccion);
        execution.setVariable("fechaPago", fechaPago.toString());
        execution.setVariable("estadoPoliza", "RENOVADA");
        execution.setVariable("pagoConfirmado", true);

        LOGGER.info("Pago procesado exitosamente");
        LOGGER.info("Póliza renovada desde " + fechaVigenciaDesde + " hasta " + fechaVigenciaHasta);
    }
}
