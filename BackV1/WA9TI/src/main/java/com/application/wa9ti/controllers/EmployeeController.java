package com.application.wa9ti.controllers;

import com.application.wa9ti.dtos.*;
import com.application.wa9ti.enums.Role;
import com.application.wa9ti.enums.SubRole;
import com.application.wa9ti.models.Employee;
import com.application.wa9ti.models.EmployeeStore;
import com.application.wa9ti.models.VacationSlot;
import com.application.wa9ti.repositories.EmployeeStoreRepository;
import com.application.wa9ti.services.auth.AuthorizationService;
import com.application.wa9ti.services.employee.EmployeeScheduleService;
import com.application.wa9ti.services.employee.EmployeeScheduleServiceImpl;
import com.application.wa9ti.services.employee.EmployeeService;
import com.application.wa9ti.services.employee.ImpEmployeeService;
import com.application.wa9ti.services.employeeStore.EmployeeStoreService;
import com.application.wa9ti.services.employeeStore.EmployeeStoreServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private EmployeeScheduleService employeeScheduleService;
    @Autowired
    private AuthorizationService authorizationService;
    @Autowired
    private EmployeeStoreService employeeStoreService;
    @Autowired
    private EmployeeStoreRepository employeeStoreRepository;


    @GetMapping("{storeId}")
    public List<EmployeeStoreDto> getAllStoreEmployees(@PathVariable Long storeId) {
        return employeeService.getAllStoreEmployees(storeId);
    }

    @GetMapping("/{storeId}/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long storeId,@PathVariable Long id) {
        authorizationService.canAccessStore(storeId);
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    /*@PostMapping("/{storeId}")
    public ResponseEntity<Employee> createEmployee(@PathVariable Long storeId, @RequestBody EmployeeDto employee) {
        return ResponseEntity.ok(employeeService.createEmployee(employee, storeId));
    }*/

    @PostMapping("/{storeId}")
    public ResponseEntity<EmployeeStoreDto> createEmployee(@PathVariable Long storeId, @RequestBody NewEmployeeDto employee) {
        authorizationService.canAccessStore(storeId);
        return ResponseEntity.ok(employeeService.createEmployee(employee, storeId));
    }

    @PutMapping("/{storeId}/{employeeStoreId}")
    public ResponseEntity<EmployeeStoreDto> updateEmployee(
            @PathVariable Long storeId,
            @PathVariable Long employeeStoreId,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) SubRole subRole) {
        authorizationService.canAccessStore(storeId);
        EmployeeStoreDto updatedEmployee = employeeService.updateEmployee(storeId,employeeStoreId, note, subRole);
        return ResponseEntity.ok(updatedEmployee);
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{storeId}/{employeeStoreId}/vacations")
    public ResponseEntity<Void> updateEmployeeVacations(@PathVariable Long storeId,@PathVariable Long employeeStoreId, @RequestBody List<VacationSlot> vacationSlots) {
        authorizationService.canAccessStore(storeId);
        employeeService.updateEmployeeVacations(employeeStoreId, vacationSlots);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{storeId}/{employeeStoreId}/service")
    public ResponseEntity<Void> updateEmployeeService(
            @PathVariable Long storeId,
            @PathVariable Long employeeStoreId,
            @RequestParam Long serviceId) {
        authorizationService.canAccessStore(storeId);
        employeeService.updateEmployeeService(employeeStoreId, serviceId);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{storeId}/{employeeStoreId}/services/{serviceId}")
    public ResponseEntity<Void> removeServiceFromEmployee(
            @PathVariable Long storeId,
            @PathVariable Long employeeStoreId,
            @PathVariable Long serviceId) {
        authorizationService.canAccessStore(storeId);
        employeeService.removeServiceFromEmployee(employeeStoreId, serviceId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile/{storeId}")
    public ResponseEntity<EmployeeStoreDto> getEmployeeProfile(@PathVariable Long storeId) {
        // Obtenir l'utilisateur authentifié depuis le contexte de sécurité
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Vérifier si l'utilisateur a le rôle "ROLE_EMPLOYEE"
        boolean isEmployee = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals(Role.ROLE_EMPLOYEE.name()));

        if (!isEmployee) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Accès interdit si pas "ROLE_EMPLOYEE"
        }

        // Récupérer l'email de l'utilisateur authentifié
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();

        // Utiliser le service pour récupérer le DTO de l'employé
        Employee employeeLoginDto = employeeService.getAuthenticatedEmployee(email);
        EmployeeStore employeeStore = employeeStoreRepository.findByEmployee_IdAndStore_Id(employeeLoginDto.getId(),storeId).orElseThrow(
                ()->new RuntimeException("Employee not found"));

        return ResponseEntity.ok(EmployeeStoreDto.fromEmployee(storeId,employeeLoginDto, employeeStore));
    }

    @GetMapping("/completedProfile/{storeId}")
    public ResponseEntity<EmployeeCompletedStoreDto> getEmployeeCompletedProfile(@PathVariable Long storeId) {
        return ResponseEntity.ok(employeeService.getEmployeeCompletedProfile(storeId));
    }

    @GetMapping("/{storeId}/schedules")
    public ResponseEntity<List<StoreEmployeeScheduleDto>> getEmployeesWithSchedulesByStore(@PathVariable Long storeId) {
        authorizationService.canAccessStore(storeId);
        List<StoreEmployeeScheduleDto> employeesWithSchedules = employeeScheduleService.getEmployeesWithSchedulesByStore(storeId);
        return ResponseEntity.ok(employeesWithSchedules);
    }

    @GetMapping("/{storeId}/schedule/{employeeStoreId}")
    public ResponseEntity<StoreEmployeeScheduleDto> getEmployeeScheduleByStore(
            @PathVariable Long employeeStoreId,
            @PathVariable Long storeId) {
        authorizationService.canAccessStore(storeId);
        StoreEmployeeScheduleDto schedule = employeeScheduleService.getEmployeeScheduleByStore(employeeStoreId, storeId);
        return ResponseEntity.ok(schedule);
    }

    @PutMapping("/{storeId}/schedules/weekly")
    public ResponseEntity<String> updateWeeklySchedule(
            @PathVariable Long storeId,
            @RequestBody StoreEmployeeScheduleDto dto) {
        authorizationService.canAccessStore(storeId);
        employeeScheduleService.updateWeeklySchedule(dto);
        return ResponseEntity.ok("Weekly schedule updated successfully");
    }

    @GetMapping("/store/{storeId}/appointments")
    public List<AppointmentEmployeeDTO> getEmployeesWithAppointments(
            @PathVariable Long storeId,
            @RequestParam LocalDate selectedDate)
    {
        authorizationService.canAccessStore(storeId);
        return employeeService.getEmployeesWithAppointments(storeId, selectedDate);
    }

    @GetMapping("/{storeId}/{employeeId}/calendar/appointments")
    public ResponseEntity<AppointmentEmployeeDTO> getEmployeeAppointmentsInDatePlage(
            @PathVariable Long storeId,
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        authorizationService.canAccessStore(storeId);
        AppointmentEmployeeDTO employeeAppointments = employeeService.getEmployeeWithAppointmentsInDatePlage(
                storeId, employeeId, startDate, endDate);

        return ResponseEntity.ok(employeeAppointments);
    }

    @DeleteMapping("{storeId}/vacation/{employeeStoreId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeVacation(
            @PathVariable Long storeId,
            @PathVariable Long employeeStoreId,
            @RequestBody VacationSlot vacationSlot) {

        authorizationService.canAccessStore(storeId);

        if (!employeeService.removeVacation(employeeStoreId, vacationSlot)) {
            throw new IllegalArgumentException("Vacation not found for the given dates");
        }
    }


    @DeleteMapping("/{employeeId}/remove-employee/{storeId}")
    public ResponseEntity<Void> removeEmployeeFromStore(@PathVariable Long employeeId,@PathVariable Long storeId) {
        authorizationService.canAccessStore(storeId); // Vérifie si l'utilisateur peut gérer ce store
        employeeService.removeEmployeeFromStore(employeeId, storeId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{employeeStoreId}/toggle-active/{storeId}")
    public ResponseEntity<Void> toggleEmployeeStoreActive(@PathVariable Long employeeStoreId,@PathVariable Long storeId) {
        authorizationService.canAccessStore(storeId); // Vérifie si l'utilisateur a les droits d'accès au store
        employeeStoreService.toggleEmployeeStoreActive(employeeStoreId, storeId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/general-info")
    public ResponseEntity<String> updateGeneralInfo(@PathVariable Long id, @RequestBody GeneralEmployeeInfoDto generalInfoDto) {
        // Appeler le service pour mettre à jour les informations générales
        authorizationService.isTheEmployee(id);
        employeeService.updateGeneralInfo(id, generalInfoDto);
        return ResponseEntity.ok("Informations générales mises à jour avec succès.");
    }

    @PutMapping("/{id}/phone-info")
    public ResponseEntity<Void> updatePhoneInfo(@PathVariable Long id, @RequestBody PhoneDto phoneDto) {
        // Appeler le service pour mettre à jour les informations générales
        authorizationService.isTheEmployee(id);
        employeeService.updatePhoneInfo(id, phoneDto);
        return ResponseEntity.ok().build();
    }

}
