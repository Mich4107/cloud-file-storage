package ru.yuubi.cloud_file_storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yuubi.cloud_file_storage.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByLogin(String login);
}
