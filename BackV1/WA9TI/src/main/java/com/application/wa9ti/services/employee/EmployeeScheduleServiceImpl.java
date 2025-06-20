package com.application.wa9ti.services.employee;

import com.application.wa9ti.dtos.EmployeeScheduleDto;
import com.application.wa9ti.dtos.SlotDTO;
import com.application.wa9ti.dtos.SlotEmployeeDto;
import com.application.wa9ti.dtos.StoreEmployeeScheduleDto;
import com.application.wa9ti.models.EmployeeSchedule;
import com.application.wa9ti.models.EmployeeStore;
import com.application.wa9ti.models.SlotEmployee;
import com.application.wa9ti.repositories.EmployeeScheduleRepository;
import com.application.wa9ti.repositories.EmployeeStoreRepository;
import com.application.wa9ti.repositories.SlotEmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeScheduleServiceImpl implements EmployeeScheduleService {

    private final EmployeeScheduleRepository scheduleRepository;
    private final EmployeeStoreRepository employeeStoreRepository;
    private final SlotEmployeeRepository slotEmployeeRepository;

    @Override
    public List<StoreEmployeeScheduleDto> getEmployeesWithSchedulesByStore(Long storeId) {
        List<EmployeeStore> employeeStores = employeeStoreRepository.findByStoreId(storeId);

        return employeeStores.stream()
                .map(employeeStore -> new StoreEmployeeScheduleDto(
                        employeeStore.getId(),
                        employeeStore.getEmployee().getId(),
                        employeeStore.getEmployee().getUser().getName(),
                        employeeStore.getEmployee().getImage(),
                        employeeStore.getRole().toString(),
                        scheduleRepository.findByEmployeeStore(employeeStore).stream()
                                .map(schedule -> new EmployeeScheduleDto(
                                        schedule.getDay(),
                                        schedule.getSlots().stream()
                                                .map(slot -> new SlotEmployeeDto(slot.getStartTime(), slot.getEndTime()))
                                                .toList()
                                ))
                                .toList()
                ))
                .toList();

    }

    @Override
    public StoreEmployeeScheduleDto getEmployeeScheduleByStore(Long employeeStoreId, Long storeId) {
        EmployeeStore employeeStore = employeeStoreRepository.findById(employeeStoreId)
                .orElseThrow(() -> new IllegalArgumentException("EmployeeStore not found for employeeId: "
                        + employeeStoreId + " and storeId: " + storeId));

        List<EmployeeSchedule> schedules = scheduleRepository.findByEmployeeStore_Employee_IdAndEmployeeStore_Store_Id(employeeStore.getEmployee().getId(), storeId);

        return new StoreEmployeeScheduleDto(
                employeeStore.getId(),
                employeeStore.getEmployee().getId(),
                employeeStore.getEmployee().getUser().getName(),
                employeeStore.getEmployee().getImage(),
                employeeStore.getRole().toString(),
                schedules.stream()
                        .map(schedule -> new EmployeeScheduleDto(
                                schedule.getDay(),
                                schedule.getSlots().stream()
                                        .map(slot -> new SlotEmployeeDto(slot.getStartTime(), slot.getEndTime()))
                                        .toList()
                        ))
                        .toList()
        );
    }





    @Override
    @Transactional
    public void updateWeeklySchedule(StoreEmployeeScheduleDto dto) {
        EmployeeStore employeeStore = employeeStoreRepository.findById(dto.employeeStoreId())
                .orElseThrow(() -> new IllegalArgumentException("EmployeeStore not found"));

        // Récupérer tous les plannings existants de l'employé
        List<EmployeeSchedule> existingSchedules = scheduleRepository.findByEmployeeStore(employeeStore);

        for (EmployeeScheduleDto scheduleDto : dto.schedules()) {
            EmployeeSchedule schedule = existingSchedules.stream()
                    .filter(s -> s.getDay().equalsIgnoreCase(scheduleDto.day()))
                    .findFirst()
                    .orElse(null);

            if (schedule == null) {
                // Si le jour n'existe pas encore, on le crée
                schedule = new EmployeeSchedule();
                schedule.setEmployeeStore(employeeStore);
                schedule.setDay(scheduleDto.day());
                schedule.setSlots(new ArrayList<>());
                existingSchedules.add(schedule);
            } else {
                // ✅ Supprimer explicitement les anciens créneaux en base pour éviter les doublons
                slotEmployeeRepository.deleteBySchedule(schedule);
                schedule.getSlots().clear();
            }

            // ✅ Ajouter les nouveaux créneaux et les lier correctement au schedule
            EmployeeSchedule finalSchedule = schedule;
            List<SlotEmployee> newSlots = scheduleDto.slots().stream()
                            .map(slotDto -> new SlotEmployee(null, slotDto.startTime(), slotDto.endTime(), finalSchedule))
                    .toList();

            schedule.getSlots().addAll(newSlots);
        }

        // ✅ Sauvegarder toutes les mises à jour
        scheduleRepository.saveAll(existingSchedules);
    }


}

