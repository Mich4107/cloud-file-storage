package ru.yuubi.cloud_file_storage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yuubi.cloud_file_storage.dto.SearchDto;
import ru.yuubi.cloud_file_storage.service.AuthService;
import ru.yuubi.cloud_file_storage.service.MinioService;

import java.util.Set;

@Controller
public class SearchController {

    private final AuthService authService;
    private final MinioService minioService;

    public SearchController(AuthService authService, MinioService minioService) {
        this.authService = authService;
        this.minioService = minioService;
    }

    @GetMapping("/search")
    public String handleSearch(@RequestParam("query") String searchQuery,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        if (searchQuery.isBlank()) {
            redirectAttributes.addAttribute("error", "empty_query");
            return "redirect:/main-page";
        }

        Integer userId = authService.getAuthenticatedUserId();
        Set<SearchDto> searchDtoSet = minioService.searchFiles(searchQuery, userId);

        model.addAttribute("searchDtoSet", searchDtoSet);

        return "search-page";
    }

}
