package ru.yuubi.cloud_file_storage.integration_test.config;

import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class MinioTestContainerConfig {

    private static final String MINIO_IMAGE = "minio/minio:latest";
    private static final String MINIO_ACCESS_KEY = "yuubi123";
    private static final String MINIO_SECRET_KEY = "yuubi123";
    private static final int CONTAINER_PORT = 9000;
    private static final String BUCKET_NAME = "user-files";


    @Bean(initMethod = "start", destroyMethod = "stop")
    public GenericContainer<?> minioContainer() {
        return new GenericContainer<>(DockerImageName.parse(MINIO_IMAGE))
                .withEnv("MINIO_ROOT_USER", MINIO_ACCESS_KEY)
                .withEnv("MINIO_ROOT_PASSWORD", MINIO_SECRET_KEY)
                .withExposedPorts(CONTAINER_PORT)
                .withCommand("server /data");
    }

    @Bean
    public MinioClient minioClient(GenericContainer<?> minioContainer) throws Exception {
        String minioUrl = String.format("http://%S:%d",
                minioContainer.getHost(),
                minioContainer.getMappedPort(CONTAINER_PORT)); //automatically finds open port for exchange (example: 58920)

        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(MINIO_ACCESS_KEY, MINIO_SECRET_KEY)
                .build();

        minioClient.makeBucket(
                MakeBucketArgs.builder()
                        .bucket(BUCKET_NAME)
                        .build());

        return minioClient;
    }

}
