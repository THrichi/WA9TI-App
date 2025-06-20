package com.application.wa9ti.repositories;

import com.application.wa9ti.models.SocialNetwork;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialNetworkRepository extends JpaRepository<SocialNetwork, Long> {

    @Transactional
    @Modifying
    @Query("DELETE FROM SocialNetwork sn WHERE sn.store.id = :storeId")
    void deleteAllByStoreId(Long storeId);
}
