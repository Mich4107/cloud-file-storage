package ru.yuubi.cloud_file_storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.yuubi.cloud_file_storage.dao.UserRepository;
import ru.yuubi.cloud_file_storage.exception.UserAlreadyExistsException;
import ru.yuubi.cloud_file_storage.service.AuthService;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(classes = PostgreTestContainerConfig.class)
public class AuthServiceTest {

    @Autowired
    AuthService authService;

    @Autowired
    UserRepository userRepository;

    @Test
    public void whenCreateUser_thenNewRecordAppearsInUsersTable() {
        authService.createUser("Aiden", "pass123");
        assertTrue(userRepository.findByLogin("Aiden").isPresent());
    }

    @Test
    public void whenCreatingUserWithNonUniqueUsername_thenThrowsUserAlreadyExistsException() {
        authService.createUser("Thomas", "pass123");
        assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.createUser("Thomas", "qwerty")
        );
    }

}



