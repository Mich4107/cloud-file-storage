package ru.yuubi.cloud_file_storage.integration_test;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.ContentType;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.yuubi.cloud_file_storage.integration_test.config.MinioTestContainerConfig;
import ru.yuubi.cloud_file_storage.repository.UserRepository;
import ru.yuubi.cloud_file_storage.service.AuthService;
import ru.yuubi.cloud_file_storage.service.DirectoryService;
import ru.yuubi.cloud_file_storage.service.MinioService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest(classes = MinioTestContainerConfig.class)
public class MinioServiceTest {

    @MockBean
    private AuthService authService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private EntityManagerFactory entityManagerFactory;

    private static final String FILE = "file.txt";
    private static final String DIRECTORY = "pathTo/";
    private static final String FILE_TEXT = "test";
    private static Integer userId = 0;

    @Autowired
    private MinioService minioService;

    @Autowired
    private DirectoryService directoryService;

    @BeforeEach
    public void incrementUserId() {
        userId++;
    }

//    @Test
//    public void uploadFiles_thenFilesUploadedSuccessfully() {
//        MockMultipartFile[] mockFiles = createMockFile(FILE);
//
//        minioService.uploadFiles(mockFiles, userId, null);
//        List<String> list = minioService.getFormattedListOfObjectNames(userId);
//
//        assertThat(list).contains(mockFiles[0].getOriginalFilename());
//    }
//
//    @Test
//    public void renameObject_thenObjectRenamedSuccessfully() {
//        MockMultipartFile[] mockFiles = createMockFile(FILE);
//        String oldName = mockFiles[0].getOriginalFilename();
//        String newName = "test"+oldName;
//
//        minioService.uploadFiles(mockFiles, userId, null);
//        minioService.renameObject(oldName, newName, userId);
//
//        List<String> list = minioService.getFormattedListOfObjectNames(userId);
//
//        assertThat(list).contains(newName);
//    }
//
//    @Test
//    public void renameDirectory_thenDirectoryRenamedSuccessfully() {
//        MockMultipartFile[] mockFiles = createMockFile(DIRECTORY+FILE);
//        String newName = "test"+DIRECTORY;
//
//        minioService.uploadFiles(mockFiles, userId, null);
//        directoryService.renameDirectory(DIRECTORY, newName, userId);
//
//        List<String> list = minioService.getFormattedListOfObjectNames(userId);
//
//        assertThat(list).contains(newName);
//    }
//
//    @Test
//    public void removeObject_thenObjectRemovedSuccessfully() {
//        MockMultipartFile[] mockFiles = createMockFile(FILE);
//        String objectName = mockFiles[0].getOriginalFilename();
//
//        minioService.uploadFiles(mockFiles, userId, null);
//        minioService.removeObject(objectName, userId, null);
//
//        List<String> list = minioService.getFormattedListOfObjectNames(userId);
//
//        assertThat(list).isEmpty();
//    }
//
//    @Test
//    public void removeDirectory_thenAllFilesInDirectoryRemovedSuccessfully() {
//        MockMultipartFile[] mockFiles = createMockFile(DIRECTORY+FILE);
//
//        minioService.uploadFiles(mockFiles, userId, null);
//        minioService.removeDirectory(DIRECTORY, userId, null);
//
//        List<String> list = minioService.getFormattedListOfObjectNames(userId);
//
//        assertThat(list).isEmpty();
//    }
//
//    @Test
//    public void removeFile_inDirectoryWithOneFile_thenDirectoryStillAvailable() {
//        MockMultipartFile[] mockFiles = createMockFile(DIRECTORY+FILE);
//
//        minioService.uploadFiles(mockFiles, userId, null);
//        minioService.removeObject(FILE, userId, DIRECTORY);
//
//        List<String> list = minioService.getFormattedListOfObjectNames(userId);
//
//        assertThat(list).contains(DIRECTORY);
//    }
//
//    @Test
//    public void getFormattedListInSubdirectory_thenListGeneratedSuccessfully() {
//        MockMultipartFile[] mockFiles = createMockFile(DIRECTORY+FILE);
//
//        minioService.uploadFiles(mockFiles, userId, null);
//        List<String> list = minioService.getFormattedListOfObjectNamesInSubdirectory(userId, DIRECTORY);
//
//        assertThat(list).contains(FILE);
//    }
//
//    private MockMultipartFile[] createMockFile(String name) {
//        return new MockMultipartFile[]{
//                new MockMultipartFile(name, name, ContentType.TEXT_PLAIN.toString(), FILE_TEXT.getBytes()),
//        };
//    }
}
