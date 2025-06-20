package com.application.wa9ti.services.storeService;

import com.application.wa9ti.dtos.ServiceDTO;
import com.application.wa9ti.dtos.StoreServiceDto;
import com.application.wa9ti.enums.Role;
import com.application.wa9ti.enums.SubRole;
import com.application.wa9ti.models.*;
import com.application.wa9ti.repositories.*;
import com.application.wa9ti.services.auth.AuthorizationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;


@org.springframework.stereotype.Service
public class StoreServiceServiceImp implements StoreServiceService {

    @Autowired
    private StoreServiceRepository serviceRepository;

    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private AuthorizationService authorizationService;
    @Autowired
    private OwnerRepository ownerRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;


    @Override
    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }

    @Override
    public Service getServiceById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id " + id));
    }

    @Override
    public List<Service> getServicesByStoreId(Long storeId) {
        return serviceRepository.findByStoreId(storeId);
    }


    @Override
    public List<Service> getActifServicesByStoreId(Long storeId) {
        // Retourne uniquement les services actifs
        return serviceRepository.findByStoreIdAndIsActifTrue(storeId);
    }

    @Override
    public ServiceDTO createService(StoreServiceDto serviceDto, Long storeId) {
        // Vérification que le Store existe
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with id " + storeId));

        // Création de l'entité Service
        Service service = new Service();
        service.setName(serviceDto.name());
        service.setTime(serviceDto.time());
        service.setPrice(serviceDto.price());
        service.setDescription(serviceDto.description());
        service.setActif(serviceDto.isActif());
        service.setCategoryName(serviceDto.categoryName());
        service.setStore(store); // Liaison avec le Store

        // Sauvegarde du Service
        serviceRepository.save(service);
        return ServiceDTO.fromEntity(service);
    }

    @Override
    @Transactional
    public Service updateService(Long id, StoreServiceDto service) {
        // Vérifier si le service existe
        Service existingService = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Service not found with ID: " + id));

// 3️⃣ Vérifier si le service est associé à des rendez-vous en cours (CONFIRMED ou PENDING)
        boolean hasActiveAppointments = appointmentRepository.existsByServiceAndStatusIn(
                existingService, List.of(Appointment.Status.CONFIRMED, Appointment.Status.PENDING));

        if (hasActiveAppointments) {
            throw new IllegalArgumentException("Ce service est actuellement utilisé dans des rendez-vous en cours. Modification impossible.");
        }
        // Mettre à jour les champs nécessaires à partir du DTO
        existingService.setName(service.name());
        existingService.setTime(service.time());
        existingService.setPrice(service.price());
        existingService.setDescription(service.description());
        existingService.setCategoryName(service.categoryName());
        existingService.setActif(service.isActif());

        // Sauvegarder les modifications
        return serviceRepository.save(existingService);
    }

    @Override
    @Transactional
    public void deleteService(Long storeId, Long serviceId) {
        // 1️⃣ Vérifier si le Store existe
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store introuvable"));

        // 2️⃣ Vérifier si le Service existe et appartient bien à ce store
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service introuvable"));

        if (!service.getStore().equals(store)) {
            throw new IllegalArgumentException("Ce service n'appartient pas à ce magasin");
        }

        // 3️⃣ Vérifier si le service est associé à des rendez-vous en cours (CONFIRMED ou PENDING)
        boolean hasActiveAppointments = appointmentRepository.existsByServiceAndStatusIn(
                service, List.of(Appointment.Status.CONFIRMED, Appointment.Status.PENDING));

        if (hasActiveAppointments) {
            throw new IllegalArgumentException("Ce service est actuellement utilisé dans des rendez-vous en cours. Suppression impossible.");
        }

        // 3️⃣ Vérifier si le service est associé à des rendez-vous en cours (CONFIRMED ou PENDING)
        boolean hasOldAppointments = appointmentRepository.existsByServiceAndStatusIn(
                service, List.of(Appointment.Status.MISSED, Appointment.Status.COMPLETED));

        if (hasOldAppointments) {
            throw new IllegalArgumentException("Ce service a déjà été utilisé pour d'anciens rendez-vous. Il ne peut pas être supprimé, mais vous pouvez le modifier.");
        }
        // 4️⃣ Vérifier si l'utilisateur connecté est bien autorisé à supprimer ce service
        String userEmail = authorizationService.getAuthenticatedUserEmail();
        Role userRole = authorizationService.getAuthenticatedUserRole();

        if (userRole == Role.ROLE_OWNER) {
            Owner owner = ownerRepository.findByUser_Email(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Owner introuvable"));
            if (!store.getOwner().equals(owner)) {
                throw new IllegalArgumentException("Vous n'êtes pas autorisé à supprimer ce service.");
            }
        } else if (userRole == Role.ROLE_EMPLOYEE) {
            Employee employee = employeeRepository.findByUser_Email(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Employé introuvable"));

            // Vérifier si l'employé est assigné au magasin et qu'il est admin
            boolean isAdmin = employee.getEmployeeStores().stream()
                    .anyMatch(employeeStore -> employeeStore.getStore().equals(store) &&
                            employeeStore.getRole() == SubRole.Admin);

            if (!isAdmin) {
                throw new IllegalArgumentException("Vous n'êtes pas autorisé à supprimer ce service.");
            }
        } else {
            throw new IllegalArgumentException("Accès non autorisé.");
        }

        // 5️⃣ Supprimer le service
        serviceRepository.delete(service);
    }



    @Override
    public void updateServiceStatus(Long id, boolean status) {
        // Récupérer le service par son ID
        Optional<Service> optionalService = serviceRepository.findById(id);

        if (optionalService.isEmpty()) {
            throw new EntityNotFoundException("Service not found with ID: " + id);
        }

        // Mettre à jour le statut
        Service service = optionalService.get();
        service.setActif(status);

        // Sauvegarder les changements
        serviceRepository.save(service);

    }

}
