package com.agua.bidones.model;

import com.agua.bidones.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;
    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;
    private String title;
    private String message;
    private Boolean read;
    @CreationTimestamp
    private Instant createdAt;
}
