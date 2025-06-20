package com.application.wa9ti.controllers;

import com.application.wa9ti.dtos.PasswordChangeDto;
import com.application.wa9ti.dtos.UserPreferencesDto;
import com.application.wa9ti.models.User;
import com.application.wa9ti.services.auth.AuthorizationService;
import com.application.wa9ti.services.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthorizationService authorizationService;

    /**
     * Récupère les préférences de l'utilisateur connecté
     */
    @GetMapping("/preferences")
    public ResponseEntity<UserPreferencesDto> getUserPreferences() {
        String email = authorizationService.getAuthenticatedUserEmail();
        UserPreferencesDto preferences = userService.getUserPreferences(email);
        return ResponseEntity.ok(preferences);
    }


    @PutMapping("/preferences")
    public ResponseEntity<String> updatePreferences(
            @RequestBody UserPreferencesDto preferences) {
        String email = authorizationService.getAuthenticatedUserEmail();
        userService.updateUserPreferences(email, preferences.language(), preferences.theme());
        return ResponseEntity.ok("Préférences mises à jour avec succès.");
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable Long id,
            @RequestBody PasswordChangeDto passwordChangeDto,
            HttpServletRequest request
    ) {
        userService.updatePassword(id, passwordChangeDto, request);
        return ResponseEntity.noContent().build();
    }
}
