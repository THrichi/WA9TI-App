package com.application.wa9ti.configuration;

import com.application.wa9ti.enums.Role;
import com.application.wa9ti.models.Owner;
import com.application.wa9ti.models.Store;
import com.application.wa9ti.models.User;
import com.application.wa9ti.models.Subscription;
import com.application.wa9ti.repositories.OwnerRepository;
import com.application.wa9ti.repositories.StoreRepository;
import com.application.wa9ti.repositories.UserRepository;
import com.application.wa9ti.repositories.SubscriptionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Component
public class DataInitializer implements CommandLineRunner {
    private final StoreRepository storeRepository;
    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final Random random = new Random();

    // Coordonnées approximatives du centre de Fès
    private static final double FES_LAT = 34.0333;
    private static final double FES_LNG = -5.0000;
    private static final double RADIUS_KM = 10.0; // Rayon de 10 km

    public DataInitializer(StoreRepository storeRepository, OwnerRepository ownerRepository,
                           UserRepository userRepository, SubscriptionRepository subscriptionRepository) {
        this.storeRepository = storeRepository;
        this.ownerRepository = ownerRepository;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public void run(String... args) {
        if (storeRepository.count() == 0) { // Vérifie si des magasins existent déjà
            System.out.println("🔹 Génération de 100 magasins avec abonnements...");

            IntStream.range(1, 101).forEach(i -> {
                // Création du User
                User user = new User();
                user.setName("Propriétaire " + i);
                user.setEmail("owner" + i + "@example.com");
                user.setPhone("060000000" + (i % 10)); // Numéros différents
                user.setPassword("password" + i);
                user.setRole(Role.ROLE_OWNER);
                user.setVerified(true);
                user = userRepository.save(user);

                // Création de l'Owner
                Owner owner = new Owner();
                owner.setUser(user);
                owner = ownerRepository.save(owner);

                // Création d'un abonnement pour cet Owner
                Subscription subscription = new Subscription();
                subscription.setOwner(owner);
                subscription.setType(getRandomSubscriptionType());
                subscription.setBillingType(Subscription.BillingType.FIXED);
                subscription.setStartDate(LocalDate.now());
                subscription.setEndDate(LocalDate.now().plusMonths(1)); // Abonnement valide pour 1 mois
                subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
                subscriptionRepository.save(subscription);

                // Création du Store
                Store store = new Store();
                store.setName("Magasin " + i);
                store.setStoreUrl("store" + i + ".com");
                store.setType("Type " + (i % 5 + 1)); // 5 types différents
                store.setEmail("store" + i + "@example.com");
                store.setPhone("061234567" + (i % 10));
                store.setAddress("Adresse " + i + ", Fès");
                store.setLatitude(generateRandomLatitude());
                store.setLongitude(generateRandomLongitude());
                store.setOwner(owner);
                store.setImage("default-image.jpg");
                store.setDescription("Description du magasin " + i);

                storeRepository.save(store);
            });

            System.out.println("✅ 100 magasins avec abonnements ont été générés !");
        } else {
            System.out.println("✅ La base contient déjà des magasins. Aucune insertion nécessaire.");
        }
    }

    // Génération d'un type d'abonnement aléatoire
    private Subscription.SubscriptionType getRandomSubscriptionType() {
        Subscription.SubscriptionType[] types = Subscription.SubscriptionType.values();
        return types[random.nextInt(types.length)];
    }

    // Méthodes pour générer des coordonnées dans un rayon autour de Fès
    private double generateRandomLatitude() {
        double radiusInDegrees = RADIUS_KM / 111.0; // 1 degré ≈ 111 km
        double randomOffset = (random.nextDouble() * 2 - 1) * radiusInDegrees;
        return FES_LAT + randomOffset;
    }

    private double generateRandomLongitude() {
        double radiusInDegrees = RADIUS_KM / (111.0 * Math.cos(Math.toRadians(FES_LAT)));
        double randomOffset = (random.nextDouble() * 2 - 1) * radiusInDegrees;
        return FES_LNG + randomOffset;
    }
}
