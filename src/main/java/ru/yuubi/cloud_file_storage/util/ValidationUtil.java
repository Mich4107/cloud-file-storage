package ru.yuubi.cloud_file_storage.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtil {
    public static final int CHARACTER_LIMIT = 220;

    public static boolean containsSpecialCharacters(String name) {
        String regex = "[/\\\\<>*?|:]";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(name);

        return matcher.find();
    }
}
