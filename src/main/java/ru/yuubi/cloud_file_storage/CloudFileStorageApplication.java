package ru.yuubi.cloud_file_storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.yuubi.cloud_file_storage.service.MinioService;

import java.util.function.Predicate;

@SpringBootApplication
public class CloudFileStorageApplication {
	public static void main(String[] args) {
		SpringApplication.run(CloudFileStorageApplication.class, args);
	}
}
