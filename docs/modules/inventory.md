# Module Specification: Inventory (Quantity-Based)

## 1. Purpose
The Inventory module manages **Physical Quantity-Based** stock levels across multiple locations. It is designed for items that are sold by count (e.g., T-shirts, Electronics).

## 2. Core Domain Models
- **Warehouse**: A storage location (e.g., "Main Warehouse", "Returns Shelf").
- **StockLevel**: The quantity of a specific `ProductVariant` in a specific `Warehouse`.
- **StockMovement**: A high-integrity audit log of every change (Reason: DAMAGED, EXPIRED, SALE, RESTOCK, RETURNED).
- **StockReservation**: A temporary "lock" on items during the checkout process.
  - **Timeout**: Defaults to **15 minutes**.
  - **Release**: A background task (or specific event) releases expired reservations back into "Sellable" stock if the order is not completed.

### Stock Calculation Definitions
To support accurate reporting, we define three types of stock counts:
1. **Stock on Hand (Physical)**: The actual quantity physically present in the warehouse. (Managed in **Inventory**)
2. **Reserved Stock**: The quantity currently locked in active checkouts/orders but not yet shipped. (Managed in **Inventory**)
3. **Sellable Stock (Available)**: The quantity available for sale on the storefront. (Managed in **Inventory**)
   - **Formula**: `Stock on Hand` - `Reserved Stock` = `Sellable Stock`.

**Important**: The `inventory` module is the **Source of Truth**. The `report` module only reads this data for analytics; it never modifies it.

## 3. Stock Management Strategy
- **Transactional Integrity**: Every change to a `StockLevel` **must** be accompanied by a `StockMovement`.
- **Source of Truth**: The `StockMovement` is the primary record. The `StockLevel` acts as a materialized view of the sum of all movements for a specific SKU/Warehouse combination.
- **Auditing**: You cannot simply "change" a number. You must create a `StockMovement` explaining *why*.

### Handling Damaged/Broken Items
When physical stock is "broken" or lost, the following flow is used:
1. **Adjustment**: An Admin/Manager calls `adjustStock` with a negative quantity.
2. **Reasoning**: The movement is tagged as `DAMAGED`.
3. **Traceability**: An optional `comment` field allows adding details (e.g., "Box dropped in aisle 4", "Leaking container").
4. **Accounting**: This allows the `Report` module to calculate "Shrinkage" or "Loss" costs accurately.

## 4. Multi-Tenancy & Performance
- **Isolation**: Each tenant manages its own Warehouses and Stock Levels.
- **Partitioning**: The `StockMovement` table is **Partitioned by Date**. Since every stock change generates a row, this table grows the fastest. Partitioning ensures audit lookups remain performant.
- **Composite Indexing**: Standard lookup index on `(tenant_id, variant_id, warehouse_id)` for O(1) stock checks.

## 5. POS & Walk-in Strategy
Unlike online orders that use "Reservations," POS sales often use **Immediate Deduction**:
- **Flow**: Item is scanned -> Payment confirmed -> Stock is deducted immediately (skipping the 15-min reservation).
- **Warehouse Mapping**: Every POS terminal is mapped to a specific "Retail Floor" warehouse to ensure local stock accuracy.

## 5. Key Integration Flows
- **Checkout Started**: `Order` module requests `Inventory` to create a `StockReservation`.
- **Payment Successful**: `Order` module notifies `Inventory` to convert the Reservation into a `SALE` StockMovement.
- **Damaged Item**: Admin records a `DAMAGED` StockMovement to adjust levels for a specific Warehouse.

## 6. Public APIs (Internal Modulith)
- `InventoryService.reserveStock(variantId, quantity)`: Reserves items for a pending order.
- `InventoryService.commitStock(orderId)`: Finalizes a sale.
- `InventoryService.adjustStock(variantId, warehouseId, quantity, reason)`: Manual adjustment for damage/expiry.
- `InventoryService.getAvailability(variantId)`: Returns the total "Sellable" stock (Total - Reserved).
