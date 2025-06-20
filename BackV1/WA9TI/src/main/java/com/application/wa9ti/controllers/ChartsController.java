package com.application.wa9ti.controllers;

import com.application.wa9ti.dtos.charts.*;
import com.application.wa9ti.services.auth.AuthorizationService;
import com.application.wa9ti.services.charts.ChartService;
import com.application.wa9ti.services.charts.ChartServiceImp;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/charts")
@AllArgsConstructor
public class ChartsController {

    private final ChartService chartService;
    private final AuthorizationService authorizationService;

    @GetMapping("/stats/appointements")
    public List<AppointmentStatusStatsDTO> getAppointmentsStatsByStatus(
            @RequestParam Long storeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        authorizationService.canAccessStore(storeId);
        return chartService.getAppointmentsCountByDateAndStatus(storeId,startDate, endDate);
    }

    @GetMapping("/stats/status")
    public List<AppointmentStatusDTO> getAppointmentsStats(
            @RequestParam Long storeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        authorizationService.canAccessStore(storeId);
        return chartService.getAppointmentsCountByStatus(storeId, startDate, endDate);
    }

    @GetMapping("/stats/income")
    public List<AppointmentIncomeDTO> getAppointmentsIncome(
            @RequestParam Long storeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        authorizationService.canAccessStore(storeId);
        return chartService.getAppointmentsIncomeByDate(storeId, startDate, endDate);
    }

    @GetMapping("/stats/new-clients")
    public List<NewClientsDTO> getNewClientsStats(
            @RequestParam Long storeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        authorizationService.canAccessStore(storeId);
        return chartService.getNewClientsByDate(storeId, startDate, endDate);
    }

    @GetMapping("/stats/services")
    public List<ServiceBookingDTO> getServiceStats(
            @RequestParam Long storeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        authorizationService.canAccessStore(storeId);
        return chartService.getServiceBookings(storeId, startDate, endDate);
    }

    @GetMapping("/stats/top-clients")
    public List<TopClientsDTO> getTopClients(
            @RequestParam Long storeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "5") int limit) {
        authorizationService.canAccessStore(storeId);
        return chartService.getTopClients(storeId, startDate, endDate, limit);
    }


    @GetMapping("/stats/average-appointments")
    public AverageAppointmentsDTO getAverageAppointments(
            @RequestParam Long storeId) {
        authorizationService.canAccessStore(storeId);
        return chartService.getAverageAppointmentsPerClient(storeId);
    }

    @GetMapping("/stats/employees")
    public List<EmployeeAppointmentsDTO> getEmployeeAppointments(
            @RequestParam Long storeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        authorizationService.canAccessStore(storeId);
        return chartService.getEmployeeAppointments(storeId, startDate, endDate);
    }

    @GetMapping("/stats/popular-times")
    public List<PopularTimesDTO> getPopularTimes(
            @RequestParam Long storeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        authorizationService.canAccessStore(storeId);
        return chartService.getPopularTimes(storeId, startDate, endDate);
    }


}
