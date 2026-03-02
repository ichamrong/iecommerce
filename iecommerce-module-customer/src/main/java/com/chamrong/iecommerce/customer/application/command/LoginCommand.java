package com.chamrong.iecommerce.customer.application.command;

public record LoginCommand(String tenantId, String username, String password, String deviceMeta) {}
