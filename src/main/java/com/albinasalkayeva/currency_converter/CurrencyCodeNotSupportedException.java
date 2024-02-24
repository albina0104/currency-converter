package com.albinasalkayeva.currency_converter;

public class CurrencyCodeNotSupportedException extends RuntimeException {
    public CurrencyCodeNotSupportedException() {
        super("This currency code is not supported!");
    }
}
