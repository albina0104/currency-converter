package com.albinasalkayeva.currency_converter;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JsonAndFileHelperImpl implements JsonAndFileHelper {
    @Override
    public JSONObject getJsonFromUrl(URL url) throws IOException {
        String json = IOUtils.toString(url, StandardCharsets.UTF_8);
        return new JSONObject(json);
    }

    @Override
    public void saveJsonToFile(JSONObject jsonObject, String fileName) throws IOException {
        Files.writeString(Path.of(fileName), jsonObject.toString(), StandardCharsets.UTF_8);
    }

    @Override
    public JSONObject getJsonFromFile(String fileName) throws IOException {
        String str = Files.readString(Path.of(fileName));
        return new JSONObject(str);
    }

    @Override
    public boolean isFileExists(String fileName) {
        Path path = Paths.get(fileName);
        return Files.exists(path);
    }
}
