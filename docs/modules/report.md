# Module Specification: Report

## 1. Purpose
The Report module provides business intelligence and analytics by aggregating data from across the system. It is strictly a **Read-Optimized** module.

## 2. Core Concepts
- **Dashboard Metrics**: Real-time KPIs (e.g., "Total Sales Today", "Active Carts").
- **Financial Reports**: Immutable records of revenue, taxes, and discounts.
- **Inventory Analytics**: Trends in stock levels, shrinkage (damaged/broken items), and restock history.
- **Customer Insights**: Segments, lifetime value (LTV), and churn rate.

## 3. Database Strategy: Phase-Based Approach
Choosing the right storage for reports depends on the scale of the business:

### Phase 1: PostgreSQL (Current Choice)
- **Why**: PostgreSQL is world-class at relational analytics. Features like `CTE` (Common Table Expressions) and `Window Functions` allow us to write complex reports with simple SQL.
- **Optimization**: We use **Materialized Views** to pre-calculate heavy reports every hour/day so they load instantly for managers.
- **Security**: Keeps all tenant data in a familiar, relational format.

### Phase 2: Dedicated Data Warehouse (Scale Choice)
- **Examples**: **ClickHouse**, **Google BigQuery**, or **Snowflake**.
- **When**: When the `report` module starts processing millions of events per day.
- **Why**: These databases use "Columnar Storage," which is 100x faster than Postgres for scanning billions of rows for total sums.

**Recommendation**: Start with **PostgreSQL**. It is simple, reliable, and more than powerful enough for the first 1-2 million orders.

## 4. Key Reports
- **Sales Performance**: By Category, Tenant, or Date range.
- **Geospatial Analysis**: **Sales by Region/Province** (identifies which geographic areas generate the most revenue).
- **Inventory Health**: Identifying slow-moving products or frequent damage reasons.
- **Tax Compliance**: Summarized tax collection for government reporting.

## 5. Public APIs (Internal Modulith)
- `ReportService.getSalesSummary(tenantId, range)`: Returns aggregated financial data.
- `ReportService.getInventoryPulse()`: Returns current "On Hand" vs "Sellable" totals for the dashboard.
