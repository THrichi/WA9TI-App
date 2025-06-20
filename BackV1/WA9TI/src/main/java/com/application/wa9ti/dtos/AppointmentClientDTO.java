package com.application.wa9ti.dtos;

import com.application.wa9ti.models.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public record AppointmentClientDTO(
        Long id,
        String formattedDate,  // Ex: "Lundi 3 févr.. 10:00"
        String rawDateTime,    // ✅ Ex: "2025-02-03T10:00:00"
        String storeName,
        String storeAddress,
        String storeImage,
        String storeUrl,
        Long storeId,
        double latitude,
        double longitude,
        Long serviceId,
        String serviceName,
        double price,
        Long employeeId,
        String employeeName,
        int totalDuration,
        Appointment.Status status
) {
    public static AppointmentClientDTO fromAppointment(Appointment appointment) {
        return new AppointmentClientDTO(
                appointment.getId(),
                formatDate(appointment.getDate(), appointment.getStartTime()),
                formatRawDateTime(appointment.getDate(), appointment.getStartTime()), // ✅ Ajout du champ au format correct
                appointment.getStore().getName(),
                appointment.getStore().getAddress(),
                appointment.getStore().getImage(),
                appointment.getStore().getStoreUrl(),
                appointment.getStore().getId(),
                appointment.getStore().getLatitude(),
                appointment.getStore().getLongitude(),
                appointment.getService().getId(),
                appointment.getService().getName(),
                appointment.getPrice(),
                appointment.getEmployee().getId(),
                appointment.getEmployee().getUser().getName(),
                (appointment.getEndTime().toSecondOfDay() - appointment.getStartTime().toSecondOfDay()) / 60,
                appointment.getStatus()
        );
    }

    private static String formatDate(LocalDate date, LocalTime time) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        String month = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH);
        return capitalize(dayOfWeek) + " " + date.getDayOfMonth() + " " + month + ". " + time.format(timeFormatter);
    }

    private static String formatRawDateTime(LocalDate date, LocalTime time) {
        return date.atTime(time).toString(); // ✅ Renvoie au format "YYYY-MM-DDTHH:mm:ss"
    }

    private static String capitalize(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
