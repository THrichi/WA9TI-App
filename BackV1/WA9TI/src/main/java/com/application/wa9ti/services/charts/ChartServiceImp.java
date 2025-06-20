package com.application.wa9ti.services.charts;

import com.application.wa9ti.dtos.charts.*;
import com.application.wa9ti.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChartServiceImp implements ChartService {

    private final AppointmentRepository appointmentRepository;

    @Override
    public List<AppointmentStatusStatsDTO> getAppointmentsCountByDateAndStatus(Long storeId,LocalDate startDate, LocalDate endDate) {
        return appointmentRepository.getAppointmentsCountByDateAndStatus(storeId,startDate, endDate)
                .stream()
                .map(AppointmentStatusStatsDTO::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    public List<AppointmentStatusDTO> getAppointmentsCountByStatus(Long storeId,LocalDate startDate, LocalDate endDate) {
        return appointmentRepository.getAppointmentsCountByStatus(storeId,startDate, endDate)
                .stream()
                .map(AppointmentStatusDTO::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    public List<AppointmentIncomeDTO> getAppointmentsIncomeByDate(Long storeId, LocalDate startDate, LocalDate endDate) {
        return appointmentRepository.getAppointmentsIncomeByDate(storeId, startDate, endDate)
                .stream()
                .map(AppointmentIncomeDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<NewClientsDTO> getNewClientsByDate(Long storeId, LocalDate startDate, LocalDate endDate) {
        return appointmentRepository.getNewClientsByDate(storeId, startDate, endDate)
                .stream()
                .map(NewClientsDTO::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    public List<ServiceBookingDTO> getServiceBookings(Long storeId, LocalDate startDate, LocalDate endDate) {
        return appointmentRepository.getServiceBookings(storeId, startDate, endDate)
                .stream()
                .map(ServiceBookingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<TopClientsDTO> getTopClients(Long storeId, LocalDate startDate, LocalDate endDate, int limit) {
        return appointmentRepository.getTopClients(storeId, startDate, endDate, limit)
                .stream()
                .map(TopClientsDTO::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    public AverageAppointmentsDTO getAverageAppointmentsPerClient(Long storeId) {
        Double avg = appointmentRepository.getAverageAppointmentsPerClient(storeId);
        return new AverageAppointmentsDTO(avg != null ? avg : 0.0);
    }

    @Override
    public List<EmployeeAppointmentsDTO> getEmployeeAppointments(Long storeId, LocalDate startDate, LocalDate endDate) {
        return appointmentRepository.getEmployeeAppointments(storeId, startDate, endDate)
                .stream()
                .map(EmployeeAppointmentsDTO::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    public List<PopularTimesDTO> getPopularTimes(Long storeId, LocalDate startDate, LocalDate endDate) {
        return appointmentRepository.getPopularTimes(storeId, startDate, endDate)
                .stream()
                .map(PopularTimesDTO::fromEntity)
                .collect(Collectors.toList());
    }

}
