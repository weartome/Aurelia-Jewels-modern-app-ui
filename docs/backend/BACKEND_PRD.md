# Aurelia Jewels — Backend Product Requirements Document (PRD)

- **Product**: Aurelia Jewels (Luxury Indian Fine Jewellery Mobile Commerce Platform)
- **Android Application ID**: `com.aistudio.aureliajewels.kxmprq`
- **Current Client Baseline**: Native Android (Kotlin, Jetpack Compose, Material 3, Room SQLite `aurelia_jewels_demo.db`)
- **Document Status**: Ready for Client Review & Phase 2 Kickoff (No backend code modified yet)

---

## 1. Executive Summary

Today, **Aurelia Jewels** runs as a full-featured, offline-first Android demo prototype backed by local Room SQLite storage (29 seeded Indian jewellery products, wishlist, shopping bag, 3-step guest checkout, demo payments, WhatsApp inquiry handoff, and an in-app Demo Design Studio).

When the client approves live launch, the backend must upgrade this prototype into a **secure, multi-device, production commerce platform** with:
1. A **zero-cost Free Testing Stage** for internal QA and client UAT before paying for hosting.
2. **Strict physical and logical separation** between the **Customer Android App** and a **Standalone Web Admin Panel** so no customer can ever view, create, or tamper with store administration controls.
3. **Server-side authority** over prices, stock, promo codes (`AURELIA10`, `HERITAGE5`), order totals, and payment verification.
4. **Database portability** so the client is not trapped on one cloud vendor if they later migrate to their own VPS or managed SQL host.

---

## 2. User Roles & Permissions Matrix

| Role | Access Surface | Allowed Actions | Strictly Forbidden |
| :--- | :--- | :--- | :--- |
| **Guest Shopper** | Android App (Unauthenticated) | Browse catalogue, filter/sort, view product specs, demo gold rates, jewellery guide, store info | Writing orders, syncing wishlist across devices, accessing any admin API |
| **Registered Customer** | Android App (Google Sign-In / Phone OTP / Email Auth) | Sync wishlist & cart across devices, manage own addresses, place orders, track own orders, cancel eligible orders | Reading/modifying other users' orders, editing products, self-assigning admin roles, setting `payment_status = paid` |
| **Store Staff** | Standalone Web Admin Portal (`admin.*`) | Add/edit products, upload product photos, update gold rates, advance order fulfilment status (`confirmed -> packed -> shipped -> delivered`) | Deleting financial audit logs, creating/removing `super_admin` accounts, exporting raw auth secrets |
| **Super Admin (Owner)** | Standalone Web Admin Portal (`admin.*`) | All Store Staff permissions + manage coupons, festive sale banners, store theme/WhatsApp settings, approve refunds, manage staff accounts | Bypassing immutable audit logs |

---

## 3. Core System Architecture

```text
┌────────────────────────────────────┐      ┌────────────────────────────────────┐
│  Aurelia Jewels Android App        │      │  Standalone Web Admin Portal       │
│  (Customer-Only Production APK)    │      │  (Separate URL: admin.domain.in)   │
│  • Guest Catalogue & Local Cache   │      │  • Separate Admin Login + MFA      │
│  • Customer Auth (Google / OTP)    │      │  • Catalogue & Media Manager       │
│  • Wishlist, Cart, Checkout        │      │  • Order Pipeline & Refund Queue   │
│  • UPI / Card Checkout SDK         │      │  • Coupons, Offers & Gold Rates    │
└─────────────────┬──────────────────┘      └─────────────────┬──────────────────┘
                  │ HTTPS + Customer JWT                      │ HTTPS + Admin JWT
                  └───────────────────────┬───────────────────┘
                                          ▼
                   ┌──────────────────────────────────────────┐
                   │   Backend API & Security Layer           │
                   │   (Supabase Edge / Vercel API /          │
                   │    Firebase Functions + Rules)           │
                   │   • Server-Side RBAC Middleware          │
                   │   • Authoritative INR Pricing & Coupons  │
                   │   • Idempotent Order Creation            │
                   │   • Payment Webhook Signature Verifier   │
                   └──────┬───────────────┬──────────────┬────┘
                          ▼               ▼              ▼
             ┌─────────────────┐ ┌────────────────┐ ┌──────────────────────┐
             │ Cloud Database  │ │ Media CDN      │ │ External Services    │
             │ (Postgres/MySQL │ │ (Cloudflare R2 │ │ • Razorpay / Cashfree│
             │  or Firestore)  │ │  or Supabase/  │ │ • WhatsApp Cloud API │
             │                 │ │  Firebase)     │ │ • Gemini AI (Opt.)   │
             └─────────────────┘ └────────────────┘ └──────────────────────┘
```

---

## 4. Functional Requirements by Domain

