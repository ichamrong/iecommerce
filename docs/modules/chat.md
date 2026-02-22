# Module Specification: Chat

## 1. Purpose
The Chat module manages real-time communication between customers and merchants/support staff. It facilitates customer inquiries, order support, and general messaging within the platform.

## 2. Core Domain Models
- **ChatRoom** / **Conversation**: A communication channel between participants (e.g., Customer and Merchant).
- **Message**: An individual payload of text, image, or document sent within a conversation.
- **Participant**: User entity (Customer or Staff) linked to a conversation track.

## 3. Key Business Logic
- **Real-Time Delivery**: Messages are pushed to connected clients via WebSockets (or SSE).
- **Read Receipts**: Tracks when participants have viewed specific messages.
- **Auto-Routing & Context**: Inquiries started from a product page or order history automatically link to the respective `ProductId` or `OrderId` for context.

## 4. Multi-Tenancy Strategy (SaaS)
- Conversations and messages are strictly isolated per `tenant_id`.
- Support agents (Staff) only see and interact with chat sessions belonging to their specific merchant (tenant).

## 5. Public APIs (Internal Modulith)
- `ChatService.sendMessage(...)`: Appends and broadcasts a new message.
- `ChatService.getConversations(...)`: Retrieves active chat threads for a tenant or user.
