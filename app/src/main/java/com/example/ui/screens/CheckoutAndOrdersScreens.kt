package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.OrderSnapshotCodec
import com.example.data.local.AddressEntity
import com.example.data.local.OrderEntity
import com.example.data.local.StoreConfigEntity
import com.example.domain.CommerceRules
import com.example.domain.InrFormatter
import com.example.ui.CartDisplayItem
import com.example.ui.components.EmptyLuxuryState
import com.example.ui.components.JewelleryPhoto
import com.example.ui.components.LuxuryTopBar
import com.example.ui.components.PrimaryOliveButton
import com.example.ui.components.SecondaryOutlinedButton
import com.example.ui.theme.LocalAureliaColors
import java.net.URLEncoder
import java.util.UUID

fun shareOrderOnWhatsApp(
    context: Context,
    config: StoreConfigEntity,
    order: OrderEntity
) {
    val items = OrderSnapshotCodec.decode(order.itemsSnapshotSerialized)
    val message = CommerceRules.composeWhatsAppOrderMessage(
        brandName = "${config.brandName} ${config.brandSubName}".trim(),
        orderId = order.orderId,
        customerName = order.customerName,
        customerPhone = order.customerPhone,
        deliveryAddressSummary = order.deliveryAddressSnapshot,
        items = items,
        grandTotalInr = order.grandTotalInr,
        paymentMethod = order.paymentMethod
    )
    val encoded = URLEncoder.encode(message, "UTF-8")
    val phoneDigits = CommerceRules.normalizeWhatsAppNumber(config.whatsappNumber)
    val waUrl = if (phoneDigits.isNotBlank()) {
        "https://wa.me/$phoneDigits?text=$encoded"
    } else {
        "https://wa.me/?text=$encoded"
    }
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
        context.startActivity(intent)
    } catch (_: Exception) {
        val fallback = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(fallback, "Share Demo Order"))
    }
}

