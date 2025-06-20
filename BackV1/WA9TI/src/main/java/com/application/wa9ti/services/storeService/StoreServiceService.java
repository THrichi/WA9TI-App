package com.application.wa9ti.services.storeService;

import com.application.wa9ti.dtos.ServiceDTO;
import com.application.wa9ti.dtos.StoreServiceDto;
import com.application.wa9ti.models.Service;
import com.application.wa9ti.models.StoreClosure;

import java.util.List;

public interface StoreServiceService {
    List<Service> getAllServices();
    Service getServiceById(Long id);
    List<Service> getServicesByStoreId(Long storeId);
    List<Service> getActifServicesByStoreId(Long storeId);
    ServiceDTO createService(StoreServiceDto service, Long storeId);
    Service updateService(Long id, StoreServiceDto service);
    void deleteService(Long storeId, Long serviceId);
    void updateServiceStatus(Long id, boolean status);
}
