package com.chamrong.iecommerce.customer.application.command;

public record ResetPasswordCommand(String tenantId, String email, String newPassword) {}
