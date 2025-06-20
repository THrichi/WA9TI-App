package com.application.wa9ti.controllers;

import com.application.wa9ti.dtos.*;
import com.application.wa9ti.enums.Role;
import com.application.wa9ti.models.*;
import com.application.wa9ti.repositories.EmployeeRepository;
import com.application.wa9ti.repositories.OwnerRepository;
import com.application.wa9ti.services.appointement.AppointmentService;
import com.application.wa9ti.services.appointement.ReservationCountService;
import com.application.wa9ti.services.auth.AuthorizationService;
import com.application.wa9ti.services.store.StoreService;
import com.application.wa9ti.services.storeService.StoreServiceService;
import com.application.wa9ti.services.subscription.SubscriptionService;
import com.application.wa9ti.services.subscription.SubscriptionServiceImp;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    @Autowired
    private StoreService storeService;

    @Autowired
    private StoreServiceService storeServiceImpl;
    @Autowired
    private OwnerRepository ownerRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AuthorizationService authorizationService;
    @Autowired
    private StoreServiceService storeServiceServiceImp;
    @Autowired
    private ReservationCountService reservationCountServiceImpl;
    @Autowired
    private SubscriptionService subscriptionService;

    @GetMapping
    public List<Store> getAllStores() {
        return storeService.getAllStores();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreDTO> getStoreById(@PathVariable Long id) {
        Store store = storeService.getStoreById(id).orElseThrow(()-> new IllegalArgumentException("No store found"));
        return ResponseEntity.ok(StoreDTO.fromEntity(store));
    }


    @GetMapping("/by-url/{storeUrl}")
    public ResponseEntity<StoreClientsDTO> getStoreByUrl(@PathVariable String storeUrl)
    {
        return ResponseEntity.ok(storeService.findByStoreUrl(storeUrl));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Store> updateStore(@PathVariable Long id, @RequestBody Store store) {
        authorizationService.canAccessStore(id);
        return ResponseEntity.ok(storeService.updateStore(id, store));
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> deleteStore(@PathVariable Long storeId) {
        storeService.deleteStore(storeId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/opening-hours")
    public ResponseEntity<Store> updateOpeningHours(
            @PathVariable Long id,
            @RequestBody List<OpeningHoursDTO> openingHoursDTOs) {
        authorizationService.canAccessStore(id);
        return ResponseEntity.ok(storeService.updateOpeningHours(id, openingHoursDTOs));
    }

    @PutMapping("/{id}/info")
    public ResponseEntity<Store> updateStoreInfo(
            @PathVariable Long id,
            @RequestBody StoreInfosDto storeInfosDto) {
        authorizationService.canAccessStore(id);
        return ResponseEntity.ok(storeService.updateStoreInfo(id, storeInfosDto));
    }

    @GetMapping("/services/{storeId}")
    public ResponseEntity<List<ServiceDTO>> getStoreServices(@PathVariable Long storeId) {
        authorizationService.canAccessStore(storeId);
        return ResponseEntity.ok(storeServiceImpl.getServicesByStoreId(storeId).stream().map(ServiceDTO::fromEntity).toList());
    }

    @GetMapping("/actifServices/{storeId}")
    public ResponseEntity<List<ServiceDTO>> getStoreActifServices(@PathVariable Long storeId) {
        authorizationService.canAccessStore(storeId);
        return ResponseEntity.ok(storeServiceImpl.getActifServicesByStoreId(storeId).stream().map(ServiceDTO::fromEntity).toList());
    }

    @PostMapping("/addService/{storeId}")
    public ResponseEntity<ServiceDTO> createStoreService(@RequestBody StoreServiceDto service,@PathVariable Long storeId) {
        authorizationService.canAccessStore(storeId);
        return ResponseEntity.ok(storeServiceImpl.createService(service,storeId));
    }

    @PatchMapping("{storeId}/services/{id}/status")
    public ResponseEntity<Void> updateServiceStatus(@PathVariable Long storeId ,@PathVariable Long id, @RequestBody boolean isActif) {
        authorizationService.canAccessStore(storeId);
        try {
            // Appeler le service pour mettre à jour le statut
            storeServiceImpl.updateServiceStatus(id, isActif);
            return ResponseEntity.noContent().build(); // Retourner un code 204 (No Content)
        } catch (EntityNotFoundException e) {
            // Si le service n'existe pas
            return ResponseEntity.notFound().build(); // Retourner un code 404
        } catch (Exception e) {
            // Gestion des erreurs génériques
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Retourner un code 500
        }
    }

    @PutMapping("/{storeId}/services/{serviceId}")
    public ResponseEntity<ServiceDTO> updateService(@PathVariable Long storeId,@PathVariable Long serviceId, @RequestBody StoreServiceDto serviceDto) {
        authorizationService.canAccessStore(storeId);
        Service updatedService = storeServiceImpl.updateService(serviceId, serviceDto);
        return ResponseEntity.ok(ServiceDTO.fromEntity(updatedService));
    }

    @PostMapping("/{storeId}/closures")
    public ResponseEntity<StoreClosure> addClosureToStore(
            @PathVariable Long storeId, @RequestBody StoreClosureDTO storeClosure) {
        authorizationService.canAccessStore(storeId);
        try {
            // Appeler le service pour ajouter la fermeture
            StoreClosure newClosure =  storeService.addClosureToStore(storeId, storeClosure);
            // Retourner la fermeture ajoutée avec un statut 201 Created
            return ResponseEntity.status(HttpStatus.CREATED).body(newClosure);
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            // Laisser le GlobalExceptionHandler gérer cette exception
            throw e;
        } catch (Exception e) {
            // Lever une exception générique pour être gérée globalement
            throw new RuntimeException("Une erreur inattendue s'est produite.", e);
        }
    }

    @GetMapping("/{storeId}/closures")
    public ResponseEntity<List<StoreClosure>> getClosuresStore(@PathVariable Long storeId) {
        authorizationService.canAccessStore(storeId);
        try {
            List<StoreClosure> closures = storeService.getClosuresStore(storeId);
            return ResponseEntity.ok(closures);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("{storeId}/closures/{closureId}")
    public ResponseEntity<Void> deleteClosureById(@PathVariable Long storeId,@PathVariable Long closureId) {
        authorizationService.canAccessStore(storeId);
        storeService.deleteClosureById(closureId);
        return ResponseEntity.noContent().build(); // Retourne un statut HTTP 204 (No Content)
    }

    @GetMapping("/owner/stores")
    public ResponseEntity<List<StoreDTO>> getStoresForAuthenticatedOwner() {
        // Obtenir l'utilisateur authentifié depuis le contexte de sécurité
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Vérifier si l'utilisateur a le rôle "ROLE_OWNER"
        boolean isOwner = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals(Role.ROLE_OWNER.name()));

        if (!isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Accès interdit si pas "ROLE_OWNER"
        }

        // Récupérer l'email de l'utilisateur authentifié
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();

        // Récupérer le propriétaire associé à cet email
        Owner owner = ownerRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Owner not found for email: " + email));

        // Retourner la liste des magasins du propriétaire
        return ResponseEntity.ok(owner.getStores().stream().map(StoreDTO::fromEntity).toList());
    }


    @GetMapping("/employee/stores")
    public ResponseEntity<List<StoreDTO>> getStoresForAuthenticatedEmployee() {
        // Obtenir l'utilisateur authentifié depuis le contexte de sécurité
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Vérifier si l'utilisateur a le rôle "ROLE_EMPLOYEE"
        boolean isEmployee = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals(Role.ROLE_EMPLOYEE.name()));

        if (!isEmployee) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Accès interdit si pas "ROLE_EMPLOYEE"
        }

        // Récupérer l'email de l'utilisateur authentifié
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();

        // Récupérer l'employé associé à cet email
        Employee employee = employeeRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Employee not found for email: " + email));

        // Récupérer les magasins associés via EmployeeStore
        List<Store> stores = employee.getEmployeeStores().stream()
                .map(EmployeeStore::getStore)
                .toList();
        return ResponseEntity.ok(stores.stream().map(StoreDTO::fromEntity).toList());
    }

   /* @GetMapping("/nearby")
    public ResponseEntity<List<StoreSearchDTO>> getNearbyStores(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String serviceName) {

        double radius = 10; // Valeur temporaire, mais ne sera pas utilisée
        List<StoreSearchDTO> stores = storeService.findStoresNearby(latitude, longitude, radius, name, type, serviceName);
        return ResponseEntity.ok(stores);
    }*/

    @GetMapping("/nearby")
    public ResponseEntity<List<StoreSearchDTO>> getNearbyStores(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam("radius") Double radius,
            @RequestParam(required = false) String keyword) {

        List<StoreSearchDTO> stores = storeService.findStoresNearby(latitude, longitude, radius, keyword);
        return ResponseEntity.ok(stores);
    }


    @GetMapping("/exists/{storeUrl}")
    public ResponseEntity<Boolean> checkStoreName(@PathVariable String storeUrl)
    {
        return ResponseEntity.ok(storeService.isStoreUrlTaken(storeUrl));
    }

    @GetMapping("/store-appointments")
    public ResponseEntity<PagedModel<EntityModel<AppointmentValidationDTO>>> getSortedAppointments(
            @RequestParam Long storeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            PagedResourcesAssembler<AppointmentValidationDTO> assembler){
        authorizationService.canAccessStore(storeId);
        String searchKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword;
        Page<AppointmentValidationDTO> appointmentsPage = appointmentService.getSortedAppointments(storeId, searchKeyword, page, size);
        PagedModel<EntityModel<AppointmentValidationDTO>> pagedModel = assembler.toModel(appointmentsPage);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/store-appointments/archive")
    public ResponseEntity<PagedModel<EntityModel<AppointmentValidationDTO>>> getSortedArchivedAppointments(
            @RequestParam Long storeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            PagedResourcesAssembler<AppointmentValidationDTO> assembler)
    {
        authorizationService.canAccessStore(storeId);
        String searchKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword;
        Page<AppointmentValidationDTO> appointmentsPage = appointmentService.getSortedArchivedAppointments(storeId, searchKeyword, page, size);
        PagedModel<EntityModel<AppointmentValidationDTO>> pagedModel = assembler.toModel(appointmentsPage);
        return ResponseEntity.ok(pagedModel);
    }


    @GetMapping("/store-appointments/validation/today")
    public List<AppointmentValidationDTO> getTodayAppointmentsForClient(
            @RequestParam Long storeId,
            @RequestParam String keyword)
    {
        authorizationService.canAccessStore(storeId);
        return appointmentService.getTodayAppointmentsForClient(storeId, keyword);
    }

    @GetMapping("/paged-clients")
    public ResponseEntity<PagedModel<EntityModel<ClientStoreDTO>>>  getClientsByStore(
            @RequestParam Long storeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            PagedResourcesAssembler<ClientStoreDTO> assembler) {

        authorizationService.canAccessStore(storeId);
        String searchKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword;

        Page<ClientStoreDTO> clientsPage = storeService.getClientsByStore(storeId, searchKeyword, page, size);
        PagedModel<EntityModel<ClientStoreDTO>> pagedModel = assembler.toModel(clientsPage);

        return ResponseEntity.ok(pagedModel);
    }

    @PatchMapping("/{storeId}/toggle-blacklist/{clientId}")
    public ResponseEntity<Void> toggleBlacklist(@PathVariable Long storeId,@PathVariable Long clientId) {
        storeService.toggleBlacklist(storeId,clientId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{storeId}/delete-service/{serviceId}")
    public ResponseEntity<Void> deleteService(
            @PathVariable Long storeId,
            @PathVariable Long serviceId) {

        storeServiceServiceImp.deleteService(storeId, serviceId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{storeId}/{clientId}/note")
    public ResponseEntity<Void> updateClientNote(@PathVariable Long storeId,
                                                 @PathVariable Long clientId,
                                                 @RequestBody String note) {
        authorizationService.canAccessStore(storeId);
        reservationCountServiceImpl.updateClientNote(storeId, clientId, note);
        return ResponseEntity.ok().build();
    }



    @PostMapping("/new-store")
    public ResponseEntity<Void> createStore(@RequestBody StoreCreateDTO storeDTO) {
        storeService.createStore(storeDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/owner-stores/{ownerId}")
    public ResponseEntity<List<StoreSimpleDTO>> getOwnerStores(@PathVariable Long ownerId) {
        authorizationService.isTheOwner(ownerId);
        return ResponseEntity.ok(storeService.getOwnerStores(ownerId));
    }

    @GetMapping("/subscription-status/{storeId}")
    public ResponseEntity<Boolean> isSubscriptionInvalid(@PathVariable Long storeId) {
        authorizationService.canAccessStore(storeId);
        Subscription.SubscriptionStatus status = subscriptionService.getSubscriptionStatusByStoreId(storeId);
        boolean isInvalid = (status == Subscription.SubscriptionStatus.EXPIRED || status == Subscription.SubscriptionStatus.CANCELED);
        return ResponseEntity.ok(isInvalid);
    }

}
