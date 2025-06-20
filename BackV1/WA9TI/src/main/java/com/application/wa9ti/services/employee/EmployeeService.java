package com.application.wa9ti.services.employee;

import com.application.wa9ti.dtos.*;
import com.application.wa9ti.enums.SubRole;
import com.application.wa9ti.models.Employee;
import com.application.wa9ti.models.VacationSlot;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeService {
    List<EmployeeStoreDto> getAllStoreEmployees(Long storeId);
    Employee getAuthenticatedEmployee(String email);
    Employee getEmployeeById(Long id);
    EmployeeStoreDto createEmployee(NewEmployeeDto employee, Long storeId);
    EmployeeStoreDto updateEmployee(Long storeId,Long employeeStoreId, String note, SubRole newRole);
    void updateEmployeeImage(Long id, String imageURL);
    void deleteEmployee(Long id);
    void updateEmployeeVacations(Long id, List<VacationSlot> vacationSlots);
    void updateEmployeeService(Long employeeStoreId, Long serviceIds);
    void removeServiceFromEmployee(Long employeeStoreId, Long serviceId);
    Employee createEmployee(NewUserDto userDto);
    Long createEmployeeAndAssignToStore(EmployeeInviteCreateDto employeeDto);
    List<AppointmentEmployeeDTO> getEmployeesWithAppointments(Long storeId, LocalDate selectedDate);
    AppointmentEmployeeDTO getEmployeeWithAppointmentsInDatePlage(Long storeId, Long employeeId, LocalDate startDate, LocalDate endDate);
    boolean removeVacation(Long employeeStoreId, VacationSlot vacationSlot);
    void removeEmployeeFromStore(Long employeeId, Long storeId);
    void updateGeneralInfo(Long id, GeneralEmployeeInfoDto generalInfoDto);
    void updatePhoneInfo(Long id, PhoneDto phoneDto);
    EmployeeCompletedStoreDto getEmployeeCompletedProfile(Long storeId);
}
