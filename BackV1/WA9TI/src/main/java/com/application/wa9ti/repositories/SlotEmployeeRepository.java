package com.application.wa9ti.repositories;

import com.application.wa9ti.models.EmployeeSchedule;
import com.application.wa9ti.models.EmployeeStore;
import com.application.wa9ti.models.SlotEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SlotEmployeeRepository extends JpaRepository<SlotEmployee, Long> {

    /** Supprime tous les SlotEmployee liés à un EmployeeStore donné */
    @Modifying
    @Query("DELETE FROM SlotEmployee se WHERE se.schedule IN (SELECT es FROM EmployeeSchedule es WHERE es.employeeStore = :employeeStore)")
    void deleteByEmployeeStore(@Param("employeeStore") EmployeeStore employeeStore);

    @Modifying
    @Query("DELETE FROM SlotEmployee se WHERE se.schedule = :schedule")
    void deleteBySchedule(@Param("schedule") EmployeeSchedule schedule);

}

