package ru.yuubi.cloud_file_storage.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.yuubi.cloud_file_storage.dto.UserDto;
import ru.yuubi.cloud_file_storage.exception.WrongDataException;
import ru.yuubi.cloud_file_storage.service.AuthService;
import ru.yuubi.cloud_file_storage.service.security.SecurityService;

@Controller
@RequestMapping("/sign-up")
@RequiredArgsConstructor
public class RegistrationController {

    private final AuthService authService;
    private final SecurityService securityService;

    @GetMapping
    public String getSignUpPage(Model model) {
        model.addAttribute("user", new UserDto());
        return "sign-up";
    }

    @PostMapping
    public String registerUser(@Valid @ModelAttribute("user") UserDto userDto,
                               BindingResult bindingResult,
                               @RequestParam("password_check") String passwordCheck,
                               HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            return "sign-up";
        }
        String login = userDto.getLogin();
        String password = userDto.getPassword();

        if (!password.equals(passwordCheck)) {
            throw new WrongDataException("Passwords don't match");
        }
        authService.createUser(login, password);

        securityService.authenticateUser(login, password);
        securityService.setSecurityContext(request);

        return "redirect:/main-page";
    }
}
