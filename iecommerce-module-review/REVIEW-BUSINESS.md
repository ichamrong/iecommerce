## Review Module – Business Workflows & Domain Map

### 1. Business Capabilities Covered

- **Product reviews**: Guests/customers can submit reviews for completed bookings of products.
- **Granular ratings**: Overall rating plus cleanliness, accuracy, communication, location, check‑in, and value.
- **Moderation**: Moderators approve/reject reviews; owners can flag reviews and reply publicly.
- **Visibility control**: Only approved reviews are shown on storefront product pages.

### 2. Public APIs and Their Workflows

All endpoints are defined in `ReviewController` and live under `/api/v1/reviews`.

- **Get approved reviews for a product**
  - **Endpoint**: `GET /api/v1/reviews/products/{productId}`
  - **Flow**:
    - Controller calls `ReviewService.getApprovedReviews(productId)`.
    - Service queries `ReviewRepository.findByProductIdAndStatus(productId, APPROVED)`.
    - Results are mapped to `ReviewResponse` DTOs and returned to the client.
  - **Business intent**: Show only moderated, approved reviews on the product detail page.

- **Submit a review**
  - **Endpoint**: `POST /api/v1/reviews`
  - **Auth**: `isAuthenticated()`
  - **Request**: `ReviewRequest` – includes product, customer, booking IDs, anonymity flag, ratings, comment, and optional media keys.
  - **Flow**:
    - `ReviewService.submit(req)` validates rating is between 1 and 5.
    - A new `Review` entity is created with:
      - `productId`, `customerId`, `bookingId` (one review per booking is intended).
      - `isAnonymous`, overall and granular ratings, comment, `mediaKeys`.
      - `status = PENDING`.
    - `ReviewRepository.save(review)` persists the entity.
    - Saved entity is mapped to `ReviewResponse`.
  - **Business intent**: Capture structured and textual feedback right after a booking, initially hidden pending moderation.

- **Approve a review**
  - **Endpoint**: `POST /api/v1/reviews/{id}/approve`
  - **Auth**: `hasAuthority('reviews:moderate')`
  - **Flow**:
    - `ReviewService.approve(id)` loads the review or fails with `EntityNotFoundException`.
    - Calls domain method `review.approve()` which sets status to `APPROVED`.
    - Saves via `ReviewRepository` and returns mapped `ReviewResponse`.
  - **Business intent**: Moderator confirms the review meets guidelines; it becomes visible on storefront.

- **Reject a review**
  - **Endpoint**: `POST /api/v1/reviews/{id}/reject`
  - **Auth**: `hasAuthority('reviews:moderate')`
  - **Flow**:
    - `ReviewService.reject(id)` loads the review and calls `review.reject()` (status `REJECTED`).
    - Saves and returns `ReviewResponse`.
  - **Business intent**: Permanently hide content that violates policies (spam, abuse, irrelevant content).

- **Get pending reviews**
  - **Endpoint**: `GET /api/v1/reviews/pending`
  - **Auth**: `hasAuthority('reviews:moderate')`
  - **Flow**:
    - `ReviewService.getPendingReviews()` queries `ReviewRepository.findByStatus(PENDING)`.
    - Results are mapped to `ReviewResponse`.
  - **Business intent**: Provide a moderation queue of all reviews awaiting decision.

- **Flag a review**
  - **Endpoint**: `POST /api/v1/reviews/{id}/flag?reason=...`
  - **Auth**: `hasAuthority('reviews:manage')` (owner/merchant side).
  - **Flow**:
    - `ReviewService.flagReview(id, reason)` loads the review.
    - Sets `status` back to `PENDING`, `flaggedByOwner = true`, and `flagReason = reason`.
    - Saves and returns `ReviewResponse`.
  - **Business intent**: Allow listing/brand owners to escalate problematic reviews for moderation re‑evaluation.

- **Reply to a review**
  - **Endpoint**: `POST /api/v1/reviews/{id}/reply`
  - **Auth**: `hasAuthority('reviews:manage')`
  - **Request body**: raw `String` reply text.
  - **Flow**:
    - `ReviewService.replyToReview(id, reply)` sets `ownerReply` on the review and saves.
  - **Business intent**: Let owners respond publicly to reviews, improving transparency and customer trust.

### 3. Core Domain Model and How It Maps to Business Concepts

Domain classes live in `com.chamrong.iecommerce.review.domain` and `com.chamrong.iecommerce.review.domain.model`.

