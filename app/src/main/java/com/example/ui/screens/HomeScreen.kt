package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SeedData
import com.example.data.local.ProductEntity
import com.example.data.local.StoreConfigEntity
import com.example.data.requiresExplicitSize
import com.example.domain.InrFormatter
import com.example.ui.components.AureliaWordmark
import com.example.ui.components.GoldShimmerClaimButton
import com.example.ui.components.JewelleryPhoto
import com.example.ui.components.LuxuryProductCard
import com.example.ui.components.SectionHeaderRow
import com.example.ui.theme.GoldFoilHighlight
import com.example.ui.theme.LocalAureliaColors
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay

private data class HeroSlide(
    val title: String,
    val subtitle: String,
    val cta: String,
    val imageKey: String,
    val targetCategory: String
)

@Composable
fun HomeScreen(
    config: StoreConfigEntity,
    products: List<ProductEntity>,
    wishlistIds: Set<String>,
    cartCount: Int,
    unreadNotifications: Int,
    onOpenSearch: () -> Unit,
    onOpenCart: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenCategory: (String) -> Unit,
    onOpenMetal: (String) -> Unit,
    onOpenOccasion: (String) -> Unit,
    onOpenProduct: (String) -> Unit,
    onToggleWishlist: (String) -> Unit,
    onOpenGuide: () -> Unit,
    onOpenGoldRates: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenStudio: () -> Unit,
    onOpenBespokeAi: () -> Unit,
    appliedCouponCode: String? = null,
    onClaimOffer: (String) -> Unit = {},
    onQuickAddToBag: (ProductEntity) -> Unit = {}
) {
    val ext = LocalAureliaColors.current
    val heroSlides = listOf(
        HeroSlide(
            title = config.heroTitle,
            subtitle = config.heroSubtitle,
            cta = config.heroCta,
            imageKey = "img_hero_bridal",
            targetCategory = "Bridal Sets"
        ),
        HeroSlide(
            title = "Floral Gold Rings",
            subtitle = "Handcrafted 22K and 18K silhouettes for engagements and milestones",
            cta = "Shop Rings",
            imageKey = "img_ring_gold",
            targetCategory = "Rings"
        ),
        HeroSlide(
            title = "Heritage Jhumkas",
            subtitle = "Fine temple filigree paired with delicate natural pearl drops",
            cta = "View Earrings",
            imageKey = "img_earrings_jhumka",
            targetCategory = "Earrings"
        )
    )

    val heroPagerState = rememberPagerState(pageCount = { heroSlides.size })
    LaunchedEffect(heroPagerState.currentPage, heroPagerState.isScrollInProgress) {
        if (!heroPagerState.isScrollInProgress) {
            delay(5500L)
            val next = (heroPagerState.currentPage + 1) % heroSlides.size
            heroPagerState.animateScrollToPage(next)
        }
    }

    val featuredProducts = products.filter { it.isFeatured }.take(6)
    val saleProducts = remember(products) {
        products
            .filter { p -> p.originalPriceInr != null && p.originalPriceInr > p.priceInr && p.stockQuantity > 0 }
            .sortedByDescending { p ->
                val orig = p.originalPriceInr ?: p.priceInr
                ((orig - p.priceInr) * 100L) / orig.coerceAtLeast(1L)
            }
    }
    val bestSellers = products.filter { it.isBestSeller }.take(4)
    val newArrivals = products.filter { it.isNewArrival }.take(4)
    val quickCategories = listOf(
        "Rings", "Necklaces", "Earrings", "Bangles",
        "Mangalsutra", "Bridal Sets", "Bracelets", "Pendants"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        // 1. Minimal Brand Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onOpenStudio,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("home_studio_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "Open Demo Design Studio",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                AureliaWordmark(
                    brandName = config.brandName,
                    brandSubName = config.brandSubName
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("home_notifications_btn")
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                            if (unreadNotifications > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ext.antiqueGold)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onOpenCart,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("home_cart_btn")
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingBag,
                                contentDescription = "Shopping Bag",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                            if (cartCount > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (cartCount > 9) "9+" else cartCount.toString(),
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Compact Luxury Search Bar
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .height(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenSearch)
                    .testTag("home_search_bar"),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.85.dp, ext.hairlineBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Search jewellery, metals, collections...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 2b. Atelier Quick-Access Pill Bar
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    QuickActionPill(
                        icon = Icons.Outlined.LocalOffer,
                        label = "Festive Sale",
                        accent = true,
                        onClick = { onOpenCategory("All") },
                        testTag = "quick_pill_sale"
                    )
                }
                item {
                    QuickActionPill(
                        icon = Icons.Outlined.GridView,
                        label = "All Jewellery",
                        onClick = { onOpenCategory("All") },
                        testTag = "quick_pill_grid"
                    )
                }
                item {
                    QuickActionPill(
                        icon = Icons.Outlined.CurrencyRupee,
                        label = "Gold Rates",
                        onClick = onOpenGoldRates,
                        testTag = "quick_pill_rates"
                    )
                }
                item {
                    QuickActionPill(
                        icon = Icons.Outlined.AutoAwesome,
                        label = "Bespoke AI",
                        onClick = onOpenBespokeAi,
                        testTag = "quick_pill_ai"
                    )
                }
            }
        }

        // 3. Editorial Hero Carousel
        item {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalPager(
                state = heroPagerState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(216.dp)
            ) { page ->
                val slide = heroSlides[page]
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onOpenCategory(slide.targetCategory) }
                        .testTag("hero_slide_$page"),
                    shape = RoundedCornerShape(16.dp),
                    color = ext.deepWarmBrown
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        JewelleryPhoto(
                            imageKey = slide.imageKey,
                            contentDescription = slide.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        0.0f to Color(0xD92B1E17),
                                        0.55f to Color(0x992B1E17),
                                        1.0f to Color.Transparent
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxWidth(0.68f)
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = slide.title,
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color(0xFFFAF6F0),
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = slide.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFEEE5D8),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF6EFE4)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = slide.cta,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = ext.deepWarmBrown
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                        contentDescription = null,
                                        tint = ext.deepWarmBrown,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(heroSlides.size) { index ->
                    val active = heroPagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(5.dp)
                            .width(if (active) 18.dp else 5.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }
        }

        // 4. Restrained Craftsmanship & Demo Info Strip (No false BIS/insured claims)
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TrustMiniItem(Icons.Outlined.Diamond, "Material\nSpecs", onOpenGuide, Modifier.weight(1f))
                TrustMiniItem(Icons.Outlined.VerifiedUser, "Demo\nCheckout", onOpenGuide, Modifier.weight(1f))
                TrustMiniItem(Icons.Outlined.MenuBook, "Sizing\nGuide", onOpenGuide, Modifier.weight(1f))
                TrustMiniItem(Icons.Outlined.SupportAgent, "Store\nConcierge", onOpenSupport, Modifier.weight(1f))
            }
        }

        // 4b. Best Offer & Festive Privilege Sale Box with Gold Foil Claim Button
        item {
            Spacer(modifier = Modifier.height(18.dp))
            BestOfferShowcaseCard(
                appliedCouponCode = appliedCouponCode,
                onClaimOffer = onClaimOffer,
                onShopSale = { onOpenCategory("All") }
            )
        }

        // 4c. Festive Sale & Special Savings Carousel
        if (saleProducts.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeaderRow(
                    title = "Festive Sale Picks",
                    actionText = "Shop All",
                    onActionClick = { onOpenCategory("All") }
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(saleProducts, key = { "sale_${it.id}" }) { product ->
                        SaleDealCard(
                            product = product,
                            isWishlisted = wishlistIds.contains(product.id),
                            onClick = { onOpenProduct(product.id) },
                            onToggleWishlist = { onToggleWishlist(product.id) },
                            onQuickAdd = {
                                if (product.requiresExplicitSize()) {
                                    onOpenProduct(product.id)
                                } else {
                                    onQuickAddToBag(product)
                                }
                            }
                        )
                    }
                }
            }
        }

        // 5. Shop by Category
        item {
            Spacer(modifier = Modifier.height(22.dp))
            SectionHeaderRow(
                title = "Shop by Category",
                actionText = "View All",
                onActionClick = onOpenCategories
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(quickCategories) { category ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(78.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenCategory(category) }
                            .testTag("home_cat_$category")
                    ) {
                        Surface(
                            modifier = Modifier.size(74.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = ext.secondaryCream,
                            border = BorderStroke(0.75.dp, ext.hairlineBorder)
                        ) {
                            JewelleryPhoto(
                                imageKey = SeedData.categoryImageKey(category),
                                contentDescription = category,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 6. Featured Collection Horizontal Showcase
        if (featuredProducts.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(26.dp))
                SectionHeaderRow(
                    title = "Featured Collection",
                    actionText = "Explore All",
                    onActionClick = { onOpenCategory("All") }
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featuredProducts, key = { it.id }) { product ->
                        LuxuryProductCard(
                            product = product,
                            isWishlisted = wishlistIds.contains(product.id),
                            onClick = { onOpenProduct(product.id) },
                            onToggleWishlist = { onToggleWishlist(product.id) },
                            modifier = Modifier.width(164.dp)
                        )
                    }
                }
            }
        }

        // 7. Shop by Metal
        item {
            Spacer(modifier = Modifier.height(26.dp))
            SectionHeaderRow(title = "Shop by Metal")
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(SeedData.allMetals) { metal ->
                    val swatch = when (metal) {
                        "Gold" -> Color(0xFFD4AF37)
                        "Silver" -> Color(0xFFC0C5CE)
                        "Rose Gold" -> Color(0xFFD4A59A)
                        "Brass" -> Color(0xFFB59442)
                        "Copper" -> Color(0xFFB87333)
                        else -> ext.antiqueGold
                    }
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onOpenMetal(metal) }
                            .testTag("home_metal_$metal"),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.8.dp, ext.hairlineBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(swatch)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = metal,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 8. Best Sellers 2-Column Grid
        if (bestSellers.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(26.dp))
                SectionHeaderRow(
                    title = "Best Sellers",
                    actionText = "View All",
                    onActionClick = { onOpenCategory("All") }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    bestSellers.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pair.forEach { product ->
                                LuxuryProductCard(
                                    product = product,
                                    isWishlisted = wishlistIds.contains(product.id),
                                    onClick = { onOpenProduct(product.id) },
                                    onToggleWishlist = { onToggleWishlist(product.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // 9. Occasion Edit
        item {
            Spacer(modifier = Modifier.height(26.dp))
            SectionHeaderRow(title = "Occasion Edit")
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(SeedData.allOccasions) { occasion ->
                    Surface(
                        modifier = Modifier
                            .width(126.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenOccasion(occasion) }
                            .testTag("home_occasion_$occasion"),
                        shape = RoundedCornerShape(12.dp),
                        color = ext.secondaryCream,
                        border = BorderStroke(0.75.dp, ext.hairlineBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = occasion,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Curated Edit ->",
                                style = MaterialTheme.typography.labelSmall,
                                color = ext.antiqueGold
                            )
                        }
                    }
                }
            }
        }

        // 10. New Arrivals
        if (newArrivals.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(26.dp))
                SectionHeaderRow(
                    title = "New Arrivals",
                    actionText = "See All",
                    onActionClick = { onOpenCategory("All") }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    newArrivals.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pair.forEach { product ->
                                LuxuryProductCard(
                                    product = product,
                                    isWishlisted = wishlistIds.contains(product.id),
                                    onClick = { onOpenProduct(product.id) },
                                    onToggleWishlist = { onToggleWishlist(product.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // 11. Subtle Demo Gold Rates Banner
        item {
            Spacer(modifier = Modifier.height(26.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenGoldRates)
                    .testTag("home_gold_rates_card"),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.85.dp, ext.hairlineBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DEMO REFERENCE RATES",
                            style = MaterialTheme.typography.labelSmall,
                            color = ext.antiqueGold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "22K Gold ${InrFormatter.format(7180)}/g  |  24K ${InrFormatter.format(7830)}/g",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Illustrative sample figures only. Tap for chart & silver guide.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = "View demo gold rates",
                        tint = ext.antiqueGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 12. Editorial Brand Story + Bespoke Studio Entry
        item {
            Spacer(modifier = Modifier.height(22.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = "THE HOUSE OF ${config.brandName.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD4AF6A),
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Quiet Luxury Rooted in Indian Craft",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFFFAF7F2)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Every piece in our demonstration atelier balances sculptural restraint with heirloom Indian proportions — from temple filigree to contemporary everyday gold.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFEAE3D8)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onOpenBespokeAi)
                            .testTag("home_bespoke_ai_cta"),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFAF7F2)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Open Bespoke Concept Studio (1K / 2K / 4K)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrustMiniItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalAureliaColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ext.deepWarmBrown,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun QuickActionPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    accent: Boolean = false,
    testTag: String
) {
    val ext = LocalAureliaColors.current
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(24.dp),
        color = if (accent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 0.85.dp,
            color = if (accent) ext.antiqueGold else ext.hairlineBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (accent) GoldFoilHighlight else ext.antiqueGold,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (accent) Color(0xFFFAF7F2) else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun secondsRemainingToday(): Long {
    val now = Calendar.getInstance()
    val midnight = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return ((midnight.timeInMillis - now.timeInMillis) / 1000L).coerceAtLeast(0L)
}

private fun formatCountdown(totalSeconds: Long): String {
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.ENGLISH, "%02dh : %02dm : %02ds", hours, minutes, seconds)
}

@Composable
private fun BestOfferShowcaseCard(
    appliedCouponCode: String?,
    onClaimOffer: (String) -> Unit,
    onShopSale: () -> Unit
) {
    val ext = LocalAureliaColors.current
    var selectedTier by remember { mutableStateOf("AURELIA10") }
    var remainingSeconds by remember { mutableLongStateOf(secondsRemainingToday()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            remainingSeconds = secondsRemainingToday()
        }
    }

    val isCurrentClaimed = appliedCouponCode.equals(selectedTier, ignoreCase = true)
    val headline = if (selectedTier == "AURELIA10") {
        "Flat 10% Off Festive Atelier Privilege"
    } else {
        "Extra 5% Off Heritage Savings"
    }
    val subCopy = if (selectedTier == "AURELIA10") {
        "Apply code AURELIA10 on orders above ₹10,000. Auto-applies in your Shopping Bag."
    } else {
        "Apply code HERITAGE5 with zero minimum bag value on any handcrafted piece."
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("best_offer_box"),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primary,
        border = BorderStroke(1.dp, ext.antiqueGold.copy(alpha = 0.7f)),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ext.antiqueGold.copy(alpha = 0.24f),
                            Color.Transparent
                        ),
                        center = Offset(720f, 60f),
                        radius = 520f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = ext.antiqueGold.copy(alpha = 0.22f),
                        border = BorderStroke(0.6.dp, GoldFoilHighlight.copy(alpha = 0.55f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocalOffer,
                                contentDescription = null,
                                tint = GoldFoilHighlight,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "BEST OFFER SALE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = GoldFoilHighlight
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = Color(0xFFEADCC9),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatCountdown(remainingSeconds),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFFFAF7F2)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = headline,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFFAF7F2)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = subCopy,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE6DDD2)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Two-Tier Offer Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OfferCodeSelectorPill(
                        code = "AURELIA10",
                        benefit = "10% OFF (Min ₹10K)",
                        selected = selectedTier == "AURELIA10",
                        claimed = appliedCouponCode.equals("AURELIA10", ignoreCase = true),
                        onClick = { selectedTier = "AURELIA10" },
                        modifier = Modifier.weight(1f),
                        testTag = "offer_tier_aurelia10"
                    )
                    OfferCodeSelectorPill(
                        code = "HERITAGE5",
                        benefit = "5% OFF (Any Bag)",
                        selected = selectedTier == "HERITAGE5",
                        claimed = appliedCouponCode.equals("HERITAGE5", ignoreCase = true),
                        onClick = { selectedTier = "HERITAGE5" },
                        modifier = Modifier.weight(1f),
                        testTag = "offer_tier_heritage5"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Gold Shimmer Claim Button + Secondary Shop Sale Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GoldShimmerClaimButton(
                        text = if (isCurrentClaimed) "CLAIMED • SHOP NOW" else "CLAIM OFFER",
                        onClick = {
                            if (isCurrentClaimed) {
                                onShopSale()
                            } else {
                                onClaimOffer(selectedTier)
                            }
                        },
                        modifier = Modifier.weight(1.35f),
                        testTag = "best_offer_claim_btn"
                    )

                    Surface(
                        modifier = Modifier
                            .weight(0.85f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(40.dp))
                            .clickable(onClick = onShopSale)
                            .testTag("best_offer_shop_sale_btn"),
                        shape = RoundedCornerShape(40.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, Color(0xFFEADCC9).copy(alpha = 0.7f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "SHOP SALE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = Color(0xFFFAF7F2)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OfferCodeSelectorPill(
    code: String,
    benefit: String,
    selected: Boolean,
    claimed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) Color(0x26FCF6BA) else Color(0x14FFFFFF),
        border = BorderStroke(
            width = if (selected) 1.dp else 0.6.dp,
            color = if (selected) GoldFoilHighlight else Color(0x33EADCC9)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = code,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = GoldFoilHighlight
                )
                if (claimed) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Offer claimed",
                        tint = GoldFoilHighlight,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = benefit,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFEEE5D8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SaleDealCard(
    product: ProductEntity,
    isWishlisted: Boolean,
    onClick: () -> Unit,
    onToggleWishlist: () -> Unit,
    onQuickAdd: () -> Unit
) {
    val ext = LocalAureliaColors.current
    val originalPrice = product.originalPriceInr ?: product.priceInr
    val savingsInr = (originalPrice - product.priceInr).coerceAtLeast(0L)
    val discountPct = if (originalPrice > 0L) ((savingsInr * 100L) / originalPrice).toInt() else 0

    Surface(
        modifier = Modifier
            .width(196.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("sale_deal_card_${product.id}"),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.85.dp, ext.antiqueGold.copy(alpha = 0.45f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
                    .background(ext.secondaryCream)
            ) {
                JewelleryPhoto(
                    imageKey = product.imageKey,
                    customImageUri = product.customImageUri,
                    metal = product.metal,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (discountPct > 0) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "$discountPct% OFF",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            ),
                            color = GoldFoilHighlight,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        .clickable(onClick = onToggleWishlist)
                        .testTag("sale_wishlist_${product.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isWishlisted) "Remove from wishlist" else "Save to wishlist",
                        tint = if (isWishlisted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(11.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Save ${InrFormatter.format(savingsInr)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = ext.success
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = InrFormatter.format(product.priceInr),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = InrFormatter.format(originalPrice),
                        style = MaterialTheme.typography.bodySmall.copy(
                            textDecoration = TextDecoration.LineThrough,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(9.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onQuickAdd)
                        .testTag("sale_quick_add_${product.id}"),
                    shape = RoundedCornerShape(8.dp),
                    color = ext.secondaryCream,
                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 7.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (product.requiresExplicitSize()) "Select Size" else "Add to Bag",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
