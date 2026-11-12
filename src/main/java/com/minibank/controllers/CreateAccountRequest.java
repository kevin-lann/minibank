package com.minibank.controllers;

import jakarta.validation.constraints.NotBlank;

public record CreateAccountRequest(@NotBlank String ownerName) {}
