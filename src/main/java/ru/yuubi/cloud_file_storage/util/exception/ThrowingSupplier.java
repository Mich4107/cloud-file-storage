package ru.yuubi.cloud_file_storage.util.exception;

@FunctionalInterface
public interface ThrowingSupplier<T>{
    T get() throws Exception;
}
