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

## 🚀 Phase 1: Foundation & HR Attendance SaaS
**Timeline:** Months 1-2 | **Target Version:** `v1.0.0-alpha`  
**Objective**: Build the multi-tenant SaaS infrastructure required to onboard Shop Admins and securely bill them, while simultaneously delivering a fully monetizable **Time & Attendance App** as the first product offering.

> 💡 **Target Markets & Solutions:**
> *   **Single-Tenant (HR Operations)**: Small offline businesses (Construction, Cafes, Clinics, Cleaning).
>   *   **The Problem:** Managers can't physically verify if employees are showing up on time across different remote job sites or cafes.
>   *   **Why They Buy:** It is cheaper and more reliable than installing physical fingerprint scanners at every location.
>   *   **How We Help:** Employees use their own phones to Check-in via GPS. The APIs automatically generate accurate end-of-month payroll timelines.

*   **Database Bedrock**: Write core Liquibase migrations to enforce row-level `tenant_id` isolation across all tables.
*   **Subscription Module (`subscription`)**: Develop the engine to handle Subscription Plans (e.g., *Accommodation Basic*, *Retail Pro*), Trial Periods, and Feature Gating.
*   **Authentication & IAM (`auth`)**: Complete RBAC mapping to Keycloak. Ensure Shop Admins can log in securely and receive JWTs scoped to their tenant.
*   **Settings & Quotas (`setting`)**: Implement quota enforcement logic (e.g., "Max 50 products on the Free tier") which other modules will invoke.
*   **Audit Logging (`audit`)**: Wire up asynchronous event listeners to capture immutable audit trails for compliance.
*   **Staff Profiles (`staff`)**: Allow Shop Admins to onboard their own employees (Cashiers, Support Agents) with restricted access roles.
*   **Time & Attendance (`staff`, `report`)**: Build GPS-verified mobile check-in/check-out APIs so merchants can track employee physical attendance and generate end-of-month payroll reports.
*   **Payment Integration (`payment`)**: Build Stripe/Braintree webhooks to handle the SaaS recurring monthly billing for the Shop Admins.
*   **Identity Verification (eKYC)**: Implement a third-party biometric verification provider (e.g., Sumsub, Onfido) during the Shop Admin onboarding flow to comply with AML laws before they can process their own cart checkouts.
*   **SaaS Invoicing (`invoice`)**: Auto-generate monthly PDF tax receipts that Shop Admins can download for their subscription payments.
*   **Initial Notifications (`notification`)**: Send automated "Welcome to the Platform" emails and verification links to newly registered merchants.

---

## 🏨 Phase 2: Single-Property Accommodation (Direct B2C APIs)
**Timeline:** Months 3-4 | **Target Version:** `v1.0.0-beta`  
**Objective**: Secure the first monetization target by selling the "Hotel & Rental" headless API package to individual property managers (direct B2C interaction).

> 💡 **Target Markets & Solutions:**
> *   **Single-Tenant (Short-Term)**: Independent Hoteliers and Boutique Lodges.
>   *   **The Problem:** Connecting to OTA platforms (Agoda, Booking.com) costs 15-20% in commission fees per nightly booking.
>   *   **Why They Buy:** To build a "Direct Booking" website where they keep 100% of the revenue.
>   *   **How We Help:** We provide the complex calendar logic, room catalog, and deposit APIs out-of-the-box.
> *   **Single-Tenant (Long-Term Rentals)**: Private Landlords, Apartment Buildings, and Room Rental Hosts.
>   *   **The Problem:** Landlords collect monthly rent manually via scattered bank transfers, constantly chasing down late payments, and dealing with disorganized WhatsApp maintenance requests.
>   *   **Why They Buy:** To fully automate rent collection and centralize their tenant communication.
>   *   **How We Help:** The system auto-generates recurring monthly rent invoices (`invoice`), processes KHQR/Credit Card payments directly from the renter (`payment`), and provides a portal for submitting plumbing/repair requests (`chat`).
> *   **Agency Model**: *"Airbnb Clones"* connecting travelers with multiple independent homeowners.

