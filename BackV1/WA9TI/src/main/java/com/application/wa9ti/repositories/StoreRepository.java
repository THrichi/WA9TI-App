package com.application.wa9ti.repositories;

import com.application.wa9ti.models.Employee;
import com.application.wa9ti.models.OpeningHours;
import com.application.wa9ti.models.Store;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    @Query("SELECT s.openingHours FROM Store s WHERE s.id = :storeId")
    List<OpeningHours> findOpeningHoursByStoreId(@Param("storeId") Long storeId);

    boolean existsByStoreUrl(String storeName);

    Optional<Store> findByStoreUrl(String storeUrl);

    @Modifying
    @Query("UPDATE Store s SET s.image = :imageURL WHERE s.id = :id")
    void updateImageUrlById(@Param("id") Long id, @Param("imageURL") String imageURL);


    @Query(value = """
    SELECT DISTINCT s FROM Store s
    LEFT JOIN s.services serv
    LEFT JOIN s.seoKeywords seo
    JOIN s.owner o
    JOIN o.subscription sub
    WHERE (6371 * acos(cos(radians(:latitude)) * cos(radians(s.latitude))
    * cos(radians(s.longitude) - radians(:longitude))
    + sin(radians(:latitude)) * sin(radians(s.latitude)))) <= :radius
        AND sub.status NOT IN (com.application.wa9ti.models.Subscription.SubscriptionStatus.EXPIRED,
                               com.application.wa9ti.models.Subscription.SubscriptionStatus.CANCELED)
       AND (:keyword IS NULL OR
        LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(s.type) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(serv.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(s.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(seo) LIKE LOWER(CONCAT('%', :keyword, '%')))
    ORDER BY (6371 * acos(cos(radians(:latitude)) * cos(radians(s.latitude))
    * cos(radians(s.longitude) - radians(:longitude))
    + sin(radians(:latitude)) * sin(radians(s.latitude))))
    ASC
    """)
    List<Store> findStoresNearby(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radius") Double radius,
            @Param("keyword") String keyword
    );




    List<Store> findByOwnerId(Long ownerId);


    @Transactional
    @Modifying
    @Query("""
    UPDATE Store s
    SET s.rdvCount = 0
    WHERE s.owner.id IN (
        SELECT o.id FROM Owner o WHERE o.subscription.type = 'FREE'
    )
""")
    int resetRdvCountForFreeStores();



}
