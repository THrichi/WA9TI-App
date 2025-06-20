package com.application.wa9ti.controllers;

import com.application.wa9ti.dtos.*;
import com.application.wa9ti.models.Client;
import com.application.wa9ti.services.appointement.AppointmentService;
import com.application.wa9ti.services.auth.AuthorizationService;
import com.application.wa9ti.services.client.ClientServiceImp;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private ClientServiceImp clientServiceImp;
    @Autowired
    private AuthorizationService authorizationService;


    @GetMapping("/available-slots")
    public ResponseEntity<AvailableSlotsDTO> getAvailableSlots(
            @RequestParam Long storeId,
            @RequestParam List<Long> serviceIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) Long employeeId) { // Ajout du paramètre optionnel

        AvailableSlotsDTO availableSlots = appointmentService.getAvailableSlots(storeId, serviceIds, startDate, employeeId);
        return ResponseEntity.ok(availableSlots);
    }

    @PostMapping("/appointments")
    public ResponseEntity<String> createAppointment(@RequestBody AppointmentCreateDTO appointmentDto) {
        authorizationService.canCreateAppointment(appointmentDto.getStoreId(),appointmentDto.getClientId());
        appointmentService.createAppointments(appointmentDto);
        return ResponseEntity.ok("Rendez-vous enregistré avec succès !");
    }

    @PostMapping("/guest")
    public ResponseEntity<String> createGuestAppointment(@RequestBody AppointmentCreateGuestDTO appointmentDto) {
        authorizationService.canCreateGuestAppointment(appointmentDto.getStoreId());
        appointmentService.createGuestAppointments(appointmentDto);
        return ResponseEntity.ok("Rendez-vous enregistré avec succès !");
    }


    @GetMapping("/verify-client/{emailOrPhone}")
    public ClientDto verifyClient(@PathVariable String emailOrPhone) {
        return clientServiceImp.getClientByEmailOrPhone(emailOrPhone);
    }

    /**
     * Récupérer uniquement les **rendez-vous à venir** d'un client
     */
    @GetMapping("/client/{clientId}/upcoming")
    public ResponseEntity<List<AppointmentClientDTO>> getUpcomingAppointmentsForClient(@PathVariable Long clientId) {
        authorizationService.isTheClient(clientId);
        List<AppointmentClientDTO> upcomingAppointments = appointmentService.getUpcomingAppointmentsForClient(clientId);
        return ResponseEntity.ok(upcomingAppointments);
    }

    /**
     * Récupérer uniquement les **rendez-vous passés** d'un client
     */
    @GetMapping("/client/{clientId}/past")
    public ResponseEntity<List<AppointmentClientDTO>> getPastAppointmentsForClient(@PathVariable Long clientId) {
        authorizationService.isTheClient(clientId);
        List<AppointmentClientDTO> pastAppointments = appointmentService.getPastAppointmentsForClient(clientId);
        return ResponseEntity.ok(pastAppointments);
    }


    /**
     * Supprimer un rendez-vous
     */

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<String> deleteClientAppointment(@PathVariable Long appointmentId, @RequestParam Long clientId) {
        authorizationService.isTheClient(clientId);
        appointmentService.deleteClientAppointment(appointmentId, clientId);
        return ResponseEntity.ok("Rendez-vous annulé avec succès.");
    }

    /**
     * Supprimer un rendez-vous
     */

    @DeleteMapping("/{storeId}/store-admin/{appointmentId}")
    public ResponseEntity<String> deleteAppointment(@PathVariable Long storeId,@PathVariable Long appointmentId) {
        authorizationService.canAccessStore(storeId);
        appointmentService.deleteAppointment(appointmentId);
        return ResponseEntity.ok("Rendez-vous annulé avec succès.");
    }

    @PutMapping("/{storeId}/store-admin/{appointmentId}/confirm")
    public ResponseEntity<String> confirmAppointment(@PathVariable Long storeId,@PathVariable Long appointmentId) {
        authorizationService.canAccessStore(storeId);
        appointmentService.validateAppointmentStatus(appointmentId);
        return ResponseEntity.ok("Rendez-vous confirmé avec succès.");
    }

    @PutMapping("/{storeId}/store-admin/honor")
    public ResponseEntity<String> honorAppointment(@PathVariable Long storeId,@RequestBody List<Long> appointmentIds) {
        authorizationService.canAccessStore(storeId);
        appointmentService.honorAppointmentStatus(appointmentIds);
        return ResponseEntity.ok("Rendez-vous confirmé avec succès.");
    }

    @PutMapping("/{storeId}/store-admin/honor/{appointmentId}")
    public ResponseEntity<String> honorAppointment(@PathVariable Long storeId, @PathVariable Long appointmentId) {
        authorizationService.canAccessStore(storeId);
        appointmentService.honorAppointmentStatus(Collections.singletonList(appointmentId));
        return ResponseEntity.ok("Rendez-vous confirmé avec succès.");
    }

    /**
     * Vérifie si un rendez-vous peut être modifié dans un store donné.
     * @param appointmentId ID du rendez-vous.
     * @param storeId ID du store.
     * @return true si la modification est autorisée, false sinon.
     */
    @GetMapping("/{appointmentId}/store/{storeId}/can-modify")
    public ResponseEntity<Boolean> canModifyAppointment(
            @PathVariable Long appointmentId,
            @PathVariable Long storeId) {

        boolean canModify = appointmentService.canModifyAppointment(appointmentId, storeId);
        return ResponseEntity.ok(canModify);
    }
}