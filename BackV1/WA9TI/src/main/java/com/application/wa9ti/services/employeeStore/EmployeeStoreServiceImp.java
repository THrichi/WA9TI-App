package com.application.wa9ti.services.employeeStore;

import com.application.wa9ti.enums.SubRole;
import com.application.wa9ti.models.*;
import com.application.wa9ti.repositories.EmployeeRepository;
import com.application.wa9ti.repositories.EmployeeScheduleRepository;
import com.application.wa9ti.repositories.EmployeeStoreRepository;
import com.application.wa9ti.repositories.StoreRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeStoreServiceImp implements EmployeeStoreService {

    @Autowired
    private EmployeeStoreRepository employeeStoreRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private EmployeeScheduleRepository employeeScheduleRepository;


    @Override
    public EmployeeStore addEmployeeToStore(Long employeeId, Long storeId, SubRole role) {
        // Vérifier si l'employé existe
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id " + employeeId));

        // Vérifier si le magasin existe
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with id " + storeId));

        // Créer une nouvelle association
        EmployeeStore employeeStore = new EmployeeStore();
        employeeStore.setEmployee(employee);
        employeeStore.setStore(store);
        employeeStore.setRole(role);

        return employeeStoreRepository.save(employeeStore);
    }

    @Override
    public long countEmployeesInStore(Long storeId) {
        // Compter les employés associés à un magasin spécifique
        return employeeStoreRepository.countByStoreId(storeId);
    }

    @Override
    // ImpEmployeeService.java
    public boolean isEmployeeInStoreByEmail(String employeeEmail, Long storeId) {
        return employeeStoreRepository.existsByEmployeeEmailAndStoreId(employeeEmail, storeId);
    }

    /*@Transactional
    @Override
    public void addEmployeeToStore(String email, Long storeId, SubRole subRole) {
        // Vérifier si l'employé existe via son email
        Employee employee = employeeRepository.findByUser_Email(email)
                .orElseThrow(() -> new IllegalArgumentException("Aucun employé trouvé avec l'email : " + email));

        // Vérifier si le magasin existe
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Aucun magasin trouvé avec l'ID : " + storeId));

        // Vérifier si l'employé est déjà associé à ce magasin
        if (employeeStoreRepository.existsByEmployeeEmailAndStoreId(email, storeId)) {
            throw new IllegalArgumentException("L'employé avec l'email " + email + " est déjà associé à ce magasin.");
        }

        // Créer une nouvelle association EmployeeStore
        EmployeeStore employeeStore = new EmployeeStore();
        employeeStore.setEmployee(employee);
        employeeStore.setStore(store);
        employeeStore.setRole(subRole);

        // Sauvegarder dans le repository
        employeeStoreRepository.save(employeeStore);
    }*/

    @Transactional
    @Override
    public void addEmployeeToStore(String email, Long storeId, SubRole subRole) {
        // Vérifier si l'employé existe via son email
        Employee employee = employeeRepository.findByUser_Email(email)
                .orElseThrow(() -> new IllegalArgumentException("Aucun employé trouvé avec l'email : " + email));

        // Vérifier si le magasin existe
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Aucun magasin trouvé avec l'ID : " + storeId));

        // Vérifier si l'employé est déjà associé à ce magasin
        if (employeeStoreRepository.existsByEmployeeEmailAndStoreId(email, storeId)) {
            throw new IllegalArgumentException("L'employé avec l'email " + email + " est déjà associé à ce magasin.");
        }

        // Créer une nouvelle association EmployeeStore
        EmployeeStore employeeStore = new EmployeeStore();
        employeeStore.setEmployee(employee);
        employeeStore.setStore(store);
        employeeStore.setRole(subRole);

        // Sauvegarde de EmployeeStore
        employeeStore = employeeStoreRepository.save(employeeStore);

        // Récupérer les horaires d'ouverture du magasin
        List<OpeningHours> openingHours = store.getOpeningHours();
        for (OpeningHours o : store.getOpeningHours()) {
            System.err.println("ID: " + o.getId());
            System.err.println("Jour: " + o.getDay());

            System.err.println("Créneaux horaires:");
            for (Slot slot : o.getSlots()) {
                System.err.println("   Début: " + slot.getStartTime() + " - Fin: " + slot.getEndTime());
            }

            System.err.println("----------");
        }

        // Créer les plannings (EmployeeSchedule) en fonction des horaires d'ouverture
        List<EmployeeSchedule> schedules = new ArrayList<>();
        for (OpeningHours openingHour : openingHours) {
            EmployeeSchedule schedule = new EmployeeSchedule();
            schedule.setEmployeeStore(employeeStore);
            schedule.setDay(openingHour.getDay());

            // Transformer les slots du magasin en SlotEmployee
            List<SlotEmployee> slotEmployees = openingHour.getSlots().stream().map(slot -> {
                SlotEmployee slotEmployee = new SlotEmployee();
                slotEmployee.setStartTime(slot.getStartTime());
                slotEmployee.setEndTime(slot.getEndTime());
                slotEmployee.setSchedule(schedule);
                return slotEmployee;
            }).collect(Collectors.toList());

            schedule.setSlots(slotEmployees);
            schedules.add(schedule);
        }

        // Sauvegarde des plannings de l'employé
        employeeScheduleRepository.saveAll(schedules);
    }


    @Override
    @Transactional
    public void toggleEmployeeStoreActive(Long employeeStoreId, Long storeId) {
        // 1️⃣ Vérifier si l'EmployeeStore existe
        EmployeeStore employeeStore = employeeStoreRepository.findById(employeeStoreId)
                .orElseThrow(() -> new IllegalArgumentException("Aucune assignation trouvée entre cet employé et ce store."));

        // 2️⃣ Basculer l'état actif/inactif
        employeeStore.setActive(!employeeStore.isActive());

        // 3️⃣ Sauvegarder la modification
        employeeStoreRepository.save(employeeStore);
    }

}