package org.example.GestionarReclamaciones;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerificarResultadoReclamacionDelegate implements JavaDelegate {

    private static final Logger LOG =
            LoggerFactory.getLogger(VerificarResultadoReclamacionDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        // Valor de la variable booleana que usas en el gateway
        Object valor = execution.getVariable("reclamacionAprobada");
        LOG.info("=== VerificarResultadoReclamacionDelegate ===");
        LOG.info("Variable 'reclamacionAprobada' = {}", valor);

        Boolean aprobada = (valor instanceof Boolean) ? (Boolean) valor : null;

        if (aprobada == null) {
            LOG.warn("La variable 'reclamacionAprobada' es null o no es Boolean. "
                    + "Tipo real = {}", (valor != null ? valor.getClass().getName() : "null"));
        } else if (aprobada) {
            LOG.info("La reclamación fue APROBADA por la DMN.");
        } else {
            LOG.info("La reclamación fue RECHAZADA por la DMN.");
        }

        // Opcional: ver todo el mapa que devolvió la DMN
        Object mapa = execution.getVariable("evaluacionReclamacion");
        LOG.info("Contenido completo de 'evaluacionReclamacion' = {}", mapa);
    }
}