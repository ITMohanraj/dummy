package com.cropdeal.invoiceservice;

import com.cropdeal.invoiceservice.controller.InvoiceController;
import com.cropdeal.invoiceservice.entity.Invoice;
import com.cropdeal.invoiceservice.entity.InvoiceStatus;
import com.cropdeal.invoiceservice.service.InvoicePdfService;
import com.cropdeal.invoiceservice.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InvoiceController.class)
@AutoConfigureMockMvc(addFilters = false)
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvoiceService invoiceService;

    @MockBean
    private InvoicePdfService pdfService;

    @Test
    void testGetInvoiceByOrderId() throws Exception {
        Invoice inv = Invoice.builder()
                .id(1L)
                .orderId(10L)
                .invoiceNumber("INV-10-1234")
                .dealerId(20L)
                .farmerId(30L)
                .cropName("Tomato")
                .quantityKg(100.0)
                .pricePerKg(new BigDecimal("25.00"))
                .totalAmount(new BigDecimal("2500.00"))
                .invoiceStatus(InvoiceStatus.GENERATED)
                .build();

        Mockito.when(invoiceService.getByOrderId(10L)).thenReturn(inv);
        Mockito.when(invoiceService.mapToResponse(any())).thenCallRealMethod();

        mockMvc.perform(get("/api/v1/invoices/order/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceNumber").value("INV-10-1234"))
                .andExpect(jsonPath("$.orderId").value(10L));
    }

    @Test
    void testDownloadInvoicePdf() throws Exception {
        Invoice inv = Invoice.builder()
                .id(1L)
                .orderId(10L)
                .invoiceNumber("INV-10-1234")
                .build();

        byte[] samplePdf = "%PDF-1.4 sample pdf content".getBytes();

        Mockito.when(invoiceService.getByOrderId(10L)).thenReturn(inv);
        Mockito.when(pdfService.generateInvoicePdf(any())).thenReturn(samplePdf);

        mockMvc.perform(get("/api/v1/invoices/order/10/download"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=Invoice-INV-10-1234.pdf"));
    }
}