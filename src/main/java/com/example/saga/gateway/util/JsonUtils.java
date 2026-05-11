package com.example.saga.gateway.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static JsonNode parse(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (Exception e) {
            return null;
        }
    }

    public static String extractField(String json, String field) {
        try {
            JsonNode node = MAPPER.readTree(json);
            JsonNode v = node.path(field);

            if (v.isMissingNode() || v.isNull()) return null;

            String text = v.asText();
            if (text == null) return null;

            text = text.trim();
            return text.isEmpty() ? null : text;

        } catch (Exception e) {
            return null;
        }
    }
}
