package com.application.wa9ti.services.appointement;
import com.application.wa9ti.dtos.AppointmentSettingsDTO;
import com.application.wa9ti.models.AppointmentSettings;

import java.util.Optional;

public interface AppointmentSettingsService {
    Optional<AppointmentSettingsDTO> findByStoreId(Long storeId);
    AppointmentSettings saveOrUpdate(Long storeId, AppointmentSettingsDTO dto);
}
