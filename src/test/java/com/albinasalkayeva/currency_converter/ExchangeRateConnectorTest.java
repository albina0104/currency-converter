package com.albinasalkayeva.currency_converter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExchangeRateConnectorTest {
    @Mock
    JsonAndFileHelper jsonAndFileHelper;

    @ParameterizedTest
    @CsvFileSource(resources = "/conversionRates.csv")
    public void testGetConversionRates_FileExistsAndUpToDate(String baseCurrencyCode, String targetCurrencyCode,
                                                             BigDecimal conversionRate) throws IOException {
        // Given
        String fileName = "conversion_rates_" + baseCurrencyCode + ".json";
        ExchangeRateConnector connector = new ExchangeRateConnector(jsonAndFileHelper);
        Date currentDate = new Date();
        int secondsInFuture = 5;
        long timestampInFutureUnix = currentDate.getTime() / 1000 + secondsInFuture;

        when(jsonAndFileHelper.isFileExists(fileName)).thenReturn(true);
        JSONObject mockJson = new JSONObject();
        mockJson.put("time_next_update_unix", timestampInFutureUnix);
        mockJson.put("conversion_rates", new JSONObject().put(targetCurrencyCode, conversionRate));
        when(jsonAndFileHelper.getJsonFromFile(fileName)).thenReturn(mockJson);

        // When
        Map<String, BigDecimal> result = connector.getConversionRates(baseCurrencyCode);

        // Then
        InOrder inOrder = inOrder(jsonAndFileHelper);
        inOrder.verify(jsonAndFileHelper, times(1)).isFileExists(fileName);
        inOrder.verify(jsonAndFileHelper, times(1)).getJsonFromFile(fileName);
        verify(jsonAndFileHelper, never()).getJsonFromUrl(any());
        verify(jsonAndFileHelper, never()).saveJsonToFile(any(), any());
        assertEquals(1, result.size());
        assertTrue(result.containsKey(targetCurrencyCode));
        assertEquals(conversionRate, result.get(targetCurrencyCode));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/conversionRates2.csv")
    public void testGetConversionRates_FileExistsButNeedsUpdate(String baseCurrencyCode, String targetCurrencyCode,
                                                                BigDecimal conversionRateBeforeUpdate,
                                                                BigDecimal conversionRateAfterUpdate) throws IOException {
        // Given
        String fileName = "conversion_rates_" + baseCurrencyCode + ".json";
        ExchangeRateConnector connector = new ExchangeRateConnector(jsonAndFileHelper);
        Date currentDate = new Date();
        int seconds = 5;
        long timestampInPastUnix = currentDate.getTime() / 1000 - seconds;
        long timestampInFutureUnix = currentDate.getTime() / 1000 + seconds;

        when(jsonAndFileHelper.isFileExists(fileName)).thenReturn(true);

        JSONObject mockJsonBeforeUpdate = new JSONObject();
        mockJsonBeforeUpdate.put("time_next_update_unix", timestampInPastUnix);
        mockJsonBeforeUpdate.put("conversion_rates", new JSONObject().put(targetCurrencyCode, conversionRateBeforeUpdate));

        JSONObject mockJsonAfterUpdate = new JSONObject();
        mockJsonAfterUpdate.put("time_next_update_unix", timestampInFutureUnix);
        mockJsonAfterUpdate.put("conversion_rates", new JSONObject().put(targetCurrencyCode, conversionRateAfterUpdate));

        when(jsonAndFileHelper.getJsonFromFile(fileName)).thenReturn(mockJsonBeforeUpdate);
        when(jsonAndFileHelper.getJsonFromUrl(any())).thenReturn(mockJsonAfterUpdate);

        // When
        Map<String, BigDecimal> result = connector.getConversionRates(baseCurrencyCode);

        // Then
        InOrder inOrder = inOrder(jsonAndFileHelper);
        inOrder.verify(jsonAndFileHelper, times(1)).isFileExists(fileName);
        inOrder.verify(jsonAndFileHelper, times(1)).getJsonFromFile(fileName);
        inOrder.verify(jsonAndFileHelper, times(1)).getJsonFromUrl(any());
        inOrder.verify(jsonAndFileHelper, times(1)).saveJsonToFile(any(), any());
        assertEquals(1, result.size());
        assertTrue(result.containsKey(targetCurrencyCode));
        assertEquals(conversionRateAfterUpdate, result.get(targetCurrencyCode));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/conversionRates.csv")
    public void testGetConversionRates_FileDoesNotExist(String baseCurrencyCode, String targetCurrencyCode,
                                                        BigDecimal conversionRate) throws IOException {
        // Given
        String fileName = "conversion_rates_" + baseCurrencyCode + ".json";
        ExchangeRateConnector connector = new ExchangeRateConnector(jsonAndFileHelper);
        Date currentDate = new Date();
        int secondsInFuture = 5;
        long timestampInFutureUnix = currentDate.getTime() / 1000 + secondsInFuture;

        when(jsonAndFileHelper.isFileExists(fileName)).thenReturn(false);
        JSONObject mockJson = new JSONObject();
        mockJson.put("time_next_update_unix", timestampInFutureUnix);
        mockJson.put("conversion_rates", new JSONObject().put(targetCurrencyCode, conversionRate));
        when(jsonAndFileHelper.getJsonFromUrl(any())).thenReturn(mockJson);

        // When
        Map<String, BigDecimal> result = connector.getConversionRates(baseCurrencyCode);

        // Then
        InOrder inOrder = inOrder(jsonAndFileHelper);
        inOrder.verify(jsonAndFileHelper, times(1)).isFileExists(fileName);
        verify(jsonAndFileHelper, never()).getJsonFromFile(fileName);
        inOrder.verify(jsonAndFileHelper, times(1)).getJsonFromUrl(any());
        inOrder.verify(jsonAndFileHelper, times(1)).saveJsonToFile(any(), any());
        assertEquals(1, result.size());
        assertTrue(result.containsKey(targetCurrencyCode));
        assertEquals(conversionRate, result.get(targetCurrencyCode));
    }

    @Test
    public void testGetSupportedCodesAndCurrencies_FileExists() throws IOException {
        // Given
        String fileName = "supported_codes.json";
        ExchangeRateConnector connector = new ExchangeRateConnector(jsonAndFileHelper);

        when(jsonAndFileHelper.isFileExists(fileName)).thenReturn(true);
        JSONObject mockJsonSupportedCodes = new JSONObject();
        JSONArray mockJsonSupportedCodesArray = new JSONArray();
        JSONArray mockJsonKzt = new JSONArray();
        mockJsonKzt.put("KZT");
        mockJsonKzt.put("Kazakhstani Tenge");
        JSONArray mockJsonRub = new JSONArray();
        mockJsonRub.put("RUB");
        mockJsonRub.put("Russian Ruble");
        JSONArray mockJsonUsd = new JSONArray();
        mockJsonUsd.put("USD");
        mockJsonUsd.put("United States Dollar");
        mockJsonSupportedCodesArray.put(mockJsonKzt);
        mockJsonSupportedCodesArray.put(mockJsonRub);
        mockJsonSupportedCodesArray.put(mockJsonUsd);
        mockJsonSupportedCodes.put("supported_codes", mockJsonSupportedCodesArray);
        when(jsonAndFileHelper.getJsonFromFile(fileName)).thenReturn(mockJsonSupportedCodes);

        // When
        Map<String, String> result = connector.getSupportedCodesAndCurrencies();

        // Then
        InOrder inOrder = inOrder(jsonAndFileHelper);
        inOrder.verify(jsonAndFileHelper, times(1)).isFileExists(fileName);
        inOrder.verify(jsonAndFileHelper, times(1)).getJsonFromFile(fileName);
        verify(jsonAndFileHelper, never()).getJsonFromUrl(any());
        verify(jsonAndFileHelper, never()).saveJsonToFile(any(), any());
        assertEquals(3, result.size());
        assertTrue(result.containsKey("KZT"));
        assertTrue(result.containsKey("RUB"));
        assertTrue(result.containsKey("USD"));
        assertEquals("Kazakhstani Tenge", result.get("KZT"));
        assertEquals("Russian Ruble", result.get("RUB"));
        assertEquals("United States Dollar", result.get("USD"));
    }
}
