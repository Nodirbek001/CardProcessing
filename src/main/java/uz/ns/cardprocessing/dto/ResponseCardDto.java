package uz.ns.cardprocessing.dto;

import lombok.*;
import uz.ns.cardprocessing.entity.CardStatus;

import uz.ns.cardprocessing.entity.Currency;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ResponseCardDto {
    private String cartId;
    private String userId;
    private Currency currency;
    private Long balance;
    private CardStatus cartStatus;
}