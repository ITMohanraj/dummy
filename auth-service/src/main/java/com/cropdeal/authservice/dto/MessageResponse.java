package com.cropdeal.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard Status Message Response")
public class MessageResponse {

    @Schema(description = "Informative status message", example = "Password reset successfully")
    private String message;
}
