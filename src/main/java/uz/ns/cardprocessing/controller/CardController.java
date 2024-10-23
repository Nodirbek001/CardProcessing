package uz.ns.cardprocessing.controller;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import uz.ns.cardprocessing.dto.ApiResult;
import uz.ns.cardprocessing.dto.CardDto;
import uz.ns.cardprocessing.dto.ResponseCardDto;
import uz.ns.cardprocessing.service.imp.CardService;

import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;
    @PostMapping("/api/cards")
    public ApiResult<ResponseCardDto> addCard(@RequestBody CardDto cardDto, HttpServletRequest request) {
        log.info("add cart {}", cardDto);
        return cardService.addCard(cardDto, request);
    }
    @GetMapping("/api/v1/cards/{cardId}")
    public ApiResult<ResponseCardDto> getCard(@PathVariable UUID cardId, HttpServletResponse response) {
        log.info("get cart {}", cardId);
        return cardService.getCards(cardId, response);
    }
    @PostMapping("/api/v1/cards/{cardId}/block")
    public ApiResult<ResponseCardDto> blockCard(@PathVariable UUID cardId, HttpServletRequest request, HttpServletResponse response) {
        log.info("block cart {}", cardId);
        return cardService.block(cardId, request, response);
    }
    @PostMapping("/api/v1/cards/{cardId}/unblock")
    public ApiResult<ResponseCardDto> unblockCard(@PathVariable UUID cardId, HttpServletRequest request, HttpServletResponse response) {
        log.info("block cart {}", cardId);
        return cardService.unblock(cardId, request, response);
    }

}
