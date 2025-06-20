package com.application.wa9ti.services.user;

import com.application.wa9ti.dtos.LoginDto;
import com.application.wa9ti.dtos.PasswordChangeDto;
import com.application.wa9ti.dtos.UserDto;
import com.application.wa9ti.dtos.UserPreferencesDto;
import com.application.wa9ti.enums.Language;
import com.application.wa9ti.enums.Theme;
import com.application.wa9ti.models.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

public interface UserService {
    User createUser(User user); // Créer un nouvel utilisateur
    UserDto verifyUser(LoginDto loginDto, HttpServletResponse response);
    Optional<User> findUserByEmail(String email); // Rechercher un utilisateur par email
    Optional<User> findUserByPhone(String email); // Rechercher un utilisateur par phone
    Optional<User> findUserById(Long id); // Rechercher un utilisateur par ID
    User updateUser(Long id, User user); // Mettre à jour un utilisateur existant
    void deleteUser(Long id); // Supprimer un utilisateur
    void updateUserPreferences(String userEmail, Language language, Theme theme);
    UserPreferencesDto getUserPreferences(String userEmail);
    void updatePassword(Long id, PasswordChangeDto passwordChangeDto, HttpServletRequest request);
    boolean checkEmailAndPhone(String email,String phone);
}
