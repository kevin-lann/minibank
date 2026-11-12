package com.minibank.controllers;

import java.math.BigDecimal;

public record WithdrawalRequest(BigDecimal amount) {}
