package com.application.wa9ti.repositories;

import com.application.wa9ti.models.Invoice;
import com.application.wa9ti.models.Owner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Page<Invoice> findByOwnerId(Long ownerId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Invoice i WHERE i.owner = :owner")
    void deleteByOwner(@Param("owner") Owner owner);


    // Recherche avec filtre sur startDate ET endDate
    @Query("""
    SELECT i FROM Invoice i
    WHERE i.owner.id = :ownerId
    AND (
        (i.startDate BETWEEN :startDate AND :endDate)
    )
    ORDER BY i.startDate DESC
""")
    Page<Invoice> findByOwnerIdAndStartDateBetweenOrAdjustedEndDateBetween(
            @Param("ownerId") Long ownerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
    boolean existsByInvoiceNumber(String invoiceNumber);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}