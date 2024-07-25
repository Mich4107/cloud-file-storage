package ru.yuubi.cloud_file_storage.util;

import io.minio.Result;
import io.minio.messages.Item;
import lombok.experimental.UtilityClass;
import ru.yuubi.cloud_file_storage.repository.MinioRepository;
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

    public boolean isOneObjectOnPath(String userPath, String pathToObject, MinioRepository minioRepository) {
        if (pathToObject != null) {
            Iterable<Result<Item>> results = minioRepository.findObjects(userPath + pathToObject);
            return isOneObjectOnParticularPath(results);
        }
        return false;
    }

    private boolean isOneObjectOnParticularPath(Iterable<Result<Item>> results) {
        int counter = 0;
        for (Result<Item> result : results) {
            counter++;
        }
        return counter == 1;
    }
}
