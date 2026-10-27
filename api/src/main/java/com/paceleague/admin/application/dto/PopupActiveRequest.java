package com.paceleague.admin.application.dto;

import jakarta.validation.constraints.NotNull;

public record PopupActiveRequest(@NotNull Boolean active) {
}
