package com.application.wa9ti.services.charts;

import com.application.wa9ti.dtos.charts.*;

import java.time.LocalDate;
import java.util.List;

public interface ChartService {
    List<AppointmentStatusStatsDTO> getAppointmentsCountByDateAndStatus(Long storeId,LocalDate startDate, LocalDate endDate);
    List<AppointmentStatusDTO> getAppointmentsCountByStatus(Long storeId,LocalDate startDate, LocalDate endDate);
    List<AppointmentIncomeDTO> getAppointmentsIncomeByDate(Long storeId, LocalDate startDate, LocalDate endDate);
    List<NewClientsDTO> getNewClientsByDate(Long storeId, LocalDate startDate, LocalDate endDate);
    List<ServiceBookingDTO> getServiceBookings(Long storeId, LocalDate startDate, LocalDate endDate);
    List<TopClientsDTO> getTopClients(Long storeId, LocalDate startDate, LocalDate endDate, int limit);
    AverageAppointmentsDTO getAverageAppointmentsPerClient(Long storeId);
    List<EmployeeAppointmentsDTO> getEmployeeAppointments(Long storeId, LocalDate startDate, LocalDate endDate);
    List<PopularTimesDTO> getPopularTimes(Long storeId, LocalDate startDate, LocalDate endDate);
}
