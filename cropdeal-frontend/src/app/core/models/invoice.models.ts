export interface InvoiceResponse {
  invoiceId: number;
  invoiceNumber: string;
  orderId: number;
  dealerId: number;
  farmerId: number;
  cropName: string;
  quantityKg: number;
  pricePerKg: number;
  subtotal: number;
  taxAmount: number;
  platformFee: number;
  totalAmount: number;
  paymentStatus: string;
  issuedAt: string;
}

export interface FarmerReceipt {
  receiptId: number;
  orderId: number;
  farmerId: number;
  cropName: string;
  grossAmount: number;
  netPayout: number;
  payoutStatus: string;
  payoutDate: string;
}
