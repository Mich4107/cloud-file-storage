package ru.yuubi.cloud_file_storage.util;

import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ValidationUtil {
    public final int CHARACTER_LIMIT = 220;
    private final Pattern pattern = Pattern.compile("[/\\\\<>*?|:]");

    public boolean containsSpecialCharacters(String name) {
        Matcher matcher = pattern.matcher(name);
        return matcher.find();
    }
}
