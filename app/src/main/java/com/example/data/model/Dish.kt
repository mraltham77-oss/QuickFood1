package com.example.data.model

data class Dish(
    val id: String,
    val name: String,
    val note: String,
    val category: String,
    val price: Double,
    val image: String,
    val available: Boolean,
    val tag: String? = null,
    val sizes: List<DishSize> = emptyList(),
    val extras: List<DishExtra> = emptyList()
) {
    fun fullImageUrl(): String {
        return if (image.startsWith("http")) {
            image
        } else {
            "https://hand-drawn-icon-foundry.lovable.app$image"
        }
    }
}

data class DishSize(
    val id: String,
    val label: String,
    val delta: Double
)

data class DishExtra(
    val id: String,
    val label: String,
    val price: Double
)

data class RestaurantSettings(
    val name: String = "QuickFood",
    val branch: String = "فرع مدينة نصر",
    val phone: String = "01000000000",
    val hours: String = "مفتوح لحد ٢ بالليل",
    val acceptingOrders: Boolean = true
)

enum class OrderType(
    val id: String,
    val label: String,
    val hint: String,
    val deliveryFee: Double
) {
    DELIVERY("delivery", "دليفري", "٣٥–٤٥ د", 25.0),
    PICKUP("pickup", "تيك أواي", "جاهز في ١٥ د", 0.0),
    DINE_IN("dine", "في الصالة", "امسح كود الطاولة", 0.0);

    companion object {
        fun fromId(id: String): OrderType {
            return entries.firstOrNull { it.id == id } ?: DELIVERY
        }
    }
}

data class CartItem(
    val key: String,
    val dishId: String,
    val name: String,
    val image: String,
    val unitPrice: Double,
    val sizeId: String,
    val sizeLabel: String,
    val extraIds: List<String>,
    val extraLabels: List<String>,
    var qty: Int
)

data class CartTotals(
    val subtotal: Double,
    val discount: Double,
    val tax: Double,
    val deliveryFee: Double,
    val total: Double,
    val count: Int
)

data class OrderSubmissionResult(
    val ok: Boolean,
    val code: String? = null,
    val total: Double? = null,
    val error: String? = null
)

data class OrderDetails(
    val code: String,
    val orderType: String,
    val subtotal: Double,
    val discount: Double,
    val tax: Double,
    val deliveryFee: Double,
    val total: Double,
    val status: String,
    val createdAt: String,
    val items: List<OrderItemDetail>
) {
    fun getStatusLabel(): String {
        return when (status.lowercase()) {
            "new", "pending" -> "تم استلام الطلب"
            "preparing" -> "جاري تحضير الأكل على النار"
            "out_for_delivery" -> "في الطريق إليك مع الدليفري"
            "ready" -> "طلبك جاهز للاستلام فوراً"
            "completed", "delivered" -> "تم التسليم بنجاح، بالهنا والشفا!"
            "cancelled" -> "ملغي"
            else -> status
        }
    }

    fun getEstimatedTime(): String {
        return if (orderType == "delivery") "٣٥–٤٥ دقيقة" else "١٥–٢٠ دقيقة"
    }
}

data class OrderItemDetail(
    val id: String,
    val name: String,
    val sizeLabel: String,
    val extras: List<String>,
    val qty: Int,
    val unitPrice: Double
)
