package org.example.GestionarRenovacionFidelizacion.Delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.logging.Logger;

@Component("generarOfertaRenovacionDelegate")
public class GenerarOfertaRenovacionDelegate implements JavaDelegate {

    private static final Logger LOGGER = Logger.getLogger(GenerarOfertaRenovacionDelegate.class.getName());

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("=== Generando Oferta de Renovación ===");

        // Obtener el mapa completo de la póliza
        @SuppressWarnings("unchecked")
        Map<String, Object> poliza = (Map<String, Object>) execution.getVariable("poliza");

        LOGGER.info("DEBUG poliza = " + poliza);

        String numeroPoliza   = poliza != null ? (String) poliza.get("numeroPoliza")   : null;
        String nombreCliente  = poliza != null ? (String) poliza.get("nombreCliente")  : null;
        Double primaActual    = poliza != null ? (Double) poliza.get("primaActual")    : null;
        Integer antiguedad    = poliza != null ? (Integer) poliza.get("antiguedadCliente") : null;
        String historial      = poliza != null ? (String) poliza.get("historialSiniestros") : null;

        LOGGER.info("Datos póliza en GeReIn -> numeroPoliza=" + numeroPoliza +
                ", nombreCliente=" + nombreCliente +
                ", primaActual=" + primaActual +
                ", antiguedad=" + antiguedad +
                ", historial=" + historial);

        // Resultado del DMN
        Integer porcentajeDescuento = (Integer) execution.getVariable("porcentajeDescuento");
        String estrategiaRetencion  = (String) execution.getVariable("estrategiaRetencion");

        if (porcentajeDescuento == null) {
            LOGGER.warning("⚠️ porcentajeDescuento es NULL. DMN no ejecutado correctamente.");
            porcentajeDescuento = 10;
        }
        if (estrategiaRetencion == null) {
            LOGGER.warning("⚠️ estrategiaRetencion es NULL. DMN no ejecutado correctamente.");
            estrategiaRetencion = "STANDARD";
        }

        if (primaActual == null) {
            primaActual = 1200.0;
        }

        LOGGER.info("Póliza: " + numeroPoliza + " - Cliente: " + nombreCliente);
        LOGGER.info("Descuento aplicado (DMN): " + porcentajeDescuento + "%");
        LOGGER.info("Estrategia de retención (DMN): " + estrategiaRetencion);

        double descuento   = primaActual * (porcentajeDescuento / 100.0);
        double primaRenovada = primaActual - descuento;

        LocalDate fechaVigenciaDesde = LocalDate.now().plusDays(30);
        LocalDate fechaVigenciaHasta = fechaVigenciaDesde.plusYears(1);

        execution.setVariable("primaRenovada", primaRenovada);
        execution.setVariable("descuentoAplicado", descuento);
        execution.setVariable("fechaVigenciaDesde", fechaVigenciaDesde.toString());
        execution.setVariable("fechaVigenciaHasta", fechaVigenciaHasta.toString());
        execution.setVariable("ofertaGenerada", true);
        execution.setVariable("numeroPoliza", numeroPoliza);
        execution.setVariable("nombreCliente", nombreCliente);
        execution.setVariable("emailCliente", poliza != null ? (String) poliza.get("emailCliente") : null);
        execution.setVariable("primaActual", primaActual);
        execution.setVariable("antiguedadCliente", antiguedad);
        execution.setVariable("historialSiniestros", historial);

        LOGGER.info("Oferta generada - Prima renovada: $" + primaRenovada +
                " (descuento: $" + descuento + ")");
        LOGGER.info("Vigencia: " + fechaVigenciaDesde + " a " + fechaVigenciaHasta);
    }


}
