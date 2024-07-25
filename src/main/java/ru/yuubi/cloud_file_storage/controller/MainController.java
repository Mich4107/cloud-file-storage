package ru.yuubi.cloud_file_storage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yuubi.cloud_file_storage.service.AuthService;
import ru.yuubi.cloud_file_storage.service.FileService;
import ru.yuubi.cloud_file_storage.util.BreadcrumbUtil;

import java.util.*;

@Controller
@RequestMapping("/main-page")
@RequiredArgsConstructor
public class MainController {

    private final AuthService authService;
    private final FileService fileService;

    @GetMapping
    public String getMainPage(@RequestParam(name = "path", required = false) String pathToSubdirectory,
                              Model model) {

        Integer userId = authService.getAuthenticatedUserId();
        List<String> objectNames;

        if (pathToSubdirectory != null && !pathToSubdirectory.isBlank()) {
            Map<String, String> breadcrumb = BreadcrumbUtil.createBreadcrumb(pathToSubdirectory);
            objectNames = fileService.getUserFilesInSubdirectory(userId, pathToSubdirectory);
            model.addAttribute("breadcrumb", breadcrumb);
        } else {
            objectNames = fileService.getUserFiles(userId);
        }

        model.addAttribute("objects", objectNames);

        return "main-page";
    }
}
