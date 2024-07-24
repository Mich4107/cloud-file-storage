package ru.yuubi.cloud_file_storage.repository;

import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static ru.yuubi.cloud_file_storage.util.exception.ExceptionHandlerUtil.*;


@Repository
@RequiredArgsConstructor
public class MinioRepository {

    @Value("${bucket-name}")
    private String bucketName;

    private final MinioClient minioClient;

    public void removeObject(String objectName) {
        handleVoidExceptions(() ->
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .build())
        );
    }

    public void copyObjectWithNewName(String oldObjectName, String newObjectName) {
        handleVoidExceptions(() ->
                minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(bucketName)
                                .object(newObjectName)
                                .source(
                                        CopySource.builder()
                                                .bucket(bucketName)
                                                .object(oldObjectName)
                                                .build()
                                ).build()
                ));
    }

    public void uploadObject(MultipartFile file, String objectName) {
        handleVoidExceptions(() ->
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(file.getInputStream(), file.getSize(), -1)
                                .build()
                ));
    }


    public InputStream getObjectInputStream(String objectName) {
        return handleExceptions(() ->
                minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .build()
                )
        );
    }

    public Iterable<Result<Item>> findObjectsRecursively(String path) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(path)
                        .recursive(true)
                        .build()
        );
    }

    public Iterable<Result<Item>> findObjects(String path) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(path)
                        .build()
        );
    }

    public void uploadEmptyDirectory(String objectName) {
        handleVoidExceptions(() ->
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                                .build()
                ));
    }
}
