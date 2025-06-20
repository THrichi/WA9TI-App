package com.application.wa9ti.services.employee;

import com.application.wa9ti.dtos.*;
import com.application.wa9ti.enums.Role;
import com.application.wa9ti.enums.SubRole;
import com.application.wa9ti.models.*;
import com.application.wa9ti.repositories.*;
import com.application.wa9ti.services.auth.AuthorizationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ImpEmployeeService implements  EmployeeService{


    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private StoreServiceRepository storeServiceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Map<Subscription.SubscriptionType, Integer> EMPLOYEE_LIMITS = Map.of(
            Subscription.SubscriptionType.FREE, 1,
            Subscription.SubscriptionType.BASIC, 3,
            Subscription.SubscriptionType.PREMIUM, 20,
            Subscription.SubscriptionType.ENTERPRISE, 50
    );
    @Autowired
    private EmployeeStoreRepository employeeStoreRepository;
    @Autowired
    private EmployeeScheduleRepository employeeScheduleRepository;
    @Autowired
    private OpeningHoursRepository openingHoursRepository;
    @Autowired
    private SlotEmployeeRepository slotEmployeeRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private OwnerRepository ownerRepository;
    @Autowired
    private AuthorizationService authorizationService;

/*
    @Override
    public List<Employee> getAllEmployees(Long storeId) {
        return employeeRepository.findByStoreId(storeId);
    }*/

    @Override
    public Employee getEmployeeById(Long id) {
        return null;
    }

    @Override
    @Transactional
    public EmployeeStoreDto createEmployee(NewEmployeeDto employeeDto, Long storeId) {
        // Vérifier si le magasin existe
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with id " + storeId));

        // Vérifier si l'email de l'utilisateur existe déjà
        if (userRepository.findByEmail(employeeDto.email()).isPresent()) {
            throw new IllegalArgumentException("A user with this email already exists");
        }

        // Créer un nouvel utilisateur (User)
        User user = new User();
        user.setEmail(employeeDto.email());
        user.setPassword(passwordEncoder.encode(employeeDto.password())); // Hasher le mot de passe
        user.setRole(Role.ROLE_EMPLOYEE); // Exemple : "ROLE_EMPLOYEE"
        user.setPhone(""); // Exemple : "ROLE_EMPLOYEE"
        user.setName(employeeDto.name());
        userRepository.save(user);

        // Créer une nouvelle entité Employee et l'associer à l'utilisateur
        Employee employee = new Employee();
        employee.setUser(user); // Association avec l'utilisateur
        employee.setImage("default_employee.jpg"); // Optionnel : une image par défaut
        employeeRepository.save(employee);

        // Créer l'association entre l'employé et le magasin via EmployeeStore
        EmployeeStore employeeStore = new EmployeeStore();
        employeeStore.setEmployee(employee);
        employeeStore.setStore(store);
        employeeStore.setRole(employeeDto.role()); // Rôle spécifique pour ce magasin
        if (employeeDto.note() != null) {
            employeeStore.setNote(employeeDto.note());
        }
        // Sauvegarde de EmployeeStore
        employeeStore = employeeStoreRepository.save(employeeStore);

        // Récupérer les horaires d'ouverture du magasin
        List<OpeningHours> openingHours = store.getOpeningHours();

        // Créer les plannings (EmployeeSchedule) en fonction des horaires d'ouverture
        List<EmployeeSchedule> schedules = new ArrayList<>();
        for (OpeningHours openingHour : openingHours) {
            EmployeeSchedule schedule = new EmployeeSchedule();
            schedule.setEmployeeStore(employeeStore);
            schedule.setDay(openingHour.getDay());

            // Transformer les slots du magasin en SlotEmployee
            List<SlotEmployee> slotEmployees = openingHour.getSlots().stream().map(slot -> {
                SlotEmployee slotEmployee = new SlotEmployee();
                slotEmployee.setStartTime(slot.getStartTime());
                slotEmployee.setEndTime(slot.getEndTime());
                slotEmployee.setSchedule(schedule);
                return slotEmployee;
            }).collect(Collectors.toList());

            schedule.setSlots(slotEmployees);
            schedules.add(schedule);
        }

        // Sauvegarde des plannings de l'employé
        employeeScheduleRepository.saveAll(schedules);

        // Retourner l'employé
        return EmployeeStoreDto.fromEmployee(storeId,employee,employeeStore);
    }



    /*
        @Override
        public Employee createEmployee(EmployeeDto employeeDto, Long storeId) {
            // Vérifier si le magasin existe
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new RuntimeException("Store not found with id " + storeId));

            // Récupérer le type d'abonnement du propriétaire
            Owner.SubscriptionType subscriptionType = store.getOwner().getSubscription();
            int maxEmployees = EMPLOYEE_LIMITS.getOrDefault(subscriptionType, 0);

            // Vérifier le nombre d'employés existants
            long currentEmployees = employeeRepository.countByStoreId(storeId);
            if (currentEmployees >= maxEmployees) {
                throw new IllegalStateException("Employee limit reached for the current subscription");
            }

            // Créer l'entité Employee comme avant
            Employee employee = new Employee();
            employee.setName(employeeDto.getName());
            employee.setImage("default_employee.jpg");
            employee.setStore(store);
            if (employeeDto.getNote() != null) {
                employee.setNote(employeeDto.getNote());
            }

            return employeeRepository.save(employee);
        }
    */
    @Override
    public EmployeeStoreDto updateEmployee(Long storeId,Long employeeStoreId, String note, SubRole newRole) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Store not found with id " + storeId));
        // Vérifier si l'association EmployeeStore existe
        EmployeeStore employeeStore = employeeStoreRepository.findById(employeeStoreId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found in this store"));
        if(Objects.equals(employeeStore.getEmployee().getUser().getId(), store.getOwner().getUser().getId()))
        {
            throw new IllegalArgumentException("La modification est interdite pour le propriétaire du magasin.");
        }
        boolean isUpdated = false; // Flag pour éviter des sauvegardes inutiles

        // Mettre à jour la note si elle est spécifiée et différente de l'existante
        if (!Objects.equals(note, employeeStore.getNote())) {
            employeeStore.setNote(note == null ? "" : note);
            isUpdated = true;
        }


        // Mettre à jour le rôle si spécifié
        if (newRole != null && newRole != employeeStore.getRole()) {
            employeeStore.setRole(newRole);
            isUpdated = true;
        }

        // Sauvegarder uniquement si des modifications ont été faites
        if (isUpdated) {
            employeeStoreRepository.save(employeeStore);
        }

        // Retourner le DTO
        return toDto(employeeStore);
    }



    @Transactional
    @Override
    public void updateEmployeeImage(Long id, String imageURL) {
        employeeRepository.updateEmployeeImage(id,imageURL);
    }

    @Override
    public void deleteEmployee(Long id) {

    }

    @Override
    @Transactional
    public void updateEmployeeVacations(Long employeeStoreId, List<VacationSlot> vacationSlots) {
        // Étape 1 : Vérifier si l'employé existe
        EmployeeStore employeeStore = employeeStoreRepository.findById(employeeStoreId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id " + employeeStoreId));

        List<VacationSlot> currentVacations = employeeStore.getVacations();
        if (currentVacations == null) {
            currentVacations = new ArrayList<>();
        }

// Appliquer la mise à jour sur `employeeStore`
        employeeStore.setVacations(currentVacations);
        employeeStoreRepository.save(employeeStore);

        if (currentVacations == null) {
            currentVacations = new ArrayList<>();
        }

        // Étape 3 : Valider et ajouter les nouvelles plages
        for (VacationSlot newSlot : vacationSlots) {
            // Vérifier si les dates de la plage sont valides
            if (newSlot.getStartDate().isAfter(newSlot.getEndDate())) {
                throw new IllegalArgumentException(
                        "Invalid VacationSlot: startDate " + newSlot.getStartDate() +
                                " is after endDate " + newSlot.getEndDate()
                );
            }

            // Vérifier les chevauchements avec les plages existantes
            for (VacationSlot existingSlot : currentVacations) {
                if (datesOverlap(existingSlot, newSlot)) {
                    throw new IllegalArgumentException(
                            "VacationSlot overlaps with an existing slot: " +
                                    existingSlot.getStartDate() + " to " + existingSlot.getEndDate()
                    );
                }
            }

            // Si tout est valide, ajouter la nouvelle plage à la liste
            currentVacations.add(newSlot);
        }

        // Étape 4 : Mettre à jour et sauvegarder
        employeeStore.setVacations(currentVacations);
        employeeStoreRepository.save(employeeStore);
    }

    /**
     * Méthode utilitaire pour vérifier si deux plages de dates se chevauchent.
     */
    private boolean datesOverlap(VacationSlot existingSlot, VacationSlot newSlot) {
        return !newSlot.getEndDate().isBefore(existingSlot.getStartDate()) &&
                !newSlot.getStartDate().isAfter(existingSlot.getEndDate());
    }


    @Override
    @Transactional
    public void updateEmployeeService(Long employeeStoreId, Long serviceId) {
        // Étape 1 : Vérifier si l'employé existe
        EmployeeStore employeeStore = employeeStoreRepository.findById(employeeStoreId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeStoreId));

        // Étape 2 : Vérifier si le service existe
        com.application.wa9ti.models.Service service = storeServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found with ID: " + serviceId));

        // Étape 3 : Ajouter le service à la liste des services de l'employé
        List<Long> currentServices = employeeStore.getServices();
        if (!currentServices.contains(serviceId)) {
            currentServices.add(serviceId); // Ajouter le nouveau service si non présent
        }

        // Étape 4 : Activer le service s'il était désactivé
        if (!service.isActif()) {
            service.setActif(true);
            storeServiceRepository.save(service);
        }

        // Étape 5 : Sauvegarder les modifications de l'employé
        employeeStore.setServices(currentServices);
        employeeStoreRepository.save(employeeStore);
    }



    @Override
    @Transactional
    public void removeServiceFromEmployee(Long employeeStoreId, Long serviceId) {

        // Étape 2 : Vérifier si le service est associé à l'employé
        EmployeeStore employeeStore = employeeStoreRepository.findById(employeeStoreId)
                .orElseThrow(() -> new RuntimeException("L'employé n'est pas associé à ce magasin."));

        if (!employeeStore.getServices().contains(serviceId)) {
            throw new RuntimeException("Service non associé à cet employé.");
        }

        employeeStore.getServices().remove(serviceId);
        employeeStoreRepository.save(employeeStore);

        // Étape 4 : Vérifier si le service est encore assigné à un employé
        boolean isServiceAssigned = employeeRepository.existsByServicesContaining(serviceId);

        // Étape 5 : Si aucun employé ne possède ce service, le désactiver
        if (!isServiceAssigned) {
            com.application.wa9ti.models.Service service = storeServiceRepository.findById(serviceId)
                    .orElseThrow(() -> new RuntimeException("Service not found with ID: " + serviceId));

            service.setActif(false);
            storeServiceRepository.save(service);
        }
    }


    @Override
    public Employee createEmployee(NewUserDto userDto) {
        // Vérifier si l'email ou le téléphone existent déjà
        if (userRepository.findByEmail(userDto.email()).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà.");
        }

        if (userDto.role().equals(Role.ROLE_EMPLOYEE)) {
            throw new IllegalArgumentException("Creation Impossible Role Incorrect");
        }

        // Créer un nouvel utilisateur
        User user = new User();
        user.setEmail(userDto.email());
        user.setPassword(passwordEncoder.encode(userDto.password())); // Hasher le mot de passe
        user.setPhone(userDto.phone());
        user.setRole(userDto.role()); // ROLE_EMPLOYEE ou ROLE_CLIENT
        user.setName(userDto.username()); // Nom de l'utilisateur
        userRepository.save(user);

        Employee employee = new Employee();
        employee.setUser(user);
        employeeRepository.save(employee);
        return employee;
    }



    @Override
    public List<EmployeeStoreDto> getAllStoreEmployees(Long storeId) {
        // Vérifie si le magasin existe
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with id " + storeId));

        // Récupère les employés via EmployeeStore
        List<EmployeeStore> employeeStores = employeeStoreRepository.findByStore(store);

        // Convertit EmployeeStore en EmployeeStoreDto
        return employeeStores.stream()
                .map(this::toDto)
                .toList();
    }

    private EmployeeStoreDto toDto(EmployeeStore employeeStore) {
        Employee employee = employeeStore.getEmployee();

        return new EmployeeStoreDto(
                employeeStore.getId(),
                employeeStore.getStore().getId(),
                employee.getId(),
                employee.getUser().getId(),
                employee.getUser().getName(),
                employee.getUser().getEmail(),
                employee.getUser().getPhone(), // Ajoute ce champ si présent
                employee.getImage(),
                employeeStore.getNote(),
                employeeStore.getRole(),
                employeeStore.getServices(),
                employeeStore.getVacations(),
                employeeStore.isActive()
        );
    }


    @Override
    public Employee getAuthenticatedEmployee(String email) {
        // Récupère l'utilisateur par email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

        // Vérifie si l'utilisateur a le rôle "ROLE_EMPLOYEE"
        if (!Role.ROLE_EMPLOYEE.equals(user.getRole())) {
            throw new RuntimeException("User with email " + email + " is not an Employee");
        }

        // Récupère l'employé associé
        Employee employee = employeeRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Employee not found for user with email: " + email));

        // Construit et retourne le DTO avec les informations nécessaires
        return employee;
    }


    @Override
    @Transactional
    public Long createEmployeeAndAssignToStore(EmployeeInviteCreateDto employeeDto) {
        // Vérifier si l'utilisateur avec cet email existe déjà
        if (userRepository.findByEmail(employeeDto.email()).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà.");
        }

        // Vérifier si le magasin existe
        Store store = storeRepository.findById(employeeDto.storeId())
                .orElseThrow(() -> new IllegalArgumentException("Aucun magasin trouvé avec l'ID : " + employeeDto.storeId()));

        // Créer un nouvel utilisateur (User)
        User user = new User();
        user.setEmail(employeeDto.email());
        user.setPassword(passwordEncoder.encode(employeeDto.password())); // Hasher le mot de passe
        user.setRole(Role.ROLE_EMPLOYEE); // Exemple : ROLE_EMPLOYEE
        user.setVerified(true);
        user.setName(employeeDto.username());
        userRepository.save(user);

        // Créer une nouvelle entité Employee
        Employee employee = new Employee();
        employee.setUser(user);
        employee.setImage("default_employee.jpg"); // Image par défaut
        employeeRepository.save(employee);

        // Créer l'association EmployeeStore
        EmployeeStore employeeStore = new EmployeeStore();
        employeeStore.setEmployee(employee);
        employeeStore.setStore(store);
        employeeStore.setRole(employeeDto.subRole());

        // Sauvegarde de EmployeeStore
        employeeStore = employeeStoreRepository.save(employeeStore);

        // Récupérer les horaires d'ouverture du magasin

        List<OpeningHours> openingHours = openingHoursRepository.findByStoreId(store.getId());

        // Créer les plannings (EmployeeSchedule) en fonction des horaires d'ouverture
        List<EmployeeSchedule> schedules = new ArrayList<>();
        for (OpeningHours openingHour : openingHours) {
            EmployeeSchedule schedule = new EmployeeSchedule();
            schedule.setEmployeeStore(employeeStore);
            schedule.setDay(openingHour.getDay());

            // Transformer les slots du magasin en SlotEmployee
            List<SlotEmployee> slotEmployees = openingHour.getSlots().stream().map(slot -> {
                SlotEmployee slotEmployee = new SlotEmployee();
                slotEmployee.setStartTime(slot.getStartTime());
                slotEmployee.setEndTime(slot.getEndTime());
                slotEmployee.setSchedule(schedule);
                return slotEmployee;
            }).collect(Collectors.toList());

            schedule.setSlots(slotEmployees);
            schedules.add(schedule);
        }

        // Sauvegarde des plannings de l'employé
        employeeScheduleRepository.saveAll(schedules);

        return employee.getId();
    }


    @Override
    public List<AppointmentEmployeeDTO> getEmployeesWithAppointments(Long storeId, LocalDate selectedDate) {
        // Vérifie si le magasin existe
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with id " + storeId));

        // Récupère les employés du store
        List<EmployeeStore> employeeStores = employeeStoreRepository.findByStore(store);

        // Convertit les employés en DTO avec leurs rendez-vous du jour sélectionné
        return employeeStores.stream()
                .map(employeeStore -> {
                    Employee employee = employeeStore.getEmployee();
                    List<AppointmentDto> appointmentsForDate = employee.getAppointments().stream()
                            .filter(appointment -> appointment.getDate().equals(selectedDate)) // Filtre par date
                            .filter(appointment -> Objects.equals(appointment.getStore().getId(), storeId))
                            .map(this::mapToAppointmentDtoWithGuestSupport) // Utilisation de la nouvelle méthode
                            .toList();

                    return new AppointmentEmployeeDTO(
                            employee.getId(),
                            employee.getUser().getName(),
                            employee.getImage(),
                            appointmentsForDate,
                            employeeStore.getVacations(),
                            employeeStore.getServices()
                    );
                })
                .toList();
    }

    @Override
    public AppointmentEmployeeDTO getEmployeeWithAppointmentsInDatePlage(Long storeId, Long employeeId, LocalDate startDate, LocalDate endDate) {
        // Vérifie si le magasin existe
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with id " + storeId));

        // Vérifie si l'employé fait bien partie du store
        EmployeeStore employeeStore = employeeStoreRepository.findByEmployee_IdAndStore_Id(employeeId,storeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found in this store"));

        Employee employee = employeeStore.getEmployee();

        // Filtrer les rendez-vous de l'employé sur la plage de dates
        List<AppointmentDto> appointmentsInRange = employee.getAppointments().stream()
                .filter(appointment -> {
                    LocalDate appointmentDate = appointment.getDate();
                    return !appointmentDate.isBefore(startDate) && !appointmentDate.isAfter(endDate);
                }) // Filtre par plage de dates
                .filter(appointment -> Objects.equals(appointment.getStore().getId(), storeId))
                .map(this::mapToAppointmentDtoWithGuestSupport) // Utilisation de la méthode existante
                .toList();

        // Retourne les informations de l'employé avec ses rendez-vous
        return new AppointmentEmployeeDTO(
                employee.getId(),
                employee.getUser().getName(),
                employee.getImage(),
                appointmentsInRange,
                employeeStore.getVacations(),
                employeeStore.getServices()
        );
    }


    @Transactional
    public boolean removeVacation(Long employeeStoreId, VacationSlot vacationSlot) {
        EmployeeStore employeeStore = employeeStoreRepository.findById(employeeStoreId).orElseThrow(
                () -> new IllegalArgumentException("Employee not found with id " + employeeStoreId));

        boolean removed = employeeStore.getVacations().removeIf(v ->
                v.getStartDate().equals(vacationSlot.getStartDate()) &&
                        v.getEndDate().equals(vacationSlot.getEndDate())
        );

        if (removed) {
            employeeStoreRepository.save(employeeStore);
        }
        return removed;
    }


    private AppointmentDto mapToAppointmentDtoWithGuestSupport(Appointment appointment) {
        return new AppointmentDto(
                appointment.getId(),
                appointment.getDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getPrice(),
                appointment.getStore().getId(),
                appointment.getStore().getName(),
                appointment.getEmployee().getId(),
                appointment.getEmployee().getUser().getName(),
                appointment.getService().getId(),
                appointment.getService().getName(),
                appointment.getClientNote(),
                appointment.getEmployeeNote(),
                appointment.getStatus().name(), // Convertir enum en String
                appointment.getClient() != null ? appointment.getClient().getId() : null, // ID du client ou null si guest
                appointment.getClient() != null ? appointment.getClient().getUser().getName() : appointment.getClientName(), // Nom du client
                appointment.getClient() != null ? appointment.getClient().getImage() : "default_client.png", // Image client ou valeur par défaut pour guest
                appointment.getClient() != null ? appointment.getClient().getUser().getEmail() : appointment.getClientEmail(),
                appointment.getClient() != null ? appointment.getClient().getUser().getPhone() : appointment.getClientPhone(),
                appointment.getClient() != null ? appointment.getClient().getReservationCounts().size() : 0 // Nombre de réservations
        );
    }



    @Override
    @Transactional
    public void removeEmployeeFromStore(Long employeeId, Long storeId) {
        // 1️⃣ Vérifier si l'Employee et le Store existent
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employé non trouvé"));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store non trouvé"));

        // 2️⃣ Vérifier si l'Employee est bien assigné à ce Store
        EmployeeStore employeeStore = employeeStoreRepository.findByEmployeeAndStore(employee, store)
                .orElseThrow(() -> new IllegalArgumentException("Cet employé n'est pas assigné à ce store."));

        // 3️⃣ Vérifier que l'utilisateur connecté a le droit de supprimer cet employé
        String authenticatedEmail = authorizationService.getAuthenticatedUserEmail();

        boolean isAuthorized = ownerRepository.existsByUser_EmailAndStoresContains(authenticatedEmail, store) ||
                employeeStoreRepository.existsByEmployee_User_EmailAndStore_IdAndRole(authenticatedEmail, storeId, SubRole.Admin);

        if (!isAuthorized) {
            throw new IllegalArgumentException("Vous n'avez pas l'autorisation de supprimer un employé de ce store.");
        }

        // 4️⃣ Empêcher la suppression de soi-même
        if (authenticatedEmail.equals(employee.getUser().getEmail())) {
            throw new IllegalArgumentException("Vous ne pouvez pas vous retirer vous-même du store.");
        }

        // 5️⃣ Vérifier si l'Employee a des rendez-vous en cours (CONFIRMED ou PENDING)
        boolean hasActiveAppointments = appointmentRepository.existsByEmployeeAndStoreIdAndStatusIn(employee,storeId,
                List.of(Appointment.Status.CONFIRMED, Appointment.Status.PENDING));

        if (hasActiveAppointments) {
            throw new IllegalArgumentException("L'employé a des rendez-vous en cours. Impossible de le retirer du store.");
        }

        // 6️⃣ Supprimer les créneaux horaires (SlotEmployee) liés à cet EmployeeSchedule
        slotEmployeeRepository.deleteByEmployeeStore(employeeStore);

        // 7️⃣ Supprimer les plannings (EmployeeSchedule) liés à cet EmployeeStore
        employeeScheduleRepository.deleteByEmployeeStore(employeeStore);

        // 8️⃣ Supprimer l’assignation de l’Employee à ce Store (EmployeeStore)
        employeeStoreRepository.delete(employeeStore);

    }


    @Override
    public void updateGeneralInfo(Long id, GeneralEmployeeInfoDto generalInfoDto) {
        // Récupérer le propriétaire existant

        Employee existingEmployee = employeeRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("Employee not found with id " + id));

        // Mettre à jour les informations générales
        existingEmployee.getUser().setName(generalInfoDto.name());

        // Sauvegarder les modifications
        employeeRepository.save(existingEmployee);
    }

    @Override
    public void updatePhoneInfo(Long id, PhoneDto phoneDto) {
        // Récupérer le propriétaire existant

        Employee existingEmployee = employeeRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("Employee not found with id " + id));

        // Mettre à jour les informations générales
        existingEmployee.getUser().setPhone(phoneDto.phone());

        // Sauvegarder les modifications
        employeeRepository.save(existingEmployee);
    }

    @Override
    public EmployeeCompletedStoreDto getEmployeeCompletedProfile(Long storeId) {
        String email = authorizationService.getAuthenticatedUserEmail();
        Employee employee = employeeRepository.findByUser_Email(email).orElseThrow(()-> new IllegalArgumentException("Employee not found with email " + email));
        EmployeeStore employeeStore = employeeStoreRepository.findByEmployee_IdAndStore_Id(employee.getId(),storeId).orElseThrow(
                () -> new IllegalArgumentException("Employee not found with id " + employee.getId())
        );
        List<String> serviceNames = employeeStore.getServices().stream()
                .map(this::getServiceNameById) // 🔥 Convertir les IDs en noms
                .toList();

        return EmployeeCompletedStoreDto.fromEmployee(storeId,employee,employeeStore,serviceNames);
    }

    private String getServiceNameById(Long serviceId) {
        // Implémentation selon ta logique (ex: chercher dans la BDD)
        return storeServiceRepository.findById(serviceId)
                .map(com.application.wa9ti.models.Service::getName)
                .orElse("Service inconnu");
    }
}
