package com.application.wa9ti.repositories;

import com.application.wa9ti.models.Client;
import com.application.wa9ti.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository  extends JpaRepository<Client, Long> {
    Optional<Client> findByUser(User user);
    @Modifying
    @Query("UPDATE Client c SET c.image = :imageURL WHERE c.id = :id")
    void updateImageUrlById(@Param("id") Long id, @Param("imageURL") String imageURL);


    @Query("SELECT c FROM Client c WHERE c.user.email = :email")
    Optional<Client> findByUserEmail(@Param("email") String email);

}
