package com.application.wa9ti.repositories;

import com.application.wa9ti.models.Owner;
import com.application.wa9ti.models.Service;
import com.application.wa9ti.models.Store;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreServiceRepository  extends JpaRepository<Service, Long> {
    List<Service> findByStoreId(Long storeId);
    List<Service> findByStoreIdAndIsActifTrue(Long storeId);
    boolean existsById(@NonNull Long id);
    int countByStore(Store store);

}
