package com.application.wa9ti.services.appointement;

import com.application.wa9ti.models.AppointmentSettings;
import com.application.wa9ti.models.Client;
import com.application.wa9ti.models.ClientStore;
import com.application.wa9ti.models.Store;
import com.application.wa9ti.repositories.AppointmentSettingsRepository;
import com.application.wa9ti.repositories.ReservationCountRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ReservationCountServiceImpl implements ReservationCountService {

    private final ReservationCountRepository reservationCountRepository;
    private final AppointmentSettingsRepository appointmentSettingsRepository;

    public ReservationCountServiceImpl(ReservationCountRepository reservationCountRepository, AppointmentSettingsRepository appointmentSettingsRepository) {
        this.reservationCountRepository = reservationCountRepository;
        this.appointmentSettingsRepository = appointmentSettingsRepository;
    }

    @Override
    public ClientStore getOrCreate(Client client, Store store) {
        return reservationCountRepository.findByClientIdAndStoreId(client.getId(), store.getId())
                .orElseGet(() -> {
                    ClientStore newClientStore= new ClientStore();
                    newClientStore.setClient(client); // Assurez-vous que Client est bien instancié
                    newClientStore.setStore(store); // Assurez-vous que Store est bien instancié
                    newClientStore.setNbRdvTotal(0);
                    newClientStore.setNbRdvActif(0);
                    newClientStore.setRdvAnnule(0);
                    newClientStore.setRdvNonRespecte(0);
                    newClientStore.setBlackListed(false);
                    newClientStore.setNewClient(true);
                    return reservationCountRepository.save(newClientStore);
                });
    }

    @Override
    public void incrementAppointmentCount(ClientStore reservationCount) {
        reservationCount.setNbRdvTotal(reservationCount.getNbRdvTotal() + 1);
        reservationCount.setNbRdvActif(reservationCount.getNbRdvActif() + 1);
        reservationCountRepository.save(reservationCount);
    }
    @Override
    public void decrementActiveAppointments(ClientStore reservationCount) {
        if (reservationCount.getNbRdvActif() > 0) {
            reservationCount.setNbRdvActif(reservationCount.getNbRdvActif() - 1);

            // Vérification avant de décrémenter nbRdvTotal pour éviter une valeur négative
            if (reservationCount.getNbRdvTotal() > 0) {
                reservationCount.setNbRdvTotal(reservationCount.getNbRdvTotal() - 1);
            }

            // Vérifier si le client n'a plus aucun RDV enregistré
            if (reservationCount.getNbRdvTotal() == 0) {
                reservationCount.setNewClient(true);
            }

            reservationCountRepository.save(reservationCount);
        }
    }

    @Override
    @Transactional
    public void updateClientNote(Long storeId, Long clientId, String note) {
        ClientStore reservationCount = reservationCountRepository.findByClientIdAndStoreId(clientId,storeId)
                .orElseThrow(() -> new IllegalArgumentException("Aucune réservation trouvée pour ce client dans ce magasin."));

        reservationCount.setNote(note);
        reservationCountRepository.save(reservationCount);
    }

}
