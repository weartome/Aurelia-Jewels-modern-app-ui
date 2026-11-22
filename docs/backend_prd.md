# Aurelia Jewels — Backend Architecture & Product Requirements Document (PRD)

- **Application**: Aurelia Jewels (Luxury Indian Fine Jewellery Commerce Platform)
- **Android Application ID**: `com.aistudio.aureliajewels.kxmprq`
- **Current Prototype Baseline**: Native Android (Kotlin, Jetpack Compose, Material 3, Room SQLite `aurelia_jewels_demo.db`)
- **Purpose**: Architecture blueprint, Customer-vs-Admin separation specification, database schema, and API contracts for future backend implementation (no live backend code is modified at this stage).

---

## 1. Product Goals & Scope

The current Android app operates fully offline with local Room SQLite persistence for demo walkthroughs. The production backend must transform Aurelia Jewels into a secure, scalable cloud commerce platform while fulfilling five core requirements:

1. **Strict Separation of Customer App and Admin Panel**: Customers must never have access to store administration screens, routes, or write permissions.
2. **Server-Side Commerce Authority**: All product prices, promo discounts (`AURELIA10`, `HERITAGE5`), shipping charges, inventory checks, and payment statuses are calculated and verified strictly on the server.
3. **Separated Order, Payment, and Refund State Model**: Prevent financial and fulfilment bugs by tracking order fulfilment, payment settlement, and refund workflows independently.
4. **Offline-Resilient Android Sync**: Keep Room SQLite on Android as a fast local cache while syncing authoritative data over HTTPS.
5. **Database Portability**: Keep new schemas compatible with standard SQL (PostgreSQL / MySQL) so the store can start on a free tier (Supabase/Firebase) and move to Hostinger VPS or managed cloud without a rewrite.

---

## 2. System Architecture & Customer vs. Admin Separation

### 2.1 High-Level Separation Diagram

```text
┌─────────────────────────────────────┐      ┌─────────────────────────────────────┐
│  CUSTOMER ANDROID APP (Play Store)  │      │  STANDALONE WEB ADMIN PORTAL        │
│  Build Flag: DEMO_STUDIO = false    │      │  Hosted at: admin.aureliajewels.in  │
│  • Guest Browse + Local Room Cache  │      │  • Dedicated Admin Login + 2FA      │
│  • Google Sign-In / Email Auth      │      │  • Jewellery SKU & Photo Manager    │
│  • Wishlist, Bag, 3-Step Checkout   │      │  • Order Buckets & Refund Queue     │
│  • Razorpay / Cashfree UPI Checkout │      │  • Offer Banners, Coupons & Rates   │
└──────────────────┬──────────────────┘      └──────────────────┬──────────────────┘
                   │ HTTPS (Customer JWT)                       │ HTTPS (Admin JWT)
                   │ Allowed: /v1/catalog, /v1/me, /v1/orders   │ Allowed: /v1/admin/*
                   └──────────────────────┬─────────────────────┘
                                          ▼
                   ┌────────────────────────────────────────────┐
                   │   BACKEND API & SECURITY BOUNDARY          │
                   │   • Server-Side RBAC (customer/staff/admin)│
                   │   • Authoritative INR Pricing & Coupons    │
                   │   • Idempotent Order Creation              │
                   │   • HMAC Payment Webhook Verification      │
                   │   • Immutable Admin Audit Logger           │
                   └───────┬───────────────┬───────────────┬────┘
                           ▼               ▼               ▼
              ┌──────────────────┐ ┌──────────────┐ ┌─────────────────────────┐
              │ Cloud Database   │ │ Media CDN    │ │ External Integrations   │
              │ (Postgres/MySQL  │ │ (Cloudflare  │ │ • Razorpay / Cashfree   │
              │  or Firestore)   │ │  R2 / S3)    │ │ • WhatsApp Cloud / FCM  │
              └──────────────────┘ └──────────────┘ │ • Gemini AI (Optional)  │
                                                    └─────────────────────────┘
```

