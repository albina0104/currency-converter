package com.albinasalkayeva;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        appStartup();
    }

    private static void appStartup() throws IOException {
        Dotenv dotenv = Dotenv.load();

        String EXCHANGE_RATE_SUPPORTED_CODES_URL = "https://v6.exchangerate-api.com/v6/%s/codes";
        String EXCHANGE_RATE_CONVERSION_RATES_URL = "https://v6.exchangerate-api.com/v6/%s/latest/%s";
        String EXCHANGE_RATE_PAIR_CONVERSION_RATE_URL = "https://v6.exchangerate-api.com/v6/%s/pair/%s/%s";
        String EXCHANGE_RATE_API_KEY = dotenv.get("EXCHANGE_RATE_API_KEY");

        Map<String, String> supportedCodes =
                getSupportedCodes(EXCHANGE_RATE_SUPPORTED_CODES_URL, EXCHANGE_RATE_API_KEY);
        Map<String, BigDecimal> conversionRates = getConversionRates(EXCHANGE_RATE_CONVERSION_RATES_URL,
                EXCHANGE_RATE_API_KEY, "USD");
//        BigDecimal pairConversionRate = getPairConversionRate(EXCHANGE_RATE_PAIR_CONVERSION_RATE_URL,
//                EXCHANGE_RATE_API_KEY, "USD", "EUR");

        System.out.println("Supported codes: " + supportedCodes);
        System.out.println("Conversion rates: " + conversionRates);
    }

    private static BigDecimal getPairConversionRate(String url, String apiKey,
                                                    String baseCurrency,
                                                    String targetCurrency) throws IOException {

        URL exchangeRatePairConversionRateUrl = URI
                .create(String.format(url, apiKey, baseCurrency, targetCurrency))
                .toURL();

        JSONObject pairConversionRateJson = getJsonFromUrl(exchangeRatePairConversionRateUrl);
        return pairConversionRateJson.getBigDecimal("conversion_rate");
    }

    private static Map<String, BigDecimal> getConversionRates(String url, String apiKey,
                                                              String currencyCode) throws IOException {

        String fileName = "conversion_rates.json";

        JSONObject conversionRatesJson = null;

        boolean isConversionRatesFileExists = isFileExists(fileName);
        boolean isTimeToUpdateConversionRates = false;
        if (isConversionRatesFileExists) {
            conversionRatesJson = getJsonFromFile(fileName);
            System.out.println("Conversion rates read from file");
            isTimeToUpdateConversionRates = isTimeToUpdateFileContents(conversionRatesJson);
        }
        if (!isConversionRatesFileExists || isTimeToUpdateConversionRates) {
            URL exchangeRateConversionRatesUrl = URI.create(String.format(url, apiKey, currencyCode)).toURL();
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

    private static Map<String, String> getSupportedCodes(String url, String apiKey) throws IOException {

        String fileName = "supported_codes.json";

        JSONObject supportedCodesJson;

        if (isFileExists(fileName)) {
            supportedCodesJson = getJsonFromFile(fileName);
            System.out.println("Supported codes read from file");
        } else {
            URL exchangeRateSupportedCodesUrl = URI.create(String.format(url, apiKey)).toURL();
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

    public static JSONObject getJsonFromUrl(URL url) throws IOException {
        String json = IOUtils.toString(url, StandardCharsets.UTF_8);
        return new JSONObject(json);
    }

    public static void saveJsonToFile(JSONObject jsonObject, String fileName) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)
        ) {
            objectOutputStream.writeObject(jsonObject.toString());
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject getJsonFromFile(String fileName) {
        try (FileInputStream fileInputStream = new FileInputStream(fileName);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)
        ) {
            String str = (String) objectInputStream.readObject();
            return new JSONObject(str);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isFileExists(String fileName) {
        Path path = Paths.get(fileName);
        return Files.exists(path);
    }
}