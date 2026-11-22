package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.AddressEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.StoreConfigEntity
import com.example.domain.CommerceRules
import com.example.domain.InrFormatter
import com.example.ui.components.EmptyLuxuryState
import com.example.ui.components.JewelleryPhoto
import com.example.ui.components.LuxuryTopBar
import com.example.ui.components.PrimaryOliveButton
import com.example.ui.components.SecondaryOutlinedButton
import com.example.ui.theme.LocalAureliaColors
import java.util.UUID

@Composable
fun AccountScreen(
    config: StoreConfigEntity,
    unreadNotifications: Int,
    ordersCount: Int,
    wishlistCount: Int,
    onUpdateProfile: (StoreConfigEntity) -> Unit,
    onOpenOrders: () -> Unit,
    onOpenWishlist: () -> Unit,
    onOpenAddresses: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenGoldRates: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenStudio: () -> Unit,
    onOpenBespokeAi: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenAdminDashboard: () -> Unit = {}
) {
    val ext = LocalAureliaColors.current
    var showEditProfile by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("account_screen")
    ) {
        LuxuryTopBar(title = "My Account")

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Demo Profile Header Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.75.dp, ext.hairlineBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(ext.champagne),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = ext.deepWarmBrown,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(config.customerName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = config.customerEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Demo Customer Profile (Local Device)",
                            style = MaterialTheme.typography.labelSmall,
                            color = ext.antiqueGold
                        )
                    }
                    IconButton(onClick = { showEditProfile = true }) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "Edit demo profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 2x3 Quick Luxury Grid (matches Reference Screen 15)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AccountGridCard(
                        icon = Icons.Outlined.ReceiptLong,
                        title = "My Orders",
                        subtitle = "$ordersCount demo order(s)",
                        onClick = onOpenOrders,
                        modifier = Modifier.weight(1f)
                    )
                    AccountGridCard(
                        icon = Icons.Outlined.FavoriteBorder,
                        title = "Wishlist",
                        subtitle = "$wishlistCount saved",
                        onClick = onOpenWishlist,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AccountGridCard(
                        icon = Icons.Outlined.LocationOn,
                        title = "Addresses",
                        subtitle = "Delivery details",
                        onClick = onOpenAddresses,
                        modifier = Modifier.weight(1f)
                    )
                    AccountGridCard(
                        icon = Icons.Outlined.Notifications,
                        title = "Notifications",
                        subtitle = if (unreadNotifications > 0) "$unreadNotifications unread" else "All caught up",
                        onClick = onOpenNotifications,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AccountGridCard(
                        icon = Icons.Outlined.MenuBook,
                        title = "Jewellery Guide",
                        subtitle = "Karat, care & sizes",
                        onClick = onOpenGuide,
                        modifier = Modifier.weight(1f)
                    )
                    AccountGridCard(
                        icon = Icons.Outlined.ShowChart,
                        title = "Demo Gold Rates",
                        subtitle = "Illustrative chart",
                        onClick = onOpenGoldRates,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Studio & Support Menu
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.75.dp, ext.hairlineBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    AccountListRow(
                        icon = Icons.Outlined.AdminPanelSettings,
                        title = "Admin Dashboard (Separated Console)",
                        subtitle = "Order Management, Product Inventory & User Analytics",
                        onClick = onOpenAdminDashboard,
                        testTag = "account_open_admin_dashboard"
                    )
                    HorizontalDivider(thickness = 0.75.dp, color = ext.hairlineBorder)
                    AccountListRow(
                        icon = Icons.Outlined.Tune,
                        title = "Admin / Design Studio (Demo Mode)",
                        subtitle = "Edit brand name, colors, products & WhatsApp",
                        onClick = onOpenStudio,
                        testTag = "account_open_studio"
                    )
                    HorizontalDivider(thickness = 0.75.dp, color = ext.hairlineBorder)
                    AccountListRow(
                        icon = Icons.Outlined.AutoAwesome,
                        title = "Bespoke AI Jewellery Studio",
                        subtitle = "Concept studio with 1K / 2K / 4K selector",
                        onClick = onOpenBespokeAi,
                        testTag = "account_open_bespoke_ai"
                    )
                    HorizontalDivider(thickness = 0.75.dp, color = ext.hairlineBorder)
                    AccountListRow(
                        icon = Icons.Outlined.SupportAgent,
                        title = "Contact & Concierge Support",
                        subtitle = "WhatsApp, call, email & store address",
                        onClick = onOpenSupport
                    )
                    HorizontalDivider(thickness = 0.75.dp, color = ext.hairlineBorder)
                    AccountListRow(
                        icon = Icons.Outlined.Policy,
                        title = "About Brand, Privacy & Demo Terms",
                        subtitle = "Prototype disclosures & store details",
                        onClick = onOpenAbout
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showEditProfile) {
        var name by remember(config.customerName) { mutableStateOf(config.customerName) }
        var email by remember(config.customerEmail) { mutableStateOf(config.customerEmail) }
        var phone by remember(config.customerPhone) { mutableStateOf(config.customerPhone) }

        AlertDialog(
            onDismissRequest = { showEditProfile = false },
            containerColor = MaterialTheme.colorScheme.background,
            title = { Text("Edit Demo Profile", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it.filter { c -> c.isDigit() }.take(10) },
                        label = { Text("Phone (10 digits)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateProfile(
                            config.copy(
                                customerName = name.trim().ifBlank { config.customerName },
                                customerEmail = email.trim().ifBlank { config.customerEmail },
                                customerPhone = phone.trim().ifBlank { config.customerPhone }
                            )
                        )
                        showEditProfile = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfile = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AccountGridCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalAureliaColors.current
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.75.dp, ext.hairlineBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = ext.antiqueGold, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AccountListRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String? = null
) {
    val ext = LocalAureliaColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .let { if (testTag != null) it.testTag(testTag) else it }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = ext.antiqueGold, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            Icons.AutoMirrored.Outlined.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun NotificationsScreen(
    notifications: List<NotificationEntity>,
    onBack: () -> Unit,
    onMarkRead: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onNavigateTarget: (String) -> Unit
) {
    val ext = LocalAureliaColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .testTag("notifications_screen")
    ) {
        LuxuryTopBar(
            title = "Notifications",
            onBack = onBack,
            actions = {
                if (notifications.any { !it.isRead }) {
                    TextButton(onClick = onMarkAllRead) {
                        Text("Mark All Read", color = ext.antiqueGold)
                    }
                }
            }
        )

        if (notifications.isEmpty()) {
            EmptyLuxuryState(
                icon = Icons.Outlined.NotificationsNone,
                title = "No Notifications",
                subtitle = "Demo order updates and curated collection announcements will appear here.",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (notif.isRead) MaterialTheme.colorScheme.surface else ext.secondaryCream,
                        border = BorderStroke(0.75.dp, ext.hairlineBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                onMarkRead(notif.id)
                                if (notif.targetRoute.isNotBlank()) {
                                    onNavigateTarget(notif.targetRoute)
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ext.champagne,
                                modifier = Modifier.size(48.dp)
                            ) {
                                JewelleryPhoto(
                                    imageKey = notif.imageKey,
                                    contentDescription = notif.title,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = notif.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (!notif.isRead) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = notif.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = InrFormatter.formatDateTime(notif.createdAtMs),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ext.antiqueGold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactSupportScreen(
    config: StoreConfigEntity,
    onBack: () -> Unit
) {
    val ext = LocalAureliaColors.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .testTag("contact_support_screen")
    ) {
        LuxuryTopBar(title = "Contact & Concierge", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Personal Jewellery Assistance",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Reach our configured store concierge for sizing advice, bridal consultations, or demo order inquiries. Contact details can be updated inside Admin Design Studio.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val waDigits = CommerceRules.normalizeWhatsAppNumber(config.whatsappNumber)
            if (waDigits.isNotBlank()) {
                PrimaryOliveButton(
                    text = "Chat on WhatsApp",
                    leadingIcon = Icons.Outlined.Send,
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://wa.me/$waDigits?text=Hello%20${Uri.encode(config.brandName)}")
                        )
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (config.contactPhone.isNotBlank()) {
                SupportActionCard(
                    icon = Icons.Outlined.Call,
                    label = "Store Phone",
                    value = config.contactPhone,
                    onClick = {
                        val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${config.contactPhone}"))
                        try { context.startActivity(dial) } catch (_: Exception) {}
                    }
                )
            }

            if (config.contactEmail.isNotBlank()) {
                SupportActionCard(
                    icon = Icons.Outlined.Email,
                    label = "Concierge Email",
                    value = config.contactEmail,
                    onClick = {
                        val mail = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${config.contactEmail}"))
                        try { context.startActivity(mail) } catch (_: Exception) {}
                    }
                )
            }

            if (config.storeAddress.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.75.dp, ext.hairlineBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Outlined.Store, contentDescription = null, tint = ext.antiqueGold)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Showroom Address (Configurable)", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = config.storeAddress,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportActionCard(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    val ext = LocalAureliaColors.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.75.dp, ext.hairlineBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = ext.antiqueGold)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleSmall)
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun JewelleryGuideScreen(onBack: () -> Unit) {
    val ext = LocalAureliaColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .testTag("jewellery_guide_screen")
    ) {
        LuxuryTopBar(title = "Jewellery Guide", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GuideSectionCard(
                title = "Understanding Gold Karat",
                body = "24K Gold (99.9%) is pure bullion and generally too soft for intricate stone settings. 22K Gold (91.6%) is the classic Indian choice for heritage necklaces, jhumkas, and kadas. 18K Gold (75.0%) offers higher structural strength for contemporary stone-set rings and daily chains."
            )
            GuideSectionCard(
                title = "Silver, Brass & Copper Craft",
                body = "925 Sterling Silver balances 92.5% silver with strengthening alloys for durable anklets, rings, and jhumkas. Artisan Brass and Pure Copper offer sculptural heritage styling and naturally develop a warm patina over time."
            )
            GuideSectionCard(
                title = "Indian Ring Sizing",
                body = "Size 8: 15.3 mm inner diameter\nSize 10: 15.9 mm\nSize 12: 16.5 mm\nSize 14: 17.2 mm\nSize 16: 17.8 mm\nSize 18: 18.4 mm"
            )
            GuideSectionCard(
                title = "Indian Bangle Sizing",
                body = "Size 2-2: 54.0 mm (2.12 in)\nSize 2-4: 57.2 mm (2.25 in)\nSize 2-6: 60.3 mm (2.37 in)\nSize 2-8: 63.5 mm (2.50 in)"
            )
            GuideSectionCard(
                title = "Basic Jewellery Care",
                body = "Apply perfumes, hairspray, and lotions before wearing fine jewellery. Wipe pieces with a dry lint-free cotton cloth after wear, and store gold, pearl, and silver designs in separate soft compartments."
            )
        }
    }
}

@Composable
private fun GuideSectionCard(title: String, body: String) {
    val ext = LocalAureliaColors.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.75.dp, ext.hairlineBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(6.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun GoldRateScreen(onBack: () -> Unit) {
    val ext = LocalAureliaColors.current
    val sampleTrend22K = listOf(7090f, 7120f, 7105f, 7145f, 7160f, 7150f, 7180f)
    val days = listOf("Day 1", "Day 2", "Day 3", "Day 4", "Day 5", "Day 6", "Day 7")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .testTag("gold_rate_screen")
    ) {
        LuxuryTopBar(title = "Demo Gold Rates", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ext.champagne,
                border = BorderStroke(0.75.dp, ext.antiqueGold.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = ext.deepWarmBrown, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Demo Rates — Illustrative sample values only. No live bullion market feed is connected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.deepWarmBrown
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RateTile("24K Gold (1g)", InrFormatter.format(7830), "Demo Sample", Modifier.weight(1f))
                RateTile("22K Gold (1g)", InrFormatter.format(7180), "Demo Sample", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RateTile("18K Gold (1g)", InrFormatter.format(5875), "Demo Sample", Modifier.weight(1f))
                RateTile("925 Silver (10g)", InrFormatter.format(940), "Demo Sample", Modifier.weight(1f))
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.75.dp, ext.hairlineBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Illustrative 7-Point 22K Trend (Demo)", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(14.dp))
                    val lineColor = ext.antiqueGold
                    val dotColor = MaterialTheme.colorScheme.primary
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val minV = sampleTrend22K.minOrNull() ?: 7000f
                        val maxV = sampleTrend22K.maxOrNull() ?: 7200f
                        val span = (maxV - minV).coerceAtLeast(1f)
                        val stepX = size.width / (sampleTrend22K.size - 1).coerceAtLeast(1)
                        val padY = 16f
                        val usableH = size.height - padY * 2

                        val pts = sampleTrend22K.mapIndexed { idx, v ->
                            val x = idx * stepX
                            val norm = (v - minV) / span
                            val y = size.height - padY - norm * usableH
                            Offset(x, y)
                        }
                        val path = Path().apply {
                            pts.forEachIndexed { idx, pt ->
                                if (idx == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 5f, cap = StrokeCap.Round)
                        )
                        pts.forEach { pt ->
                            drawCircle(color = dotColor, radius = 8f, center = pt)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        days.forEach { d ->
                            Text(d, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RateTile(label: String, value: String, note: String, modifier: Modifier = Modifier) {
    val ext = LocalAureliaColors.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.75.dp, ext.hairlineBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(note, style = MaterialTheme.typography.labelSmall, color = ext.antiqueGold)
        }
    }
}

@Composable
fun AddressesScreen(
    addresses: List<AddressEntity>,
    onBack: () -> Unit,
    onSaveAddress: (AddressEntity) -> Unit,
    onDeleteAddress: (String) -> Unit
) {
    val ext = LocalAureliaColors.current
    var showForm by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var line1 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var stateName by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .testTag("addresses_screen")
    ) {
        LuxuryTopBar(
            title = "Delivery Addresses",
            onBack = onBack,
            actions = {
                TextButton(onClick = { showForm = !showForm }) {
                    Text(if (showForm) "Close" else "Add New", color = ext.antiqueGold)
                }
            }
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (showForm) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.75.dp, ext.hairlineBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("New Delivery Address", style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, isError = errors.containsKey("fullName"), singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = phone, onValueChange = { phone = it.filter { c -> c.isDigit() }.take(10) }, label = { Text("10-Digit Mobile") }, isError = errors.containsKey("phone"), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = line1, onValueChange = { line1 = it }, label = { Text("Address Line 1") }, isError = errors.containsKey("line1"), singleLine = true, modifier = Modifier.fillMaxWidth())
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, isError = errors.containsKey("city"), singleLine = true, modifier = Modifier.weight(1f))
                                OutlinedTextField(value = stateName, onValueChange = { stateName = it }, label = { Text("State") }, isError = errors.containsKey("state"), singleLine = true, modifier = Modifier.weight(1f))
                            }
                            OutlinedTextField(value = pinCode, onValueChange = { pinCode = it.filter { c -> c.isDigit() }.take(6) }, label = { Text("6-Digit PIN Code") }, isError = errors.containsKey("pinCode"), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                            if (errors.isNotEmpty()) {
                                Text(errors.values.first(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                            PrimaryOliveButton(
                                text = "Save Address",
                                onClick = {
                                    val res = CommerceRules.validateIndianAddress(fullName, phone, line1, city, stateName, pinCode)
                                    errors = res.errors
                                    if (res.isValid) {
                                        onSaveAddress(
                                            AddressEntity(
                                                id = "addr_${UUID.randomUUID()}",
                                                label = "Saved",
                                                fullName = fullName.trim(),
                                                phone = phone.trim(),
                                                line1 = line1.trim(),
                                                line2 = "",
                                                landmark = "",
                                                city = city.trim(),
                                                state = stateName.trim(),
                                                pinCode = pinCode.trim(),
                                                isDefault = true
                                            )
                                        )
                                        fullName = ""; phone = ""; line1 = ""; city = ""; stateName = ""; pinCode = ""
                                        showForm = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            items(addresses, key = { it.id }) { addr ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.75.dp, ext.hairlineBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${addr.fullName}  (${addr.label})", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${addr.line1}${if (addr.line2.isNotBlank()) ", ${addr.line2}" else ""}, ${addr.city}, ${addr.state} - ${addr.pinCode}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("Mobile: ${addr.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onDeleteAddress(addr.id) }) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete address")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AboutBrandScreen(config: StoreConfigEntity, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .testTag("about_brand_screen")
    ) {
        LuxuryTopBar(title = "About & Demo Policies", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GuideSectionCard(
                title = "${config.brandName} ${config.brandSubName}",
                body = "A luxury Indian fine jewellery storefront demonstration crafted around warm ivory, champagne, antique gold, and deep olive visual restraint. All brand details, products, pricing, and contact channels are configurable in the Design Studio."
            )
            GuideSectionCard(
                title = "Prototype & Payment Notice",
                body = "This application is a local-persistence UI prototype powered by on-device Room SQLite storage. No real payment gateway is active, no card or UPI PIN credentials are collected, and all products, weights, gold rates, and order statuses are illustrative demo records."
            )
            GuideSectionCard(
                title = "Privacy & WhatsApp Sharing",
                body = "Addresses and demo orders are stored strictly on this device. When you tap 'Send Order on WhatsApp', a prefilled message opens in WhatsApp or your share sheet so you can review and explicitly press Send."
            )
        }
    }
}
