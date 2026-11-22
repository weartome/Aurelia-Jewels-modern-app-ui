package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ProductEntity
import com.example.data.local.StoreConfigEntity
import com.example.data.parsedSizes
import com.example.data.parsedVariants
import com.example.data.requiresExplicitSize
import com.example.domain.CommerceRules
import com.example.domain.InrFormatter
import com.example.ui.CartDisplayItem
import com.example.ui.components.EmptyLuxuryState
import com.example.ui.components.GoldShimmerClaimButton
import com.example.ui.components.JewelleryPhoto
import com.example.ui.components.LuxuryProductCard
import com.example.ui.components.LuxuryTopBar
import com.example.ui.components.PrimaryOliveButton
import com.example.ui.components.QuantityStepper
import com.example.ui.theme.LocalAureliaColors

@Composable
fun WishlistScreen(
    allProducts: List<ProductEntity>,
    wishlistIds: Set<String>,
    onOpenProduct: (String) -> Unit,
    onToggleWishlist: (String) -> Unit,
    onQuickAddToBag: (ProductEntity) -> Unit,
    onExploreShop: () -> Unit
) {
    val savedProducts = remember(allProducts, wishlistIds) {
        allProducts.filter { wishlistIds.contains(it.id) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("wishlist_screen")
    ) {
        LuxuryTopBar(
            title = "Wishlist",
            subtitle = if (savedProducts.isEmpty()) "Saved pieces" else "${savedProducts.size} Saved"
        )

        if (savedProducts.isEmpty()) {
            EmptyLuxuryState(
                icon = Icons.Outlined.FavoriteBorder,
                title = "Your Wishlist is Empty",
                subtitle = "Save rings, bangles, jhumkas, and bridal pieces you love to revisit them anytime.",
                actionLabel = "Explore Collections",
                onAction = onExploreShop,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(savedProducts, key = { it.id }) { product ->
                    Column {
                        LuxuryProductCard(
                            product = product,
                            isWishlisted = true,
                            onClick = { onOpenProduct(product.id) },
                            onToggleWishlist = { onToggleWishlist(product.id) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (product.requiresExplicitSize()) {
                                        onOpenProduct(product.id)
                                    } else {
                                        onQuickAddToBag(product)
                                    }
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = if (product.requiresExplicitSize()) "Select Size" else "Move to Bag",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartScreen(
    cartItems: List<CartDisplayItem>,
    config: StoreConfigEntity,
    appliedCouponCode: String?,
    onBack: () -> Unit,
    onOpenProduct: (String) -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onApplyCoupon: (String) -> CommerceRules.CouponValidation,
    onClearCoupon: () -> Unit,
    onProceedToCheckout: () -> Unit,
    onExploreShop: () -> Unit
) {
    val ext = LocalAureliaColors.current
    var couponInput by remember(appliedCouponCode) { mutableStateOf(appliedCouponCode.orEmpty()) }
    var couponFeedback by remember { mutableStateOf<CommerceRules.CouponValidation?>(null) }

    val breakdown = remember(cartItems, appliedCouponCode, config.standardShippingInr) {
        CommerceRules.calculateTotals(
            itemTotalsInr = cartItems.map { it.lineTotalInr },
            couponCode = appliedCouponCode,
            shippingChargeInr = config.standardShippingInr
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("cart_screen")
    ) {
        LuxuryTopBar(
            title = "My Bag (${cartItems.sumOf { it.entity.quantity }})",
            onBack = onBack
        )

        if (cartItems.isEmpty()) {
            EmptyLuxuryState(
                icon = Icons.Outlined.ShoppingBag,
                title = "Your Shopping Bag is Empty",
                subtitle = "Discover handcrafted gold, silver, and heritage jewellery pieces to add to your bag.",
                actionLabel = "Browse Collection",
                onAction = onExploreShop,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems, key = { it.entity.cartKey }) { item ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.75.dp, ext.hairlineBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cart_item_${item.product.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ext.secondaryCream,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onOpenProduct(item.product.id) }
                            ) {
                                JewelleryPhoto(
                                    imageKey = item.product.imageKey,
                                    customImageUri = item.product.customImageUri,
                                    metal = item.product.metal,
                                    contentDescription = item.product.name,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = item.product.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { onRemoveItem(item.entity.cartKey) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteOutline,
                                            contentDescription = "Remove ${item.product.name}",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }
                                val sizeLabel = if (item.entity.selectedSize != "One Size") {
                                    "  |  Size ${item.entity.selectedSize}"
                                } else ""
                                Text(
                                    text = "${item.entity.selectedVariant}$sizeLabel",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = InrFormatter.format(item.lineTotalInr),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    QuantityStepper(
                                        quantity = item.entity.quantity,
                                        onDecrease = {
                                            onUpdateQuantity(item.entity.cartKey, item.entity.quantity - 1)
                                        },
                                        onIncrease = {
                                            onUpdateQuantity(item.entity.cartKey, item.entity.quantity + 1)
                                        },
                                        maxQuantity = item.product.stockQuantity.coerceAtLeast(1)
                                    )
                                }
                            }
                        }
                    }
                }

                // Working Demo Coupon Card
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.75.dp, ext.hairlineBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.LocalOffer,
                                    contentDescription = null,
                                    tint = ext.antiqueGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Demo Privilege Code", style = MaterialTheme.typography.titleSmall)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = couponInput,
                                    onValueChange = { couponInput = it.uppercase() },
                                    placeholder = { Text("AURELIA10 or HERITAGE5") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("coupon_input")
                                )
                                if (appliedCouponCode != null) {
                                    IconButton(
                                        onClick = {
                                            onClearCoupon()
                                            couponInput = ""
                                            couponFeedback = null
                                        }
                                    ) {
                                        Icon(Icons.Outlined.Close, contentDescription = "Remove coupon")
                                    }
                                } else {
                                    TextButton(
                                        onClick = {
                                            couponFeedback = onApplyCoupon(couponInput)
                                        },
                                        modifier = Modifier.testTag("apply_coupon_btn")
                                    ) {
                                        Text("Apply", color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            if (couponFeedback != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = couponFeedback!!.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (couponFeedback!!.isValid) ext.success else MaterialTheme.colorScheme.error
                                )
                            } else if (appliedCouponCode != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Applied demo code: $appliedCouponCode",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ext.success
                                )
                            } else {
                                Spacer(modifier = Modifier.height(10.dp))
                                val suggestedCode = if (breakdown.subtotalInr >= 10_000L) "AURELIA10" else "HERITAGE5"
                                val suggestedLabel = if (suggestedCode == "AURELIA10") {
                                    "CLAIM 10% OFFER (AURELIA10)"
                                } else {
                                    "CLAIM 5% OFFER (HERITAGE5)"
                                }
                                GoldShimmerClaimButton(
                                    text = suggestedLabel,
                                    onClick = {
                                        couponInput = suggestedCode
                                        couponFeedback = onApplyCoupon(suggestedCode)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "cart_quick_claim_offer_btn"
                                )
                            }
                        }
                    }
                }

                // Price Summary Breakdown
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.75.dp, ext.hairlineBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SummaryPriceRow("Subtotal", InrFormatter.format(breakdown.subtotalInr))
                            if (breakdown.discountInr > 0L) {
                                SummaryPriceRow(
                                    label = "Demo Privilege (${breakdown.appliedCouponCode})",
                                    value = "-${InrFormatter.format(breakdown.discountInr)}",
                                    highlightColor = ext.success
                                )
                            }
                            SummaryPriceRow(
                                label = "Standard Delivery",
                                value = if (breakdown.shippingInr == 0L) "Free" else InrFormatter.format(breakdown.shippingInr)
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                thickness = 0.75.dp,
                                color = ext.hairlineBorder
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total (Demo)", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = InrFormatter.format(breakdown.grandTotalInr),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = MaterialTheme.typography.titleMedium.fontFamily
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.navigationBarsPadding()) {
                    HorizontalDivider(thickness = 0.75.dp, color = ext.hairlineBorder)
                    PrimaryOliveButton(
                        text = "Proceed to Checkout",
                        leadingIcon = Icons.AutoMirrored.Outlined.ArrowForward,
                        onClick = onProceedToCheckout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        testTag = "proceed_checkout_btn"
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryPriceRow(
    label: String,
    value: String,
    highlightColor: androidx.compose.ui.graphics.Color? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = highlightColor ?: MaterialTheme.colorScheme.onSurface
        )
    }
}
