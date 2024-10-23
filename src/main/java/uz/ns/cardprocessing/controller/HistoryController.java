package uz.ns.cardprocessing.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.ns.cardprocessing.dto.ApiResult;
import uz.ns.cardprocessing.entity.Transaction;
import uz.ns.cardprocessing.service.imp.HistoryService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HistoryController {
    private final HistoryService historyService;

    @GetMapping("/api/v1/cards/{cardId}/transactions")
    public ApiResult<?> getHistory(@PathVariable("cardId") String cardId, @RequestParam Map<String, String> params,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        log.info("getHistory");
        Page<Transaction> filteredTransactions =
                historyService.getFilteredTransactions(cardId, params, page, size);
        return ApiResult.successResponse(filteredTransactions, 200);
    }
}
