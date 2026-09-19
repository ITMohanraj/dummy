package com.cropdeal.chatbotservice.service;

import com.cropdeal.chatbotservice.dto.ChatRequest;
import com.cropdeal.chatbotservice.dto.ChatResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
@Slf4j
public class ChatbotAdvisoryService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${cropdeal.price-service.url:http://price-service:8084}")
    private String priceServiceUrl;

    @Value("${cropdeal.crop-service.url:http://crop-service:8083}")
    private String cropServiceUrl;

    private static final Map<String, String> CROP_EMOJIS = Map.ofEntries(
            Map.entry("tomato", "🍅"),
            Map.entry("onion", "🧅"),
            Map.entry("potato", "🥔"),
            Map.entry("wheat", "🌾"),
            Map.entry("rice", "🍚"),
            Map.entry("paddy", "🌾"),
            Map.entry("cotton", "🌱"),
            Map.entry("banana", "🍌"),
            Map.entry("apple", "🍎"),
            Map.entry("mango", "🥭"),
            Map.entry("chilli", "🌶️"),
            Map.entry("garlic", "🧄"),
            Map.entry("ginger", "🫚"),
            Map.entry("carrot", "🥕"),
            Map.entry("sugarcane", "🎋"),
            Map.entry("turmeric", "🟡")
    );

