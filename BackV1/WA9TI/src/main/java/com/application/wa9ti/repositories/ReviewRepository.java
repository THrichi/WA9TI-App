package com.application.wa9ti.repositories;

import com.application.wa9ti.dtos.StoreReviewStatsDTO;
import com.application.wa9ti.models.Client;
import com.application.wa9ti.models.Review;
import com.application.wa9ti.models.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByClientAndStore(Client client, Store store);
    List<Review> findByStoreOrderByCreatedAtDesc(Store store); // Liste des avis pour un magasin
    List<Review> findByClient(Client client); // Liste des avis laissés par un client
    long countByStore(Store store);
    Page<Review> findByStoreId(Long storeId, Pageable pageable);
    Review findById(long id);

    @Query("""
    SELECT new com.application.wa9ti.dtos.StoreReviewStatsDTO(
        AVG(r.rating),
        AVG(r.hospitality),
        AVG(r.cleanliness),
        AVG(r.serviceQuality),
        AVG(r.valueForMoney),
        AVG(r.experience),
        COUNT(r)
    )
    FROM Review r
    WHERE r.store.id = :storeId
""")
    StoreReviewStatsDTO getStoreReviewStats(@Param("storeId") Long storeId);


    @Query("""
        SELECT r FROM Review r
        WHERE r.store.id = :storeId
        AND (:keyword IS NULL OR LOWER(r.client.user.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(r.storeResponse) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:minRating IS NULL OR r.rating >= :minRating)
        AND (:maxRating IS NULL OR r.rating <= :maxRating)
        AND (:onlyNoResponse IS FALSE OR r.storeResponse IS NULL OR r.storeResponse = '')
    """)
    Page<Review> findFilteredReviews(
            @Param("storeId") Long storeId,
            @Param("keyword") String keyword,
            @Param("minRating") Float minRating,
            @Param("maxRating") Float maxRating,
            @Param("onlyNoResponse") Boolean onlyNoResponse,
            Pageable pageable
    );
}
