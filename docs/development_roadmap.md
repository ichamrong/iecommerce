# Development Roadmap: Headless B2B2C SaaS Platform

> **Status:** Active Planning & Initial Implementation  
> **Architecture:** Modular Monolith (Spring Modulith)  
> **Primary Objective:** Build a Multi-Tenant, API-First platform for Merchants to power varying storefront experiences.

This active roadmap breaks down the development lifecycle of the SaaS platform. The development sequence focuses on establishing the secure billing foundation first, followed by incremental monetization packages that Shop Admins can subscribe to.

---

## 👑 Phase 0: The SuperAdmin Control Plane
**Timeline:** Month 0 (Pre-Launch) | **Target Version:** `v0.9.0-core`  
**Objective**: Build the centralized management dashboard for the **SaaS Owner** (You) to control the platform, manage tenant subscriptions, and view global revenue before onboarding a single customer.

> 💡 **Target Markets & Solutions:**
> *   **Internal Only (The SaaS Operating System)**
>   *   **The Problem:** You cannot run a scalable SaaS business effectively by manually editing database rows when a merchant wants to upgrade their plan or gets locked out of their account.
>   *   **Why Build It:** To have full oversight of system health, total revenue, active merchants, and support tickets in one centralized Next.js / React dashboard.
>   *   **How It Helps:** This is the command center where you can click a button to approve Merchant eKYC documents, configure new global Pricing Plans (e.g., "Create a $19/mo Promotion Plan"), and suspend bad actors.

*   **Global IAM (`auth`)**: Configure the highest level `SUPER_ADMIN` roles in Keycloak, ensuring absolute isolation from standard Tenant-scoped roles.
*   **Tenant Management (`setting`, `auth`)**: Build APIs to manually create, suspend, or hard-delete an entire Tenant (Shop Admin).
*   **Plan Configuration (`subscription`)**: Develop the interface to define Global Pricing Plans, set storage quotas, and define which modules are turned on/off per plan.
*   **Global Financial Dashboards (`report`)**: Produce overarching analytics showing Total Gross SaaS Revenue, Active MRR (Monthly Recurring Revenue), and Churn Rate across all tenants.
*   **eKYC Approval Workflows (`customer`, `asset`)**: Build a secure queue for SuperAdmins to manually review and approve uploaded Business Licenses or Passports if the automated system flags them for review.

---

## 🚀 Phase 1: SaaS Foundation & Mentorship APIs
**Timeline:** Months 1-3 | **Target Version:** `v1.0.0-alpha`  
**Objective**: Build the multi-tenant SaaS infrastructure required to securely bill Shop Admins, while simultaneously delivering **Mentorship, Life Planning, and E-Learning** as your very first monetizable products.

> 💡 **Target Markets & Solutions:**
> *   **Single-Tenant (Coaching & Mentorship)**: Life Coaches, Career Mentors, Fitness Trainers, Therapists.
>   *   **The Problem:** Coaches discuss 'Life Plans' over Zoom, but clients forget their goals and fail to execute their daily to-do lists once the call ends.
>   *   **Why They Buy:** Clients pay life coaches for real accountability. Coaches buy your SaaS to provide their clients with an interactive app/dashboard that automatically follows up with them and tracks progress.
>   *   **How We Help:** Coaches can assign a personalized 'Todo List' or 'Goal Plan'. The system uses the `notification` module to automatically check-in on the client (e.g., via WhatsApp) to ensure they are completing tasks, eliminating the coach's manual follow-up work.
> *   **Single-Tenant (Education)**: Universities, Private Tutors, Corporate Training departments.
>   *   **The Problem:** Sending Zoom links and Google Drive PDFs manually makes them look unprofessional and increases the risk of piracy.
>   *   **Why They Buy:** To host a professional, branded learning portal that automatically tracks student progress.
>   *   **How We Help:** We provide a complex course hierarchy (Modules/Lessons), drip-released content via email, and automated certificate generation upon completion.

**Block A: Master Foundation & Core Models**
*   **Database Bedrock**: Write core Liquibase migrations to enforce row-level `tenant_id` isolation across all tables.
*   **Subscription Module (`subscription`)**: Develop the engine to handle Subscription Plans, Trial Periods, and Feature Gating.
*   **Authentication & IAM (`auth`)**: Complete RBAC mapping to Keycloak. Ensure Shop Admins can log in securely and receive JWTs scoped to their tenant.
*   **Payment Integration (`payment`)**: Build Stripe/Braintree webhooks to handle the SaaS recurring monthly billing for the Shop Admins.
*   **Identity Verification (eKYC)**: Implement a third-party biometric verification provider (e.g., Sumsub, Onfido) during the Shop Admin onboarding flow.

