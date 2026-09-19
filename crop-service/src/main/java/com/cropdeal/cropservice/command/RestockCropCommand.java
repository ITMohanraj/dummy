package com.cropdeal.cropservice.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestockCropCommand {
    private Long cropId;
    private Double addedQuantityKg;
}
