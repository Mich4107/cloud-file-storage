package ru.yuubi.cloud_file_storage.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yuubi.cloud_file_storage.exception.NoSuchObjectException;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class MinioService {

    @Value("${bucket-name}")
    private String bucketName;

    @Value("${base-user-files-path}")
    private String basePath;
    private final MinioClient minioClient;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String getDownloadUrl(String name, Integer userId) {
        try {
            Map<String, String> reqParams = new HashMap<>();
            reqParams.put("response-content-disposition", "attachment");
            boolean isPackage = name.endsWith("/");

            if (isPackage) {
                return getDownloadUrlForDirectory(name, userId, reqParams);
            }

            return getDownloadUrlForFile(name, userId, reqParams);

        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getFormattedListOfObjectNames(Integer userId) {
        try {
            String userPath = String.format(basePath, userId);
            Iterable<Result<Item>> results = getListObjects(userPath);

            List<String> objectNames = new ArrayList<>();

            for (Result<Item> result : results) {
                Item item = result.get();
                objectNames.add(item.objectName());
            }

            return removePackagesFromList(objectNames, userPath);

        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getFormattedListOfObjectNamesInSubdirectory(Integer userId, String pathToSubdirectory) {
        try {
            String userPath = String.format(basePath, userId);
            Iterable<Result<Item>> results = getListObjects(userPath + pathToSubdirectory);

            List<String> objectNames = new ArrayList<>();

            for (Result<Item> result : results) {
                Item item = result.get();
                objectNames.add(item.objectName());
            }

            return removePackagesFromList(objectNames, userPath + pathToSubdirectory);

        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RuntimeException(e);
        }
    }


    private void uploadFile(MultipartFile file, Integer userId) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String userPath = String.format(basePath, userId);
        String name = file.getOriginalFilename();
        InputStream inputStream = file.getInputStream();
        String contentType = file.getContentType();
        long size = file.getSize();

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(userPath + name)
                        .stream(inputStream, size, -1)
                        .contentType(contentType)
                        .build()
        );
    }

    public void uploadFiles(MultipartFile[] files, Integer userId) {
        try {

            for (MultipartFile file : files) {
                uploadFile(file, userId);
            }

        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RuntimeException(e);
        }
    }

    public void renameObject(String oldName, String newName, Integer userId) {
        try {
            String userPath = String.format(basePath, userId);

            String oldObjectName = userPath + oldName;
            String newObjectName = userPath + newName;

            copyObjectWithNewName(oldObjectName, newObjectName);
            removeObject(oldObjectName);

        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException | NoSuchObjectException e) {
            throw new RuntimeException(e);
        }
    }

    public void renameDirectory(String oldName, String newName, Integer userId) {
        try {
            String userPath = String.format(basePath, userId);
            Iterable<Result<Item>> results = getListObjectsRecursively(userPath + oldName);

            for (Result<Item> result : results) {
                Item item = result.get();

                String oldObjectName = item.objectName();
                String newObjectName = oldObjectName.replace(oldName, newName);

                copyObjectWithNewName(oldObjectName, newObjectName);
                removeObject(oldObjectName);
            }

        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException | NoSuchObjectException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeObject(String name, Integer userId) {
        String userPath = String.format(basePath, userId);
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(userPath + name)
                            .build()
            );
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeDirectory(String directoryName, Integer userId) {
        try {
            String userPath = String.format(basePath, userId);
            Iterable<Result<Item>> results = getListObjectsRecursively(userPath + directoryName);

            for (Result<Item> result : results) {
                Item item = result.get();
                removeObject(item.objectName());
            }

        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RuntimeException(e);
        }
    }

    private Iterable<Result<Item>> getListObjects(String path) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(path)
                        .build()
        );
    }

    private Iterable<Result<Item>> getListObjectsRecursively(String path) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(path)
                        .recursive(true)
                        .build()
        );
    }

    private String getDownloadUrlForDirectory(String name, Integer userId, Map<String, String> reqParams) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String userPath = String.format(basePath, userId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zipOut = new ZipOutputStream(baos)) {

            Iterable<Result<Item>> results = getListObjectsRecursively(userPath + name);

            for (Result<Item> result : results) {
                Item item = result.get();
                if (!item.isDir()) {
                    try (InputStream inputStream = getObjectInputStream(item.objectName())) {
                        String formattedName = removePackagesFromString(item.objectName(), userPath + name);
                        addFileToZip(zipOut, formattedName, inputStream);
                    }
                }
            }
        }
        byte[] zipData = baos.toByteArray();
        String zipObjectName = uploadZipFile(zipData, userId, name);

        return getPresignedObjectUrl(zipObjectName, reqParams);
    }

    private String getDownloadUrlForFile(String name, Integer userId, Map<String, String> reqParams) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String userPath = String.format(basePath, userId);
        return getPresignedObjectUrl(userPath + name, reqParams);
    }

    private String getPresignedObjectUrl(String objectName, Map<String, String> reqParams) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(1, TimeUnit.DAYS)
                        .extraQueryParams(reqParams)
                        .build()
        );
    }

    private InputStream getObjectInputStream(String objectName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    private String uploadZipFile(byte[] zipData, Integer userId, String name) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        try (InputStream inputStream = new ByteArrayInputStream(zipData)) {

            String zipName = createZipName(name);
            String userPath = String.format("user-%d-zip-files/", userId);
            String objectName = userPath + zipName;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, -1, 5 * 1024 * 1024)
                            .build()
            );
            return objectName;
        }
    }

    private String createZipName(String name) {
        String[] strings = name.split("/");
        int length = strings.length;
        return strings[length - 1] + ".zip";
    }

    private List<String> removePackagesFromList(List<String> objectNames, String packagesToRemove) {
        return objectNames.stream()
                .map(object -> object.replaceAll(packagesToRemove, ""))
                .toList();
    }

    private String removePackagesFromString(String objectName, String packagesToRemove) {
        return objectName.replaceAll(packagesToRemove, "");
    }

    private void addFileToZip(ZipOutputStream zipOut, String name, InputStream inputStream) throws IOException {
        ZipEntry zipEntry = new ZipEntry(name);
        zipOut.putNextEntry(zipEntry);

        byte[] buffer = new byte[5 * 1024 * 1024];
        int length;

        while ((length = inputStream.read(buffer)) >= 0) {
            zipOut.write(buffer, 0, length);
        }
        zipOut.closeEntry();
    }

    private void removeObject(String objectName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    private void copyObjectWithNewName(String oldObjectName, String newObjectName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
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
        );
    }


}
