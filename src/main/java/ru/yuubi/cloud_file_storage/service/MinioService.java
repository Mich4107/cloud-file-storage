package ru.yuubi.cloud_file_storage.service;

import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yuubi.cloud_file_storage.repository.MinioRepository;
import ru.yuubi.cloud_file_storage.util.exception.MinioExceptionHandlerUtil;

import java.io.*;
import java.util.*;

import static ru.yuubi.cloud_file_storage.util.MinioUtil.*;


@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioRepository minioRepository;

    public List<String> getFormattedListOfObjectNames(Integer userId) {

        String userPath = getUserRootFolderPrefix(userId);
        Iterable<Result<Item>> results = minioRepository.findObjects(userPath);

        List<String> objectNames = new ArrayList<>();

        for (Result<Item> result : results) {
            Item item = getItemFromResult(result);
            objectNames.add(item.objectName());
        }
        return removePackagesFromList(objectNames, userPath);
    }

    public List<String> getFormattedListOfObjectNamesInSubdirectory(Integer userId, String pathToSubdirectory) {
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


    public void uploadFiles(MultipartFile[] files, Integer userId, String pathToUpload) {
        String userPath = getUserRootFolderPrefix(userId);
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


    public boolean isOneObjectOnParticularPath(String path) {
        Iterable<Result<Item>> results = minioRepository.findObjects(path);
        int counter = 0;

        for (Result<Item> result : results) {
            counter++;
        }
        return counter == 1;
    }

    private Item getItemFromResult(Result<Item> result) {
        return MinioExceptionHandlerUtil.handleExceptions(result::get);
    }
}
