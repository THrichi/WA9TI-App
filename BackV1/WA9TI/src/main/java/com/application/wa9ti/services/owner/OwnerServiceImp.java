package com.application.wa9ti.services.owner;

import com.application.wa9ti.dtos.*;
import com.application.wa9ti.enums.Role;
import com.application.wa9ti.enums.SubRole;
import com.application.wa9ti.models.*;
import com.application.wa9ti.repositories.*;
import com.application.wa9ti.services.auth.JWTService;
import com.application.wa9ti.services.store.StoreServiceImp;
import com.application.wa9ti.services.user.UserServiceImp;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OwnerServiceImp implements OwnerService {


    private final AuthenticationManager authenticationManager;
    private final InvoiceRepository invoiceRepository;
    private final StoreServiceRepository storeServiceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final StoreClosureRepository storeClosureRepository;
    private UserRepository userRepository;

    private final OwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserServiceImp userServiceImp;
    private final StoreServiceImp storeServiceImpl;
    private final AppointmentSettingsRepository appointmentSettingsRepository;
    private final EmployeeScheduleRepository employeeScheduleRepository;
    private final EmployeeStoreRepository employeeStoreRepository;
    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final OpeningHoursRepository openingHoursRepository;
    private final AppointmentRepository appointmentRepository;
    private final SlotEmployeeRepository slotEmployeeRepository;
    private final JWTService jwtService;


    // Récupérer tous les propriétaires
    public List<Owner> getAllOwners() {
        return ownerRepository.findAll();
    }

    // Récupérer un propriétaire par ID
    public Owner getOwnerById(Long id) {
        return ownerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Owner not found with ID: " + id));
    }



    // Mettre à jour les informations d'un propriétaire
    public Owner updateOwner(Long id, Owner updatedOwner) {
        Owner existingOwner = getOwnerById(id);
        existingOwner.getUser().setName(updatedOwner.getUser().getName());
        existingOwner.setImage(updatedOwner.getImage());
        return ownerRepository.save(existingOwner);
    }

    @Override
    @Transactional
    public void deleteOwner(Long ownerId) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner introuvable"));

        if(owner.getSubscription().getType() != Subscription.SubscriptionType.FREE)
        {
            throw new IllegalStateException("Impossible de désactiver cet Owner : abonnement doit etre FREE.");
        }

        if (!canCancelAccount(ownerId)) {
            throw new IllegalStateException("Impossible de supprimer cet Owner : Un de ses magasins a des rendez-vous à venir.");
        }

        owner.getSubscription().setStatus(Subscription.SubscriptionStatus.CANCELED);
        owner.getUser().setActive(false);
        ownerRepository.save(owner);
        System.out.println("✅ Owner supprimé avec succès.");
    }


    @Override
    public boolean canCancelAccount(Long ownerId) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner introuvable"));

        // Vérifier si un de ses stores a un rendez-vous futur
        boolean hasFutureAppointments = owner.getStores().stream()
                .anyMatch(store -> appointmentRepository.existsByStoreAndDateAfter(store, LocalDate.now()));
        boolean hasEmployees = owner.getStores().stream()
                .anyMatch(store -> !store.getEmployeeStores().isEmpty());

        return !(hasFutureAppointments || hasEmployees);
    }


    @Override
    @Transactional
    public Owner cancelSubscription(Long ownerId) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Propriétaire introuvable"));

        // Annuler l'abonnement
        owner.getSubscription().setStatus(Subscription.SubscriptionStatus.CANCELED);

        return ownerRepository.save(owner);
    }

    // Vérifier si la souscription d'un propriétaire est valide
    @Override
    public boolean isSubscriptionValid(Long ownerId) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Propriétaire introuvable"));

        return owner.getSubscription().getStatus() == Subscription.SubscriptionStatus.ACTIVE;
    }

    @Override
    public void updateGeneralInfo(Long id, GeneralOwnerInfoDto generalInfoDto) {
        // Récupérer le propriétaire existant

        Owner existingOwner = getOwnerById(id);

        // Mettre à jour les informations générales
        existingOwner.getUser().setName(generalInfoDto.name());


        // Sauvegarder les modifications
        ownerRepository.save(existingOwner);


    }

    /*@Override
    public void updateEmail(Long id, EmailDto emailDto) {
        // Récupérer le propriétaire existant
        Owner existingOwner = getOwnerById(id);

        // Mettre à jour l'email
        existingOwner.setEmail(emailDto.getEmail());

        // Sauvegarder les modifications
        ownerRepository.save(existingOwner);
    }

    @Override
    public void updatePhone(Long id, PhoneDto phoneDto) {
        // Récupérer le propriétaire existant
        Owner existingOwner = getOwnerById(id);

        // Mettre à jour le numéro de téléphone
        existingOwner.setPhone(phoneDto.getPhone());

        // Sauvegarder les modifications
        ownerRepository.save(existingOwner);
    }
*/




    @Transactional
    @Override
    public void updateOwnerImage(Long id, String imageURL) {
        ownerRepository.updateImageUrlById(id,imageURL);
        Owner owner = getOwnerById(id);
        Employee employee = owner.getUser().getEmployee();
        if (employee != null) {
            employee.setImage(imageURL);
        }
    }

    /*@Override
    @Transactional
    public Owner createOwnerWithStore(OwnerWithStoreDTO dto) {
        // Étape 1 : Validation de l'email et du téléphone
        if (userServiceImp.findUserByEmail(dto.ownerEmail()).isPresent()) {
            throw new IllegalArgumentException("A user with this email already exists.");
        }

        if (userServiceImp.findUserByPhone(dto.ownerPhone()).isPresent()) {
            throw new IllegalArgumentException("A user with this phone number already exists.");
        }


        // Étape 2 : Création de l'utilisateur
        User user = new User();
        user.setEmail(dto.ownerEmail());
        user.setPassword(passwordEncoder.encode(dto.ownerPassword()));
        user.setRole(Role.ROLE_OWNER);
        user.setName(dto.ownerName());
        user.setVerified(true);
        user.setPhone(dto.ownerPhone());
        user = userRepository.save(user);


        // Étape 3 : Création de l'Owner
        Owner owner = new Owner();
        owner.setUser(user);

        Subscription subscription = new Subscription();
        subscription.setOwner(owner);
        subscription.setType(Subscription.SubscriptionType.valueOf(dto.subscriptionType().toUpperCase()));
        subscription.setBillingType(Subscription.BillingType.FIXED); // À adapter selon la logique
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusDays(30)); // Période d'essai
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);

        owner.setSubscriptions(List.of(subscription));

        // Étape 4 : Création du Store

        Store store = new Store();
        store.setName(dto.storeName());
        store.setType(dto.storeType());
        store.setAddress(dto.storeAddress());
        store.setLatitude(dto.storeLatitude());
        store.setLongitude(dto.storeLongitude());
        store.setEmail(dto.storeEmail());
        store.setPhone(dto.storePhone());
        store.setOwner(owner);
        String baseUrl = store.getName().toLowerCase().replaceAll("[^a-z0-9]", "-"); // Remplace espaces et caractères spéciaux par "-"
        String uniqueUrl = generateUniqueStoreUrl(baseUrl);

        store.setStoreUrl(uniqueUrl);

        // Gestion des horaires d'ouverture
        List<OpeningHours> openingHours = createOpeningHours(dto.openingHours());
        store.setOpeningHours(openingHours);

        // Définir les valeurs par défaut pour le Store
        store.setImage("default_store.png");
        store.setGallery(List.of());
        store.setClosures(List.of());

        // Ajouter un service par défaut
        com.application.wa9ti.models.Service defaultService = new com.application.wa9ti.models.Service(
                null,
                "Non spécifié",
                30,
                0.0,
                "Un service par défaut pour une prise en charge rapide.",
                true,
                "Non spécifié",
                store
        );
        store.setServices(List.of(defaultService));
        // Initialiser les paramètres des rendez-vous pour le store
        AppointmentSettings appointmentSettings = new AppointmentSettings();
        appointmentSettings.setStore(store);
        appointmentSettings.setCancellationPolicy(AppointmentSettings.CancellationPolicy.ALLOWED_WITH_NOTICE);
        appointmentSettings.setCancellationDeadlineHours(24); // Par défaut, annulation possible jusqu'à 24h avant
        appointmentSettings.setModificationPolicy(AppointmentSettings.ModificationPolicy.ALLOWED_WITH_NOTICE);
        appointmentSettings.setModificationDeadlineHours(24); // Par défaut, modification possible jusqu'à 24h avant
        appointmentSettings.setBlockingPolicy(AppointmentSettings.BlockingPolicy.NO_BLOCKING); // Pas de blocage automatique par défaut
        appointmentSettings.setAutoBlockThreshold(10); // Valeur par défaut (peut être définie plus tard)
        appointmentSettings.setValidationMode(AppointmentSettings.ValidationMode.AUTOMATIC_FOR_ALL); // Validation auto par défaut
        appointmentSettings.setMaxAppointmentsPerClient(5); // Limite de 5 RDV actifs par client par défaut
        store.setAppointmentSettings(appointmentSettings);


        // Associer le Store à l'Owner
        owner.setStores(List.of(store));

        // Étape 5 : Sauvegarde de l'Owner (cascade vers le Store et ses relations)
        return ownerRepository.save(owner);
    }*/
    private List<OpeningHours> createOpeningHours(List<OpeningHoursDTO> openingHoursDTOs) {
        return openingHoursDTOs.stream().map(oh -> {
            OpeningHours hours = new OpeningHours();
            hours.setDay(oh.day());

            // Vérification si la liste des slots est non nulle
            if (oh.slots() != null && !oh.slots().isEmpty()) {
                List<Slot> slots = oh.slots().stream()
                        .filter(slotDto -> isNotBlank(slotDto.startTime().toString()) && isNotBlank(slotDto.endTime().toString()))
                        .map(slotDto -> {
                            try {
                                return new Slot(
                                        null,
                                        slotDto.startTime(),
                                        slotDto.endTime()
                                );
                            } catch (Exception e) {
                                System.err.println("Erreur de parsing des heures : " + e.getMessage());
                                throw new IllegalArgumentException("Format d'heure invalide pour le slot.");
                            }
                        })
                        .toList();
                hours.setSlots(slots);
            } else {
                // Si aucun slot n'est défini, on initialise une liste vide
                hours.setSlots(List.of());
            }

            // Debugging pour vérifier le contenu
            return hours;
        }).toList();
    }

    // Méthode utilitaire pour vérifier si une chaîne est ni nulle ni vide
    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }



