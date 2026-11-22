package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.ProductEntity
import com.example.domain.InrFormatter
import com.example.ui.theme.CormorantGaramond
import com.example.ui.theme.LocalAureliaColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@DrawableRes
fun resolveDrawableRes(imageKey: String): Int = when (imageKey) {
    "img_hero_bridal" -> R.drawable.img_hero_bridal
    "img_ring_gold" -> R.drawable.img_ring_gold
    "img_necklace_gold" -> R.drawable.img_necklace_gold
    "img_earrings_jhumka" -> R.drawable.img_earrings_jhumka
    "img_bangle_gold" -> R.drawable.img_bangle_gold
    "ic_aurelia_logo" -> R.drawable.ic_aurelia_logo
    else -> R.drawable.img_ring_gold
}

/**
 * Applies subtle photographic metal grading for non-gold demo finishes while preserving natural studio lighting.
 */
fun metalColorFilter(metal: String): ColorFilter? {
    return when {
        metal.equals("Silver", ignoreCase = true) -> {
            val matrix = ColorMatrix().apply { setToSaturation(0.12f) }
            ColorFilter.colorMatrix(matrix)
        }
        metal.equals("Rose Gold", ignoreCase = true) -> {
            val matrix = ColorMatrix(
                floatArrayOf(
                    1.05f, 0.04f, 0f, 0f, 4f,
                    0f, 0.94f, 0.02f, 0f, 0f,
                    0.02f, 0.02f, 0.92f, 0f, 2f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            ColorFilter.colorMatrix(matrix)
        }
        metal.equals("Copper", ignoreCase = true) -> {
            val matrix = ColorMatrix(
                floatArrayOf(
                    1.08f, 0.05f, 0f, 0f, 2f,
                    0f, 0.86f, 0f, 0f, -4f,
                    0f, 0f, 0.80f, 0f, -6f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            ColorFilter.colorMatrix(matrix)
        }
        metal.equals("Brass", ignoreCase = true) -> {
            val matrix = ColorMatrix(
                floatArrayOf(
                    0.96f, 0.04f, 0f, 0f, -4f,
                    0f, 0.92f, 0f, 0f, -4f,
                    0f, 0f, 0.84f, 0f, -6f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            ColorFilter.colorMatrix(matrix)
        }
        else -> null
    }
}

@Composable
fun JewelleryPhoto(
    imageKey: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    customImageUri: String? = null,
    metal: String = "Gold",
    contentScale: ContentScale = ContentScale.Crop
) {
    val filter = if (customImageUri.isNullOrBlank()) metalColorFilter(metal) else null
    if (!customImageUri.isNullOrBlank()) {
        AsyncImage(
            model = customImageUri,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            error = painterResource(id = resolveDrawableRes(imageKey)),
            placeholder = painterResource(id = resolveDrawableRes(imageKey))
        )
    } else {
        Image(
            painter = painterResource(id = resolveDrawableRes(imageKey)),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            colorFilter = filter
        )
    }
}

@Composable
fun AureliaMonogramEmblem(
    modifier: Modifier = Modifier,
    color: Color = LocalAureliaColors.current.antiqueGold
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerR = size.minDimension * 0.44f
        val innerR = size.minDimension * 0.16f
        val strokeWidth = size.minDimension * 0.035f
        val petalCount = 8
        for (i in 0 until petalCount) {
            val angle = (2.0 * PI * i / petalCount)
            val tip = Offset(
                center.x + (outerR * cos(angle)).toFloat(),
                center.y + (outerR * sin(angle)).toFloat()
            )
            val mid = Offset(
                center.x + (outerR * 0.56f * cos(angle)).toFloat(),
                center.y + (outerR * 0.56f * sin(angle)).toFloat()
            )
            drawCircle(
                color = color,
                radius = outerR * 0.24f,
                center = mid,
                style = Stroke(width = strokeWidth)
            )
            drawLine(
                color = color.copy(alpha = 0.7f),
                start = center,
                end = tip,
                strokeWidth = strokeWidth * 0.65f,
                cap = StrokeCap.Round
            )
        }
        drawCircle(
            color = color,
            radius = innerR,
            center = center,
            style = Stroke(width = strokeWidth)
        )
    }
}

@Composable
fun AureliaWordmark(
    brandName: String,
    brandSubName: String,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    subColor: Color = LocalAureliaColors.current.antiqueGold,
    large: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = brandName.uppercase(),
            fontFamily = CormorantGaramond,
            fontWeight = FontWeight.SemiBold,
            fontSize = if (large) 30.sp else 21.sp,
            letterSpacing = if (large) 5.sp else 3.2.sp,
            color = titleColor,
            maxLines = 1
        )
        if (brandSubName.isNotBlank()) {
            Text(
                text = brandSubName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontSize = if (large) 11.sp else 9.sp,
                letterSpacing = if (large) 4.sp else 3.sp,
                color = subColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun LuxuryTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    subtitle: String? = null,
    actions: @Composable () -> Unit = {}
) {
    val ext = LocalAureliaColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("top_bar_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Navigate back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                actions()
            }
        }
        HorizontalDivider(thickness = 0.75.dp, color = ext.hairlineBorder)
    }
}

@Composable
fun LuxuryProductCard(
    product: ProductEntity,
    isWishlisted: Boolean,
    onClick: () -> Unit,
    onToggleWishlist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalAureliaColors.current
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.75.dp, ext.hairlineBorder)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
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

                if (product.stockQuantity <= 0) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f)
                    ) {
                        Text(
                            text = "Out of Stock",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                } else if (product.isNewArrival) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        border = BorderStroke(0.5.dp, ext.hairlineBorder)
                    ) {
                        Text(
                            text = "New",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.purity.ifBlank { product.metal },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = InrFormatter.format(product.priceInr),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        if (product.originalPriceInr != null && product.originalPriceInr > product.priceInr) {
                            Text(
                                text = InrFormatter.format(product.originalPriceInr),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    textDecoration = TextDecoration.LineThrough,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleWishlist()
                            }
                            .testTag("wishlist_btn_${product.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isWishlisted) "Remove from wishlist" else "Save to wishlist",
                            tint = if (isWishlisted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrimaryOliveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    testTag: String? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .height(50.dp)
            .let { if (testTag != null) it.testTag(testTag) else it },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        ),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun SecondaryOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    testTag: String? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(50.dp)
            .let { if (testTag != null) it.testTag(testTag) else it },
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 0.7f else 0.3f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        ),
        contentPadding = PaddingValues(horizontal = 18.dp)
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun QuantityStepper(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    maxQuantity: Int = 99
) {
    val ext = LocalAureliaColors.current
    Row(
        modifier = modifier
            .border(1.dp, ext.hairlineBorder, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clickable(onClick = onDecrease)
                .testTag("qty_decrease"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Remove,
                contentDescription = "Decrease quantity",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .clickable(enabled = quantity < maxQuantity, onClick = onIncrease)
                .testTag("qty_increase"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Increase quantity",
                modifier = Modifier.size(16.dp),
                tint = if (quantity < maxQuantity) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun EmptyLuxuryState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val ext = LocalAureliaColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(ext.secondaryCream),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ext.antiqueGold,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (!actionLabel.isNullOrBlank() && onAction != null) {
            Spacer(modifier = Modifier.height(22.dp))
            PrimaryOliveButton(
                text = actionLabel,
                onClick = onAction,
                testTag = "empty_state_cta"
            )
        }
    }
}

enum class MainTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    SHOP("categories", "Shop", Icons.Filled.Storefront, Icons.Outlined.Storefront),
    WISHLIST("wishlist", "Wishlist", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    ORDERS("orders", "Orders", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    ACCOUNT("account", "Account", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun AureliaBottomBar(
    currentRoute: String?,
    wishlistCount: Int,
    onSelectTab: (MainTab) -> Unit
) {
    val ext = LocalAureliaColors.current
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            HorizontalDivider(thickness = 0.75.dp, color = ext.hairlineBorder)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MainTab.entries.forEach { tab ->
                    val selected = currentRoute == tab.route
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectTab(tab) }
                            .testTag("tab_${tab.route}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(21.dp),
                                tint = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (tab == MainTab.WISHLIST && wishlistCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(ext.antiqueGold)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 10.5.sp
                            ),
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeaderRow(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    horizontalPadding: Dp = 16.dp
) {
    val ext = LocalAureliaColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (!actionText.isNullOrBlank() && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelMedium,
                color = ext.antiqueGold,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}
