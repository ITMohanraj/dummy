package com.cropdeal.invoiceservice.service;

import com.cropdeal.invoiceservice.entity.Invoice;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class InvoicePdfService {

    public byte[] generateInvoicePdf(Invoice invoice) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            // Font Styles
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(46, 125, 50));
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

            // Header Banner
            Paragraph title = new Paragraph("CROPDEAL TAX INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("Agricultural Direct Marketplace & Intelligence Platform", subTitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);
            document.add(new Paragraph(" "));

            // Invoice Meta Table
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setWidths(new float[]{1, 1});

            PdfPCell c1 = new PdfPCell();
            c1.setBorder(Rectangle.NO_BORDER);
            c1.addElement(new Paragraph("Invoice No: " + invoice.getInvoiceNumber(), boldFont));
            c1.addElement(new Paragraph("Order ID: #" + invoice.getOrderId(), bodyFont));
            String dateStr = invoice.getIssuedAt() != null ?
                    invoice.getIssuedAt().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")) : "N/A";
            c1.addElement(new Paragraph("Date: " + dateStr, bodyFont));
            c1.addElement(new Paragraph("Payment Ref: " + (invoice.getPaymentReference() != null ? invoice.getPaymentReference() : "PAID"), bodyFont));
            metaTable.addCell(c1);

            PdfPCell c2 = new PdfPCell();
            c2.setBorder(Rectangle.NO_BORDER);
            c2.addElement(new Paragraph("Bill To (Dealer):", boldFont));
            c2.addElement(new Paragraph("Dealer ID: #" + invoice.getDealerId() + " (" + (invoice.getDealerName() != null ? invoice.getDealerName() : "Registered Dealer") + ")", bodyFont));
            c2.addElement(new Paragraph("Sold By (Farmer):", boldFont));
            c2.addElement(new Paragraph("Farmer ID: #" + invoice.getFarmerId() + " (" + (invoice.getFarmerName() != null ? invoice.getFarmerName() : "Registered Farmer") + ")", bodyFont));
            metaTable.addCell(c2);

            document.add(metaTable);
            document.add(new Paragraph(" "));

            // Items Table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 1.5f, 1.5f, 1.5f, 2});

            // Headers
            String[] headers = {"Item Description", "Qty (KG)", "Price/KG", "Tax/Fee", "Total Amount"};
            for (String h : headers) {
                PdfPCell headerCell = new PdfPCell(new Phrase(h, headerFont));
                headerCell.setBackgroundColor(new Color(46, 125, 50));
                headerCell.setPadding(6);
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(headerCell);
            }

            // Row 1: Crop Item
            table.addCell(new Phrase(invoice.getCropName() != null ? invoice.getCropName() : "Produce", bodyFont));
            table.addCell(new Phrase(String.valueOf(invoice.getQuantityKg()), bodyFont));
            table.addCell(new Phrase("₹" + invoice.getPricePerKg(), bodyFont));
            table.addCell(new Phrase("₹" + (invoice.getPlatformFee() != null ? invoice.getPlatformFee() : "0.00"), bodyFont));
            table.addCell(new Phrase("₹" + invoice.getCropSubtotal(), bodyFont));

            // Row 2: Delivery if applicable
            if (invoice.getDeliveryCharge() != null && invoice.getDeliveryCharge().doubleValue() > 0) {
                table.addCell(new Phrase("Logistics & Transport Fee", bodyFont));
                table.addCell(new Phrase("-", bodyFont));
                table.addCell(new Phrase("-", bodyFont));
                table.addCell(new Phrase("-", bodyFont));
                table.addCell(new Phrase("₹" + invoice.getDeliveryCharge(), bodyFont));
            }

            document.add(table);
            document.add(new Paragraph(" "));

            // Grand Total Section
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(100);
            totalTable.setWidths(new float[]{3, 2});

            PdfPCell tc1 = new PdfPCell(new Phrase("Payment Status: " + (invoice.getPaymentStatus() != null ? invoice.getPaymentStatus() : "CONFIRMED"), boldFont));
            tc1.setBorder(Rectangle.NO_BORDER);
            totalTable.addCell(tc1);

            PdfPCell tc2 = new PdfPCell(new Phrase("Grand Total: ₹" + invoice.getTotalAmount(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(46, 125, 50))));
            tc2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tc2.setBorder(Rectangle.NO_BORDER);
            totalTable.addCell(tc2);

            document.add(totalTable);
            document.add(new Paragraph(" "));

            // Footer
            Paragraph footer = new Paragraph("This is a computer generated invoice valid for pickup and delivery verification under CropDeal platform terms.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate invoice PDF for Invoice #{}: {}", invoice.getInvoiceNumber(), e.getMessage());
            throw new RuntimeException("Error rendering invoice PDF: " + e.getMessage());
        }
    }
}