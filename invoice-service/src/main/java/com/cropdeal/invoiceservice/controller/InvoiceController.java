package com.cropdeal.invoiceservice.controller;

import com.cropdeal.invoiceservice.dto.InvoiceResponse;
import com.cropdeal.invoiceservice.entity.FarmerReceipt;
import com.cropdeal.invoiceservice.entity.Invoice;
import com.cropdeal.invoiceservice.service.InvoicePdfService;
import com.cropdeal.invoiceservice.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoice Service", description = "Tax Invoices, Farmer Receipts & PDF Generation APIs")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoicePdfService pdfService;

    @GetMapping("/{invoiceId}")
    @Operation(summary = "Get invoice by Invoice ID")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable("invoiceId") Long invoiceId) {
        return ResponseEntity.ok(invoiceService.mapToResponse(invoiceService.getById(invoiceId)));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get invoice for specific Order ID")
    public ResponseEntity<InvoiceResponse> getInvoiceByOrderId(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(invoiceService.mapToResponse(invoiceService.getByOrderId(orderId)));
    }

    @GetMapping("/order/{orderId}/download")
    @Operation(summary = "Download official Invoice PDF for an order")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable("orderId") Long orderId) {
        Invoice invoice = invoiceService.getByOrderId(orderId);
        byte[] pdfBytes = pdfService.generateInvoicePdf(invoice);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Invoice-" + invoice.getInvoiceNumber() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/dealer/{dealerId}")
    @Operation(summary = "List all invoices for a dealer")
    public ResponseEntity<List<InvoiceResponse>> getDealerInvoices(@PathVariable("dealerId") Long dealerId) {
        return ResponseEntity.ok(invoiceService.getByDealerId(dealerId));
    }

    @GetMapping("/farmer/{farmerId}")
    @Operation(summary = "List all invoices for a farmer")
    public ResponseEntity<List<InvoiceResponse>> getFarmerInvoices(@PathVariable("farmerId") Long farmerId) {
        return ResponseEntity.ok(invoiceService.getByFarmerId(farmerId));
    }

    // --- Backward Compatibility Endpoints for existing clients ---
    @GetMapping("/dealer/order/{orderId}")
    @Operation(summary = "Get Dealer Invoice for completed purchase (Backward Compatible)")
    public ResponseEntity<InvoiceResponse> getDealerInvoice(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(invoiceService.mapToResponse(invoiceService.getByOrderId(orderId)));
    }

    @GetMapping("/farmer/order/{orderId}")
    @Operation(summary = "Get Farmer Payment Receipt (Backward Compatible)")
    public ResponseEntity<FarmerReceipt> getFarmerReceipt(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(invoiceService.getFarmerReceiptByOrderId(orderId));
    }
}