- **`Review` (entity)**
  - Represents a single customer review tied to a **product** and **booking**.
  - Key fields:
    - `productId`, `customerId`, `bookingId` (booking enforces “one review per stay/experience”).
    - `isAnonymous`: whether customer identity is hidden publicly.
    - `rating` and granular rating fields (cleanliness, accuracy, communication, location, check‑in, value).
    - `comment` and `mediaKeys` (CSV of up to three image keys in storage).
    - `status`: `PENDING`, `APPROVED`, `REJECTED`.
    - Moderation metadata: `flaggedByOwner`, `flagReason`, `ownerReply`.
  - Behaviors:
    - `approve()`: transitions to `APPROVED` (visible on storefront).
    - `reject()`: transitions to `REJECTED` (not shown to customers).

- **`ReviewStatus` (domain enum)**
  - Current lifecycle states: `PENDING`, `APPROVED`, `REJECTED`.
  - Used both on the entity and in repository queries to control visibility and moderation queues.

- **`ReviewStatus` in `domain.model`**
  - Extended lifecycle: `PENDING`, `APPROVED`, `REJECTED`, `HIDDEN`, `DELETED`.
  - Reflects a richer business vision:
    - `HIDDEN`: temporarily removed from storefront (e.g., under investigation).
    - `DELETED`: soft‑deleted for audit/compliance while keeping it out of all user‑facing views.
  - **Currently not wired** into the live entity, services, or API — this is a key alignment gap for future work.

- **`Rating` (value object)**
  - Wraps an integer 1..5 and enforces this invariant in its constructor.
  - Captures business rule “rating must be between 1 and 5”.
  - Not yet used by `Review` or `ReviewService` (they still use raw integers).

- **`ReviewTarget` (enum)**
  - Declares supported review targets: `PRODUCT`, `ORDER`, `SERVICE`, `BOOKING`, `STORE`, `STAFF`.
  - Represents a broader strategy where reviews may eventually apply to multiple verticals (not just products).
  - **Currently** the live API and entity focus on product/booking; other targets are planned but not implemented.

### 4. Summary of Current vs Intended Business Alignment

- **Currently implemented**:
  - Product/booking‑based reviews with overall and granular ratings, text, and media.
  - Simple moderation lifecycle via `PENDING → APPROVED/REJECTED`.
  - Owner tools: flagging for re‑moderation and public reply.
  - Storefront only shows `APPROVED` reviews via the public fetch endpoint.

- **Intended but not yet fully implemented (from domain model)**:
  - Richer lifecycle with `HIDDEN` and `DELETED` states for temporary hides and soft delete.
  - Generalized review targets beyond products (orders, services, bookings, stores, staff).
  - Encapsulated rating validation and other invariants using value objects like `Rating`.

This document should be kept in sync with future changes to APIs and domain classes so product, engineering, and data teams share a single view of how reviews behave across the ecommerce platform.

### 5. Key Business Rules & Invariants (Implemented)

- **Rating invariants**
  - Overall rating is **required** and must be between 1 and 5; enforced by the `Rating` value object.
  - Granular ratings (cleanliness, accuracy, communication, location, check‑in, value) are optional but, when provided, must also be between 1 and 5.
- **Lifecycle and moderation**
  - New reviews are created in `PENDING` status and are not visible on storefronts until approved.
  - Moderators can transition a review to `APPROVED` or `REJECTED`. These transitions are guarded in the `Review` aggregate; attempts to mutate a `DELETED` review raise a `ReviewDomainException`.
  - A `DELETED` review is considered immutable from a business perspective (no further state changes allowed).
- **Flagging and replies**
  - Owners can flag a review only with a **non‑blank reason**; flagging moves the review back to `PENDING` and records `flaggedByOwner` and `flagReason`.
  - Owners can reply to a review only with **non‑blank** reply text.
- **Tenant isolation**
  - Every review is associated with a `tenantId` (set from `TenantContext` during submission); this ID is also propagated to outbox events for cross‑module consumers.
- **Outbox and integration events**
  - When a review is submitted, a `ReviewSubmittedEvent` is written to the review outbox table.
  - When a review is approved or rejected, corresponding `ReviewApprovedEvent` or `ReviewRejectedEvent` entries are written to the outbox.
  - An outbox relay scheduler asynchronously reads pending review events and dispatches them via the platform `EventDispatcher` to downstream modules (e.g. analytics, loyalty, notifications).