/*
    public OwnerDto verifyOwner(LoginDto loginDto, HttpServletResponse response) {
        // Authentification de l'utilisateur
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
        );

        if (authentication.isAuthenticated()) {
            // Génération du token JWT
            String token = jwtService.generateToken(loginDto.getEmail());
            jwtService.addJwtCookieToResponse(token, response);

            // Récupération de l'utilisateur authentifié
            User user = userRepository.findByEmail(loginDto.getEmail())
                    .orElseThrow(() -> new RuntimeException("Owner not found with email: " + loginDto.getEmail()));

            // Conversion de l'entité Owner en DTO
            return new OwnerDto(
                    owner.getId(),
                    owner.getName(),
                    owner.getSubscription().name(), // Convert Enum to String
                    owner.isActive(),
                    owner.getSubscriptionStartDate(),
                    owner.getSubscriptionEndDate(),
                    getFirstStoreId(owner),
                    getFirstStoreName(owner),
                    owner.getImage()
            );
        }

        // Retourner une exception ou gérer le cas d'échec
        throw new RuntimeException("Authentication failed for email: " + loginDto.getEmail());
    }*/

    /*public OwnerDto getAuthenticatedOwner(String email) {
        // Récupère le propriétaire à partir de l'email
        Owner owner = ownerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Owner not found for email: " + email));

        // Retourne le DTO avec les informations nécessaires
        return new OwnerDto(
                owner.getId(),
                owner.getName(),
                owner.getSubscription().name(), // Convert Enum to String
                owner.isActive(),
                owner.getSubscriptionStartDate(),
                owner.getSubscriptionEndDate(),
                getFirstStoreId(owner),
                getFirstStoreName(owner),
                owner.getImage()
        );
    }*/

    @Override
    public OwnerDto getAuthenticatedOwner(String email) {
        // Récupère l'utilisateur par email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

        // Vérifie si l'utilisateur a le rôle "ROLE_OWNER"
        if (!Role.ROLE_OWNER.equals(user.getRole())) {
            throw new RuntimeException("User with email " + email + " is not an Owner");
        }

        // Récupère le propriétaire associé
        Owner owner = ownerRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Owner not found for user with email: " + email));

        // Récupère les IDs des magasins associés à cet Owner
        List<Long> storeIds = owner.getStores().stream()
                .map(Store::getId)
                .toList();
        // Retourne le DTO avec les informations nécessaires
        return OwnerDto.fromEntity(owner);
    }


    private Long getFirstStoreId(Owner owner) {
        Long firstStoreId = null;
        if (!owner.getStores().isEmpty()) {
            firstStoreId = owner.getStores().iterator().next().getId(); // Utiliser iterator pour éviter d'appeler toString
        }
        return firstStoreId;
    }

    private String getFirstStoreName(Owner owner) {
        String firstStoreName = null;
        if (!owner.getStores().isEmpty()) {
            firstStoreName = owner.getStores().iterator().next().getName(); // Utiliser iterator pour éviter d'appeler toString
        }
        return firstStoreName;
    }

    /**
     * Génère un storeUrl unique en ajoutant un suffixe si nécessaire.
     */
    private String generateUniqueStoreUrl(String baseUrl) {
        String storeUrl = baseUrl;
        int counter = 1;

        while (storeServiceImpl.isStoreUrlTaken(storeUrl)) {
            storeUrl = baseUrl + "-" + counter;
            counter++;
        }

        return storeUrl;
    }


    @Override
    @Transactional
    public void assignOwnerAsEmployee(Long ownerId, Long storeId) {
        // 1️⃣ Vérifier si l'Owner existe
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner non trouvé"));

        // 2️⃣ Vérifier si le Store appartient bien à cet Owner
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store non trouvé"));

        if (!store.getOwner().equals(owner)) {
            throw new IllegalArgumentException("Ce store n'appartient pas à cet Owner");
        }

        // 3️⃣ Vérifier si l'Owner a déjà un Employee
        Employee employee = owner.getUser().getEmployee();

        if (employee == null) {
            // ✅ Si l'Employee n'existe pas, on le crée directement
            employee = new Employee();
            employee.setUser(owner.getUser());
            employee.setImage(owner.getImage()); // Image par défaut
            employee.setEmployeeStores(new ArrayList<>());
            employee.setAppointments(new ArrayList<>());

            employeeRepository.save(employee);
        } else {
            // ✅ Vérifier si l'Employee est déjà assigné au Store UNIQUEMENT s'il existe déjà
            boolean alreadyAssigned = employeeStoreRepository.existsByEmployeeAndStore(employee, store);
            if (alreadyAssigned) {
                throw new IllegalArgumentException("L'Owner est déjà employé dans ce Store.");
            }
        }

        // 4️⃣ Associer l'Employee à son Store via EmployeeStore
        EmployeeStore employeeStore = new EmployeeStore();
        employeeStore.setEmployee(employee);
        employeeStore.setStore(store);
        employeeStore.setNote("");
        employeeStore.setRole(SubRole.Admin);
        employeeStore.setVacations(new ArrayList<>());
        employeeStore.setServices(new ArrayList<>());

        employeeStoreRepository.save(employeeStore);

        // 5️⃣ Copier les OpeningHours du Store en EmployeeSchedule
        List<OpeningHours> openingHoursList = openingHoursRepository.findByStoreId(store.getId());
        List<EmployeeSchedule> employeeSchedules = new ArrayList<>();

        for (OpeningHours openingHour : openingHoursList) {
            EmployeeSchedule schedule = new EmployeeSchedule();
            schedule.setEmployeeStore(employeeStore);
            schedule.setDay(openingHour.getDay());

            // ✅ Associer chaque SlotEmployee à son EmployeeSchedule
            List<SlotEmployee> slotEmployees = openingHour.getSlots().stream().map(slot -> {
                SlotEmployee slotEmployee = new SlotEmployee();
                slotEmployee.setStartTime(slot.getStartTime());
                slotEmployee.setEndTime(slot.getEndTime());
                slotEmployee.setSchedule(schedule); // ✅ Ajout de l'association
                return slotEmployee;
            }).collect(Collectors.toList());

            schedule.setSlots(slotEmployees);
            employeeSchedules.add(schedule);
        }

        // 6️⃣ Sauvegarder les plannings
        employeeScheduleRepository.saveAll(employeeSchedules);
    }



    @Override
    @Transactional
    public void removeOwnerAsEmployee(Long ownerId, Long storeId) {
        // 1️⃣ Vérifier si l'Owner et le Store existent
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner non trouvé"));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store non trouvé"));

        if (!store.getOwner().equals(owner)) {
            throw new IllegalArgumentException("Ce store n'appartient pas à cet Owner");
        }

        // 2️⃣ Vérifier si l'Owner est bien un Employee
        Employee employee = owner.getUser().getEmployee();
        if (employee == null) {
            throw new IllegalArgumentException("L'Owner n'est pas un Employee");
        }

        // 3️⃣ Vérifier si l'Employee a des rendez-vous en cours
        boolean hasActiveAppointments = appointmentRepository.existsByEmployeeAndStoreIdAndStatusIn(employee,storeId,
                List.of(Appointment.Status.CONFIRMED, Appointment.Status.PENDING));

        if (hasActiveAppointments) {
            throw new IllegalArgumentException("Vous avez des rendez-vous en cours. Veuillez les honorer avant de vous retirer.");
        }

        // 4️⃣ Trouver l'assignation EmployeeStore spécifique à ce Store
        EmployeeStore employeeStore = employeeStoreRepository.findByEmployeeAndStore(employee, store)
                .orElseThrow(() -> new IllegalArgumentException("Aucune assignation trouvée entre cet employé et ce store."));

        // 5️⃣ Supprimer d'abord les SlotEmployee avant EmployeeSchedule
        slotEmployeeRepository.deleteByEmployeeStore(employeeStore);

        // 6️⃣ Supprimer tous les EmployeeSchedule associés à cet EmployeeStore
        employeeScheduleRepository.deleteByEmployeeStore(employeeStore);

        // 7️⃣ Supprimer l'assignation EmployeeStore
        employeeStoreRepository.delete(employeeStore);

    }


        @Override
        @Transactional
        public Owner createOwner(OwnerRegistrationDto dto) {
            // Vérifier si l'utilisateur existe déjà
            if (userRepository.existsByEmail(dto.email())) {
                throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà.");
            }

            // Création et sauvegarde de l'utilisateur
            User user = new User();
            user.setEmail(dto.email());
            user.setPhone(dto.phone());
            user.setPassword(passwordEncoder.encode(dto.password()));
            user.setVerified(true);
            user.setName(dto.name());
            user.setRole(Role.ROLE_OWNER);

            userRepository.save(user);

            // Création et sauvegarde du propriétaire (Owner)
            Owner owner = new Owner();
            owner.setUser(user);
            owner.setImage("default_owner.png");


// Création de l'abonnement actif
            Subscription subscription = new Subscription();
            subscription.setOwner(owner);
            subscription.setType(dto.subscriptionType());
            subscription.setBillingType(Subscription.BillingType.FIXED); // À adapter si nécessaire
            subscription.setStartDate(LocalDate.now());
            if(dto.subscriptionType() != Subscription.SubscriptionType.FREE)
            {
                subscription.setEndDate(LocalDate.now().plusDays(30)); // 30 jours d’essai
            }
            subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);