**Block B: Mentorship & E-Learning Features**
*   **Life Plans & Course Hierarchies (`catalog`)**: Add support for the `COURSE` product type. Unlike a standard e-commerce item, this requires complex hierarchy (Goal -> Milestones -> Daily Tasks / Course -> Modules -> Lessons).
*   **Goal Tracking & To-Do Lists (`customer`)**: Expand the student's profile to allow Mentors/Coaches to assign personalized "Life Plans" or daily Habit Trackers.
*   **Progress Tracking (`customer`)**: Track lesson-by-lesson completion or daily to-do list checkboxes mapped directly to the user profile.
*   **Automated Accountability (`notification`)**: Schedule automated emails or WhatsApp messages to remind clients to complete their daily goals (e.g., "Don't forget to apply for 3 jobs today!").
*   **Private Materials (`asset`)**: Securely host PDF workbooks, slides, and accompanying lecture videos, preventing unauthorized downloads.
*   **Mentorship Chat (`chat`)**: Facilitate direct 1-to-1 WebSockets Q&A messaging between clients and coaches.
*   **Assessments & Check-ins (`review`)**: Repurpose the review system to handle grading, mood tracking submissions, and coach feedback.
*   **Certificate Generation (`invoice`)**: Repurpose the PDF generator to issue customized "Certificates of Completion" when a student finishes the curriculum.

---

## 🏨 Phase 2: Single-Property Accommodation (Direct B2C APIs)
**Timeline:** Months 4-5 | **Target Version:** `v1.0.0-beta`  
**Objective**: Secure the next monetization target by selling the "Hotel & Rental" headless API package to individual property managers.

> 💡 **Target Markets & Solutions:**
> *   **Single-Tenant (Short-Term)**: Independent Hoteliers and Boutique Lodges.
>   *   **The Problem:** Connecting to OTA platforms (Agoda, Booking.com) costs 15-20% in commission fees per nightly booking.
>   *   **Why They Buy:** To build a "Direct Booking" website where they keep 100% of the revenue.
>   *   **How We Help:** We provide the complex calendar logic, room catalog, and deposit APIs out-of-the-box.
> *   **Single-Tenant (Long-Term Rentals)**: Private Landlords, Apartment Buildings, and Room Rental Hosts.
>   *   **The Problem:** Landlords collect monthly rent manually via scattered bank transfers, constantly chasing down late payments, and dealing with disorganized WhatsApp maintenance requests.
>   *   **Why They Buy:** To fully automate rent collection and centralize their tenant communication.
>   *   **How We Help:** The system auto-generates recurring monthly rent invoices (`invoice`), processes KHQR/Credit Card payments directly from the renter (`payment`), and provides a portal for submitting plumbing/repair requests (`chat`).

*   **Catalog Upgrades (`catalog`)**: Add support for the `ACCOMMODATION` product type, allowing hoteliers to define room types, features, and amenities.
*   **Booking Engine (`booking`)**: Implement complex time-based availability calendars, handling blackout dates, minimum stays, and conflict checks.
*   **Guest Profiles & eKYC (`customer`)**: Capture the Guest's core profile, along with secure, encrypted uploads of National IDs/Passports for hotel compliance.
*   **Order Processing (`order`)**: Enhance the state machine to handle check-in/check-out dates.
*   **Deposit Payments (`payment`)**: Process partial reservation deposits and cancellation policy refunds.
*   **Guest Receipts (`invoice`)**: Generate "Proforma" and final Folio tax invoices for the guests upon checkout.
*   **Accommodation Promos (`promotion`)**: Support seasonal discount codes or "Stay 3 Nights, get 10% off" logic.
*   **Occupancy Dashboards (`report`)**: Track daily Occupancy Rates and total Revenue analytics for the hoteliers.

---

## 🛍️ Phase 3: E-commerce APIs
**Timeline:** Months 6-8 | **Target Version:** `v1.1.0`  
**Objective**: Open the platform to digital and retail store owners selling standard goods.

> 💡 **Target Markets & Solutions:**
> *   **Single-Tenant**: Clothing Brands, Supermarkets, Digital Asset creators.
>   *   **The Problem:** Selling manually via Instagram or Facebook Messenger is chaotic and leads to lost inventory and uncollected payments.
>   *   **Why They Buy:** They need to automate checkout so they can make money 24/7 without manual chatting.
>   *   **How We Help:** We provide shopping carts, fully automated Stripe/Bakong gateways, and pick/pack fulfillment workflows.

