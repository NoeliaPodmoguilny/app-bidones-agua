package com.agua.bidones.model;

import com.agua.bidones.enums.UserType;
import jakarta.persistence.*;
import jdk.jfr.Enabled;
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
@Table(name = "users", indexes = {
        @Index(name = "idx_users_repartidor", columnList = "repartidor_id"),
        @Index(name = "idx_users_admin", columnList = "admin_id")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(value = EnumType.STRING)
    private UserType userType;
    private String name;
    @Column(unique = true)
    private String identifier;
    private String password;
    @Column(length = 255)
    private String pushToken;
    @CreationTimestamp
    private Instant createdAt;

    private Boolean active;

    public boolean isAccountActive() {
        return active == null || active;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartidor_id")
    private User repartidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;

}
