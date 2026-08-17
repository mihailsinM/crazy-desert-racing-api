package com.crazydesert.racing.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ImageFocusRequest {

    @NotNull(message = "Horizontal image focus is required")
    @Min(value = 0, message = "Horizontal image focus must be at least 0")
    @Max(value = 100, message = "Horizontal image focus must be at most 100")
    public Integer focusX;

    @NotNull(message = "Vertical image focus is required")
    @Min(value = 0, message = "Vertical image focus must be at least 0")
    @Max(value = 100, message = "Vertical image focus must be at most 100")
    public Integer focusY;
}
