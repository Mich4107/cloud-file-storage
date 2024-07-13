package ru.yuubi.cloud_file_storage.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class BreadcrumbUtil {
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
