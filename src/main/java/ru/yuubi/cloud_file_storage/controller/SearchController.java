package ru.yuubi.cloud_file_storage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yuubi.cloud_file_storage.dto.SearchDto;
import ru.yuubi.cloud_file_storage.service.AuthService;
import ru.yuubi.cloud_file_storage.service.SearchService;

import java.util.Set;

@Controller
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final AuthService authService;
    private final SearchService searchService;

    @GetMapping
    public String handleSearch(@RequestParam("query") String searchQuery,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        if (searchQuery.isBlank()) {
            redirectAttributes.addAttribute("error", "empty_query");
            return "redirect:/main-page";
        }

        Integer userId = authService.getAuthenticatedUserId();
        Set<SearchDto> searchDtoSet = searchService.search(searchQuery, userId);

        model.addAttribute("searchDtoSet", searchDtoSet);

        return "search-page";
    }

}