*   **Catalog Variants (`catalog`)**: Implement `PHYSICAL` and `DIGITAL` product types with fully localized translations and SKUs (e.g., T-Shirt - Red/Large).
*   **Inventory Tracking (`inventory`)**: Build real-time stock counting, reservations, and low-stock threshold alerts.
*   **Fulfillment Workflows (`order`)**: Develop the Pick, Pack, and Ship logic. Generate and attach tracking numbers to physical shipments.
*   **Checkout & Carts (`payment`)**: Build end-customer checkout flows processing Credit Cards, Bakong, and Cash on Delivery.
*   **Customer Addresses & Loyalty (`customer`)**: Expand the Guest profile domain to support multiple physical shipping addresses and track loyalty reward points.
*   **Promotions Engine (`promotion`)**: Create flexible rule engines for percentage discounts and voucher codes.
*   **Product Ratings (`review`)**: Allow verified buyers to leave 1-5 star ratings with attached photo evidence of the received product.
*   **Retail Dashboards (`report`)**: Produce visual E-commerce analytics showing Top Selling Items, Abandoned Carts, and retail Revenue charts.

---

## 💆 Phase 4: Booking Service APIs
**Timeline:** Months 9-10 | **Target Version:** `v1.2.0`  
**Objective**: Expand the market to appointment-based businesses (Salons, Spas, Clinics).

> 💡 **Target Markets & Solutions:**
> *   **Single-Tenant**: Local Spas, Barbershops, Medical Clinics, Plumbers, Cleaners.
>   *   **The Problem:** Receptionists spend hours on the phone negotiating time slots, and customers frequently "no-show".
>   *   **Why They Buy:** To free up staff time and capture upfront booking deposits to secure revenue.
>   *   **How We Help:** Customers book open hourly slots themselves online, pay a non-refundable deposit, and receive automated WhatsApp reminders 24 hours prior.

*   **Service Entities (`catalog`)**: Add support for the `BOOKING` product type, defining service durations.
*   **Hourly Scheduling (`booking`)**: Adapt the booking engine to handle minute/hourly slot reservations rather than just nightly stays. 
*   **Service Staff Allocation (`staff`)**: Map available employees (e.g. Masseur, Hair Stylist) onto specific booking slots based on their shift schedules.
*   **Appointment Deposits (`payment`, `order`)**: Charge a non-refundable upfront deposit to secure the hourly slot.
*   **Client Histories (`customer`)**: Enhance profiles to store "past service notes" or client preferences across multiple visits.
*   **Off-Peak Promos (`promotion`)**: Introduce time-of-day dynamic discounts (e.g., "-20% for 10am Tuesday appointments" to fill dead slots).

---

## 🧾 Phase 5: POS (Point of Sale) APIs
**Timeline:** Months 11-12 | **Target Version:** `v1.3.0`  
**Objective**: Provide an Omni-channel solution allowing existing E-commerce clients to ring up customers in physical brick-and-mortar stores.

> 💡 **Target Markets & Solutions:**
> *   **Single-Tenant**: Physical Clothing Boutiques, Coffee Shops, local Grocery Stores.
>   *   **The Problem:** The shop has an online store, but their physical cash register doesn't sync. If they sell a shirt in-store, they have to manually delete it from the website.
>   *   **Why They Buy:** To achieve true "Omni-channel" retail where 1 centralized dashboard controls both online and offline stock.
>   *   **How We Help:** The POS API allows cashiers to scan barcodes on iPads, instantly debiting the global cloud inventory while printing physical thermal receipts.

*   **Terminal Tracking (`auth`, `order`)**: Extend the APIs to identify exactly which physical cash register or employee iPad initiated a sale.
*   **Cashier Management (`staff`)**: Manage shift opening/closing, register assignments, and restrict discounts to managers.
*   **Instant Fulfillment (`inventory`)**: Bypass the standard warehouse "Pick/Pack/Ship" workflow to instantly relieve inventory for in-person handovers.
*   **Instore Loyalty (`customer`)**: Allow cashiers to look up a customer via phone number to apply accrued loyalty points in person.
*   **Thermal Receipts (`invoice`)**: Develop PDF templates optimized for physical receipt printers (tax details, return policies).
*   **Reconciliation (`report`)**: Build End-of-Day (Z-Out) reporting APIs to track cash drawer totals, discrepancies, and salesperson performance.

---

## 🍿 Phase 6: Streaming Media APIs (Movies & Dramas)
**Timeline:** Months 13-14 | **Target Version:** `v1.4.0`  
**Objective**: Build out the infrastructure to allow media groups to sell Netflix-style recurring subscriptions or Pay-Per-View access to video content.

> 💡 **Target Markets & Solutions:**
> *   **Single-Tenant**: Film Studios, News Outlets, Independent Production companies.
>   *   **The Problem:** Relying on YouTube ads is unpredictable and pays pennies per view.
>   *   **Why They Buy:** To sell premium video content directly to hardcore fans without middlemen.
>   *   **How We Help:** We provide secure video streaming (DRM) locked behind automated recurring subscription billing blocks.

