package ru.yuubi.cloud_file_storage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yuubi.cloud_file_storage.service.AuthService;
import ru.yuubi.cloud_file_storage.service.MinioService;
import ru.yuubi.cloud_file_storage.util.BreadcrumbUtil;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final MinioService minioService;
    private final AuthService authService;

    @GetMapping("/main-page")
    public String getMainPage(@RequestParam(name = "path", required = false) String pathToSubdirectory,
                              Model model) {

        Integer userId = authService.getAuthenticatedUserId();
        List<String> objectNames;

        if (pathToSubdirectory != null && !pathToSubdirectory.isBlank()) {
            Map<String, String> breadcrumb = BreadcrumbUtil.createBreadcrumb(pathToSubdirectory);
            objectNames = minioService.getFormattedListOfObjectNamesInSubdirectory(userId, pathToSubdirectory);
            model.addAttribute("breadcrumb", breadcrumb);
        } else {
            objectNames = minioService.getFormattedListOfObjectNames(userId);
        }

        model.addAttribute("objects", objectNames);

        return "main-page";
    }

    @PostMapping("/upload")
    public String handleUploading(@RequestParam("files") MultipartFile[] files,
                                  @RequestParam(value = "upload_path", required = false) String pathToUpload,
                                  RedirectAttributes redirectAttributes) {

        String fileName = files[0].getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            redirectAttributes.addAttribute("error", "empty_form");
            return "redirect:/main-page";
        }

        Integer userId = authService.getAuthenticatedUserId();
        minioService.uploadFiles(files, userId, pathToUpload);

        if (pathToUpload != null) {
            redirectAttributes.addAttribute("path", pathToUpload);
            return "redirect:/main-page";
        }

        return "redirect:/main-page";
    }
}
