package uz.ns.cardprocessing.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString
public class Transaction {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator")
    private UUID transactionId;
    private UUID externalId;
    private UUID idempotencyKey;
    private Long userId;
    private UUID cardId;
    private Long amount;
    private Currency currency;
    private Long exchange_rate;
    private Purpose purpose;
    private Long after_balance;
    private String description;
}
