package com.application.wa9ti.services.review;

import com.application.wa9ti.dtos.ReviewDTO;
import com.application.wa9ti.dtos.ReviewResponseDto;
import com.application.wa9ti.dtos.StoreReviewStatsDTO;
import com.application.wa9ti.models.Client;
import com.application.wa9ti.models.Review;
import com.application.wa9ti.models.Store;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    ReviewResponseDto saveOrUpdateReview(Long clientId,ReviewDTO reviewDto);
    Optional<Review> getReviewByClientAndStore(Client client, Store store);
    Page<ReviewResponseDto> getReviewsByStore(Long storeId, int page, int size);
    List<ReviewResponseDto> getReviewsByClient(Client client);
    void deleteReview(Long reviewId);
    StoreReviewStatsDTO getReviewStats(Long storeId);
    Page<ReviewResponseDto> getFilteredReviews(Long storeId, String keyword, Float minRating, Float maxRating, Boolean onlyNoResponse, int page, int size);
    ReviewResponseDto saveStoreResponse(Long storeId, Long reviewId, String response);
}
