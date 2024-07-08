package ru.yuubi.cloud_file_storage.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yuubi.cloud_file_storage.dao.MinioRepository;
import ru.yuubi.cloud_file_storage.service.AuthService;
import ru.yuubi.cloud_file_storage.service.CleanupService;
import ru.yuubi.cloud_file_storage.service.MinioService;

import java.io.IOException;
import java.util.*;

@Controller
public class MainController {

    private final MinioService minioService;
    private final AuthService authService;
    private final CleanupService cleanupService;

    public MainController(MinioService minioService, AuthService authService, CleanupService cleanupService) {
        this.minioService = minioService;
        this.authService = authService;
        this.cleanupService = cleanupService;
    }

    @GetMapping("/main-page")
    public String getMainPage(@RequestParam(name = "path", required = false) String pathToSubdirectory,
                              Model model) {

        Integer userId = authService.getAuthenticatedUserId();
        List<String> objectNames;

        if (pathToSubdirectory != null && !pathToSubdirectory.isBlank()) {

            Map<String, String> breadcrumb = new LinkedHashMap<>();
            objectNames = minioService.getFormattedListOfObjectNamesInSubdirectory(userId, pathToSubdirectory);
            fillBreadcrumb(breadcrumb, pathToSubdirectory);
            model.addAttribute("breadcrumb", breadcrumb);

        } else {

            objectNames = minioService.getFormattedListOfObjectNames(userId);
        }

        model.addAttribute("objects", objectNames);

        return "main-page";
    }

    @GetMapping("/search")
    public String handleSearch(@RequestParam("query") String searchQuery,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        if(searchQuery.isBlank()) {
            redirectAttributes.addAttribute("error", "empty_query");
            return "redirect:/main-page";
        }

        Integer userId = authService.getAuthenticatedUserId();
        Map<String, String> objectPathMap = minioService.searchFiles(searchQuery, userId);

        model.addAttribute("objectPathMap", objectPathMap);

        return "search-page";
    }

    @PostMapping("/add-files")
    public String handleUploadFile(@RequestParam("files") MultipartFile[] files,
                                   @RequestParam(value = "upload_path", required = false) String pathToUpload,
                                   RedirectAttributes redirectAttributes) {

        String fileName = files[0].getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            redirectAttributes.addAttribute("error", "empty_form");
            return "redirect:/main-page";
        }

        Integer userId = authService.getAuthenticatedUserId();
        minioService.uploadFiles(files, userId, pathToUpload);

        if(pathToUpload != null) {
            redirectAttributes.addAttribute("path", pathToUpload);
            return "redirect:/main-page";
        }

        return "redirect:/main-page";
    }

    @PostMapping("/delete-file")
    public String handleDeletingFile(@RequestParam("object_name") String objectName,
                                     @RequestParam(value = "path_to_object", required = false) String pathToObject,
                                     RedirectAttributes redirectAttributes) {

        Integer userId = authService.getAuthenticatedUserId();
        minioService.removeObject(objectName, userId, pathToObject);

        if(pathToObject != null) {
            redirectAttributes.addAttribute("path", pathToObject);
        }

        return "redirect:/main-page";

    }

    @PostMapping("/delete-directory")
    public String handleDeletingDirectory(@RequestParam("directory_name") String directoryName,
                                          @RequestParam(value = "path_to_object", required = false) String pathToObject,
                                          RedirectAttributes redirectAttributes) {

        Integer userId = authService.getAuthenticatedUserId();
        minioService.removeDirectory(directoryName, userId, pathToObject);

        if(pathToObject != null) {
            redirectAttributes.addAttribute("path", pathToObject);
        }

        return "redirect:/main-page";

    }


    @PostMapping("/download")
    public void handleDownloading(@RequestParam("name") String name,
                                  HttpServletResponse response) throws IOException {

        Integer userId = authService.getAuthenticatedUserId();
        String url = minioService.getDownloadUrl(name, userId);
        boolean isPackage = name.endsWith("/");

        if(isPackage) {
            cleanupService.scheduleZipFileDeletion(url);
        }

        response.sendRedirect(url);
    }

    @PostMapping("/rename-file")
    public String handleRenamingFile(@RequestParam("new_object_name") String newObjectName,
                                     @RequestParam("old_object_name") String oldObjectName,
                                     @RequestParam(value = "path_to_object", required = false) String pathToObject,
                                     RedirectAttributes redirectAttributes) {

        if (newObjectName.equals(oldObjectName)) {
            return "redirect:/main-page";
        }

        if(pathToObject != null) {
            newObjectName = pathToObject + newObjectName;
            oldObjectName = pathToObject + oldObjectName;

            redirectAttributes.addAttribute("path", pathToObject);
        }

        Integer userId = authService.getAuthenticatedUserId();
        minioService.renameObject(oldObjectName, newObjectName, userId);

        return "redirect:/main-page";
    }

    @PostMapping("/rename-directory")
    public String handleRenamingDirectory(@RequestParam("new_directory_name") String newDirectoryName,
                                          @RequestParam("old_directory_name") String oldDirectoryName,
                                          @RequestParam(value = "path_to_directory", required = false) String pathToDirectory,
                                          RedirectAttributes redirectAttributes) {

        newDirectoryName = newDirectoryName + "/";

        if (newDirectoryName.equals(oldDirectoryName)) {
            redirectAttributes.addAttribute("path", pathToDirectory);
            return "redirect:/main-page";
        }

        if(pathToDirectory != null) {
            newDirectoryName = pathToDirectory + newDirectoryName;
            oldDirectoryName = pathToDirectory + oldDirectoryName;
        }

        Integer userId = authService.getAuthenticatedUserId();
        minioService.renameDirectory(oldDirectoryName, newDirectoryName, userId);

        if(pathToDirectory != null) {
            redirectAttributes.addAttribute("path", pathToDirectory);
            return "redirect:/main-page";
        }

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
