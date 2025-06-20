package com.application.wa9ti.services.client;

import com.application.wa9ti.dtos.*;
import com.application.wa9ti.enums.Role;
import com.application.wa9ti.models.Appointment;
import com.application.wa9ti.models.Client;
import com.application.wa9ti.models.ClientStore;
import com.application.wa9ti.models.User;
import com.application.wa9ti.repositories.AppointmentRepository;
import com.application.wa9ti.repositories.ClientRepository;
import com.application.wa9ti.repositories.ReservationCountRepository;
import com.application.wa9ti.repositories.UserRepository;
import com.application.wa9ti.services.appointement.ReservationCountServiceImpl;
import com.application.wa9ti.services.user.UserServiceImp;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClientServiceImp implements ClientService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserServiceImp userServiceImp;
    private final AppointmentRepository appointmentRepository;
    private final ReservationCountServiceImpl reservationCountServiceImpl;
    private final ReservationCountRepository reservationCountRepository;

    public ClientServiceImp(ClientRepository clientRepository, PasswordEncoder passwordEncoder, UserRepository userRepository, UserServiceImp userServiceImp, AppointmentRepository appointmentRepository, ReservationCountServiceImpl reservationCountServiceImpl, ReservationCountRepository reservationCountRepository) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userServiceImp = userServiceImp;
        this.appointmentRepository = appointmentRepository;
        this.reservationCountServiceImpl = reservationCountServiceImpl;
        this.reservationCountRepository = reservationCountRepository;
    }

    @Override
    public Optional<Client> getClientById(Long idClient) {
        return clientRepository.findById(idClient);
    }

    @Override
    public void createClient(NewUserDto userDto) {
        // Vérifiez si l'email existe déjà
        if (userServiceImp.findUserByEmail(userDto.email()).isPresent()) {
            throw new IllegalArgumentException("EXCEPTION.USER_EMAIL_EXISTS");
        }

        // Créez l'utilisateur
        User user = new User();
        user.setEmail(userDto.email());
        user.setPassword(passwordEncoder.encode(userDto.password())); // Encodez le mot de passe
        user.setPhone(userDto.phone());
        user.setRole(Role.ROLE_CLIENT);
        user.setName(userDto.username());
        user.setVerified(false); // Non vérifié par défaut
        userRepository.save(user);

        // Créez le client
        Client client = new Client();
        client.setUser(user);
        clientRepository.save(client);


        // Associer les rendez-vous existants au nouveau client
        List<Appointment> guestAppointments = appointmentRepository.findByClientEmail(userDto.email());
        for (Appointment appointment : guestAppointments) {
            appointment.setClient(client);
            appointment.setClientEmail(null);  // Effacer l'email
            appointment.setClientPhone(null);  // Effacer le téléphone
            appointment.setClientName(null);   // Effacer le nom

            ClientStore reservationCount = reservationCountServiceImpl.getOrCreate(client, appointment.getStore());

            // Incrémenter nbRdvTotal
            reservationCount.setNbRdvTotal(reservationCount.getNbRdvTotal() + 1);

            // Vérifier si le RDV est dans le futur ou encore actif aujourd’hui
            if (appointment.getDate().isAfter(LocalDate.now()) ||
                    (appointment.getDate().isEqual(LocalDate.now()) && appointment.getStartTime().isAfter(LocalTime.now()))) {
                reservationCount.setNbRdvActif(reservationCount.getNbRdvActif() + 1);
            }

            // Sauvegarde de ReservationCount après modification
            reservationCountRepository.save(reservationCount);

        }
        appointmentRepository.saveAll(guestAppointments);
    }

    @Override
    public ClientDto getAuthenticatedOwner(String email) {
        // Récupère l'utilisateur par email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

        // Vérifie si l'utilisateur a le rôle "ROLE_OWNER"
        if (!Role.ROLE_CLIENT.equals(user.getRole())) {
            throw new RuntimeException("User with email " + email + " is not a Client");
        }

        // Récupère le propriétaire associé
        Client client = clientRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Owner not found for user with email: " + email));

        // Retourne le DTO avec les informations nécessaires
        return new ClientDto(
                client.getId(),
                client.getUser().getName(),
                client.getImage()
        );
    }


    @Override
    public List<AppointmentDto> getAppointmentsByClientId(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'ID : " + clientId));

        return client.getAppointments().stream()
                .map(appointment -> {
                    // Récupérer le nombre de réservations pour ce magasin
                    /*int reservationCount = client.getReservationCount().stream()
                            .filter(rc -> rc.getIdStore().equals(appointment.getStore().getId()))
                            .map(ReservationCount::getCount)
                            .findFirst()
                            .orElse(0); // Si aucune réservation trouvée, retourner 0*/

                    return new AppointmentDto(
                            appointment.getId(),
                            appointment.getDate(),
                            appointment.getStartTime(),
                            appointment.getEndTime(),
                            appointment.getPrice(),
                            appointment.getStore() != null ? appointment.getStore().getId() : null,
                            appointment.getStore() != null ? appointment.getStore().getName() : null,
                            appointment.getEmployee() != null ? appointment.getEmployee().getId() : null,
                            appointment.getEmployee() != null ? appointment.getEmployee().getUser().getName() : null,
                            appointment.getService() != null ? appointment.getService().getId() : null,
                            appointment.getService() != null ? appointment.getService().getName() : null,
                            appointment.getClientNote(),
                            appointment.getEmployeeNote(),
                            appointment.getStatus().name(),
                            client.getId(),
                            client.getUser().getName(),
                            client.getImage(), // Correction : récupération de l'image du client
                            client.getUser().getEmail(),
                            client.getUser().getPhone(),
                            0 // Ajout du nombre de réservations pour ce magasin
                    );
                })
                .toList();
    }


    @Override
    @Transactional
    public void updateClientImage(Long id, String imageURL) {
        clientRepository.updateImageUrlById(id,imageURL);
    }

    @Override
    public ClientUpdateDto updateClient(Long clientId,ClientUpdateDto clientUpdateDto) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'ID : " + clientId));

        // Mettre à jour les informations du client
        client.getUser().setName(clientUpdateDto.getName());

        // Sauvegarder les modifications
        clientRepository.save(client);

        // Retourner le DTO mis à jour
        return new ClientUpdateDto(client.getUser().getName());
    }


    // Méthode pour récupérer un client existant ou null si le client n'est pas trouvé
    @Override
    public ClientDto getClientByEmailOrPhone(String emailOrPhone) {
        // Vérifier si l'email existe
        Optional<User> userByEmail = userRepository.findByEmail(emailOrPhone);

        // Vérifier si le téléphone existe
        Optional<User> userByPhone = userRepository.findByPhone(emailOrPhone);

        // Si on trouve un utilisateur avec le bon rôle
        if (userByEmail.isPresent() && isClient(userByEmail.get())) {
            return convertToClientDto(userByEmail.get().getClient());
        }

        if (userByPhone.isPresent() && isClient(userByPhone.get())) {
            return convertToClientDto(userByPhone.get().getClient());
        }

        return null;  // Retourne null si aucun client n'est trouvé
    }

    @Override
    public List<FavoriteStoreDTO> getTop3FavoriteStores(String clientEmail) {
        return appointmentRepository.findTop3StoresByClient(clientEmail);
    }


    // Méthode pour convertir Client en ClientDto
    private ClientDto convertToClientDto(Client client) {
        return new ClientDto(
                client.getId(),
                client.getUser().getName(),
                client.getImage()
        );
    }


    // Vérifie si un utilisateur a le rôle CLIENT
    private boolean isClient(User user) {
        return user.getRole() == Role.ROLE_CLIENT;  // Vérification que l'utilisateur a bien le rôle CLIENT
    }
}
