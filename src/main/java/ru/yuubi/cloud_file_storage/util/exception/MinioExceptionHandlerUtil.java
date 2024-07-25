package ru.yuubi.cloud_file_storage.util.exception;

import lombok.experimental.UtilityClass;
import ru.yuubi.cloud_file_storage.exception.MinioException;

@UtilityClass
public class MinioExceptionHandlerUtil {

    public <T> T handleExceptions(ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    public void handleVoidExceptions(ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }
}
