package com.application.wa9ti.dtos;

import com.application.wa9ti.models.Invoice;
import com.application.wa9ti.models.Subscription.SubscriptionType;
import com.application.wa9ti.models.Subscription.BillingType;
import com.application.wa9ti.models.Invoice.PaymentMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record InvoiceDto(
        Long id,
        String ownerName,
        String ownerAddress,
        String ownerICE,
        SubscriptionType subscriptionType,
        BillingType billingType,
        LocalDate startDate,
        LocalDate endDate,
        Double amountHT,
        Double amountTVA,
        Double amountTTC,
        PaymentMethod paymentMethod,
        String invoiceNumber,
        LocalDateTime invoiceDate,
        String pdfPath,
        LocalDateTime createdAt
) {
    public static InvoiceDto fromEntity(Invoice invoice) {
        return new InvoiceDto(
                invoice.getId(),
                invoice.getOwnerName(),
                invoice.getOwnerAddress(),
                invoice.getOwnerICE(),
                invoice.getSubscriptionType(),
                invoice.getBillingType(),
                invoice.getStartDate(),
                invoice.getEndDate(),
                invoice.getAmountHT(),
                invoice.getAmountTVA(),
                invoice.getAmountTTC(),
                invoice.getPaymentMethod(),
                invoice.getInvoiceNumber(),
                invoice.getInvoiceDate(),
                invoice.getPdfPath(),
                invoice.getCreatedAt()
        );
    }
}
