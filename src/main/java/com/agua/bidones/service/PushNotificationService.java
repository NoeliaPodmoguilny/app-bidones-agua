package com.agua.bidones.service;

import com.agua.bidones.dto.ExpoPushMessageDTO;
import com.agua.bidones.model.User;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;

@Service
public class PushNotificationService {

    private final RestClient restClient;
    private final String expoAccessToken;

    public PushNotificationService(@Value("${expo.access-token}") String expoAccessToken) {
        this.expoAccessToken = expoAccessToken;
        this.restClient = RestClient.builder()
                .baseUrl("https://exp.host/--/api/v2")
                .build();
    }

    public void sendToUser(User user, String title, String body) {
        if (user.getPushToken() == null || user.getPushToken().isBlank()) {
            return;
        }
        try {
            ExpoPushMessageDTO message = new ExpoPushMessageDTO(
                    user.getPushToken(),
                    title,
                    body
            );
            restClient.post()
                    .uri("/push/send")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + expoAccessToken)
                    .body(message)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.err.println("Error enviando push notification: " + e.getMessage());
        }
    }
}


