package com.application.wa9ti.repositories;

import com.application.wa9ti.enums.SubRole;
import com.application.wa9ti.models.Employee;
import com.application.wa9ti.models.EmployeeStore;
import com.application.wa9ti.models.Owner;
import com.application.wa9ti.models.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeStoreRepository extends JpaRepository<EmployeeStore, Long> {
    // Compter le nombre d'employés pour un magasin spécifique
    List<EmployeeStore> findByStoreId(Long storeId);
    List<EmployeeStore> findByStore(Store store);
    boolean existsByEmployeeAndStore(Employee employee, Store store);
    void deleteByEmployee(Employee employee);
    long countByStoreId(Long storeId);

    @Query("SELECT COUNT(es) > 0 FROM EmployeeStore es WHERE es.employee.user.email = :email AND es.store.id = :storeId")
    boolean existsByEmployeeEmailAndStoreId(@Param("email") String email, @Param("storeId") Long storeId);

    @Query("SELECT es FROM EmployeeStore es WHERE es.employee.id = :employeeId AND es.store.id = :storeId")
    Optional<EmployeeStore> findByEmployee_IdAndStore_Id(@Param("employeeId") Long employeeId, @Param("storeId") Long storeId);

    /** Supprime l'assignation d'un Employee à un Store spécifique */
    Optional<EmployeeStore> findByEmployeeAndStore(Employee employee, Store store);

    /** Vérifie si un Employee est encore assigné à au moins un Store */
    boolean existsByEmployee(Employee employee);

    boolean existsByEmployee_User_EmailAndStore_IdAndRole(String email, Long storeId, SubRole role);

    int countByStore(Store store);

    @Query("SELECT COUNT(e) FROM EmployeeStore e WHERE e.store = :store AND e.employee.user.id <> :ownerId")
    int countEmployeesExcludingOwner(@Param("store") Store store, @Param("ownerId") Long ownerId);

}
