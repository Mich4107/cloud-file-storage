package ru.yuubi.cloud_file_storage.util;

public class FormatUtil {

    public static String clearPackagesFromName(String name) {
        String[] split = name.split("/");
        return split[split.length - 1];
    }

    public static String formatNameToZip(String name) {
        name = clearPackagesFromName(name);
        return name+".zip";
    }
}
