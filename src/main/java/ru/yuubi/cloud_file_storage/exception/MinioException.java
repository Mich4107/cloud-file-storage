package ru.yuubi.cloud_file_storage.exception;

public class MinioException extends RuntimeException {
    public MinioException(String message) {
        super(message);
    }
}
