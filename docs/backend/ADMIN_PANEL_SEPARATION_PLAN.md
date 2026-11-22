# Aurelia Jewels — Standalone Admin Panel & Security Separation Blueprint

## 1. Why Admin Must Be 100% Separate From the Customer App

In the current offline demo APK, `AdminDesignStudioScreen` lives inside the Android app so a stakeholder can test adding products and tweaking colors on a single phone without a server.

**In any real/hosted deployment, shipping Admin controls inside the Customer APK is a critical security risk:**
- Anyone can decompile an Android APK and discover hidden screens, routes, or API endpoints.
- If admin access relies on UI hiding or a client-side PIN, a modified client or direct HTTP call can still overwrite prices (e.g., changing a INR 1,28,000 gold bangle to INR 1) or inspect other customers' phone numbers and addresses.
- Store owners and staff prefer managing 100+ jewellery SKUs, uploading high-res studio photos, and printing invoices from a desktop/tablet browser — not a cramped phone screen.

**Core Design Mandate:**
> Customers install a **Customer-Only Android App**. Store owners/staff sign in to a **Standalone Web Admin Portal** at a separate subdomain (e.g., `https://admin.aureliajewels.in`), and **every admin action is enforced strictly on the server via Role-Based Access Control (RBAC)**.

---

## 2. Separation Architecture (3-Layer Defense)

```text
Layer 1: Separate Client Builds
├── Customer Android APK (Production Flavor / Flag: ENABLE_DEMO_STUDIO = false)
│   └── Zero admin screens, zero admin API keys, zero role-elevation fields
└── Standalone Web Admin Portal (React / Next.js on Vercel or Cloudflare Pages)
    └── Hosted at admin.<domain>.in with strict CORS allowlist + login wall

Layer 2: Server-Side Authentication & Role Enforcement (RBAC)
├── Customer JWT  -> role = "customer" (default on every signup; immutable by client)
└── Admin JWT     -> role = "staff" | "super_admin" (assigned ONLY via DB seed or Super Admin API)

Layer 3: Database & Audit Isolation
├── Row-level / middleware checks reject any customer JWT hitting /api/admin/* (HTTP 403)
└── Every admin write appends an immutable row to admin_audit_logs (actor, action, before/after JSON)
```

---

## 3. Preventing Customers From Ever Creating an Admin Account

| Attack Vector | Required Protection |
| :--- | :--- |
| **Self-signup role injection** (sending `{"role": "admin"}` in signup payload) | Ignore all role fields on public signup endpoints. Server hardcodes `role = 'customer'`. |
| **Direct database write** (in Firebase/Supabase client SDKs) | Lock `user_roles` table/collection so **zero clients can write to it** (`allow write: if false` in Firestore; no `INSERT/UPDATE` RLS policy in Supabase). Only server-side admin scripts / service role key can promote an account. |
| **Guessing Admin URL** (`/admin` or `admin.aureliajewels.in`) | Separate login screen requiring a verified `staff` or `super_admin` JWT claim; any `customer` login attempt immediately receives `403 Forbidden` and is signed out. |
| **Stolen Admin Password** | Enforce strong password policy + **TOTP 2FA / Email MFA** for `super_admin` accounts; short-lived access tokens (15-60 min) + revocable refresh sessions. |
| **Decompiled Demo Studio in Production APK** | Gate the existing `AdminDesignStudioScreen` behind `BuildConfig.DEMO_MODE` (true only for offline demo APKs; `false` for all production builds). |

---

## 4. Standalone Admin Web Portal — Core Modules

The standalone web admin portal (buildable in React + Vite/Next.js and hostable free on Cloudflare Pages or Vercel) needs 7 clean modules:

1. **Dashboard Overview**
   - Today's orders count, pending confirmation queue, total revenue (verified paid orders only), low-stock alerts (`stock_quantity <= 2`).
2. **Product & Media Manager**
   - Add / Edit / Archive jewellery SKUs across all 12 categories and 5 metals.
   - Multi-photo upload with automatic WebP/JPEG compression before uploading to Cloudflare R2 / Supabase Storage / Firebase Storage.
   - Toggle `is_featured`, `is_best_seller`, `is_new_arrival`, `stock_quantity`, `price_inr`, and `original_price_inr` (for automatic `SAVE X%` badges).
3. **Order Pipeline & Refund Queue**
   - Filterable buckets with live counts:
     - `Payment Pending` (`payment_status` in `unpaid`, `pending`, `failed` and not expired)
     - `Awaiting Confirmation` (`paid` + `placed`)
     - `In Fulfilment` (`confirmed`, `packed`, `shipped`, `out_for_delivery`)
     - `Delivered`
     - `Cancelled / Rejected & Refund Queue` (highlighting `refund_status = initiated`)
   - Enforced transition buttons only (staff cannot jump illegally from `cancelled` back to `delivered`).
4. **Offers, Festive Sale Banners & Coupons**
   - Manage Home screen Hero Banners (title, subtitle, image, CTA category, start/end schedule, sort order).
   - Manage Coupons (`AURELIA10`, `HERITAGE5`, seasonal codes) with percentage/flat discount, min order value, max cap, expiry date, and active toggle.
5. **Daily Gold & Silver Rates Manager**
   - Update 24K, 22K, 18K Gold and 925 Silver per-gram rates with automatic timestamping.
6. **Store Configuration & Branding**
   - Edit brand display name, WhatsApp concierge number, standard/express shipping fees, support phone/email, and legal policy text.
7. **Staff Accounts & Audit Log (Super Admin Only)**
   - Invite or revoke `staff` users.
   - Read-only `admin_audit_logs` feed showing every price change, order status change, and coupon edit with timestamp and actor email.

---

## 5. First Super Admin Bootstrap Procedure

To ensure zero open registration for admins:
1. Disable public admin registration completely.
2. Create the first `super_admin` account once via a private server CLI script or direct database seed during initial deployment (never via an unauthenticated HTTP route).
3. The `super_admin` logs into `admin.aureliajewels.in`, enables 2FA, and invites additional `staff` users via expiring invite links.
