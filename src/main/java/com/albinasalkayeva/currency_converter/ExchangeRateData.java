package com.albinasalkayeva.currency_converter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

public class ExchangeRateData {
    private final ExchangeRateConnector connector;
    private Map<String, String> supportedCodesAndCurrencies;
    private String chosenBaseCurrency;
    private Map<String, BigDecimal> conversionRates;
    private String chosenTargetCurrency;

    public ExchangeRateData(ExchangeRateConnector connector) {
        this.connector = connector;
    }

    public Map<String, String> getSupportedCodesAndCurrencies() {
        if (supportedCodesAndCurrencies == null) {
            populateSupportedCodesAndCurrencies();
        }
        return supportedCodesAndCurrencies;
    }

    public Map<String, BigDecimal> getConversionRates(final String currencyCode) {
        if (!isCurrencyCodeSupported(currencyCode)) {
            throw new CurrencyCodeNotSupportedException();
        }
        if (!currencyCode.equals(chosenBaseCurrency) || conversionRates == null) {
            populateConversionRates(currencyCode);
        }
        return conversionRates;
    }

    public void populateSupportedCodesAndCurrencies() {
        try {
            supportedCodesAndCurrencies = connector.getSupportedCodesAndCurrencies();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void populateConversionRates(final String currencyCode) {
        if (!isCurrencyCodeSupported(currencyCode)) {
            throw new CurrencyCodeNotSupportedException();
        }
        try {
            conversionRates = connector.getConversionRates(currencyCode);
            chosenBaseCurrency = currencyCode;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void populateConversionRates() {
        if (!isCurrencyCodeSupported(chosenBaseCurrency)) {
            throw new CurrencyCodeNotSupportedException();
        }
        try {
            conversionRates = connector.getConversionRates(chosenBaseCurrency);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printSupportedCodesAndCurrencies() {
        System.out.println("Supported codes:");

        for (Map.Entry<String, String> codeAndCurrency : supportedCodesAndCurrencies.entrySet()) {
            System.out.println(codeAndCurrency.getKey() + " - " + codeAndCurrency.getValue());
        }
    }

    public boolean isCurrencyCodeSupported(final String currencyCode) {
        return supportedCodesAndCurrencies.containsKey(currencyCode);
    }

    public String getChosenBaseCurrency() {
        return chosenBaseCurrency;
    }

    public void setChosenBaseCurrency(String currencyCode) {
        if (!isCurrencyCodeSupported(currencyCode)) {
            throw new CurrencyCodeNotSupportedException();
        }
        chosenBaseCurrency = currencyCode;
    }

    public String getChosenTargetCurrency() {
        return chosenTargetCurrency;
    }

    public void setChosenTargetCurrency(String currencyCode) {
        if (!isCurrencyCodeSupported(currencyCode)) {
            throw new CurrencyCodeNotSupportedException();
        }
        chosenTargetCurrency = currencyCode;
    }

    public BigDecimal convertMoneyFromBaseToTargetCurrency(final BigDecimal moneyAmount) {
        return moneyAmount.multiply(getConversionRateOfTargetCurrency());
    }

    public BigDecimal getConversionRateOfTargetCurrency() {
        return conversionRates.get(chosenTargetCurrency);
    }
}
