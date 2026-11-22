# Aurelia Jewels — Hosting & Infrastructure Guide (Free Testing vs. Paid Production)

This guide compares practical **free-tier hosting options** for development, QA, and client demonstrations against **paid production hosting solutions** (including Hostinger VPS, Supabase Pro, Firebase Blaze, and managed PaaS options) for scaling **Aurelia Jewels** safely in India.

> *Pricing Note: Cloud provider quotas and billing rates evolve over time. Always verify current regional pricing (prefer Mumbai `ap-south-1` or Singapore `ap-southeast-1`) before activating production billing.*

---

## 1. Free-Tier Options (Development, QA & Client Testing — INR 0/mo)

Use these tiers strictly for **staging, demos, and pre-launch validation** before accepting real customer payments.

### 1.1 Firebase Spark Plan (Google)
- **Included Services**: Cloud Firestore (1 GiB storage, 50K reads/day, 20K writes/day), Firebase Authentication (Google Sign-In free), Firebase Hosting (10 GB/mo for Admin Web Portal), App Check.
- **Pros**:
  - Fastest integration with native Android (Kotlin/Compose) via official Android SDKs and offline persistence out of the box.
  - Built-in support for Firebase Cloud Messaging (push notifications) and optional **Firebase AI Logic** (`gemini-3-pro-image-preview`) for the Bespoke Concept Studio.
  - Zero server provisioning or Linux maintenance required during prototyping.
- **Cons**:
  - Cloud Functions and Cloud Storage buckets typically require upgrading to Blaze (card on file) even at low usage.
  - NoSQL document model makes complex financial reporting, multi-table joins, and future migration to MySQL/PostgreSQL harder.
  - Unbounded client queries can trigger unexpected read spikes if rules/pagination are not strictly designed.

### 1.2 Supabase Free Plan + Vercel / Cloudflare Pages (Recommended for SQL & Admin)
- **Included Services**: Managed PostgreSQL (500 MB DB, 5 GB egress/mo), Supabase Auth (50,000 MAU), Supabase Storage (1 GB), Edge Functions, plus free Admin Web Portal hosting on Cloudflare Pages or Vercel Hobby.
- **Pros**:
  - Real **PostgreSQL** database with standard SQL tables, foreign keys, `UNIQUE` constraints, and transactions — ideal for e-commerce orders, inventory, and coupons.
  - Clean exit path: standard SQL schema migrates smoothly to Hostinger VPS, AWS RDS, or any managed Postgres/MySQL host later without rewriting data models.
  - Built-in Row Level Security (RLS), instant REST APIs, and table viewer for rapid client UAT.
- **Cons**:
  - **7-day inactivity auto-pause**: Free projects pause automatically if unused for a week (acceptable for testing, unacceptable for live stores).
  - 500 MB database limit and 1 GB media storage fill up quickly if unoptimized jewellery photos are uploaded directly.
  - Vercel Hobby is restricted to non-commercial/personal projects (use Cloudflare Pages or upgrade to Vercel Pro for commercial production).

### 1.3 Vercel Hobby + Neon Serverless Postgres + Cloudflare R2
- **Included Services**: Node.js/TypeScript serverless API routes (Vercel), Serverless Postgres (Neon 0.5 GB), Object Storage (Cloudflare R2 10 GB free with zero egress fees).
- **Pros**:
  - Full control over custom backend business logic, middleware, and REST endpoint design.
  - Cloudflare R2 eliminates bandwidth charges for high-resolution jewellery product galleries.
- **Cons**:
  - More moving parts to wire together compared to all-in-one Firebase or Supabase.
  - Serverless cold starts add latency on the first request after idle periods.
  - Vercel Hobby cannot be used for commercial production workloads.

### Quick Free-Tier Comparison Matrix

| Stack | Database Type | Admin Hosting | Free-Tier Risk | Best Use Case |
| :--- | :--- | :--- | :--- | :--- |
| **Firebase Spark** | NoSQL (Firestore) | Firebase Hosting | Card needed for Functions/Storage; NoSQL lock-in | Rapid Android-only testing & Gemini AI Logic |
| **Supabase Free + Cloudflare Pages** | SQL (PostgreSQL) | Cloudflare Pages | Auto-pauses after 7 days of inactivity | Best testing mirror of a portable SQL production stack |
| **Vercel + Neon + Cloudflare R2** | SQL (PostgreSQL) | Vercel Hobby | Non-commercial Hobby license; serverless cold starts | Custom TypeScript/Node API prototyping |

---

## 2. Recommended Paid Production Hosting Solutions (Live Launch & Scale)

Before launching to real customers and connecting Razorpay/Cashfree payments, migrate off free tiers to guarantee **zero auto-pausing, daily backups, predictable latency in India, and SLA uptime**.

### 2.1 Hostinger KVM VPS (Best Fixed-Cost Scalable Option — ~INR 600 - 1,200/mo)
- **Setup**: Hostinger KVM 2 (2 vCPU, 8 GB RAM, 100 GB NVMe, Mumbai or Singapore datacenter) running Docker Compose (`Node.js/Fastify or Kotlin/Ktor API` + `PostgreSQL 16` + `Caddy/Nginx auto-SSL` + `Standalone Admin Web Portal`) paired with **Cloudflare R2** for product images.
- **Pros**:
  - **Lowest predictable fixed monthly bill** with zero surprise per-read/per-write billing spikes during Diwali/Akshaya Tritiya sale traffic.
  - Full ownership of database, server logs, cron jobs, and backups; zero vendor lock-in.
  - Runs the API, database, and Admin Portal together cleanly on a single Mumbai-region server up to tens of thousands of daily visits.
