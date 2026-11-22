# Aurelia Jewels — Database Schema & REST / RPC API Specification

This specification maps the Android app's existing 8 local **Room SQLite** entities (`aurelia_jewels_demo.db`) directly into a **portable cloud database schema** (compatible with PostgreSQL, MySQL, and adaptable to Firestore) alongside full Customer, Admin, and Payment Webhook API contracts.

---

## 1. Migration Map: Existing Room SQLite -> Cloud Database

| Existing Android Room Table | Cloud Target | Sync Strategy | Notes |
| :--- | :--- | :--- | :--- |
| `products` (`Product`) | `products` + `product_variants` | Server-authoritative -> Room local cache | Add `is_active`, `deleted_at` (soft delete so old orders stay valid) |
| `wishlist` (`WishlistEntity`) | `wishlists` | Two-way sync per authenticated `user_id` | Guest wishlist stays local in Room until login merge |
| `cart_items` (`CartItemEntity`) | `cart_items` | Two-way sync per `user_id` | Backend always recomputes unit price from `products` |
| `addresses` (`AddressEntity`) | `addresses` | Owned by `user_id` | Server enforces 10-digit Indian phone & 6-digit PIN |
| `orders` (`OrderEntity`) | `orders` + `order_items` | Server-authoritative (immutable snapshot) | Split single status into `order_status`, `payment_status`, `refund_status` |
| `notifications` (`NotificationEntity`) | `notifications` | Server-pushed + FCM | Per-user order updates + broadcast festive offers |
| `store_config` (`StoreConfigEntity`) | `store_config` (public subset) | Read-only for customers; Admin-only write | Never expose private keys or internal flags publicly |
| `recent_searches` (`RecentSearchEntity`) | *Remains Device-Local* | Local Room SQLite only | No need to burn cloud DB writes on search history |

---

## 2. Portable Relational Schema (PostgreSQL / MySQL Compatible)

> *Portability Rule: Uses standard SQL types (`VARCHAR`, `BIGINT`, `BOOLEAN`, `TEXT`, `TIMESTAMP`), explicit `PRIMARY KEY` / `UNIQUE` / `FOREIGN KEY` constraints, and zero vendor-locked extensions so it deploys cleanly on Supabase Postgres, Neon, AWS RDS, or Hostinger MySQL.*

### 2.1 Auth & RBAC Tables (Backend-Only)
- **`users`**: `id (PK)`, `email (UNIQUE)`, `phone`, `full_name`, `auth_provider` (`google` / `email` / `phone`), `created_at`, `updated_at`
- **`user_roles`**: `user_id (PK, FK -> users.id)`, `role` (`customer`, `staff`, `super_admin`), `assigned_by`, `updated_at`
  - *Constraint*: Never writable by customer endpoints.

### 2.2 Catalogue & Merchandising Tables
- **`products`**: `id (PK)`, `sku (UNIQUE)`, `name`, `category`, `metal`, `purity`, `weight_grams`, `price_inr (BIGINT)`, `original_price_inr (BIGINT NULL)`, `stock_quantity (INT)`, `description (TEXT)`, `occasion`, `collection`, `image_urls_json (TEXT)`, `variants_json (TEXT)`, `sizes_json (TEXT)`, `is_featured`, `is_best_seller`, `is_new_arrival`, `is_active`, `featured_order`, `created_at`, `updated_at`
- **`hero_banners`**: `id (PK)`, `title`, `subtitle`, `cta_label`, `target_category`, `image_url`, `sort_order`, `is_active`, `starts_at`, `ends_at`, `created_at`
- **`gold_rates`**: `id (PK)`, `metal_key` (`GOLD_24K`, `GOLD_22K`, `GOLD_18K`, `SILVER_925`), `rate_per_gram_inr (BIGINT)`, `effective_at`, `updated_by (FK -> users.id)`
- **`coupons`**: `code (PK)`, `discount_type` (`percent` / `flat`), `discount_value (INT)`, `min_bag_inr (BIGINT)`, `max_discount_inr (BIGINT NULL)`, `usage_limit (INT NULL)`, `used_count (INT)`, `is_active`, `starts_at`, `ends_at`
  - *Initial Seed*: `AURELIA10` (10% off, min `10000`, active), `HERITAGE5` (5% off, min `0`, active).

