package com.application.wa9ti.batch;

import com.application.wa9ti.models.Owner;
import com.application.wa9ti.models.Subscription;
import com.application.wa9ti.repositories.OwnerRepository;
import com.application.wa9ti.repositories.StoreRepository;
import com.application.wa9ti.repositories.SubscriptionRepository;
import com.application.wa9ti.services.subscription.SubscriptionService;
import com.application.wa9ti.services.subscription.SubscriptionServiceImp;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionBatchProcessor {
    private final SubscriptionRepository subscriptionRepository;
    private final StoreRepository storeRepository;
    private final SubscriptionService subscriptionService;
    private final OwnerRepository ownerRepository;

    @Transactional
    @Scheduled(cron = "0 * * * * *")  // Exécution tous les jours à minuit
    public void updateSubscriptionStatuses() {
        LocalDate today = LocalDate.now();
        LocalDate expiredDate = today.minusDays(14);

        // Mise à jour des abonnements en PENDING et EXPIRED
        int pendingCount = subscriptionRepository.markSubscriptionsAsPending(today);
        int expiredCount = subscriptionRepository.markSubscriptionsAsExpired(expiredDate);

        // Gestion du passage automatique à un nouvel abonnement
        List<Subscription> subscriptionsToSwitch = subscriptionRepository.findByEndDateAndSwitchSubscriptionTypeNotNull(today);
        List<Subscription> validSubscriptions = new ArrayList<>();

        for (Subscription sub : subscriptionsToSwitch) {
            // 🛠 CHARGER `owner` DANS LA SESSION AVANT `canUpgradeSubscription`
            Owner owner = ownerRepository.findById(sub.getOwner().getId())
                    .orElseThrow(() -> new IllegalStateException("Owner not found"));

            List<String> restrictions = subscriptionService.canUpgradeSubscription(owner.getId(), sub.getSwitchSubscriptionType());

            if (restrictions.isEmpty()) { // ✅ Aucune restriction → on applique le changement
                sub.setType(sub.getSwitchSubscriptionType());
                sub.setSwitchSubscriptionType(null);
                validSubscriptions.add(sub);
            } else {
                sub.setSwitchSubscriptionType(null);
                System.err.println("Le changement d'abonnement de l'utilisateur " + owner.getId() + " a été annulé : " + restrictions);
            }
        }

        subscriptionRepository.saveAll(validSubscriptions);
    }



    @Transactional
    @Scheduled(cron = "0 0 0 1 * *")  // Exécute chaque 1er du mois à 00:00
    public void resetRdvCountForFreeStoresScheduled() {
        int updatedStores = storeRepository.resetRdvCountForFreeStores();
        int updatedSubscriptions = subscriptionRepository.activateExpiredFreeSubscriptions();

        //System.out.println("✅ " + updatedStores + " magasins FREE réinitialisés à 0 rendez-vous !");
        //System.out.println("🔄 " + updatedSubscriptions + " abonnements FREE réactivés !");
    }

}
