package com.application.wa9ti.services.owner;

import com.application.wa9ti.dtos.*;
import com.application.wa9ti.models.Owner;
import com.application.wa9ti.models.Subscription;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.temporal.ChronoUnit;
import java.util.List;

public interface OwnerService {
    List<Owner> getAllOwners();
    Owner getOwnerById(Long id);
    Owner updateOwner(Long id, Owner updatedOwner);
    void deleteOwner(Long id);
    boolean canCancelAccount(Long ownerId);
    Owner cancelSubscription(Long ownerId); // Annuler la souscription
    boolean isSubscriptionValid(Long ownerId);// Vérifier la validité de la souscription
    void updateGeneralInfo(Long id, GeneralOwnerInfoDto generalInfoDto);
    //void updateEmail(Long id, EmailDto emailDto);
    //void updatePhone(Long id, PhoneDto phoneDto);
    OwnerDto getAuthenticatedOwner(String email);
    void updateOwnerImage(Long id, String imageURL);
    //Owner createOwnerWithStore(OwnerWithStoreDTO dto);
    void assignOwnerAsEmployee(Long ownerId, Long storeId);
    void removeOwnerAsEmployee(Long ownerId, Long storeId);
    Owner createOwner(OwnerRegistrationDto dto);
    SubscriptionDto getSubscriptionByOwnerId(Long storeId,Long ownerId);
    Page<InvoiceDto> getInvoicesByOwnerId(Long ownerId, int page, int size, String startDate, String endDate);
    OwnerStatsDTO getOwnerStats(Long ownerId);
}
