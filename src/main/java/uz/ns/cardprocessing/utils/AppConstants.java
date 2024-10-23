package uz.ns.cardprocessing.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface AppConstants {

    ObjectMapper objectMapper = new ObjectMapper();

    String[] OPEN_PAGES = {

            "/api/register",

    };
}
 /*
         "/api/cards",
         "/api/v1/cards/{cardId}",
         "/api/v1/cards/{cardId}/block",
         "/api/v1/cards/{cardId}/debit",
         "/api/v1/cards/{cardId}/credit",
         "/api/v1/cards/{cardId}/transactions"

  */