package com.cropdeal.walletservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscrowReleaseRequest {
    @NotNull
    private Long deliveryId;
    @NotNull
    private Long deliveryPartnerId;
}
