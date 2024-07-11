package ru.yuubi.cloud_file_storage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yuubi.cloud_file_storage.service.AuthService;
import ru.yuubi.cloud_file_storage.service.MinioService;
import ru.yuubi.cloud_file_storage.util.ControllerUtil;

@Controller
public class FileController {

    private final AuthService authService;
    private final MinioService minioService;

    public FileController(AuthService authService, MinioService minioService) {
        this.authService = authService;
        this.minioService = minioService;
    }

    @PostMapping("/rename-file")
    public String handleRenamingFile(@RequestParam("new_object_name") String newObjectName,
                                     @RequestParam("old_object_name") String oldObjectName,
                                     @RequestParam(value = "path_to_object", required = false) String pathToObject,
                                     RedirectAttributes redirectAttributes) {

        if(newObjectName.isBlank()) {
            redirectAttributes.addAttribute("error", "empty_rename_form");
            return "redirect:/main-page";
        }

        if (newObjectName.equals(oldObjectName)) {
            return "redirect:/main-page";
        }

        if (ControllerUtil.containsSpecialCharacters(newObjectName)) {
            redirectAttributes.addAttribute("error", "special_character");
            return "redirect:/main-page";
        }

        if (pathToObject != null) {
            newObjectName = pathToObject + newObjectName;
            oldObjectName = pathToObject + oldObjectName;

            redirectAttributes.addAttribute("path", pathToObject);
        }

        Integer userId = authService.getAuthenticatedUserId();
        minioService.renameObject(oldObjectName, newObjectName, userId);

        return "redirect:/main-page";
    }

    @PostMapping("/delete-file")
    public String handleDeletingFile(@RequestParam("object_name") String objectName,
                                     @RequestParam(value = "path_to_object", required = false) String pathToObject,
                                     RedirectAttributes redirectAttributes) {

        Integer userId = authService.getAuthenticatedUserId();
        minioService.removeObject(objectName, userId, pathToObject);

        if (pathToObject != null) {
            redirectAttributes.addAttribute("path", pathToObject);
        }

        return "redirect:/main-page";

    }
}
