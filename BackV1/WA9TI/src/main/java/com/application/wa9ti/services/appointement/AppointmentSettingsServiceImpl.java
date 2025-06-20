package com.application.wa9ti.services.appointement;

import com.application.wa9ti.dtos.AppointmentSettingsDTO;
import com.application.wa9ti.models.AppointmentSettings;
import com.application.wa9ti.models.Store;
import com.application.wa9ti.repositories.AppointmentSettingsRepository;
import com.application.wa9ti.repositories.StoreRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AppointmentSettingsServiceImpl implements AppointmentSettingsService {

    private final AppointmentSettingsRepository repository;
    private final StoreRepository storeRepository;

    public AppointmentSettingsServiceImpl(AppointmentSettingsRepository repository, StoreRepository storeRepository) {
        this.repository = repository;
        this.storeRepository = storeRepository;
    }

    @Override
    public Optional<AppointmentSettingsDTO> findByStoreId(Long storeId) {
        return repository.findByStoreId(storeId)
                .map(this::convertToDTO);
    }

    /**
     * Convertit un objet `AppointmentSettings` en `AppointmentSettingsDTO`
     * @param settings L'entité `AppointmentSettings`
     * @return Le DTO correspondant
     */
    private AppointmentSettingsDTO convertToDTO(AppointmentSettings settings) {
        return new AppointmentSettingsDTO(
                settings.getCancellationPolicy(),
                settings.getCancellationDeadlineHours(),
                settings.getModificationPolicy(),
                settings.getModificationDeadlineHours(),
                settings.getBlockingPolicy(),
                settings.getAutoBlockThreshold(),
                settings.getValidationMode(),
                settings.getMaxAppointmentsPerClient(),
                settings.getToleranceTimeMinutes()
        );
    }

    @Override
    public AppointmentSettings saveOrUpdate(Long storeId, AppointmentSettingsDTO dto) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        AppointmentSettings settings = repository.findByStoreId(storeId)
                .orElse(new AppointmentSettings());

        settings.setStore(store);
        settings.setCancellationPolicy(dto.cancellationPolicy());
        settings.setCancellationDeadlineHours(dto.cancellationDeadlineHours());
        settings.setModificationPolicy(dto.modificationPolicy());
        settings.setModificationDeadlineHours(dto.modificationDeadlineHours());
        settings.setBlockingPolicy(dto.blockingPolicy());
        settings.setAutoBlockThreshold(dto.autoBlockThreshold());
        settings.setValidationMode(dto.validationMode());
        settings.setMaxAppointmentsPerClient(dto.maxAppointmentsPerClient());
        settings.setToleranceTimeMinutes(dto.toleranceTimeMinutes());

        return repository.save(settings);
    }
}
