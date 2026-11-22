package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VariantOption
import com.example.data.local.ProductEntity
import com.example.data.parsedSizes
import com.example.data.parsedVariants
import com.example.data.requiresExplicitSize
import com.example.domain.InrFormatter
import com.example.ui.components.EmptyLuxuryState
import com.example.ui.components.JewelleryPhoto
import com.example.ui.components.LuxuryProductCard
import com.example.ui.components.PrimaryOliveButton
import com.example.ui.components.QuantityStepper
import com.example.ui.components.SecondaryOutlinedButton
import com.example.ui.theme.LocalAureliaColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: ProductEntity?,
    relatedProducts: List<ProductEntity>,
    isWishlisted: Boolean,
    wishlistIds: Set<String>,
    onBack: () -> Unit,
    onToggleWishlist: (String) -> Unit,
    onAddToCart: (ProductEntity, VariantOption, String, Int) -> Unit,
    onBuyNow: (ProductEntity, VariantOption, String, Int) -> Unit,
    onOpenProduct: (String) -> Unit
) {
    val ext = LocalAureliaColors.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    if (product == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
        ) {
            EmptyLuxuryState(
                icon = Icons.Outlined.Diamond,
                title = "Piece Unavailable",
                subtitle = "This jewellery piece is no longer in the demo catalogue.",
                actionLabel = "Go Back",
                onAction = onBack
            )
        }
        return
    }

    val variants = remember(product.id, product.variantsCsv) { product.parsedVariants() }
    val sizes = remember(product.id, product.sizesCsv) { product.parsedSizes() }
    val needsExplicitSize = remember(product.id, product.sizesCsv) { product.requiresExplicitSize() }

    var selectedVariant by remember(product.id) { mutableStateOf(variants.first()) }
    var selectedSize by remember(product.id) {
        mutableStateOf(if (needsExplicitSize) "" else sizes.first())
    }
    var quantity by remember(product.id) { mutableIntStateOf(1) }
    var sizeError by remember(product.id) { mutableStateOf<String?>(null) }
    var showSizeGuide by remember { mutableStateOf(false) }

    val activePrice = (product.priceInr + selectedVariant.priceDeltaInr).coerceAtLeast(100L)
    val galleryFrames = remember(product.id, product.imageKey) {
        listOf(
            product.imageKey to "Studio View",
            "img_hero_bridal" to "Editorial Mood",
            product.imageKey to "Detail Finish"
        )
    }
    val pagerState = rememberPagerState(pageCount = { galleryFrames.size })

    val sizeLabel = when (product.category) {
        "Rings" -> "Select Ring Size (Indian)"
        "Bangles" -> "Select Bangle Size"
        "Bracelets" -> "Select Wrist Size"
        "Anklets" -> "Select Anklet Length"
        else -> "Select Size"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("product_detail_screen")
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Large Editorial Hero Gallery
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .background(ext.secondaryCream)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { idx ->
                    val (key, desc) = galleryFrames[idx]
                    JewelleryPhoto(
                        imageKey = key,
                        customImageUri = if (idx == 0) product.customImageUri else null,
                        metal = product.metal,
                        contentDescription = "${product.name} - $desc",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Floating Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FloatingCircleAction(
                        icon = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBack,
                        testTag = "pdp_back_btn"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FloatingCircleAction(
                            icon = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isWishlisted) "Remove from wishlist" else "Save to wishlist",
                            tint = if (isWishlisted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleWishlist(product.id)
                            },
                            testTag = "pdp_wishlist_btn"
                        )
                        FloatingCircleAction(
                            icon = Icons.Outlined.Share,
                            contentDescription = "Share product",
                            onClick = {
                                val shareText = "${product.name} (${product.purity}) — ${InrFormatter.format(activePrice)} at Aurelia Jewels (Demo Catalogue)"
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Jewellery Piece"))
                            },
                            testTag = "pdp_share_btn"
                        )
                    }
                }

                // Pager Position Dots
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.28f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(galleryFrames.size) { index ->
                        val active = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (active) 7.dp else 5.dp)
                                .clip(CircleShape)
                                .background(if (active) Color.White else Color.White.copy(alpha = 0.45f))
                        )
                    }
                }
            }

            // Thumbnail strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                galleryFrames.forEachIndexed { index, (key, label) ->
                    val selected = pagerState.currentPage == index
                    Surface(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (selected) 1.5.dp else 0.75.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else ext.hairlineBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                scope.launch { pagerState.animateScrollToPage(index) }
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = ext.secondaryCream
                    ) {
                        JewelleryPhoto(
                            imageKey = key,
                            customImageUri = if (index == 0) product.customImageUri else null,
                            metal = product.metal,
                            contentDescription = label,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // 2. Product Details Header
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = product.collection.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = ext.antiqueGold,
                    letterSpacing = 1.4.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${product.purity}  |  ${product.weightGrams}  |  SKU ${product.sku}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = InrFormatter.format(activePrice),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = MaterialTheme.typography.titleMedium.fontFamily
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (product.originalPriceInr != null && product.originalPriceInr > activePrice) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = InrFormatter.format(product.originalPriceInr),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = TextDecoration.LineThrough
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ext.champagne,
                        border = BorderStroke(0.5.dp, ext.antiqueGold.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = product.purity.ifBlank { product.metal },
                            style = MaterialTheme.typography.labelSmall,
                            color = ext.deepWarmBrown,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (product.stockQuantity > 0) {
                        "Illustrative demo price  |  In stock (${product.stockQuantity} available)"
                    } else {
                        "Currently unavailable in demo inventory"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.stockQuantity > 0) ext.success else MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(thickness = 0.75.dp, color = ext.hairlineBorder)
                Spacer(modifier = Modifier.height(16.dp))

                // 3. Metal Variant Selector
                if (variants.isNotEmpty()) {
                    Text("Metal & Finish", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        variants.forEach { variant ->
                            val selected = selectedVariant == variant
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) MaterialTheme.colorScheme.primary else ext.hairlineBorder
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedVariant = variant }
                            ) {
                                Text(
                                    text = variant.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 4. Category-Relevant Size Selector
                if (needsExplicitSize) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(sizeLabel, style = MaterialTheme.typography.titleSmall)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { showSizeGuide = true }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Straighten,
                                contentDescription = null,
                                tint = ext.antiqueGold,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Size Guide",
                                style = MaterialTheme.typography.labelMedium,
                                color = ext.antiqueGold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sizes.forEach { size ->
                            val selected = selectedSize == size
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) MaterialTheme.colorScheme.primary else ext.hairlineBorder
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedSize = size
                                        sizeError = null
                                    }
                                    .testTag("size_chip_$size")
                            ) {
                                Text(
                                    text = size,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                                )
                            }
                        }
                    }
                    if (!sizeError.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = sizeError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 5. Quantity Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Quantity", style = MaterialTheme.typography.titleSmall)
                    QuantityStepper(
                        quantity = quantity,
                        onDecrease = { if (quantity > 1) quantity-- },
                        onIncrease = { if (quantity < product.stockQuantity.coerceAtLeast(1)) quantity++ },
                        maxQuantity = product.stockQuantity.coerceAtLeast(1)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(thickness = 0.75.dp, color = ext.hairlineBorder)
                Spacer(modifier = Modifier.height(16.dp))

                // 6. Craftsmanship Description
                Text("Craftsmanship & Details", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 7. Honest Demo Specification & Care Cards (no false hallmark/certification claims)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ext.secondaryCream,
                    border = BorderStroke(0.75.dp, ext.hairlineBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DetailInfoRow(
                            icon = Icons.Outlined.Diamond,
                            title = "Material & Occasion",
                            body = "${product.metal} (${product.purity})  |  Suited for ${product.occasion}"
                        )
                        DetailInfoRow(
                            icon = Icons.Outlined.AutoAwesome,
                            title = "Care Guidance",
                            body = product.careInstructions
                        )
                        DetailInfoRow(
                            icon = Icons.Outlined.LocalShipping,
                            title = "Demo Delivery Estimate",
                            body = "Standard & Express demo options selectable at checkout. No live courier booking is active."
                        )
                        DetailInfoRow(
                            icon = Icons.Outlined.Info,
                            title = "Prototype Disclosure",
                            body = "Catalogue item, weight, and INR price are illustrative demonstration data only."
                        )
                    }
                }
            }

            // 8. Related Pieces
            if (relatedProducts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "You May Also Like",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(relatedProducts, key = { it.id }) { related ->
                        LuxuryProductCard(
                            product = related,
                            isWishlisted = wishlistIds.contains(related.id),
                            onClick = { onOpenProduct(related.id) },
                            onToggleWishlist = { onToggleWishlist(related.id) },
                            modifier = Modifier.width(156.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Sticky Bottom CTA Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.navigationBarsPadding()) {
                HorizontalDivider(thickness = 0.75.dp, color = ext.hairlineBorder)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PrimaryOliveButton(
                        text = "Add to Bag",
                        leadingIcon = Icons.Outlined.ShoppingBag,
                        enabled = product.stockQuantity > 0,
                        onClick = {
                            if (needsExplicitSize && selectedSize.isBlank()) {
                                sizeError = "Please choose a size before adding to bag"
                            } else {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onAddToCart(
                                    product,
                                    selectedVariant,
                                    selectedSize.ifBlank { "One Size" },
                                    quantity
                                )
                            }
                        },
                        modifier = Modifier.weight(1.25f),
                        testTag = "pdp_add_to_bag"
                    )
                    SecondaryOutlinedButton(
                        text = "Buy Now",
                        enabled = product.stockQuantity > 0,
                        onClick = {
                            if (needsExplicitSize && selectedSize.isBlank()) {
                                sizeError = "Please choose a size to continue"
                            } else {
                                onBuyNow(
                                    product,
                                    selectedVariant,
                                    selectedSize.ifBlank { "One Size" },
                                    quantity
                                )
                            }
                        },
                        modifier = Modifier.weight(0.95f),
                        testTag = "pdp_buy_now"
                    )
                }
            }
        }
    }

    if (showSizeGuide) {
        ModalBottomSheet(
            onDismissRequest = { showSizeGuide = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text("Indian Sizing Reference", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (product.category == "Bangles") {
                        "Measure the inner diameter of a comfortable bangle across its widest center:\n\n" +
                            "2-2  :  54.0 mm (2.12 in)\n" +
                            "2-4  :  57.2 mm (2.25 in)\n" +
                            "2-6  :  60.3 mm (2.37 in)\n" +
                            "2-8  :  63.5 mm (2.50 in)"
                    } else {
                        "Measure the inner diameter of a well-fitting ring:\n\n" +
                            "Size 8   :  15.3 mm\n" +
                            "Size 10  :  15.9 mm\n" +
                            "Size 12  :  16.5 mm\n" +
                            "Size 14  :  17.2 mm\n" +
                            "Size 16  :  17.8 mm\n" +
                            "Size 18  :  18.4 mm"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun FloatingCircleAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onBackground,
    testTag: String
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shadowElevation = 2.dp,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun DetailInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String
) {
    val ext = LocalAureliaColors.current
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ext.antiqueGold,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
