package com.application.wa9ti.models;

import com.application.wa9ti.enums.Language;
import com.application.wa9ti.enums.Role;
import com.application.wa9ti.enums.Theme;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;


    @Column(nullable = false)
    private String name;

    private String phone;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean isVerified = false;

    private String verificationToken;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = true)
    @JsonManagedReference
    private Owner owner;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = true)
    @JsonManagedReference
    @JsonBackReference
    private Employee employee;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = true)
    @JsonManagedReference
    private Client client;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Language language = Language.FR; // Langue par défaut

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Theme theme = Theme.LIGHT; // Thème par défaut

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private boolean isActive = true;
}
