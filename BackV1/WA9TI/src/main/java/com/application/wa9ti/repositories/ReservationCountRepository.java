package com.application.wa9ti.repositories;

import com.application.wa9ti.models.ClientStore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationCountRepository extends JpaRepository<ClientStore, Long> {
    Optional<ClientStore> findByClientIdAndStoreId(Long clientId, Long storeId);

    @Query("""
        SELECT rc FROM ClientStore rc
        JOIN rc.client c
        JOIN c.user u
        WHERE rc.store.id = :storeId
          AND (
            (:keyword IS NULL OR :keyword = '') OR
            LOWER(c.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
               ORDER BY LOWER(c.user.name) ASC
    """)
    Page<ClientStore> findByStoreIdWithKeyword(
            @Param("storeId") Long storeId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

}

