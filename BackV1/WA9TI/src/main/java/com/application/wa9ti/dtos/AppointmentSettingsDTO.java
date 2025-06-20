package com.application.wa9ti.dtos;

import com.application.wa9ti.models.AppointmentSettings.*;

public record AppointmentSettingsDTO(
        CancellationPolicy cancellationPolicy,
        Integer cancellationDeadlineHours,
        ModificationPolicy modificationPolicy,
        Integer modificationDeadlineHours,
        BlockingPolicy blockingPolicy,
        Integer autoBlockThreshold,
        ValidationMode validationMode,
        int maxAppointmentsPerClient,
        int toleranceTimeMinutes
) {
    // Ajout de valeurs par défaut pour éviter les problèmes de null
    public AppointmentSettingsDTO {
        if (cancellationPolicy == null) cancellationPolicy = CancellationPolicy.FORBIDDEN;
        if (modificationPolicy == null) modificationPolicy = ModificationPolicy.FORBIDDEN;
        if (blockingPolicy == null) blockingPolicy = BlockingPolicy.NO_BLOCKING;
        if (validationMode == null) validationMode = ValidationMode.AUTOMATIC_FOR_ALL;
        if (maxAppointmentsPerClient < 1) maxAppointmentsPerClient = 1; // Valeur minimale 1
        if (cancellationDeadlineHours < 1) cancellationDeadlineHours = 24; // Valeur minimale 1
        if (modificationDeadlineHours < 1) modificationDeadlineHours = 24; // Valeur minimale 1
        if (autoBlockThreshold < 1) autoBlockThreshold = 1; // Valeur minimale 1
        if (toleranceTimeMinutes < 1) toleranceTimeMinutes = 1; // Valeur minimale 1
    }
}
