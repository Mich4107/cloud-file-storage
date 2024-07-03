package ru.yuubi.cloud_file_storage.exception;

public class NoSuchObjectException extends RuntimeException{
    public NoSuchObjectException() {
        super("No such object found");
    }
}
