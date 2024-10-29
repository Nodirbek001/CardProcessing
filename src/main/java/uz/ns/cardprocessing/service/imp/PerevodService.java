package uz.ns.cardprocessing.service.imp;

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

    public ApiResult<RespTransactionDto> debitCard(String cardId, ReqTransactionDto reqTransactionDto, String header, HttpServletResponse resp) {


        Optional<Card> byId = cardRepository.findById(UUID.fromString(cardId));

        String checkCard = checkCard(resp, header, reqTransactionDto, byId);
        if (checkCard != null) {
            return ApiResult.errorResponse(checkCard, checkCard, resp.getStatus());
        }
        Card card = byId.orElseThrow();

        Transaction transaction = createTransaction(reqTransactionDto, header, card);
        transaction.setDescription("debit");
        if (reqTransactionDto.getCurrency() == null || reqTransactionDto.getCurrency().equals(Currency.UZS)) {
            if (card.getCurrency().equals(Currency.UZS)) {
                transaction.setAfter_balance(card.getAmount() + reqTransactionDto.getAmount());
                card.setAmount(card.getAmount() + reqTransactionDto.getAmount());

            } else {
                transaction.setAfter_balance(card.getAmount() + reqTransactionDto.getAmount() / getRate());
                card.setAmount(card.getAmount() + reqTransactionDto.getAmount() / getRate());
            }

        } else {
            if (card.getCurrency().equals(Currency.USD)) {
                transaction.setAfter_balance(card.getAmount() + reqTransactionDto.getAmount());
                card.setAmount(card.getAmount() + reqTransactionDto.getAmount());

            } else {
                transaction.setAfter_balance(card.getAmount() + reqTransactionDto.getAmount() * getRate());
                card.setAmount(card.getAmount() + reqTransactionDto.getAmount() * getRate());
            }
        }
        return save(card, transaction);

    }


    public ApiResult<RespTransactionDto> creditCard(String cardId, ReqTransactionDto reqTransactionDto, String header, HttpServletResponse resp) {
        Optional<Card> byId = cardRepository.findById(UUID.fromString(cardId));
        String check = checkCard(resp, header, reqTransactionDto, byId);
        if (check != null) {
            return ApiResult.errorResponse(check, check, resp.getStatus());
        }
        Card card = byId.orElseThrow();

        Transaction transaction = createTransaction(reqTransactionDto, header, card);
        transaction.setDescription("credit");
        if (reqTransactionDto.getCurrency() == null || reqTransactionDto.getCurrency().equals(Currency.UZS)) {
            if (card.getCurrency().equals(Currency.UZS)) {
                if (card.getAmount() < (reqTransactionDto.getAmount()))
                    return ApiResult.errorResponse("mablag' yetarli emas", "Mablag' yetarli emas", 400);
                transaction.setAfter_balance(card.getAmount() - reqTransactionDto.getAmount());
                card.setAmount(card.getAmount() - reqTransactionDto.getAmount());
            } else {
                if (card.getAmount() < (reqTransactionDto.getAmount() / getRate()))
                    return ApiResult.errorResponse("mablag' yetarli emas", "Mablag' yetarli emas", 400);
                transaction.setAfter_balance(card.getAmount() - reqTransactionDto.getAmount() / getRate());
                card.setAmount(card.getAmount() - reqTransactionDto.getAmount() / getRate());
            }

        } else {
            if (card.getCurrency().equals(Currency.USD)) {
                if (card.getAmount() < (reqTransactionDto.getAmount()))
                    return ApiResult.errorResponse("mablag' yetarli emas", "Mablag' yetarli emas", 400);
                transaction.setAfter_balance(card.getAmount() - reqTransactionDto.getAmount());
                card.setAmount(card.getAmount() - reqTransactionDto.getAmount());
            } else {
                if (card.getAmount() < (reqTransactionDto.getAmount() * getRate()))
                    return ApiResult.errorResponse("mablag' yetarli emas", "Mablag' yetarli emas", 400);
                transaction.setAfter_balance(card.getAmount() - reqTransactionDto.getAmount() * getRate());
                card.setAmount(card.getAmount() - reqTransactionDto.getAmount() * getRate());
            }
        }
        return save(card, transaction);

    }

    private ApiResult<RespTransactionDto> save(Card card, Transaction transaction) {
        cardRepository.save(card);
        perevodRepository.save(transaction);

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

    public Transaction createTransaction(ReqTransactionDto reqTransactionDto, String header, Card card) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setExternalId(UUID.fromString(reqTransactionDto.getExternal_id()));
        transaction.setAmount(reqTransactionDto.getAmount());
        transaction.setUserId(card.getUser().getId());
        transaction.setCardId(card.getId());
        transaction.setPurpose(reqTransactionDto.getPurpose());
        transaction.setIdempotencyKey(UUID.fromString(header));
        transaction.setExchange_rate(getRate());
        transaction.setCurrency(reqTransactionDto.getCurrency());
        return transaction;
    }

    public String checkCard(HttpServletResponse resp, String header, ReqTransactionDto reqTransactionDto, Optional<Card> byId) {
        if (byId.isEmpty() || byId.get().getCardStatus().equals(CardStatus.CLOSED)) {
            resp.setStatus(404);
            return "card blocked";
        }
        if (byId.get().getCardStatus().equals(CardStatus.BLOCKED)) {
            return "card blocked";
        }

        Optional<Transaction> byIdempotencyKey = perevodRepository.findByIdempotencyKey(UUID.fromString(header));
        if (byIdempotencyKey.isPresent()) {
            resp.setStatus(400);
            return "UUID error";
        }
        if (reqTransactionDto.getExternal_id().isEmpty() ||
                reqTransactionDto.getAmount() == null ||
                reqTransactionDto.getPurpose() == null) {
            resp.setStatus(400);
            return "invalid request";

        }
        return null;
    }
}
