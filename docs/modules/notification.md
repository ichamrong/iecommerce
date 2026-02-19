# Module Specification: Notification

## 1. Purpose
The Notification module manages the delivery of system alerts, customer confirmations, and marketing messages across multiple channels. It ensures reliable delivery via background queuing.

## 2. Core Concepts
- **NotificationTemplate**: Reusable message formats managed per tenant.
  - **Email Template**: Supports **HTML/Handlebars** for rich layouts (Header, Footer, CSS).
  - **Chat Template**: Supports **Markdown/Text** optimized for Telegram, WhatsApp, and SMS.
  - **Push Template**: Support for titles, icons, and "Deep Links" into the app.
- **NotificationChannel**: The delivery method:
  - **Email**: Transactional engines (SMTP, SendGrid, SES).
  - **Telegram**: Fast alerts with markdown support.
  - **WhatsApp**: Customer engagement via Template Messages (WABA API).
  - **SMS**: Supports **Pluggable Providers** (Twilio, Vonage, Infobip, or Local Gateways).
  - **Push**: Mobile background alerts.
- **DeliveryLog**: A record of every message sent, including its status (SENT, FAILED, RETRYING).

## 3. Provider Strategy: Pluggable & Configurable
Instead of being locked into one vendor, the module uses a **Strategy Pattern**:
1. **Interface**: A generic `SmsProvider` interface is defined in the code.
2. **Implementations**: Specific classes for Twilio, Vonage, etc.
3. **Selection**: The system selects the provider at runtime based on the **Tenant Settings**.
4. **Fallback**: If a primary provider fails, the system can automatically switch to a secondary "Backup" provider.

## 4. Delivery Strategy: Queuing & Reliability
To prevent slowing down the user experience, all notifications are processed asynchronously:
1. **Trigger**: An event (e.g., `OrderPaid`) occurs.
2. **Queueing**: The message is pushed to a **Message Queue** (RabbitMQ or Redis via Spring Boot `@Async`).
3. **Consumption**: A background worker picks up the message, retrieves credentials from the **Setting** module, and sends it via the provider.
4. **Retry Logic**: If a provider (like Telegram) is down, the message is rescheduled for retry with exponential backoff.

## 4. Multi-Tenancy Strategy
- Each tenant uses their own SMTP/Telegram credentials configured in the `setting` module.
- If no credentials exist, the system falls back to a **Global Tenant** provider (if configured).

## 5. Public APIs (Internal Modulith)
- `NotificationService.send(templateCode, data, recipient)`: Generic send method.
- `NotificationService.broadcast(channel, message)`: For bulk maintenance alerts.
