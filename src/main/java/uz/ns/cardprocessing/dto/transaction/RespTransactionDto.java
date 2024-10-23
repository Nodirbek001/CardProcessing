package uz.ns.cardprocessing.dto.transaction;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uz.ns.cardprocessing.entity.Currency;
import uz.ns.cardprocessing.entity.Purpose;

@Getter
@Setter
@Builder
public class RespTransactionDto {
    private String transaction_id;
    private String external_id;
    private String cart_id;
    private Long after_balance;
    private Long amount;
    private Currency currency;
    private Purpose purpose;
    private Long exchange_rate;
}
