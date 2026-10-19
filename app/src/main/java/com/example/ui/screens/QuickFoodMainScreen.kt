package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CartSheet
import com.example.ui.components.CategoryTabs
import com.example.ui.components.CheckoutDialog
import com.example.ui.components.DishCard
import com.example.ui.components.DishCustomizeSheet
import com.example.ui.components.HeroBanner
import com.example.ui.components.OrderHistoryDialog
import com.example.ui.components.OrderTrackingDialog
import com.example.ui.components.OrderTypeSelector
import com.example.ui.components.QuickFoodTopBar
import com.example.ui.components.SearchBar
import com.example.ui.theme.CharcoalBg
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.EmberLight
import com.example.ui.theme.EmberPrimary
import com.example.ui.viewmodel.QuickFoodViewModel

@Composable
fun QuickFoodMainScreen(
    viewModel: QuickFoodViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val pastOrders by viewModel.pastOrders.collectAsState()

    // Handle back button for any open sheets
    BackHandler(
        enabled = state.isCartOpen || state.customizingDish != null || state.isCheckoutOpen || state.isTrackingOpen || state.isHistoryOpen
    ) {
        when {
            state.isCheckoutOpen -> viewModel.setCheckoutOpen(false)
            state.isCartOpen -> viewModel.setCartOpen(false)
            state.customizingDish != null -> viewModel.closeCustomizingDish()
            state.isTrackingOpen -> viewModel.setTrackingOpen(false)
            state.isHistoryOpen -> viewModel.setHistoryOpen(false)
        }
    }

    Scaffold(
        containerColor = CharcoalBg,
        topBar = {
            QuickFoodTopBar(
                cartCount = state.cartTotals.count,
                branchInfo = state.settings.branch,
                onCartClick = { viewModel.setCartOpen(true) },
                onHistoryClick = { viewModel.setHistoryOpen(true) }
            )
        },
        bottomBar = {
            // Floating cart bottom bar when items in cart
            AnimatedVisibility(
                visible = state.cartTotals.count > 0 && !state.isCartOpen && !state.isCheckoutOpen,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Surface(
                    color = CharcoalSurface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(EmberPrimary)
                                .clickable { viewModel.setCartOpen(true) }
                                .padding(horizontal = 18.dp, vertical = 14.dp)
                                .testTag("floating_cart_bar"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingBag,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${state.cartTotals.count}",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = "عرض السلة وإتمام الطلب",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Text(
                                text = "${state.cartTotals.total.toInt()} ج.م",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading && state.dishes.isEmpty()) {
                // Loading screen
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = EmberPrimary, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "جاري تحميل أشهى الوجبات من السيرفر...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (state.error != null && state.dishes.isEmpty()) {
                // Error state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تعذر الاتصال بالسيرفر",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.error ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadCatalog() },
                        colors = ButtonDefaults.buttonColors(containerColor = EmberPrimary)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إعادة المحاولة")
                    }
                }
            } else {
                // Content LazyColumn
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("dishes_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Hero Banner
                    item {
                        HeroBanner()
                    }

                    // Order Type Selector
                    item {
                        Column {
                            Text(
                                text = "طريقة استلام الطلب",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OrderTypeSelector(
                                selectedType = state.orderType,
                                onSelectType = { viewModel.setOrderType(it) }
                            )
                        }
                    }

                    // Search Bar
                    item {
                        SearchBar(
                            query = state.searchQuery,
                            onQueryChange = { viewModel.setSearchQuery(it) }
                        )
                    }

                    // Categories Bar
                    item {
                        CategoryTabs(
                            selectedCategory = state.selectedCategory,
                            onCategorySelected = { viewModel.setCategory(it) },
                            searchQuery = state.searchQuery,
                            onSearchQueryChange = { viewModel.setSearchQuery(it) }
                        )
                    }

                    // Section title
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المنيو",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp
                                ),
                                color = Color.White
                            )

                            Text(
                                text = "${state.filteredDishes.size} صنف",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Dishes Grid / List
                    if (state.filteredDishes.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "ملقيناش نتائج للبحث…",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "جرّب تبحث باسم تاني أو اختار تصنيف مختلف",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(state.filteredDishes, key = { it.id }) { dish ->
                            DishCard(
                                dish = dish,
                                onDishClick = { viewModel.openCustomizingDish(it) }
                            )
                        }
                    }

                    // Extra space at bottom so floating cart bar doesn't obscure content
                    item {
                        Spacer(modifier = Modifier.height(70.dp))
                    }
                }
            }
        }
    }

    // Modal Bottom Sheets & Dialogs
    state.customizingDish?.let { dish ->
        DishCustomizeSheet(
            dish = dish,
            onDismiss = { viewModel.closeCustomizingDish() },
            onAddToCart = { d, size, extras, qty ->
                viewModel.addToCart(d, size, extras, qty)
            }
        )
    }

    if (state.isCartOpen) {
        CartSheet(
            cartItems = state.cartItems,
            cartTotals = state.cartTotals,
            orderType = state.orderType,
            coupon = state.coupon,
            couponError = state.couponError,
            onDismiss = { viewModel.setCartOpen(false) },
            onUpdateQty = { key, qty -> viewModel.updateCartItemQty(key, qty) },
            onApplyCoupon = { code -> viewModel.applyCoupon(code) },
            onRemoveCoupon = { viewModel.removeCoupon() },
            onProceedToCheckout = { viewModel.setCheckoutOpen(true) }
        )
    }

    if (state.isCheckoutOpen) {
        CheckoutDialog(
            orderType = state.orderType,
            totalAmount = state.cartTotals.total,
            isSubmitting = state.isSubmittingOrder,
            errorMessage = state.submitError,
            onDismiss = { viewModel.setCheckoutOpen(false) },
            onSubmit = { name, phone, address, notes ->
                viewModel.submitOrder(name, phone, address, notes)
            }
        )
    }

    if (state.isTrackingOpen) {
        OrderTrackingDialog(
            orderCode = state.activeOrderCode ?: "",
            orderDetails = state.activeOrderDetails,
            isLoading = state.isTrackingLoading,
            errorMessage = state.trackingError,
            onRefresh = {
                state.activeOrderCode?.let { viewModel.trackOrder(it) }
            },
            onDismiss = { viewModel.setTrackingOpen(false) }
        )
    }

    if (state.isHistoryOpen) {
        OrderHistoryDialog(
            pastOrderCodes = pastOrders,
            onSelectOrder = { code ->
                viewModel.setHistoryOpen(false)
                viewModel.trackOrder(code)
            },
            onDismiss = { viewModel.setHistoryOpen(false) }
        )
    }
}
