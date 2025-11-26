package org.example.historial;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class HistRenovacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String processInstanceId;
    private String numeroPoliza;
    private String emailCliente;

    private String estado;
    private String motivo;

    @Column(length = 1000)
    private String detalle;

    private LocalDateTime fechaCreacion;
}
