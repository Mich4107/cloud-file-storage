package ru.yuubi.cloud_file_storage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {

    @NotBlank(message = "Login cannot be blank")
    @Size(max = 64, message = "Login cannot be larger than 64 characters")
    private String login;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 3, message = "Password must contain at least 3 characters")
    private String password;
}
