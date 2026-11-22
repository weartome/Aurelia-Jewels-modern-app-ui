# Client Handoff & APK Delivery Guide

This guide explains how to build, download, and share the **Aurelia Jewels** Android `.apk` file with a client without installing Android Studio or writing code.

---

## Part 1: Get the `.apk` File from GitHub (2-4 Minutes)

### Step 1: Push the Project to GitHub
1. In Google AI Studio, open the **Export / Save to GitHub** menu and push this project to a new GitHub repository (public or private).

### Step 2: Wait for the Automatic Build
1. Open your GitHub repository in a browser.
2. Click the **Actions** tab near the top of the page.
3. You will see a workflow named **Build Aurelia Jewels Android APK** running automatically.
4. Wait until the yellow spinner turns into a **green checkmark** (typically 2-4 minutes).

### Step 3: Download the APK
- **Method A (Standard Artifact):**
  1. Click on the completed workflow run.
  2. Scroll down to **Artifacts** and click **Aurelia-Jewels-APK**.
  3. Unzip the downloaded file to get `Aurelia-Jewels-debug.apk`.
- **Method B (Direct APK Link via GitHub Release — Easiest for Clients):**
  1. Open **Actions** -> **Build Aurelia Jewels Android APK**.
  2. Click **Run workflow** on the right.
  3. Check the box **Create a GitHub Release with direct APK download link**, keep `v1.0.0-demo` (or change the version), and click **Run workflow**.
  4. When done, open **Releases** on the repository homepage and download or share `Aurelia-Jewels-debug.apk` directly.

---

## Part 2: Send the APK to Your Client

1. **WhatsApp (Recommended):** Send `Aurelia-Jewels-debug.apk` as a **Document** (not as a photo/video), OR upload it to **Google Drive** (set access to "Anyone with the link can view") and send the Drive link.
2. **Client Installation Steps (Android Phone):**
   - Tap the downloaded `Aurelia-Jewels-debug.apk` file.
   - If Android shows *"For your security, your phone is not allowed to install unknown apps from this source"*, tap **Settings** -> turn ON **Allow from this source** -> go back -> tap **Install**.
   - If Google Play Protect shows a warning (normal for unsigned/debug demo APKs not downloaded from Play Store), tap **More details** -> **Install anyway**.
   - Open **Aurelia Jewels** from the home screen.

---

## Part 3: Ready-to-Send Client Message Template

Copy and customize the message below when sharing the APK:

> Hi [Client Name],
>
> Sharing the Android demo build (`Aurelia-Jewels-debug.apk`) for the **Aurelia Jewels** mobile app.
>
> **How to install on your Android phone:**
> 1. Download the attached APK file (or use this link: [Google Drive / GitHub Release Link]).
> 2. Tap the file to install. If prompted, enable **Allow from this source** in Settings.
> 3. Open **Aurelia Jewels** to explore the full flow.
>
> **Key highlights to test:**
> - Luxury editorial home screen, 12 jewellery categories, and 29 pre-loaded products
> - Filter & Sort luxury grid (by Category, Material, Price Range, and Saved items)
> - Persistent Wishlist (heart icon) and Shopping Bag with promo codes (`AURELIA10` or `HERITAGE5`)
> - 3-step guest checkout, simulated demo payment, order tracking, and WhatsApp inquiry handoff
> - Built-in Admin Design Studio (accessible from Account) to add/edit jewellery items or adjust brand accent colors
>
> *Note: This is an offline interactive demo build — all data stays on your phone and checkout payments are simulated.*
>
> Let me know your feedback after trying it out.
