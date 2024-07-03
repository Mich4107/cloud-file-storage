package ru.yuubi.cloud_file_storage.exception;

public class WrongDataException extends RuntimeException {
    public WrongDataException(String message) {
        super(message);
    }
}
