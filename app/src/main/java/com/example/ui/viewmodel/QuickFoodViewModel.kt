package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CartItem
import com.example.data.model.CartTotals
import com.example.data.model.Dish
import com.example.data.model.DishExtra
import com.example.data.model.DishSize
import com.example.data.model.OrderDetails
import com.example.data.model.OrderType
import com.example.data.model.RestaurantSettings
import com.example.data.repository.QuickFoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class QuickFoodUiState(
    val dishes: List<Dish> = emptyList(),
    val settings: RestaurantSettings = RestaurantSettings(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String = "all",
    val searchQuery: String = "",
    val orderType: OrderType = OrderType.DELIVERY,
    val cartItems: List<CartItem> = emptyList(),
    val coupon: String? = null,
    val couponError: String? = null,
    val isCartOpen: Boolean = false,
    val customizingDish: Dish? = null,
    val isCheckoutOpen: Boolean = false,
    val isSubmittingOrder: Boolean = false,
    val submitError: String? = null,
    val activeOrderCode: String? = null,
    val activeOrderDetails: OrderDetails? = null,
    val isTrackingOpen: Boolean = false,
    val isTrackingLoading: Boolean = false,
    val trackingError: String? = null,
    val isHistoryOpen: Boolean = false
) {
    val filteredDishes: List<Dish>
        get() {
            return dishes.filter { dish ->
                val matchCat = selectedCategory == "all" || dish.category.equals(selectedCategory, ignoreCase = true)
                val matchSearch = searchQuery.isBlank() ||
                        dish.name.contains(searchQuery.trim(), ignoreCase = true) ||
                        dish.note.contains(searchQuery.trim(), ignoreCase = true)
                matchCat && matchSearch
            }
        }

    val cartTotals: CartTotals
        get() {
            val subtotal = cartItems.sumOf { it.unitPrice * it.qty }
            val discountRate = if (coupon.equals("QUICK10", ignoreCase = true)) 0.10 else 0.0
            val discount = (subtotal * discountRate).roundToInt().toDouble()
            val taxableAmount = subtotal - discount
            val tax = (taxableAmount * 0.14).roundToInt().toDouble()
            val deliveryFee = if (orderType == OrderType.DELIVERY && subtotal > 0) 25.0 else 0.0
            val total = subtotal - discount + tax + deliveryFee
            val count = cartItems.sumOf { it.qty }
            return CartTotals(
                subtotal = subtotal,
                discount = discount,
                tax = tax,
                deliveryFee = deliveryFee,
                total = total,
                count = count
            )
        }
}

class QuickFoodViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuickFoodRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(QuickFoodUiState())
    val uiState: StateFlow<QuickFoodUiState> = _uiState.asStateFlow()

    val pastOrders: StateFlow<List<String>> = repository.pastOrderCodes

    init {
        loadCatalog()
    }

    fun loadCatalog() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val (dishes, settings) = repository.getCatalog()
                _uiState.value = _uiState.value.copy(
                    dishes = dishes,
                    settings = settings,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "حدث خطأ أثناء تحميل المنيو"
                )
            }
        }
    }

    fun setCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setOrderType(type: OrderType) {
        _uiState.value = _uiState.value.copy(orderType = type)
    }

    fun openCustomizingDish(dish: Dish) {
        _uiState.value = _uiState.value.copy(customizingDish = dish)
    }

    fun closeCustomizingDish() {
        _uiState.value = _uiState.value.copy(customizingDish = null)
    }

    fun addToCart(dish: Dish, selectedSize: DishSize?, selectedExtras: List<DishExtra>, qty: Int) {
        val sizeId = selectedSize?.id ?: (dish.sizes.firstOrNull()?.id ?: "default")
        val sizeLabel = selectedSize?.label ?: (dish.sizes.firstOrNull()?.label ?: "")
        val sizeDelta = selectedSize?.delta ?: 0.0

        val extraIds = selectedExtras.map { it.id }.sorted()
        val extraLabels = selectedExtras.map { it.label }
        val extrasCost = selectedExtras.sumOf { it.price }

        val unitPrice = dish.price + sizeDelta + extrasCost
        val itemKey = "${dish.id}_${sizeId}_${extraIds.joinToString("-")}"

        val currentItems = _uiState.value.cartItems.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.key == itemKey }

        if (existingIndex >= 0) {
            val existing = currentItems[existingIndex]
            currentItems[existingIndex] = existing.copy(qty = existing.qty + qty)
        } else {
            currentItems.add(
                CartItem(
                    key = itemKey,
                    dishId = dish.id,
                    name = dish.name,
                    image = dish.fullImageUrl(),
                    unitPrice = unitPrice,
                    sizeId = sizeId,
                    sizeLabel = sizeLabel,
                    extraIds = extraIds,
                    extraLabels = extraLabels,
                    qty = qty
                )
            )
        }

        _uiState.value = _uiState.value.copy(
            cartItems = currentItems,
            customizingDish = null
        )
    }

    fun updateCartItemQty(key: String, newQty: Int) {
        val currentItems = _uiState.value.cartItems.toMutableList()
        val index = currentItems.indexOfFirst { it.key == key }
        if (index >= 0) {
            if (newQty <= 0) {
                currentItems.removeAt(index)
            } else {
                currentItems[index] = currentItems[index].copy(qty = newQty)
            }
            _uiState.value = _uiState.value.copy(cartItems = currentItems)
        }
    }

    fun applyCoupon(code: String) {
        val clean = code.trim().uppercase()
        if (clean == "QUICK10") {
            _uiState.value = _uiState.value.copy(coupon = clean, couponError = null)
        } else {
            _uiState.value = _uiState.value.copy(couponError = "كود الخصم غير صالح، جرّب QUICK10")
        }
    }

    fun removeCoupon() {
        _uiState.value = _uiState.value.copy(coupon = null, couponError = null)
    }

    fun setCartOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isCartOpen = open)
    }

    fun setCheckoutOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(
            isCheckoutOpen = open,
            isCartOpen = false,
            submitError = null
        )
    }

    fun setTrackingOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isTrackingOpen = open)
    }

    fun setHistoryOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isHistoryOpen = open)
    }

    fun submitOrder(
        name: String,
        phone: String,
        address: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSubmittingOrder = true,
                submitError = null
            )

            val currentState = _uiState.value
            val result = repository.submitOrder(
                orderType = currentState.orderType,
                name = name,
                phone = phone,
                address = address,
                notes = notes,
                coupon = currentState.coupon,
                cartItems = currentState.cartItems
            )

            if (result.ok && result.code != null) {
                _uiState.value = _uiState.value.copy(
                    isSubmittingOrder = false,
                    isCheckoutOpen = false,
                    cartItems = emptyList(),
                    activeOrderCode = result.code,
                    isTrackingOpen = true
                )
                trackOrder(result.code)
            } else {
                val errorMsg = when (result.error) {
                    "dish_unavailable" -> "أكلة من طلبك خلصت للأسف — شيلها وكمّل"
                    "closed" -> "المطعم مش بيستقبل طلبات دلوقتي"
                    else -> result.error ?: "حصلت مشكلة في إرسال الطلب، جرّب تاني"
                }
                _uiState.value = _uiState.value.copy(
                    isSubmittingOrder = false,
                    submitError = errorMsg
                )
            }
        }
    }

    fun trackOrder(code: String) {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                activeOrderCode = cleanCode,
                isTrackingLoading = true,
                trackingError = null,
                isTrackingOpen = true
            )

            val res = repository.fetchOrderStatus(cleanCode)
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    activeOrderDetails = res.getOrThrow(),
                    isTrackingLoading = false
                )
                repository.saveOrderCode(cleanCode)
            } else {
                _uiState.value = _uiState.value.copy(
                    isTrackingLoading = false,
                    trackingError = "مفيش طلب بالكود ده، اتأكد من الرمز وجرب تاني"
                )
            }
        }
    }
}
