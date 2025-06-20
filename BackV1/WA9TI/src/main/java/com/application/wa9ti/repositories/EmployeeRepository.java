package com.application.wa9ti.repositories;

import com.application.wa9ti.models.Client;
import com.application.wa9ti.models.Employee;
import com.application.wa9ti.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT e FROM Employee e JOIN e.employeeStores es WHERE es.store.id = :storeId")
    List<Employee> findByStoreId(@Param("storeId") Long storeId);

    Optional<Employee> findByUser_Email(String email);
    Optional<Employee> findByUser(User user);
    @Modifying
    @Query("UPDATE Employee e SET e.image = :image WHERE e.id = :id")
    void updateEmployeeImage(@Param("id") Long id, @Param("image") String image);

    @Query("SELECT COUNT(es) > 0 FROM EmployeeStore es JOIN es.services s WHERE s = :serviceId")
    boolean existsByServicesContaining(Long serviceId);


    @Query("SELECT e FROM Employee e WHERE e.user.email = :email")
    Optional<Employee> findByUserEmail(@Param("email") String email);
}
