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
@Table(name = "users")
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

    //Si es cliente debe tener asociado un repartidor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartidor_id")
    private User repartidor;

    // el repartidor tiene asociado el admin
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;

}
