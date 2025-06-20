package com.application.wa9ti.services.store;

import com.application.wa9ti.dtos.*;
import com.application.wa9ti.models.*;
import com.application.wa9ti.repositories.*;
import com.application.wa9ti.services.auth.AuthorizationService;
import com.application.wa9ti.services.subscription.SubscriptionService;
import com.application.wa9ti.services.subscription.SubscriptionServiceImp;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StoreServiceImp implements StoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreClosureRepository storeClosureRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ReservationCountRepository reservationCountRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private SocialNetworkRepository socialNetworkRepository;
    @Autowired
    private OpeningHoursRepository openingHoursRepository;
    @Autowired
    private OwnerRepository ownerRepository;
    @Autowired
    private AuthorizationService authorizationService;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private EmployeeStoreRepository employeeStoreRepository;
    @Autowired
    private AppointmentSettingsRepository appointmentSettingsRepository;
    @Autowired
    private SubscriptionService subscriptionService;

    @Override
    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    @Override
    public Optional<Store> getStoreById(Long id) {
        return storeRepository.findById(id);
    }


    @Override
    public StoreClientsDTO findByStoreUrl(String storeUrl) {
        Store store =  storeRepository.findByStoreUrl(storeUrl).orElseThrow(() -> new EntityNotFoundException("Store not found"));
        Subscription subscription = store.getOwner().getSubscription();
        if (subscription != null &&
                (subscription.getStatus() == Subscription.SubscriptionStatus.EXPIRED
                        || subscription.getStatus() == Subscription.SubscriptionStatus.CANCELED)) {
            throw new IllegalStateException("Store subscription is expired or canceled");
        }
        AppointmentSettings settings = appointmentSettingsRepository.findByStoreId(store.getId()).orElseThrow(() -> new EntityNotFoundException("Appointment settings not found"));
        return StoreClientsDTO.fromEntity(store,StoreAppointmentSettingsDTO.fromEntity(settings));
    }

    @Override
    public Page<ClientStoreDTO> getClientsByStore(Long storeId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ClientStore> reservationCounts = reservationCountRepository.findByStoreIdWithKeyword(storeId, keyword, pageable);

        return reservationCounts.map(rc -> new ClientStoreDTO(
                rc.getClient().getId(),
                rc.getClient().getUser().getName(),
                rc.getClient().getUser().getEmail(),
                rc.getClient().getUser().getPhone(),
                rc.getClient().getImage(),
                rc.getNbRdvTotal(),
                rc.getNbRdvCompleted(),
                rc.getNbRdvActif(),
                rc.getRdvAnnule(),
                rc.getRdvNonRespecte(),
                rc.isBlackListed(),
                rc.isNewClient(),
                rc.getNote()
        ));
    }


    @Override
    public Store updateStore(Long id, Store updatedStore) {
        Store existingStore = getStoreById(id)
                .orElseThrow(() -> new RuntimeException("Store not found with id: " + id));
        existingStore.setName(updatedStore.getName());
        existingStore.setType(updatedStore.getType());
        existingStore.setEmail(updatedStore.getEmail());
        existingStore.setPhone(updatedStore.getPhone());
        existingStore.setAddress(updatedStore.getAddress());
        existingStore.setImage(updatedStore.getImage());
        existingStore.setGallery(updatedStore.getGallery());
        existingStore.setServices(updatedStore.getServices());
        existingStore.setOpeningHours(updatedStore.getOpeningHours());
        return storeRepository.save(existingStore);
    }

    @Transactional
    @Override
    public void updateStoreImage(Long id, String imageURL) {
        storeRepository.updateImageUrlById(id,imageURL);
    }

    @Override
    @Transactional
    public void deleteStore(Long storeId) {
        Optional<Store> storeOpt = storeRepository.findById(storeId);

        Owner owner = ownerRepository.findByUser_Email(authorizationService.getAuthenticatedUserEmail()).orElseThrow(
                () -> new EntityNotFoundException("Owner not found with email: " + authorizationService.getAuthenticatedUserEmail())
        );


        if (storeOpt.isEmpty()) {
            throw new IllegalArgumentException("Le magasin n'existe pas.");
        }

        Store store = storeOpt.get();


        if(!Objects.equals(owner.getId(), store.getOwner().getId()))
        {
            throw new IllegalArgumentException("Vous n'avez pas le droit pour supprimer ce magasin.");
        }

        // Vérifie s'il y a des rendez-vous CONFIRMED ou PENDING
        List<Appointment> activeAppointments = appointmentRepository.findByStoreIdAndStatusIn(
                storeId, List.of(Appointment.Status.CONFIRMED, Appointment.Status.PENDING));

        if (!activeAppointments.isEmpty()) {
            throw new IllegalArgumentException("Impossible de supprimer ce magasin. Il y a des rendez-vous en cours.");
        }

        // Vérifie s'il y a des employés actifs
        List<EmployeeStore> activeEmployees = employeeStoreRepository.findByStoreId(storeId);

        if (!activeEmployees.isEmpty()) {
            throw new IllegalArgumentException("Impossible de supprimer ce magasin. Il y a encore des employés.");
        }

        storeRepository.delete(store);
    }

    @Override
    public Store updateOpeningHours(Long storeId, List<OpeningHoursDTO> openingHoursDTOs) {
        // Récupérer le Store
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Store not found with id: " + storeId));

        // Convertir les DTO en entités OpeningHours
        List<OpeningHours> openingHours = openingHoursDTOs.stream().map(dto -> {
            OpeningHours openingHour = new OpeningHours();
            openingHour.setDay(dto.day());
            openingHour.setSlots(dto.slots().stream().map(slotDto -> {
                Slot slot = new Slot();
                slot.setStartTime(slotDto.startTime());
                slot.setEndTime(slotDto.endTime());
                return slot;
            }).toList());
            return openingHour;
        }).toList();

        // Mettre à jour les heures d'ouverture
        store.getOpeningHours().clear();
        store.getOpeningHours().addAll(openingHours);

        // Sauvegarder le store avec les nouvelles heures
        return storeRepository.save(store);
    }

    @Override
    public Store updateStoreInfo(Long storeId, StoreInfosDto storeInfosDto) {
        // Récupérer le Store par son ID
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Store not found with id: " + storeId));

        // Mettre à jour les informations de base
        store.setName(storeInfosDto.getStoreName());
        store.setType(storeInfosDto.getStoreType());
        store.setAddress(storeInfosDto.getStoreAddress());
        store.setLatitude(storeInfosDto.getStoreLatitude());
        store.setLongitude(storeInfosDto.getStoreLongitude());
        store.setEmail(storeInfosDto.getStoreEmail());
        store.setPhone(storeInfosDto.getStorePhone());
        store.setDescription(storeInfosDto.getStoreDescription());
        store.setSeoKeywords(storeInfosDto.getStoreSeo());

        if (storeInfosDto.getSocialNetworks() != null) {
            // Supprimer les anciens réseaux sociaux
            socialNetworkRepository.deleteAllByStoreId(storeId);

            // Ajouter les nouveaux réseaux sociaux
            List<SocialNetwork> updatedSocialNetworks = storeInfosDto.getSocialNetworks().stream()
                    .map(dto -> new SocialNetwork(null, dto.platform(), dto.url(), dto.icon(), store))
                    .toList();

            if (store.getSocialNetworks() == null) {
                store.setSocialNetworks(new ArrayList<>(updatedSocialNetworks));
            } else {
                store.getSocialNetworks().clear();
                store.getSocialNetworks().addAll(updatedSocialNetworks);
            }
        }

        // Sauvegarder les changements
        return storeRepository.save(store);
    }


    @Override
    public void addImageToGallery(Long storeId, List<String> newImageUrls) {
        // Récupérer le magasin
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with id: " + storeId));

        // Ajouter les nouvelles images à la galerie existante
        List<String> gallery = store.getGallery();
        gallery.addAll(newImageUrls); // Ajoute toutes les URLs à la galerie
        store.setGallery(gallery);

        // Sauvegarder les modifications
        storeRepository.save(store);
    }

    @Override
    @Transactional
    public void removeImageFromGallery(Long storeId, String imageUrl) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with id: " + storeId));

        List<String> gallery = store.getGallery();
        gallery.remove(imageUrl);
        store.setGallery(gallery);

        storeRepository.save(store);
    }


    @Override
    public StoreClosure addClosureToStore(Long storeId, StoreClosureDTO closureDto) {
        // Vérification que le magasin existe
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with id " + storeId));

        // Vérifier les chevauchements
        boolean isOverlapping = store.getClosures().stream().anyMatch(existingClosure -> {
            // Vérifie si les dates se chevauchent
            boolean dateOverlap = !(closureDto.endDate().isBefore(existingClosure.getStartDate()) ||
                    closureDto.startDate().isAfter(existingClosure.getEndDate()));

            // Vérifie si les heures se chevauchent, si elles sont définies
            boolean timeOverlap = true;
            if (closureDto.startTime() != null && closureDto.endTime() != null &&
                    existingClosure.getStartTime() != null && existingClosure.getEndTime() != null) {
                timeOverlap = !(closureDto.endTime().isBefore(existingClosure.getStartTime()) ||
                        closureDto.startTime().isAfter(existingClosure.getEndTime()));
            }

            return dateOverlap && timeOverlap;
        });

        if (isOverlapping) {
            throw new RuntimeException("La fermeture se chevauche avec une fermeture existante.");
        }

        // Créer une entité StoreClosure à partir du DTO
        StoreClosure closure = new StoreClosure();
        closure.setStore(store); // Associer le magasin
        closure.setStartDate(closureDto.startDate());
        closure.setEndDate(closureDto.endDate());
        closure.setStartTime(closureDto.startTime());
        closure.setEndTime(closureDto.endTime());

        // Sauvegarder la fermeture dans la base de données
        return storeClosureRepository.save(closure);
    }


    @Override
    public List<StoreClosure> getClosuresStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with id: " + storeId));

        // Charger les fermetures sans inclure les détails du Store
        return store.getClosures().stream()
                .peek(closure -> {
                    closure.setStore(null); // Supprimer la référence au Store
                }).toList();
    }

    @Override
    @Transactional
    public void deleteClosureById(Long closureId) {
        // Vérifier si le closure existe
        StoreClosure closure = storeClosureRepository.findById(closureId)
                .orElseThrow(() -> new RuntimeException("Closure not found with id: " + closureId));

        // Supprimer le closure
        storeClosureRepository.delete(closure);
    }

    @Override
    public List<StoreSearchDTO> findStoresNearby(Double latitude, Double longitude, Double radius, String keyword) {

        List<Store> stores = storeRepository.findStoresNearby(latitude, longitude, radius,keyword);
        return stores.stream().map(this::convertToDTO).toList();
    }

    @Override
    public boolean isStoreUrlTaken(String storeUrl) {
        return storeRepository.existsByStoreUrl(storeUrl);
    }

    private StoreSearchDTO convertToDTO(Store store) {
        double rating = calculateAverageRating(store);
        long reviewCount = countReviews(store);// Calculer le rating moyen
        return new StoreSearchDTO(
                store.getId(),
                store.getName(),
                store.getStoreUrl(),
                store.getType(),
                store.getEmail(),
                store.getPhone(),
                store.getAddress(),
                store.getLatitude(),
                store.getLongitude(),
                store.getImage(),
                rating,
                reviewCount
        );
    }


    // Calculer la moyenne des ratings
    private double calculateAverageRating(Store store) {
        List<Review> reviews = reviewRepository.findByStoreOrderByCreatedAtDesc(store);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
    }

    // Obtenir le nombre total d'avis
    private long countReviews(Store store) {
        return reviewRepository.countByStore(store);
    }


    /**
     * Bloque ou débloque un client en fonction de son état actuel.
     * @param clientId ID du client
     * @param storeId ID du store
     */
    @Transactional
    public void toggleBlacklist(Long storeId,Long clientId) {
        // Vérifier si le client existe
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client non trouvé"));

        // Récupérer le dernier ReservationCount pour ce client
        ClientStore clientStore = reservationCountRepository.findByClientIdAndStoreId(clientId,storeId)
                .orElseThrow(() -> new IllegalArgumentException("Aucune réservation trouvée pour ce client"));

        // Inverser le statut blacklisté
        clientStore.setBlackListed(!clientStore.isBlackListed());
        reservationCountRepository.save(clientStore);
    }

    @Override
    @Transactional
    public void createStore(StoreCreateDTO storeDTO) {
        // 🔥 Vérifier si l'Owner existe
        Owner owner = ownerRepository.findById(storeDTO.ownerId())
                .orElseThrow(() -> new RuntimeException("Propriétaire non trouvé avec l'ID : " + storeDTO.ownerId()));

        if(!subscriptionService.canAddStore(owner.getId())) {
            throw new IllegalArgumentException("can't add store");
        }

        // 🔥 Créer l'entité `Store`
        Store store = new Store();
        store.setName(storeDTO.name());
        store.setStoreUrl(storeDTO.storeUrl());
        store.setType(storeDTO.type());
        store.setEmail(storeDTO.email());
        store.setPhone(storeDTO.phone());
        store.setAddress(storeDTO.address());
        store.setLatitude(storeDTO.latitude());
        store.setLongitude(storeDTO.longitude());
        store.setImage("default_store.png");
        store.setOwner(owner); // 🔥 Associer le propriétaire


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

        // 🔥 Sauvegarde du magasin (sans les horaires d'ouverture)
        store = storeRepository.save(store);

        // 🔥 Sauvegarder les horaires d'ouverture
        Store finalStore = store;
        List<OpeningHours> openingHours = storeDTO.openingHours().stream()
                .map(dto -> convertToOpeningHours(dto, finalStore))
                .collect(Collectors.toList());

        openingHoursRepository.saveAll(openingHours);
    }

    private OpeningHours convertToOpeningHours(OpeningHoursDTO dto, Store store) {
        OpeningHours openingHours = new OpeningHours();
        openingHours.setDay(dto.day());
        openingHours.setStore(store);

        // Convertir et sauvegarder les slots
        List<Slot> slots = dto.slots().stream()
                .map(this::convertToSlot)
                .collect(Collectors.toList());

        openingHours.setSlots(slots);
        return openingHours;
    }

    private Slot convertToSlot(SlotDTO dto) {
        Slot slot = new Slot();
        slot.setStartTime(dto.startTime());
        slot.setEndTime(dto.endTime());
        return slot;
    }


    @Override
    public List<StoreSimpleDTO> getOwnerStores(Long ownerId) {
        return storeRepository.findByOwnerId(ownerId)
                .stream()
                .map(StoreSimpleDTO::fromEntity)
                .toList();
    }


}