*   **Catalog Upgrades (`catalog`)**: Add support for the `ACCOMMODATION` product type, allowing hoteliers to define room types, features, and amenities.
*   **Asset Management (`asset`)**: Integrate Minio storage to serve high-resolution room and property imagery.
*   **Booking Engine (`booking`)**: Implement complex time-based availability calendars, handling blackout dates, minimum stays, and conflict checks.
*   **Guest Profiles & eKYC (`customer`)**: Capture the Guest's core profile (Name, Email, Phone) when making a reservation, along with secure, encrypted uploads of National IDs/Passports for hotel compliance.
*   **Order Processing (`order`)**: Enhance the state machine to handle check-in/check-out dates.
*   **Deposit Payments (`payment`)**: Process partial reservation deposits and cancellation policy refunds.
*   **Guest Receipts (`invoice`)**: Generate "Proforma" and final Folio tax invoices for the guests upon checkout.
*   **Booking Notifications (`notification`)**: Send automated "Booking Confirmed" and "Check-in Instructions" alerts to the Guests.
*   **Accommodation Promos (`promotion`)**: Support seasonal discount codes or "Stay 3 Nights, get 10% off" logic.
*   **Pre-arrival Inquiries (`chat`)**: Allow guests to message the front desk or host before their stay directly via the API.
*   **Post-Stay Reviews (`review`)**: Collect verified feedback and ratings after the guest has checked out.
*   **Occupancy Dashboards (`report`)**: Track daily Occupancy Rates and total Revenue analytics for the hoteliers.

---

## 🛍️ Phase 3: E-commerce APIs
**Timeline:** Months 5-7 | **Target Version:** `v1.1.0`  
**Objective**: Open the platform to digital and retail store owners selling standard goods.

> 💡 **Target Markets & Solutions:**
> *   **Single-Tenant**: Clothing Brands, Supermarkets, Digital Asset creators.
>   *   **The Problem:** Selling manually via Instagram or Facebook Messenger is chaotic and leads to lost inventory and uncollected payments.
>   *   **Why They Buy:** They need to automate checkout so they can make money 24/7 without manual chatting.
>   *   **How We Help:** We provide shopping carts, fully automated Stripe/Bakong gateways, and pick/pack fulfillment workflows.
> *   **Agency Model**: *"Amazon Clones"* connecting buyers with multiple independent retail sellers.

*   **Catalog Variants (`catalog`)**: Implement `PHYSICAL` and `DIGITAL` product types with fully localized translations and SKUs (e.g., T-Shirt - Red/Large).
*   **Product Media (`asset`)**: Handle multi-image galleries, 360-views, and video uploads per product variant.
*   **Inventory Tracking (`inventory`)**: Build real-time stock counting, reservations, and low-stock threshold alerts.
*   **Fulfillment Workflows (`order`)**: Develop the Pick, Pack, and Ship logic. Generate and attach tracking numbers to physical shipments.
*   **Checkout & Carts (`payment`)**: Build end-customer checkout flows processing Credit Cards, Bakong, and Cash on Delivery.
*   **Customer Addresses & Loyalty (`customer`)**: Expand the Guest profile domain to support multiple physical shipping addresses and track loyalty reward points.
*   **Promotions Engine (`promotion`)**: Create flexible rule engines for percentage discounts and voucher codes.
*   **Product Ratings (`review`)**: Allow verified buyers to leave 1-5 star ratings with attached photo evidence of the received product.
*   **Customer Support (`chat`)**: Introduce real-time WebSockets messaging linking a buyer's Order ID to a support agent's dashboard.
*   **Retail Dashboards (`report`)**: Produce visual E-commerce analytics showing Top Selling Items, Abandoned Carts, and retail Revenue charts.
*   **Fulfillment Notifications (`notification`)**: Send "Order Shipped" and "Out for Delivery" SMS/Email updates to retail buyers.

---

## 💆 Phase 4: Booking Service APIs
**Timeline:** Months 8-9 | **Target Version:** `v1.2.0`  
**Objective**: Expand the market to appointment-based businesses (Salons, Spas, Clinics).

