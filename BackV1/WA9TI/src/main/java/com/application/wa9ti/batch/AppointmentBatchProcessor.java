package com.application.wa9ti.batch;

import com.application.wa9ti.models.Appointment;
import com.application.wa9ti.models.AppointmentSettings;
import com.application.wa9ti.repositories.AppointmentRepository;
import com.application.wa9ti.repositories.AppointmentSettingsRepository;
import com.application.wa9ti.repositories.ReservationCountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentBatchProcessor {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentSettingsRepository appointmentSettingsRepository;

    private final ReservationCountRepository reservationCountRepository;

    @Scheduled(cron = "0 * * * * *") // Exécute toutes les minutes
    @Transactional
    public void processMissedAppointments() {

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        LocalTime startTime = currentTime.minusHours(1);
        LocalTime endTime = currentTime.plusHours(1);
        // Récupérer les rendez-vous confirmés dans la plage de -1h à +1h
        List<Appointment> appointments = appointmentRepository.findConfirmedAppointmentsInTimeRange(today, startTime, endTime);

        for (Appointment appointment : appointments) {
            int toleranceMinutes = appointmentSettingsRepository.findByStoreId(appointment.getStore().getId())
                    .map(AppointmentSettings::getToleranceTimeMinutes)
                    .orElse(15);

            LocalDateTime startTimeWithTolerance = LocalDateTime.of(today, appointment.getStartTime())
                    .plusMinutes(toleranceMinutes);
            if (now.isAfter(startTimeWithTolerance)) {
                    appointment.setStatus(Appointment.Status.MISSED);
                    appointmentRepository.save(appointment);
                    log.info("⚠️ Rendez-vous ID {} marqué comme MISSED (Store: {}).", appointment.getId(), appointment.getStore().getId());

                    if (appointment.getClient() != null) {

                        reservationCountRepository.findByClientIdAndStoreId(appointment.getClient().getId(), appointment.getStore().getId())
                                .ifPresent(reservationCount -> {

                                    appointmentSettingsRepository.findByStoreId(appointment.getStore().getId())
                                            .ifPresent(settings -> {

                                                reservationCount.setRdvNonRespecte(reservationCount.getRdvNonRespecte() + 1);
                                                reservationCount.setNbRdvActif(reservationCount.getNbRdvActif() - 1);
                                                if (reservationCount.getRdvNonRespecte() >= settings.getAutoBlockThreshold() &&
                                                                !settings.getBlockingPolicy().equals(AppointmentSettings.BlockingPolicy.NO_BLOCKING)) {
                                                    reservationCount.setBlackListed(true);
                                                }
                                                reservationCountRepository.save(reservationCount);
                                                log.info("📌 Mise à jour du rdvNonRespecte pour le client ID {} dans le store ID {}.",
                                                        appointment.getClient().getId(), appointment.getStore().getId());

                                        });
                                });
                    }
                }
            }
        }
    }
