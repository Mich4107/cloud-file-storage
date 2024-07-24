package ru.yuubi.cloud_file_storage.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FormatUtil {

    public String clearPackagesFromName(String name) {
        String[] split = name.split("/");
        return split[split.length - 1];
    }

    public String formatNameToZip(String name) {
        name = clearPackagesFromName(name);
        return name+".zip";
    }
}
