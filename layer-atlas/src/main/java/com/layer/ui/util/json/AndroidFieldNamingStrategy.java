package com.layer.ui.util.json;

import com.google.gson.FieldNamingStrategy;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

public class AndroidFieldNamingStrategy implements FieldNamingStrategy {
    private static final String JSON_WORD_DELIMITER = "_";
    private final static Pattern UPPERCASE_PATTERN = Pattern.compile("(?=\\p{Lu})");

    @Override
    public String translateName(final Field f) {
        if (f.getName().startsWith("m")) {
            return handleWords(f.getName().substring(1));
        } else {
            return handleWords(f.getName());
        }
    }

    static String handleWords(final String fieldName) {
        String[] words = UPPERCASE_PATTERN.split(fieldName);
        final StringBuffer sb = new StringBuffer();
        for (String word : words) {
            if (sb.length() > 0) {
                sb.append(JSON_WORD_DELIMITER);
            }
            sb.append(word.toLowerCase());
        }
        return sb.toString();
    }
}