// Ajouter l'abonnement à la liste des abonnements de l'Owner
            owner.setSubscription(subscription);

            return ownerRepository.save(owner);
        }


        @Override
        public SubscriptionDto getSubscriptionByOwnerId(Long storeId,Long ownerId) {
            Owner owner = ownerRepository.findById(ownerId)
                    .orElseThrow(() -> new IllegalArgumentException("Owner not found"));

            return SubscriptionDto.fromEntity(owner.getSubscription(),storeId);
        }

    public Page<InvoiceDto> getInvoicesByOwnerId(Long ownerId, int page, int size, String startDate, String endDate) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Vérifier si on filtre par date
        if (startDate != null && endDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter).minusDays(1);;


            Page<Invoice> invoicesPage = invoiceRepository.findByOwnerIdAndStartDateBetweenOrAdjustedEndDateBetween(ownerId, start, end, pageable);
            return invoicesPage.map(InvoiceDto::fromEntity);
        }

        // Si pas de filtre, récupérer toutes les factures
        Page<Invoice> invoicesPage = invoiceRepository.findByOwnerId(ownerId, pageable);
        return invoicesPage.map(InvoiceDto::fromEntity);
    }


    @Transactional
    public OwnerStatsDTO getOwnerStats(Long ownerId) {
        Optional<Owner> ownerOpt = ownerRepository.findById(ownerId);
        if (ownerOpt.isEmpty()) {
            throw new IllegalArgumentException("Owner not found");
        }

        Owner owner = ownerOpt.get();
        int numberOfStores = owner.getStores().size();

        // Si l'owner a 0 ou plusieurs magasins, on renvoie -1 partout
        if (numberOfStores != 1) {
            return new OwnerStatsDTO(ownerId, -1, -1, -1, -1);
        }

        Store store = owner.getStores().get(0); // Récupérer son unique magasin


        int numberOfAppointmentsThisMonth = store.getRdvCount();

        // Calcul du nombre d'employés pour ce magasin
        int numberOfEmployees = employeeStoreRepository.countEmployeesExcludingOwner(store, owner.getUser().getId());

        // Calcul du nombre de services pour ce magasin
        int numberOfServices = storeServiceRepository.countByStore(store);

        return new OwnerStatsDTO(ownerId, numberOfStores, numberOfAppointmentsThisMonth, numberOfEmployees, numberOfServices);
    }
}

