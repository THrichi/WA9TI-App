package com.application.wa9ti.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VacationSlot {

    @Column(name = "start_date", nullable = false) // Renommage pour éviter le conflit
    private LocalDate startDate; // Correspond à `from`

    @Column(name = "end_date", nullable = false) // Renommage pour éviter le conflit
    private LocalDate endDate; // Correspond à `to`
}
