package com.application.wa9ti.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AppointmentSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "store_id", nullable = false)
    @JsonBackReference
    private Store store;

    // 📌 ANNULATION
    public enum CancellationPolicy {
        FORBIDDEN,
        ALLOWED_WITH_NOTICE,
        FREE_CANCELLATION
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CancellationPolicy cancellationPolicy = CancellationPolicy.FORBIDDEN;

    private Integer cancellationDeadlineHours; // Délai en heures (nullable si policy = FORBIDDEN ou ALLOWED)

    // 📌 MODIFICATION
    public enum ModificationPolicy {
        FORBIDDEN,
        ALLOWED_WITH_NOTICE,
        FREE_MODIFICATION
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModificationPolicy modificationPolicy = ModificationPolicy.FORBIDDEN;

    private Integer modificationDeadlineHours; // Délai en heures (nullable si policy = FORBIDDEN ou ALLOWED)

    // 📌 BLOCAGE AUTOMATIQUE
    public enum BlockingPolicy {
        NO_BLOCKING,
        BLOCK_AFTER_MISSED_APPOINTMENTS
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlockingPolicy blockingPolicy = BlockingPolicy.NO_BLOCKING;

    private Integer autoBlockThreshold; // Nombre de RDV non respectés avant blocage (nullable si policy = NO_BLOCKING)

    // 📌 VALIDATION
    public enum ValidationMode {
        AUTOMATIC_FOR_ALL,
        MANUAL_FOR_NEW_CLIENTS,
        MANUAL_FOR_ALL
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ValidationMode validationMode = ValidationMode.AUTOMATIC_FOR_ALL;

    // 📌 RESTRICTION PAR CLIENT
    @Column(nullable = false)
    private int maxAppointmentsPerClient = 5; // Nombre max de RDV actifs par client


    private Integer toleranceTimeMinutes = 15; // Temps de tolérance en minutes avant marquage "MISSED"

}