> 💡 **Target Markets & Solutions:**
> *   **Single-Tenant**: Local Spas, Barbershops, Medical Clinics, Plumbers, Cleaners.
>   *   **The Problem:** Receptionists spend hours on the phone negotiating time slots, and customers frequently "no-show".
>   *   **Why They Buy:** To free up staff time and capture upfront booking deposits to secure revenue.
>   *   **How We Help:** Customers book open hourly slots themselves online, pay a non-refundable deposit, and receive automated WhatsApp reminders 24 hours prior.
> *   **Agency Model**: *"TaskRabbit or Zocdoc Clones"* connecting users with multiple independent freelancers or doctors.

*   **Service Entities (`catalog`)**: Add support for the `BOOKING` product type, defining service durations.
*   **Hourly Scheduling (`booking`)**: Adapt the booking engine to handle minute/hourly slot reservations rather than just nightly stays. 
*   **Service Staff Allocation (`staff`)**: Map available employees (e.g. Masseur, Hair Stylist) onto specific booking slots based on their shift schedules.
*   **Appointment Deposits (`payment`, `order`)**: Charge a non-refundable upfront deposit to secure the hourly slot.
*   **Client Histories (`customer`)**: Enhance profiles to store "past service notes" or client preferences across multiple visits.
*   **Automated Reminders (`notification`)**: Connect messaging APIs (WhatsApp/Telegram) to send 24-hour appointment reminders to reduce no-shows.
*   **Off-Peak Promos (`promotion`)**: Introduce time-of-day dynamic discounts (e.g., "-20% for 10am Tuesday appointments" to fill dead slots).
*   **Feedback Loop (`review`)**: Implement a system to automatically request and collect customer ratings after a verified service has been completed.

---

## 🧾 Phase 5: POS (Point of Sale) APIs
**Timeline:** Months 10-11 | **Target Version:** `v1.3.0`  
**Objective**: Provide an Omni-channel solution allowing existing E-commerce clients to ring up customers in physical brick-and-mortar stores.

> 💡 **Target Markets & Solutions:**
> *   **Single-Tenant**: Physical Clothing Boutiques, Coffee Shops, local Grocery Stores.
>   *   **The Problem:** The shop has an online store, but their physical cash register doesn't sync. If they sell a shirt in-store, they have to manually delete it from the website.
>   *   **Why They Buy:** To achieve true "Omni-channel" retail where 1 centralized dashboard controls both online and offline stock.
>   *   **How We Help:** The POS API allows cashiers to scan barcodes on iPads, instantly debiting the global cloud inventory while printing physical thermal receipts.
> *   **Agency Model**: *"Food Court"* models where a centralized till handles checkout for dozens of independent vendor stalls.

*   **Terminal Tracking (`auth`, `order`)**: Extend the APIs to identify exactly which physical cash register or employee iPad initiated a sale.
*   **Cashier Management (`staff`)**: Manage shift opening/closing, register assignments, and restrict discounts to managers.
*   **Instant Fulfillment (`inventory`)**: Bypass the standard warehouse "Pick/Pack/Ship" workflow to instantly relieve inventory for in-person handovers.
*   **Instore Loyalty (`customer`)**: Allow cashiers to look up a customer via phone number to apply accrued loyalty points in person.
*   **Physical Payments (`payment`)**: Interface with physical card readers or manual Cash Drawer entries.
*   **Thermal Receipts (`invoice`)**: Develop PDF templates optimized for physical receipt printers (tax details, return policies).
*   **Reconciliation (`report`)**: Build End-of-Day (Z-Out) reporting APIs to track cash drawer totals, discrepancies, and salesperson performance.

---

## 🍿 Phase 6: Streaming Media APIs (Movies & Dramas)
**Timeline:** Months 12-13 | **Target Version:** `v1.4.0`  
**Objective**: Build out the infrastructure to allow media groups to sell Netflix-style recurring subscriptions or Pay-Per-View access to video content (Dramas, Movies, Live Events).

