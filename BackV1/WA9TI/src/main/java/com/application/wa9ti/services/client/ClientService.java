package com.application.wa9ti.services.client;

import com.application.wa9ti.dtos.*;
import com.application.wa9ti.models.Appointment;
import com.application.wa9ti.models.Client;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;

public interface ClientService {
    Optional<Client> getClientById(Long idClient);
    void createClient(NewUserDto userDto);
    ClientDto getAuthenticatedOwner(String email);
    List<AppointmentDto> getAppointmentsByClientId(Long clientId);
    void updateClientImage(Long id, String imageURL);
    ClientUpdateDto updateClient(Long clientId,ClientUpdateDto clientUpdateDto);
    ClientDto getClientByEmailOrPhone(String emailOrPhone);
    List<FavoriteStoreDTO> getTop3FavoriteStores(String clientEmail);
}
