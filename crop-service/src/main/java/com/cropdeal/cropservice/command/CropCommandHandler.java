package com.cropdeal.cropservice.command;

import com.cropdeal.cropservice.dto.CropResponse;
import com.cropdeal.cropservice.service.CropService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CropCommandHandler {

    private final CropService cropService;

    public CropResponse handle(CreateCropCommand command) {
        log.info("CQRS CommandHandler: Executing CreateCropCommand for crop: {}", command.getRequest().getCropName());
        return cropService.createCrop(command.getRequest());
    }

    public CropResponse handle(RestockCropCommand command) {
        log.info("CQRS CommandHandler: Executing RestockCropCommand for cropId: {}, added: {} KG",
                command.getCropId(), command.getAddedQuantityKg());
        return cropService.restockCrop(command.getCropId(), command.getAddedQuantityKg());
    }
}
