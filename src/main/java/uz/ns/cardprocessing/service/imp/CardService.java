package uz.ns.cardprocessing.service.imp;

import jakarta.servlet.http.HttpServletRequest;
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

    public ApiResult<ResponseCardDto> addCard(CardDto cardDto, HttpServletRequest request) {
        boolean checkUUID = checkUUID(request.getHeader("IdempotencyKey"));
        if (!checkUUID) {
            return ApiResult.errorResponse("UUID error", "UUID error", 400);
        }
        UUID idempotencyKeyUUID = UUID.fromString(request.getHeader("IdempotencyKey"));
        Optional<Card> idempotencyKey = cartRepository.findByIdempotencyKey(idempotencyKeyUUID);
        if (idempotencyKey.isPresent()) {
            ResponseCardDto build = ResponseCardDto.builder()
                    .cartId(String.valueOf(idempotencyKey.get().getId()))
                    .cartStatus(idempotencyKey.get().getCardStatus())
                    .balance(idempotencyKey.get().getAmount())
                    .userId(String.valueOf(idempotencyKey.get().getUser().getId()))
                    .currency(idempotencyKey.get().getCurrency())
                    .build();
            System.out.println(build);
            return ApiResult.successResponse(
                    build, 200
            );
        }
        List<Card> carts = cartRepository.findByUserId(cardDto.getUser_id()).get();
        int a = 0;
        for (Card cart : carts) {
            if (cart.getCardStatus().equals(CardStatus.CLOSED))
                a++;

        }
        int i = carts.size() - a;
        if (i >= 3)
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
        long randomNumber = (long) (Math.random() * 9000000000000000L) + 1000000000000000L;
        cart.setCardNumber(String.valueOf(randomNumber));
        cart.setIdempotencyKey(UUID.fromString(request.getHeader("IdempotencyKey")));
        cart.setAmount(cardDto.getInitial_amount());
        cart.setUser(byId.orElse(new User()));
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


    private boolean checkUUID(String idempotencyKey) {
        try {
            UUID.fromString(idempotencyKey);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public ApiResult<ResponseCardDto> getCards(UUID cardId, HttpServletResponse response) {
        Card cart = checkCard(cardId);
        if (cart == null)
            return ApiResult.errorResponse("Card not found", "Card not found", 404);
        ResponseCardDto build = ResponseCardDto.builder()
                .cartId(String.valueOf(cart.getId()))
                .cartStatus(cart.getCardStatus())
                .balance(cart.getAmount())
                .userId(String.valueOf(cart.getUser().getId()))
                .currency(cart.getCurrency())
                .build();
        UUID ETag = UUID.randomUUID();
        cart.setETag(ETag);
        cartRepository.save(cart);

        response.setHeader("ETag", ETag.toString());
        return ApiResult.successResponse(build, 200);


    }

    private Card checkCard(UUID cardId) {
        Optional<Card> byId = cartRepository.findById(cardId);
        return byId.orElse(null);

    }

    public ApiResult<ResponseCardDto> block(UUID cardId, HttpServletRequest request, HttpServletResponse response) {
        Optional<Card> byId = cartRepository.findById(cardId);
        Card cart = byId.orElse(null);

        if (cart == null || cart.getCardStatus().equals(CardStatus.CLOSED)) {
            response.setStatus(404);
            return ApiResult.errorResponse("Card not found", "Card not found", 404);
        }
        if (cart.getCardStatus().equals(CardStatus.BLOCKED)) {
            response.setStatus(404);
            return ApiResult.errorResponse("Card already blocked", "Card already blocked", 404);
        }

        if (!byId.get().getETag().equals(UUID.fromString(request.getHeader("If-Match")))) {
            response.setStatus(400);
            return ApiResult.errorResponse("Invalid ETag", "Invalid ETag", 400);

        }
        cart.setCardStatus(CardStatus.BLOCKED);
        cartRepository.save(cart);

        response.setStatus(204);
        return ApiResult.successResponse();
    }

    public ApiResult<ResponseCardDto> unblock(UUID cardId, HttpServletRequest request, HttpServletResponse response) {
        Optional<Card> byId = cartRepository.findById(cardId);
        Card cart = byId.orElse(null);

        if (cart == null || cart.getCardStatus().equals(CardStatus.CLOSED)) {
            response.setStatus(404);
            return ApiResult.errorResponse("Card not found", "Card not found", 404);
        }
        if (cart.getCardStatus().equals(CardStatus.ACTIVE)) {
            response.setStatus(404);
            return ApiResult.errorResponse("Card already active", "Card already active", 404);
        }
        if (!byId.get().getETag().equals(UUID.fromString(request.getHeader("If-Match")))) {
            response.setStatus(400);
            return ApiResult.errorResponse("Invalid ETag", "Invalid ETag", 400);

        }

        cart.setCardStatus(CardStatus.ACTIVE);
        cartRepository.save(cart);

        response.setStatus(204);
        return ApiResult.successResponse();
    }
}
