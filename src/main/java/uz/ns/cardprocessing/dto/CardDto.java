package uz.ns.cardprocessing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.ns.cardprocessing.entity.Currency;
import uz.ns.cardprocessing.entity.Status;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardDto {
    private Long user_id;
    private Status status;
    private long initial_amount;
    private Currency currency;
}
