package uz.ns.cardprocessing.dto.transaction;

import lombok.Getter;
import lombok.Setter;
import uz.ns.cardprocessing.entity.Currency;
import uz.ns.cardprocessing.entity.Purpose;
@Getter
@Setter
public class ReqTransactionDto {
    private String external_id;
    private Long amount;
    private Currency currency;
    private Purpose purpose;
}
