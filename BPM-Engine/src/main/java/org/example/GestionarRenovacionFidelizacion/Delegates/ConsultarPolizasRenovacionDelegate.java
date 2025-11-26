package org.example.GestionarRenovacionFidelizacion.Delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Component("consultarPolizasRenovacionDelegate")
public class ConsultarPolizasRenovacionDelegate implements JavaDelegate {

    private static final Logger LOGGER = Logger.getLogger(ConsultarPolizasRenovacionDelegate.class.getName());

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("=== Consultando Pólizas que Vencen en 60 Días ===");

        // Simular consulta a base de datos
        List<Map<String, Object>> polizasARenovar = new ArrayList<>();

        // Póliza 1
        Map<String, Object> poliza1 = new HashMap<>();
        poliza1.put("numeroPoliza", "POL-2024-001");
        poliza1.put("nombreCliente", "Juan Pérez");
        poliza1.put("emailCliente", "juan.perez@email.com");
        poliza1.put("primaActual", 1200.0);
        poliza1.put("antiguedadCliente", 5);
        poliza1.put("historialSiniestros", "BAJO");
        poliza1.put("fechaVencimiento", LocalDate.now().plusDays(60).toString());
        polizasARenovar.add(poliza1);

        // Póliza 2
        Map<String, Object> poliza2 = new HashMap<>();
        poliza2.put("numeroPoliza", "POL-2024-002");
        poliza2.put("nombreCliente", "María García");
        poliza2.put("emailCliente", "maria.garcia@email.com");
        poliza2.put("primaActual", 1500.0);
        poliza2.put("antiguedadCliente", 3);
        poliza2.put("historialSiniestros", "MEDIO");
        poliza2.put("fechaVencimiento", LocalDate.now().plusDays(62).toString());
        polizasARenovar.add(poliza2);

        // Póliza 3
        Map<String, Object> poliza3 = new HashMap<>();
        poliza3.put("numeroPoliza", "POL-2024-003");
        poliza3.put("nombreCliente", "Carlos López");
        poliza3.put("emailCliente", "carlos.lopez@email.com");
        poliza3.put("primaActual", 980.0);
        poliza3.put("antiguedadCliente", 1);
        poliza3.put("historialSiniestros", "BAJO");
        poliza3.put("fechaVencimiento", LocalDate.now().plusDays(58).toString());
        polizasARenovar.add(poliza3);

        // Póliza 4
        Map<String, Object> poliza4 = new HashMap<>();
        poliza4.put("numeroPoliza", "POL-2024-004");
        poliza4.put("nombreCliente", "Luisa Martínez");
        poliza4.put("emailCliente", "luisa.martinez@email.com");
        poliza4.put("primaActual", 1800.0);
        poliza4.put("antiguedadCliente", 4);
        poliza4.put("historialSiniestros", "ALTO");
        poliza4.put("fechaVencimiento", LocalDate.now().plusDays(59).toString());
        polizasARenovar.add(poliza4);

        // Póliza 5
        Map<String, Object> poliza5 = new HashMap<>();
        poliza5.put("numeroPoliza", "POL-2024-005");
        poliza5.put("nombreCliente", "Ana Torres");
        poliza5.put("emailCliente", "ana.torres@email.com");
        poliza5.put("primaActual", 1100.0);
        poliza5.put("antiguedadCliente", 2);
        poliza5.put("historialSiniestros", "MEDIO");
        poliza5.put("fechaVencimiento", LocalDate.now().plusDays(61).toString());
        polizasARenovar.add(poliza5);

        // Póliza 6
        Map<String, Object> poliza6 = new HashMap<>();
        poliza6.put("numeroPoliza", "POL-2024-006");
        poliza6.put("nombreCliente", "Pedro Ramírez");
        poliza6.put("emailCliente", "pedro.ramirez@email.com");
        poliza6.put("primaActual", 1350.0);
        poliza6.put("antiguedadCliente", 6);
        poliza6.put("historialSiniestros", "BAJO");
        poliza6.put("fechaVencimiento", LocalDate.now().plusDays(57).toString());
        polizasARenovar.add(poliza6);


        LOGGER.info("Se encontraron " + polizasARenovar.size() + " pólizas por vencer");

        for (Map<String, Object> poliza : polizasARenovar) {
            LOGGER.info("- " + poliza.get("numeroPoliza") + ": " +
                    poliza.get("nombreCliente") + " (vence: " +
                    poliza.get("fechaVencimiento") + ")");
        }

        // Esta variable es la que usa el Call Activity multi-instancia
        execution.setVariable("polizasARenovar", polizasARenovar);
        execution.setVariable("totalPolizasARenovar", polizasARenovar.size());

        LOGGER.info("DEBUG polizasARenovar = " + polizasARenovar);
        execution.setVariable("polizasARenovar", polizasARenovar);

        LOGGER.info("Lista de pólizas guardada en variable 'polizasARenovar'");
    }
}
