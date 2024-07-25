package ru.yuubi.cloud_file_storage.util;

import io.minio.Result;
import io.minio.messages.Item;
import lombok.experimental.UtilityClass;
import ru.yuubi.cloud_file_storage.util.exception.MinioExceptionHandlerUtil;

@UtilityClass
public class MinioUtil {

    public String getUserRootFolderPrefix(Integer userId) {
        return String.format("user-%d-files/", userId);
    }

    public Item getItemFromResult(Result<Item> result) {
        return MinioExceptionHandlerUtil.handleExceptions(result::get);
    }

    public String removePackagesFromString(String objectName, String packagesToRemove) {
        return objectName.replaceAll(packagesToRemove, "");
    }
}
