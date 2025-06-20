package com.application.wa9ti.dtos;

import com.application.wa9ti.models.AppointmentSettings;
import com.application.wa9ti.models.AppointmentSettings.CancellationPolicy;
import com.application.wa9ti.models.AppointmentSettings.ModificationPolicy;
import com.application.wa9ti.models.AppointmentSettings.ValidationMode;

public record StoreAppointmentSettingsDTO(
        CancellationPolicy cancellationPolicy,
        Integer cancellationDeadlineHours,
        ModificationPolicy modificationPolicy,
        Integer modificationDeadlineHours,
        ValidationMode validationMode,
        Integer toleranceTimeMinutes
) {
    public static StoreAppointmentSettingsDTO fromEntity(AppointmentSettings settings) {
        return new StoreAppointmentSettingsDTO(
                settings.getCancellationPolicy(),
                settings.getCancellationDeadlineHours(),
                settings.getModificationPolicy(),
                settings.getModificationDeadlineHours(),
                settings.getValidationMode(),
                settings.getToleranceTimeMinutes()
        );
    }
}
