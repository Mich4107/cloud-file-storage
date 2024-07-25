package ru.yuubi.cloud_file_storage.service;

import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yuubi.cloud_file_storage.repository.MinioRepository;
import ru.yuubi.cloud_file_storage.util.MinioUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static ru.yuubi.cloud_file_storage.util.MinioUtil.*;

@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioRepository minioRepository;

    public InputStream getObjectInputStream(String name, Integer userId) {
        return minioRepository.getObjectInputStream(getUserRootFolderPrefix(userId) + name);
    }

    public void renameObject(String oldName, String newName, Integer userId) {
        String userPath = getUserRootFolderPrefix(userId);

        String oldObjectName = userPath + oldName;
        String newObjectName = userPath + newName;

        minioRepository.copyObjectWithNewName(oldObjectName, newObjectName);
        minioRepository.removeObject(oldObjectName);
    }

    public void removeObject(String name, Integer userId, String pathToObject) {
        String userPath = getUserRootFolderPrefix(userId);

        if (isOneObjectOnPath(userPath, pathToObject, minioRepository)) {
            minioRepository.uploadEmptyDirectory(userPath + pathToObject);
        }

        minioRepository.removeObject(userPath + name);
    }

    public void uploadFiles(MultipartFile[] files, Integer userId, String pathToUpload) {
        for (MultipartFile file : files) {
            uploadFile(file, getUserRootFolderPrefix(userId), pathToUpload);
        }
    }

    private void uploadFile(MultipartFile file, String userPath, String pathToUpload) {
        String name = file.getOriginalFilename();

        if (pathToUpload != null) {
            name = pathToUpload + name;
        }
        minioRepository.uploadObject(file, userPath + name);
    }

    public List<String> getUserFiles(Integer userId) {
        String userPath = getUserRootFolderPrefix(userId);
        Iterable<Result<Item>> results = minioRepository.findObjects(userPath);

        List<String> objectNames = new ArrayList<>();

        for (Result<Item> result : results) {
            Item item = getItemFromResult(result);
            objectNames.add(item.objectName());
        }
        return removePackagesFromList(objectNames, userPath);
    }

    public List<String> getUserFilesInSubdirectory(Integer userId, String pathToSubdirectory) {
        String userPath = getUserRootFolderPrefix(userId);
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
}
