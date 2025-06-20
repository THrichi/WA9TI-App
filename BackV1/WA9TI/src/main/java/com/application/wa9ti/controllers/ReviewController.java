package com.application.wa9ti.controllers;

import com.application.wa9ti.dtos.ReviewDTO;
import com.application.wa9ti.dtos.ReviewResponseDto;
import com.application.wa9ti.dtos.StoreReviewStatsDTO;
import com.application.wa9ti.services.auth.AuthorizationService;
import com.application.wa9ti.services.client.ClientService;
import com.application.wa9ti.services.client.ClientServiceImp;
import com.application.wa9ti.services.review.ReviewService;
import com.application.wa9ti.services.store.StoreService;
import com.application.wa9ti.services.store.StoreServiceImp;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final StoreService storeServiceImpl;
    private final ClientService clientServiceImp;
    private final AuthorizationService authorizationService;

    public ReviewController(ReviewService reviewService, StoreServiceImp storeServiceImpl, ClientServiceImp clientServiceImp, AuthorizationService authorizationService) {
        this.reviewService = reviewService;
        this.storeServiceImpl = storeServiceImpl;
        this.clientServiceImp = clientServiceImp;
        this.authorizationService = authorizationService;
    }

    // Endpoint pour ajouter un nouvel avis
    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(@Valid @RequestBody ReviewDTO reviewDto) {
        ReviewResponseDto savedReview = reviewService.saveOrUpdateReview(authorizationService.getAuthenticatedClient().getId(),reviewDto);
        return ResponseEntity.ok(savedReview);
    }

    @PostMapping("/{storeId}/store-response/{reviewId}")
    public ResponseEntity<ReviewResponseDto> setStoreResponse(
            @PathVariable Long storeId,
            @PathVariable Long reviewId,
            @RequestBody(required = false) String response) {

        authorizationService.canAccessStore(storeId);

        // Si la réponse est nulle ou vide, on considère que l'utilisateur veut la supprimer
        if (response == null || response.trim().isEmpty()) {
            response = ""; // Assurer une valeur par défaut
        }

        ReviewResponseDto savedReview = reviewService.saveStoreResponse(storeId, reviewId, response);

        return ResponseEntity.ok(savedReview);
    }



    // Endpoint pour récupérer les avis d'un magasin
    @GetMapping("/store-reviews")
    public ResponseEntity<PagedModel<EntityModel<ReviewResponseDto>>> getReviewsByStore(
            @RequestParam Long storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            PagedResourcesAssembler<ReviewResponseDto> assembler) {

        storeServiceImpl.getStoreById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with ID: " + storeId));

        Page<ReviewResponseDto> reviewsPage = reviewService.getReviewsByStore(storeId, page, size);
        PagedModel<EntityModel<ReviewResponseDto>> pagedModel = assembler.toModel(reviewsPage);

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/owner/store-reviews")
    public ResponseEntity<PagedModel<EntityModel<ReviewResponseDto>>> getFilteredReviews(
            @RequestParam Long storeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Float minRating,
            @RequestParam(required = false) Float maxRating,
            @RequestParam(defaultValue = "false") Boolean onlyNoResponse,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            PagedResourcesAssembler<ReviewResponseDto> assembler) {
        authorizationService.canAccessStore(storeId);
        // Vérifier si le store existe
        storeServiceImpl.getStoreById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with ID: " + storeId));

        // Récupération des avis filtrés
        Page<ReviewResponseDto> reviewsPage = reviewService.getFilteredReviews(storeId, keyword, minRating, maxRating, onlyNoResponse, page, size);
        PagedModel<EntityModel<ReviewResponseDto>> pagedModel = assembler.toModel(reviewsPage);

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/store-stats/{storeId}")
    public ResponseEntity<StoreReviewStatsDTO> getReviewStats(@PathVariable Long storeId) {
        return ResponseEntity.ok(reviewService.getReviewStats(storeId));
    }

    // Endpoint pour récupérer les avis d'un client
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByClient(@PathVariable Long clientId) {
        authorizationService.isTheClient(clientId);
        List<ReviewResponseDto> reviews = reviewService.getReviewsByClient(clientServiceImp.getClientById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId)));
        return ResponseEntity.ok(reviews);
    }

    // Endpoint pour récupérer les avis d'un client
    /*@GetMapping("{storeId}/review/{clientId}")
    public ResponseEntity<List<ReviewResponseDto>> getReviewByClientAndStore(@PathVariable Long storeId,@PathVariable Long clientId) {
        authorizationService.isTheClient(clientId);
        List<ReviewResponseDto> reviews = reviewService.getReviewsByClient(clientServiceImp.getClientById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId)));
        return ResponseEntity.ok(reviews);
    }*/

    // Endpoint pour supprimer un avis
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
