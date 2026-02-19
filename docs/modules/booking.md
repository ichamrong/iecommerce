# Module Specification: Booking (Time-Based)

## 1. Purpose
The Booking module manages **Time-Based Availability**. Unlike physical inventory, it tracks the "Occupancy" of a resource over a calendar rather than a numerical count.

## 2. Core Domain Models
- **BookableResource**: The **Physical Entity** (e.g., "Room 101", "Villa A2").
  - *Linking*: Links back to a `ProductVariant` in the Catalog module to inherit its name, price, and rules.
- **AvailabilityCalendar**: A time-series record of open/blocked/booked slots.
- **BookingRecord**: Links an `Order` to a `BookableResource`.
  - **Data**: Stores Arrival/Departure dates.
  - **Rule Acknowledgment**: Snapshot of the "House Rules" the customer agreed to at the time of booking.
- **ResourceBlock**: A manual override used by Admins to "Blackout" dates.
  - **Maintenance**: Room is out of order.
  - **Offline Booking**: Room was booked via phone/in-person.
  - **Reserved for Owner**: Room is not for sale during specific periods.

## 3. Availability Strategy: Overbooking Protection
Instead of a simple "Quantity," we use **Occupancy Logic**:
1. **Request**: `checkAvailability(resourceId, startDate, endDate)`.
2. **Logic**: The system checks if the range intersects with ANY `BookingRecord` **OR** `ResourceBlock`.
3. **Capcity Enforcement**: Before confirming a booking, the system validates the request against the **Product Capacity Rules** (Adults, Children, Pets).
4. **Locking**: Similar to "Stock Reservation," a **Time Lock** is placed for 15 minutes during the checkout process to prevent double-booking.

## 4. Multi-Tenancy Strategy
- Tenants can define their own "Booking Rules" (e.g., "Minimum 2-night stay", "No check-ins on Sundays").
- Calendars are strictly isolated by `tenant_id`.

## 5. Public APIs (Internal Modulith)
- `BookingService.isAvailable(resourceId, range)`: Boolean check.
- `BookingService.getCalendar(resourceId, month)`: Returns a list of busy/free slots for UI display.
- `BookingService.reserveTimeSlot(orderId, range)`: Temporary lock.
- `BookingService.confirmBooking(orderId)`: Finalizes the reservation.

## 6. Cancellation & Refund Policies
For accommodations, the system supports **Time-Based Tiers**:
- **Full Refund**: Cancellation > 7 days before arrival.
- **Partial (50%)**: Cancellation between 7 days and 48 hours before arrival.
- **Non-Refundable**: Cancellation < 48 hours before arrival.
- **Service Fees**: Tenants can configure a fixed "Cancellation Fee" regardless of the policy.

### Integration
1. **Trigger**: Customer clicks "Cancel Booking".
2. **Logic**: `Booking` module calculates the time difference.
3. **Action**: `Booking` notifies `Order` module of the allowed refund percentage.
4. **Finalization**: `Order` module executes the money transfer via `Payment`.
