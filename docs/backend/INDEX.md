# Aurelia Jewels — Backend Architecture & PRD Documentation Pack

This folder contains the complete, implementation-ready backend specifications for **Aurelia Jewels** (`com.aistudio.aureliajewels.kxmprq`). These documents are prepared for GitHub version control, client proposals, and Phase 2 backend engineering — without altering the current offline Room SQLite Android prototype.

---

## Document Directory

| Document | Purpose | Audience |
| :--- | :--- | :--- |
| [`BACKEND_PRD.md`](BACKEND_PRD.md) | Master Product Requirements Document: business goals, user roles, free testing scope vs. production scope, commerce workflows, payment/WhatsApp/AI integrations, and acceptance criteria | Client, Product Owner, Backend Lead |
| [`HOSTING_AND_COST_COMPARISON.md`](HOSTING_AND_COST_COMPARISON.md) | Detailed comparison of **Free Testing Stacks** (Firebase, Supabase, Vercel) vs. **Paid Production Hosting** (Blaze, Supabase Pro, Hostinger VPS, Render/Railway), limits, gotchas, and INR/USD monthly budgets | Client, Freelancer, DevOps |
| [`ADMIN_PANEL_SEPARATION_PLAN.md`](ADMIN_PANEL_SEPARATION_PLAN.md) | Strict separation blueprint between the **Customer Android App** and the **Standalone Web Admin Portal**, server-side RBAC, customer-lockout guarantees, and Admin Audit Logging | Client, Security Reviewer, Full-Stack Lead |
| [`API_AND_DATABASE_SPEC.md`](API_AND_DATABASE_SPEC.md) | 1:1 migration map from the app's 8 local Room entities to portable SQL / Firestore schemas, Order/Payment/Refund state machines, REST/RPC API endpoints, and webhook verification contracts | Android Engineer, Backend Engineer |

---

## Recommended Implementation Sequence (When Client Approves Phase 2)

1. **Stage 1 — Free Testing Backend (0 Cost, 3-5 Days)**:
   - Provision either **Supabase Free Tier** (Postgres + Auth + Storage) + **Vercel Free Tier** (Standalone Admin Web Portal), OR **Firebase Spark Free Tier** (Firestore + Google Sign-In + Storage).
   - Sync product catalogue, offers, wishlist, and customer orders; keep payments in clearly labeled test/sandbox mode.
2. **Stage 2 — Standalone Admin Panel & Security Hardening (4-6 Days)**:
   - Deploy the separate web Admin Portal (`admin.aureliajewels.in`) with server-side RBAC (`staff` / `super_admin`).
   - Disable the demo in-app `AdminDesignStudioScreen` in the production Android build flavor/flag so customers can never see or access admin tools.
3. **Stage 3 — Production Hosting & Live Payments (5-7 Days)**:
   - Upgrade to a paid tier (no auto-pause, automated daily backups) before public launch.
   - Integrate **Razorpay** or **Cashfree** (UPI, Cards, NetBanking) with server-verified webhooks and optional WhatsApp Business Cloud API order alerts.
