package org.example.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudCotizacionDTO implements Serializable {

    // Estos nombres deben coincidir con las variables de tu proceso Camunda
    private String nombreCliente;
    private String emailCliente;
    private String tipoProducto; // "vida", "salud", "vehiculo"
    private Double primaAnual;
    private Double montoAsegurado;

    // Nota: Aunque el formulario dice "primaAnual",
    // usualmente el usuario pide cotización y el sistema calcula la prima.
    // Pero si el formulario de entrada lo pide, aquí está.
}