### 2.3 Customer Commerce Tables
- **`wishlists`**: `user_id (FK)`, `product_id (FK)`, `created_at`, `PRIMARY KEY (user_id, product_id)`
- **`cart_items`**: `id (PK)`, `user_id (FK)`, `product_id (FK)`, `selected_variant`, `selected_size`, `quantity`, `updated_at`, `UNIQUE (user_id, product_id, selected_variant, selected_size)`
- **`addresses`**: `id (PK)`, `user_id (FK)`, `full_name`, `phone`, `line1`, `line2`, `city`, `state`, `pin_code`, `is_default`, `created_at`

### 2.4 Orders, Payments & Audit Tables
- **`orders`**: `order_id (PK)`, `user_id (FK)`, `idempotency_key (UNIQUE)`, `order_status`, `payment_status`, `refund_status`, `payment_method` (`UPI`, `CARD`, `NETBANKING`, `COD`), `delivery_method`, `subtotal_inr (BIGINT)`, `discount_inr (BIGINT)`, `shipping_inr (BIGINT)`, `grand_total_inr (BIGINT)`, `applied_coupon_code`, `shipping_address_snapshot_json (TEXT)`, `created_at`, `updated_at`
- **`order_items`**: `id (PK)`, `order_id (FK -> orders.order_id)`, `product_id`, `product_name_snapshot`, `image_url_snapshot`, `selected_variant`, `selected_size`, `unit_price_inr (BIGINT)`, `quantity (INT)`, `line_total_inr (BIGINT)`
- **`payment_transactions`**: `id (PK)`, `order_id (FK)`, `gateway` (`razorpay` / `cashfree` / `cod`), `gateway_order_id (UNIQUE)`, `gateway_payment_id`, `amount_inr (BIGINT)`, `status`, `webhook_verified (BOOLEAN)`, `raw_event_id (UNIQUE)`, `created_at`, `updated_at`
- **`order_status_history`**: `id (PK)`, `order_id (FK)`, `from_status`, `to_status`, `changed_by (FK -> users.id)`, `reason`, `created_at`
- **`admin_audit_logs`**: `id (PK)`, `actor_user_id (FK)`, `entity_type`, `entity_id`, `action`, `before_json (TEXT)`, `after_json (TEXT)`, `ip_address`, `created_at`

*(Firestore Adaptation Note: If Firebase Firestore is chosen instead of SQL, map top-level collections to `products`, `hero_banners`, `coupons`, `gold_rates`, `store_config`, `orders`, `admin_audit_logs`, and per-user subcollections `users/{uid}/wishlist`, `users/{uid}/cart`, `users/{uid}/addresses` gated strictly by `request.auth.uid == uid` and Admin Custom Claims.)*

---

## 3. Order, Payment & Refund State Machine

All transitions must be validated in a single backend service function:

```text
ORDER STATUS FLOW:
  placed ──► confirmed ──► packed ──► shipped ──► out_for_delivery ──► delivered
    │            │
    ├──► rejected (Admin only; triggers refund_status = initiated if paid)
    ├──► cancelled (Customer before packed, or Admin; triggers refund if paid)
    └──► expired (Auto-expired unpaid checkout after 30 mins)

PAYMENT STATUS FLOW:
  unpaid ──► pending ──► paid ──► refund_pending ──► refunded / partially_refunded
                │
                └──► failed (Customer may retry payment while order is not expired)
```

---

## 4. API Endpoint Specification

Base URL convention: `https://api.aureliajewels.in/v1` (or Supabase Edge Functions / Vercel `/api/v1`).

### 4.1 Public & Customer Endpoints (Android App)

