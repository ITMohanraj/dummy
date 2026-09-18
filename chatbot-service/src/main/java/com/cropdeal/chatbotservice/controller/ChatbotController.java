package com.cropdeal.chatbotservice.controller;

import com.cropdeal.chatbotservice.dto.ChatRequest;
import com.cropdeal.chatbotservice.dto.ChatResponse;
import com.cropdeal.chatbotservice.service.ChatbotAdvisoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
@Tag(name = "Agricultural Assistant", description = "Market Intelligence Advisory & FAQ Chatbot APIs")
public class ChatbotController {

    private final ChatbotAdvisoryService advisoryService;

    @PostMapping("/ask")
    @Operation(summary = "Ask the agricultural advisory assistant a question")
    public ResponseEntity<ChatResponse> askQuestion(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(advisoryService.processQuery(request));
    }
}
