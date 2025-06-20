package com.application.wa9ti.services.user;

import com.application.wa9ti.configuration.SubscriptionConfig;
import com.application.wa9ti.dtos.LoginDto;
import com.application.wa9ti.dtos.PasswordChangeDto;
import com.application.wa9ti.dtos.UserDto;
import com.application.wa9ti.dtos.UserPreferencesDto;
import com.application.wa9ti.enums.Language;
import com.application.wa9ti.enums.Role;
import com.application.wa9ti.enums.Theme;
import com.application.wa9ti.models.Owner;
import com.application.wa9ti.models.Store;
import com.application.wa9ti.models.Subscription;
import com.application.wa9ti.models.User;
import com.application.wa9ti.repositories.UserRepository;
import com.application.wa9ti.services.auth.JWTService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImp implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    AuthenticationManager authManager;


    @Override
    public User createUser(User user) {
        // Vérifier si un utilisateur avec cet email existe déjà
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("EXCEPTION.USER_EMAIL_EXISTS");
        }

        // Vérifier si un utilisateur avec ce téléphone existe déjà
        if (userRepository.findByPhone(user.getPhone()).isPresent()) {
            throw new IllegalArgumentException("EXCEPTION.USER_PHONE_EXISTS");
        }

        // Sauvegarder le nouvel utilisateur
        return userRepository.save(user);
    }

    @Override
    public UserDto verifyUser(LoginDto loginDto, HttpServletResponse response) {
        try {
            // Vérifier si l'utilisateur existe d'abord
            User user = userRepository.findByEmail(loginDto.email())
                    .orElseThrow(() -> new IllegalArgumentException("EXCEPTION.INCORRECT_EMAIL"));

            // Vérifier si le compte est activé avant l'authentification
            if (!user.isVerified()) {
                throw new IllegalArgumentException("EXCEPTION.ACCOUNT_NOT_VERIFIED");
            }

            if(!user.isActive() && user.getRole().equals(Role.ROLE_OWNER))
            {
                Owner owner = user.getOwner();
                if(owner != null)
                {
                    if(!owner.getStores().isEmpty())
                    {
                        Store store = owner.getStores().get(0);
                        if(store != null){
                            if(store.getRdvCount() >= SubscriptionConfig.FREE_APPOINTEMENTS)
                            {
                                owner.getSubscription().setStatus(Subscription.SubscriptionStatus.EXPIRED);
                            }else{
                                owner.getSubscription().setStatus(Subscription.SubscriptionStatus.ACTIVE);
                            }
                        }
                    }
                    owner.getSubscription().setType(Subscription.SubscriptionType.FREE);
                }
                user.setActive(true);
                userRepository.save(user);
            }

            try {
                // Authentification de l'utilisateur
                Authentication authentication = authManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password())
                );

                if (!authentication.isAuthenticated()) {
                    throw new IllegalArgumentException("EXCEPTION.EXCEPTION.INCORRECT_PASSWORD");
                }

            } catch (Exception e) {
                throw new IllegalArgumentException("EXCEPTION.INCORRECT_PASSWORD");
            }

            // Génération du token JWT
            String token = jwtService.generateToken(loginDto.email());
            jwtService.addJwtCookieToResponse(token, response);

            // Retourner l'utilisateur en DTO
            return new UserDto(
                    user.getId(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getRole()
            );

        } catch (IllegalArgumentException ex) {
            throw ex; // GlobalExceptionHandler capture cette exception
        }
    }






    @Override
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    @Override
    public Optional<User> findUserByPhone(String email) {
        return userRepository.findByPhone(email);
    }

    @Override
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User updateUser(Long id, User user) {
        // Vérifier si l'utilisateur existe
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        // Mettre à jour les champs nécessaires
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setPassword(user.getPassword());
        existingUser.setRole(user.getRole());

        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) {
        // Vérifier si l'utilisateur existe
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public void updateUserPreferences(String userEmail, Language language, Theme theme) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));


        user.setLanguage(language);
        user.setTheme(theme);
        userRepository.save(user);
    }

    @Override
    public UserPreferencesDto getUserPreferences(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return new UserPreferencesDto(user.getLanguage(), user.getTheme());
    }

    @Override
    public void updatePassword(Long id, PasswordChangeDto passwordChangeDto, HttpServletRequest request) {
        // Récupérer le token JWT depuis les cookies
        String token = jwtService.extractTokenFromCookies(request);
        if (token == null || !jwtService.validateToken(token)) {
            throw new RuntimeException("Utilisateur non authentifié ou token invalide.");
        }

        // Récupérer l'utilisateur connecté à partir de l'email
        User existingUser = userRepository.findByEmail(jwtService.extractUserName(token))
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec cet email."));

        // Vérifier si l'utilisateur connecté correspond à l'ID donné
        if (!existingUser.getId().equals(id)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier ce mot de passe.");
        }

        // Vérifier si le mot de passe actuel correspond
        if (!passwordEncoder.matches(passwordChangeDto.getCurrentPassword(), existingUser.getPassword())) {
            throw new RuntimeException("Le mot de passe actuel est incorrect.");
        }

        // Vérifier si le nouveau mot de passe et la confirmation correspondent
        if (!passwordChangeDto.getNewPassword().equals(passwordChangeDto.getConfirmPassword())) {
            throw new RuntimeException("Les nouveaux mots de passe ne correspondent pas.");
        }

        // Mettre à jour le mot de passe avec BCrypt
        existingUser.setPassword(passwordEncoder.encode(passwordChangeDto.getNewPassword()));

        // Sauvegarder les modifications
        userRepository.save(existingUser);
    }

    @Override
    public boolean  checkEmailAndPhone(String email,String phone) {
        boolean exists = userRepository.existsByEmail(email);
        if (exists) {
            throw new IllegalArgumentException("Cet email est déjà utilisé.");
        }
        exists = userRepository.existsByPhoneAndRole(phone, Role.ROLE_OWNER);
        if (exists) {
            throw new IllegalArgumentException("Ce numéro de telephone est déjà utilisé.");
        }
        return true;
    }

}