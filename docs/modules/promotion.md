# Module Specification: Promotion

## 1. Purpose
The Promotion module manages marketing campaigns, discount rules, and coupon codes to drive sales and customer loyalty.

## 2. Core Concepts
- **Promotion**: The high-level campaign (e.g., "Year-End Sale").
- **PromotionRule**: The logic that determines if a discount applies (e.g., "If Order Total > $100").
- **PromotionAction**: The effect of the promotion (e.g., "Apply 10% Discount", "Free Shipping").
- **CouponCode**: A specific code a user enters at checkout (e.g., `SAVE10`).
- **DiscountRecord**: An audit of every discount applied to an order.

## 3. Promotion Engine Strategy
To handle complex marketing needs, we use a **Rule Engine** approach:

### A. Core Rule Types
- **Flash Sales (Time Windows)**: Promotions that are strictly valid only during a specific time window (e.g., "12:00 PM to 2:00 PM today").
- **Buy X Get Y (BOGO)**: 
    - *Example*: Buy 2 get 1 free.
    - *Example*: Buy a Camera, get a Memory Card at 50% off.
- **Tiered Discounts**: "Spend $100, save 10% | Spend $200, save 20%".
- **Early Bird Discounts**: 
    - *Example*: Book 60 days in advance to get 15% off.
    - *Logic*: `Booking.startDate` - `currentDate` > 60 days.
- **Last-Minute Deals**: 
    - *Example*: Book within 2 days of stay for 50% off.
    - *Logic*: `Booking.startDate` - `currentDate` < 2 days.
- **Seasonal & Event Promotions**: 
    - **One-Time Events**: Unique dates (e.g., "Grand Opening" on June 1st, 2026). Once finished, it never repeats.
    - **Recurring Seasonal (Looping)**: 
        - **Fixed Date**: Annual dates (e.g., "Songkran Sale" every April 13-15).
        - **Floating Date**: Holidays that change dates every year (e.g., **Black Friday**, **Chinese New Year**). These are usually managed as **One-Time Events** each year to allow for unique marketing creative and specific date alignment.
- **Loyalty Tiers (Membership Levels)**: 
    - *Concept*: Tiers are **Dynamic Customer Groups**. The system automatically moves users into these groups based on their behavior.
    - *Example*: **Diamond** members get free airport pickup.
    - *Logic*: The engine looks back at the last `X` months and calculates:
        - **Total Spend ($)**: Total revenue from this customer.
        - **Frequency (Count)**: Total number of completed bookings.
        - **Duration (Days)**: Total nights stayed (for Accommodations).
    - *Tiers*: **SILVER** (Entry), **GOLD** (Medium), **PLATINUM** (High), **DIAMOND** (VIP).
    
### B. Tier Progression Rules (How to Earn)
Administrators configure rules to move customers between tiers:
- **Upgrade Rules**: 
    - *"If Spend > $2,000 in 12 months -> Move to GOLD"*
    - *"If Bookings > 10 in 12 months -> Move to GOLD"*
- **Retention Rules**: 
    - *"Must spend at least $500/year to keep SILVER status."*
- **Tier Downgrade**: If a customer's look-back data falls below the threshold, they are moved down a tier automatically.

### C. Execution Logic
1. **Conditions**: Rules can be based on:
   - **Time & Schedule**: Precise Start/End timestamps (down to the minute).
   - **Cart Contents**: Quantities, Categories, or specific Product IDs.
   - **Customer Status**: (e.g., "New Customer" or "VIP Tier").
2. **Prioritization**: If multiple promotions apply, we use a **Priority Score** to decide which one to use, or if they can be combined (stackable).
3. **Validation**: The `Order` module calls the `Promotion` module during checkout to calculate the final price.

### B. Conflict Resolution: Stacking & Priority
When multiple promotions apply to the same cart, the engine uses a two-step resolution process:

1. **Priority Index**: Every promotion has a `priority` number (e.g., 0 to 100). The engine processes them from **Highest to Lowest**.
2. **Stackable Flag (`isStackable`)**:
   - **If NOT Stackable**: Once this promotion is applied, no other promotions can be added to the cart. It "locks" the price. (Example: "Clearance Sale - 70% Off" usually doesn't allow coupons).
   - **If Stackable**: Other promotions can still be applied if they are also stackable. (Example: "Free Shipping" often stacks with a "10% Coupon").

**Standard Recommendation**: 
- **Auto-Apply Promotions** (Seasonal, Flash Sales) usually have a higher priority and are non-stackable.
- **Manual Coupons** usually have lower priority but can be stackable with shipping offers.

## 4. Multi-Tenancy Strategy
- Promotions and Coupons are strictly isolated by `tenant_id`.
- Support for "Global Promotions" if configured by the platform owner.

## 5. Public APIs (Internal Modulith)
- `PromotionService.applyPromotions(cart)`: Evaluates all active rules against a cart and returns the discounts.
- `PromotionService.validateCoupon(code, customerId)`: Checks if a coupon is valid and hasn't exceeded its usage limit.
- `PromotionService.getActiveCampaings()`: Returns currently running promotions for the storefront.
