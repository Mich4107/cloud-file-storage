package ru.yuubi.cloud_file_storage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 64, unique = true)
    private String login;

    @Column(nullable = false, length = 64)
    private String password;

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
