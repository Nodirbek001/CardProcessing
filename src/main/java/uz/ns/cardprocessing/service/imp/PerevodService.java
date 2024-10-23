package uz.ns.cardprocessing.service.imp;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import uz.ns.cardprocessing.dto.ApiResult;
import uz.ns.cardprocessing.dto.transaction.ReqTransactionDto;
import uz.ns.cardprocessing.dto.transaction.RespTransactionDto;
import uz.ns.cardprocessing.entity.Card;
import uz.ns.cardprocessing.entity.CardStatus;
import uz.ns.cardprocessing.entity.Currency;
import uz.ns.cardprocessing.entity.Transaction;
import uz.ns.cardprocessing.repository.CardRepository;
import uz.ns.cardprocessing.repository.PerevodRepository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class PerevodService {
    private final CardRepository cardRepository;
    private final PerevodRepository perevodRepository;

    public PerevodService(CardRepository cardRepository, PerevodRepository perevodRepository) {
        this.cardRepository = cardRepository;
        this.perevodRepository = perevodRepository;
    }

    public ApiResult<RespTransactionDto> debitCard(String cardId, ReqTransactionDto reqTransactionDto, HttpServletRequest req, HttpServletResponse resp) {
        Optional<Card> byId = cardRepository.findById(UUID.fromString(cardId));

        if (byId.isEmpty() || byId.get().getCardStatus().equals(CardStatus.CLOSED)) {
            resp.setStatus(404);
            return ApiResult.errorResponse("card blocked", "cart not found", 404);
        }
        if (byId.get().getCardStatus().equals(CardStatus.BLOCKED)) {
            return ApiResult.errorResponse("card blocked", "card blocked", 404);
        }
//        if (byId.get().getAmount() < reqTransactionDto.getAmount()) {
//            resp.setStatus(400);
//            return ApiResult.errorResponse("insufficient amount", "insufficient amount", 400);
//        }
        String idempotencyKey = req.getHeader("IdempotencyKey");
        Optional<Transaction> byIdempotencyKey = perevodRepository.findByIdempotencyKey(UUID.fromString(idempotencyKey));
        if (byIdempotencyKey.isPresent()) {
            resp.setStatus(400);
            return ApiResult.errorResponse("UUID error", "UUID error", 400);
        }
        if (reqTransactionDto.getExternal_id().isEmpty() ||
                reqTransactionDto.getAmount() == null ||
                reqTransactionDto.getPurpose() == null) {
            resp.setStatus(400);
            return ApiResult.errorResponse("invalid request", "invalid request", 400);

        }
        Card card = byId.get();

        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setExternalId(UUID.fromString(reqTransactionDto.getExternal_id()));
        transaction.setAmount(reqTransactionDto.getAmount());
        transaction.setUserId(card.getUser().getId());
        transaction.setCardId(UUID.fromString(cardId));
        transaction.setPurpose(reqTransactionDto.getPurpose());
        transaction.setIdempotencyKey(UUID.fromString(idempotencyKey));
        transaction.setDescription("debit");
        transaction.setExchange_rate(getRate());
        transaction.setCurrency(reqTransactionDto.getCurrency());
        if (reqTransactionDto.getCurrency() == null || reqTransactionDto.getCurrency().equals(Currency.UZS)) {
            if (card.getCurrency().equals(Currency.UZS)) {
                transaction.setAfter_balance(card.getAmount() + reqTransactionDto.getAmount());

                card.setAmount(card.getAmount() + reqTransactionDto.getAmount());
                cardRepository.save(card);
                perevodRepository.save(transaction);

            } else {
                transaction.setAfter_balance(card.getAmount() + reqTransactionDto.getAmount() / getRate());
                card.setAmount(card.getAmount() + reqTransactionDto.getAmount() / getRate());
                cardRepository.save(card);
                perevodRepository.save(transaction);

            }

        } else {
            if (card.getCurrency().equals(Currency.USD)) {
                transaction.setAfter_balance(card.getAmount() + reqTransactionDto.getAmount());
                card.setAmount(card.getAmount() + reqTransactionDto.getAmount());
                cardRepository.save(card);
                perevodRepository.save(transaction);
            } else {
                transaction.setAfter_balance(card.getAmount() + reqTransactionDto.getAmount() * getRate());
                card.setAmount(card.getAmount() + reqTransactionDto.getAmount() * getRate());
                cardRepository.save(card);
                perevodRepository.save(transaction);

            }
        }

        return ApiResult.successResponse(
                RespTransactionDto.builder()
                        .amount(transaction.getAmount())
                        .cart_id(String.valueOf(transaction.getCardId()))
                        .after_balance(transaction.getAfter_balance())
                        .currency(transaction.getCurrency())
                        .exchange_rate(transaction.getExchange_rate())
                        .purpose(transaction.getPurpose())
                        .transaction_id(String.valueOf(transaction.getTransactionId()))
                        .external_id(String.valueOf(transaction.getExternalId())).build(), 200

        );

    }

    public Long getRate() {

        JSONArray currencies = new JSONArray(getJson());
        for (int i = 0; i < currencies.length(); i++) {
            JSONObject currency = currencies.getJSONObject(i);
            if (currency.getString("Ccy").equals("USD")) {

                String rate = currency.getString("Rate");
                System.out.println("AQSH dollari (USD) narxi: " + rate);
                return (Long) (long) (Double.parseDouble(rate) * 100); // Topilgandan so'ng, tsiklni to'xtatamiz
            }
        }
        return null;
    }

    public String getJson() {
        try {
            // API URL manzili
            String apiUrl = "https://cbu.uz/ru/arkhiv-kursov-valyut/json/";

            // HttpClient yaratish
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            // So'rovni yuborish va javobni olish
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ApiResult<RespTransactionDto> creditCard(String cardId, ReqTransactionDto reqTransactionDto, HttpServletRequest req, HttpServletResponse resp) {
        Optional<Card> byId = cardRepository.findById(UUID.fromString(cardId));

        if (byId.isEmpty() || byId.get().getCardStatus().equals(CardStatus.CLOSED)) {
            resp.setStatus(404);
            return ApiResult.errorResponse("card blocked", "cart not found", 404);
        }
        if (byId.get().getCardStatus().equals(CardStatus.BLOCKED)) {
            return ApiResult.errorResponse("card blocked", "card blocked", 404);
        }
//        if (byId.get().getAmount() < reqTransactionDto.getAmount()) {
//            resp.setStatus(400);
//            return ApiResult.errorResponse("insufficient amount", "insufficient amount", 400);
//        }
        String idempotencyKey = req.getHeader("IdempotencyKey");
        Optional<Transaction> byIdempotencyKey = perevodRepository.findByIdempotencyKey(UUID.fromString(idempotencyKey));
        if (byIdempotencyKey.isPresent()) {
            resp.setStatus(400);
            return ApiResult.errorResponse("UUID error", "UUID error", 400);
        }
        if (reqTransactionDto.getExternal_id().isEmpty() ||
                reqTransactionDto.getAmount() == null ||
                reqTransactionDto.getPurpose() == null) {
            resp.setStatus(400);
            return ApiResult.errorResponse("invalid request", "invalid request", 400);

        }
        Card card = byId.get();

        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setExternalId(UUID.fromString(reqTransactionDto.getExternal_id()));
        transaction.setAmount(reqTransactionDto.getAmount());
        transaction.setUserId(card.getUser().getId());
        transaction.setCardId(UUID.fromString(cardId));
        transaction.setPurpose(reqTransactionDto.getPurpose());
        transaction.setIdempotencyKey(UUID.fromString(idempotencyKey));
        transaction.setDescription("credit");
        transaction.setExchange_rate(getRate());
        transaction.setCurrency(reqTransactionDto.getCurrency());
        if (reqTransactionDto.getCurrency() == null || reqTransactionDto.getCurrency().equals(Currency.UZS)) {
            if (card.getCurrency().equals(Currency.UZS)) {
                if (card.getAmount() < (reqTransactionDto.getAmount()))
                    return ApiResult.errorResponse("mablag' yetarli emas", "Mablag' yetarli emas", 400);
                transaction.setAfter_balance(card.getAmount() - reqTransactionDto.getAmount());
                card.setAmount(card.getAmount() - reqTransactionDto.getAmount());
                cardRepository.save(card);
                perevodRepository.save(transaction);

            } else {
                if (card.getAmount() < (reqTransactionDto.getAmount() / getRate()))
                    return ApiResult.errorResponse("mablag' yetarli emas", "Mablag' yetarli emas", 400);
                transaction.setAfter_balance(card.getAmount() - reqTransactionDto.getAmount() / getRate());
                card.setAmount(card.getAmount() - reqTransactionDto.getAmount() / getRate());
                cardRepository.save(card);
                perevodRepository.save(transaction);

            }

        } else {
            if (card.getCurrency().equals(Currency.USD)) {
                if (card.getAmount() < (reqTransactionDto.getAmount()))
                    return ApiResult.errorResponse("mablag' yetarli emas", "Mablag' yetarli emas", 400);
                transaction.setAfter_balance(card.getAmount() - reqTransactionDto.getAmount());
                card.setAmount(card.getAmount() - reqTransactionDto.getAmount());
                cardRepository.save(card);
                perevodRepository.save(transaction);
            } else {
                if (card.getAmount() < (reqTransactionDto.getAmount() * getRate()))
                    return ApiResult.errorResponse("mablag' yetarli emas", "Mablag' yetarli emas", 400);
                transaction.setAfter_balance(card.getAmount() - reqTransactionDto.getAmount() * getRate());
                card.setAmount(card.getAmount() - reqTransactionDto.getAmount() * getRate());
                cardRepository.save(card);
                perevodRepository.save(transaction);

            }
        }

        return ApiResult.successResponse(
                RespTransactionDto.builder()
                        .amount(transaction.getAmount())
                        .cart_id(String.valueOf(transaction.getCardId()))
                        .after_balance(transaction.getAfter_balance())
                        .currency(transaction.getCurrency())
                        .exchange_rate(transaction.getExchange_rate())
                        .purpose(transaction.getPurpose())
                        .transaction_id(String.valueOf(transaction.getTransactionId()))
                        .external_id(String.valueOf(transaction.getExternalId())).build(), 200

        );

    }
}
