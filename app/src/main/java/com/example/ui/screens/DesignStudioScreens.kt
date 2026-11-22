package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.ImageResolution
import com.example.data.SeedData
import com.example.data.local.ProductEntity
import com.example.data.local.StoreConfigEntity
import com.example.domain.InrFormatter
import com.example.ui.BespokeAiUiState
import com.example.ui.components.JewelleryPhoto
import com.example.ui.components.LuxuryTopBar
import com.example.ui.components.PrimaryOliveButton
import com.example.ui.components.SecondaryOutlinedButton
import com.example.ui.theme.LocalAureliaColors
import com.example.ui.theme.isValidHexColor
import com.example.ui.theme.parseHexColorOrDefault
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminDesignStudioScreen(
    config: StoreConfigEntity,
    products: List<ProductEntity>,
    onBack: () -> Unit,
    onSaveConfig: (StoreConfigEntity) -> Unit,
    onUpsertProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onResetDemo: () -> Unit,
    onOpenBespokeAi: () -> Unit
) {
    val ext = LocalAureliaColors.current
    var activeTab by remember { mutableIntStateOf(0) } // 0=Brand & Theme, 1=Catalogue CRUD
    var showResetConfirm by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var showCreateProduct by remember { mutableStateOf(false) }
    var deletingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .testTag("admin_studio_screen")
    ) {
        LuxuryTopBar(
            title = "Design Studio (Demo)",
            subtitle = "Local storefront customization — not production RBAC",
            onBack = onBack,
            actions = {
                IconButton(onClick = { showResetConfirm = true }) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Reset demo defaults")
                }
            }
        )

        TabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Brand & Theme") }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Catalogue (${products.size})") }
            )
        }

        if (activeTab == 0) {
            var brandName by remember(config) { mutableStateOf(config.brandName) }
            var brandSub by remember(config) { mutableStateOf(config.brandSubName) }
            var tagline by remember(config) { mutableStateOf(config.tagline) }
            var heroTitle by remember(config) { mutableStateOf(config.heroTitle) }
            var heroSubtitle by remember(config) { mutableStateOf(config.heroSubtitle) }
            var primaryHex by remember(config) { mutableStateOf(config.primaryColorHex) }
            var accentHex by remember(config) { mutableStateOf(config.accentColorHex) }
            var whatsapp by remember(config) { mutableStateOf(config.whatsappNumber) }
            var phone by remember(config) { mutableStateOf(config.contactPhone) }
            var email by remember(config) { mutableStateOf(config.contactEmail) }
            var address by remember(config) { mutableStateOf(config.storeAddress) }
            var stdShipping by remember(config) { mutableStateOf(config.standardShippingInr.toString()) }
            var expShipping by remember(config) { mutableStateOf(config.expressShippingInr.toString()) }
            var colorError by remember { mutableStateOf<String?>(null) }

            val curatedPrimarySwatches = listOf(
                "#27351A" to "Deep Olive",
                "#36271F" to "Warm Espresso",
                "#1F3B34" to "Emerald Heritage",
                "#3B2A26" to "Bronze Umber"
            )
            val curatedAccentSwatches = listOf(
                "#AD853C" to "Antique Gold",
                "#9A6B2F" to "Temple Brass",
                "#A36D5C" to "Muted Rose",
                "#7D6744" to "Champagne Bronze"
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ext.champagne,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = ext.deepWarmBrown, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Need custom jewellery imagery? Launch Bespoke AI Concept Studio (1K / 2K / 4K).",
                            style = MaterialTheme.typography.bodySmall,
                            color = ext.deepWarmBrown,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onOpenBespokeAi) {
                            Text("Open")
                        }
                    }
                }

                Text("Brand Identity", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(value = brandName, onValueChange = { brandName = it }, label = { Text("Brand Display Name") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("studio_brand_name"))
                OutlinedTextField(value = brandSub, onValueChange = { brandSub = it }, label = { Text("Supporting Wordmark") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = tagline, onValueChange = { tagline = it }, label = { Text("Brand Tagline") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = heroTitle, onValueChange = { heroTitle = it }, label = { Text("Home Hero Headline") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = heroSubtitle, onValueChange = { heroSubtitle = it }, label = { Text("Home Hero Subtitle") }, modifier = Modifier.fillMaxWidth())

                HorizontalDivider(thickness = 0.75.dp, color = ext.hairlineBorder)
                Text("Luxury Palette (Contrast-Safe)", style = MaterialTheme.typography.titleMedium)
                Text("Primary Accent Presets", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    curatedPrimarySwatches.forEach { (hex, label) ->
                        SwatchChip(hex = hex, label = label, selected = primaryHex.equals(hex, true)) {
                            primaryHex = hex
                            colorError = null
                        }
                    }
                }
                OutlinedTextField(value = primaryHex, onValueChange = { primaryHex = it }, label = { Text("Primary Hex (#RRGGBB)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                Text("Secondary Gold / Accent Presets", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    curatedAccentSwatches.forEach { (hex, label) ->
                        SwatchChip(hex = hex, label = label, selected = accentHex.equals(hex, true)) {
                            accentHex = hex
                            colorError = null
                        }
                    }
                }
                OutlinedTextField(value = accentHex, onValueChange = { accentHex = it }, label = { Text("Accent Hex (#RRGGBB)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                if (!colorError.isNullOrBlank()) {
                    Text(colorError!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }

                HorizontalDivider(thickness = 0.75.dp, color = ext.hairlineBorder)
                Text("Concierge & Shipping", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("WhatsApp Business Number (with 91)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Contact Phone") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Contact Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Store Address") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = stdShipping, onValueChange = { stdShipping = it.filter { c -> c.isDigit() } }, label = { Text("Standard Shipping (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = expShipping, onValueChange = { expShipping = it.filter { c -> c.isDigit() } }, label = { Text("Express Shipping (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(6.dp))
                PrimaryOliveButton(
                    text = "Save Store Configuration",
                    onClick = {
                        if (!isValidHexColor(primaryHex) || !isValidHexColor(accentHex)) {
                            colorError = "Enter valid 6-digit hex color values (e.g. #27351A)"
                        } else {
                            colorError = null
                            onSaveConfig(
                                config.copy(
                                    brandName = brandName.trim().ifBlank { "AURELIA" },
                                    brandSubName = brandSub.trim(),
                                    tagline = tagline.trim().ifBlank { config.tagline },
                                    heroTitle = heroTitle.trim().ifBlank { config.heroTitle },
                                    heroSubtitle = heroSubtitle.trim(),
                                    primaryColorHex = primaryHex.trim(),
                                    accentColorHex = accentHex.trim(),
                                    whatsappNumber = whatsapp.trim(),
                                    contactPhone = phone.trim(),
                                    contactEmail = email.trim(),
                                    storeAddress = address.trim(),
                                    standardShippingInr = stdShipping.toLongOrNull() ?: 0L,
                                    expressShippingInr = expShipping.toLongOrNull() ?: 149L
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "studio_save_config_btn"
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        } else {
            // Catalogue CRUD
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Demo Products", style = MaterialTheme.typography.titleMedium)
                    PrimaryOliveButton(
                        text = "Add Piece",
                        leadingIcon = Icons.Outlined.Add,
                        onClick = { showCreateProduct = true },
                        testTag = "studio_add_product_btn"
                    )
                }

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(products, key = { it.id }) { prod ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.75.dp, ext.hairlineBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ext.secondaryCream,
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    JewelleryPhoto(
                                        imageKey = prod.imageKey,
                                        customImageUri = prod.customImageUri,
                                        metal = prod.metal,
                                        contentDescription = prod.name,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.name, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                    Text(
                                        text = "${prod.category}  |  ${InrFormatter.format(prod.priceInr)}  |  Stock ${prod.stockQuantity}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { editingProduct = prod }) {
                                    Icon(Icons.Outlined.Edit, contentDescription = "Edit ${prod.name}")
                                }
                                IconButton(onClick = { deletingProduct = prod }) {
                                    Icon(
                                        Icons.Outlined.DeleteOutline,
                                        contentDescription = "Delete ${prod.name}",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            containerColor = MaterialTheme.colorScheme.background,
            title = { Text("Reset Demo Store?", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("This restores the default Aurelia Jewels branding, colors, and seeded demo products.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetDemo()
                        showResetConfirm = false
                    }
                ) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (deletingProduct != null) {
        val target = deletingProduct!!
        AlertDialog(
            onDismissRequest = { deletingProduct = null },
            containerColor = MaterialTheme.colorScheme.background,
            title = { Text("Delete Product?", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("Remove \"${target.name}\" from the demo catalogue? It will also be removed from active bags and wishlists.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProduct(target.id)
                        deletingProduct = null
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deletingProduct = null }) { Text("Cancel") }
            }
        )
    }

    if (showCreateProduct || editingProduct != null) {
        ProductEditorDialog(
            existing = editingProduct,
            onDismiss = {
                showCreateProduct = false
                editingProduct = null
            },
            onSave = { updated ->
                onUpsertProduct(updated)
                showCreateProduct = false
                editingProduct = null
            }
        )
    }
}

@Composable
private fun SwatchChip(
    hex: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val ext = LocalAureliaColors.current
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) ext.secondaryCream else MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (selected) 1.5.dp else 0.75.dp, if (selected) MaterialTheme.colorScheme.primary else ext.hairlineBorder),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(parseHexColorOrDefault(hex, Color.Black))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductEditorDialog(
    existing: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var name by remember(existing) { mutableStateOf(existing?.name.orEmpty()) }
    var description by remember(existing) { mutableStateOf(existing?.description.orEmpty()) }
    var category by remember(existing) { mutableStateOf(existing?.category ?: "Rings") }
    var metal by remember(existing) { mutableStateOf(existing?.metal ?: "Gold") }
    var purity by remember(existing) { mutableStateOf(existing?.purity ?: "22K Gold") }
    var weight by remember(existing) { mutableStateOf(existing?.weightGrams ?: "5.0g (Approx.)") }
    var priceStr by remember(existing) { mutableStateOf((existing?.priceInr ?: 35000L).toString()) }
    var stockStr by remember(existing) { mutableStateOf((existing?.stockQuantity ?: 5).toString()) }
    var isFeatured by remember(existing) { mutableStateOf(existing?.isFeatured ?: false) }
    var imageKey by remember(existing) { mutableStateOf(existing?.imageKey ?: SeedData.categoryImageKey(category)) }
    var customUri by remember(existing) { mutableStateOf(existing?.customImageUri) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            customUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = {
            Text(
                text = if (existing == null) "Add Jewellery Piece" else "Edit Jewellery Piece",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = priceStr, onValueChange = { priceStr = it.filter { c -> c.isDigit() } }, label = { Text("Price (INR)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = stockStr, onValueChange = { stockStr = it.filter { c -> c.isDigit() } }, label = { Text("Stock") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(0.7f))
                }
                OutlinedTextField(value = purity, onValueChange = { purity = it }, label = { Text("Purity (e.g. 22K Gold)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Weight") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())

                Text("Category", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SeedData.allCategories.take(8).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = {
                                category = cat
                                if (customUri == null) imageKey = SeedData.categoryImageKey(cat)
                            },
                            label = { Text(cat) }
                        )
                    }
                }

                Text("Metal", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SeedData.allMetals.forEach { m ->
                        FilterChip(selected = metal == m, onClick = { metal = m }, label = { Text(m) })
                    }
                }

                SecondaryOutlinedButton(
                    text = if (customUri != null) "Replace Photo from Gallery" else "Pick Custom Photo",
                    leadingIcon = Icons.Outlined.Image,
                    onClick = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isFeatured, onCheckedChange = { isFeatured = it })
                    Text("Feature on Home Screen", style = MaterialTheme.typography.bodyMedium)
                }

                if (!errorMsg.isNullOrBlank()) {
                    Text(errorMsg!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val price = priceStr.toLongOrNull() ?: 0L
                    if (name.trim().length < 2 || price < 100L) {
                        errorMsg = "Enter a valid product name and INR price (>= 100)"
                    } else {
                        val defaultSizes = when (category) {
                            "Rings" -> "8,10,12,14,16"
                            "Bangles" -> "2-2,2-4,2-6,2-8"
                            else -> "One Size"
                        }
                        onSave(
                            ProductEntity(
                                id = existing?.id ?: "prod_custom_${UUID.randomUUID().toString().take(8)}",
                                sku = existing?.sku ?: "AJ-CU-${(100..999).random()}",
                                name = name.trim(),
                                description = description.trim().ifBlank { "Handcrafted jewellery piece added via Aurelia Design Studio." },
                                category = category,
                                collection = existing?.collection ?: "Studio Edit",
                                metal = metal,
                                purity = purity.trim().ifBlank { metal },
                                weightGrams = weight.trim().ifBlank { "5.0g (Approx.)" },
                                priceInr = price,
                                originalPriceInr = existing?.originalPriceInr,
                                imageKey = imageKey,
                                customImageUri = customUri,
                                variantsCsv = existing?.variantsCsv ?: "${purity.trim().ifBlank { metal }}:0",
                                sizesCsv = existing?.sizesCsv ?: defaultSizes,
                                occasion = existing?.occasion ?: "Festive",
                                careInstructions = existing?.careInstructions ?: "Store in a dry lined pouch away from perfumes.",
                                stockQuantity = stockStr.toIntOrNull() ?: 0,
                                isFeatured = isFeatured,
                                isBestSeller = existing?.isBestSeller ?: false,
                                isNewArrival = existing?.isNewArrival ?: true,
                                featuredOrder = existing?.featuredOrder ?: 2
                            )
                        )
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BespokeAiStudioScreen(
    uiState: BespokeAiUiState,
    onBack: () -> Unit,
    onGenerate: (String, ImageResolution) -> Unit
) {
    val ext = LocalAureliaColors.current
    var prompt by remember {
        mutableStateOf("Handcrafted 22K gold lotus bridal ring on warm ivory travertine stone, soft natural studio lighting")
    }
    var selectedResolution by remember { mutableStateOf(ImageResolution.SIZE_2K) }

    val quickPrompts = listOf(
        "22K Polki Bridal Necklace on Warm Champagne Satin",
        "Heritage Gold Jhumka Earrings with Pearl Drops",
        "Carved Royal Gold Kada Bangles on Cream Linen",
        "Minimal Sterling Silver Lotus Ring Studio Macro"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .testTag("bespoke_ai_studio_screen")
    ) {
        LuxuryTopBar(
            title = "Bespoke Concept Studio",
            subtitle = "Model: gemini-3-pro-image-preview",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Honest notice when Firebase AI Logic is not provisioned
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ext.champagne,
                border = BorderStroke(0.75.dp, ext.antiqueGold.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = ext.deepWarmBrown, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "AI Concept Preview Mode: Live high-resolution image generation with gemini-3-pro-image-preview requires enabling Firebase AI Logic. Sample studio concepts are currently returned.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.deepWarmBrown
                    )
                }
            }

            Text("Describe Your Jewellery Concept", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                label = { Text("Design Brief") },
                placeholder = { Text("Describe metal, motif, stones, and lighting...") },
                minLines = 3,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bespoke_prompt_input")
            )

            // Resolution Selector (1K, 2K, 4K)
            Text("Output Image Size", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ImageResolution.entries.forEach { res ->
                    val selected = selectedResolution == res
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (selected) MaterialTheme.colorScheme.primary else ext.hairlineBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedResolution = res }
                            .testTag("resolution_${res.label}")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = res.label,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = res.dimensions.substringBefore(" px"),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Text("Inspiration Prompts", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickPrompts.forEach { idea ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ext.secondaryCream,
                        border = BorderStroke(0.75.dp, ext.hairlineBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { prompt = idea }
                    ) {
                        Text(
                            text = idea,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            PrimaryOliveButton(
                text = "Generate ${selectedResolution.label} Concept",
                leadingIcon = Icons.Outlined.AutoAwesome,
                loading = uiState is BespokeAiUiState.Loading,
                onClick = { onGenerate(prompt, selectedResolution) },
                modifier = Modifier.fillMaxWidth(),
                testTag = "bespoke_generate_btn"
            )

            // Output Preview Area
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.75.dp, ext.hairlineBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                when (uiState) {
                    is BespokeAiUiState.Idle -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Choose a resolution (1K, 2K, or 4K) and generate a bespoke jewellery concept.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    is BespokeAiUiState.Loading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Preparing ${selectedResolution.label} jewellery concept...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    is BespokeAiUiState.Error -> {
                        Box(modifier = Modifier.padding(20.dp)) {
                            Text(uiState.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is BespokeAiUiState.Success -> {
                        Column {
                            JewelleryPhoto(
                                imageKey = uiState.sample.previewDrawableKey,
                                metal = uiState.sample.metalGradeHint,
                                contentDescription = uiState.sample.prompt,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.1f),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Requested: ${uiState.sample.resolution.label} (${uiState.sample.resolution.dimensions})  |  ${uiState.sample.modelId}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ext.antiqueGold
                                )
                                Text(
                                    text = uiState.sample.notice,
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
}
