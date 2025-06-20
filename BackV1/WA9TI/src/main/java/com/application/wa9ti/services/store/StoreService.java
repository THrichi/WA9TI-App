package com.application.wa9ti.services.store;

import com.application.wa9ti.dtos.*;
import com.application.wa9ti.models.Store;
import com.application.wa9ti.models.StoreClosure;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface StoreService {
    List<Store> getAllStores();
    Optional<Store> getStoreById(Long id);
    Store updateStore(Long id, Store store);
    void updateStoreImage(Long id, String imageURL);
    void deleteStore(Long storeId);
    Store updateOpeningHours(Long id, List<OpeningHoursDTO> openingHours);
    Store updateStoreInfo(Long id, StoreInfosDto storeInfosDto);
    void addImageToGallery(Long storeId, List<String> newImageUrls);
    void removeImageFromGallery(Long storeId, String imageUrl);
    StoreClosure addClosureToStore(Long storeId, StoreClosureDTO closure);
    List<StoreClosure> getClosuresStore(Long storeId);
    void deleteClosureById(Long closureId);
    //List<StoreSearchDTO> findStoresNearby(Double latitude, Double longitude, Double radius, String name, String type, String serviceName);
    List<StoreSearchDTO> findStoresNearby(Double latitude, Double longitude, Double radius, String keyword);
    boolean isStoreUrlTaken(String storeUrl);
    StoreClientsDTO findByStoreUrl(String storeUrl);
    Page<ClientStoreDTO> getClientsByStore(Long storeId, String keyword, int page, int size);
    void toggleBlacklist(Long storeId,Long clientId);
    void createStore(StoreCreateDTO storeDTO);
    List<StoreSimpleDTO> getOwnerStores(Long ownerId);

}
