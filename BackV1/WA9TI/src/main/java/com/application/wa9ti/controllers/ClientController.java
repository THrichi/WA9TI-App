package com.application.wa9ti.controllers;

import com.application.wa9ti.dtos.*;
import com.application.wa9ti.enums.Role;
import com.application.wa9ti.models.Appointment;
import com.application.wa9ti.services.appointement.AppointmentService;
import com.application.wa9ti.services.appointement.AppointmentServiceImpl;
import com.application.wa9ti.services.auth.AuthorizationService;
import com.application.wa9ti.services.client.ClientService;
import com.application.wa9ti.services.client.ClientServiceImp;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client")
@AllArgsConstructor
public class ClientController {

    private final ClientService clientServiceImp;
    private final AuthorizationService authorizationService;
    private final AppointmentServiceImpl appointmentServiceImpl;


    @GetMapping("/profile")
    public ResponseEntity<ClientDto> getOwnerProfile() {
        // Obtenir l'utilisateur authentifié depuis le contexte de sécurité
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isCLient = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals(Role.ROLE_CLIENT.name()));

        if (!isCLient) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }


        // Récupérer l'email de l'utilisateur authentifié
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        // Utiliser le service pour récupérer le DTO
        ClientDto clientDto = clientServiceImp.getAuthenticatedOwner(email);

        return ResponseEntity.ok(clientDto);
    }

    @PutMapping("/{clientId}/update")
    public ResponseEntity<ClientUpdateDto> updateClient(
            @PathVariable Long clientId,
            @RequestBody ClientUpdateDto clientUpdateDto) {
        authorizationService.isTheClient(clientId);
        ClientUpdateDto updatedClient = clientServiceImp.updateClient(clientId, clientUpdateDto);
        return ResponseEntity.ok(updatedClient);
    }

    @GetMapping("/favorite-stores")
    public List<FavoriteStoreDTO> getTop3FavoriteStores() {
        return clientServiceImp.getTop3FavoriteStores(authorizationService.getAuthenticatedClient().getUser().getEmail());
    }

    @PutMapping("/{clientId}/{id}/update-date-time")
    public ResponseEntity<AppointmentClientDTO> updateAppointmentDateTime(
            @PathVariable Long id,
            @PathVariable Long clientId,
            @RequestBody UpdateAppointmentDateTimeDTO dto) {
        authorizationService.isTheClient(clientId);
        return ResponseEntity.ok(appointmentServiceImpl.updateAppointmentDateTime(id, dto));
    }

}
