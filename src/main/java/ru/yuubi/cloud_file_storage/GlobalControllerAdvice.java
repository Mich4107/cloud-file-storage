package ru.yuubi.cloud_file_storage;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import ru.yuubi.cloud_file_storage.exception.UserAlreadyExistsException;
import ru.yuubi.cloud_file_storage.exception.WrongDataException;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler({UserAlreadyExistsException.class, WrongDataException.class})
    public RedirectView handleRegistrationExceptions(Exception e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("error", e.getMessage());
        return new RedirectView("/sign-up", true);
    }

    @ExceptionHandler(Exception.class)
    public RedirectView handleExceptions(Exception e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("error", "unhandled_exception");
        return new RedirectView("/main-page", true);
    }
}
