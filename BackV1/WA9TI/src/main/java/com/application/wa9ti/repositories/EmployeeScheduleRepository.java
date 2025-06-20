package com.application.wa9ti.repositories;

import com.application.wa9ti.models.Employee;
import com.application.wa9ti.models.EmployeeSchedule;
import com.application.wa9ti.models.EmployeeStore;
import com.application.wa9ti.models.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeScheduleRepository extends JpaRepository<EmployeeSchedule, Long> {
    List<EmployeeSchedule> findByEmployeeStore(EmployeeStore employeeStore);
    List<EmployeeSchedule> findByEmployeeStore_Employee_IdAndEmployeeStore_Store_Id(Long employeeId, Long storeId);

    @Modifying
    @Query("DELETE FROM EmployeeSchedule es WHERE es.employeeStore = :employeeStore")
    void deleteByEmployeeStore(@Param("employeeStore") EmployeeStore employeeStore);

}


