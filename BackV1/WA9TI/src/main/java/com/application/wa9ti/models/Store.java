package com.application.wa9ti.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String storeUrl;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false) // Définit la clé étrangère
    @JsonBackReference
    private Owner owner; // Relation plusieurs-à-un avec Owner

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private String image;

    @Lob // Stocker du HTML
    @Column(columnDefinition = "TEXT")
    private String description;


    @ElementCollection
    @CollectionTable(name = "store_seo_keywords", joinColumns = @JoinColumn(name = "store_id"))
    @Column(name = "keyword")
    private List<String> seoKeywords;

    @ElementCollection
    @CollectionTable(name = "store_gallery", joinColumns = @JoinColumn(name = "store_id"))
    @Column(name = "image_url")
    private List<String> gallery;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Service> services;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<EmployeeStore> employeeStores;


    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "store_id")
    private List<OpeningHours> openingHours;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<StoreClosure> closures;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Review> reviews;

    @OneToOne(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private AppointmentSettings appointmentSettings;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<SocialNetwork> socialNetworks;


    @Column(nullable = false)
    private int rdvCount = 0;

    @Column(nullable = false)
    private boolean hasReachedLimit = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();


}
