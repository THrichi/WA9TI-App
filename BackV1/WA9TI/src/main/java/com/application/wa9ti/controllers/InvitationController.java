package com.application.wa9ti.controllers;


import com.application.wa9ti.enums.Role;
import com.application.wa9ti.enums.SubRole;
import com.application.wa9ti.models.Owner;
import com.application.wa9ti.models.User;
import com.application.wa9ti.services.auth.AuthorizationService;
import com.application.wa9ti.services.auth.InvitationTokenService;
import com.application.wa9ti.services.employeeStore.EmployeeStoreServiceImp;
import com.application.wa9ti.services.subscription.SubscriptionServiceImp;
import com.application.wa9ti.services.user.UserServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    @Autowired
    private InvitationTokenService invitationTokenService;
    @Autowired
    private EmployeeStoreServiceImp employeeStoreService;
    @Autowired
    private UserServiceImp userServiceImp;
    @Autowired
    private AuthorizationService authorizationService;
    @Autowired
    private SubscriptionServiceImp subscriptionServiceImp;

    /**
     * Endpoint pour générer un lien d'invitation
     * @param email Email de l'employé invité
     * @param storeId ID du store
     * @param subRole Sous-rôle de l'employé (Manager, Secretary, etc.)
     * @return Le lien d'invitation contenant le token
     */
    @PostMapping("/generate")
    public ResponseEntity<String> generateInviteLink(
            @RequestParam String email,
            @RequestParam Long storeId,
            @RequestParam SubRole subRole) {

        authorizationService.canAccessStore(storeId);
        if(!subscriptionServiceImp.canAddEmployee(storeId))
        {
            throw new IllegalArgumentException("Accès refusé : ce magasin a atteint la limite maximale d’employés autorisés avec son abonnement actuel. Le propriétaire doit passer à une offre supérieure pour permettre de nouvelles embauches.");
        }
        boolean newUser = true;

        // Vérifier si l'employé est déjà associé au magasin
        if (employeeStoreService.isEmployeeInStoreByEmail(email, storeId)) {
            throw new IllegalArgumentException("L'employé avec l'email " + email + " est déjà associé à ce magasin.");
        }
        Optional<User> optionalUser = userServiceImp.findUserByEmail(email);
        if(optionalUser.isPresent())
        {
            newUser = false;
            User user = optionalUser.get();
            if (!user.getRole().equals(Role.ROLE_EMPLOYEE)) {
                throw new IllegalArgumentException(
                        "L'utilisateur avec l'email " + email + " n'a pas le rôle d'employé et ne peut pas être associé à un magasin."
                );
            }
        }
        String token = invitationTokenService.generateInvitationToken(email, storeId, subRole);
        String inviteLink = "";
        // Générer le token d'invitation
        if(newUser)
        {
            inviteLink = "http://192.168.1.135:4200/create-invite-employee?token=" + token;
        }else{
            inviteLink = "http://192.168.1.135:4200/invite-employee?token=" + token;
        }

        return ResponseEntity.ok(inviteLink);
    }


    /**
     * Endpoint pour valider un token d'invitation
     * @param token Token d'invitation envoyé dans le lien
     * @return Un message confirmant la validité ou l'invalidité du token
     */
    @GetMapping("/validate")
    public ResponseEntity<String> validateInviteToken(@RequestParam String token) {
        boolean isValid = invitationTokenService.validateInvitationToken(token);
        if (isValid) {
            return ResponseEntity.ok("Le token est valide.");
        } else {
            return ResponseEntity.badRequest().body("Le token est invalide ou expiré.");
        }
    }

    /**
     * Endpoint pour ajouter un employé à un magasin
     * @param email L'email de l'employé à ajouter
     * @param storeId L'ID du magasin auquel ajouter l'employé
     * @param subRole Le rôle de l'employé dans ce magasin
     * @param token Le token d'invitation pour validation
     * @return Un message confirmant l'ajout
     * @throws IllegalArgumentException si le token est invalide ou si une autre erreur survient
     */
    @PostMapping("/add-employee")
    public ResponseEntity<String> addEmployeeToStore(
            @RequestParam String email,
            @RequestParam Long storeId,
            @RequestParam SubRole subRole,
            @RequestParam String token) {

        if(!subscriptionServiceImp.canAddEmployee(storeId))
        {
            throw new IllegalArgumentException("Accès refusé : ce magasin a atteint la limite maximale d’employés autorisés avec son abonnement actuel. Le propriétaire doit passer à une offre supérieure pour permettre de nouvelles embauches.");
        }

        email = email.toLowerCase();

        // Valider le token
        if (!invitationTokenService.validateInvitationToken(token)) {
            throw new IllegalArgumentException("Le token est invalide ou expiré.");
        }

        if (employeeStoreService.isEmployeeInStoreByEmail(email, storeId)) {
            throw new IllegalArgumentException("L'employé avec l'email " + email + " est déjà associé à ce magasin.");
        }

        // Ajouter l'employé au magasin
        employeeStoreService.addEmployeeToStore(email, storeId, subRole);

        // Retourner une réponse de succès
        return ResponseEntity.ok("L'employé a été ajouté au magasin avec succès.");
    }

    /**
     * Vérifie si un employé est déjà associé à un magasin
     * @param email L'email de l'employé à vérifier
     * @param storeId L'ID du magasin à vérifier
     * @return Une réponse indiquant si l'employé peut être ajouté ou non
     * @throws IllegalArgumentException si l'employé est déjà associé au magasin
     */
    @PostMapping("/verify-add-employee")
    public ResponseEntity<String> verifyAddEmployeeToStore(
            @RequestParam String email,
            @RequestParam Long storeId) {

        if(!subscriptionServiceImp.canAddEmployee(storeId))
        {
            throw new IllegalArgumentException("Accès refusé : ce magasin a atteint la limite maximale d’employés autorisés avec son abonnement actuel. Le propriétaire doit passer à une offre supérieure pour permettre de nouvelles embauches.");
        }
        // Vérifier si l'employé est déjà associé au magasin
        if (employeeStoreService.isEmployeeInStoreByEmail(email, storeId)) {
            throw new IllegalArgumentException(
                    String.format("L'employé avec l'email %s est déjà associé au magasin avec l'ID %d.", email, storeId));
        }

        // Retourner une réponse de succès
        return ResponseEntity.ok("L'employé peut être ajouté au magasin.");
    }




}