*   **Media Access Plans (`catalog`)**: Add support for the `MEDIA_SUBSCRIPTION` product type, allowing merchants to lock specific video content behind a paywall.
*   **Recurring Billing (`payment`, `order`)**: Extend Stripe/Braintree webhooks to handle recurring tokenized subscription charges and one-off Pay-Per-View sales from the End-Customer.
*   **Secure Delivery & DRM (`asset`)**: Implement secure, expiring, signed URLs (HLS/DASH streaming formats) to prevent piracy or unauthorized downloading of the videos.
*   **Watch History (`customer`)**: Track an End-Customer's playback state (e.g., "Resume Episode 3 at 14:22").

---

## 🌍 Phase 7: Multi-Vendor Marketplaces (Agency Clones)
**Timeline:** Months 15-16 | **Target Version:** `v1.5.0`  
**Objective**: Expand the architecture from B2B2C to **B2B2B2C**, allowing "Agencies" to buy the SaaS, board multiple third-party "Hosts", and take a cut of the sales.

> 💡 **Target Markets & Solutions:**
> *   **Agency Model:** Entrepreneurs who want to build the next *"Airbnb"*, *"TaskRabbit"*, *"Patreon"*, or *"Udemy"*.
>   *   **The Problem:** Building a massive multi-vendor marketplace from scratch costs hundreds of thousands of dollars in engineering.
>   *   **Why They Buy:** To instantly launch an agency marketplace using our powerful Phase 1-6 foundation.
>   *   **How We Help:** Our APIs handle the payment-splitting, letting the Agency automatically pocket a 15% commission on every transaction while restricting vendors to only see their own sales.

*   **Host Scoping (`staff`, `auth`)**: Add a strict `HOST` role to the RBAC engine. A host can only `INSERT` their own properties into the catalog and can only `SELECT` orders tied to their own listings.
*   **Multi-Vendor Carts (`order`)**: Enhance the shopping cart so if an End-Customer books two different houses from two different hosts, the single order splits into sub-orders for fulfillment tracking.
*   **Commission Logic (`setting`, `catalog`)**: Allow the Agency to define flat or percentage-based marketplace commission fees (e.g., "The Agency takes 15% of all bookings").
*   **Payment Splitting (`payment`)**: At checkout time, instruct the payment gateway to automatically route 15% of the transaction to the Agency's bank account, and 85% directly to the Host's connected bank account.
*   **Host Payout Dashboards (`report`, `invoice`)**: Provide individual dashboards for hosts to see their earnings, and auto-generate end-of-month payout statements for tax purposes.

---

## 🛵 Phase 8: Logistics & Delivery Marketplaces (3-Way Splits)
**Timeline:** Months 17-18 | **Target Version:** `v1.6.0`  
**Objective**: Expand the Marketplace APIs to include "Last Mile" logistics, allowing Agencies to launch DoorDash or Nham24 clones that involve three parties: The Merchant, The Customer, and The Driver.

> 💡 **Target Markets & Solutions:**
> *   **Agency Model:** Food Delivery apps (FoodPanda clones), Grocery Delivery apps, or local courier networks.
>   *   **The Problem:** Merely selling items online isn't enough; managing hundreds of independent drivers is chaotic and logistically impossible without automation.
>   *   **Why They Buy:** To compete with mega-corporations like Grab by launching their own perfectly optimized delivery fleet in a specific city.
>   *   **How We Help:** We provide live geospatial driver tracking, routing ETAs, and a complex 3-way commission split.

*   **Driver Profiles (`staff`, `auth`)**: Add a `DRIVER` role. Drivers can only view `orders` that are actively assigned to them or marked as "Ready for Pickup".
*   **Geospatial Routing (`order`, `setting`)**: Integrate with Google Maps or Mapbox APIs to calculate distance-based delivery fees and estimate arrival times (ETAs).
*   **Order State Machine Expansion (`order`)**: Add intermediate logistical states: `COOKING` -> `DRIVER_ASSIGNED` -> `PICKED_UP` -> `ON_THE_WAY` -> `DELIVERED`.
*   **Live Driver Tracking (`chat`, `notification`)**: Use WebSockets to push the driver's GPS coordinates to the End-Customer's app in real-time.
*   **Three-Way Commission Split (`payment`)**: Expand the Phase 7 payment splitter to route revenue 3 ways: 1) The Food Cost to the Restaurant, 2) The Delivery Fee to the Driver, 3) The Commission to the Agency's bank account.

---

### Future Horizons (Phase 9+)
*   *Global Search API:* Elasticsearch integration for lightning-fast catalog querying across millions of SKUs.
*   *Headless Storefront Templates:* Open-sourcing Next.js and Vue storefront templates that come pre-wired to our `iecommerce` endpoints.