### 4.1 Catalogue, Categories & Merchandising
- Serve active products with pagination, category/metal/purity/occasion/price filters, and sort order (`recommended`, `price_asc`, `price_desc`, `newest`).
- Support dynamic **Best Offer / Festive Sale** banners and discount pricing (`price_inr`, `original_price_inr`) managed solely from the Admin Portal.
- Android app caches the catalogue locally in **Room SQLite** (stale-while-revalidate) so the app opens instantly even on slow 4G connections.

### 4.2 Customer Authentication & Profile
- **Guest Mode**: Customers can browse all public products without signing in.
- **Account Sync**: Signing in (Google Sign-In via Android Credential Manager, or Email/Phone OTP) merges local Room wishlist/bag items into the user's cloud profile.
- **Address Book**: Validated Indian delivery addresses (10-digit Indian mobile starting with 6-9, 6-digit PIN code).

### 4.3 Cart, Promo Codes & Authoritative Pricing
- Never trust client-submitted prices or discount amounts.
- When creating an order, the backend fetches authoritative unit prices from `products`, validates coupon rules (`AURELIA10`: 10% off on orders >= INR 10,000; `HERITAGE5`: 5% off; plus dynamic admin-created coupons with expiry & usage limits), calculates shipping, and freezes an immutable line-item snapshot.

### 4.4 Order, Payment & Refund Lifecycle (State Separation)
Orders must track **three independent status fields** rather than one overloaded column:
- `order_status`: `draft`, `placed`, `confirmed`, `packed`, `shipped`, `out_for_delivery`, `delivered`, `cancelled`, `rejected`, `expired`
- `payment_status`: `unpaid`, `pending`, `paid`, `failed`, `refund_pending`, `refunded`, `partially_refunded`
- `refund_status`: `not_required`, `initiated`, `processing`, `credited`, `failed`

Key rules:
- Every order creation requires a client-generated `idempotency_key` (UUID) so double-taps or network retries never create duplicate orders.
- Only verified payment gateway webhooks (or logged Admin COD settlement) may transition `payment_status` to `paid`.
- Cancelling or rejecting a `paid` order automatically creates a `refund_status = initiated` entry in the Admin Refund Queue.

### 4.5 Daily Gold & Silver Rates
- Admin enters or schedules daily 22K, 24K, 18K Gold, and 925 Silver reference rates.
- Stored with `effective_at` timestamps so the Android app displays a verified "Last updated" timestamp and optional 7-day trend chart.

### 4.6 Notifications & WhatsApp Integration
- **Testing / Free Phase**: Continue using deep-link WhatsApp intents (`https://wa.me/...`) pre-filled with the real backend Order ID and summary (zero messaging cost).
- **Production Phase (Optional)**: Integrate **Meta WhatsApp Business Cloud API** or **Firebase Cloud Messaging (FCM)** push notifications for automated order confirmation and shipment updates.

### 4.7 Bespoke AI Concept Studio (Optional Phase 2 Add-on)
- Currently routed through `AiImageRepository` (`PlaceholderAiImageRepository`).
- When approved, enable **Firebase AI Logic** (`gemini-3-pro-image-preview` with App Check) or a server-proxied Gemini image endpoint with per-user daily rate limits (e.g., max 5 generations/day per authenticated user) to cap AI spend.

---

## 5. Phased Delivery Roadmap

| Phase | Goal | Infrastructure | Target Duration |
| :--- | :--- | :--- | :--- |
| **Phase 0 (Current)** | Polished offline Android Demo APK + GitHub Actions CI + Backend PRD | Local Room SQLite (INR 0) | Completed |
| **Phase 1 — Free Cloud Testing** | Live product sync, customer login, cloud orders, separate Admin Web Portal | Free tiers (Supabase + Vercel, or Firebase Spark) | 1-2 Weeks |
| **Phase 2 — Payments & Hardening** | Razorpay/Cashfree UPI & Cards, webhook verification, stock locking, FCM push | Paid Starter Tier (no sleep/pause) | 1-2 Weeks |
| **Phase 3 — Scale & Analytics** | Automated backups, WhatsApp Cloud API, GST invoice PDFs, Live AI Bespoke Studio | Paid Production / VPS Tier | Ongoing |

---

## 6. Non-Functional Requirements

- **Performance**: Catalogue API p95 response under 300ms within India (prefer **Mumbai `ap-south-1`** or **Singapore `ap-southeast-1`** regions).
- **Security**: HTTPS/TLS everywhere, server-side RBAC on every mutation, bcrypt/Argon2 or managed Auth, parameterized SQL queries, strict CORS for Admin Portal, rate limiting on auth/checkout/AI routes.
- **Availability & Backups**: Automated daily database snapshots in production; graceful offline cache fallback in the Android app.
- **Compliance (India)**: Accurate INR GST breakdown support when enabled by client, clear Privacy Policy / Refund Policy links, and zero storage of raw card numbers (handled strictly by PCI-DSS compliant Razorpay/Cashfree SDKs).
