package ru.yuubi.cloud_file_storage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yuubi.cloud_file_storage.service.AuthService;
import ru.yuubi.cloud_file_storage.service.MinioService;
import ru.yuubi.cloud_file_storage.util.FormatUtil;
import ru.yuubi.cloud_file_storage.util.ValidationUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class DirectoryController {

    private final AuthService authService;
    private final MinioService minioService;

    @PostMapping("/download-directory")
    public ResponseEntity<InputStreamResource> handleDownloadingDirectory(@RequestParam("name") String name) throws IOException {
        Integer userId = authService.getAuthenticatedUserId();
        try(InputStream inputStream = minioService.createZipFile(name, userId)) {
            InputStreamResource resource = new InputStreamResource(inputStream);

            String zipName = FormatUtil.formatNameToZip(name);
            String encodedZipName = URLEncoder.encode(zipName, StandardCharsets.UTF_8).replace("+", "%20");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", encodedZipName);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        }
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

        if (newDirectoryName.length() > ValidationUtil.CHARACTER_LIMIT) {
            redirectAttributes.addAttribute("error", "character_limit");
            return "redirect:/main-page";
        }

        if(ValidationUtil.containsSpecialCharacters(newDirectoryName)) {
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