> 💡 **Target Markets & Solutions:**
> *   **Single-Tenant**: Film Studios, News Outlets, Independent Production companies.
>   *   **The Problem:** Relying on YouTube ads is unpredictable and pays pennies per view.
>   *   **Why They Buy:** To sell premium video content directly to hardcore fans without middlemen.
>   *   **How We Help:** We provide secure video streaming (DRM) locked behind automated recurring subscription billing blocks.
> *   **Agency Model**: *"Patreon Clones"* connecting fans with multiple independent video creators.

*   **Media Access Plans (`catalog`)**: Add support for the `MEDIA_SUBSCRIPTION` product type, allowing merchants to lock specific video content behind a paywall (e.g., "$9.99/mo for Premium Drama" or "$2.00 to Rent").
*   **Recurring Billing (`payment`, `order`)**: Extend Stripe/Braintree webhooks to handle recurring tokenized subscription charges and one-off Pay-Per-View sales from the End-Customer.
*   **Secure Delivery & DRM (`asset`)**: Implement secure, expiring, signed URLs (HLS/DASH streaming formats) to prevent piracy or unauthorized downloading of the videos.
*   **Watch History (`customer`)**: Track an End-Customer's playback state (e.g., "Resume Episode 3 at 14:22").
*   **Viewership Dashboards (`report`)**: Provide media companies analytics on most-watched episodes and subscriber retention rates.
*   **Audience Reactions (`review`)**: Implement rating systems (e.g. Rotten Tomatoes style thumbs up/down, or 10-star scales) for media content.
*   **Release Schedules (`notification`)**: Send automated push notifications or emails when a "New Episode drops."
*   **Free Trials (`promotion`)**: Enable "First 7 Days Free" logic for monthly recurring video subscriptions.

---

## 🎓 Phase 7: Education, E-Learning & Mentorship APIs
**Timeline:** Months 14-15 | **Target Version:** `v1.5.0`  
**Objective**: Open the platform to digital creators, tutors, universities, and life coaches selling structured online courses, certifications, and personalized mentorship plans.

> 💡 **Target Markets & Solutions:**
> *   **Single-Tenant (Education)**: Universities, Private Tutors, Corporate Training departments.
>   *   **The Problem:** Sending Zoom links and Google Drive PDFs manually makes them look unprofessional and increases the risk of piracy.
>   *   **Why They Buy:** To host a professional, branded learning portal that automatically tracks student progress.
>   *   **How We Help:** We provide a complex course hierarchy (Modules/Lessons), drip-released content via email, and automated certificate generation upon completion.
> *   **Single-Tenant (Coaching & Mentorship)**: Life Coaches, Career Mentors, Fitness Trainers.
>   *   **The Problem:** Coaches discuss 'Life Plans' over Zoom, but clients forget their goals and fail to execute their daily to-do lists.
>   *   **Why They Buy:** To provide their clients with an interactive dashboard that automatically follows up with them.
>   *   **How We Help:** Coaches can assign a personalized 'Todo List' or 'Goal Plan'. The system uses the `notification` module to automatically check-in on the client (e.g., via WhatsApp) to ensure they are completing their tasks.
> *   **Agency Model**: *"Udemy or Coursera Clones"* connecting students with multiple independent instructors.

*   **Course Hierarchies (`catalog`)**: Add support for the `COURSE` product type. Unlike a simple movie, a course requires a complex hierarchy (Course -> Modules -> Lessons -> Quizzes).
*   **Course Materials (`asset`)**: Securely host PDF workbooks, slides, and accompanying lecture videos.
*   **Course Purchases (`payment`, `order`)**: Handle one-off course purchases or bundled pricing architectures.
*   **Goal Tracking & To-Do Lists (`customer`)**: Expand the student's profile to allow Mentors/Coaches to assign personalized "Life Plans" or daily Habit Trackers.
*   **Progress & Completion (`customer`)**: Track lesson-by-lesson completion or daily to-do list checkboxes mapped directly to the student's profile.
*   **Interactive Assessments (`review`)**: Repurpose the review system to handle grading, quiz submissions, and teacher feedback.
*   **Student-Teacher Support (`chat`)**: Facilitate direct 1-to-1 WebSockets Q&A messaging between students and professors.
*   **Drip Content (`notification`)**: Schedule automated emails to release new course modules on a time-delay (e.g., "Week 2 module unlocked!").
*   **Certificate Generation (`invoice`)**: Repurpose the PDF generator to issue customized "Certificates of Completion" when a student finishes the curriculum.
*   **Affiliate & Bundle Promos (`promotion`)**: Support "Buy Python 101, get Java 101 for half price" rule sets and instructor referral codes.

