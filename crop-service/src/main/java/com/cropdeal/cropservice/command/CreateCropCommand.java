package com.cropdeal.cropservice.command;

import com.cropdeal.cropservice.dto.CropCreateRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCropCommand {
    private CropCreateRequest request;
}
