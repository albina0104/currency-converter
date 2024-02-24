package com.albinasalkayeva.currency_converter;

import java.math.BigDecimal;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ExchangeRateData exchangeRateData = new ExchangeRateData();

        exchangeRateData.populateSupportedCodesAndCurrencies();
        System.out.println();
        exchangeRateData.printSupportedCodesAndCurrencies();

        Scanner scanner = new Scanner(System.in);
        System.out.println();
        System.out.print("Input code of base currency: ");

        exchangeRateData.setChosenBaseCurrency(getCurrencyCodeFromUserInput(scanner, exchangeRateData));
        exchangeRateData.populateConversionRates();
        System.out.println("Chosen base currency: " + exchangeRateData.getChosenBaseCurrency());

        System.out.println();
        System.out.print("Input code of target currency: ");

        exchangeRateData.setChosenTargetCurrency(getCurrencyCodeFromUserInput(scanner, exchangeRateData));
        System.out.println("Chosen target currency: " + exchangeRateData.getChosenTargetCurrency());

        System.out.println();
        System.out.println("How much " + exchangeRateData.getChosenBaseCurrency() + " would you like to convert to "
                + exchangeRateData.getChosenTargetCurrency() + "?");

        BigDecimal moneyAmount = getMoneyAmountFromUserInput(scanner);
        BigDecimal convertedMoneyAmount = exchangeRateData.convertMoneyFromBaseToTargetCurrency(moneyAmount);
        System.out.println();
        System.out.println("Result: " + moneyAmount + " " + exchangeRateData.getChosenBaseCurrency()
                + " is " + convertedMoneyAmount + " " + exchangeRateData.getChosenTargetCurrency());
    }

    private static String getCurrencyCodeFromUserInput(Scanner scanner, ExchangeRateData exchangeRateData) {
        String currencyCode;
        do {
            currencyCode = scanner.nextLine();
            if (!exchangeRateData.isCurrencyCodeSupported(currencyCode)) {
                System.out.print("This currency code is not supported! Please input the valid currency: ");
            }
        } while (!exchangeRateData.isCurrencyCodeSupported(currencyCode));

        return currencyCode;
    }

    private static BigDecimal getMoneyAmountFromUserInput(Scanner scanner) {
        return scanner.nextBigDecimal();
    }
}