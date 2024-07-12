package ru.yuubi.cloud_file_storage.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ControllerUtil {

    public static final int CHARACTER_LIMIT = 220;

    public static boolean containsSpecialCharacters(String name) {
        String regex = "[/\\\\<>*?|:]";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(name);

        return matcher.find();
    }

    public static Map<String, String> createBreadcrumb(String subdirectory) {

        Map<String, String> breadcrumb = new LinkedHashMap<>();

        String baseUrl = "/main-page";
        breadcrumb.put(baseUrl, "Main");

        String[] paths = subdirectory.split("/");

        StringBuilder pathBuilder = new StringBuilder();

        for (String path : paths) {
            pathBuilder.append(path).append("/");
            String url = String.format("%s?path=%s", baseUrl, pathBuilder);
            breadcrumb.put(url, path);
        }

        return breadcrumb;
    }
}