---

## 🌍 Phase 8: Multi-Vendor Marketplaces (Airbnb/Agoda Clones)
**Timeline:** Months 16-17 | **Target Version:** `v1.6.0`  
**Objective**: Expand the architecture from B2B2C to **B2B2B2C**, allowing "Agencies" to buy the SaaS, board multiple third-party "Hosts", and take a cut of the sales.

*   **Host Scoping (`staff`, `auth`)**: Add a strict `HOST` role to the RBAC engine. A host can only `INSERT` their own properties into the catalog and can only `SELECT` orders tied to their own listings, preventing them from viewing the parent Agency's total revenue.
*   **Multi-Vendor Carts (`order`)**: Enhance the shopping cart so if an End-Customer books two different houses from two different hosts, the single order splits into sub-orders for fulfillment tracking.
*   **Commission Logic (`setting`, `catalog`)**: Allow the Agency to define flat or percentage-based marketplace commission fees (e.g., "The Agency takes 15% of all bookings").
*   **Payment Splitting (`payment`)**: At checkout time, instruct the payment gateway to automatically route 15% of the transaction to the Agency's bank account, and 85% directly to the Host's connected bank account.
*   **Host Payout Dashboards (`report`, `invoice`)**: Provide individual dashboards for hosts to see their earnings, and auto-generate end-of-month payout statements for tax purposes.

---

## 🛵 Phase 9: Logistics & Delivery Marketplaces
**Timeline:** Months 18-19 | **Target Version:** `v1.7.0`  
**Objective**: Expand the Marketplace APIs to include "Last Mile" logistics, allowing Agencies to launch DoorDash or Nham24 clones that involve three parties: The Merchant, The Customer, and The Driver.

> 💡 **Target Markets & Solutions:**
> *   **Agency Model:** Food Delivery apps (FoodPanda clones), Grocery Delivery apps, or local courier networks.
>   *   **The Problem:** Merely selling items online isn't enough; restaurants need help physically moving the food. Managing hundreds of independent drivers is chaotic and logistically impossible without automation.
>   *   **Why They Buy:** To compete with mega-corporations like Grab by launching their own highly localized, perfectly optimized delivery fleet in a specific city.
>   *   **How We Help:** We provide live geospatial driver tracking, routing ETAs, and a complex 3-way commission split to ensure the Restaurant, the Driver, and the App Owner all get paid fairly.

*   **Driver Profiles (`staff`, `auth`)**: Add a `DRIVER` role. Drivers can only view `orders` that are actively assigned to them or marked as "Ready for Pickup".
*   **Geospatial Routing (`order`, `setting`)**: Integrate with Google Maps or Mapbox APIs to calculate distance-based delivery fees and estimate arrival times (ETAs).
*   **Order State Machine Expansion (`order`)**: Add intermediate logistical states: `COOKING` -> `DRIVER_ASSIGNED` -> `PICKED_UP` -> `ON_THE_WAY` -> `DELIVERED`.
*   **Live Driver Tracking (`chat`, `notification`)**: Use WebSockets to push the driver's GPS coordinates to the End-Customer's app in real-time.
*   **Three-Way Commission Split (`payment`)**: Expand the Phase 8 payment splitter to route revenue 3 ways: 1) The Food Cost to the Restaurant, 2) The Delivery Fee to the Driver, 3) The Commission to the Agency's bank account.

---

### Future Horizons (Phase 10+)
*   *Global Search API:* Elasticsearch integration for lightning-fast catalog querying across millions of SKUs.
*   *Headless Storefront Templates:* Open-sourcing Next.js and Vue storefront templates that come pre-wired to our `iecommerce` endpoints.
