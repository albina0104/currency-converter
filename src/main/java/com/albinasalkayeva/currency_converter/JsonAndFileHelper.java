package com.albinasalkayeva.currency_converter;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public interface JsonAndFileHelper {
    JSONObject getJsonFromUrl(URL url) throws IOException;

    void saveJsonToFile(JSONObject jsonObject, String fileName) throws IOException;

    JSONObject getJsonFromFile(String fileName) throws IOException;

    boolean isFileExists(String fileName);
}
