package com.application.wa9ti.dtos;

import com.application.wa9ti.enums.SubRole;
import com.application.wa9ti.models.Employee;
import com.application.wa9ti.models.EmployeeStore;

public record EmployeeProfileDTO(
        Long id,
        String name,
        String email,
        String phone,
        String note,
        String image,
        SubRole role
) {
    public static EmployeeProfileDTO fromEmployee(Employee employee, EmployeeStore employeeStore) {
        return new EmployeeProfileDTO(
                employee.getId(),
                employee.getUser().getName(),
                employee.getUser().getEmail(),
                employee.getUser().getPhone(),
                employeeStore.getNote(),
                employee.getImage(),
                employeeStore.getRole()
        );
    }

}
