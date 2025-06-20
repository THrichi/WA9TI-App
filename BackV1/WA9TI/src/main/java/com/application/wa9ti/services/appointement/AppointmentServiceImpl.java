package com.application.wa9ti.services.appointement;

import com.application.wa9ti.configuration.SubscriptionConfig;
import com.application.wa9ti.dtos.*;
import com.application.wa9ti.models.*;
import com.application.wa9ti.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final StoreRepository storeRepository;
    private final EmployeeRepository employeeRepository;
    private final AppointmentRepository appointmentRepository;
    private final StoreClosureRepository storeClosureRepository;
    private final StoreServiceRepository storeServiceRepository;
    private final ClientRepository clientRepository;
    private final OpeningHoursRepository openingHoursRepository;
    private final EmployeeScheduleRepository employeeScheduleRepository;
    private final ReservationCountRepository reservationCountRepository;
    private final AppointmentSettingsRepository appointmentSettingsRepository;
    private final ReservationCountServiceImpl reservationCountServiceImpl;

    // ========================
    //       GET AVAILABLE
    // ========================
    @Override
    public AvailableSlotsDTO getAvailableSlots(Long storeId,
                                               List<Long> serviceIds,
                                               LocalDate startDate,
                                               Long employeeId) {
        // 1) Vérifier la présence du Store
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Magasin introuvable"));

        if(store.getOwner().getSubscription().getStatus().equals(Subscription.SubscriptionStatus.CANCELED) ||
                store.getOwner().getSubscription().getStatus().equals(Subscription.SubscriptionStatus.EXPIRED))
        {
            throw new IllegalArgumentException("Magasin indisponible pour le moment");
        }
        // Plage de 7 jours
        LocalDate endDate = startDate.plusDays(6);

        // 2) Récupération des horaires d'ouverture (patchés) du magasin
        Map<DayOfWeek, List<Slot>> storeOpeningHours = getStoreOpeningHours(storeId);

        // 3) Récupération des employés qualifiés pour les services
        List<Employee> qualifiedEmployees = getQualifiedEmployees(storeId, serviceIds);
        // 4) Récupération des rendez-vous existants sur la période
        List<Appointment> existingAppointments =
                appointmentRepository.findByStoreIdAndDateBetween(storeId, startDate, endDate);

        if (employeeId != null) {
            qualifiedEmployees = qualifiedEmployees.stream()
                    .filter(emp -> emp.getId().equals(employeeId))
                    .collect(Collectors.toList());

            existingAppointments = existingAppointments.stream()
                    .filter(app -> app.getEmployee().getId().equals(employeeId))
                    .collect(Collectors.toList());
        }

        // 5) Fermetures du magasin
        List<StoreClosure> closures = storeClosureRepository.findByStoreIdAndDateBetween(storeId, startDate, endDate);

        if (qualifiedEmployees.isEmpty()) {
            throw new IllegalArgumentException("Aucun employé qualifié disponible pour ces services.");
        }

        // 6) Construire le DTO final
        return buildAvailableSlotsDTO(
                store,
                startDate,
                endDate,
                storeOpeningHours,
                closures,
                existingAppointments,
                qualifiedEmployees,
                employeeId
        );
    }

    // =========================================================
    //              BUILDING THE AVAILABLE SLOTS
    // =========================================================
    private AvailableSlotsDTO buildAvailableSlotsDTO(Store store,
                                                     LocalDate startDate,
                                                     LocalDate endDate,
                                                     Map<DayOfWeek, List<Slot>> storeOpeningHours,
                                                     List<StoreClosure> closures,
                                                     List<Appointment> appointments,
                                                     List<Employee> availableEmployees,
                                                     Long employeeId) {

        List<AvailableSlotsDTO.DayAvailability> daysAvailability = new ArrayList<>();

        for (LocalDate currentDate = startDate; !currentDate.isAfter(endDate); currentDate = currentDate.plusDays(1)) {

            // Si le magasin est complètement fermé (closure total), on saute
            if (isStoreCompletelyClosed(currentDate, closures)) {
                continue;
            }

            Map<Slot, List<Employee>> slotToEmployees = new LinkedHashMap<>();

            // ============================
            // BOUCLE SUR TOUS LES EMPLOYÉS
            // ============================
            for (Employee employee : availableEmployees) {

                // Vérifier si l’employé est en vacances ce jour
                List<Slot> employeeDailySchedule;
                if (isEmployeeOnVacation(store.getId(),employee, currentDate)) {
                    // Employé en congé => Pas de créneaux
                    employeeDailySchedule = Collections.emptyList();
                } else {
                    // Sinon, on récupère son planning normal (déjà patché)
                    employeeDailySchedule = getEmployeeDailySchedule(
                            employee, store.getId(), currentDate.getDayOfWeek()
                    );
                }

                // Si pas de slots => on continue
                if (employeeDailySchedule.isEmpty()) {
                    continue;
                }

                // Horaires du store pour ce jour
                List<Slot> storeSlots = storeOpeningHours.getOrDefault(currentDate.getDayOfWeek(), new ArrayList<>());
                if (storeSlots.isEmpty()) {
                    continue;
                }

                // Intersection [horaires employé ∩ horaires store]
                List<Slot> intersectedSlots = intersectSlots(employeeDailySchedule, storeSlots);

                // Retrait des fermetures partielles
                List<Slot> minusClosures = new ArrayList<>();
                for (Slot s : intersectedSlots) {
                    minusClosures.addAll(adjustSlotsForStoreClosures(
                            currentDate,
                            s.getStartTime(),
                            s.getEndTime(),
                            closures
                    ));
                }

                // Retrait des RDVs existants
                List<Slot> finalEmpSlots = new ArrayList<>();
                for (Slot s : minusClosures) {
                    finalEmpSlots.addAll(adjustSlotsForAppointments(
                            currentDate,
                            s.getStartTime(),
                            s.getEndTime(),
                            appointments,
                            employee
                    ));
                }

                // Ajouter au map final
                for (Slot slot : finalEmpSlots) {
                    Slot existingSlot = findExactSlot(slotToEmployees.keySet(), slot);
                    if (existingSlot != null) {
                        slotToEmployees.get(existingSlot).add(employee);
                    } else {
                        slotToEmployees.put(slot, new ArrayList<>(List.of(employee)));
                    }
                }
            }

            // (CHANGEMENT) On ajoute un "dummy slot" 00:00..00:00 pour l’employé
            // qui n'a AUCUN slot dans slotToEmployees (uniquement si vous le souhaitez)
            // ---------------------------------------------------------------
            for (Employee emp : availableEmployees) {
                if (employeeId != null && !emp.getId().equals(employeeId)) {
                    continue;
                }
                boolean appears = slotToEmployees.values().stream()
                        .anyMatch(list -> list.contains(emp));
                if (!appears) {
                    // Ici, on crée un slot [00:00..00:00].
                    // Si vous préférez ne PAS l’afficher, commentez ce bloc.
                    Slot dummySlot = new Slot(null, LocalTime.MIDNIGHT, LocalTime.MIDNIGHT);
                    Slot existingSlot = findExactSlot(slotToEmployees.keySet(), dummySlot);
                    if (existingSlot != null) {
                        slotToEmployees.get(existingSlot).add(emp);
                    } else {
                        slotToEmployees.put(dummySlot, new ArrayList<>(List.of(emp)));
                    }
                }
            }

            // Construction de la liste SlotAvailability
            List<AvailableSlotsDTO.SlotAvailability> slotAvailabilities = slotToEmployees.entrySet().stream()
                    .map(entry -> {
                        Slot slot = entry.getKey();
                        List<Employee> employeesForSlot = entry.getValue();

                        // Si un employeeId est demandé, on filtre
                        if (employeeId != null) {
                            employeesForSlot = employeesForSlot.stream()
                                    .filter(e -> e.getId().equals(employeeId))
                                    .collect(Collectors.toList());
                            if (employeesForSlot.isEmpty()) return null;
                        }

                        // Construire la liste d’employés
                        List<AvailableSlotsDTO.EmployeeAvailability> empList = employeesForSlot.stream()
                                .map(e -> {
                                    // Récupérer les services pour le magasin concerné
                                    List<Long> employeeServices = e.getEmployeeStores().stream()
                                            .filter(es -> es.getStore().getId().equals(store.getId()))
                                            .flatMap(es -> es.getServices().stream())
                                            .toList();

                                    return new AvailableSlotsDTO.EmployeeAvailability(
                                            e.getId(),
                                            e.getUser().getName(),
                                            e.getImage(),
                                            employeeServices.size() > 1
                                    );
                                })
                                .collect(Collectors.toList());


                        return new AvailableSlotsDTO.SlotAvailability(
                                slot.getStartTime(),
                                slot.getEndTime(),
                                empList
                        );
                    })
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(AvailableSlotsDTO.SlotAvailability::getStartTime))
                    .collect(Collectors.toList());

            if (!slotAvailabilities.isEmpty()) {
                daysAvailability.add(
                        new AvailableSlotsDTO.DayAvailability(
                                currentDate,
                                currentDate.getDayOfWeek().name(),
                                slotAvailabilities
                        )
                );
            }
        }

        return new AvailableSlotsDTO(
                store.getId(),
                store.getName(),
                startDate,
                endDate,
                daysAvailability
        );
    }

    // ========================
    //    CREATE APPOINTMENT
    // ========================
    @Override
    @Transactional
    public void createAppointments(AppointmentCreateDTO appointmentDto) {
        // Vérifier l'existence du magasin
        Store store = storeRepository.findById(appointmentDto.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("Magasin introuvable"));

        // Vérifier l'existence du client
        Client client = clientRepository.findById(appointmentDto.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable"));



        // Récupérer ou créer un enregistrement ReservationCount pour ce client et ce magasin
        ClientStore reservationCount = reservationCountServiceImpl.getOrCreate(client, store);


        // Récupération (ou sélection automatique) de l'employé
        Employee employee;
        if (appointmentDto.getEmployeeId() != null) {
            employee = employeeRepository.findById(appointmentDto.getEmployeeId())
                    .orElseThrow(() -> new IllegalArgumentException("Employé introuvable"));
        } else {
            employee = findLeastBusyEmployee(
                    appointmentDto.getStoreId(),
                    appointmentDto.getDate(),
                    appointmentDto.getStartTime(),
                    appointmentDto.getStartTime().plusMinutes(getTotalDuration(appointmentDto.getServices())),
                    appointmentDto.getServices().stream().map(ServiceRequestDTO::getServiceId).toList()
            );
        }

        // Vérifier que l'employé n'est pas en vacances ce jour-là
        if (isEmployeeOnVacation(store.getId(),employee, appointmentDto.getDate())) {
            throw new IllegalArgumentException("L'employé est en vacances ce jour-là. Impossible de créer le rendez-vous.");
        }

        if (employee.getEmployeeStores().stream()
                .noneMatch(es -> es.getStore().getId().equals(appointmentDto.getStoreId()) && es.isActive())) {
            throw new IllegalArgumentException("L'employé sélectionné est inactif et ne peut pas prendre de rendez-vous.");
        }


        // Vérifier que le créneau demandé est dans les horaires d'ouverture du magasin
        Map<DayOfWeek, List<Slot>> storeOpeningHours = getStoreOpeningHours(appointmentDto.getStoreId());
        List<Slot> storeSlotsForDay = storeOpeningHours.get(appointmentDto.getDate().getDayOfWeek());

        if (storeSlotsForDay == null || storeSlotsForDay.isEmpty()) {
            throw new IllegalArgumentException("Le magasin est fermé ce jour-là.");
        }

        // Vérifier si le créneau [start .. start+totalDuration] rentre dans un slot du magasin
        LocalTime startTime = appointmentDto.getStartTime();
        LocalTime endTime = startTime.plusMinutes(getTotalDuration(appointmentDto.getServices()));
        boolean withinOpeningHours = storeSlotsForDay.stream()
                .anyMatch(slot -> !startTime.isBefore(slot.getStartTime()) && !endTime.isAfter(slot.getEndTime()));
        if (!withinOpeningHours) {
            throw new IllegalArgumentException("Le créneau sélectionné est hors des heures d'ouverture du magasin.");
        }

        // Vérifier les fermetures (storeClosure)
        boolean isStoreClosed = storeClosureRepository.findByStoreIdAndDateBetween(
                        appointmentDto.getStoreId(), appointmentDto.getDate(), appointmentDto.getDate())
                .stream()
                .anyMatch(closure ->
                        !closure.getStartDate().isAfter(appointmentDto.getDate()) &&
                                !closure.getEndDate().isBefore(appointmentDto.getDate()) &&
                                (closure.getStartTime() == null || !closure.getStartTime().isAfter(startTime)) &&
                                (closure.getEndTime() == null || !closure.getEndTime().isBefore(endTime))
                );
        if (isStoreClosed) {
            throw new IllegalArgumentException("Le magasin est fermé sur ce créneau.");
        }

        // Vérifier que l'employé travaille bien ce jour-là (horaires patchés)
        List<Slot> employeeScheduleForDay = getEmployeeDailySchedule(
                employee, store.getId(), appointmentDto.getDate().getDayOfWeek()
        );
        if (employeeScheduleForDay.isEmpty()) {
            throw new IllegalArgumentException("Cet employé ne travaille pas ce jour-là.");
        }

        // Vérifier que [start .. endTime] rentre dans au moins un slot de l'employé
        boolean withinEmployeeSchedule = employeeScheduleForDay.stream()
                .anyMatch(es -> !startTime.isBefore(es.getStartTime()) && !endTime.isAfter(es.getEndTime()));
        if (!withinEmployeeSchedule) {
            throw new IllegalArgumentException("L'employé ne travaille pas sur ce créneau horaire.");
        }

        // Gérer les créneaux pour plusieurs services
        LocalTime currentStartTime = appointmentDto.getStartTime();
        for (ServiceRequestDTO serviceRequest : appointmentDto.getServices()) {
            com.application.wa9ti.models.Service service = storeServiceRepository.findById(serviceRequest.getServiceId())
                    .orElseThrow(() -> new IllegalArgumentException("Service introuvable"));

            LocalTime serviceEndTime = currentStartTime.plusMinutes(serviceRequest.getDuration());

            // Vérifier disponibilité de l'employé sur ce créneau (pas de RDV déjà pris)
            LocalTime finalCurrentStartTime = currentStartTime;
            boolean isEmpAvailable = appointmentRepository.findByStoreIdAndDateBetween(
                    appointmentDto.getStoreId(), appointmentDto.getDate(), appointmentDto.getDate()
            ).stream().noneMatch(app ->
                    app.getEmployee().equals(employee) &&
                            app.getStartTime().isBefore(serviceEndTime) &&
                            app.getEndTime().isAfter(finalCurrentStartTime)
            );
            if (!isEmpAvailable) {
                throw new IllegalArgumentException("L'employé n'est pas disponible pour ce créneau.");
            }
            // Vérifier si le client a dépassé le nombre maximum de RDV actifs autorisés
            AppointmentSettings settings = appointmentSettingsRepository.findByStoreId(store.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Paramètres de rendez-vous introuvables pour ce magasin"));

            if (reservationCount.getNbRdvActif() >= settings.getMaxAppointmentsPerClient()) {
                throw new IllegalArgumentException("Ce client a dépassé le nombre maximum de rendez-vous actifs autorisés.");
            }
            if (reservationCount.isBlackListed()) {
                // Si le client était déjà bloqué manuellement, message générique
                throw new IllegalArgumentException("Ce magasin vous a bloqué. Contactez " + store.getPhone() + " pour plus d'informations.");
            }




            Appointment.Status status = (settings.getValidationMode().equals(AppointmentSettings.ValidationMode.MANUAL_FOR_ALL) ||
                    (reservationCount.isNewClient() && settings.getValidationMode().equals(AppointmentSettings.ValidationMode.MANUAL_FOR_NEW_CLIENTS)))
                    ? Appointment.Status.PENDING
                    : Appointment.Status.CONFIRMED;

            if(status == Appointment.Status.CONFIRMED && reservationCount.isNewClient()) {
                reservationCount.setNewClient(false);
                reservationCountRepository.save(reservationCount);
            }

            // Créer le rendez-vous
            Appointment appointment = new Appointment();
            appointment.setDate(appointmentDto.getDate());
            appointment.setStartTime(currentStartTime);
            appointment.setEndTime(serviceEndTime);
            appointment.setPrice(serviceRequest.getPrice());
            appointment.setStore(store);
            appointment.setEmployee(employee);
            appointment.setService(service);
            appointment.setClient(client);
            appointment.setClientNote(appointmentDto.getClientNote());
            appointment.setEmployeeNote(appointmentDto.getEmployeeNote());
            appointment.setStatus(status);

            // Enregistrer l'appointment
            appointmentRepository.save(appointment);

            // Passer au service suivant
            currentStartTime = serviceEndTime;
            reservationCount.setNbRdvTotal(reservationCount.getNbRdvTotal() + 1);
            reservationCount.setNbRdvActif(reservationCount.getNbRdvActif() + 1);
        }
        store.setRdvCount(store.getRdvCount() + 1);
        if(store.getRdvCount() >= SubscriptionConfig.FREE_APPOINTEMENTS && store.getOwner().getSubscription().getType() == Subscription.SubscriptionType.FREE){
            store.getOwner().getSubscription().setStatus(Subscription.SubscriptionStatus.EXPIRED);
        }
        storeRepository.save(store);
        reservationCountRepository.save(reservationCount);

    }


    @Override
    @Transactional
    public void createGuestAppointments(AppointmentCreateGuestDTO appointmentDto) {
        // Vérifier l'existence du magasin
        Store store = storeRepository.findById(appointmentDto.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("Magasin introuvable"));


        // Vérifier l'existence du magasin
        if (clientRepository.findByUserEmail(appointmentDto.getClientEmail()).isPresent()) {
            throw new IllegalArgumentException("Un compte client existe déjà avec cet email.");
        }


        // Récupération (ou sélection automatique) de l'employé
        Employee employee;
        if (appointmentDto.getEmployeeId() != null) {
            employee = employeeRepository.findById(appointmentDto.getEmployeeId())
                    .orElseThrow(() -> new IllegalArgumentException("Employé introuvable"));
        } else {
            employee = findLeastBusyEmployee(
                    appointmentDto.getStoreId(),
                    appointmentDto.getDate(),
                    appointmentDto.getStartTime(),
                    appointmentDto.getStartTime().plusMinutes(getTotalDuration(appointmentDto.getServices())),
                    appointmentDto.getServices().stream().map(ServiceRequestDTO::getServiceId).toList()
            );
        }

        // Vérifier que l'employé n'est pas en vacances ce jour-là
        if (isEmployeeOnVacation(store.getId(), employee, appointmentDto.getDate())) {
            throw new IllegalArgumentException("L'employé est en vacances ce jour-là. Impossible de créer le rendez-vous.");
        }

        if (employee.getEmployeeStores().stream()
                .noneMatch(es -> es.getStore().getId().equals(appointmentDto.getStoreId()) && es.isActive())) {
            throw new IllegalArgumentException("L'employé sélectionné est inactif et ne peut pas prendre de rendez-vous.");
        }


        // Vérifier que le créneau demandé est dans les horaires d'ouverture du magasin
        Map<DayOfWeek, List<Slot>> storeOpeningHours = getStoreOpeningHours(appointmentDto.getStoreId());
        List<Slot> storeSlotsForDay = storeOpeningHours.get(appointmentDto.getDate().getDayOfWeek());

        if (storeSlotsForDay == null || storeSlotsForDay.isEmpty()) {
            throw new IllegalArgumentException("Le magasin est fermé ce jour-là.");
        }

        // Vérifier si le créneau rentre dans un slot du magasin
        LocalTime startTime = appointmentDto.getStartTime();
        LocalTime endTime = startTime.plusMinutes(getTotalDuration(appointmentDto.getServices()));
        boolean withinOpeningHours = storeSlotsForDay.stream()
                .anyMatch(slot -> !startTime.isBefore(slot.getStartTime()) && !endTime.isAfter(slot.getEndTime()));
        if (!withinOpeningHours) {
            throw new IllegalArgumentException("Le créneau sélectionné est hors des heures d'ouverture du magasin.");
        }

        // Vérifier les fermetures exceptionnelles du magasin
        boolean isStoreClosed = storeClosureRepository.findByStoreIdAndDateBetween(
                        appointmentDto.getStoreId(), appointmentDto.getDate(), appointmentDto.getDate())
                .stream()
                .anyMatch(closure ->
                        !closure.getStartDate().isAfter(appointmentDto.getDate()) &&
                                !closure.getEndDate().isBefore(appointmentDto.getDate()) &&
                                (closure.getStartTime() == null || !closure.getStartTime().isAfter(startTime)) &&
                                (closure.getEndTime() == null || !closure.getEndTime().isBefore(endTime))
                );
        if (isStoreClosed) {
            throw new IllegalArgumentException("Le magasin est fermé sur ce créneau.");
        }

        // Vérifier que l'employé travaille bien ce jour-là
        List<Slot> employeeScheduleForDay = getEmployeeDailySchedule(
                employee, store.getId(), appointmentDto.getDate().getDayOfWeek()
        );
        if (employeeScheduleForDay.isEmpty()) {
            throw new IllegalArgumentException("Cet employé ne travaille pas ce jour-là.");
        }

        // Vérifier que [start .. endTime] rentre dans au moins un slot de l'employé
        boolean withinEmployeeSchedule = employeeScheduleForDay.stream()
                .anyMatch(es -> !startTime.isBefore(es.getStartTime()) && !endTime.isAfter(es.getEndTime()));
        if (!withinEmployeeSchedule) {
            throw new IllegalArgumentException("L'employé ne travaille pas sur ce créneau horaire.");
        }

        // Gérer les créneaux pour plusieurs services
        LocalTime currentStartTime = appointmentDto.getStartTime();
        for (ServiceRequestDTO serviceRequest : appointmentDto.getServices()) {
            com.application.wa9ti.models.Service service = storeServiceRepository.findById(serviceRequest.getServiceId())
                    .orElseThrow(() -> new IllegalArgumentException("Service introuvable"));

            LocalTime serviceEndTime = currentStartTime.plusMinutes(serviceRequest.getDuration());

            // Vérifier disponibilité de l'employé sur ce créneau
            LocalTime finalCurrentStartTime = currentStartTime;
            boolean isEmpAvailable = appointmentRepository.findByStoreIdAndDateBetween(
                    appointmentDto.getStoreId(), appointmentDto.getDate(), appointmentDto.getDate()
            ).stream().noneMatch(app ->
                    app.getEmployee().equals(employee) &&
                            app.getStartTime().isBefore(serviceEndTime) &&
                            app.getEndTime().isAfter(finalCurrentStartTime)
            );
            if (!isEmpAvailable) {
                throw new IllegalArgumentException("L'employé n'est pas disponible pour ce créneau.");
            }

            // Créer le rendez-vous (client null, mais stocke les infos invité)
            Appointment appointment = new Appointment();
            appointment.setDate(appointmentDto.getDate());
            appointment.setStartTime(currentStartTime);
            appointment.setEndTime(serviceEndTime);
            appointment.setPrice(serviceRequest.getPrice());
            appointment.setStore(store);
            appointment.setEmployee(employee);
            appointment.setService(service);
            appointment.setClient(null); // Client non enregistré
            appointment.setClientName(appointmentDto.getClientName());
            appointment.setClientEmail(appointmentDto.getClientEmail());
            appointment.setClientPhone(appointmentDto.getClientPhone());
            appointment.setClientNote(appointmentDto.getClientNote());
            appointment.setEmployeeNote(appointmentDto.getEmployeeNote());
            appointment.setStatus(Appointment.Status.CONFIRMED);

            // Enregistrer l'appointment
            appointmentRepository.save(appointment);


            store.setRdvCount(store.getRdvCount() + 1);
            if(store.getRdvCount() >= SubscriptionConfig.FREE_APPOINTEMENTS && store.getOwner().getSubscription().getType() == Subscription.SubscriptionType.FREE){
                store.getOwner().getSubscription().setStatus(Subscription.SubscriptionStatus.EXPIRED);
            }
            storeRepository.save(store);

            // Passer au service suivant
            currentStartTime = serviceEndTime;
        }
    }




    // ========================
    //   GET CLIENT APPOINT.
    // ========================
    @Override
    public List<AppointmentClientDTO> getUpcomingAppointmentsForClient(Long clientId) {
        List<Appointment> appointments = appointmentRepository.findUpcomingAppointmentsByClient(clientId);
        return appointments.stream()
                .map(AppointmentClientDTO::fromAppointment)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentClientDTO> getPastAppointmentsForClient(Long clientId) {
        List<Appointment> appointments = appointmentRepository.findPastAppointmentsByClient(clientId);
        return appointments.stream()
                .map(AppointmentClientDTO::fromAppointment)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteClientAppointment(Long appointmentId, Long clientId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Rendez-vous introuvable"));

        // Vérifier si le RDV appartient bien au client
        if(clientId != null)
        {
            if (!appointment.getClient().getId().equals(clientId)) {
                throw new IllegalArgumentException("Vous ne pouvez pas supprimer ce rendez-vous.");
            }
        }
        if(appointment.getStatus().equals(Appointment.Status.COMPLETED))
        {
            throw new IllegalArgumentException("Vous ne pouvez pas supprimer ce rendez-vous.");
        }

        // Récupérer les paramètres du magasin
        AppointmentSettings settings = appointmentSettingsRepository.findByStoreId(appointment.getStore().getId())
                .orElseThrow(() -> new IllegalArgumentException("Paramètres de rendez-vous introuvables pour ce magasin."));

        // Vérifier la politique d'annulation
        if (settings.getCancellationPolicy().equals(AppointmentSettings.CancellationPolicy.FORBIDDEN)) {
            throw new IllegalArgumentException("L'annulation de rendez-vous est interdite pour ce magasin.");
        }

        if (settings.getCancellationPolicy().equals(AppointmentSettings.CancellationPolicy.ALLOWED_WITH_NOTICE) &&
                appointment.getDate().atTime(appointment.getStartTime()).minusHours(settings.getCancellationDeadlineHours())
                        .isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Vous ne pouvez plus annuler ce rendez-vous car le délai est dépassé.");
        }

        // Mettre à jour ReservationCount
        ClientStore reservationCount = reservationCountServiceImpl.getOrCreate(appointment.getClient(), appointment.getStore());
        reservationCount.setRdvAnnule(reservationCount.getRdvAnnule() + 1);
        reservationCountServiceImpl.decrementActiveAppointments(reservationCount);


        appointment.getStore().setRdvCount(appointment.getStore().getRdvCount() - 1);
        if(appointment.getStore().getRdvCount() < SubscriptionConfig.FREE_APPOINTEMENTS && appointment.getStore().getOwner().getSubscription().getType() == Subscription.SubscriptionType.FREE){
            appointment.getStore().getOwner().getSubscription().setStatus(Subscription.SubscriptionStatus.ACTIVE);
        }
        storeRepository.save(appointment.getStore());

        // Supprimer le rendez-vous
        appointmentRepository.deleteById(appointmentId);
    }

    @Override
    @Transactional
    public void deleteAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Rendez-vous introuvable"));
        if(appointment.getClient() != null) {
            // Mettre à jour ReservationCount
            ClientStore reservationCount = reservationCountServiceImpl.getOrCreate(appointment.getClient(), appointment.getStore());
            reservationCountServiceImpl.decrementActiveAppointments(reservationCount);
        }

        appointment.getStore().setRdvCount(appointment.getStore().getRdvCount() - 1);
        if(appointment.getStore().getRdvCount() < SubscriptionConfig.FREE_APPOINTEMENTS && appointment.getStore().getOwner().getSubscription().getType() == Subscription.SubscriptionType.FREE){
            appointment.getStore().getOwner().getSubscription().setStatus(Subscription.SubscriptionStatus.ACTIVE);
        }
        storeRepository.save(appointment.getStore());

        // Supprimer le rendez-vous
        appointmentRepository.deleteById(appointmentId);
    }

    @Override
    @Transactional
    public void validateAppointmentStatus(Long appointmentId) {
        // Vérification de l'existence du rendez-vous
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Rendez-vous introuvable"));

        // Vérification si le statut est déjà confirmé (évite des mises à jour inutiles)
        if (appointment.getStatus() == Appointment.Status.CONFIRMED) {
            throw new IllegalStateException("Le rendez-vous est déjà confirmé.");
        }

        // Mise à jour du statut
        appointment.setStatus(Appointment.Status.CONFIRMED);

        if(appointment.getClient() != null)
        {
            ClientStore reservationCount = reservationCountServiceImpl.getOrCreate(
                    appointment.getClient(), appointment.getStore());

            if (reservationCount.isNewClient()) {
                reservationCount.setNewClient(false);
                reservationCountRepository.save(reservationCount);
            }

        }

        // Sauvegarde du rendez-vous
        appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public void honorAppointmentStatus(List<Long> appointmentIds) {
        for (Long appointmentId : appointmentIds) {
            // Vérification de l'existence du rendez-vous
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Rendez-vous introuvable"));

            // Vérification si le statut est déjà confirmé (évite des mises à jour inutiles)
            if (appointment.getStatus() == Appointment.Status.COMPLETED) {
                throw new IllegalStateException("Le rendez-vous est déjà honoré.");
            }

            if(appointment.getClient() != null )
            {
                ClientStore reservationCount = reservationCountServiceImpl.getOrCreate(
                        appointment.getClient(), appointment.getStore());

                // Mise à jour du compteur de réservation
                if (appointment.getStatus() == Appointment.Status.MISSED){
                    reservationCount.setRdvNonRespecte(reservationCount.getRdvNonRespecte()-1);
                    if(reservationCount.isBlackListed())
                    {
                        reservationCount.setBlackListed(false);
                    }
                }else{
                    reservationCount.setNbRdvActif(reservationCount.getNbRdvActif()-1);
                }
                reservationCount.setNbRdvCompleted(reservationCount.getNbRdvCompleted()+1);
                reservationCountRepository.save(reservationCount);
            }

            // Mise à jour du statut
            appointment.setStatus(Appointment.Status.COMPLETED);

            // Sauvegarde du rendez-vous
            appointmentRepository.save(appointment);
        }



    }


    // =========================================================
    //              FONCTIONS UTILITAIRES
    // =========================================================

    /**
     * Vérifie si l'employé est en vacances à la date donnée.
     */
    private boolean isEmployeeOnVacation(Long storeId,Employee employee, LocalDate currentDate) {
        return employee.getEmployeeStores().stream()
                .filter(es -> es.getStore().getId().equals(storeId))
                .flatMap(es -> es.getVacations().stream())
                .anyMatch(vac -> !currentDate.isBefore(vac.getStartDate()) && !currentDate.isAfter(vac.getEndDate()));

    }

    /**
     * Récupère la map des horaires d'ouverture du magasin, par DayOfWeek,
     * en patchant minuit => 23:59 si nécessaire.
     */
    private Map<DayOfWeek, List<Slot>> getStoreOpeningHours(Long storeId) {
        List<OpeningHours> openingHours = openingHoursRepository.findByStoreId(storeId);

        return openingHours.stream()
                .collect(Collectors.toMap(
                        oh -> convertToDayOfWeek(oh.getDay()),
                        oh -> patchMidnightSlotList(oh.getSlots()),
                        (existing, replacement) -> existing
                ));
    }

    /**
     * Récupère la liste des slots de l'employé (EmployeeSchedule),
     * et patch minuit => 23:59 si besoin.
     */
    private List<Slot> getEmployeeDailySchedule(Employee employee, Long storeId, DayOfWeek dayOfWeek) {
        boolean isActive = employee.getEmployeeStores().stream()
                .anyMatch(es -> es.getStore().getId().equals(storeId) && es.isActive());

        if (!isActive) {
            return Collections.emptyList(); // ✅ Empêche les inactifs d'avoir des créneaux
        }

        List<EmployeeSchedule> schedules = employeeScheduleRepository
                .findByEmployeeStore_Employee_IdAndEmployeeStore_Store_Id(employee.getId(), storeId);

        String dayString = dayOfWeekToForm(dayOfWeek);

        return schedules.stream()
                .filter(es -> es.getDay().equalsIgnoreCase(dayString))
                .flatMap(es -> es.getSlots().stream())
                .map(se -> new Slot(null, se.getStartTime(), se.getEndTime()))
                .map(this::patchMidnightSlot)
                .collect(Collectors.toList());
    }

    private String dayOfWeekToForm(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "form.Lundi";
            case TUESDAY -> "form.Mardi";
            case WEDNESDAY -> "form.Mercredi";
            case THURSDAY -> "form.Jeudi";
            case FRIDAY -> "form.Vendredi";
            case SATURDAY -> "form.Samedi";
            case SUNDAY -> "form.Dimanche";
        };
    }
    /**
     * Patch un slot unique :
     *  - Si [XX:XX..00:00] (XX:XX != 00:00), remplacer fin par 23:59.
     *  - Si [00:00..00:00], on peut l'interpréter comme 24h => [00:00..23:59],
     *    ou le laisser tel quel si vous préférez.
     */
    private Slot patchMidnightSlot(Slot originalSlot) {
        LocalTime start = originalSlot.getStartTime();
        LocalTime end   = originalSlot.getEndTime();

        // Cas 1 : [hh:mm..00:00] avec hh:mm != 00:00 => On met 23:59
        if (!start.equals(LocalTime.MIDNIGHT) && end.equals(LocalTime.MIDNIGHT)) {
            return new Slot(null, start, LocalTime.of(23, 59));
        }
        // Cas 2 : [00:00..00:00] => 24h/24
        else if (start.equals(LocalTime.MIDNIGHT) && end.equals(LocalTime.MIDNIGHT)) {
            // Au choix : le considérer comme 24h => [00:00..23:59],
            // ou le garder tel quel si vous voulez un créneau vide.
            return new Slot(null, LocalTime.MIDNIGHT, LocalTime.of(23, 59));
        }
        // Sinon, on ne touche pas
        return originalSlot;
    }

    /**
     * Patch une liste de slots.
     */
    private List<Slot> patchMidnightSlotList(List<Slot> slots) {
        return slots.stream()
                .map(this::patchMidnightSlot)
                .collect(Collectors.toList());
    }

    /**
     * Convertit un "form.Lundi" en DayOfWeek.MONDAY, etc.
     */
    private DayOfWeek convertToDayOfWeek(String day) {
        Map<String, DayOfWeek> dayMapping = Map.of(
                "Lundi", DayOfWeek.MONDAY,
                "Mardi", DayOfWeek.TUESDAY,
                "Mercredi", DayOfWeek.WEDNESDAY,
                "Jeudi", DayOfWeek.THURSDAY,
                "Vendredi", DayOfWeek.FRIDAY,
                "Samedi", DayOfWeek.SATURDAY,
                "Dimanche", DayOfWeek.SUNDAY
        );
        String cleanDay = day.replace("form.", "").trim();
        return dayMapping.getOrDefault(cleanDay, null);
    }

    /**
     * Liste d'employés ayant tous les services demandés.
     */
    private List<Employee> getQualifiedEmployees(Long storeId, List<Long> serviceIds) {
        return employeeRepository.findByStoreId(storeId)
                .stream()
                .filter(emp -> {
                    // Vérifier si l'employé a au moins une assignation active
                    boolean hasActiveAssignment = emp.getEmployeeStores().stream()
                            .anyMatch(es -> es.getStore().getId().equals(storeId) && es.isActive());

                    if (!hasActiveAssignment) {
                        return false; // Ignorer les employés désactivés
                    }

                    // Vérifier s'il possède tous les services demandés
                    Set<Long> empServices = emp.getEmployeeStores().stream()
                            .filter(es -> es.getStore().getId().equals(storeId))
                            .flatMap(es -> es.getServices().stream())
                            .collect(Collectors.toSet());

                    return empServices.containsAll(serviceIds);
                })
                .collect(Collectors.toList());
    }


    /**
     * Vérifie si le magasin est complètement fermé un jour donné
     * (startTime/endTime == null et la date est couverte par [startDate..endDate]).
     */
    private boolean isStoreCompletelyClosed(LocalDate date, List<StoreClosure> closures) {
        return closures.stream()
                .anyMatch(closure ->
                        closure.getStartTime() == null &&
                                closure.getEndTime() == null &&
                                !closure.getStartDate().isAfter(date) &&
                                !closure.getEndDate().isBefore(date)
                );
    }

    /**
     * Retire d'un slot les fermetures partielles du magasin.
     */
    private List<Slot> adjustSlotsForStoreClosures(LocalDate date,
                                                   LocalTime slotStart,
                                                   LocalTime slotEnd,
                                                   List<StoreClosure> closures) {

        List<Slot> adjusted = new ArrayList<>();

        // Filtrer les fermetures qui concernent ce jour
        List<StoreClosure> relevantClosures = closures.stream()
                .filter(closure ->
                        !closure.getStartDate().isAfter(date) &&
                                !closure.getEndDate().isBefore(date) &&
                                closure.getStartTime() != null &&
                                closure.getEndTime() != null
                )
                .sorted(Comparator.comparing(StoreClosure::getStartTime))
                .toList();

        LocalTime currentStart = slotStart;

        for (StoreClosure closure : relevantClosures) {
            if (closure.getStartTime().isAfter(slotEnd) || closure.getEndTime().isBefore(slotStart)) {
                // fermeture hors du créneau, on ignore
                continue;
            }
            // partie avant la fermeture
            if (currentStart.isBefore(closure.getStartTime())) {
                adjusted.add(new Slot(null, currentStart, closure.getStartTime()));
            }
            // on avance après la fermeture
            currentStart = closure.getEndTime();
        }

        // après la dernière fermeture
        if (currentStart.isBefore(slotEnd)) {
            adjusted.add(new Slot(null, currentStart, slotEnd));
        }

        return adjusted;
    }

    /**
     * Retire d'un slot les RDVs existants de l'employé.
     */
    private List<Slot> adjustSlotsForAppointments(LocalDate date,
                                                  LocalTime slotStart,
                                                  LocalTime slotEnd,
                                                  List<Appointment> appointments,
                                                  Employee employee) {

        List<Slot> freeSlots = new ArrayList<>();

        // Rendez-vous de cet employé pour CE jour
        List<Appointment> dailyAppointments = appointments.stream()
                .filter(app -> app.getEmployee().getId().equals(employee.getId()))
                .filter(app -> app.getDate().equals(date))
                .sorted(Comparator.comparing(Appointment::getStartTime))
                .toList();

        LocalTime currentStart = slotStart;

        for (Appointment appointment : dailyAppointments) {
            if (appointment.getStartTime().isAfter(slotEnd) || appointment.getEndTime().isBefore(slotStart)) {
                continue; // RDV hors du créneau
            }
            // avant le RDV
            if (currentStart.isBefore(appointment.getStartTime())) {
                freeSlots.add(new Slot(null, currentStart, appointment.getStartTime()));
            }
            // avancer le curseur
            currentStart = appointment.getEndTime();
        }

        // après le dernier RDV
        if (currentStart.isBefore(slotEnd)) {
            freeSlots.add(new Slot(null, currentStart, slotEnd));
        }

        return freeSlots;
    }

    /**
     * Intersection de deux listes de slots (paires [start, end]).
     */
    private List<Slot> intersectSlots(List<Slot> slots1, List<Slot> slots2) {
        List<Slot> result = new ArrayList<>();
        for (Slot s1 : slots1) {
            for (Slot s2 : slots2) {
                LocalTime start = max(s1.getStartTime(), s2.getStartTime());
                LocalTime end   = min(s1.getEndTime(), s2.getEndTime());
                if (start.isBefore(end)) {
                    result.add(new Slot(null, start, end));
                }
            }
        }
        return result;
    }

    /**
     * Trouve si un slot équivalent [start, end] existe déjà dans la collection.
     */
    private Slot findExactSlot(Collection<Slot> slotCollection, Slot candidate) {
        for (Slot slot : slotCollection) {
            if (slot.getStartTime().equals(candidate.getStartTime()) &&
                    slot.getEndTime().equals(candidate.getEndTime())) {
                return slot;
            }
        }
        return null;
    }

    /**
     * Trouve l'employé qualifié avec le moins de RDVs sur la plage demandée.
     */
    private Employee findLeastBusyEmployee(Long storeId,
                                           LocalDate date,
                                           LocalTime startTime,
                                           LocalTime endTime,
                                           List<Long> serviceIds) {

        // Récupérer tous les employés du magasin
        List<Employee> employees = employeeRepository.findByStoreId(storeId);

        // Filtrer ceux qui offrent tous les services demandés
        List<Employee> qualified = employees.stream()
                .filter(emp -> {
                    Set<Long> empServices = emp.getEmployeeStores().stream()
                            .filter(es -> es.getStore().getId().equals(storeId))
                            .flatMap(es -> es.getServices().stream())
                            .collect(Collectors.toSet());
                    return empServices.containsAll(serviceIds);
                })
                .collect(Collectors.toList());

        if (qualified.isEmpty()) {
            throw new IllegalArgumentException("Aucun employé qualifié disponible pour ces services.");
        }

        // Éliminer ceux qui sont en vacances le jour concerné
        qualified = qualified.stream()
                .filter(emp -> !isEmployeeOnVacation(storeId,emp, date))
                .collect(Collectors.toList());

        if (qualified.isEmpty()) {
            throw new IllegalArgumentException("Tous les employés qualifiés sont en vacances ce jour-là.");
        }

        // **NOUVEAU : Filtrer les employés selon leurs horaires de travail**
        qualified = qualified.stream()
                .filter(emp -> {
                    // Récupérer l'horaire de travail de l'employé pour le jour concerné
                    List<Slot> dailySchedule = getEmployeeDailySchedule(emp, storeId, date.getDayOfWeek());
                    if (dailySchedule.isEmpty()) {
                        return false;
                    }
                    // Vérifier qu'au moins un slot couvre entièrement le créneau demandé
                    return dailySchedule.stream()
                            .anyMatch(slot -> !startTime.isBefore(slot.getStartTime()) && !endTime.isAfter(slot.getEndTime()));
                })
                .collect(Collectors.toList());

        if (qualified.isEmpty()) {
            throw new IllegalArgumentException("Aucun employé qualifié disponible sur ce créneau horaire.");
        }

        // Sélectionner l'employé ayant le moins de rendez-vous sur le créneau demandé
        return qualified.stream()
                .min(Comparator.comparingInt(emp -> getEmployeeAppointmentCount(emp, date, startTime, endTime)))
                .orElseThrow(() -> new IllegalArgumentException("Aucun employé qualifié disponible sur ce créneau."));
    }


    /**
     * Nombre de RDVs d'un employé donné sur [startTime..endTime].
     */
    private int getEmployeeAppointmentCount(Employee employee,
                                            LocalDate date,
                                            LocalTime startTime,
                                            LocalTime endTime) {
        return (int) appointmentRepository.findByEmployeeIdAndDate(employee.getId(), date)
                .stream()
                .filter(app -> app.getStartTime().isBefore(endTime) &&
                        app.getEndTime().isAfter(startTime))
                .count();
    }

    /**
     * Calcule la durée totale (en minutes) de la liste de services.
     */
    private int getTotalDuration(List<ServiceRequestDTO> services) {
        return services.stream().mapToInt(ServiceRequestDTO::getDuration).sum();
    }

    // ===============================================
    //               Fonctions min/max LocalTime
    // ===============================================
    private LocalTime max(LocalTime t1, LocalTime t2) {
        return t1.isAfter(t2) ? t1 : t2;
    }

    private LocalTime min(LocalTime t1, LocalTime t2) {
        return t1.isBefore(t2) ? t1 : t2;
    }

    @Override
    public Page<AppointmentValidationDTO> getSortedAppointments(Long storeId,String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Appointment> appointments = appointmentRepository.findAppointmentsSorted(storeId,keyword, pageable);

        return appointments.map(a -> {
            ClientStore reservationCount = (a.getClient() != null)
                    ? reservationCountServiceImpl.getOrCreate(a.getClient(), a.getStore())
                    : new ClientStore(null, null, a.getStore(), 0, 0,0, 0, 0, false, true,"");


            return new AppointmentValidationDTO(
                    a.getId(),
                    (a.getClient() != null) ? a.getClient().getId() : null,
                    a.getDate(),
                    a.getStartTime(),
                    a.getEndTime(),
                    (a.getClientName() != null) ? a.getClientName() : (a.getClient() != null ? a.getClient().getUser().getName() : "Inconnu"),
                    (a.getClientPhone() != null) ? a.getClientPhone() : (a.getClient() != null ? a.getClient().getUser().getPhone() : "Non disponible"),
                    (a.getClientEmail() != null) ? a.getClientEmail() : (a.getClient() != null ? a.getClient().getUser().getEmail() : "Non disponible"),
                    (a.getClient() != null) ? a.getClient().getImage() : "default_client.png",
                    a.getClientNote(),
                    reservationCount.getNbRdvTotal(),
                    reservationCount.getRdvAnnule(),
                    reservationCount.getNbRdvActif(),
                    reservationCount.getRdvNonRespecte(),
                    a.getService().getName(),
                    a.getService().getPrice(),
                    a.getService().getTime(),
                    a.getStore().getName(),
                    a.getEmployee() != null ? a.getEmployee().getUser().getName() : "Non assigné",
                    a.getEmployee() != null ? a.getEmployee().getImage() : "default_employee.jpg",
                    a.getEmployeeNote(),
                    a.getStatus()
            );
        });
    }

    @Override
    public Page<AppointmentValidationDTO> getSortedArchivedAppointments(Long storeId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Appointment> appointments = appointmentRepository.findArchivedAppointments(storeId,keyword, pageable);

        return appointments.map(a -> {
            ClientStore reservationCount = (a.getClient() != null)
                    ? reservationCountServiceImpl.getOrCreate(a.getClient(), a.getStore())
                    : new ClientStore(null, null, a.getStore(), 0, 0,0, 0, 0, false, true,"");


            return new AppointmentValidationDTO(
                    a.getId(),
                    (a.getClient() != null) ? a.getClient().getId() : null,
                    a.getDate(),
                    a.getStartTime(),
                    a.getEndTime(),
                    (a.getClientName() != null) ? a.getClientName() : (a.getClient() != null ? a.getClient().getUser().getName() : "Inconnu"),
                    (a.getClientPhone() != null) ? a.getClientPhone() : (a.getClient() != null ? a.getClient().getUser().getPhone() : "Non disponible"),
                    (a.getClientEmail() != null) ? a.getClientEmail() : (a.getClient() != null ? a.getClient().getUser().getEmail() : "Non disponible"),
                    (a.getClient() != null) ? a.getClient().getImage() : "default_client.png",
                    a.getClientNote(),
                    reservationCount.getNbRdvTotal(),
                    reservationCount.getRdvAnnule(),
                    reservationCount.getNbRdvActif(),
                    reservationCount.getRdvNonRespecte(),
                    a.getService().getName(),
                    a.getService().getPrice(),
                    a.getService().getTime(),
                    a.getStore().getName(),
                    a.getEmployee() != null ? a.getEmployee().getUser().getName() : "Non assigné",
                    a.getEmployee() != null ? a.getEmployee().getImage() : "default_employee.jpg",
                    a.getEmployeeNote(),
                    a.getStatus()
            );
        });
    }


    @Override
    public List<AppointmentValidationDTO> getTodayAppointmentsForClient(Long storeId, String keyword) {
        List<Appointment> appointments = appointmentRepository.findTodayAppointmentsToHonor(storeId, keyword);

        return appointments.stream().map(a -> {
            ClientStore reservationCount = (a.getClient() != null)
                    ? reservationCountServiceImpl.getOrCreate(a.getClient(), a.getStore())
                    : new ClientStore(null, null, a.getStore(), 0, 0,0, 0, 0, false, true,"");

            return new AppointmentValidationDTO(
                    a.getId(),
                    (a.getClient() != null) ? a.getClient().getId() : null,
                    a.getDate(),
                    a.getStartTime(),
                    a.getEndTime(),
                    (a.getClientName() != null) ? a.getClientName() : (a.getClient() != null ? a.getClient().getUser().getName() : "Inconnu"),
                    (a.getClientPhone() != null) ? a.getClientPhone() : (a.getClient() != null ? a.getClient().getUser().getPhone() : "Non disponible"),
                    (a.getClientEmail() != null) ? a.getClientEmail() : (a.getClient() != null ? a.getClient().getUser().getEmail() : "Non disponible"),
                    (a.getClient() != null) ? a.getClient().getImage() : "default_client.png",
                    a.getClientNote(),
                    reservationCount.getNbRdvTotal(),
                    reservationCount.getRdvAnnule(),
                    reservationCount.getNbRdvActif(),
                    reservationCount.getRdvNonRespecte(),
                    a.getService().getName(),
                    a.getService().getPrice(),
                    a.getService().getTime(),
                    a.getStore().getName(),
                    a.getEmployee() != null ? a.getEmployee().getUser().getName() : "Non assigné",
                    a.getEmployee() != null ? a.getEmployee().getImage() : "default_employee.jpg",
                    a.getEmployeeNote(),
                    a.getStatus()
            );
        }).collect(Collectors.toList());
    }



    @Override
    public boolean canModifyAppointment(Long appointmentId, Long storeId) {
        // Récupérer les paramètres du store
        AppointmentSettings settings = appointmentSettingsRepository.findByStoreId(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Aucun paramètre trouvé pour le store d'id " + storeId));

        // Cas 1️⃣ : Modification toujours autorisée
        if (settings.getModificationPolicy() == AppointmentSettings.ModificationPolicy.FREE_MODIFICATION) {
            return true;
        }

        // Cas 2️⃣ : Modification interdite
        if (settings.getModificationPolicy() == AppointmentSettings.ModificationPolicy.FORBIDDEN) {
            return false;
        }

        // Cas 3️⃣ : ALLOWED_WITH_NOTICE → Vérifier le délai avant le RDV
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Rendez-vous non trouvé pour l'id " + appointmentId));

        // Reconstruction de la date et heure complète du rendez-vous
        LocalDateTime appointmentDateTime = LocalDateTime.of(appointment.getDate(), appointment.getStartTime());

        // Vérification du délai de modification
        if (settings.getModificationDeadlineHours() != null) {
            LocalDateTime modificationLimit = appointmentDateTime.minusHours(settings.getModificationDeadlineHours());
            return LocalDateTime.now().isBefore(modificationLimit);
        }
        return true;
    }

    @Override
    @Transactional
    public AppointmentClientDTO updateAppointmentDateTime(Long appointmentId, UpdateAppointmentDateTimeDTO dto) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Rendez-vous non trouvé avec l'ID : " + appointmentId));

        appointment.setDate(dto.date());
        appointment.setStartTime(dto.startTime());
        appointment.setEndTime(dto.endTime());

        ;
        return AppointmentClientDTO.fromAppointment(appointmentRepository.save(appointment));
    }
}
