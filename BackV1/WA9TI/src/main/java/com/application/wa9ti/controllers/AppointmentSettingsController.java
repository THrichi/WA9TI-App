package com.application.wa9ti.controllers;

import com.application.wa9ti.dtos.AppointmentSettingsDTO;
import com.application.wa9ti.models.AppointmentSettings;
import com.application.wa9ti.services.appointement.AppointmentSettingsService;
import com.application.wa9ti.services.auth.AuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointment-settings")
public class AppointmentSettingsController {

    private final AppointmentSettingsService service;
    private final AuthorizationService authorizationService;

    public AppointmentSettingsController(AppointmentSettingsService service, AuthorizationService authorizationService) {
        this.service = service;
        this.authorizationService = authorizationService;
    }

    // 📌 Récupérer les paramètres d'un store
    @GetMapping("/{storeId}")
    public ResponseEntity<AppointmentSettingsDTO> getSettingsByStore(@PathVariable Long storeId) {
        authorizationService.canAccessStore(storeId);
        return service.findByStoreId(storeId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 📌 Créer ou mettre à jour les paramètres d'un store
    @PostMapping("/{storeId}")
    public ResponseEntity<AppointmentSettings> saveOrUpdateSettings(
            @PathVariable Long storeId,
            @RequestBody AppointmentSettingsDTO dto
    ) {
        authorizationService.canAccessStore(storeId);
        AppointmentSettings updatedSettings = service.saveOrUpdate(storeId, dto);
        return ResponseEntity.ok(updatedSettings);
    }


}
