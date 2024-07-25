package ru.yuubi.cloud_file_storage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yuubi.cloud_file_storage.repository.MinioRepository;
import ru.yuubi.cloud_file_storage.util.MinioUtil;

import java.io.InputStream;

import static ru.yuubi.cloud_file_storage.util.MinioUtil.getUserRootFolderPrefix;

@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioRepository minioRepository;
    private final MinioService minioService;

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
        if (pathToObject != null) {
            boolean isOneObject = minioService.isOneObjectOnParticularPath(userPath + pathToObject);
            if (isOneObject) {
                minioRepository.uploadEmptyDirectory(userPath + pathToObject);
            }
        }
        minioRepository.removeObject(userPath + name);
    }
}