| Method & Path | Auth | Purpose | Key Rules |
| :--- | :--- | :--- | :--- |
| `GET /catalog/products` | Public | List products with filters (`category`, `metal`, `minPrice`, `maxPrice`, `saleOnly`, `sort`, `page`) | Returns only `is_active = true`; cached 60s at CDN |
| `GET /catalog/products/:id` | Public | Single product detail + variants + live stock | Returns `404` if archived |
| `GET /store/bootstrap` | Public | Active hero banners, public store config, latest gold rates, active promo headlines | Single fast call on app launch |
| `POST /auth/session` | Public | Exchange Google ID Token / OTP for backend session JWT | Always assigns `role = customer` on first signup |
| `GET /me/wishlist` & `POST /me/wishlist/toggle` | Customer JWT | Fetch or toggle saved product IDs | Scoped strictly to `jwt.sub` |
| `GET /me/cart` / `PUT /me/cart/items` / `DELETE /me/cart/items/:id` | Customer JWT | Sync shopping bag | Server returns authoritative recalculated totals |
| `POST /coupons/validate` | Customer JWT | Validate code (`AURELIA10`, `HERITAGE5`) against live cart subtotal | Checks min bag value, expiry, and usage limits |
| `GET /me/addresses` / `POST /me/addresses` / `DELETE /me/addresses/:id` | Customer JWT | Manage delivery addresses | Server validates Indian mobile & 6-digit PIN |
| `POST /orders` | Customer JWT | Create order from verified cart + `Idempotency-Key` header | Recomputes all prices server-side; reserves stock; returns `order_id` + gateway payment order token if online payment |
| `GET /me/orders` & `GET /me/orders/:orderId` | Customer JWT | List own orders and tracking timeline | Enforces `order.user_id == jwt.sub` |
| `POST /me/orders/:orderId/cancel` | Customer JWT | Cancel order before `packed` stage | Sets `refund_status = initiated` if already `paid` |

### 4.2 Payment Webhook Endpoints (Server-to-Server Only)

| Method & Path | Auth | Purpose |
| :--- | :--- | :--- |
| `POST /webhooks/razorpay` (or `/webhooks/cashfree`) | HMAC-SHA256 Signature Header | Verifies raw body signature using gateway webhook secret, deduplicates via `raw_event_id`, marks `payment_status = paid` or `failed`, and writes `order_status_history`. Never accepts client browser/app calls. |

### 4.3 Standalone Admin Portal Endpoints (Admin JWT Required)

Every route under `/v1/admin/*` runs strict middleware checking `user_roles.role IN ('staff', 'super_admin')` and returns `403 Forbidden` for any customer token.

| Method & Path | Minimum Role | Purpose |
| :--- | :--- | :--- |
| `GET /admin/dashboard/metrics` | `staff` | Order bucket counts, verified revenue, low-stock items |
| `POST /admin/products` / `PATCH /admin/products/:id` / `DELETE /admin/products/:id` | `staff` | Create, update, or soft-archive products (logged to `admin_audit_logs`) |
| `POST /admin/media/presign` | `staff` | Issue short-lived signed upload URL (MIME & max-size enforced, e.g., JPG/WebP <= 5 MB) |
| `GET /admin/orders` & `PATCH /admin/orders/:orderId/status` | `staff` | View bucketed orders and apply legal status transitions with required reason note |
| `POST /admin/orders/:orderId/refund` | `super_admin` | Mark refund `processing` / `credited` or trigger gateway refund |
| `PUT /admin/gold-rates` | `staff` | Publish updated 24K / 22K / 18K / Silver rates |
| `POST /admin/coupons` & `PATCH /admin/coupons/:code` | `super_admin` | Create/toggle promo codes and festive offers |
| `PUT /admin/hero-banners` & `PATCH /admin/store-config` | `super_admin` | Manage storefront banners, shipping fees, and WhatsApp concierge number |
| `GET /admin/audit-logs` & `POST /admin/staff/invite` | `super_admin` | Inspect immutable audit trail and manage staff access |

---

## 5. Standard Error Response Contract

All endpoints return a consistent JSON structure so the Android app can map failures cleanly to UI snackbars and retry states:

```json
{
  "error": {
    "code": "COUPON_MIN_AMOUNT_NOT_MET",
    "message": "Minimum bag value of INR 10,000 is required for AURELIA10.",
    "field": "couponCode",
    "requestId": "req_01J9X..."
  }
}
```

---

## 6. Production Go-Live Security Checklist

- [ ] Customer Android production build compiles with `DEMO_STUDIO_ENABLED = false` (no in-app Admin screen).
- [ ] All secrets (`DATABASE_URL`, `JWT_SECRET`, `RAZORPAY_WEBHOOK_SECRET`, `R2_SECRET_KEY`) stored strictly in hosting environment variables (never in Git or Android APK).
- [ ] HTTPS enforced with HSTS on `api.*` and `admin.*`; strict CORS allowing only `https://admin.aureliajewels.in` on admin routes.
- [ ] Rate limiting enabled on `/auth/*`, `/orders`, `/coupons/validate`, and `/ai/*`.
- [ ] Automated daily database backups tested with at least one restore drill before accepting real jewellery payments.
