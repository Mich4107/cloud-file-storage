package ru.yuubi.cloud_file_storage.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yuubi.cloud_file_storage.dto.UserDto;
import ru.yuubi.cloud_file_storage.exception.UserAlreadyExistsException;
import ru.yuubi.cloud_file_storage.exception.WrongDataException;
import ru.yuubi.cloud_file_storage.service.AuthService;

@Controller
public class AuthController {
    private final AuthService authService;

    @GetMapping("/sign-in")
    public String getSignInPage() {
        return "sign-in";
    }

    @GetMapping("/sign-up")
    public String getSignUpPage(Model model) {
        model.addAttribute("user", new UserDto());
        return "sign-up";
    }

    @PostMapping("/sign-up")
    public String registerUser(@Valid @ModelAttribute("user") UserDto userDto,
                               BindingResult bindingResult,
                               @RequestParam("password_check") String passwordCheck,
                               Model model,
                               HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            return "sign-up";
        }
        String login = userDto.getLogin();
        String password = userDto.getPassword();

        try {

            if (!password.equals(passwordCheck)) {
                throw new WrongDataException("Passwords don't match");
            }
            authService.createUser(login, password);
            authService.authenticateUser(login, password);
            setSecurityContext(request);

            return "redirect:/main-page";

        } catch (UserAlreadyExistsException | WrongDataException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "sign-up";
        }
    }

    private void setSecurityContext(HttpServletRequest request) {

        // HttpSession stores the SecurityContext attribute, which stores the Authentication header, which contains user authentication,
        // and we need an explicit process of setting this attribute after manual authentication

        HttpSession session = request.getSession(true);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
    }

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

}
