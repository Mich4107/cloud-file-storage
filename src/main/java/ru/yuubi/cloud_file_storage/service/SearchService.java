package ru.yuubi.cloud_file_storage.service;

import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yuubi.cloud_file_storage.dto.SearchDto;
import ru.yuubi.cloud_file_storage.repository.MinioRepository;

import java.util.HashSet;
import java.util.Set;

import static ru.yuubi.cloud_file_storage.util.MinioUtil.*;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final MinioRepository minioRepository;

    public Set<SearchDto> search(String query, Integer userId) {
        String userPath = getUserRootFolderPrefix(userId);
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

    private boolean isNameContainsQuery(String name, String query) {
        name = name.toLowerCase();
        query = query.toLowerCase();
        return name.matches(".*" + query + ".*");
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
}
