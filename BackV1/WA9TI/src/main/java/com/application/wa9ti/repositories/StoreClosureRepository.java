package com.application.wa9ti.repositories;

import com.application.wa9ti.models.StoreClosure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StoreClosureRepository extends JpaRepository<StoreClosure, Long> {
    @Query("SELECT sc FROM StoreClosure sc WHERE sc.store.id = :storeId AND sc.startDate <= :endDate AND (sc.endDate IS NULL OR sc.endDate >= :startDate)")
    List<StoreClosure> findByStoreIdAndDateBetween(@Param("storeId") Long storeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}

