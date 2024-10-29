package uz.ns.cardprocessing.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import uz.ns.cardprocessing.dto.ApiResult;
import uz.ns.cardprocessing.dto.CardDto;
import uz.ns.cardprocessing.dto.ResponseCardDto;
import uz.ns.cardprocessing.service.imp.CardService;

import java.util.UUID;

@RestController
@Slf4j
public class CardController {
    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/api/cards")
    public ApiResult<ResponseCardDto> addCard(@RequestBody CardDto cardDto, @RequestHeader(name = "IdempotencyKey") String header) {
        log.info("add cart {}", cardDto);
        return cardService.addCard(cardDto, header);
    }

    @GetMapping("/api/v1/cards/{cardId}")
    public ApiResult<ResponseCardDto> getCard(@PathVariable UUID cardId, HttpServletResponse response) {
        log.info("get cart {}", cardId);
        return cardService.getCards(cardId, response);
    }

    @PostMapping("/api/v1/cards/{cardId}/block")
    public ApiResult<ResponseCardDto> blockCard(@PathVariable UUID cardId, @RequestHeader(name = "If-Match") String header, HttpServletResponse response) {
        log.info("block cart {}", cardId);
        return cardService.block(cardId, header, response);
    }

    @PostMapping("/api/v1/cards/{cardId}/unblock")
    public ApiResult<ResponseCardDto> unblockCard(@PathVariable UUID cardId, @RequestHeader(name = "If-Match") String header, HttpServletResponse response) {
        log.info("block cart {}", cardId);
        return cardService.unblock(cardId, header, response);
    }

}
