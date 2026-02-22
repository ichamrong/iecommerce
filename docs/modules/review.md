# Module Specification: Review

## 1. Purpose
The Review module handles the collection and display of customer feedback, ratings, and reviews for products or services (e.g., purchased items, bookings, stays).

## 2. Core Domain Models
- **Review**: The central record containing a numerical rating and optional text feedback.
- **ReviewImage**: Links to the `asset` module for user-uploaded photos of the product.
- **Reply**: A response from the merchant/tenant to a customer's review.

## 3. Key Business Logic
- **Verified Purchases**: Enforces that a customer can only review products they have successfully purchased or booked (checked via the `Order` or `Booking` modules).
- **Moderation**: Allows tenants to flag inappropriate reviews, while ensuring a transparent and tamper-resistant feedback system.
- **Aggregations**: Automatically calculates and caches the average rating score and total review count per product.

## 4. Multi-Tenancy Strategy (SaaS)
- Reviews are fully isolated per `tenant_id` to ensure accurate feedback tracking for individual merchants. 
- Aggregations and overall product ratings are computed only within the tenant's context.

## 5. Public APIs (Internal Modulith)
- `ReviewService.submitReview(...)`: Adds a new customer review to a product.
- `ReviewService.getProductReviews(...)`: Retrieves paginated reviews with their merchant replies.
