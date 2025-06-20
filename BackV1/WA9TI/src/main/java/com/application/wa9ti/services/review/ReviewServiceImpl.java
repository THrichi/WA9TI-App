package com.application.wa9ti.services.review;

import com.application.wa9ti.dtos.ReviewDTO;
import com.application.wa9ti.dtos.ReviewResponseDto;
import com.application.wa9ti.dtos.StoreReviewStatsDTO;
import com.application.wa9ti.models.Appointment;
import com.application.wa9ti.models.Client;
import com.application.wa9ti.models.Review;
import com.application.wa9ti.models.Store;
import com.application.wa9ti.repositories.AppointmentRepository;
import com.application.wa9ti.repositories.ReviewRepository;
import com.application.wa9ti.services.client.ClientServiceImp;
import com.application.wa9ti.services.store.StoreServiceImp;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService{


    private final ReviewRepository reviewRepository;
    private final ClientServiceImp clientServiceImp;
    private final StoreServiceImp storeServiceImpl;
    private final AppointmentRepository appointmentRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository, ClientServiceImp clientServiceImp, StoreServiceImp storeServiceImpl, AppointmentRepository appointmentRepository) {
        this.reviewRepository = reviewRepository;
        this.clientServiceImp = clientServiceImp;
        this.storeServiceImpl = storeServiceImpl;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public ReviewResponseDto saveOrUpdateReview(Long clientId,ReviewDTO reviewDto) {
        // Vérifie si le client existe via le ClientService
        Client client = clientServiceImp.getClientById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Le client avec l'ID spécifié n'existe pas."));

        // Vérifie si le store existe via le StoreService
        Store store = storeServiceImpl.getStoreById(reviewDto.storeId())
                .orElseThrow(() -> new IllegalArgumentException("Le magasin avec l'ID spécifié n'existe pas."));

        // Vérifie si le client a au moins un rendez-vous complété avec ce store
        boolean hasCompletedAppointment = appointmentRepository.existsByClientIdAndStoreIdAndStatus(client.getId(), store.getId(), Appointment.Status.COMPLETED);
        if (!hasCompletedAppointment) {
            throw new IllegalArgumentException("Vous ne pouvez laisser un avis que si vous avez un rendez-vous complété avec ce magasin.");
        }

        // Vérifie si un avis existe déjà
        Optional<Review> existingReview = reviewRepository.findByClientAndStore(client, store);
        Review review;

        if (existingReview.isPresent()) {
            // Mettre à jour l'avis existant
            review = existingReview.get();
            review.setCleanliness(reviewDto.cleanliness());
            review.setHospitality(reviewDto.hospitality());
            review.setServiceQuality(reviewDto.serviceQuality());
            review.setValueForMoney(reviewDto.valueForMoney());
            review.setExperience(reviewDto.experience());
            review.setComment(reviewDto.comment());
        } else {
            // Créer un nouvel avis
            review = new Review();
            review.setClient(client);
            review.setStore(store);
            review.setCleanliness(reviewDto.cleanliness());
            review.setHospitality(reviewDto.hospitality());
            review.setServiceQuality(reviewDto.serviceQuality());
            review.setValueForMoney(reviewDto.valueForMoney());
            review.setExperience(reviewDto.experience());
            review.setComment(reviewDto.comment());
        }

        // Enregistre (insert ou update)
        Review savedReview = reviewRepository.save(review);

        // Retourne l'avis sous forme de DTO
        return mapToReviewResponseDto(savedReview);
    }

    @Override
    public Optional<Review> getReviewByClientAndStore(Client client, Store store) {
        return reviewRepository.findByClientAndStore(client, store);
    }

    @Override
    public Page<ReviewResponseDto> getReviewsByStore(Long storeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewRepository.findByStoreId(storeId, pageable);

        return reviews.map(this::mapToReviewResponseDto);
    }

    @Override
    public Page<ReviewResponseDto> getFilteredReviews(Long storeId, String keyword, Float minRating, Float maxRating, Boolean onlyNoResponse, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Normalisation du keyword (null -> vide pour éviter les problèmes SQL)
        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        // Si minRating ou maxRating sont null, on les remplace par des valeurs par défaut
        Float min = (minRating != null) ? minRating : 0.0f;
        Float max = (maxRating != null) ? maxRating : 5.0f;

        Page<Review> reviews = reviewRepository.findFilteredReviews(storeId, searchKeyword, min, max, onlyNoResponse, pageable);

        return reviews.map(this::mapToReviewResponseDto);
    }

    @Override
    @Transactional
    public ReviewResponseDto saveStoreResponse(Long storeId, Long reviewId, String response) {
        storeServiceImpl.getStoreById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Le magasin avec l'ID spécifié n'existe pas."));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("L'avis avec l'ID spécifié n'existe pas."));

        // Vérification que l'avis appartient bien au magasin
        if (!review.getStore().getId().equals(storeId)) {
            throw new IllegalArgumentException("Cet avis n'appartient pas au magasin spécifié.");
        }

        review.setStoreResponse(response);
        reviewRepository.save(review);

        return mapToReviewResponseDto(review);
    }



    @Override
    public List<ReviewResponseDto> getReviewsByClient(Client client) {
        return reviewRepository.findByClient(client)
                .stream()
                .map(this::mapToReviewResponseDto)
                .toList();
    }


    @Override
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }


    private ReviewResponseDto mapToReviewResponseDto(Review review) {
        return new ReviewResponseDto(
                review.getId(),
                review.getClient().getId(),
                review.getClient().getUser().getName(),
                review.getClient().getImage(),
                review.getStore().getId(),
                review.getCleanliness(),
                review.getHospitality(),
                review.getServiceQuality(),
                review.getValueForMoney(),
                review.getExperience(),
                review.getRating(),
                review.getComment(),
                review.getStoreResponse(),
                review.getCreatedAt()
        );
    }

    @Override
    public StoreReviewStatsDTO getReviewStats(Long storeId) {
        return reviewRepository.getStoreReviewStats(storeId);
    }

}
