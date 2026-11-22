package com.example

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import com.example.data.OrderSnapshotCodec
import com.example.data.SeedData
import com.example.data.local.CartItemEntity
import com.example.data.local.OrderEntity
import com.example.domain.CommerceRules
import com.example.domain.OrderLineSummary
import com.example.ui.CartDisplayItem
import com.example.ui.components.AureliaBottomBar
import com.example.ui.components.MainTab
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LuxuryJewelleryGridScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.AureliaJewelsTheme
import java.io.File
import java.io.FileOutputStream
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-xhdpi")
class AppScreenshotCaptureTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val shouldExportScreenshots: Boolean
        get() = System.getenv("CAPTURE_SCREENSHOTS") == "1"

    private fun outputDir(): File {
        val dir = when {
            File("docs/screenshots").exists() -> File("docs/screenshots")
            File("../docs/screenshots").exists() -> File("../docs/screenshots")
            else -> File("../docs/screenshots").apply { mkdirs() }
        }
        dir.mkdirs()
        return dir
    }

    private fun captureScreenBitmap(): Bitmap {
        composeTestRule.mainClock.advanceTimeByFrame()
        composeTestRule.mainClock.advanceTimeBy(350L)
        shadowOf(Looper.getMainLooper()).idle()
        lateinit var bitmap: Bitmap
        composeTestRule.runOnUiThread {
            val view = composeTestRule.activity.window.decorView
            val w = view.width.coerceAtLeast(1)
            val h = view.height.coerceAtLeast(1)
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
        }
        return bitmap
    }

    private fun savePng(bitmap: Bitmap, filename: String) {
        val file = File(outputDir(), filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
    }

    private fun saveJpg(bitmap: Bitmap, filename: String) {
        val file = File(outputDir(), filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
    }

    @Test
    fun t01_renderSplashScreen() {
        val config = SeedData.defaultStoreConfig()

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            AureliaJewelsTheme(
                primaryColorHex = config.primaryColorHex,
                accentColorHex = config.accentColorHex
            ) {
                SplashScreen(
                    config = config,
                    isSeeded = false,
                    onContinue = {}
                )
            }
        }
        val splashBmp = captureScreenBitmap()
        assertTrue("Splash bitmap should have non-zero size", splashBmp.width > 100 && splashBmp.height > 100)
        if (shouldExportScreenshots) {
            savePng(splashBmp, "01-splash.png")
        }
    }

    @Test
    fun t02_renderHomeAndOffersScreens() {
        val config = SeedData.defaultStoreConfig()
        val products = SeedData.defaultProducts()
        val wishlist = setOf("prod_ring_floral_gold", "prod_neck_temple_choker", "prod_bng_royal_kada")

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            AureliaJewelsTheme(
                primaryColorHex = config.primaryColorHex,
                accentColorHex = config.accentColorHex
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        HomeScreen(
                            config = config,
                            products = products,
                            wishlistIds = wishlist,
                            cartCount = 2,
                            unreadNotifications = 2,
                            onOpenSearch = {},
                            onOpenCart = {},
                            onOpenNotifications = {},
                            onOpenCategories = {},
                            onOpenCategory = {},
                            onOpenMetal = {},
                            onOpenOccasion = {},
                            onOpenProduct = {},
                            onToggleWishlist = {},
                            onOpenGuide = {},
                            onOpenGoldRates = {},
                            onOpenSupport = {},
                            onOpenStudio = {},
                            onOpenBespokeAi = {},
                            appliedCouponCode = null,
                            onClaimOffer = {},
                            onQuickAddToBag = {}
                        )
                    }
                    AureliaBottomBar(
                        currentRoute = MainTab.HOME.route,
                        wishlistCount = wishlist.size,
                        onSelectTab = {}
                    )
                }
            }
        }
        val homeTopBmp = captureScreenBitmap()
        assertTrue(homeTopBmp.width > 100)
        if (shouldExportScreenshots) {
            savePng(homeTopBmp, "02-home.png")
        }

        // Scroll Home LazyColumn to Best Offer VIP Box & Festive Sale Picks
        composeTestRule.onNodeWithTag("home_screen").performScrollToIndex(5)
        val homeOffersBmp = captureScreenBitmap()
        assertTrue(homeOffersBmp.width > 100)
        if (shouldExportScreenshots) {
            savePng(homeOffersBmp, "03-home-offers.png")
        }
    }

    @Test
    fun t03_renderLuxuryGridScreen() {
        val config = SeedData.defaultStoreConfig()
        val products = SeedData.defaultProducts()
        val wishlist = setOf("prod_ring_floral_gold", "prod_neck_temple_choker", "prod_bng_royal_kada")

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            AureliaJewelsTheme(
                primaryColorHex = config.primaryColorHex,
                accentColorHex = config.accentColorHex
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        LuxuryJewelleryGridScreen(
                            products = products,
                            wishlistIds = wishlist,
                            cartCount = 2,
                            title = "Luxury Collection",
                            onBack = {},
                            onOpenSearch = {},
                            onOpenWishlist = {},
                            onOpenCart = {},
                            onProductClick = {},
                            onToggleWishlist = {}
                        )
                    }
                    AureliaBottomBar(
                        currentRoute = MainTab.SHOP.route,
                        wishlistCount = wishlist.size,
                        onSelectTab = {}
                    )
                }
            }
        }
        val gridBmp = captureScreenBitmap()
        assertTrue(gridBmp.width > 100)
        if (shouldExportScreenshots) {
            savePng(gridBmp, "04-luxury-grid.png")
        }
    }

    @Test
    fun t04_renderProductDetailScreen() {
        val config = SeedData.defaultStoreConfig()
        val products = SeedData.defaultProducts()
        val ring = products.first { it.id == "prod_ring_floral_gold" }
        val wishlist = setOf("prod_ring_floral_gold")

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            AureliaJewelsTheme(
                primaryColorHex = config.primaryColorHex,
                accentColorHex = config.accentColorHex
            ) {
                ProductDetailScreen(
                    product = ring,
                    relatedProducts = products.filter { it.id != ring.id }.take(4),
                    isWishlisted = true,
                    wishlistIds = wishlist,
                    onBack = {},
                    onToggleWishlist = {},
                    onAddToCart = { _, _, _, _ -> },
                    onBuyNow = { _, _, _, _ -> },
                    onOpenProduct = {},
                    whatsappNumber = config.whatsappNumber
                )
            }
        }
        val pdpBmp = captureScreenBitmap()
        assertTrue(pdpBmp.width > 100)
        if (shouldExportScreenshots) {
            savePng(pdpBmp, "05-product-detail.png")
        }
    }

    @Test
    fun t05_renderShoppingBagScreen() {
        val config = SeedData.defaultStoreConfig()
        val products = SeedData.defaultProducts()
        val ring = products.first { it.id == "prod_ring_floral_gold" }
        val jhumka = products.first { it.id == "prod_ear_heritage_jhumka" }
        val cartItems = listOf(
            CartDisplayItem(
                entity = CartItemEntity(
                    cartKey = "${ring.id}|22K Yellow Gold|12",
                    productId = ring.id,
                    selectedVariant = "22K Yellow Gold",
                    variantPriceDeltaInr = 0L,
                    selectedSize = "12",
                    quantity = 1
                ),
                product = ring,
                unitPriceInr = ring.priceInr
            ),
            CartDisplayItem(
                entity = CartItemEntity(
                    cartKey = "${jhumka.id}|22K Yellow Gold|One Size",
                    productId = jhumka.id,
                    selectedVariant = "22K Yellow Gold",
                    variantPriceDeltaInr = 0L,
                    selectedSize = "One Size",
                    quantity = 1
                ),
                product = jhumka,
                unitPriceInr = jhumka.priceInr
            )
        )

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            AureliaJewelsTheme(
                primaryColorHex = config.primaryColorHex,
                accentColorHex = config.accentColorHex
            ) {
                CartScreen(
                    cartItems = cartItems,
                    config = config,
                    appliedCouponCode = "AURELIA10",
                    onBack = {},
                    onOpenProduct = {},
                    onUpdateQuantity = { _, _ -> },
                    onRemoveItem = {},
                    onApplyCoupon = { code -> CommerceRules.validateCoupon(code, 125_000L) },
                    onClearCoupon = {},
                    onProceedToCheckout = {},
                    onExploreShop = {}
                )
            }
        }
        val bagBmp = captureScreenBitmap()
        assertTrue(bagBmp.width > 100)
        if (shouldExportScreenshots) {
            savePng(bagBmp, "06-shopping-bag.png")
        }
    }

    @Test
    fun t06_renderAdminDashboardAndCompositeBanner() {
        val config = SeedData.defaultStoreConfig()
        val products = SeedData.defaultProducts()
        val wishlist = setOf("prod_ring_floral_gold", "prod_neck_temple_choker", "prod_bng_royal_kada")
        val nowMs = 1763820000000L
        val sampleOrder1 = OrderEntity(
            orderId = "AUR-2026-8412",
            createdAtMs = nowMs - 7_200_000L,
            status = "Processing",
            customerName = "Ananya Deshmukh",
            customerPhone = "9876543210",
            deliveryAddressSnapshot = "402 Gulmohar Residency, Bandra West, Mumbai, Maharashtra - 400050",
            deliveryMethod = "Express Insured Demo",
            paymentMethod = "UPI (Demo)",
            couponCode = "AURELIA10",
            subtotalInr = 125_000L,
            discountInr = 12_500L,
            shippingInr = 149L,
            grandTotalInr = 112_649L,
            itemsSnapshotSerialized = OrderSnapshotCodec.encode(
                listOf(
                    OrderLineSummary("prod_ring_floral_gold", "Floral Gold Ring", "img_ring_gold", "22K Yellow Gold", "12", 38_500L, 1),
                    OrderLineSummary("prod_ear_heritage_jhumka", "Heritage Jhumka Earrings", "img_earrings_jhumka", "22K Yellow Gold", "One Size", 86_500L, 1)
                )
            )
        )
        val sampleOrder2 = OrderEntity(
            orderId = "AUR-2026-8398",
            createdAtMs = nowMs - 86_400_000L,
            status = "Delivered",
            customerName = "Meera Kulkarni",
            customerPhone = "9820112233",
            deliveryAddressSnapshot = "18 Koregaon Park Lane 4, Pune, Maharashtra - 411001",
            deliveryMethod = "Standard Delivery",
            paymentMethod = "Card (Demo)",
            couponCode = null,
            subtotalInr = 224_000L,
            discountInr = 0L,
            shippingInr = 0L,
            grandTotalInr = 224_000L,
            itemsSnapshotSerialized = OrderSnapshotCodec.encode(
                listOf(
                    OrderLineSummary("prod_neck_temple_choker", "Kashi Heritage Choker", "img_necklace_gold", "22K Yellow Gold", "One Size", 224_000L, 1)
                )
            )
        )

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            AureliaJewelsTheme(
                primaryColorHex = config.primaryColorHex,
                accentColorHex = config.accentColorHex
            ) {
                AdminDashboardScreen(
                    orders = listOf(sampleOrder1, sampleOrder2),
                    products = products,
                    wishlistIds = wishlist,
                    cartItems = emptyList(),
                    addresses = listOf(SeedData.defaultAddress()),
                    onBack = {},
                    onAdvanceOrderStatus = {},
                    onUpsertProduct = {},
                    onDeleteProduct = {},
                    onOpenBrandStudio = {}
                )
            }
        }
        val adminBmp = captureScreenBitmap()
        assertTrue(adminBmp.width > 100)
        if (shouldExportScreenshots) {
            savePng(adminBmp, "07-admin-dashboard.png")
            buildCompositeBannerIfReady()
        }
    }

    private fun buildCompositeBannerIfReady() {
        val dir = outputDir()
        val files = listOf(
            "01-splash.png",
            "02-home.png",
            "04-luxury-grid.png",
            "05-product-detail.png",
            "07-admin-dashboard.png"
        ).map { File(dir, it) }

        if (files.any { !it.exists() }) return

        val bitmaps = files.mapNotNull { BitmapFactory.decodeFile(it.absolutePath) }
        if (bitmaps.size != 5) return

        val bannerW = 1920
        val bannerH = 920
        val phoneW = 324
        val phoneH = 702
        val gap = 36
        val totalPhonesW = phoneW * 5 + gap * 4
        val startX = (bannerW - totalPhonesW) / 2f
        val startY = (bannerH - phoneH) / 2f
        val cornerRadius = 32f
        val bezel = 9f

        val banner = Bitmap.createBitmap(bannerW, bannerH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(banner)

        canvas.drawColor(Color.parseColor("#FAF7F2"))

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EADCC9")
            strokeWidth = 2f
        }
        canvas.drawLine(72f, 44f, bannerW - 72f, 44f, linePaint)
        canvas.drawLine(72f, bannerH - 44f, bannerW - 72f, bannerH - 44f, linePaint)

        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1827351A")
        }
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2A241E")
        }
        val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D5C4A8")
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        val bmpPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        bitmaps.forEachIndexed { idx, src ->
            val left = startX + idx * (phoneW + gap)
            val top = startY

            val shadowRect = RectF(
                left - bezel + 6f,
                top - bezel + 14f,
                left + phoneW + bezel + 6f,
                top + phoneH + bezel + 14f
            )
            canvas.drawRoundRect(shadowRect, cornerRadius + 6f, cornerRadius + 6f, shadowPaint)

            val outerRect = RectF(
                left - bezel,
                top - bezel,
                left + phoneW + bezel,
                top + phoneH + bezel
            )
            canvas.drawRoundRect(outerRect, cornerRadius + 4f, cornerRadius + 4f, framePaint)
            canvas.drawRoundRect(outerRect, cornerRadius + 4f, cornerRadius + 4f, rimPaint)

            val screenRect = RectF(left, top, left + phoneW, top + phoneH)
            val clipPath = Path().apply {
                addRoundRect(screenRect, cornerRadius - 4f, cornerRadius - 4f, Path.Direction.CW)
            }
            canvas.save()
            canvas.clipPath(clipPath)
            canvas.drawBitmap(
                src,
                Rect(0, 0, src.width, src.height),
                screenRect,
                bmpPaint
            )
            canvas.restore()
            src.recycle()
        }

        savePng(banner, "aurelia-app-showcase.png")
        saveJpg(banner, "aurelia-app-showcase.jpg")
        banner.recycle()
    }
}
