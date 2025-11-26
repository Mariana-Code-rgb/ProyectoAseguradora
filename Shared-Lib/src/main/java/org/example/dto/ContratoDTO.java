package org.example.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ContratoDTO implements Serializable {
    private String numeroContrato;
    private String terminosCondiciones;
    // + los campos heredados de cliente/producto si los necesitas
}
