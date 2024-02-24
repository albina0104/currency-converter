package com.albinasalkayeva.currency_converter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

public class ExchangeRateData {
    private Map<String, String> supportedCodes;
    private String chosenBaseCurrency;
    private Map<String, BigDecimal> conversionRates;

    public Map<String, String> getSupportedCodes() {
        if (supportedCodes == null) {
            populateSupportedCodes();
        }
        return supportedCodes;
    }

    public Map<String, BigDecimal> getConversionRates(final String currencyCode) {
        if (!currencyCode.equals(chosenBaseCurrency) || conversionRates == null) {
            populateConversionRates(currencyCode);
        }
        return conversionRates;
    }

    private void populateSupportedCodes() {
        try {
            supportedCodes = ExchangeRateConnector.getSupportedCodes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateConversionRates(final String currencyCode) {
        try {
            conversionRates = ExchangeRateConnector.getConversionRates(currencyCode);
            chosenBaseCurrency = currencyCode;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
