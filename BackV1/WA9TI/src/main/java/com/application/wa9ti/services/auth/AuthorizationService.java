package com.application.wa9ti.services.auth;

import com.application.wa9ti.enums.Role;
import com.application.wa9ti.exceptions.GlobalExceptionHandler;
import com.application.wa9ti.models.Client;
import com.application.wa9ti.models.Employee;
import com.application.wa9ti.models.Owner;
import com.application.wa9ti.models.Store;
import com.application.wa9ti.repositories.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {


    private final StoreRepository storeRepository;
    private final OwnerRepository ownerRepository;
    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;

    public AuthorizationService(StoreRepository storeRepository, OwnerRepository ownerRepository, EmployeeRepository employeeRepository, ClientRepository clientRepository) {
        this.storeRepository = storeRepository;
        this.ownerRepository = ownerRepository;
        this.employeeRepository = employeeRepository;
        this.clientRepository = clientRepository;
    }

    public void canAccessStore(Long storeId) {
        String email = getAuthenticatedUserEmail();
        Role role = getAuthenticatedUserRole();

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with ID: " + storeId));

        if (role == Role.ROLE_OWNER) {
            Owner owner = ownerRepository.findByUser_Email(email)
                    .orElseThrow(() -> new IllegalArgumentException("Owner not found for email: " + email));

            if (!owner.getStores().contains(store)) {
                throw new IllegalArgumentException("You do not have access to this store.");
            }
        } else if (role == Role.ROLE_EMPLOYEE) {
            Employee employee = employeeRepository.findByUser_Email(email)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found for email: " + email));

            boolean isAssignedToStore = employee.getEmployeeStores().stream()
                    .anyMatch(employeeStore -> employeeStore.getStore().equals(store));

            if (!isAssignedToStore) {
                throw new IllegalArgumentException("You do not have access to this store.");
            }
        } else {
            throw new IllegalArgumentException("Unauthorized access.");
        }
    }

    public void isTheOwner(Long ownerId) {
        String email = getAuthenticatedUserEmail();
        Role role = getAuthenticatedUserRole();

        if (role == Role.ROLE_OWNER) {
            Owner owner = ownerRepository.findByUser_Email(email)
                    .orElseThrow(() -> new IllegalArgumentException("Owner not found for email: " + email));

            if (!owner.getId().equals(ownerId)) {
                throw new IllegalArgumentException("You do not have access to this store.");
            }
        }
    }

    public void isTheEmployee(Long employeeId) {
        String email = getAuthenticatedUserEmail();
        Role role = getAuthenticatedUserRole();

        if (role == Role.ROLE_EMPLOYEE) {
            Employee employee = employeeRepository.findByUser_Email(email)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found for email: " + email));

            if (!employee.getId().equals(employeeId)) {
                throw new IllegalArgumentException("You do not have access to this store.");
            }
        }
    }

    public void isTheClient(Long clientId) {
        String email = getAuthenticatedUserEmail();
        Role role = getAuthenticatedUserRole();

        if (role == Role.ROLE_CLIENT) {
            Client client = clientRepository.findByUserEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found for email: " + email));

            if (!client.getId().equals(clientId)) {
                throw new IllegalArgumentException("You do not have access to this store.");
            }
        }
    }

    public String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalArgumentException("Unauthorized user.");
        }
        return ((UserDetails) authentication.getPrincipal()).getUsername();
    }

    public Client getAuthenticatedClient() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalArgumentException("Unauthorized user.");
        }
        return clientRepository.findByUserEmail(((UserDetails) authentication.getPrincipal()).getUsername()).orElseThrow(
                () -> new IllegalArgumentException("Unauthorized user.")
        );
    }

    public Employee getAuthenticatedEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalArgumentException("Unauthorized user.");
        }
        return employeeRepository.findByUserEmail(((UserDetails) authentication.getPrincipal()).getUsername()).orElseThrow(
                () -> new IllegalArgumentException("Unauthorized user.")
        );
    }

    public Role getAuthenticatedUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .map(grantedAuthority -> Role.valueOf(grantedAuthority.getAuthority()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User role not found."));
    }

    public void canCreateAppointment(Long storeId,Long clientId) {
        String email = getAuthenticatedUserEmail();
        Role role = getAuthenticatedUserRole();

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with ID: " + storeId));

        if (role == Role.ROLE_OWNER) {
            Owner owner = ownerRepository.findByUser_Email(email)
                    .orElseThrow(() -> new IllegalArgumentException("Owner not found for email: " + email));

            if (!owner.getStores().contains(store)) {
                throw new IllegalArgumentException("You do not have permission to create an appointment for this store.");
            }
        } else if (role == Role.ROLE_EMPLOYEE) {
            Employee employee = employeeRepository.findByUser_Email(email)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found for email: " + email));

            // Vérifier si l'employé est affecté au magasin donné
            boolean isAssignedToStore = employee.getEmployeeStores().stream()
                    .anyMatch(employeeStore -> employeeStore.getStore().getId().equals(storeId));

            if (!isAssignedToStore) {
                throw new IllegalArgumentException("You do not have permission to create an appointment for this store.");
            }
        } else if (role == Role.ROLE_CLIENT) {
            Client client = clientRepository.findByUserEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Client not found for email: " + email));

            if (!client.getId().equals(clientId)) {
                throw new IllegalArgumentException("You do not have permission to create an appointment for this client.");
            }
        } else {
            throw new IllegalArgumentException("Unauthorized access.");
        }
    }

    public void canCreateGuestAppointment(Long storeId) {
        String email = getAuthenticatedUserEmail();
        Role role = getAuthenticatedUserRole();

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with ID: " + storeId));

        if (role == Role.ROLE_OWNER) {
            Owner owner = ownerRepository.findByUser_Email(email)
                    .orElseThrow(() -> new IllegalArgumentException("Owner not found for email: " + email));

            if (!owner.getStores().contains(store)) {
                throw new IllegalArgumentException("You do not have permission to create an appointment for this store.");
            }
        } else if (role == Role.ROLE_EMPLOYEE) {
            Employee employee = employeeRepository.findByUser_Email(email)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found for email: " + email));

            // Vérifier si l'employé est affecté au magasin donné
            boolean isAssignedToStore = employee.getEmployeeStores().stream()
                    .anyMatch(employeeStore -> employeeStore.getStore().getId().equals(storeId));

            if (!isAssignedToStore) {
                throw new IllegalArgumentException("You do not have permission to create an appointment for this store.");
            }
        }else {
            throw new IllegalArgumentException("Unauthorized access.");
        }
    }


}