### 2.2 How Customers Are Strictly Prevented From Accessing Admin

| Security Layer | Enforcement Rule |
| :--- | :--- |
| **Separate Application Surface** | The Admin Panel is a separate web application (`admin.aureliajewels.in`), completely detached from the customer Android APK. |
| **Production APK Gating** | The existing in-app `AdminDesignStudioScreen` is gated behind a build flag (`DEMO_STUDIO_ENABLED = false` in production builds) so no admin UI ships to Play Store customers. |
| **Server-Side RBAC Only** | Public signup/login endpoints **always** assign `role = 'customer'` and ignore any role field sent by a client. |
| **Locked Role Storage** | The `user_roles` table/collection denies all client writes. The first `super_admin` is seeded once via a private server script; additional `staff` users can only be invited by a `super_admin`. |
| **Strict Route Guarding** | Every `/v1/admin/*` endpoint verifies `role IN ('staff', 'super_admin')` on the server and returns `403 Forbidden` for any customer token. |
| **Audit Trail** | Every product price edit, stock change, coupon update, and order status change appends an immutable row to `admin_audit_logs`. |

---

## 3. Proposed Database Schema (Mapped From Existing Room Entities)

### 3.1 Room SQLite -> Cloud Mapping

| Current Local Room Table | Cloud Table | Ownership & Write Access |
| :--- | :--- | :--- |
| `products` (`Product`) | `products` | Public read (`is_active = true`); Admin (`staff` / `super_admin`) write only |
| `wishlist` (`WishlistEntity`) | `wishlists` | Authenticated customer (`user_id == jwt.sub`) read/write only |
| `cart_items` (`CartItemEntity`) | `cart_items` | Authenticated customer (`user_id == jwt.sub`) read/write only |
| `addresses` (`AddressEntity`) | `addresses` | Authenticated customer (`user_id == jwt.sub`) read/write only |
| `orders` (`OrderEntity`) | `orders` + `order_items` | Customer creates via server checkout & reads own orders; Admin advances status |
| `notifications` (`NotificationEntity`) | `notifications` | Customer reads/marks own read; Backend/Admin creates |
| `store_config` (`StoreConfigEntity`) | `store_config` | Public subset readable by all; `super_admin` write only |
| `recent_searches` (`RecentSearchEntity`) | *Stays Local Only* | Remains in on-device Room SQLite (zero cloud cost) |

### 3.2 Core Portable SQL Tables (PostgreSQL / MySQL Compatible)

