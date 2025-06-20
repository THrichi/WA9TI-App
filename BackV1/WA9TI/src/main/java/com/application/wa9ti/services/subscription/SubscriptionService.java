package com.application.wa9ti.services.subscription;

import com.application.wa9ti.dtos.CalculateSubscriptionAmountDTO;
import com.application.wa9ti.dtos.InvoiceDto;
import com.application.wa9ti.dtos.SubscriptionAmountResponseDTO;
import com.application.wa9ti.dtos.SubscriptionPaymentDTO;
import com.application.wa9ti.models.Subscription;

import java.util.List;

public interface SubscriptionService {
    InvoiceDto updateSubscriptionAfterPayment(SubscriptionPaymentDTO subscriptionPaymentDTO);
    SubscriptionAmountResponseDTO calculateSubscriptionAmount(CalculateSubscriptionAmountDTO amountDTO);
    void downgradeToFree(Long ownerId);
    Subscription.SubscriptionStatus getSubscriptionStatusByStoreId(Long storeId);
    List<String> canUpgradeSubscription(Long ownerId, Subscription.SubscriptionType newSubscriptionType);
    void updateSwitchSubscription(Long ownerId, Subscription.SubscriptionType newType);
    SubscriptionAmountResponseDTO calculateUpgradeAmount(CalculateSubscriptionAmountDTO amountDTO);
    boolean canAddEmployee(Long storeId);
    boolean canAddService(Long storeId);
    boolean canAddStore(Long ownerId);
}
