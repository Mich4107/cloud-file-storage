package ru.yuubi.cloud_file_storage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SearchDto {
    private String path;
    private String name;
}
