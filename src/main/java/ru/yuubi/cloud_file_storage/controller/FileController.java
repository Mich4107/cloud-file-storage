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

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class FileController {

    private final AuthService authService;
    private final MinioService minioService;

    @PostMapping("/download-file")
    public ResponseEntity<InputStreamResource> handleDownloading(@RequestParam("name") String name) {
        Integer userId = authService.getAuthenticatedUserId();
        InputStream inputStream = minioService.getObjectInputStream(name, userId);
        InputStreamResource resource = new InputStreamResource(inputStream);

        name = FormatUtil.clearPackagesFromName(name);
        String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", encodedName);

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @PostMapping("/rename-file")
    public String handleRenamingFile(@RequestParam("new_object_name") String newObjectName,
                                     @RequestParam("old_object_name") String oldObjectName,
                                     @RequestParam(value = "path_to_object", required = false) String pathToObject,
                                     RedirectAttributes redirectAttributes) {

        if (pathToObject != null) {
            redirectAttributes.addAttribute("path", pathToObject);
        }

        if(newObjectName.isBlank()) {
            redirectAttributes.addAttribute("error", "empty_rename_form");
            return "redirect:/main-page";
        }

        if (newObjectName.equals(oldObjectName)) {
            return "redirect:/main-page";
        }

        if (newObjectName.length() > ValidationUtil.CHARACTER_LIMIT) {
            redirectAttributes.addAttribute("error", "character_limit");
            return "redirect:/main-page";
        }

        if (ValidationUtil.containsSpecialCharacters(newObjectName)) {
            redirectAttributes.addAttribute("error", "special_character");
            return "redirect:/main-page";
        }

        if (pathToObject != null) {
            newObjectName = pathToObject + newObjectName;
            oldObjectName = pathToObject + oldObjectName;
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
