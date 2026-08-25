package com.agua.bidones.model;

import com.agua.bidones.enums.Status;
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
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false, length = 200)
    private String address;
    @Column(length = 300)
    private String rejectionReason;
    @CreationTimestamp
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private User cliente;
    @ManyToOne(fetch = FetchType.LAZY)
    private User repartidor;

}
