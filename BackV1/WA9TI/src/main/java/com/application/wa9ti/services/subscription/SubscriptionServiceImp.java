package com.application.wa9ti.services.subscription;

import com.application.wa9ti.configuration.SubscriptionConfig;
import com.application.wa9ti.dtos.CalculateSubscriptionAmountDTO;
import com.application.wa9ti.dtos.InvoiceDto;
import com.application.wa9ti.dtos.SubscriptionAmountResponseDTO;
import com.application.wa9ti.dtos.SubscriptionPaymentDTO;
import com.application.wa9ti.models.Invoice;
import com.application.wa9ti.models.Owner;
import com.application.wa9ti.models.Store;
import com.application.wa9ti.models.Subscription;
import com.application.wa9ti.repositories.InvoiceRepository;
import com.application.wa9ti.repositories.OwnerRepository;
import com.application.wa9ti.repositories.StoreRepository;
import com.application.wa9ti.repositories.SubscriptionRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@AllArgsConstructor
public class SubscriptionServiceImp implements SubscriptionService {

    private final OwnerRepository ownerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoicePdfService invoicePdfService;
    private final StoreRepository storeRepository;

    @Override
    @Transactional
    public InvoiceDto updateSubscriptionAfterPayment(SubscriptionPaymentDTO dto) {
        Owner owner = ownerRepository.findById(dto.ownerId())
                .orElseThrow(() -> new IllegalArgumentException("Propriétaire introuvable"));

        Subscription subscription = owner.getSubscription();
        if (subscription == null) {
            subscription = new Subscription();
            subscription.setOwner(owner);
        }
        LocalDate startInvoiceDate;
        // Définir les nouvelles dates d'abonnement
        if(subscription.getEndDate() == null)
        {
            startInvoiceDate = LocalDate.now();
            if(subscription.isNew())
            {
                startInvoiceDate = LocalDate.now().plusDays(30);
                subscription.setNew(false);
            }
        }else{
            startInvoiceDate = subscription.getEndDate();
        }
        LocalDate EndInvoiceDate = dto.nextBillingDate();

        // Mettre à jour l'abonnement
        subscription.setType(dto.type());
        subscription.setBillingType(dto.billingType());
        subscription.setStartDate(startInvoiceDate);
        subscription.setEndDate(EndInvoiceDate);
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        subscription.setDeletionDate(subscription.getEndDate().plusMonths(2));
        if(dto.type() != null)
        {
            subscription.setType(dto.type());
        }
        if(subscription.getSwitchSubscriptionType() != null)
        {
            subscription.setSwitchSubscriptionType(null);
        }
        subscriptionRepository.save(subscription);

        // Générer une facture
        Invoice invoice = new Invoice();
        invoice.setOwner(owner);
        invoice.setOwnerName(owner.getUser().getName());
        invoice.setOwnerAddress("Adresse par défaut");
        invoice.setOwnerICE("ICE-XXXXXX");

        invoice.setSubscriptionType(dto.type());
        invoice.setBillingType(dto.billingType());
        invoice.setStartDate(startInvoiceDate);
        invoice.setEndDate(EndInvoiceDate);

        double amount = dto.totalToPay();
        // Calcul des montants (exemple avec 20% TVA)
        double amountHT = amount / 1.2;
        double amountTVA = amount - amountHT;

        invoice.setAmountHT(roundToTwoDecimals(amountHT));
        invoice.setAmountTVA(roundToTwoDecimals(amountTVA));
        invoice.setAmountTTC(roundToTwoDecimals(invoice.getAmountTVA()+invoice.getAmountHT()));
        invoice.setPaymentMethod(dto.paymentMethod());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String datePart = startInvoiceDate.format(dateFormatter);
        Random random = new Random();
        String invoiceNumber;

        do {
            String randomPart = String.format("%05d", random.nextInt(100000)); // 00000 - 99999
            invoiceNumber = "INV-" + datePart + "-" + randomPart;
        } while (invoiceRepository.existsByInvoiceNumber(invoiceNumber)); // Vérification en base

        invoice.setInvoiceNumber(invoiceNumber);

        // Générer et enregistrer le PDF
        Map<String, Object> data = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        data.put("invoiceNumber", invoice.getInvoiceNumber());
        data.put("invoiceDate", invoice.getInvoiceDate().format(formatter));
        data.put("ownerName", invoice.getOwnerName());
        data.put("ownerAddress", invoice.getOwnerAddress());
        data.put("ownerICE", invoice.getOwnerICE());
        data.put("subscriptionType", invoice.getSubscriptionType());
        data.put("billingType", invoice.getBillingType());
        data.put("startDate", invoice.getStartDate().format(formatter));
        data.put("endDate", invoice.getEndDate().format(formatter));
        data.put("amountHT", invoice.getAmountHT());
        data.put("amountTVA", invoice.getAmountTVA());
        data.put("amountTTC", invoice.getAmountTTC());
        data.put("paymentMethod", invoice.getPaymentMethod());

        String pdfPath = invoicePdfService.generateInvoicePdf(invoice.getInvoiceNumber(),dto.ownerId(), data);
        invoice.setPdfPath(pdfPath);

        invoiceRepository.save(invoice);

        return InvoiceDto.fromEntity(invoice);
    }

