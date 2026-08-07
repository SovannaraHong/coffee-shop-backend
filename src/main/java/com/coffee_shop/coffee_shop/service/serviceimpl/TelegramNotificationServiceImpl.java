package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.service.TelegramNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class TelegramNotificationServiceImpl implements TelegramNotificationService {

    @Value("${telegram.bot-token}")
    private String botToken;

    @Value("${telegram.chat-id}")
    private String chatId;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final MediaType JSON = MediaType.parse("application/json");

    @Override
    public void sendMessage(String text) {
        try {
            Map<String, Object> payload = Map.of(
                    "chat_id", chatId,
                    "text", text,
                    "parse_mode", "Markdown",
                    "disable_web_page_preview", false,
                    "disable_notification", false
            );

            String jsonBody = objectMapper.writeValueAsString(payload);
            RequestBody body = RequestBody.create(jsonBody, JSON);

            Request request = new Request.Builder()
                    .url("https://api.telegram.org/bot" + botToken + "/sendMessage")
                    .post(body)
                    .addHeader("accept", "application/json")
                    .addHeader("content-type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.err.println(
                            "Telegram API error: "
                                    + response.code()
                                    + " "
                                    + response.body().string()
                    );
                }
            }

        } catch (IOException e) {
            // Don't let a Telegram failure break order creation
            System.err.println("Failed to send Telegram notification: " + e.getMessage());
        }
    }
}