1. **`users`**: `id (PK)`, `email (UNIQUE)`, `phone`, `full_name`, `auth_provider`, `created_at`, `updated_at`
2. **`user_roles`**: `user_id (PK, FK -> users.id)`, `role` (`customer`, `staff`, `super_admin`), `assigned_by`, `updated_at`
3. **`products`**: `id (PK)`, `sku (UNIQUE)`, `name`, `category`, `metal`, `purity`, `weight_grams`, `price_inr (BIGINT)`, `original_price_inr (BIGINT NULL)`, `stock_quantity (INT)`, `description (TEXT)`, `occasion`, `collection`, `image_urls_json (TEXT)`, `variants_json (TEXT)`, `sizes_json (TEXT)`, `is_featured`, `is_best_seller`, `is_new_arrival`, `is_active`, `featured_order`, `created_at`, `updated_at`
4. **`hero_banners`**: `id (PK)`, `title`, `subtitle`, `cta_label`, `target_category`, `image_url`, `sort_order`, `is_active`, `starts_at`, `ends_at`, `created_at`
5. **`coupons`**: `code (PK)`, `discount_type` (`percent` / `flat`), `discount_value`, `min_bag_inr (BIGINT)`, `max_discount_inr (BIGINT NULL)`, `usage_limit`, `used_count`, `is_active`, `starts_at`, `ends_at`
6. **`gold_rates`**: `id (PK)`, `metal_key` (`GOLD_24K`, `GOLD_22K`, `GOLD_18K`, `SILVER_925`), `rate_per_gram_inr (BIGINT)`, `effective_at`, `updated_by (FK -> users.id)`
7. **`wishlists`**: `user_id (FK)`, `product_id (FK)`, `created_at`, `PRIMARY KEY (user_id, product_id)`
8. **`cart_items`**: `id (PK)`, `user_id (FK)`, `product_id (FK)`, `selected_variant`, `selected_size`, `quantity`, `updated_at`
9. **`addresses`**: `id (PK)`, `user_id (FK)`, `full_name`, `phone`, `line1`, `line2`, `city`, `state`, `pin_code`, `is_default`, `created_at`
10. **`orders`**: `order_id (PK)`, `user_id (FK)`, `idempotency_key (UNIQUE)`, `order_status`, `payment_status`, `refund_status`, `payment_method`, `delivery_method`, `subtotal_inr (BIGINT)`, `discount_inr (BIGINT)`, `shipping_inr (BIGINT)`, `grand_total_inr (BIGINT)`, `applied_coupon_code`, `shipping_address_snapshot_json (TEXT)`, `created_at`, `updated_at`
11. **`order_items`**: `id (PK)`, `order_id (FK)`, `product_id`, `product_name_snapshot`, `image_url_snapshot`, `selected_variant`, `selected_size`, `unit_price_inr (BIGINT)`, `quantity`, `line_total_inr (BIGINT)`
12. **`payment_transactions`**: `id (PK)`, `order_id (FK)`, `gateway`, `gateway_order_id (UNIQUE)`, `gateway_payment_id`, `amount_inr (BIGINT)`, `status`, `webhook_verified`, `raw_event_id (UNIQUE)`, `created_at`
13. **`order_status_history`**: `id (PK)`, `order_id (FK)`, `from_status`, `to_status`, `changed_by (FK)`, `reason`, `created_at`
14. **`admin_audit_logs`**: `id (PK)`, `actor_user_id (FK)`, `entity_type`, `entity_id`, `action`, `before_json (TEXT)`, `after_json (TEXT)`, `ip_address`, `created_at`

---

## 4. Order, Payment & Refund State Model

Orders strictly separate fulfilment, payment, and refund statuses:

- **`order_status`**: `placed` -> `confirmed` -> `packed` -> `shipped` -> `out_for_delivery` -> `delivered` (or `cancelled`, `rejected`, `expired`)
- **`payment_status`**: `unpaid` -> `pending` -> `paid` (or `failed`; after `paid`: `refund_pending` -> `refunded` / `partially_refunded`)
- **`refund_status`**: `not_required` -> `initiated` -> `processing` -> `credited` / `failed`

Key safeguards:
- A failed payment attempt does not close the order immediately (customer can retry before expiry).
- Client apps can **never** set `payment_status = paid`; only signed payment gateway webhooks or logged Admin COD confirmations can.
- Rejecting or cancelling a `paid` order automatically sets `refund_status = initiated` and surfaces it in the Admin Refund Queue.

---

## 5. API Contracts (`https://api.aureliajewels.in/v1`)

### 5.1 Public & Customer App Endpoints

