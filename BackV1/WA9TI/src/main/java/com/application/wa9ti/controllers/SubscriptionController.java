package com.application.wa9ti.controllers;

import com.application.wa9ti.dtos.CalculateSubscriptionAmountDTO;
import com.application.wa9ti.dtos.InvoiceDto;
import com.application.wa9ti.dtos.SubscriptionAmountResponseDTO;
import com.application.wa9ti.dtos.SubscriptionPaymentDTO;
import com.application.wa9ti.models.Invoice;
import com.application.wa9ti.models.Subscription;
import com.application.wa9ti.repositories.InvoiceRepository;
import com.application.wa9ti.services.auth.AuthorizationService;
import com.application.wa9ti.services.subscription.SubscriptionService;
import com.application.wa9ti.services.subscription.SubscriptionServiceImp;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/subscription")
@AllArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final AuthorizationService authorizationService;
    private final InvoiceRepository invoiceRepository;

    @PostMapping("/{storeId}/update-after-payment")
    public ResponseEntity<InvoiceDto> updateSubscription(@PathVariable Long storeId, @RequestBody SubscriptionPaymentDTO dto) {
        authorizationService.canAccessStore(storeId);
        authorizationService.isTheOwner(dto.ownerId());
        InvoiceDto invoiceDto = subscriptionService.updateSubscriptionAfterPayment(dto);
        return ResponseEntity.ok(invoiceDto);
    }


    @PostMapping("/calculate-amount")
    public ResponseEntity<SubscriptionAmountResponseDTO> getSubscriptionAmount(
            @RequestBody CalculateSubscriptionAmountDTO amountDTO) {
        authorizationService.isTheOwner(amountDTO.ownerId());
        return ResponseEntity.ok(subscriptionService.calculateSubscriptionAmount(amountDTO));
    }

    @PostMapping("/calculate-upgrade-amount")
    public ResponseEntity<SubscriptionAmountResponseDTO> upgradeSubscription(
            @RequestBody CalculateSubscriptionAmountDTO upgradeRequest) {
        authorizationService.isTheOwner(upgradeRequest.ownerId());
        SubscriptionAmountResponseDTO response = subscriptionService.calculateUpgradeAmount(upgradeRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{ownerId}/downgrade-to-free")
    public ResponseEntity<Void> downgradeToFree(@PathVariable Long ownerId) {
        authorizationService.isTheOwner(ownerId);
        subscriptionService.downgradeToFree(ownerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download/{ownerId}/{invoiceNumber}")
    public ResponseEntity<Resource> downloadInvoice(
            @PathVariable Long ownerId,
            @PathVariable String invoiceNumber) {
        authorizationService.isTheOwner(ownerId);
        try {
            // Vérifier si la facture existe et appartient bien à ce ownerId
            Optional<Invoice> invoiceOpt = invoiceRepository.findByInvoiceNumber(invoiceNumber);

            if (invoiceOpt.isEmpty() || !invoiceOpt.get().getOwner().getId().equals(ownerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(null);
            }

            // Construire le chemin du fichier PDF
            Path pdfPath = Paths.get("invoices/" + ownerId + "/" + invoiceNumber + ".pdf");
            Resource resource = new UrlResource(pdfPath.toUri());

            // Vérifier si le fichier existe et est lisible
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }

            // Définir les headers pour forcer le téléchargement
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }


    @GetMapping("/check-upgrade")
    public ResponseEntity<Map<String, Object>> checkUpgrade(
            @RequestParam Long ownerId,
            @RequestParam Subscription.SubscriptionType newType) {
        authorizationService.isTheOwner(ownerId);
        List<String> restrictions = subscriptionService.canUpgradeSubscription(ownerId, newType);
        boolean canUpgrade = restrictions.isEmpty();

        Map<String, Object> response = Map.of(
                "canUpgrade", canUpgrade,
                "restrictions", restrictions
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/switch")
    public ResponseEntity<Void> updateSwitchSubscription(
            @RequestParam Long ownerId,
            @RequestParam Subscription.SubscriptionType newType) {
        authorizationService.isTheOwner(ownerId);
        subscriptionService.updateSwitchSubscription(ownerId, newType);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/can-add-employee/{storeId}")
    public ResponseEntity<Boolean> canAddEmployee(@PathVariable Long storeId)
    {
        authorizationService.canAccessStore(storeId);
        boolean canAdd = subscriptionService.canAddEmployee(storeId);
        return ResponseEntity.ok(canAdd);
    }


    @GetMapping("/can-add-service/{storeId}")
    public ResponseEntity<Boolean> canAddService(@PathVariable Long storeId)
    {
        authorizationService.canAccessStore(storeId);
        boolean canAdd = subscriptionService.canAddService(storeId);
        return ResponseEntity.ok(canAdd);
    }

    @GetMapping("/can-add-store/{ownerId}")
    public ResponseEntity<Boolean> canAddStore(@PathVariable Long ownerId)
    {
        authorizationService.isTheOwner(ownerId);
        boolean canAdd = subscriptionService.canAddStore(ownerId);
        return ResponseEntity.ok(canAdd);
    }

}
