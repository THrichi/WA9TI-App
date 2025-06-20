package com.application.wa9ti.services.appointement;

import com.application.wa9ti.models.Client;
import com.application.wa9ti.models.ClientStore;
import com.application.wa9ti.models.Store;

public interface ReservationCountService {
    ClientStore getOrCreate(Client client, Store store);
    void incrementAppointmentCount(ClientStore reservationCount);
    void decrementActiveAppointments(ClientStore reservationCount);
    void updateClientNote(Long storeId, Long clientId, String note);
}
