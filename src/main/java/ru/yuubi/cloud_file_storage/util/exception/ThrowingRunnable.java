package ru.yuubi.cloud_file_storage.util.exception;

@FunctionalInterface
public interface ThrowingRunnable {
    void run() throws Exception;
}
