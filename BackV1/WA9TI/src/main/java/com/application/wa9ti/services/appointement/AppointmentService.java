package com.application.wa9ti.services.appointement;

import com.application.wa9ti.dtos.*;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {
    AvailableSlotsDTO getAvailableSlots(Long storeId, List<Long> serviceIds, LocalDate startDate, Long employeeId);
    void createAppointments(AppointmentCreateDTO appointmentDto);
    void createGuestAppointments(AppointmentCreateGuestDTO appointmentDto);
    List<AppointmentClientDTO> getUpcomingAppointmentsForClient(Long clientId);
    List<AppointmentClientDTO> getPastAppointmentsForClient(Long clientId);
    void deleteClientAppointment(Long appointmentId, Long clientId);
    void deleteAppointment(Long appointmentId);
    Page<AppointmentValidationDTO> getSortedAppointments(Long storeId,String keyword, int page, int size);
    Page<AppointmentValidationDTO> getSortedArchivedAppointments(Long storeId,String keyword, int page, int size);
    void validateAppointmentStatus(Long appointmentId);
    void honorAppointmentStatus(List<Long> appointmentId);
    List<AppointmentValidationDTO> getTodayAppointmentsForClient(Long storeId, String keyword);
    boolean canModifyAppointment(Long appointmentId, Long storeId);
    AppointmentClientDTO updateAppointmentDateTime(Long appointmentId, UpdateAppointmentDateTimeDTO dto);
}
