package com.financebuddy.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Category type is required")
    @Pattern(regexp = "^(INCOME|EXPENSE)$", message = "Category type must be INCOME or EXPENSE")
    private String type;

    @Size(max = 20, message = "Color must not exceed 20 characters")
    private String color;

    @Size(max = 100, message = "Icon must not exceed 100 characters")
    private String icon;
}
