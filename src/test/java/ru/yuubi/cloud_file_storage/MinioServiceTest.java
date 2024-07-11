package ru.yuubi.cloud_file_storage;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.ContentType;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.yuubi.cloud_file_storage.dao.UserRepository;
import ru.yuubi.cloud_file_storage.service.AuthService;
import ru.yuubi.cloud_file_storage.service.MinioService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@Testcontainers
@SpringBootTest(classes = MinioTestContainerConfig.class)
public class MinioServiceTest {

    @MockBean
    private AuthService authService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private EntityManagerFactory entityManagerFactory;

    private static final String FILE_NAME = "file%d.txt";
    private static final String FILE_TEXT = "test";

    @Autowired
    private MinioService minioService;

    @Test
    public void uploadFiles_thenFilesUploadedSuccessfully() {
        Integer userId = 2;
        String pathToUpload = null;
        MockMultipartFile[] mockFiles = createMockFiles();

        minioService.uploadFiles(mockFiles, userId, pathToUpload);
        List<String> list = minioService.getFormattedListOfObjectNames(userId);

        assertThat(list).containsExactlyInAnyOrder(
                mockFiles[0].getOriginalFilename(),
                mockFiles[1].getOriginalFilename()
        );
    }

    private MockMultipartFile[] createMockFiles() {
        String file1Name = String.format(FILE_NAME, 1);
        String file2Name = String.format(FILE_NAME, 2);

        return new MockMultipartFile[]{
                new MockMultipartFile(file1Name, file1Name, ContentType.TEXT_PLAIN.toString(), FILE_TEXT.getBytes()),
                new MockMultipartFile(file2Name, file2Name, ContentType.TEXT_PLAIN.toString(), FILE_TEXT.getBytes())
        };
    }
}