@Composable
fun CheckoutScreen(
    cartItems: List<CartDisplayItem>,
    addresses: List<AddressEntity>,
    config: StoreConfigEntity,
    appliedCouponCode: String?,
    isExpressDelivery: Boolean,
    isPlacingOrder: Boolean,
    onBack: () -> Unit,
    onSetExpressDelivery: (Boolean) -> Unit,
    onSaveAddress: (AddressEntity) -> Unit,
    onPlaceDemoOrder: (AddressEntity, String) -> Unit
) {
    val ext = LocalAureliaColors.current
    var currentStep by remember { mutableIntStateOf(0) } // 0=Address, 1=Payment, 2=Review
    var selectedAddressId by remember(addresses) {
        mutableStateOf(addresses.firstOrNull { it.isDefault }?.id ?: addresses.firstOrNull()?.id)
    }
    var showNewAddressForm by remember { mutableStateOf(addresses.isEmpty()) }

    // Form state (preserved on validation error)
    var fullName by remember { mutableStateOf(config.customerName) }
    var phone by remember { mutableStateOf(config.customerPhone) }
    var line1 by remember { mutableStateOf("") }
    var line2 by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var stateName by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    var fieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    var selectedPayment by remember { mutableStateOf("UPI (Demo)") }

    val shippingCharge = if (isExpressDelivery) config.expressShippingInr else config.standardShippingInr
    val breakdown = remember(cartItems, appliedCouponCode, shippingCharge) {
        CommerceRules.calculateTotals(
            itemTotalsInr = cartItems.map { it.lineTotalInr },
            couponCode = appliedCouponCode,
            shippingChargeInr = shippingCharge
        )
    }

    val activeAddress = addresses.firstOrNull { it.id == selectedAddressId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("checkout_screen")
    ) {
        LuxuryTopBar(
            title = when (currentStep) {
                0 -> "Checkout"
                1 -> "Demo Payment"
                else -> "Review Demo Order"
            },
            onBack = {
                if (currentStep > 0) currentStep-- else onBack()
            }
        )

        // 3-Step Indicator (matches Reference Screen 10)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepBadge(number = "1", label = "Address", active = currentStep >= 0)
            HorizontalDivider(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                thickness = 1.dp,
                color = if (currentStep >= 1) MaterialTheme.colorScheme.primary else ext.hairlineBorder
            )
            StepBadge(number = "2", label = "Payment", active = currentStep >= 1)
            HorizontalDivider(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                thickness = 1.dp,
                color = if (currentStep >= 2) MaterialTheme.colorScheme.primary else ext.hairlineBorder
            )
            StepBadge(number = "3", label = "Review", active = currentStep >= 2)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (currentStep) {
                0 -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Delivery Address", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { showNewAddressForm = !showNewAddressForm }) {
                            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showNewAddressForm) "Saved Addresses" else "Add New")
                        }
                    }

                    if (!showNewAddressForm && addresses.isNotEmpty()) {
                        addresses.forEach { addr ->
                            val selected = addr.id == selectedAddressId
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    if (selected) 1.5.dp else 0.75.dp,
                                    if (selected) MaterialTheme.colorScheme.primary else ext.hairlineBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedAddressId = addr.id }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    RadioButton(
                                        selected = selected,
                                        onClick = { selectedAddressId = addr.id }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "${addr.fullName}  (${addr.label})",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "${addr.line1}${if (addr.line2.isNotBlank()) ", ${addr.line2}" else ""}, ${addr.city}, ${addr.state} - ${addr.pinCode}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Mobile: ${addr.phone}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    } else {
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
                                Text("Guest Delivery Details", style = MaterialTheme.typography.titleSmall)
                                ValidatedField("Full Name", fullName, { fullName = it }, fieldErrors["fullName"])
                                ValidatedField(
                                    label = "10-Digit Mobile Number",
                                    value = phone,
                                    onValueChange = { phone = it.filter { c -> c.isDigit() }.take(10) },
                                    error = fieldErrors["phone"],
                                    keyboardType = KeyboardType.Phone
                                )
                                ValidatedField("Address Line 1", line1, { line1 = it }, fieldErrors["line1"])
                                ValidatedField("Address Line 2 (Optional)", line2, { line2 = it }, null)
                                ValidatedField("Landmark (Optional)", landmark, { landmark = it }, null)
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    ValidatedField(
                                        label = "City",
                                        value = city,
                                        onValueChange = { city = it },
                                        error = fieldErrors["city"],
                                        modifier = Modifier.weight(1f)
                                    )
                                    ValidatedField(
                                        label = "State",
                                        value = stateName,
                                        onValueChange = { stateName = it },
                                        error = fieldErrors["state"],
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                ValidatedField(
                                    label = "6-Digit PIN Code",
                                    value = pinCode,
                                    onValueChange = { pinCode = it.filter { c -> c.isDigit() }.take(6) },
                                    error = fieldErrors["pinCode"],
                                    keyboardType = KeyboardType.Number
                                )
                                SecondaryOutlinedButton(
                                    text = "Save Address",
                                    onClick = {
                                        val check = CommerceRules.validateIndianAddress(
                                            fullName, phone, line1, city, stateName, pinCode
                                        )
                                        fieldErrors = check.errors
                                        if (check.isValid) {
                                            val newAddr = AddressEntity(
                                                id = "addr_${UUID.randomUUID()}",
                                                label = "Delivery",
                                                fullName = fullName.trim(),
                                                phone = phone.trim(),
                                                line1 = line1.trim(),
                                                line2 = line2.trim(),
                                                landmark = landmark.trim(),
                                                city = city.trim(),
                                                state = stateName.trim(),
                                                pinCode = pinCode.trim(),
                                                isDefault = true
                                            )
                                            onSaveAddress(newAddr)
                                            selectedAddressId = newAddr.id
                                            showNewAddressForm = false
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "save_address_btn"
                                )
                            }
                        }
                    }

                    // Delivery Options
                    Text("Delivery Options (Demo)", style = MaterialTheme.typography.titleMedium)
                    DeliveryOptionCard(
                        title = "Standard Demo Delivery",
                        subtitle = "Estimated 3 - 5 business days (illustrative)",
                        priceLabel = if (config.standardShippingInr == 0L) "Free" else InrFormatter.format(config.standardShippingInr),
                        selected = !isExpressDelivery,
                        onSelect = { onSetExpressDelivery(false) }
                    )
                    DeliveryOptionCard(
                        title = "Express Demo Delivery",
                        subtitle = "Estimated 1 - 2 business days (illustrative)",
                        priceLabel = InrFormatter.format(config.expressShippingInr),
                        selected = isExpressDelivery,
                        onSelect = { onSetExpressDelivery(true) }
                    )
                }

                1 -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ext.champagne,
                        border = BorderStroke(0.75.dp, ext.antiqueGold.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Info, contentDescription = null, tint = ext.deepWarmBrown, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Demo checkout. No payment will be collected and no card, CVV, or UPI PIN details are requested.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ext.deepWarmBrown
                            )
                        }
                    }

                    Text("Select Payment Method (Demo)", style = MaterialTheme.typography.titleMedium)

                    val methods = listOf(
                        Triple("UPI (Demo)", "Simulated UPI selection — no PIN requested", Icons.Outlined.Smartphone),
                        Triple("Debit / Credit Card (Demo)", "Simulated card selection — no card number collected", Icons.Outlined.CreditCard),
                        Triple("Net Banking (Demo)", "Simulated bank transfer selection", Icons.Outlined.AccountBalance),
                        Triple("Cash on Delivery (Demo)", "Pay at delivery in live deployments", Icons.Outlined.Payments)
                    )

                    methods.forEach { (title, sub, icon) ->
                        val selected = selectedPayment == title
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                if (selected) 1.5.dp else 0.75.dp,
                                if (selected) MaterialTheme.colorScheme.primary else ext.hairlineBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedPayment = title }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selected, onClick = { selectedPayment = title })
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(icon, contentDescription = null, tint = ext.antiqueGold, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(title, style = MaterialTheme.typography.titleSmall)
                                    Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.75.dp, ext.hairlineBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Delivery To", style = MaterialTheme.typography.labelMedium, color = ext.antiqueGold)
                            Text(activeAddress?.fullName.orEmpty(), style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = activeAddress?.let {
                                    "${it.line1}${if (it.line2.isNotBlank()) ", ${it.line2}" else ""}, ${it.city}, ${it.state} - ${it.pinCode}"
                                }.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("Payment Mode: $selectedPayment", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.75.dp, ext.hairlineBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Items (${cartItems.sumOf { it.entity.quantity }})", style = MaterialTheme.typography.titleSmall)
                            cartItems.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${item.product.name} x${item.entity.quantity}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = InrFormatter.format(item.lineTotalInr),
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                            }
                            HorizontalDivider(thickness = 0.75.dp, color = ext.hairlineBorder)
                            SummaryPriceRow("Subtotal", InrFormatter.format(breakdown.subtotalInr))
                            if (breakdown.discountInr > 0L) {
                                SummaryPriceRow("Discount", "-${InrFormatter.format(breakdown.discountInr)}", ext.success)
                            }
                            SummaryPriceRow(
                                "Delivery",
                                if (breakdown.shippingInr == 0L) "Free" else InrFormatter.format(breakdown.shippingInr)
                            )
                            HorizontalDivider(thickness = 0.75.dp, color = ext.hairlineBorder)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Demo Order Total", style = MaterialTheme.typography.titleMedium)
                                Text(InrFormatter.format(breakdown.grandTotalInr), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }

                    Text(
                        text = "Demo checkout. No payment will be collected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Bottom CTA
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.navigationBarsPadding()) {
                HorizontalDivider(thickness = 0.75.dp, color = ext.hairlineBorder)
                PrimaryOliveButton(
                    text = when (currentStep) {
                        0 -> "Continue to Payment"
                        1 -> "Review Demo Order"
                        else -> "Place Demo Order  (${InrFormatter.format(breakdown.grandTotalInr)})"
                    },
                    loading = isPlacingOrder,
                    enabled = cartItems.isNotEmpty() && (currentStep != 0 || activeAddress != null),
                    onClick = {
                        when (currentStep) {
                            0 -> if (activeAddress != null) currentStep = 1
                            1 -> currentStep = 2
                            2 -> if (activeAddress != null) {
                                onPlaceDemoOrder(activeAddress, selectedPayment)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    testTag = "checkout_primary_btn"
                )
            }
        }
    }
}

@Composable
private fun StepBadge(number: String, label: String, active: Boolean) {
    val ext = LocalAureliaColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (active) MaterialTheme.colorScheme.primary else ext.secondaryCream),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelMedium,
                color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DeliveryOptionCard(
    title: String,
    subtitle: String,
    priceLabel: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val ext = LocalAureliaColors.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (selected) 1.5.dp else 0.75.dp,
            if (selected) MaterialTheme.colorScheme.primary else ext.hairlineBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onSelect)
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(priceLabel, style = MaterialTheme.typography.titleSmall, color = ext.antiqueGold)
        }
    }
}

