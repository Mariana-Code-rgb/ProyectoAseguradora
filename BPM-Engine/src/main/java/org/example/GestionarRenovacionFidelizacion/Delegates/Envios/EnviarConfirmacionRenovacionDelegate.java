package org.example.GestionarRenovacionFidelizacion.Delegates.Envios;

import org.example.historial.HistRenovacion;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.historial.HistRenovacionRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.logging.Logger;

@Component("enviarConfirmacionRenovacionDelegate")
public class EnviarConfirmacionRenovacionDelegate implements JavaDelegate {

    private static final Logger LOGGER =
            Logger.getLogger(EnviarConfirmacionRenovacionDelegate.class.getName());

    private final HistRenovacionRepository histRepo;

    // Spring inyecta el repositorio
    public EnviarConfirmacionRenovacionDelegate(HistRenovacionRepository histRepo) {
        this.histRepo = histRepo;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("=== Enviando Confirmación de Renovación ===");

        String numeroPoliza = (String) execution.getVariable("numeroPoliza");
        String nombreCliente = (String) execution.getVariable("nombreCliente");
        String emailCliente  = (String) execution.getVariable("emailCliente");
        String numeroTransaccion = (String) execution.getVariable("numeroTransaccion");
        String fechaVigenciaDesde = (String) execution.getVariable("fechaVigenciaDesde");
        String fechaVigenciaHasta = (String) execution.getVariable("fechaVigenciaHasta");
        Double primaRenovada = (Double) execution.getVariable("primaRenovada");

        String mensajeCorreo = String.format(
                "Estimado/a %s,\n\n" +
                        "¡Su póliza %s ha sido renovada exitosamente!\n\n" +
                        "Detalles de la renovación:\n" +
                        "- Prima pagada: $%.2f\n" +
                        "- Transacción: %s\n" +
                        "- Vigencia: %s al %s\n\n" +
                        "Puede descargar su certificado de póliza renovada desde nuestro portal.\n\n" +
                        "Gracias por confiar en nosotros.\n\n" +
                        "Saludos,\nEquipo de Atención al Cliente",
                nombreCliente, numeroPoliza, primaRenovada, numeroTransaccion,
                fechaVigenciaDesde, fechaVigenciaHasta
        );

        LOGGER.info("Enviando confirmación a: " + emailCliente);
        LOGGER.info("Contenido:\n" + mensajeCorreo);

        // Marcadores de proceso
        execution.setVariable("confirmacionEnviada", true);
        execution.setVariable("fechaConfirmacion", LocalDateTime.now().toString());

        // Variables para el portal del cliente
        execution.setVariable("estadoFinal", "RENOVACION_RENOVADA");
        String mensajePortal = String.format(
                "Tu póliza %s fue renovada exitosamente. La nueva vigencia es del %s al %s y la prima pagada fue de %.2f.",
                numeroPoliza, fechaVigenciaDesde, fechaVigenciaHasta, primaRenovada
        );
        execution.setVariable("mensajePortalFinal", mensajePortal);

        // Registro histórico en H2
        HistRenovacion hist = new HistRenovacion();
        hist.setProcessInstanceId(execution.getProcessInstanceId());
        hist.setNumeroPoliza(numeroPoliza);
        hist.setEmailCliente(emailCliente);
        hist.setEstado("RENOVACION_RENOVADA");
        hist.setMotivo("Renovación exitosa");
        hist.setDetalle(mensajePortal);
        hist.setFechaCreacion(LocalDateTime.now());
        histRepo.save(hist);

        LOGGER.info("Confirmación de renovación enviada y registrada en histórico");
    }
}
