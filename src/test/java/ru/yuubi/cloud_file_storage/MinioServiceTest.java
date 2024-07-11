package ru.yuubi.cloud_file_storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.yuubi.cloud_file_storage.dao.MinioRepository;
import ru.yuubi.cloud_file_storage.service.MinioService;

import java.util.List;

@Testcontainers
@SpringBootTest(classes = MinioTestContainerConfig.class)
public class MinioServiceTest {
    private static final String BUCKET_NAME = "user-files";

    @Autowired
    private MinioService minioService;

//    @BeforeAll
//    public static void setUp() throws Exception {
//        minioContainer = new GenericContainer<>(DockerImageName.parse(MINIO_IMAGE))
//                .withEnv("MINIO_ROOT_USER", MINIO_ACCESS_KEY)
//                .withEnv("MINIO_ROOT_PASSWORD", MINIO_SECRET_KEY)
//                .withExposedPorts(CONTAINER_PORT)
//                .withCommand("server /data");
//
//        minioContainer.start();
//
//        String minioUrl = String.format("http://%S:%d",
//                minioContainer.getHost(),
//                minioContainer.getMappedPort(CONTAINER_PORT)); //automatically finds open port for exchange (example: 58920)
//
//        minioClient = MinioClient.builder()
//                .endpoint(minioUrl)
//                .credentials(MINIO_ACCESS_KEY, MINIO_SECRET_KEY)
//                .build();
//
//        minioClient.makeBucket(
//                MakeBucketArgs.builder()
//                        .bucket(BUCKET_NAME)
//                        .build());
//
//        MinioRepository minioRepository = new MinioRepository(minioClient);
//        minioService = new MinioService(minioRepository);
//    }

//    @AfterAll
//    public static void close() {
//        if (minioContainer.isCreated()) {
//            minioContainer.stop();
//        }
//    }

//    @Test
//    public void minioClient_bucketCreatedSuccessfully() throws Exception {
//        boolean found = minioClient.bucketExists(
//                BucketExistsArgs.builder()
//                        .bucket(BUCKET_NAME)
//                        .build());
//
//        Assertions.assertTrue(found);
//    }

    @Test
    public void uploadFiles_thenFilesUploadedSuccessfully() {
        Integer userId = 1;
        String pathToUpload = null;
        MockMultipartFile[] mockFiles = new MockMultipartFile[2];
        mockFiles[0] = new MockMultipartFile("file1.txt", "text1".getBytes());
        mockFiles[1] = new MockMultipartFile("file2.txt", "text2".getBytes());

        minioService.uploadFiles(mockFiles, userId, pathToUpload);
        List<String> list = minioService.getFormattedListOfObjectNames(userId);

        int counter = 0;

        for (String s : list) {
            if (s.equals(mockFiles[0].getName()) || s.equals(mockFiles[1].getName())) {
                counter++;
            }
        }

        Assertions.assertEquals(counter, 2);
    }
}
