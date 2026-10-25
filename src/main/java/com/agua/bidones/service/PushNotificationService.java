package com.agua.bidones.service;

import com.agua.bidones.dto.ExpoPushMessageDTO;
import com.agua.bidones.model.User;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    private final RestClient restClient;
    private final String expoAccessToken;
    private final ExecutorService executor = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "push-sender");
        thread.setDaemon(true);
        return thread;
    });

    public PushNotificationService(@Value("${expo.access-token}") String expoAccessToken) {
        this.expoAccessToken = expoAccessToken;
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(3));
        this.restClient = RestClient.builder()
                .baseUrl("https://exp.host/--/api/v2")
                .requestFactory(requestFactory)
                .build();
    }

    public void sendToUser(User user, String title, String body) {
        if (user == null || user.getPushToken() == null || user.getPushToken().isBlank()) {
            return;
        }
        final ExpoPushMessageDTO message = new ExpoPushMessageDTO(user.getPushToken(), title, body);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    submit(message);
                }
            });
        } else {
            submit(message);
        }
    }

    private void submit(ExpoPushMessageDTO message) {
        try {
            executor.execute(() -> post(message));
        } catch (Exception e) {
            log.warn("No se pudo encolar el push: {}", e.getMessage());
        }
    }

    private void post(ExpoPushMessageDTO message) {
        try {
            restClient.post()
                    .uri("/push/send")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + expoAccessToken)
                    .body(message)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Error enviando push notification: {}", e.getMessage());
        }
    }

    @PreDestroy
    void shutdown() {
        executor.shutdown();
    }
}
