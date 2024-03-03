package com.albinasalkayeva.currency_converter;

import org.json.JSONObject;
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
}
