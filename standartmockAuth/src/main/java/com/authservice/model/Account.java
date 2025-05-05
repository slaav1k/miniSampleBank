package com.authservice.model;

import lombok.Data;
import jakarta.persistence.*;
import java.util.UUID;

@Data
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @Column(name = "card_number", nullable = false, unique = true)
    private String cardNumber;

    @Column(name = "balance", nullable = false)
    private double balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    public Account() {
        this.accountNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        this.cardNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        this.balance = 0.0;
    }
}