package uz.ns.cardprocessing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Card {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    private UUID idempotencyKey;
    private UUID ETag;
    @ManyToOne
    private User user;
    @Enumerated(EnumType.STRING)
    private CardStatus cardStatus = CardStatus.ACTIVE;
    private Long amount = 0L;
    private String cardNumber;
    private String endDate;
    @Enumerated(EnumType.STRING)
    private Currency currency = Currency.UZS;

}
