package com.application.wa9ti.repositories;

import com.application.wa9ti.models.Owner;
import com.application.wa9ti.models.Subscription;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByOwnerId(Long ownerId);

    @Modifying
    @Query("DELETE FROM Subscription s WHERE s.owner = :owner")
    void deleteByOwner(@Param("owner") Owner owner);


    // Met à jour les abonnements dont la fin est dépassée en "PENDING"
    @Modifying
    @Transactional
    @Query("UPDATE Subscription s SET s.status = 'PENDING' WHERE s.endDate <= :today AND s.status = 'ACTIVE' AND s.type != 'FREE'")
    int markSubscriptionsAsPending(LocalDate today);

    // Met à jour les abonnements "PENDING" depuis plus de 15 jours en "EXPIRED"
    @Modifying
    @Transactional
    @Query("UPDATE Subscription s SET s.status = 'EXPIRED' WHERE s.status = 'PENDING' AND s.type != 'FREE' AND s.endDate < :expiredDate")
    int markSubscriptionsAsExpired(LocalDate expiredDate);



    @Transactional
    @Modifying
    @Query("""
    UPDATE Subscription sub
    SET sub.status = 'ACTIVE'
    WHERE sub.type = 'FREE' AND sub.status = 'EXPIRED'
""")
    int activateExpiredFreeSubscriptions();

    Optional<Subscription> findByOwner(Owner owner);


    @Query("SELECT s FROM Subscription s WHERE s.endDate = :today AND s.switchSubscriptionType IS NOT NULL")
    List<Subscription> findByEndDateAndSwitchSubscriptionTypeNotNull(@Param("today") LocalDate today);

}
