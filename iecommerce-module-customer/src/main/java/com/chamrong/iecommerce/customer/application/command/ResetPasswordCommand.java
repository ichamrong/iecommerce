package com.chamrong.iecommerce.customer.application.command;

public record ResetPasswordCommand(String email, String newPassword) {}
