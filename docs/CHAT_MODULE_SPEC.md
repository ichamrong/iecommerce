# Chat Module — Enterprise Spec

## 1. Overview

The chat module provides multi-tenant conversations and messaging: customer support, internal staff chat, and optional order/booking/POS-linked threads. It follows DDD + Hexagonal structure with cursor-only pagination, tenant isolation, and ASVS-aligned security.

## 2. Package Structure

```
com.chamrong.iecommerce.chat
├── api              — ConversationController, MessageController, exception handler
├── application
│   ├── command     — Create conversation, send message, add/remove participant
│   ├── query       — ConversationQueryService, MessageQueryService (cursor list)
│   ├── usecase     — Orchestration
│   └── dto         — Request/Response, filters
├── domain
│   ├── model       — ConversationType, ConversationStatus, MessageStatus (enums)
│   ├── event       — ConversationCreatedEvent, MessageSentEvent
│   ├── ports       — ConversationRepositoryPort, MessageRepositoryPort
│   ├── policy      — AccessPolicy, ContentPolicy
│   ├── service     — (optional) ChatDomainService
│   └── exception   — ChatDomainException
└── infrastructure
    ├── config
    ├── persistence — JpaConversationRepositoryAdapter, JpaMessageRepositoryAdapter
    ├── persistence.jpa — SpringDataConversationRepository, SpringDataMessageRepository
    ├── outbox      — (optional)
    ├── realtime    — (optional) SSE/WebSocket
    └── client
```

- **Domain** repository interfaces live in `domain/ports`. Implementations in infrastructure.
- **List endpoints** use shared `CursorPageResponse` + `CursorCodec` + `FilterHasher`. Cursor from a different filter set returns 400 `INVALID_CURSOR_FILTER_MISMATCH`.

## 3. Conversation Types

| Type     | Use case                    | Linking        |
|----------|-----------------------------|----------------|
| SUPPORT  | Customer ↔ staff            | —              |
| INTERNAL | Staff ↔ staff               | —              |
| ORDER    | Linked to orderId           | order_id       |
| BOOKING  | Linked to bookingId         | booking_id     |
| POS      | Terminal/session support    | terminal/session |

(ConversationType enum is in domain.model; persistence can add type/order_id/booking_id columns in a follow-up.)

## 4. Participant & Access

- **Participant**: stored as participant_id (Long) per conversation. Unique per (conversation_id, participant_id).
- **AccessPolicy**: `canRead(conversation, actorId, isStaff)` — participant or staff can read. `canSend(conversation, actorId)` — only participants can send.
- **IDOR**: Non-participants get 403/404 when listing messages or getting a conversation. TenantGuard enforces tenant on every load.

## 5. Message Lifecycle

- **Send**: ContentPolicy validates length (max 4KB). Sender must be participant. Tenant from context.
- **Soft delete**: (Future) status=DELETED tombstone; does not break pagination.
- **Edit**: (Future) staff-only with audit.

## 6. Cursor Pagination

- **List conversations**: `GET /api/v1/chat/conversations?userId=&cursor=&limit=`. filterHash includes participantId. Order: created_at DESC, id DESC.
- **List messages**: `GET /api/v1/chat/conversations/{id}/messages?actorId=&cursor=&limit=`. filterHash includes conversationId. Order: created_at DESC, id DESC. Caller must be participant.

## 7. Security (ASVS)

- **L1**: Input validation (ContentPolicy message length, attachment count). Tenant enforcement. IDOR: participant check before returning messages/conversation.
- **L2-grade**: ContentPolicy caps (4KB text, 5 attachments). Audit for moderation (optional; integrate with audit module). Rate limiting for send (optional).

## 8. Reliability

- **Idempotency**: SendMessage can accept idempotencyKey (optional; store snapshot and return same messageId on retry).
- **Outbox**: MessageSentEvent, ConversationCreatedEvent can be published via outbox (optional).
- **Soft delete**: Message tombstone keeps row for cursor stability.

## 9. Database (Liquibase)

- **Tables**: chat_conversation, chat_conversation_participants, chat_message (in changelog-v1). changelog-v31 adds keyset indexes and participant unique.
- **Indexes**: idx_chat_conversation_cursor (tenant_id, created_at DESC, id DESC), idx_chat_message_cursor (tenant_id, conversation_id, created_at DESC, id DESC), uq_chat_conversation_participant (conversation_id, participant_id).

## 10. UAT Scenarios

1. **Customer support**: Create SUPPORT conversation with customer + staff; send messages; list messages (cursor); non-participant gets 403.
2. **Staff internal**: Create INTERNAL conversation; list my conversations (cursor) with participantId=staffId.
3. **Booking-linked**: (Future) Create BOOKING conversation with bookingId; list by booking.
4. **Cursor filter mismatch**: List conversations with participantId=1; copy nextCursor; call with participantId=2 and same cursor → 400 INVALID_CURSOR_FILTER_MISMATCH.
5. **Tenant isolation**: Tenant A cannot see or list Tenant B’s conversations (tenantId from TenantGuard).

## 11. Assumptions

- **Conversation** and **ChatMessage** remain JPA entities in the domain package for backward compatibility; a future step can move them to infrastructure/persistence/jpa as *Entity and introduce pure domain aggregates.
- **actorId** and **participantId** are passed as request params; in production they would come from the security context (authenticated user).
- **iecommerce-common** provides CursorCodec, FilterHasher, CursorPageResponse, TenantGuard, TenantContext.
- Liquibase changelogs live in **iecommerce-app**; chat module has no Liquibase dependency.
