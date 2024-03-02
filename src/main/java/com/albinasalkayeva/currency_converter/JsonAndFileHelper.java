package com.albinasalkayeva.currency_converter;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JsonAndFileHelper {
    public static JSONObject getJsonFromUrl(URL url) throws IOException {
        String json = IOUtils.toString(url, StandardCharsets.UTF_8);
        return new JSONObject(json);
    }

    public static void saveJsonToFile(JSONObject jsonObject, String fileName) throws IOException {
        Files.writeString(Path.of(fileName), jsonObject.toString(), StandardCharsets.UTF_8);
    }

    public static JSONObject getJsonFromFile(String fileName) throws IOException {
        String str = Files.readString(Path.of(fileName));
        return new JSONObject(str);
    }

    public static boolean isFileExists(String fileName) {
        Path path = Paths.get(fileName);
        return Files.exists(path);
    }
}
