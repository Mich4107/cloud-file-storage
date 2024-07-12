package ru.yuubi.cloud_file_storage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yuubi.cloud_file_storage.service.AuthService;
import ru.yuubi.cloud_file_storage.service.MinioService;
import ru.yuubi.cloud_file_storage.util.ControllerUtil;

@Controller
public class DirectoryController {

    private final AuthService authService;
    private final MinioService minioService;

    public DirectoryController(AuthService authService, MinioService minioService) {
        this.authService = authService;
        this.minioService = minioService;
    }

    @PostMapping("/rename-directory")
    public String handleRenamingDirectory(@RequestParam("new_directory_name") String newDirectoryName,
                                          @RequestParam("old_directory_name") String oldDirectoryName,
                                          @RequestParam(value = "path_to_directory", required = false) String pathToDirectory,
                                          RedirectAttributes redirectAttributes) {

        if (pathToDirectory != null) {
            redirectAttributes.addAttribute("path", pathToDirectory);
        }

        if(newDirectoryName.isBlank()) {
            redirectAttributes.addAttribute("error", "empty_rename_form");
            return "redirect:/main-page";
        }

        if (newDirectoryName.length() > ControllerUtil.CHARACTER_LIMIT) {
            redirectAttributes.addAttribute("error", "character_limit");
            return "redirect:/main-page";
        }

        if(ControllerUtil.containsSpecialCharacters(newDirectoryName)) {
            redirectAttributes.addAttribute("error", "special_character");
            return "redirect:/main-page";
        }

        newDirectoryName = newDirectoryName + "/";

        if (newDirectoryName.equals(oldDirectoryName)) {
            redirectAttributes.addAttribute("path", pathToDirectory);
            return "redirect:/main-page";
        }

        if (pathToDirectory != null) {
            newDirectoryName = pathToDirectory + newDirectoryName;
            oldDirectoryName = pathToDirectory + oldDirectoryName;
        }

        Integer userId = authService.getAuthenticatedUserId();
        minioService.renameDirectory(oldDirectoryName, newDirectoryName, userId);

        return "redirect:/main-page";
    }

    @PostMapping("/delete-directory")
    public String handleDeletingDirectory(@RequestParam("directory_name") String directoryName,
                                          @RequestParam(value = "path_to_object", required = false) String pathToObject,
                                          RedirectAttributes redirectAttributes) {

        Integer userId = authService.getAuthenticatedUserId();
        minioService.removeDirectory(directoryName, userId, pathToObject);

        if (pathToObject != null) {
            redirectAttributes.addAttribute("path", pathToObject);
        }

        return "redirect:/main-page";

    }

}