@Composable
private fun ValidatedField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = !error.isNullOrBlank(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )
        if (!error.isNullOrBlank()) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
            )
        }
    }
}

@Composable
fun OrderSuccessScreen(
    order: OrderEntity?,
    config: StoreConfigEntity,
    onTrackOrder: (String) -> Unit,
    onContinueShopping: () -> Unit
) {
    val ext = LocalAureliaColors.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("order_success_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(36.dp))
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(ext.success.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = "Demo order placed",
                tint = ext.success,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text("Demo Order Placed", style = MaterialTheme.typography.displayMedium)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Order #${order?.orderId.orEmpty()} recorded locally. No online payment was charged.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (order != null) {
            Spacer(modifier = Modifier.height(22.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.75.dp, ext.hairlineBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryPriceRow("Order ID", "#${order.orderId}")
                    SummaryPriceRow("Demo Status", order.status)
                    SummaryPriceRow("Payment Mode", order.paymentMethod)
                    SummaryPriceRow("Total", InrFormatter.format(order.grandTotalInr))
                    HorizontalDivider(thickness = 0.75.dp, color = ext.hairlineBorder)
                    Text("Delivery Address", style = MaterialTheme.typography.labelMedium, color = ext.antiqueGold)
                    Text(order.deliveryAddressSnapshot, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            PrimaryOliveButton(
                text = "Send Order on WhatsApp",
                leadingIcon = Icons.Outlined.Send,
                onClick = { shareOrderOnWhatsApp(context, config, order) },
                modifier = Modifier.fillMaxWidth(),
                testTag = "order_success_whatsapp_btn"
            )
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryOutlinedButton(
                text = "Track Demo Order",
                leadingIcon = Icons.Outlined.LocalShipping,
                onClick = { onTrackOrder(order.orderId) },
                modifier = Modifier.fillMaxWidth(),
                testTag = "order_success_track_btn"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        TextButton(
            onClick = onContinueShopping,
            modifier = Modifier.testTag("order_success_continue_btn")
        ) {
            Text("Continue Shopping", color = ext.antiqueGold)
        }
    }
}

@Composable
fun OrdersScreen(
    orders: List<OrderEntity>,
    onOpenOrderTracking: (String) -> Unit,
    onReorder: (OrderEntity) -> Unit,
    onExploreShop: () -> Unit
) {
    val ext = LocalAureliaColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("orders_screen")
    ) {
        LuxuryTopBar(
            title = "My Orders",
            subtitle = "${orders.size} recorded demo order(s)"
        )

        if (orders.isEmpty()) {
            EmptyLuxuryState(
                icon = Icons.Outlined.ReceiptLong,
                title = "No Demo Orders Yet",
                subtitle = "Orders you place through demo checkout are stored persistently here for tracking and WhatsApp sharing.",
                actionLabel = "Start Shopping",
                onAction = onExploreShop,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(orders, key = { it.orderId }) { order ->
                    val items = remember(order.itemsSnapshotSerialized) {
                        OrderSnapshotCodec.decode(order.itemsSnapshotSerialized)
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.75.dp, ext.hairlineBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onOpenOrderTracking(order.orderId) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Order #${order.orderId}", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = InrFormatter.formatDateTime(order.createdAtMs),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ext.champagne
                                ) {
                                    Text(
                                        text = order.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ext.deepWarmBrown,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ext.secondaryCream,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    JewelleryPhoto(
                                        imageKey = items.firstOrNull()?.imageKey ?: "img_ring_gold",
                                        contentDescription = "Order item",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = items.firstOrNull()?.productName ?: "Jewellery Order",
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${items.sumOf { it.quantity }} piece(s)  |  ${InrFormatter.format(order.grandTotalInr)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                SecondaryOutlinedButton(
                                    text = "Track Order",
                                    onClick = { onOpenOrderTracking(order.orderId) },
                                    modifier = Modifier.weight(1f)
                                )
                                PrimaryOliveButton(
                                    text = "Reorder",
                                    onClick = { onReorder(order) },
                                    modifier = Modifier.weight(1f)
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
fun OrderTrackingScreen(
    order: OrderEntity?,
    config: StoreConfigEntity,
    onBack: () -> Unit,
    onAdvanceDemoStatus: (String) -> Unit
) {
    val ext = LocalAureliaColors.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("order_tracking_screen")
    ) {
        LuxuryTopBar(title = "Order Tracking", onBack = onBack)

        if (order == null) {
            EmptyLuxuryState(
                icon = Icons.Outlined.ReceiptLong,
                title = "Order Not Found",
                subtitle = "This demo order record could not be located.",
                actionLabel = "Back to Orders",
                onAction = onBack
            )
            return
        }

        val items = remember(order.itemsSnapshotSerialized) {
            OrderSnapshotCodec.decode(order.itemsSnapshotSerialized)
        }
        val stages = listOf("Placed", "Processing", "Shipped", "Out for Delivery", "Delivered")
        val currentIdx = stages.indexOf(order.status).coerceAtLeast(0)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.75.dp, ext.hairlineBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ext.secondaryCream,
                        modifier = Modifier.size(56.dp)
                    ) {
                        JewelleryPhoto(
                            imageKey = items.firstOrNull()?.imageKey ?: "img_ring_gold",
                            contentDescription = "Order item",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Order #${order.orderId}", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Placed on ${InrFormatter.formatDateShort(order.createdAtMs)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Demo tracking state — no live courier connected",
                            style = MaterialTheme.typography.labelSmall,
                            color = ext.antiqueGold
                        )
                    }
                }
            }

            // Vertical Luxury Progress Timeline
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.75.dp, ext.hairlineBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Demo Progress Timeline", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(14.dp))
                    stages.forEachIndexed { idx, stage ->
                        val completed = idx <= currentIdx
                        Row(verticalAlignment = Alignment.Top) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (completed) MaterialTheme.colorScheme.primary else ext.secondaryCream
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (completed) {
                                        Icon(
                                            Icons.Outlined.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                if (idx < stages.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(30.dp)
                                            .background(
                                                if (idx < currentIdx) MaterialTheme.colorScheme.primary else ext.hairlineBorder
                                            )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.padding(top = 2.dp)) {
                                Text(
                                    text = stage,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (completed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = when {
                                        idx == currentIdx -> "Current demo stage"
                                        idx < currentIdx -> "Completed (demo)"
                                        else -> "Pending"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Order Items Snapshot
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.75.dp, ext.hairlineBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Order Snapshot", style = MaterialTheme.typography.titleSmall)
                    items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.productName} (${item.selectedVariant}) x${item.quantity}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text(InrFormatter.format(item.lineTotalInr), style = MaterialTheme.typography.titleSmall)
                        }
                    }
                    HorizontalDivider(thickness = 0.75.dp, color = ext.hairlineBorder)
                    SummaryPriceRow("Grand Total", InrFormatter.format(order.grandTotalInr))
                    Text("Delivering to: ${order.deliveryAddressSnapshot}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            PrimaryOliveButton(
                text = "Send Order on WhatsApp",
                leadingIcon = Icons.Outlined.Send,
                onClick = { shareOrderOnWhatsApp(context, config, order) },
                modifier = Modifier.fillMaxWidth()
            )

            if (currentIdx < stages.lastIndex) {
                SecondaryOutlinedButton(
                    text = "Advance Demo Status to ${stages[currentIdx + 1]}",
                    onClick = { onAdvanceDemoStatus(order.orderId) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "advance_demo_status_btn"
                )
            }
        }
    }
}
