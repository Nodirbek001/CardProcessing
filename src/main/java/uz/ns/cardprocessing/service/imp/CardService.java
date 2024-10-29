package uz.ns.cardprocessing.service.imp;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.ns.cardprocessing.dto.ApiResult;
import uz.ns.cardprocessing.dto.CardDto;
import uz.ns.cardprocessing.dto.ResponseCardDto;
import uz.ns.cardprocessing.entity.Card;
import uz.ns.cardprocessing.entity.CardStatus;
import uz.ns.cardprocessing.entity.Currency;
import uz.ns.cardprocessing.entity.User;
import uz.ns.cardprocessing.repository.CardRepository;
import uz.ns.cardprocessing.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class CardService {
    private final CardRepository cartRepository;
    private final UserRepository userRepository;


    public CardService(CardRepository cartRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }

    public ApiResult<ResponseCardDto> addCard(CardDto cardDto, String header) {
        UUID idempotencyKeyUUID = UUID.fromString(header);
        Optional<Card> optionalCard = cartRepository.findByIdempotencyKey(idempotencyKeyUUID);
        if (optionalCard.isPresent()) {
            Card card = optionalCard.get();
            ResponseCardDto build = ResponseCardDto.builder()
                    .cartId(String.valueOf(card.getId()))
                    .cartStatus(card.getCardStatus())
                    .balance(card.getAmount())
                    .userId(String.valueOf(card.getUser().getId()))
                    .currency(card.getCurrency())
                    .build();
            System.out.println(build);
            return ApiResult.successResponse(
                    build, 200
            );
        }
        List<Card> carts = cartRepository.findByUserIdAndCardStatus(cardDto.getUser_id(), CardStatus.CLOSED).orElseThrow();
        if (carts.size() >= 3)
            return ApiResult.errorResponse("3tadan kop karta ochib bolmaydi", "3tadan kop karta ochib bolmaydi", 400);

        Card cart = new Card();
        String cartStatus = String.valueOf(cardDto.getStatus()).toUpperCase();
        if (cartStatus.equals("CLOSED"))
            cart.setCardStatus(CardStatus.CLOSED);
        if (cartStatus.equals("ACTIVE"))
            cart.setCardStatus(CardStatus.ACTIVE);
        if (cartStatus.equals("BLOCKED"))
            cart.setCardStatus(CardStatus.BLOCKED);
        else
            cart.setCardStatus(CardStatus.ACTIVE);
        if (cardDto.getInitial_amount() > 10000) {
            return ApiResult.errorResponse("10.000 dan kop kiritib bolmaydi", "10.000 dan kop kiritib bolmaydi", 400);

        }
        if (cardDto.getCurrency() == null) {
            cart.setCurrency(Currency.UZS);
        }


        Optional<User> byId = userRepository.findById(cardDto.getUser_id());
        if (byId.isEmpty()) {
            return ApiResult.errorResponse("User not found", "user not found", 400);
        }
        String randomNumber = String.valueOf((Math.random() * 9000000000000000L) + 1000000000000000L);
        cart.setCardNumber(randomNumber);
        cart.setIdempotencyKey(idempotencyKeyUUID);
        cart.setAmount(cardDto.getInitial_amount());
        cart.setUser(byId.get());
        Card save = cartRepository.save(cart);

        return ApiResult.successResponse(
                ResponseCardDto.builder()
                        .cartId(String.valueOf(save.getId()))
                        .cartStatus(save.getCardStatus())
                        .balance(save.getAmount())
                        .userId(String.valueOf(save.getUser().getId()))
                        .currency(save.getCurrency())
                        .build(), 201);


    }


    public ApiResult<ResponseCardDto> getCards(UUID cardId, HttpServletResponse response) {

        Optional<Card> cart = cartRepository.findById(cardId);
        if (cart.isEmpty())
            return ApiResult.errorResponse("Card not found", "Card not found", 404);
        Card card = cart.get();
        ResponseCardDto build = ResponseCardDto.builder()
                .cartId(String.valueOf(card.getId()))
                .cartStatus(card.getCardStatus())
                .balance(card.getAmount())
                .userId(String.valueOf(card.getUser().getId()))
                .currency(card.getCurrency())
                .build();
        UUID ETag = UUID.randomUUID();
        card.setETag(ETag);
        cartRepository.save(card);

        response.setHeader("ETag", ETag.toString());
        return ApiResult.successResponse(build, 200);


    }


    public ApiResult<ResponseCardDto> block(UUID cardId, String header, HttpServletResponse response) {
        Card cart = cartRepository.findById(cardId).orElse(null);


        if (cart == null || cart.getCardStatus().equals(CardStatus.CLOSED)) {
            response.setStatus(404);
            return ApiResult.errorResponse("Card not found", "Card not found", 404);
        }
        if (cart.getCardStatus().equals(CardStatus.BLOCKED)) {
            response.setStatus(404);
            return ApiResult.errorResponse("Card already blocked", "Card already blocked", 404);
        }

        if (!cart.getETag().equals(UUID.fromString(header))) {
            response.setStatus(400);
            return ApiResult.errorResponse("Invalid ETag", "Invalid ETag", 400);

        }
        cart.setCardStatus(CardStatus.BLOCKED);
        cartRepository.save(cart);

        response.setStatus(204);
        return ApiResult.successResponse();
    }

    public ApiResult<ResponseCardDto> unblock(UUID cardId, String header, HttpServletResponse response) {
        Card cart = cartRepository.findById(cardId).orElse(null);

        if (cart == null || cart.getCardStatus().equals(CardStatus.CLOSED)) {
            response.setStatus(404);
            return ApiResult.errorResponse("Card not found", "Card not found", 404);
        }
        if (cart.getCardStatus().equals(CardStatus.ACTIVE)) {
            response.setStatus(404);
            return ApiResult.errorResponse("Card already active", "Card already active", 404);
        }
        if (!cart.getETag().equals(UUID.fromString(header))) {
            response.setStatus(400);
            return ApiResult.errorResponse("Invalid ETag", "Invalid ETag", 400);

        }

        cart.setCardStatus(CardStatus.ACTIVE);
        cartRepository.save(cart);

        response.setStatus(204);
        return ApiResult.successResponse();
    }
}
