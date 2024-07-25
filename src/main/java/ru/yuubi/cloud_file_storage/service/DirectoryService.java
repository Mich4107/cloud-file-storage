package ru.yuubi.cloud_file_storage.service;

import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yuubi.cloud_file_storage.repository.MinioRepository;
import ru.yuubi.cloud_file_storage.util.MinioUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static ru.yuubi.cloud_file_storage.util.MinioUtil.*;

@Service
@RequiredArgsConstructor
public class DirectoryService {

    private final MinioRepository minioRepository;
    private final MinioService minioService;

    public InputStream createZipFile(String name, Integer userId) throws IOException {
        String userPath = getUserRootFolderPrefix(userId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zipOut = new ZipOutputStream(baos)) {
            Iterable<Result<Item>> results = minioRepository.findObjectsRecursively(userPath + name);
            String packagesToRemove = getPackagesBeforeName(name);
            fillZipFile(results, zipOut, userPath + packagesToRemove);
        }
        byte[] zipData = baos.toByteArray();
        return new ByteArrayInputStream(zipData);
    }

    private String getPackagesBeforeName(String name) {
        String[] split = name.split("/");
        StringBuilder pathBuilder = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            if (i < split.length - 1) {
                pathBuilder.append(split[i]).append("/");
            }
        }
        return pathBuilder.toString();
    }

    private void fillZipFile(Iterable<Result<Item>> results, ZipOutputStream zipOut, String packagesToRemove) throws IOException {
        for (Result<Item> result : results) {
            Item item = getItemFromResult(result);
            if (!item.isDir()) {
                try (InputStream inputStream = minioRepository.getObjectInputStream(item.objectName())) {
                    String formattedName = removePackagesFromString(item.objectName(), packagesToRemove);
                    addFileToZip(zipOut, formattedName, inputStream);
                }
            }
        }
    }

    private void addFileToZip(ZipOutputStream zipOut, String name, InputStream inputStream) throws IOException {

        // the name can be empty if the directory was empty earlier, and for the convenience of the user, we created
        // directory that contained an empty file (0 byte & empty name) so that the directory path could
        // remain in Minio, even if all files from it have already been deleted (This was done due to the specificity
        // of Minio). Therefore, we need to ignore this file when creating the zip.
        if (name.isBlank()) {
            return;
        }

        ZipEntry zipEntry = new ZipEntry(name);
        zipOut.putNextEntry(zipEntry);

        byte[] buffer = new byte[5 * 1024 * 1024];
        int length;

        while ((length = inputStream.read(buffer)) >= 0) {
            zipOut.write(buffer, 0, length);
        }
        zipOut.closeEntry();
    }

    public void renameDirectory(String oldName, String newName, Integer userId) {
        String userPath = getUserRootFolderPrefix(userId);
        Iterable<Result<Item>> results = minioRepository.findObjectsRecursively(userPath + oldName);

        for (Result<Item> result : results) {
            Item item = getItemFromResult(result);

            String oldObjectName = item.objectName();
            String newObjectName = oldObjectName.replace(oldName, newName);

            minioRepository.copyObjectWithNewName(oldObjectName, newObjectName);
            minioRepository.removeObject(oldObjectName);
        }
    }

    public void removeDirectory(String directoryName, Integer userId, String pathToObject) {
        String userPath = getUserRootFolderPrefix(userId);

        if (pathToObject != null) {
            boolean isOneObject = minioService.isOneObjectOnParticularPath(userPath + pathToObject);
            if (isOneObject) {
                minioRepository.uploadEmptyDirectory(userPath + pathToObject);
            }
        }

        Iterable<Result<Item>> results = minioRepository.findObjectsRecursively(userPath + directoryName);

        for (Result<Item> result : results) {
            Item item = getItemFromResult(result);
            minioRepository.removeObject(item.objectName());
        }
    }



}
