package ru.yuubi.cloud_file_storage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users", indexes = @Index(name = "login_index", columnList = "login", unique = true))
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "login", nullable = false, length = 64)
    private String login;

    @Column(name = "password", nullable = false, length = 64)
    private String password;

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
