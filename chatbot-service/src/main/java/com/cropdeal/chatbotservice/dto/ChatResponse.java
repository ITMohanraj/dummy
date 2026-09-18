package com.cropdeal.chatbotservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String query;
    private String answer;
    private String intent;
    private List<String> suggestedActions;
    private String apiReference;
}