- **Cons**:
  - Requires basic Linux/Docker DevOps setup (firewall, SSH hardening, automated off-server `pg_dump` backups to R2/S3, uptime alerts).
  - Manual scaling step needed if traffic grows beyond a single VPS (move DB to managed Postgres later).

### 2.2 Supabase Pro + Cloudflare Pages + Cloudflare R2 (Best Zero-DevOps SQL Stack — ~INR 2,100 - 2,400/mo)
- **Setup**: Supabase Pro ($25/mo, Mumbai/Singapore region) + Cloudflare Pages (free/commercial Admin Web Portal) + Cloudflare R2 (media CDN).
- **Pros**:
  - Zero server patching or Linux administration; **no auto-pausing**.
  - Automated daily database backups, 8 GB Postgres included, 100K monthly active users, and connection pooling.
  - Fastest transition from Supabase Free testing to production (literally a 1-click plan upgrade).
- **Cons**:
  - Higher baseline monthly cost (~$25/mo) than a single VPS at very small order volumes.
  - Bandwidth/storage overages apply if media is not offloaded to Cloudflare R2.

### 2.3 Firebase Blaze (Pay-As-You-Go — ~INR 500 - 3,500+/mo)
- **Setup**: Firestore + Firebase Auth + Cloud Functions (2nd Gen, `asia-south1` Mumbai) + Firebase Hosting + App Check + optional Firebase AI Logic.
- **Pros**:
  - Starts very inexpensive during early launch months (free tier quotas still apply before billing begins).
  - Native Android offline sync, FCM push notifications, and seamless Gemini AI image generation support.
- **Cons**:
  - **Variable usage-based billing**: requires strict Google Cloud budget alerts and query limits to avoid unexpected bills during traffic bursts.
  - Document-store structure is less ergonomic than SQL for GST invoicing, refund reconciliation, and multi-table admin analytics.

### 2.4 Render / Railway Starter + Managed Postgres (~INR 1,200 - 2,200/mo)
- **Pros**: Git-push auto-deploys for custom Node/Python/Kotlin APIs with managed Postgres and zero SSH administration.
- **Cons**: Regional datacenter choice for India is more limited than Mumbai VPS/Supabase/AWS (`ap-south-1`), and RAM pricing rises faster than Hostinger VPS at scale.

### Paid Production Comparison Matrix

| Solution | Approx. Monthly Cost | Scalability | Maintenance Effort | Recommended When |
| :--- | :--- | :--- | :--- | :--- |
| **Hostinger KVM VPS + Cloudflare R2** | INR 600 - 1,200 | High (vertical + easy DB split later) | Medium (Docker + backup script) | Client wants lowest fixed monthly cost & full server control |
| **Supabase Pro + Cloudflare Pages + R2** | INR 2,100 - 2,400 | High (managed Postgres + pooling) | Very Low | Client wants hands-off reliability + portable SQL |
| **Firebase Blaze** | INR 500 - 3,500+ (usage) | Very High (auto-scaling) | Very Low | Client prioritizes native Android SDK + live Gemini AI Studio |
| **Render / Railway + Managed Postgres** | INR 1,200 - 2,200 | Medium-High | Low | Team prefers Git-push PaaS over raw VPS management |

---

## 3. Additional Production Launch Costs (Third-Party Services)

| Item | Estimated Cost (India) | Purpose |
| :--- | :--- | :--- |
| Domain (`.in` / `.com`) | INR 600 - 1,200 / year | Custom `api.aureliajewels.in` and `admin.aureliajewels.in` endpoints |
| Google Play Console | $25 one-time (~INR 2,100) | Publishing the Customer Android APK/AAB on Play Store |
| Razorpay / Cashfree Gateway | ~2% Cards/NetBanking; 0% standard UPI | Live Indian payment collection & verified webhooks |
| WhatsApp Business Cloud API | Free service replies; ~INR 0.12 - 0.85 per template | Optional automated order confirmation messages (free `wa.me` links remain INR 0) |
| SMS OTP (MSG91 / Fast2SMS) | ~INR 0.15 - 0.50 / SMS + DLT registration | Optional if Phone OTP is added beyond Google Sign-In / Email |

---

## 4. Recommended Phased Upgrade Path

1. **Today (Offline Demo)**: Local Room SQLite APK (`INR 0/mo`) — zero backend required.
2. **Client UAT / Testing (2-4 Weeks)**: **Supabase Free Tier** (Postgres + Auth) + **Cloudflare Pages** (Standalone Admin Web Portal) (`INR 0/mo`).
3. **Production Go-Live**:
   - **Budget-Optimized Path**: Migrate the exact same PostgreSQL schema to a **Hostinger KVM 2 VPS (Mumbai)** + **Cloudflare R2** (`~INR 800/mo`).
   - **Zero-Ops Path**: Upgrade Supabase project to **Supabase Pro** + **Cloudflare R2** (`~INR 2,200/mo`).
