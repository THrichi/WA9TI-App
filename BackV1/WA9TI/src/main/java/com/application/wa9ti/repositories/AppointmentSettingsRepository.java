package com.application.wa9ti.repositories;

import com.application.wa9ti.models.AppointmentSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppointmentSettingsRepository extends JpaRepository<AppointmentSettings, Long> {
    Optional<AppointmentSettings> findByStoreId(Long storeId);


}
