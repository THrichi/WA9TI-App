package com.application.wa9ti.services.employeeStore;

import com.application.wa9ti.enums.SubRole;
import com.application.wa9ti.models.EmployeeStore;

public interface EmployeeStoreService {
    // Créer une association entre un employé et un magasin
    EmployeeStore addEmployeeToStore(Long employeeId, Long storeId, SubRole role);
    boolean isEmployeeInStoreByEmail(String employeeEmail, Long storeId);
    void addEmployeeToStore(String email, Long storeId, SubRole subRole);
    // Compter les employés associés à un magasin
    long countEmployeesInStore(Long storeId);
    void toggleEmployeeStoreActive(Long employeeStoreId, Long storeId);
}
