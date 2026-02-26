package com.chamrong.iecommerce.chat.application.dto;

import java.util.Set;

public record StartConversationRequest(Set<Long> participantIds) {}