| Method & Endpoint | Auth | Request / Query Summary | Response & Server Rules |
| :--- | :--- | :--- | :--- |
| `GET /store/bootstrap` | Public | None | Active hero banners, public store config, latest gold rates, active offer codes |
| `GET /catalog/products` | Public | `category`, `metal`, `minPrice`, `maxPrice`, `saleOnly`, `sort`, `page` | Paginated active products (`is_active = true`) |
| `GET /catalog/products/:id` | Public | Path `id` | Full product details, variants, sizes, real-time stock |
| `POST /auth/session` | Public | Google ID token or OTP payload | Returns customer JWT; always enforces `role = customer` on signup |
| `GET /me/wishlist` / `POST /me/wishlist/toggle` | Customer JWT | `{ "productId": "..." }` | Syncs wishlist scoped strictly to `jwt.sub` |
| `GET /me/cart` / `PUT /me/cart/items` | Customer JWT | `{ "productId", "variant", "size", "quantity" }` | Server recalculates INR subtotal using authoritative `products.price_inr` |
| `POST /coupons/validate` | Customer JWT | `{ "code": "AURELIA10" }` | Validates min bag value, dates, and usage cap against live cart |
| `GET /me/addresses` / `POST /me/addresses` | Customer JWT | Indian address fields | Validates 10-digit mobile (`^[6-9][0-9]{9}$`) and 6-digit PIN (`^[1-9][0-9]{5}$`) |
| `POST /orders` | Customer JWT + `Idempotency-Key` | `{ "addressId", "deliveryMethod", "paymentMethod", "couponCode" }` | Deduplicates retries, verifies stock, snapshots prices, creates order + gateway token |
| `GET /me/orders` / `GET /me/orders/:orderId` | Customer JWT | Path `orderId` | Returns customer's own orders + `order_status_history` timeline |
| `POST /me/orders/:orderId/cancel` | Customer JWT | `{ "reason": "..." }` | Allowed before `packed`; initiates refund queue entry if paid |

### 5.2 Payment Gateway Webhook Endpoint (Server-to-Server)

| Method & Endpoint | Auth | Behavior |
| :--- | :--- | :--- |
| `POST /webhooks/payments/razorpay` | `X-Razorpay-Signature` (HMAC-SHA256) | Verifies raw payload signature, deduplicates via `raw_event_id`, updates `payment_transactions` and `orders.payment_status`, logs status history. |

### 5.3 Standalone Admin Portal Endpoints (`/v1/admin/*`)

| Method & Endpoint | Required Role | Behavior |
| :--- | :--- | :--- |
| `GET /admin/dashboard` | `staff` | Order bucket counts (`Payment Pending`, `Awaiting Confirmation`, `Active`, `Refund Queue`), verified paid revenue, low-stock list |
| `POST /admin/products` / `PATCH /admin/products/:id` | `staff` | Create/update product specs, prices, discounts, stock; writes `admin_audit_logs` |
| `POST /admin/media/presign` | `staff` | Issues short-lived signed upload URL to R2/S3 (JPEG/WebP only, <= 5 MB) |
| `GET /admin/orders` / `PATCH /admin/orders/:orderId/status` | `staff` | Lists bucketed orders; validates legal status transitions and writes `order_status_history` |
| `POST /admin/orders/:orderId/refund` | `super_admin` | Triggers/records refund state progression |
| `PUT /admin/gold-rates` | `staff` | Publishes new 24K / 22K / 18K / Silver rates with `effective_at` |
| `POST /admin/coupons` / `PUT /admin/hero-banners` / `PATCH /admin/store-config` | `super_admin` | Manages promo codes, seasonal banners, shipping rates, and WhatsApp concierge number |
| `GET /admin/audit-logs` / `POST /admin/staff/invite` | `super_admin` | Reviews immutable admin audit trail and manages staff access |

### 5.4 Standard Error Payload

```json
{
  "error": {
    "code": "ORDER_STOCK_UNAVAILABLE",
    "message": "Selected bangle size is currently out of stock.",
    "field": "size",
    "requestId": "req_01J9Y..."
  }
}
```

---

## 6. Future Implementation Milestones

1. **Milestone 1 (Free Testing Backend)**: Stand up Supabase Free (Postgres + Auth + Storage) + Cloudflare Pages Admin Portal; connect Android app via Retrofit/Ktor with Room offline cache.
2. **Milestone 2 (Admin Lockout & QA)**: Set `DEMO_STUDIO_ENABLED = false` in production Android builds; test RBAC (`403` on customer tokens hitting `/v1/admin/*`), idempotent double-submit orders, and coupon validation.
3. **Milestone 3 (Production Hosting & Payments)**: Move to Hostinger KVM VPS or Supabase Pro (no auto-pause, daily backups), enable Razorpay/Cashfree HMAC webhooks, and optionally activate Firebase AI Logic for Bespoke Studio with per-user daily caps.
