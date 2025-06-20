package com.application.wa9ti.repositories;

import com.application.wa9ti.models.Owner;
import com.application.wa9ti.models.Store;
import com.application.wa9ti.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {
    // Méthode personnalisée pour trouver un utilisateur par email
    Optional<Owner> findByUser(User user);

    Optional<Owner> findByUser_Email(String email);

    @Modifying
    @Query("UPDATE Owner o SET o.image = :imageURL WHERE o.id = :id")
    void updateImageUrlById(@Param("id") Long id, @Param("imageURL") String imageURL);

    boolean existsByUser_EmailAndStoresContains(String email, Store store);


    @Modifying
    @Query("DELETE FROM SlotEmployee se WHERE se.schedule IN " +
            "(SELECT es FROM EmployeeSchedule es WHERE es.employeeStore IN " +
            "(SELECT e FROM EmployeeStore e WHERE e.store IN " +
            "(SELECT s FROM Store s WHERE s.owner.id = :ownerId)))")
    void deleteSlotEmployeesByOwner(@Param("ownerId") Long ownerId);



}
