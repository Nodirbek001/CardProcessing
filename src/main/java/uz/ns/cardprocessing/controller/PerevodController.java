package uz.ns.cardprocessing.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import uz.ns.cardprocessing.dto.ApiResult;
import uz.ns.cardprocessing.dto.transaction.ReqTransactionDto;
import uz.ns.cardprocessing.dto.transaction.RespTransactionDto;
import uz.ns.cardprocessing.service.imp.PerevodService;

@RestController
@Slf4j

public class PerevodController {
    private final PerevodService perevodService;

    public PerevodController(PerevodService perevodService) {
        this.perevodService = perevodService;
    }

    @PostMapping("/api/v1/cards/{cardId}/debit")
    public ApiResult<RespTransactionDto> debitCard(@PathVariable("cardId") String cardId, @RequestBody ReqTransactionDto reqTransactionDto, HttpServletRequest req, HttpServletResponse resp) {
        log.info("debit card");
        return perevodService.debitCard(cardId, reqTransactionDto, req, resp);
    }
    @PostMapping("/api/v1/cards/{cardId}/credit")
    public ApiResult<RespTransactionDto> creditCard(@PathVariable("cardId") String cardId, @RequestBody ReqTransactionDto reqTransactionDto, HttpServletRequest req, HttpServletResponse resp) {
        log.info("credit card");
        return perevodService.creditCard(cardId, reqTransactionDto, req, resp);
    }
}
