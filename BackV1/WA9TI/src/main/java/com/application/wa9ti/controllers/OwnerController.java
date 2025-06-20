package com.application.wa9ti.controllers;

import com.application.wa9ti.dtos.*;
import com.application.wa9ti.enums.Role;
import com.application.wa9ti.models.Employee;
import com.application.wa9ti.models.Owner;
import com.application.wa9ti.models.Store;
import com.application.wa9ti.repositories.EmployeeStoreRepository;
import com.application.wa9ti.repositories.OwnerRepository;
import com.application.wa9ti.repositories.StoreRepository;
import com.application.wa9ti.services.auth.AuthorizationService;
import com.application.wa9ti.services.owner.OwnerService;
import com.application.wa9ti.services.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/owners")
@AllArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;
    private final AuthorizationService authorizationService;
    private final OwnerRepository ownerRepository;
    private final EmployeeStoreRepository employeeStoreRepository;
    private final UserService userService;
    private final StoreRepository storeRepository;

    @GetMapping
    public List<Owner> getAllOwners() {
        return ownerService.getAllOwners();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Owner> getOwnerById(@PathVariable Long id) {
        return ResponseEntity.ok(ownerService.getOwnerById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Owner> updateOwner(@PathVariable Long id, @RequestBody Owner owner) {
        return ResponseEntity.ok(ownerService.updateOwner(id, owner));
    }

    @DeleteMapping("/{ownerId}")
    public ResponseEntity<Void> deleteOwner(@PathVariable Long ownerId) {
        authorizationService.isTheOwner(ownerId);
        ownerService.deleteOwner(ownerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check-can-delete/{ownerId}")
    public ResponseEntity<Boolean> canCheckDeleteOwner(@PathVariable Long ownerId) {
        authorizationService.isTheOwner(ownerId);
        return ResponseEntity.ok(ownerService.canCancelAccount(ownerId));
    }

    @PostMapping("/{id}/cancel-subscription")
    public ResponseEntity<Owner> cancelSubscription(@PathVariable Long id) {
        authorizationService.isTheOwner(id);
        return ResponseEntity.ok(ownerService.cancelSubscription(id));
    }

    @GetMapping("/{id}/is-subscription-valid")
    public ResponseEntity<Boolean> isSubscriptionValid(@PathVariable Long id) {
        authorizationService.isTheOwner(id);
        return ResponseEntity.ok(ownerService.isSubscriptionValid(id));
    }

    @PutMapping("/{id}/general-info")
    public ResponseEntity<String> updateGeneralInfo(@PathVariable Long id, @RequestBody GeneralOwnerInfoDto generalInfoDto) {
        // Appeler le service pour mettre à jour les informations générales
        authorizationService.isTheOwner(id);
        ownerService.updateGeneralInfo(id, generalInfoDto);
        return ResponseEntity.ok("Informations générales mises à jour avec succès.");
    }

    @GetMapping("/profile")
    public ResponseEntity<OwnerDto> getOwnerProfile() {
        // Obtenir l'utilisateur authentifié depuis le contexte de sécurité
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isOwner = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals(Role.ROLE_OWNER.name()));

        if (!isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }


        // Récupérer l'email de l'utilisateur authentifié
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        // Utiliser le service pour récupérer le DTO
        OwnerDto ownerDto = ownerService.getAuthenticatedOwner(email);
        return ResponseEntity.ok(ownerDto);
    }

    @GetMapping("/{storeId}/is-employee/{ownerId}")
    public ResponseEntity<Boolean> checkIfOwnerIsEmployeeWithStore(@PathVariable Long storeId,@PathVariable Long ownerId) {
        // Vérifier que l'utilisateur est bien le propriétaire
        authorizationService.isTheOwner(ownerId);

        // Récupérer l'Owner
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner non trouvé"));
        Store store = storeRepository.findById(storeId)
                .orElseThrow(()-> new IllegalArgumentException("Store non trouvé"));
        // Vérifier s'il a un Employee assigné
        Employee employee = owner.getUser().getEmployee();

        // ✅ Vérifier s'il a AU MOINS un Store actif dans EmployeeStore
        boolean isEmployee = (employee != null) && employeeStoreRepository.existsByEmployee(employee);

        if(isEmployee)
        {
            isEmployee = employeeStoreRepository.existsByEmployeeAndStore(employee, store);
        }

        return ResponseEntity.ok(isEmployee);
    }


    @PostMapping("/{ownerId}/assign-owner/{storeId}")
    public ResponseEntity<Void> assignOwnerAsEmployee(@PathVariable Long ownerId, @PathVariable Long storeId) {
        authorizationService.isTheOwner(ownerId);
        ownerService.assignOwnerAsEmployee(ownerId, storeId);
        return ResponseEntity.ok().build();
    }

    // 2️⃣ Supprimer l'assignation (retirer l'Employee)
    @DeleteMapping("/{ownerId}/remove-owner/{storeId}")
    public ResponseEntity<Void> removeOwnerAsEmployee(@PathVariable Long ownerId,@PathVariable Long storeId) {
        authorizationService.canAccessStore(storeId);
        ownerService.removeOwnerAsEmployee(ownerId,storeId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{storeId}/subscription/{ownerId}")
    public SubscriptionDto getSubscriptionByOwner(@PathVariable Long storeId,@PathVariable Long ownerId) {
        authorizationService.isTheOwner(ownerId);
        authorizationService.canAccessStore(storeId);
        return ownerService.getSubscriptionByOwnerId(storeId,ownerId);
    }

    @GetMapping("/paged-owner-invoices")
    public ResponseEntity<PagedModel<EntityModel<InvoiceDto>>> getInvoicesByOwner(
            @RequestParam Long ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            PagedResourcesAssembler<InvoiceDto> assembler)
    {

        authorizationService.isTheOwner(ownerId);
        Page<InvoiceDto> invoicesPage = ownerService.getInvoicesByOwnerId(ownerId, page, size, startDate, endDate);
        PagedModel<EntityModel<InvoiceDto>> pagedModel = assembler.toModel(invoicesPage);

        return ResponseEntity.ok(pagedModel);
    }


    @GetMapping("/{ownerId}/stats")
    public ResponseEntity<OwnerStatsDTO> getOwnerStats(@PathVariable Long ownerId) {
        OwnerStatsDTO stats = ownerService.getOwnerStats(ownerId);
        return ResponseEntity.ok(stats);
    }

}
