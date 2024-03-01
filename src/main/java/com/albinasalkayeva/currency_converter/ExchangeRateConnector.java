package com.albinasalkayeva.currency_converter;

import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.albinasalkayeva.currency_converter.JsonAndFileHelper.*;

public class ExchangeRateConnector {

    private static final String EXCHANGE_RATE_SUPPORTED_CODES_URL = "https://v6.exchangerate-api.com/v6/%s/codes";
    private static final String EXCHANGE_RATE_CONVERSION_RATES_URL = "https://v6.exchangerate-api.com/v6/%s/latest/%s";
    private static final String EXCHANGE_RATE_PAIR_CONVERSION_RATE_URL = "https://v6.exchangerate-api.com/v6/%s/pair/%s/%s";
    private static final String EXCHANGE_RATE_API_KEY;

    static {
        Dotenv dotenv = Dotenv.load();
        EXCHANGE_RATE_API_KEY = dotenv.get("EXCHANGE_RATE_API_KEY");
    }

    protected static BigDecimal getPairConversionRate(final String baseCurrency,
                                                      final String targetCurrency) throws IOException {

        URL exchangeRatePairConversionRateUrl = URI
                .create(String.format(EXCHANGE_RATE_PAIR_CONVERSION_RATE_URL,
                        EXCHANGE_RATE_API_KEY, baseCurrency, targetCurrency))
                .toURL();

        JSONObject pairConversionRateJson = getJsonFromUrl(exchangeRatePairConversionRateUrl);
        return pairConversionRateJson.getBigDecimal("conversion_rate");
    }

    protected static Map<String, BigDecimal> getConversionRates(final String currencyCode) throws IOException {

        String fileName = "conversion_rates_" + currencyCode + ".json";

        JSONObject conversionRatesJson = null;

        boolean isConversionRatesFileExists = isFileExists(fileName);
        boolean isTimeToUpdateConversionRates = false;
        if (isConversionRatesFileExists) {
            conversionRatesJson = getJsonFromFile(fileName);
            System.out.println("Conversion rates read from file");
            isTimeToUpdateConversionRates = isTimeToUpdateFileContents(conversionRatesJson);
        }
        if (!isConversionRatesFileExists || isTimeToUpdateConversionRates) {
            URL exchangeRateConversionRatesUrl = URI
                    .create(String.format(EXCHANGE_RATE_CONVERSION_RATES_URL, EXCHANGE_RATE_API_KEY, currencyCode))
                    .toURL();
            conversionRatesJson = getJsonFromUrl(exchangeRateConversionRatesUrl);
            saveJsonToFile(conversionRatesJson, fileName);
            System.out.println("Conversion rates saved to file");
        }

        JSONObject conversionRates = conversionRatesJson.getJSONObject("conversion_rates");

        Map<String, BigDecimal> codesAndConversionRates = new LinkedHashMap<>();

        conversionRates.keys().forEachRemaining(key -> {
            BigDecimal conversionRate = conversionRates.getBigDecimal(key);
            codesAndConversionRates.put(key, conversionRate);
        });

        return codesAndConversionRates;
    }

    private static boolean isTimeToUpdateFileContents(JSONObject conversionRatesJson) {
        Date currentTime = new Date();
        Date timeOfNextUpdate = new Date();
        long timeNextUpdateUnix = conversionRatesJson.getLong("time_next_update_unix");
        timeOfNextUpdate.setTime(timeNextUpdateUnix * 1000);
        System.out.println("Current time is " + currentTime);
        System.out.println("The next update time is " + timeOfNextUpdate);
        if (currentTime.compareTo(timeOfNextUpdate) >= 0) {
            System.out.println("Time to update conversion rates");
            return true;
        }
        return false;
    }

    protected static Map<String, String> getSupportedCodesAndCurrencies() throws IOException {

        String fileName = "supported_codes.json";

        JSONObject supportedCodesJson;

        if (isFileExists(fileName)) {
            supportedCodesJson = getJsonFromFile(fileName);
            System.out.println("Supported codes read from file");
        } else {
            URL exchangeRateSupportedCodesUrl = URI
                    .create(String.format(EXCHANGE_RATE_SUPPORTED_CODES_URL, EXCHANGE_RATE_API_KEY))
                    .toURL();
            supportedCodesJson = getJsonFromUrl(exchangeRateSupportedCodesUrl);
            saveJsonToFile(supportedCodesJson, fileName);
            System.out.println("Supported codes saved to file");
        }

        JSONArray supportedCodes = supportedCodesJson.getJSONArray("supported_codes");

        Map<String, String> codesAndCurrencies = new LinkedHashMap<>();

        for (Object codeAndCurrencyObj : supportedCodes) {
            JSONArray codeAndCurrency = (JSONArray) codeAndCurrencyObj;
            String code = codeAndCurrency.getString(0);
            String currency = codeAndCurrency.getString(1);
            codesAndCurrencies.put(code, currency);
        }

        return codesAndCurrencies;
    }
}
