package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.data.SeedData
import com.example.data.local.ProductEntity
import com.example.ui.CatalogueFilterState
import com.example.ui.SortOption
import com.example.ui.components.EmptyLuxuryState
import com.example.ui.components.JewelleryPhoto
import com.example.ui.components.LuxuryProductCard
import com.example.ui.components.LuxuryTopBar
import com.example.ui.components.PrimaryOliveButton
import com.example.ui.components.SecondaryOutlinedButton
import com.example.ui.theme.LocalAureliaColors

@Composable
fun CategoryDirectoryScreen(
    products: List<ProductEntity>,
    onSelectCategory: (String) -> Unit,
    onOpenSearch: () -> Unit
) {
    val ext = LocalAureliaColors.current
    val countsByCategory = remember(products) {
        products.groupingBy { it.category }.eachCount()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("category_directory_screen")
    ) {
        LuxuryTopBar(
            title = "Categories",
            subtitle = "${products.size} curated pieces across ${SeedData.allCategories.size} collections",
            actions = {
                IconButton(
                    onClick = onOpenSearch,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search jewellery"
                    )
                }
            }
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(SeedData.allCategories, key = { it }) { category ->
                val count = countsByCategory[category] ?: 0
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectCategory(category) }
                        .testTag("category_tile_$category"),
                    shape = RoundedCornerShape(16.dp),
                    color = ext.secondaryCream,
                    border = BorderStroke(0.75.dp, ext.hairlineBorder)
                ) {
                    Column {
                        JewelleryPhoto(
                            imageKey = SeedData.categoryImageKey(category),
                            contentDescription = category,
                            metal = when (category) {
                                "Anklets" -> "Silver"
                                "Brass Craft" -> "Brass"
                                "Copper Heritage" -> "Copper"
                                else -> "Gold"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.12f),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (count > 0) "View $count ->" else "Explore ->",
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductListingScreen(
    initialTitle: String,
    initialCategory: String?,
    initialMetal: String?,
    initialOccasion: String?,
    allProducts: List<ProductEntity>,
    wishlistIds: Set<String>,
    filterProducts: (List<ProductEntity>, CatalogueFilterState) -> List<ProductEntity>,
    onBack: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenProduct: (String) -> Unit,
    onToggleWishlist: (String) -> Unit
) {
    val ext = LocalAureliaColors.current
    var filterState by remember(initialCategory, initialMetal, initialOccasion) {
        mutableStateOf(
            CatalogueFilterState(
                category = initialCategory?.takeIf { it != "All" && it.isNotBlank() },
                metal = initialMetal?.takeIf { it.isNotBlank() },
                occasion = initialOccasion?.takeIf { it.isNotBlank() }
            )
        )
    }

    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }

    val visibleProducts = remember(allProducts, filterState) {
        filterProducts(allProducts, filterState)
    }

    val screenTitle = filterState.category
        ?: filterState.metal?.let { "$it Jewellery" }
        ?: filterState.occasion?.let { "$it Edit" }
        ?: initialTitle.ifBlank { "All Jewellery" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("product_listing_screen")
    ) {
        LuxuryTopBar(
            title = screenTitle,
            subtitle = "${visibleProducts.size} Products",
            onBack = onBack,
            actions = {
                IconButton(onClick = onOpenSearch) {
                    Icon(Icons.Outlined.Search, contentDescription = "Search")
                }
                IconButton(
                    onClick = { showSortSheet = true },
                    modifier = Modifier.testTag("listing_sort_btn")
                ) {
                    Icon(Icons.AutoMirrored.Outlined.Sort, contentDescription = "Sort products")
                }
                IconButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier.testTag("listing_filter_btn")
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(Icons.Outlined.FilterList, contentDescription = "Filter products")
                        if (filterState.activeCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(ext.antiqueGold)
                            )
                        }
                    }
                }
            }
        )

        // Quick Metal Filter Chips (matches Reference 5)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                val allSelected = filterState.metal == null
                FilterChip(
                    selected = allSelected,
                    onClick = { filterState = filterState.copy(metal = null) },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = ext.secondaryCream
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = allSelected,
                        borderColor = ext.hairlineBorder
                    )
                )
            }
            items(SeedData.allMetals) { metal ->
                val selected = filterState.metal == metal
                FilterChip(
                    selected = selected,
                    onClick = {
                        filterState = filterState.copy(metal = if (selected) null else metal)
                    },
                    label = { Text(metal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = ext.secondaryCream
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = ext.hairlineBorder
                    )
                )
            }
        }

        // Active filter summary strip
        if (filterState.activeCount > 0 || filterState.sortOption != SortOption.RECOMMENDED) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sorted by: ${filterState.sortOption.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (filterState.activeCount > 0) {
                    Text(
                        text = "Clear Filters (${filterState.activeCount})",
                        style = MaterialTheme.typography.labelMedium,
                        color = ext.antiqueGold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { filterState = CatalogueFilterState() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (visibleProducts.isEmpty()) {
            EmptyLuxuryState(
                icon = Icons.Outlined.SearchOff,
                title = "No Matching Pieces",
                subtitle = "No jewellery items match the active filter selection. Reset filters to view the full collection.",
                actionLabel = "Reset Filters",
                onAction = { filterState = CatalogueFilterState() },
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(visibleProducts, key = { it.id }) { product ->
                    LuxuryProductCard(
                        product = product,
                        isWishlisted = wishlistIds.contains(product.id),
                        onClick = { onOpenProduct(product.id) },
                        onToggleWishlist = { onToggleWishlist(product.id) }
                    )
                }
            }
        }
    }

    if (showSortSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Sort Collection",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                SortOption.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                filterState = filterState.copy(sortOption = option)
                                showSortSheet = false
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = filterState.sortOption == option,
                            onClick = {
                                filterState = filterState.copy(sortOption = option)
                                showSortSheet = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showFilterSheet) {
        var draft by remember(filterState) { mutableStateOf(filterState) }
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text("Refine Jewellery", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Category", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SeedData.allCategories.forEach { cat ->
                        val selected = draft.category == cat
                        FilterChip(
                            selected = selected,
                            onClick = { draft = draft.copy(category = if (selected) null else cat) },
                            label = { Text(cat) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Occasion", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SeedData.allOccasions.forEach { occ ->
                        val selected = draft.occasion == occ
                        FilterChip(
                            selected = selected,
                            onClick = { draft = draft.copy(occasion = if (selected) null else occ) },
                            label = { Text(occ) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Price Ceiling", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                val priceCeilings = listOf(
                    10_000L to "Under ₹10,000",
                    50_000L to "Under ₹50,000",
                    1_00_000L to "Under ₹1,00,000",
                    2_50_000L to "Under ₹2,50,000"
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    priceCeilings.forEach { (limit, label) ->
                        val selected = draft.maxPriceInr == limit
                        FilterChip(
                            selected = selected,
                            onClick = { draft = draft.copy(maxPriceInr = if (selected) null else limit) },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { draft = draft.copy(inStockOnly = !draft.inStockOnly) }
                ) {
                    Checkbox(
                        checked = draft.inStockOnly,
                        onCheckedChange = { draft = draft.copy(inStockOnly = it) }
                    )
                    Text("Available in stock only", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryOutlinedButton(
                        text = "Clear All",
                        onClick = {
                            filterState = CatalogueFilterState()
                            showFilterSheet = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryOliveButton(
                        text = "Apply Filters",
                        onClick = {
                            filterState = draft
                            showFilterSheet = false
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "apply_filters_btn"
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    allProducts: List<ProductEntity>,
    wishlistIds: Set<String>,
    recentSearches: List<String>,
    onBack: () -> Unit,
    onRecordSearch: (String) -> Unit,
    onRemoveRecent: (String) -> Unit,
    onClearRecent: () -> Unit,
    onOpenProduct: (String) -> Unit,
    onToggleWishlist: (String) -> Unit
) {
    val ext = LocalAureliaColors.current
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val results = remember(allProducts, query) {
        val q = query.trim().lowercase()
        if (q.isBlank()) emptyList()
        else allProducts.filter { p ->
            p.name.lowercase().contains(q) ||
                p.category.lowercase().contains(q) ||
                p.metal.lowercase().contains(q) ||
                p.purity.lowercase().contains(q) ||
                p.collection.lowercase().contains(q) ||
                p.occasion.lowercase().contains(q)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .testTag("search_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search rings, jhumkas, 22K gold, silver...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = ext.hairlineBorder
                ),
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Clear search")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onRecordSearch(query)
                        keyboard?.hide()
                    }
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .focusRequester(focusRequester)
                    .testTag("search_input")
            )
        }
        HorizontalDivider(thickness = 0.75.dp, color = ext.hairlineBorder)

        if (query.isBlank()) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (recentSearches.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Recent Searches", style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = onClearRecent) {
                                Text("Clear All", color = ext.antiqueGold)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        recentSearches.forEach { term ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { query = term }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = term,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onRemoveRecent(term) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Close,
                                        contentDescription = "Remove $term",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Popular Searches", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(10.dp))
                    val suggestions = listOf(
                        "22K Gold", "Bridal", "Jhumka", "Mangalsutra",
                        "Silver", "Bangle", "Floral Ring", "Copper", "Brass"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        suggestions.forEach { suggestion ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ext.secondaryCream,
                                border = BorderStroke(0.75.dp, ext.hairlineBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        query = suggestion
                                        onRecordSearch(suggestion)
                                    }
                            ) {
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else if (results.isEmpty()) {
            EmptyLuxuryState(
                icon = Icons.Outlined.SearchOff,
                title = "No Jewellery Found",
                subtitle = "No results matched \"$query\". Try searching by category, metal, or occasion.",
                actionLabel = "Clear Search",
                onAction = { query = "" }
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(results, key = { it.id }) { product ->
                    LuxuryProductCard(
                        product = product,
                        isWishlisted = wishlistIds.contains(product.id),
                        onClick = {
                            onRecordSearch(query)
                            onOpenProduct(product.id)
                        },
                        onToggleWishlist = { onToggleWishlist(product.id) }
                    )
                }
            }
        }
    }
}
