# Aurelia Jewels — Free Testing vs. Paid Production Hosting & Cost Guide

This guide compares realistic **free testing backends** and **paid production hosting stacks** for Aurelia Jewels so you can pick the right option for initial client testing today and scale cleanly without surprises later.

> *Note: Cloud pricing and free-tier quotas change periodically. Always verify current provider pricing pages immediately before provisioning production billing.*

---

## 1. Free Testing Stacks (INR 0 / Month — For Demo, QA & Client UAT)

Use these stacks for **testing, staging, and client demonstrations** before taking real customer orders.

| Criteria | Option A: Firebase Spark (Free) | Option B: Supabase Free + Vercel Hobby (Recommended for SQL & Admin) | Option C: Vercel Hobby + Neon Postgres + Cloudflare R2 |
| :--- | :--- | :--- | :--- |
| **Database** | Cloud Firestore (NoSQL) | Managed PostgreSQL (Full SQL) | Serverless PostgreSQL (Neon) |
| **Free DB Limits** | 1 GiB storage, 50K reads/day, 20K writes/day | 500 MB database, 5 GB egress/month | 0.5 GB storage, compute-hour cap |
| **Auth** | Firebase Auth (Google Sign-In free; Phone OTP limited) | Supabase Auth (50,000 MAU free; Google + Email) | Custom JWT / Clerk / Auth.js |
| **Image Storage** | Firebase Storage (requires Blaze card for new buckets in many regions) | Supabase Storage (1 GB free) | Cloudflare R2 (10 GB free, 0 egress fee) |
| **Admin Web Portal Hosting** | Firebase Hosting (10 GB transfer/mo) | Vercel Hobby (free for personal/non-commercial testing) | Vercel Hobby or Cloudflare Pages (free) |
| **Android Integration Effort** | Lowest (official Android SDK + AI Studio native provisioning) | Low-Medium (Ktor/Retrofit REST or Supabase Kotlin SDK) | Medium (REST API via Retrofit/Ktor) |
| **Biggest Free-Tier Catch** | Cloud Functions & Storage often require linking a billing card (Blaze); NoSQL makes complex reporting harder | **Free projects auto-pause after 7 days of inactivity** (must wake up manually or upgrade to Pro) | Vercel Hobby is strictly for **non-commercial/personal use**; cold starts on serverless functions |
| **Best For** | Fastest zero-server Android test + native Gemini AI Logic | Best balance of real SQL, separate Admin Web Panel, and clean future VPS migration | Developers who want full custom Node.js/TypeScript API control |

### Recommendation for Testing Phase
- **Pick Option B (Supabase Free + Cloudflare Pages / Vercel)** if the client may eventually move to their own SQL server (Hostinger VPS, AWS RDS, DigitalOcean) — standard PostgreSQL schemas migrate cleanly.
- **Pick Option A (Firebase Spark)** if you want the fastest direct Android SDK integration and plan to activate **Firebase AI Logic** (`gemini-3-pro-image-preview`) for the Bespoke Studio.

---

## 2. Paid Production Hosting Stacks (When Launching Live to Real Customers)

Never run a live jewellery business taking real payments on a free tier that auto-pauses or lacks automated backups.

### Tier 1: Managed Starter Production (Best for 1,000 to 15,000 Monthly Visitors)

| Stack | Monthly Cost (Approx.) | What You Get | Pros & Cons |
| :--- | :--- | :--- | :--- |
| **Supabase Pro + Cloudflare Pages (Admin) + Cloudflare R2 (Images)** | **~$25/mo** (~INR 2,100 - 2,300/mo) | 8 GB Postgres (no auto-pause), daily automated backups, 100K MAU Auth, 100 GB storage, Mumbai/Singapore region | Zero server maintenance, standard SQL, very reliable for Indian startups |
| **Firebase Blaze (Pay-As-You-Go)** | **~$5 - $35/mo** (~INR 500 - 3,000/mo, usage-based) | Firestore, Auth, Cloud Functions, App Check, FCM Push, Firebase AI Logic | Starts very cheap on low traffic, **must set billing budget alerts** to prevent runaway read spikes |
| **Hostinger KVM VPS (KVM 2) + Docker (Node/FastAPI + Postgres/MySQL)** | **~$7 - $12/mo** (~INR 600 - 1,000/mo on annual plan) | Fixed predictable monthly cost, full control over API + DB + Admin Web Portal, Indian/Singapore datacenter | Lowest fixed cost for client; requires basic Linux/Docker maintenance, SSL (Caddy/Nginx), and off-server backup script |
| **Render / Railway Starter + Managed Postgres** | **~$14 - $25/mo** (~INR 1,200 - 2,200/mo) | Git-push auto-deploys for Node/Python API + managed Postgres (no sleep) | Easy developer workflow without managing raw Linux SSH |

### Tier 2: Growth Production (15,000 to 1,00,000+ Monthly Visitors / High Festive Sale Spikes)

- **Database**: Managed Postgres (Supabase Pro/Team, Neon Scale, or AWS RDS Mumbai `ap-south-1`) with automated point-in-time recovery (PITR) and connection pooling.
- **API & Admin**: Dedicated Node.js/TypeScript (Fastify/Express) or Kotlin/Ktor backend deployed on Render/Railway/Cloud Run (`ap-south-1` Mumbai) + Admin Portal on Cloudflare Pages / Vercel Pro ($20/mo).
- **Media CDN**: **Cloudflare R2** + Image Resizing (zero egress charges even when thousands of users scroll high-res jewellery photos).
- **Estimated Total**: **~$50 - $120/mo** (~INR 4,500 - 10,500/mo).

---

## 3. Hidden / Third-Party Production Costs to Disclose to the Client

Always include these line items in your client quotation so there are no surprises later:

| Service | Cost Model | Notes for India |
| :--- | :--- | :--- |
| **Domain Name** (`.in` / `.com`) | ~INR 600 - 1,200 / year | Needed for `api.aureliajewels.in` and `admin.aureliajewels.in` |
| **Google Play Console Account** | $25 one-time (~INR 2,100) | Required once to publish the Android app on Google Play Store |
| **Payment Gateway (Razorpay / Cashfree)** | ~2% per card/netbanking txn; 0% on standard UPI (subject to gateway plan) | No monthly fixed fee on standard plans; KYC + business bank account required |
| **WhatsApp Business Cloud API** | Free for customer-initiated service replies; ~INR 0.12 - 0.85 per business-initiated utility/marketing template | Optional; free `wa.me` deep links remain INR 0 |
| **SMS / Phone OTP (MSG91 / Twilio / Firebase)** | ~INR 0.15 - 0.50 per SMS (India DLT registration required for SMS) | Google Sign-In + Email OTP are free and avoid Indian DLT paperwork initially |
| **Gemini AI Image Generation (Optional)** | Pay-per-image via Firebase AI Logic / Gemini API | Add strict per-user daily caps before enabling publicly |

---

## 4. Decision Cheat Sheet for the Client

- **"I want to test for 2 weeks with zero investment"** -> **Supabase Free Tier + Cloudflare Pages (Admin)** or **Firebase Spark**.
- **"I want lowest fixed monthly cost after launch (~INR 800/mo)"** -> **Hostinger KVM VPS (Mumbai/Singapore)** running Dockerized API + PostgreSQL + Cloudflare R2 images.
- **"I want zero server headaches after launch (~INR 2,200/mo)"** -> **Supabase Pro + Cloudflare Pages (Admin) + Cloudflare R2**.
- **"I want live Gemini AI Bespoke Studio + Push Notifications with minimal backend code"** -> **Firebase Blaze** (with budget cap alerts enabled).