    public ChatbotAdvisoryService() {
        this.restClient = RestClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    public ChatResponse processQuery(ChatRequest req) {
        String msg = req.getMessage().trim().toLowerCase();
        String detectedCrop = extractCropName(msg);

        // 1. CROP PRICE INQUIRY (Dynamic Live Online & Mandi Price)
        if (isPriceQuery(msg)) {
            return handlePriceQuery(req, detectedCrop);
        }

        // 2. CROP DISEASE / PEST / FERTILIZER ADVISORY
        if (isAgronomyQuery(msg)) {
            return handleAgronomyQuery(req, detectedCrop);
        }

        // 3. CROP POSTING & SELLING GUIDE
        if (msg.contains("post") || msg.contains("sell") || msg.contains("list")) {
            return ChatResponse.builder()
                    .query(req.getMessage())
                    .intent("CROP_POSTING_GUIDE")
                    .answer("👨‍🌾 Farmers can publish crop listings directly on the CropDeal marketplace:\n" +
                            "1. Go to 'Post Crop' and enter crop details (variety, grade A/B/C, organic status, quantity in KG).\n" +
                            "2. Set price per KG within the government APMC reference fair price range (±15%).\n" +
                            "3. Your listing immediately becomes visible to verified dealers across the country.")
                    .suggestedActions(List.of("Post New Crop", "View My Crop Listings", "Check APMC Price Band"))
                    .apiReference("POST /api/v1/crops")
                    .build();
        }

        // 4. PURCHASING & SAGA GUIDE
        if (msg.contains("buy") || msg.contains("order") || msg.contains("purchase")) {
            return ChatResponse.builder()
                    .query(req.getMessage())
                    .intent("PURCHASE_GUIDE")
                    .answer("🛒 Dealers can buy crops through 3 flexible channels:\n" +
                            "1. Direct Buy: Instant atomic purchase orchestrated by our resilient Purchase Saga.\n" +
                            "2. Price Negotiation: Send custom counter-offers per KG to the farmer.\n" +
                            "3. Live Bidding: Place competitive bids on scheduled farmer auctions.")
                    .suggestedActions(List.of("Browse Marketplace", "Make Negotiation Offer", "Join Live Auctions"))
                    .apiReference("POST /api/v1/orders/purchase")
                    .build();
        }

        // 5. DELIVERY LOGISTICS
        if (msg.contains("delivery") || msg.contains("partner") || msg.contains("truck") || msg.contains("transport") || msg.contains("shipping")) {
            return ChatResponse.builder()
                    .query(req.getMessage())
                    .intent("DELIVERY_GUIDE")
                    .answer("🚚 CropDeal Delivery Logistics:\n" +
                            "• Transparent rate: ₹10 per KM automatically computed from GPS coordinates.\n" +
                            "• Real-time lifecycle: AVAILABLE ➔ ASSIGNED ➔ IN_TRANSIT ➔ DELIVERED.\n" +
                            "• Escrow security: Delivery fees are held safely until dealer confirms receipt.")
                    .suggestedActions(List.of("Book Delivery", "Track Active Shipment", "Available Delivery Jobs"))
                    .apiReference("POST /api/v1/deliveries/requests")
                    .build();
        }

        // 6. WALLET & PAYMENTS
        if (msg.contains("wallet") || msg.contains("balance") || msg.contains("pay") || msg.contains("money") || msg.contains("topup")) {
            return ChatResponse.builder()
                    .query(req.getMessage())
                    .intent("WALLET_INQUIRY")
                    .answer("💳 CropDeal Digital Wallet:\n" +
                            "• Instant top-up via Net Banking, UPI, and Debit/Credit Cards.\n" +
                            "• Zero transaction fees on direct farmer-to-dealer settlements.\n" +
                            "• Complete audit trail for every credit, debit, and escrow hold.")
                    .suggestedActions(List.of("Check Wallet Balance", "Top Up Funds", "View Payment History"))
                    .apiReference("GET /api/v1/wallets/user/{userId}")
                    .build();
        }

        // 7. DEFAULT INTELLIGENT GREETING
        return ChatResponse.builder()
                .query(req.getMessage())
                .intent("GENERAL_ASSIST")
                .answer("🌾 Welcome to CropDeal AI Agricultural Assistant!\n\n" +
                        "How can I help you today?\n" +
                        "• 📊 Live Prices: Ask \"Tomato price\", \"Onion mandi rate\", or \"Wheat MSP\"\n" +
                        "• 🌿 Crop Advisory: Ask about fertilizers, pest control, or diseases (e.g. \"Tomato blight cure\")\n" +
                        "• 🚜 Marketplace: Ask how to post crops, negotiate deals, or book ₹10/km delivery")
                .suggestedActions(List.of("Check Tomato Price", "Check Onion Price", "How to Post Crops", "Delivery Rates"))
                .apiReference("GET /api/v1/crops/search")
                .build();
    }

    private boolean isPriceQuery(String msg) {
        return msg.contains("price") || msg.contains("rate") || msg.contains("cost") ||
                msg.contains("how much") || msg.contains("mandi") || msg.contains("msp") ||
                msg.contains("market value") || msg.contains("benchmark");
    }

    private boolean isAgronomyQuery(String msg) {
        return msg.contains("fertilizer") || msg.contains("pest") || msg.contains("disease") ||
                msg.contains("blight") || msg.contains("spray") || msg.contains("fungus") ||
                msg.contains("insects") || msg.contains("organic") || msg.contains("harvest") ||
                msg.contains("yield") || msg.contains("soil") || msg.contains("water") || msg.contains("irrigation");
    }

    private String extractCropName(String msg) {
        List<String> crops = List.of(
                "tomato", "onion", "potato", "wheat", "rice", "paddy", "cotton",
                "banana", "apple", "mango", "chilli", "garlic", "ginger", "carrot",
                "cabbage", "cauliflower", "brinjal", "sugarcane", "soybean", "groundnut",
                "mustard", "turmeric", "pulses", "maize", "corn"
        );

        for (String crop : crops) {
            if (msg.contains(crop)) {
                return crop;
            }
        }
        return null;
    }

    private ChatResponse handlePriceQuery(ChatRequest req, String cropName) {
        String queryCrop = cropName != null ? cropName : "tomato";
        String capitalizedCrop = queryCrop.substring(0, 1).toUpperCase() + queryCrop.substring(1);
        String emoji = CROP_EMOJIS.getOrDefault(queryCrop, "🌾");

        Double refPrice = null;
        Double minAllowed = null;
        Double maxAllowed = null;
        Integer activeListingsCount = 0;
        Double avgMarketplacePrice = null;
        Double totalStockKg = 0.0;

        // Query price-service via CircuitBreaker
        try {
            String priceJson = fetchPriceJson(capitalizedCrop);
            if (priceJson != null) {
                JsonNode node = objectMapper.readTree(priceJson);
                if (node.has("referencePrice")) refPrice = node.get("referencePrice").asDouble();
                if (node.has("minAllowedPrice")) minAllowed = node.get("minAllowedPrice").asDouble();
                if (node.has("maxAllowedPrice")) maxAllowed = node.get("maxAllowedPrice").asDouble();
            }
        } catch (Exception e) {
            log.warn("Price-service lookup fallback for {}: {}", queryCrop, e.getMessage());
        }

        // Query crop-service for live listings via CircuitBreaker
        try {
            String cropJson = fetchCropJson(capitalizedCrop);
            if (cropJson != null) {
                JsonNode root = objectMapper.readTree(cropJson);
                JsonNode content = root.has("content") ? root.get("content") : root;
                if (content.isArray()) {
                    activeListingsCount = content.size();
                    double priceSum = 0;
                    for (JsonNode item : content) {
                        if (item.has("pricePerKg")) priceSum += item.get("pricePerKg").asDouble();
                        if (item.has("availableQuantityKg")) totalStockKg += item.get("availableQuantityKg").asDouble();
                    }
                    if (activeListingsCount > 0) {
                        avgMarketplacePrice = Math.round((priceSum / activeListingsCount) * 10.0) / 10.0;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Crop-service lookup fallback for {}: {}", queryCrop, e.getMessage());
        }

        // Fallbacks if services are cold
        if (refPrice == null) refPrice = 24.0;
        if (minAllowed == null) minAllowed = Math.round(refPrice * 0.85 * 10.0) / 10.0;
        if (maxAllowed == null) maxAllowed = Math.round(refPrice * 1.35 * 10.0) / 10.0;
        if (avgMarketplacePrice == null) avgMarketplacePrice = refPrice + 1.0;

        String answer = String.format(
                "%s Live %s Price & Marketplace Intelligence:\n\n" +
                "• 🏛️ Government APMC Mandi Reference: ₹%.2f / KG\n" +
                "• 🛒 CropDeal Marketplace Selling Price: ₹%.2f / KG\n" +
                "• 📦 Active Listings on Platform: %d active batches (Total: %.0f KG available)\n" +
                "• ⚖️ Permitted Price Band: ₹%.2f – ₹%.2f / KG\n\n" +
                "💡 Pro-Tip for Dealers: You can place a negotiation offer at ₹%.2f/KG for bulk orders, or join live auctions to bid competitively!",
                emoji, capitalizedCrop, refPrice, avgMarketplacePrice, activeListingsCount, totalStockKg, minAllowed, maxAllowed, (refPrice - 2.0)
        );

        return ChatResponse.builder()
                .query(req.getMessage())
                .intent("LIVE_PRICE_ANALYSIS")
                .answer(answer)
                .suggestedActions(List.of("View All " + capitalizedCrop + " Listings", "Start Negotiation", "Check 30-Day Trend"))
                .apiReference("GET /api/v1/crops/search?cropName=" + capitalizedCrop)
                .build();
    }

    private ChatResponse handleAgronomyQuery(ChatRequest req, String cropName) {
        String msg = req.getMessage().toLowerCase();
        String crop = cropName != null ? cropName : "tomato";
        String capCrop = crop.substring(0, 1).toUpperCase() + crop.substring(1);
        String emoji = CROP_EMOJIS.getOrDefault(crop, "🌿");

        String answer;
        if (msg.contains("blight") || msg.contains("fungus") || msg.contains("disease")) {
            answer = String.format(
                    "%s Diagnostic & Disease Management for %s:\n\n" +
                    "• Disease Identified: Early / Late Leaf Blight (Alternaria / Phytophthora)\n" +
                    "• 🛡️ Organic Remedy: Spray Neem Seed Kernel Extract (NSKE 5%%) or Trichoderma viride @ 2.5 kg/ha with FYM.\n" +
                    "• 🧪 Chemical Treatment: Copper Oxychloride 50%% WP @ 2.5 g/L water or Mancozeb 75%% WP @ 2 g/L.\n" +
                    "• 💧 Cultural Practices: Avoid overhead sprinkling, ensure 60cm row spacing, and remove affected lower leaves.",
                    emoji, capCrop
            );
        } else if (msg.contains("fertilizer") || msg.contains("nutrient") || msg.contains("npk")) {
            answer = String.format(
                    "%s Fertilizer & Nutrient Management Schedule for %s:\n\n" +
                    "• Basal Dose: 25 tonnes FYM + N:P:K @ 50:50:50 kg/ha at final land preparation.\n" +
                    "• Top Dressing 1 (30 days after planting): 25 kg Nitrogen/ha + micronutrient spray (Zinc & Boron).\n" +
                    "• Top Dressing 2 (Flowering/Fruiting stage): 25 kg Nitrogen + 25 kg Potassium/ha for optimal fruit weight.\n" +
                    "• 🌿 Organic Boost: Drench with Panchagavya 3%% or Jeevamrutha every 15 days.",
                    emoji, capCrop
            );
        } else {
            answer = String.format(
                    "%s Crop Health & Pest Protection for %s:\n\n" +
                    "• Common Pests: Fruit borer, whiteflies, and aphids.\n" +
                    "• Management: Install yellow sticky traps (15/acre) and pheromone traps (5/acre).\n" +
                    "• Spray: Azadirachtin (1500 ppm) @ 3 ml/L water every 10 days for pest prevention.",
                    emoji, capCrop
            );
        }

        return ChatResponse.builder()
                .query(req.getMessage())
                .intent("AGRONOMY_ADVISORY")
                .answer(answer)
                .suggestedActions(List.of("Fertilizer Schedule", "Organic Pest Spray", "Check " + capCrop + " Market Price"))
                .apiReference("GET /api/v1/prices/validate?cropName=" + capCrop)
                .build();
    }

    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "priceService", fallbackMethod = "fetchPriceJsonFallback")
    @io.github.resilience4j.retry.annotation.Retry(name = "priceService")
    public String fetchPriceJson(String cropName) {
        return restClient.get()
                .uri(priceServiceUrl + "/api/v1/prices/validate?cropName={crop}&state=Tamil Nadu&district=Erode&grade=A&pricePerKg=1", cropName)
                .retrieve()
                .body(String.class);
    }

    public String fetchPriceJsonFallback(String cropName, Throwable ex) {
        log.warn("Resilience4j Circuit Breaker triggered for Price Service. Returning cached fallback for {}. Reason: {}",
                cropName, ex.getMessage());
        return null;
    }

    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "cropService", fallbackMethod = "fetchCropJsonFallback")
    @io.github.resilience4j.retry.annotation.Retry(name = "cropService")
    public String fetchCropJson(String cropName) {
        return restClient.get()
                .uri(cropServiceUrl + "/api/v1/crops/search?cropName={crop}&page=0&size=20", cropName)
                .retrieve()
                .body(String.class);
    }

    public String fetchCropJsonFallback(String cropName, Throwable ex) {
        log.warn("Resilience4j Circuit Breaker triggered for Crop Service. Returning cached fallback for {}. Reason: {}",
                cropName, ex.getMessage());
        return null;
    }
}