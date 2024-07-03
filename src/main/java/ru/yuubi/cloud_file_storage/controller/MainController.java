package ru.yuubi.cloud_file_storage.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yuubi.cloud_file_storage.service.AuthService;
import ru.yuubi.cloud_file_storage.service.MinioService;

import java.io.IOException;
import java.util.*;

@Controller
public class MainController {

    private final MinioService minioService;
    private final AuthService authService;

    public MainController(MinioService minioService, AuthService authService) {
        this.minioService = minioService;
        this.authService = authService;
    }

    @GetMapping("/main-page")
    public String getMainPage(@RequestParam(name = "action", required = false) String action,
                              @RequestParam(name = "path", required = false) String pathToSubdirectory,
                              Model model) {

        Integer userId = authService.getAuthenticatedUserId();
        List<String> listOfObjectNames;

        if (pathToSubdirectory != null) {

            Map<String, String> breadcrumb = new LinkedHashMap<>();
            listOfObjectNames = minioService.getFormattedListOfObjectNamesInSubdirectory(userId, pathToSubdirectory);
            fillBreadcrumb(breadcrumb, pathToSubdirectory);
            model.addAttribute("breadcrumb", breadcrumb);

        } else {

            listOfObjectNames = minioService.getFormattedListOfObjectNames(userId);
        }

        model.addAttribute("objects", listOfObjectNames);
        model.addAttribute("action", action);

        return "main-page";
    }

    @PostMapping("/add-files")
    public String handleUploadFile(@RequestParam("files") MultipartFile[] files,
                                   @RequestParam("action") String action,
                                   RedirectAttributes redirectAttributes) {

        String fileName = files[0].getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            redirectAttributes.addAttribute("error", "empty_form_request");
            redirectAttributes.addAttribute("action", action);
            return "redirect:/main-page";
        }

        Integer userId = authService.getAuthenticatedUserId();
        minioService.uploadFiles(files, userId);

        return "redirect:/main-page";
    }

    @PostMapping("/delete-file")
    public String handleDeletingFile(@RequestParam("object_name") String objectName) {
        Integer userId = authService.getAuthenticatedUserId();
        minioService.removeObject(objectName, userId);
        return "redirect:/main-page";
    }

    @PostMapping("/delete-directory")
    public String handleDeletingDirectory(@RequestParam("directory_name") String directoryName) {
        Integer userId = authService.getAuthenticatedUserId();
        minioService.removeDirectory(directoryName, userId);
        return "redirect:/main-page";
    }


    @PostMapping("/download")
    public void handleDownloadingFile(@RequestParam("name") String name,
                                      HttpServletResponse response) throws IOException {

        Integer userId = authService.getAuthenticatedUserId();
        String url = minioService.getDownloadUrl(name, userId);

        response.sendRedirect(url);
    }

    @PostMapping("/rename-file")
    public String handleRenamingFile(@RequestParam("new_object_name") String newObjectName,
                                     @RequestParam("old_object_name") String oldObjectName) {

        if (newObjectName.equals(oldObjectName)) {
            return "redirect:/main-page";
        }

        Integer userId = authService.getAuthenticatedUserId();
        minioService.renameObject(oldObjectName, newObjectName, userId);

        return "redirect:/main-page";
    }

    @PostMapping("/rename-directory")
    public String handleRenamingDirectory(@RequestParam("new_directory_name") String newDirectoryName,
                                          @RequestParam("old_directory_name") String oldDirectoryName) {

        newDirectoryName = newDirectoryName + "/";

        if (newDirectoryName.equals(oldDirectoryName)) {
            return "redirect:/main-page";
        }

        Integer userId = authService.getAuthenticatedUserId();
        minioService.renameDirectory(oldDirectoryName, newDirectoryName, userId);

        return "redirect:/main-page";
    }


    @PostMapping("/search")
    public String smth() {
        System.out.println("Do smth");
        return "redirect:/main-page";
    }

    private void fillBreadcrumb(Map<String, String> breadcrumb, String subdirectory) {
        String baseUrl = "/main-page";
        breadcrumb.put(baseUrl, "Main");

        String[] paths = subdirectory.split("/");

        StringBuilder pathBuilder = new StringBuilder();

        for (String path : paths) {
            pathBuilder.append(path).append("/");
            String url = String.format("%s?path=%s", baseUrl, pathBuilder);
            breadcrumb.put(url, path);
        }
    }
}
