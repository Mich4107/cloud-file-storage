package ru.yuubi.cloud_file_storage.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.yuubi.cloud_file_storage.dao.MinioRepository;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


@Service
public class CleanupService {
    private final MinioRepository minioRepository;
    private final ConcurrentMap<String, LocalDateTime> filesToDelete = new ConcurrentHashMap<>();

    public CleanupService(MinioRepository minioRepository) {
        this.minioRepository = minioRepository;
    }

    public void scheduleZipFileDeletion(String url) {
        String objectName = parseObjectNameFromUrl(url);
        filesToDelete.put(objectName, LocalDateTime.now().plusHours(1));
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void deleteFile() {
        filesToDelete.forEach((key, value) -> {
            if (LocalDateTime.now().isAfter(value)) {
                minioRepository.removeObject(key);
            }
        });
    }

    private String parseObjectNameFromUrl(String url) {
        return url.replaceAll(".*(user-\\d+-zip-files/.*\\.zip).*", "$1");
    }
}
