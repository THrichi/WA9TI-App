package com.application.wa9ti.controllers;

import com.application.wa9ti.dtos.LoginDto;
import com.application.wa9ti.dtos.OwnerDto;
import com.application.wa9ti.dtos.UserDto;
import com.application.wa9ti.models.User;
import com.application.wa9ti.services.auth.JWTService;
import com.application.wa9ti.services.owner.OwnerServiceImp;
import com.application.wa9ti.services.user.UserServiceImp;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserServiceImp userService;

    @Autowired
    private JWTService jwtService;

    /*@PostMapping("/login")
    public String login(@RequestBody LoginDto user) {
        return this.ownerService.verifyOwner(user);
    }*/

    @PostMapping("/login")
    public UserDto login(@RequestBody LoginDto user, HttpServletResponse response) {
        return this.userService.verifyUser(user, response);
    }

    @GetMapping("/validate-cookie")
    public ResponseEntity<Boolean> validateCookie(HttpServletRequest request) {
        String token = jwtService.extractTokenFromCookies(request);
        if (token != null && jwtService.validateToken(token)) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.status(401).body(false);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("JWT", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // Utilisez true en production
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // Supprime immédiatement le cookie
        response.addCookie(jwtCookie);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getProfile(Authentication authentication) {
        // Récupère l'utilisateur connecté
        String email = authentication.getName();
        User user = userService.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Retourne un UserDto avec les informations pertinentes
        return ResponseEntity.ok(new UserDto(
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                user.getRole()
        ));
    }




}
