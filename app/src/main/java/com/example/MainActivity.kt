package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.parsedSizes
import com.example.data.parsedVariants
import com.example.ui.AureliaViewModel
import com.example.ui.components.AureliaBottomBar
import com.example.ui.components.MainTab
import com.example.ui.screens.AboutBrandScreen
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AddressesScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminDesignStudioScreen
import com.example.ui.screens.BespokeAiStudioScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CategoryDirectoryScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.ContactSupportScreen
import com.example.ui.screens.GoldRateScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.JewelleryGuideScreen
import com.example.ui.screens.LuxuryJewelleryGridScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.OrderSuccessScreen
import com.example.ui.screens.OrderTrackingScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProductListingScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WishlistScreen
import com.example.ui.theme.AureliaJewelsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            val viewModel: AureliaViewModel = viewModel()
            AureliaAppRoot(viewModel = viewModel)
        }
    }
}

@Composable
fun AureliaAppRoot(viewModel: AureliaViewModel) {
    val isSeeded by viewModel.isSeeded.collectAsStateWithLifecycle()
    val config by viewModel.storeConfig.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val wishlistIds by viewModel.wishlistIds.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartDisplayItems.collectAsStateWithLifecycle()
    val cartCount by viewModel.cartItemCount.collectAsStateWithLifecycle()
    val addresses by viewModel.addresses.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadNotifications by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val appliedCouponCode by viewModel.appliedCouponCode.collectAsStateWithLifecycle()
    val isExpressDelivery by viewModel.selectedDeliveryExpress.collectAsStateWithLifecycle()
    val isPlacingOrder by viewModel.isPlacingOrder.collectAsStateWithLifecycle()
    val bespokeAiState by viewModel.bespokeAiState.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.toastMessages.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val topLevelRoutes = remember { MainTab.entries.map { it.route }.toSet() }
    val showBottomBar = currentRoute in topLevelRoutes

    AureliaJewelsTheme(
        primaryColorHex = config.primaryColorHex,
        accentColorHex = config.accentColorHex
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (showBottomBar) {
                    AureliaBottomBar(
                        currentRoute = currentRoute,
                        wishlistCount = wishlistIds.size,
                        onSelectTab = { tab ->
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else innerPadding.calculateBottomPadding() * 0f)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {
                    composable("splash") {
                        SplashScreen(
                            config = config,
                            isSeeded = isSeeded,
                            onContinue = {
                                val next = if (config.onboardingCompleted) "home" else "onboarding"
                                navController.navigate(next) {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("onboarding") {
                        OnboardingScreen(
                            config = config,
                            onFinish = {
                                viewModel.completeOnboarding()
                                navController.navigate("home") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            config = config,
                            products = products,
                            wishlistIds = wishlistIds,
                            cartCount = cartCount,
                            unreadNotifications = unreadNotifications,
                            onOpenSearch = { navController.navigate("search") },
                            onOpenCart = { navController.navigate("cart") },
                            onOpenNotifications = { navController.navigate("notifications") },
                            onOpenCategories = { navController.navigate("categories") },
                            onOpenCategory = { cat ->
                                if (cat == "All") {
                                    navController.navigate("jewellery_grid")
                                } else {
                                    navController.navigate("listing?category=${Uri.encode(cat)}")
                                }
                            },
                            onOpenMetal = { metal ->
                                navController.navigate("listing?metal=${Uri.encode(metal)}")
                            },
                            onOpenOccasion = { occ ->
                                navController.navigate("listing?occasion=${Uri.encode(occ)}")
                            },
                            onOpenProduct = { id -> navController.navigate("product/$id") },
                            onToggleWishlist = { viewModel.toggleWishlist(it) },
                            onOpenGuide = { navController.navigate("guide") },
                            onOpenGoldRates = { navController.navigate("gold_rates") },
                            onOpenSupport = { navController.navigate("support") },
                            onOpenStudio = { navController.navigate("studio") },
                            onOpenBespokeAi = { navController.navigate("bespoke_ai") },
                            appliedCouponCode = appliedCouponCode,
                            onClaimOffer = { code -> viewModel.claimOfferCode(code) },
                            onQuickAddToBag = { prod ->
                                viewModel.addToCart(
                                    product = prod,
                                    variant = prod.parsedVariants().first(),
                                    size = prod.parsedSizes().first(),
                                    quantity = 1
                                )
                            }
                        )
                    }

                    composable("jewellery_grid") {
                        LuxuryJewelleryGridScreen(
                            products = products,
                            wishlistIds = wishlistIds,
                            cartCount = cartCount,
                            title = "All Luxury Jewellery",
                            onBack = { navController.popBackStack() },
                            onOpenSearch = { navController.navigate("search") },
                            onOpenWishlist = { navController.navigate("wishlist") },
                            onOpenCart = { navController.navigate("cart") },
                            onProductClick = { product -> navController.navigate("product/${product.id}") },
                            onToggleWishlist = { viewModel.toggleWishlist(it) }
                        )
                    }

                    composable("categories") {
                        CategoryDirectoryScreen(
                            products = products,
                            onSelectCategory = { cat ->
                                navController.navigate("listing?category=${Uri.encode(cat)}")
                            },
                            onOpenSearch = { navController.navigate("search") }
                        )
                    }

                    composable(
                        route = "listing?category={category}&metal={metal}&occasion={occasion}",
                        arguments = listOf(
                            navArgument("category") { type = NavType.StringType; defaultValue = "" },
                            navArgument("metal") { type = NavType.StringType; defaultValue = "" },
                            navArgument("occasion") { type = NavType.StringType; defaultValue = "" }
                        )
                    ) { backStack ->
                        val cat = backStack.arguments?.getString("category")?.takeIf { it.isNotBlank() }
                        val metal = backStack.arguments?.getString("metal")?.takeIf { it.isNotBlank() }
                        val occasion = backStack.arguments?.getString("occasion")?.takeIf { it.isNotBlank() }
                        ProductListingScreen(
                            initialTitle = cat ?: metal ?: occasion ?: "Collection",
                            initialCategory = cat,
                            initialMetal = metal,
                            initialOccasion = occasion,
                            allProducts = products,
                            wishlistIds = wishlistIds,
                            filterProducts = { list, f -> viewModel.filterProducts(list, f) },
                            onBack = { navController.popBackStack() },
                            onOpenSearch = { navController.navigate("search") },
                            onOpenProduct = { id -> navController.navigate("product/$id") },
                            onToggleWishlist = { viewModel.toggleWishlist(it) }
                        )
                    }

                    composable("search") {
                        SearchScreen(
                            allProducts = products,
                            wishlistIds = wishlistIds,
                            recentSearches = recentSearches,
                            onBack = { navController.popBackStack() },
                            onRecordSearch = { viewModel.recordSearch(it) },
                            onRemoveRecent = { viewModel.removeRecentSearch(it) },
                            onClearRecent = { viewModel.clearRecentSearches() },
                            onOpenProduct = { id -> navController.navigate("product/$id") },
                            onToggleWishlist = { viewModel.toggleWishlist(it) }
                        )
                    }

                    composable(
                        route = "product/{productId}",
                        arguments = listOf(navArgument("productId") { type = NavType.StringType })
                    ) { backStack ->
                        val id = backStack.arguments?.getString("productId").orEmpty()
                        val product = products.firstOrNull { it.id == id }
                        val related = remember(products, product) {
                            if (product == null) emptyList()
                            else products.filter { it.category == product.category && it.id != product.id }.take(6)
                        }
                        ProductDetailScreen(
                            product = product,
                            relatedProducts = related,
                            isWishlisted = wishlistIds.contains(id),
                            wishlistIds = wishlistIds,
                            onBack = { navController.popBackStack() },
                            onToggleWishlist = { viewModel.toggleWishlist(it) },
                            onAddToCart = { p, v, s, q -> viewModel.addToCart(p, v, s, q) },
                            onBuyNow = { p, v, s, q ->
                                viewModel.addToCart(p, v, s, q) {
                                    navController.navigate("cart")
                                }
                            },
                            onOpenProduct = { nextId -> navController.navigate("product/$nextId") },
                            whatsappNumber = config.whatsappNumber
                        )
                    }

                    composable("wishlist") {
                        WishlistScreen(
                            allProducts = products,
                            wishlistIds = wishlistIds,
                            onOpenProduct = { id -> navController.navigate("product/$id") },
                            onToggleWishlist = { viewModel.toggleWishlist(it) },
                            onQuickAddToBag = { prod ->
                                viewModel.addToCart(
                                    product = prod,
                                    variant = prod.parsedVariants().first(),
                                    size = prod.parsedSizes().first(),
                                    quantity = 1
                                )
                            },
                            onExploreShop = { navController.navigate("categories") }
                        )
                    }

                    composable("cart") {
                        CartScreen(
                            cartItems = cartItems,
                            config = config,
                            appliedCouponCode = appliedCouponCode,
                            onBack = { navController.popBackStack() },
                            onOpenProduct = { id -> navController.navigate("product/$id") },
                            onUpdateQuantity = { key, qty -> viewModel.updateCartQuantity(key, qty) },
                            onRemoveItem = { key -> viewModel.removeCartItem(key) },
                            onApplyCoupon = { code -> viewModel.applyCoupon(code) },
                            onClearCoupon = { viewModel.clearCoupon() },
                            onProceedToCheckout = { navController.navigate("checkout") },
                            onExploreShop = { navController.navigate("categories") }
                        )
                    }

                    composable("checkout") {
                        CheckoutScreen(
                            cartItems = cartItems,
                            addresses = addresses,
                            config = config,
                            appliedCouponCode = appliedCouponCode,
                            isExpressDelivery = isExpressDelivery,
                            isPlacingOrder = isPlacingOrder,
                            onBack = { navController.popBackStack() },
                            onSetExpressDelivery = { viewModel.setExpressDelivery(it) },
                            onSaveAddress = { viewModel.saveAddress(it) },
                            onPlaceDemoOrder = { addr, payment ->
                                viewModel.placeDemoOrder(addr, payment) { order ->
                                    navController.navigate("order_success/${order.orderId}") {
                                        popUpTo("cart") { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    composable(
                        route = "order_success/{orderId}",
                        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                    ) { backStack ->
                        val orderId = backStack.arguments?.getString("orderId").orEmpty()
                        val order = orders.firstOrNull { it.orderId == orderId }
                        OrderSuccessScreen(
                            order = order,
                            config = config,
                            onTrackOrder = { id ->
                                navController.navigate("order_tracking/$id") {
                                    popUpTo("home") { inclusive = false }
                                }
                            },
                            onContinueShopping = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("orders") {
                        OrdersScreen(
                            orders = orders,
                            onOpenOrderTracking = { id -> navController.navigate("order_tracking/$id") },
                            onReorder = { order ->
                                viewModel.reorderFromOrder(order) {
                                    navController.navigate("cart")
                                }
                            },
                            onExploreShop = { navController.navigate("categories") }
                        )
                    }

                    composable(
                        route = "order_tracking/{orderId}",
                        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                    ) { backStack ->
                        val orderId = backStack.arguments?.getString("orderId").orEmpty()
                        val order = orders.firstOrNull { it.orderId == orderId }
                        OrderTrackingScreen(
                            order = order,
                            config = config,
                            onBack = { navController.popBackStack() },
                            onAdvanceDemoStatus = { viewModel.advanceOrderStatus(it) }
                        )
                    }

                    composable("account") {
                        AccountScreen(
                            config = config,
                            unreadNotifications = unreadNotifications,
                            ordersCount = orders.size,
                            wishlistCount = wishlistIds.size,
                            onUpdateProfile = { viewModel.saveStoreConfig(it) },
                            onOpenOrders = { navController.navigate("orders") },
                            onOpenWishlist = { navController.navigate("wishlist") },
                            onOpenAddresses = { navController.navigate("addresses") },
                            onOpenNotifications = { navController.navigate("notifications") },
                            onOpenGuide = { navController.navigate("guide") },
                            onOpenGoldRates = { navController.navigate("gold_rates") },
                            onOpenSupport = { navController.navigate("support") },
                            onOpenStudio = { navController.navigate("studio") },
                            onOpenBespokeAi = { navController.navigate("bespoke_ai") },
                            onOpenAbout = { navController.navigate("about") },
                            onOpenAdminDashboard = { navController.navigate("admin_dashboard") }
                        )
                    }

                    composable("notifications") {
                        NotificationsScreen(
                            notifications = notifications,
                            onBack = { navController.popBackStack() },
                            onMarkRead = { viewModel.markNotificationRead(it) },
                            onMarkAllRead = { viewModel.markAllNotificationsRead() },
                            onNavigateTarget = { route ->
                                when {
                                    route.startsWith("category/") -> {
                                        val cat = route.removePrefix("category/")
                                        navController.navigate("listing?category=${Uri.encode(cat)}")
                                    }
                                    else -> navController.navigate(route)
                                }
                            }
                        )
                    }

                    composable("addresses") {
                        AddressesScreen(
                            addresses = addresses,
                            onBack = { navController.popBackStack() },
                            onSaveAddress = { viewModel.saveAddress(it) },
                            onDeleteAddress = { viewModel.deleteAddress(it) }
                        )
                    }

                    composable("support") {
                        ContactSupportScreen(
                            config = config,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("guide") {
                        JewelleryGuideScreen(onBack = { navController.popBackStack() })
                    }

                    composable("gold_rates") {
                        GoldRateScreen(onBack = { navController.popBackStack() })
                    }

                    composable("about") {
                        AboutBrandScreen(
                            config = config,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("admin_dashboard") {
                        AdminDashboardScreen(
                            orders = orders,
                            products = products,
                            wishlistIds = wishlistIds,
                            cartItems = cartItems,
                            addresses = addresses,
                            onBack = { navController.popBackStack() },
                            onAdvanceOrderStatus = { viewModel.advanceOrderStatus(it) },
                            onUpsertProduct = { viewModel.upsertProduct(it) },
                            onDeleteProduct = { viewModel.deleteProduct(it) },
                            onOpenBrandStudio = { navController.navigate("studio") }
                        )
                    }

                    composable("studio") {
                        AdminDesignStudioScreen(
                            config = config,
                            products = products,
                            onBack = { navController.popBackStack() },
                            onSaveConfig = { viewModel.saveStoreConfig(it) },
                            onUpsertProduct = { viewModel.upsertProduct(it) },
                            onDeleteProduct = { viewModel.deleteProduct(it) },
                            onResetDemo = { viewModel.resetDemoConfiguration() },
                            onOpenBespokeAi = { navController.navigate("bespoke_ai") }
                        )
                    }

                    composable("bespoke_ai") {
                        BespokeAiStudioScreen(
                            uiState = bespokeAiState,
                            onBack = { navController.popBackStack() },
                            onGenerate = { prompt, resolution ->
                                viewModel.generateBespokeJewelleryImage(prompt, resolution)
                            }
                        )
                    }
                }
            }
        }
    }
}
