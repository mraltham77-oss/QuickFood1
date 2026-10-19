package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.CartItem
import com.example.data.model.Dish
import com.example.data.model.DishExtra
import com.example.data.model.DishSize
import com.example.data.model.OrderDetails
import com.example.data.model.OrderSubmissionResult
import com.example.data.model.OrderType
import com.example.data.model.RestaurantSettings
import com.example.data.remote.QuickFoodApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class QuickFoodRepository(context: Context) {

    private val apiClient = QuickFoodApiClient()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("quickfood_prefs", Context.MODE_PRIVATE)

    private val _pastOrderCodes = MutableStateFlow<List<String>>(loadPastOrders())
    val pastOrderCodes: StateFlow<List<String>> = _pastOrderCodes.asStateFlow()

    private fun loadPastOrders(): List<String> {
        val raw = prefs.getString("past_orders", "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split(",").filter { it.isNotBlank() }
    }

    fun saveOrderCode(code: String) {
        val current = _pastOrderCodes.value.toMutableList()
        if (!current.contains(code)) {
            current.add(0, code)
            _pastOrderCodes.value = current
            prefs.edit().putString("past_orders", current.joinToString(",")).apply()
        }
    }

    suspend fun getCatalog(): Pair<List<Dish>, RestaurantSettings> {
        val result = apiClient.fetchCatalog()
        return if (result.isSuccess) {
            result.getOrThrow()
        } else {
            // Resilient fallback with the dishes from the server
            Pair(getFallbackDishes(), RestaurantSettings())
        }
    }

    suspend fun submitOrder(
        orderType: OrderType,
        name: String,
        phone: String,
        address: String?,
        notes: String?,
        coupon: String?,
        cartItems: List<CartItem>
    ): OrderSubmissionResult {
        val lines = cartItems.map { item ->
            mapOf(
                "dishId" to item.dishId,
                "sizeId" to item.sizeId,
                "extraIds" to item.extraIds,
                "qty" to item.qty
            )
        }

        val result = apiClient.createOrder(
            orderType = orderType.id,
            name = name,
            phone = phone,
            address = address,
            notes = notes,
            coupon = coupon,
            lines = lines
        )

        return if (result.isSuccess) {
            val res = result.getOrThrow()
            if (res.ok && res.code != null) {
                saveOrderCode(res.code)
            }
            res
        } else {
            OrderSubmissionResult(
                ok = false,
                error = result.exceptionOrNull()?.message ?: "فشل الاتصال بالسيرفر"
            )
        }
    }

    suspend fun fetchOrderStatus(code: String): Result<OrderDetails> {
        return apiClient.getOrderStatus(code)
    }

    private fun getFallbackDishes(): List<Dish> {
        return listOf(
            Dish(
                id = "smash",
                name = "سماش برجر",
                note = "قطعتين لحم مضغوطين على الصاج، شيدر سايح وصوص البيت",
                category = "burger",
                price = 165.0,
                image = "/assets/burger-B8VINx2M.jpg",
                available = true,
                tag = "الأكتر طلبًا",
                sizes = listOf(
                    DishSize(id = "s", label = "عادي", delta = 0.0),
                    DishSize(id = "m", label = "دبل", delta = 45.0)
                ),
                extras = listOf(
                    DishExtra(id = "cheese", label = "شيدر زيادة", price = 20.0),
                    DishExtra(id = "bacon", label = "بيف بيكون", price = 35.0),
                    DishExtra(id = "jalapeno", label = "هالابينو", price = 10.0)
                )
            ),
            Dish(
                id = "marg",
                name = "مارجريتا نابولي",
                note = "عجينة متخمرة ٤٨ ساعة في فرن حطب، موتزاريلا وريحان",
                category = "pizza",
                price = 190.0,
                image = "/assets/pizza-CE7E9Z7b.jpg",
                available = true,
                sizes = listOf(
                    DishSize(id = "s", label = "وسط", delta = 0.0),
                    DishSize(id = "l", label = "كبير", delta = 60.0)
                ),
                extras = listOf(
                    DishExtra(id = "burrata", label = "بوراتا", price = 55.0),
                    DishExtra(id = "olive", label = "زيتون أسود", price = 15.0)
                )
            ),
            Dish(
                id = "mix",
                name = "مشكل مشويات",
                note = "كفتة وشيش طاووق على الفحم، طحينة وعيش بلدي",
                category = "grill",
                price = 240.0,
                image = "/assets/grill-BS8AouK2.jpg",
                available = true,
                tag = "عرض اليوم",
                sizes = listOf(
                    DishSize(id = "1", label = "فرد", delta = 0.0),
                    DishSize(id = "2", label = "لتنين", delta = 210.0)
                ),
                extras = listOf(
                    DishExtra(id = "rice", label = "رز بالشعرية", price = 30.0),
                    DishExtra(id = "salad", label = "سلطة بلدي", price = 20.0)
                )
            ),
            Dish(
                id = "fries",
                name = "بطاطس مقلية مرتين",
                note = "مقرمشة بجد، ملح بحري وصوص سريراتشا مايو",
                category = "sides",
                price = 55.0,
                image = "/assets/fries-Bdx4Ni44.jpg",
                available = true,
                sizes = listOf(
                    DishSize(id = "s", label = "صغير", delta = 0.0),
                    DishSize(id = "l", label = "كبير", delta = 20.0)
                ),
                extras = listOf(
                    DishExtra(id = "cheese", label = "صوص جبنة", price = 15.0)
                )
            ),
            Dish(
                id = "truffle",
                name = "ترافل برجر",
                note = "مشروم سوتيه ومايونيز ترافل — بيخلص بدري",
                category = "burger",
                price = 210.0,
                image = "/assets/burger-B8VINx2M.jpg",
                available = false,
                sizes = listOf(
                    DishSize(id = "s", label = "عادي", delta = 0.0),
                    DishSize(id = "m", label = "دبل", delta = 45.0)
                ),
                extras = emptyList()
            ),
            Dish(
                id = "kofta",
                name = "كفتة على الفحم",
                note = "نص كيلو كفتة متبلة بإيدينا كل يوم الصبح",
                category = "grill",
                price = 185.0,
                image = "/assets/grill-BS8AouK2.jpg",
                available = true,
                sizes = listOf(
                    DishSize(id = "1", label = "ربع", delta = 0.0),
                    DishSize(id = "2", label = "نص", delta = 160.0)
                ),
                extras = listOf(
                    DishExtra(id = "tahini", label = "طحينة زيادة", price = 10.0)
                )
            )
        )
    }
}