    public SubscriptionAmountResponseDTO calculateSubscriptionAmount(CalculateSubscriptionAmountDTO amountDTO) {
        Owner owner = ownerRepository.findById(amountDTO.ownerId())
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));
        Subscription subscription = owner.getSubscription();
        if(subscription == null){
            throw new IllegalArgumentException("Subscription not found");
        }

        LocalDate startDate;
        // 1. Récupérer la date de départ (fin d'abonnement actuel)
        if(subscription.getEndDate() == null)
        {
            startDate = LocalDate.now();
            if(subscription.isNew())
            {
                startDate = LocalDate.now().plusDays(30);
            }
        }else{
            startDate = subscription.getEndDate();
        }
        // 2. Vérifier le prix mensuel
        double monthlyPrice = 0;
        if(amountDTO.subscriptionType() == null)
        {
            monthlyPrice = SubscriptionConfig.getPrice(subscription.getType());
        }else{
            monthlyPrice = SubscriptionConfig.getPrice(amountDTO.subscriptionType());
        }

        if (monthlyPrice == 0.0) {
            throw new IllegalArgumentException("Invalid subscription type");
        }

        // 3. Calcul du prorata du mois en cours, si startDate n’est pas le 1er
        YearMonth currentMonth = YearMonth.from(startDate);
        int totalDaysInMonth = currentMonth.lengthOfMonth();
        int daysRemaining = totalDaysInMonth - startDate.getDayOfMonth();

        double prorata = 0.0;
        if (startDate.getDayOfMonth() > 1) {
            prorata = (monthlyPrice * daysRemaining) / totalDaysInMonth;
            // Ex: si startDate=17 (janvier), on prend ~15 jours sur 31
        }

        // 4. Vérifier si on inclut le mois suivant (si startDate est dans les 5 derniers jours du mois)
        boolean includesNextMonth = false;
        if (startDate.getDayOfMonth() > (totalDaysInMonth - 5)) {
            includesNextMonth = true;
        }


        // 5. Déterminer le “billedMonth” (celui qu’on facture réellement dans la foulée)
        String billedMonth = includesNextMonth
                ? startDate.plusMonths(1).getMonth().toString()
                : startDate.getMonth().toString();

        // 6. Calculer la date de la prochaine facturation (1er du mois suivant ou +2 mois si on inclut le mois prochain)
        LocalDate nextBillingDate = includesNextMonth
                ? startDate.plusMonths(2).withDayOfMonth(1)
                : startDate.plusMonths(1).withDayOfMonth(1);

        // 7. Montant total initial (prorata + éventuellement un mois complet)
        double finalAmount = includesNextMonth ? (prorata + monthlyPrice) : prorata;

        // 8. Choix de la clé de traduction par défaut (si pas de retard)
        String translationKey;
        if (startDate.getDayOfMonth() == 1) {
            translationKey = "subscription.message.only_monthly"; // Cas sans prorata
            finalAmount = monthlyPrice;
        } else {
            translationKey = includesNextMonth
                    ? "subscription.message.with_prorata"
                    : "subscription.message.only_prorata";
        }



        // 10. Retourner la réponse
        return new SubscriptionAmountResponseDTO(
                roundToTwoDecimals(monthlyPrice),
                roundToTwoDecimals(prorata),
                roundToTwoDecimals(finalAmount),
                nextBillingDate,
                translationKey,
                daysRemaining,
                startDate.getMonth().toString(),
                billedMonth,
                nextBillingDate.getMonth().toString()
        );
    }




    @Transactional
    public void downgradeToFree(Long ownerId) {
        Optional<Owner> ownerOpt = ownerRepository.findById(ownerId);
        if (ownerOpt.isEmpty()) {
            throw new IllegalArgumentException("Propriétaire introuvable");
        }

        Owner owner = ownerOpt.get();
        Optional<Subscription> subscriptionOpt = subscriptionRepository.findByOwner(owner);
        if (subscriptionOpt.isEmpty()) {
            throw new IllegalArgumentException("Aucun abonnement trouvé pour ce propriétaire");
        }

        Subscription subscription = subscriptionOpt.get();

        // Mise à jour des valeurs
        subscription.setType(Subscription.SubscriptionType.FREE);
        subscription.setEndDate(null);
        subscription.setDeletionDate(null);
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE); // On considère l'abonnement FREE comme actif

        subscriptionRepository.save(subscription);
    }


    @Override
    public Subscription.SubscriptionStatus getSubscriptionStatusByStoreId(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        if (store.getOwner() == null || store.getOwner().getSubscription() == null) {
            throw new IllegalArgumentException("Subscription not found for this store");
        }

        return store.getOwner().getSubscription().getStatus();
    }

    @Override
    public List<String> canUpgradeSubscription(Long ownerId, Subscription.SubscriptionType newSubscriptionType) {
        List<String> restrictions = new ArrayList<>();

        // Récupérer les limites du nouvel abonnement
        int maxStores = SubscriptionConfig.getMaxStores(newSubscriptionType);
        int maxEmployeesPerStore = SubscriptionConfig.getMaxEmployees(newSubscriptionType);
        int maxServicesPerStore = SubscriptionConfig.getMaxServices(newSubscriptionType);

        // Récupérer les magasins de l'owner
        List<Store> stores = storeRepository.findByOwnerId(ownerId);
        int totalStores = stores.size();

        // Vérification du nombre de magasins
        if (totalStores > maxStores) {
            restrictions.add("Le nombre de magasins actuel (" + totalStores + ") dépasse la limite de l'abonnement " + newSubscriptionType + " (" + maxStores + ").");
        }

        // Vérification pour chaque magasin
        for (Store store : stores) {
            int storeEmployees = store.getEmployeeStores().size();
            int storeServices = store.getServices().size();

            if (!canAddEmployee(store.getId())) {
                restrictions.add("Le magasin '" + store.getName() + "' a " + storeEmployees + " employés, mais la limite pour l'abonnement " + newSubscriptionType + " est de " + maxEmployeesPerStore + " employés par magasin.");
            }

            if (!canAddService(store.getId())) {
                restrictions.add("Le magasin '" + store.getName() + "' a " + storeServices + " services, mais la limite pour l'abonnement " + newSubscriptionType + " est de " + maxServicesPerStore + " services par magasin.");
            }
        }

        return restrictions;
    }


    @Transactional
    @Override
    public void updateSwitchSubscription(Long ownerId, Subscription.SubscriptionType newType) {
        Subscription subscription = subscriptionRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Aucun abonnement trouvé pour cet utilisateur"));

        if (!subscription.getStatus().equals(Subscription.SubscriptionStatus.ACTIVE)) { // ✅ Si today > endDate, on applique immédiatement
            throw new IllegalArgumentException("Changement d'abonnement impossible, vous devez payer votre facture avant");
        } else { // 🔄 Sinon (today ≤ endDate), on programme le changement
            subscription.setSwitchSubscriptionType(newType);
        }

        subscriptionRepository.save(subscription);
    }

    @Override
    public SubscriptionAmountResponseDTO calculateUpgradeAmount(CalculateSubscriptionAmountDTO amountDTO) {
        // 1. Vérifier l'existence de l’utilisateur et de son abonnement
        Owner owner = ownerRepository.findById(amountDTO.ownerId())
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));
        Subscription currentSubscription = owner.getSubscription();
        if (currentSubscription == null || currentSubscription.getEndDate() == null) {
            throw new IllegalArgumentException("No active subscription found");
        }

        //LocalDate today = LocalDate.of(2025,3,27);
        LocalDate today = LocalDate.now();
        LocalDate currentEndDate = currentSubscription.getEndDate();

        // 2. Vérifier que l'abonnement courant est toujours valide
        if (today.isAfter(currentEndDate)) {
            throw new IllegalArgumentException("Current subscription already expired");
        }

        // 3. Récupérer les prix
        double currentMonthlyPrice = SubscriptionConfig.getPrice(currentSubscription.getType());
        double newMonthlyPrice = SubscriptionConfig.getPrice(amountDTO.subscriptionType());

        // 4. Vérifier qu'il s'agit bien d'un upgrade
        if (newMonthlyPrice <= currentMonthlyPrice) {
            throw new IllegalArgumentException("Upgrade must be to a higher-tier subscription");
        }

        // 5. Calcul du nombre de jours restants dans le mois en cours
        // Ici, ChronoUnit.DAYS.between(today, currentEndDate) compte les jours entiers de today (inclus) à currentEndDate (exclus)
        long baseRemainingDays = ChronoUnit.DAYS.between(today, currentEndDate);
        if (baseRemainingDays <= 0) {
            throw new IllegalArgumentException("Current subscription already expired or ends today");
        }

        // 6. Calcul du coût journalier pour chaque abonnement (en se basant sur le nombre de jours dans le mois en cours)
        YearMonth currentMonth = YearMonth.from(today);
        int daysInMonth = currentMonth.lengthOfMonth();
        double currentDailyRate = currentMonthlyPrice / daysInMonth;
        double newDailyRate = newMonthlyPrice / daysInMonth;
        double diffPerDay = newDailyRate - currentDailyRate;

        // 7. Application de la règle des 5 derniers jours du mois
        // Si baseRemainingDays est <= 5, on facture le prorata pour ces jours et le mois complet suivant au nouveau tarif
        boolean includesNextMonth = (baseRemainingDays <= 5);
        double totalToPay;
        int daysCount; // Pour l'affichage du nombre de jours facturés
        double prorataDifference;
        String billedMonth ;
        if (includesNextMonth) {
            // Calcul du prorata pour les jours restants en cours (ex. du 27 au 31 mars)
            prorataDifference = diffPerDay * baseRemainingDays;
            // Ajout du mois complet (facturé au tarif plein du nouvel abonnement)
            totalToPay = prorataDifference + newMonthlyPrice;
            // Pour information, on peut indiquer le nombre de jours facturés : jours restants + jours du mois complet
            daysCount = (int) (baseRemainingDays);
            billedMonth = today.plusMonths(1).getMonth().toString();
        } else {
            totalToPay = diffPerDay * baseRemainingDays;
            prorataDifference = totalToPay;
            daysCount = (int) baseRemainingDays;
            billedMonth = today.getMonth().toString();
        }

        // 8. Calcul de la prochaine date de facturation
        // Si on inclut le mois suivant, nextBillingDate = 1er du mois suivant l'extension (mois +1)
        LocalDate nextBillingDate = includesNextMonth
                ? currentEndDate.plusMonths(1).withDayOfMonth(1)
                : currentEndDate;

        // 9. Détermination de la clé de traduction
        String translationKey = includesNextMonth
                ? "subscription.message.upgrade_with_prorata"
                : "subscription.message.upgrade_only_prorata";

        // 10. Récupération des informations de mois pour l'affichage
        String nextBillingMonth = nextBillingDate.getMonth().toString();

        // 11. Retour de la réponse
        return new SubscriptionAmountResponseDTO(
                roundToTwoDecimals(newMonthlyPrice),
                roundToTwoDecimals(prorataDifference),
                roundToTwoDecimals(totalToPay),
                nextBillingDate,
                translationKey,
                daysCount,
                today.getMonth().toString(),
                billedMonth,
                nextBillingMonth
        );
    }


    public boolean canAddEmployee(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Magasin introuvable avec l'ID : " + storeId));

        var owner = store.getOwner();
        if (owner == null || owner.getSubscription() == null) {
            return false; // Aucun abonnement actif
        }

        int maxEmployees = SubscriptionConfig.getMaxEmployees(owner.getSubscription().getType());

        // Exclure le propriétaire du comptage des employés
        int currentEmployeeCount = (int) store.getEmployeeStores().stream()
                .filter(employeeStore -> !employeeStore.getEmployee().getUser().getId().equals(owner.getUser().getId()))
                .count();

        return currentEmployeeCount < maxEmployees;
    }

    @Override
    public boolean canAddService(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Magasin introuvable avec l'ID : " + storeId));

        var owner = store.getOwner();
        if (owner == null || owner.getSubscription() == null) {
            return false; // Aucun abonnement actif
        }

        int maxServices = SubscriptionConfig.getMaxServices(owner.getSubscription().getType());

        // Exclure le propriétaire du comptage des employés
        int currentServiceCount = store.getServices().size();
        return currentServiceCount < maxServices;
    }
    @Override
    public boolean canAddStore(Long ownerId) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner introuvable avec l'ID : " + ownerId));

        if ( owner.getSubscription() == null) {
            return false; // Aucun abonnement actif
        }

        int maxStores = SubscriptionConfig.getMaxStores(owner.getSubscription().getType());

        // Exclure le propriétaire du comptage des employés
        int currentStoresCount = owner.getStores().size();
        return currentStoresCount < maxStores;
    }




    private double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP) // Arrondi standard
                .doubleValue();
    }


}
