# App Preview Screenshots Directory

This folder stores the real Jetpack Compose screenshots displayed in the root `README.md`.

## Included Files

- `aurelia-app-showcase.png` (and `aurelia-app-showcase.jpg`) -- 5-screen panoramic showcase composited from the real rendered app screens below
- `01-splash.png` -- Branded Deep Forest Olive Splash screen with lotus monogram emblem
- `02-home.png` -- Home Discovery screen with Bridal Hero Carousel, Quick Pills, Trust Strip, and Best Offer VIP Box
- `03-home-offers.png` -- Best Offer VIP Box with metallic Gold Foil Crown "CLAIM OFFER" button and Festive Sale Picks
- `04-luxury-grid.png` -- Luxury Jewellery Grid with metal/saved chips, sort/filter controls, purity badges, and wishlist hearts
- `05-product-detail.png` -- Product Detail screen for Floral Gold Ring with HD gallery, metal finish, ring sizes, and sticky CTAs
- `06-shopping-bag.png` -- Shopping Bag with quantity steppers, applied `AURELIA10` privilege code, and INR breakdown
- `07-admin-dashboard.png` -- Separated Admin Operations Console with Order Management KPIs and 1-tap Advance Stage action

## Regenerating Screenshots After UI Changes

Run the Robolectric Native Graphics capture test from the repository root:

```bash
CAPTURE_SCREENSHOTS=1 gradle :app:testDebugUnitTest \
  --tests "com.example.AppScreenshotCaptureTest" \
  -Drobolectric.dependency.repo.url=https://maven-central.storage-download.googleapis.com/maven2
```

Normal unit test runs (`gradle :app:testDebugUnitTest`) verify that all screens render cleanly without modifying files in this directory.
