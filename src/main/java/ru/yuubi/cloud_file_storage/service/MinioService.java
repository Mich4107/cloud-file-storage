package ru.yuubi.cloud_file_storage.service;

import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yuubi.cloud_file_storage.dao.MinioRepository;
import ru.yuubi.cloud_file_storage.dto.SearchDto;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class MinioService {

    @Value("${base-user-files-path}")
    private String basePath;
    private final MinioRepository minioRepository;

    public MinioService(MinioRepository minioRepository) {
        this.minioRepository = minioRepository;
    }

    public Set<SearchDto> searchFiles(String query, Integer userId) {

        String userPath = String.format(basePath, userId);
        Iterable<Result<Item>> results = minioRepository.findObjectsRecursively(userPath);

        Set<SearchDto> searchDtoSet = new HashSet<>();

        for (Result<Item> result : results) {
            Item item = getItemFromResult(result);

            String objectName = item.objectName();
            objectName = removePackagesFromString(objectName, userPath);

            searchProcess(objectName, searchDtoSet, query);
        }

        return searchDtoSet;

    }

    private void searchProcess(String objectName, Set<SearchDto> searchDtoSet, String query) {

        if (isNameContainsQuery(objectName, query)) {

            String[] names = objectName.split("/");
            boolean isSingleObject = names.length == 1;

            if (isSingleObject) {
                searchDtoSet.add(new SearchDto("", objectName));
            } else {
                findingMatchesInNames(names, objectName, searchDtoSet, query);
            }
        }
    }

    private void findingMatchesInNames(String[] names, String objectName, Set<SearchDto> searchDtoSet, String query) {
        StringBuilder pathBuilder = new StringBuilder();

        for (int i = 0; i < names.length; i++) {
            boolean isLastElement = i == names.length - 1;
            String name = names[i];

            if (!isLastElement) {
                if (isNameContainsQuery(name, query)) {
                    searchDtoSet.add(new SearchDto(pathBuilder.toString(), name + "/"));
                }
                pathBuilder.append(name).append("/");
            } else {
                if (isNameContainsQuery(name, query)) {
                    boolean isLastObjectPackage = objectName.endsWith("/");
                    if (isLastObjectPackage) {
                        name = name + "/";
                    }
                    searchDtoSet.add(new SearchDto(pathBuilder.toString(), name));
                }
            }
        }
    }

    private boolean isNameContainsQuery(String name, String query) {
        name = name.toLowerCase();
        query = query.toLowerCase();
        return name.matches(".*" + query + ".*");
    }

    public String getDownloadUrl(String name, Integer userId) throws IOException {
        Map<String, String> reqParams = new HashMap<>();
        reqParams.put("response-content-disposition", "attachment");
        boolean isPackage = name.endsWith("/");

        if (isPackage) {
            return getDownloadUrlForDirectory(name, userId, reqParams);
        } else {
            return getDownloadUrlForFile(name, userId, reqParams);
        }
    }

    private String getDownloadUrlForDirectory(String name, Integer userId, Map<String, String> reqParams) throws IOException {
        String userPath = String.format(basePath, userId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zipOut = new ZipOutputStream(baos)) {
            Iterable<Result<Item>> results = minioRepository.findObjectsRecursively(userPath + name);
            createZipFile(results, zipOut, userPath + name);
        }
        byte[] zipData = baos.toByteArray();
        String zipObjectName = uploadZipFile(zipData, userId, name);

        return minioRepository.getPresignedObjectUrl(zipObjectName, reqParams);
    }

    private void createZipFile(Iterable<Result<Item>> results, ZipOutputStream zipOut, String packagesToRemove) throws IOException {
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

    private String removePackagesFromString(String objectName, String packagesToRemove) {
        return objectName.replaceAll(packagesToRemove, "");
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

    private String uploadZipFile(byte[] zipData, Integer userId, String name) throws IOException {

        try (InputStream inputStream = new ByteArrayInputStream(zipData)) {

            String zipName = createZipName(name);
            String userPath = String.format("user-%d-zip-files/", userId);
            String objectName = userPath + zipName;

            minioRepository.uploadObject(inputStream, objectName);

            return objectName;
        }
    }

    private String createZipName(String name) {
        String[] strings = name.split("/");
        int length = strings.length;
        return strings[length - 1] + ".zip";
    }

    private String getDownloadUrlForFile(String name, Integer userId, Map<String, String> reqParams) {
        String userPath = String.format(basePath, userId);
        return minioRepository.getPresignedObjectUrl(userPath + name, reqParams);
    }

    public List<String> getFormattedListOfObjectNames(Integer userId) {

        String userPath = String.format(basePath, userId);
        Iterable<Result<Item>> results = minioRepository.findObjects(userPath);

        List<String> objectNames = new ArrayList<>();

        for (Result<Item> result : results) {
            Item item = getItemFromResult(result);
            objectNames.add(item.objectName());
        }
        return removePackagesFromList(objectNames, userPath);
    }

    public List<String> getFormattedListOfObjectNamesInSubdirectory(Integer userId, String pathToSubdirectory) {
        String userPath = String.format(basePath, userId);
        Iterable<Result<Item>> results = minioRepository.findObjects(userPath + pathToSubdirectory);

        List<String> objectNames = new ArrayList<>();

        for (Result<Item> result : results) {
            Item item = getItemFromResult(result);
            objectNames.add(item.objectName());
        }

        return removePackagesFromList(objectNames, userPath + pathToSubdirectory);
    }

    private List<String> removePackagesFromList(List<String> objectNames, String packagesToRemove) {
        return objectNames.stream()
                .map(object -> object.replaceAll(packagesToRemove, ""))
                .toList();
    }


    public void uploadFiles(MultipartFile[] files, Integer userId, String pathToUpload) {
        String userPath = String.format(basePath, userId);
        for (MultipartFile file : files) {
            uploadFile(file, userPath, pathToUpload);
        }
    }

    private void uploadFile(MultipartFile file, String userPath, String pathToUpload) {
        String name = file.getOriginalFilename();

        if (pathToUpload != null) {
            name = pathToUpload + name;
        }
        minioRepository.uploadObject(file, userPath + name);
    }

    public void renameObject(String oldName, String newName, Integer userId) {
        String userPath = String.format(basePath, userId);

        String oldObjectName = userPath + oldName;
        String newObjectName = userPath + newName;

        minioRepository.copyObjectWithNewName(oldObjectName, newObjectName);
        minioRepository.removeObject(oldObjectName);
    }

    public void renameDirectory(String oldName, String newName, Integer userId) {
        String userPath = String.format(basePath, userId);
        Iterable<Result<Item>> results = minioRepository.findObjectsRecursively(userPath + oldName);

        for (Result<Item> result : results) {
            Item item = getItemFromResult(result);

            String oldObjectName = item.objectName();
            String newObjectName = oldObjectName.replace(oldName, newName);

            minioRepository.copyObjectWithNewName(oldObjectName, newObjectName);
            minioRepository.removeObject(oldObjectName);
        }
    }

    public void removeObject(String name, Integer userId, String pathToObject) {
        String userPath = String.format(basePath, userId);
        if (pathToObject != null) {
            boolean isOneObject = isOneObjectOnParticularPath(userPath + pathToObject);
            if (isOneObject) {
                minioRepository.uploadEmptyDirectory(userPath + pathToObject);
            }
        }
        minioRepository.removeObject(userPath + name);
    }

    public void removeDirectory(String directoryName, Integer userId, String pathToObject) {
        String userPath = String.format(basePath, userId);

        if (pathToObject != null) {
            boolean isOneObject = isOneObjectOnParticularPath(userPath + pathToObject);
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

    private boolean isOneObjectOnParticularPath(String path) {
        Iterable<Result<Item>> results = minioRepository.findObjects(path);
        int counter = 0;

        for (Result<Item> result : results) {
            counter++;
        }
        return counter == 1;
    }

    private Item getItemFromResult(Result<Item> result) {
        try {

            return result.get();

        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RuntimeException(e);
        }
    }
}
