package com.cropdeal.invoiceservice.repository;

import com.cropdeal.invoiceservice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByOrderId(Long orderId);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    List<Invoice> findByDealerId(Long dealerId);
    List<Invoice> findByFarmerId(Long farmerId);
}