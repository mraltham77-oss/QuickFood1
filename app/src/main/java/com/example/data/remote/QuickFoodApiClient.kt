package com.example.data.remote

import android.util.Log
import com.example.data.model.Dish
import com.example.data.model.DishExtra
import com.example.data.model.DishSize
import com.example.data.model.OrderDetails
import com.example.data.model.OrderItemDetail
import com.example.data.model.OrderSubmissionResult
import com.example.data.model.RestaurantSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class QuickFoodApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://hand-drawn-icon-foundry.lovable.app"
    private val catalogFn = "/_serverFn/2a45d9b79d4ba9547992f1eac18039c2bae0ddf7df7670a8638b3bf5e0a6962f"
    private val createOrderFn = "/_serverFn/7f92d135aa3763ddd5bf6d4d9f84832b6b591cbaa35dcc4048b4b1beed8e7bf3"
    private val orderStatusFn = "/_serverFn/8d89b7179a61ab3d48e536226751a804c2a4a27f478238699b8c0eb2189983ec"

    private val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    suspend fun fetchCatalog(): Result<Pair<List<Dish>, RestaurantSettings>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(baseUrl + catalogFn)
                .header("User-Agent", userAgent)
                .header("Referer", "$baseUrl/")
                .header("Origin", baseUrl)
                .header("x-tsr-serverFn", "true")
                .header("Accept", "application/json")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: $body"))
            }

            val parsed = SerovalCodec.parse(body) as? Map<*, *>
            val result = parsed?.get("result") as? Map<*, *>
                ?: return@withContext Result.failure(Exception("Invalid server response format"))

            val dishesRaw = result["dishes"] as? List<*> ?: emptyList<Any>()
            val dishes = mutableListOf<Dish>()

            for (item in dishesRaw) {
                if (item is Map<*, *>) {
                    val id = item["id"]?.toString().orEmpty()
                    val name = item["name"]?.toString().orEmpty()
                    val note = item["note"]?.toString().orEmpty()
                    val category = item["category"]?.toString().orEmpty()
                    val price = (item["price"] as? Number)?.toDouble() ?: 0.0
                    val image = item["image"]?.toString().orEmpty()
                    val available = item["available"] == true
                    val tag = item["tag"]?.toString()

                    val sizesList = mutableListOf<DishSize>()
                    (item["sizes"] as? List<*>)?.forEach { s ->
                        if (s is Map<*, *>) {
                            sizesList.add(
                                DishSize(
                                    id = s["id"]?.toString().orEmpty(),
                                    label = s["label"]?.toString().orEmpty(),
                                    delta = (s["delta"] as? Number)?.toDouble() ?: 0.0
                                )
                            )
                        }
                    }

                    val extrasList = mutableListOf<DishExtra>()
                    (item["extras"] as? List<*>)?.forEach { e ->
                        if (e is Map<*, *>) {
                            extrasList.add(
                                DishExtra(
                                    id = e["id"]?.toString().orEmpty(),
                                    label = e["label"]?.toString().orEmpty(),
                                    price = (e["price"] as? Number)?.toDouble() ?: 0.0
                                )
                            )
                        }
                    }

                    dishes.add(
                        Dish(
                            id = id,
                            name = name,
                            note = note,
                            category = category,
                            price = price,
                            image = image,
                            available = available,
                            tag = tag,
                            sizes = sizesList,
                            extras = extrasList
                        )
                    )
                }
            }

            val settingsRaw = result["settings"] as? Map<*, *>
            val settings = RestaurantSettings(
                name = settingsRaw?.get("name")?.toString() ?: "QuickFood",
                branch = settingsRaw?.get("branch")?.toString() ?: "فرع مدينة نصر",
                phone = settingsRaw?.get("phone")?.toString() ?: "01000000000",
                hours = settingsRaw?.get("hours")?.toString() ?: "مفتوح لحد ٢ بالليل",
                acceptingOrders = settingsRaw?.get("accepting_orders") == true
            )

            Result.success(Pair(dishes, settings))
        } catch (e: Exception) {
            Log.e("QuickFoodApi", "fetchCatalog error", e)
            Result.failure(e)
        }
    }

    suspend fun createOrder(
        orderType: String,
        name: String,
        phone: String,
        address: String?,
        notes: String?,
        coupon: String?,
        lines: List<Map<String, Any>>
    ): Result<OrderSubmissionResult> = withContext(Dispatchers.IO) {
        try {
            val dataMap = mutableMapOf<String, Any?>()
            dataMap["orderType"] = orderType
            dataMap["name"] = name
            dataMap["phone"] = phone
            dataMap["address"] = address.orEmpty()
            dataMap["notes"] = notes.orEmpty()
            dataMap["coupon"] = coupon.orEmpty()
            dataMap["lines"] = lines

            val payloadObj = mapOf("data" to dataMap)
            val jsonPayload = SerovalCodec.serialize(payloadObj)

            val requestBody = jsonPayload.toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(baseUrl + createOrderFn)
                .header("User-Agent", userAgent)
                .header("Referer", "$baseUrl/")
                .header("Origin", baseUrl)
                .header("x-tsr-serverFn", "true")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: $body"))
            }

            val parsed = SerovalCodec.parse(body) as? Map<*, *>
            val result = parsed?.get("result") as? Map<*, *>
            val ok = result?.get("ok") == true
            val code = result?.get("code")?.toString()
            val total = (result?.get("total") as? Number)?.toDouble()
            val error = result?.get("error")?.toString()
                ?: (parsed?.get("error") as? Map<*, *>)?.get("message")?.toString()
                ?: if (!ok && code == null) "حصلت مشكلة في إرسال الطلب، جرّب تاني" else null

            Result.success(
                OrderSubmissionResult(
                    ok = ok,
                    code = code,
                    total = total,
                    error = error
                )
            )
        } catch (e: Exception) {
            Log.e("QuickFoodApi", "createOrder error", e)
            Result.failure(e)
        }
    }

    suspend fun getOrderStatus(code: String): Result<OrderDetails> = withContext(Dispatchers.IO) {
        try {
            val queryPayload = SerovalCodec.buildQueryPayload(mapOf("code" to code))
            val urlWithQuery = "$baseUrl$orderStatusFn?payload=$queryPayload"

            val request = Request.Builder()
                .url(urlWithQuery)
                .header("User-Agent", userAgent)
                .header("Referer", "$baseUrl/")
                .header("Origin", baseUrl)
                .header("x-tsr-serverFn", "true")
                .header("Accept", "application/json")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: $body"))
            }

            val parsed = SerovalCodec.parse(body) as? Map<*, *>
            val result = parsed?.get("result") as? Map<*, *>
                ?: return@withContext Result.failure(Exception("Order not found or invalid response"))

            val orderCode = result["code"]?.toString() ?: code
            val orderType = result["order_type"]?.toString() ?: "delivery"
            val subtotal = (result["subtotal"] as? Number)?.toDouble() ?: 0.0
            val discount = (result["discount"] as? Number)?.toDouble() ?: 0.0
            val tax = (result["tax"] as? Number)?.toDouble() ?: 0.0
            val deliveryFee = (result["delivery_fee"] as? Number)?.toDouble() ?: 0.0
            val total = (result["total"] as? Number)?.toDouble() ?: 0.0
            val status = result["status"]?.toString() ?: "new"
            val createdAt = result["created_at"]?.toString().orEmpty()

            val itemsRaw = result["order_items"] as? List<*> ?: emptyList<Any>()
            val items = mutableListOf<OrderItemDetail>()

            for (it in itemsRaw) {
                if (it is Map<*, *>) {
                    val id = it["id"]?.toString().orEmpty()
                    val qty = (it["qty"] as? Number)?.toInt() ?: 1
                    val name = it["name"]?.toString().orEmpty()
                    val sizeLabel = it["size_label"]?.toString().orEmpty()
                    val unitPrice = (it["unit_price"] as? Number)?.toDouble() ?: 0.0

                    val extrasList = mutableListOf<String>()
                    val extrasRaw = it["extras"]
                    if (extrasRaw is List<*>) {
                        extrasRaw.forEach { ex ->
                            if (ex != null) extrasList.add(ex.toString())
                        }
                    }

                    items.add(
                        OrderItemDetail(
                            id = id,
                            name = name,
                            sizeLabel = sizeLabel,
                            extras = extrasList,
                            qty = qty,
                            unitPrice = unitPrice
                        )
                    )
                }
            }

            Result.success(
                OrderDetails(
                    code = orderCode,
                    orderType = orderType,
                    subtotal = subtotal,
                    discount = discount,
                    tax = tax,
                    deliveryFee = deliveryFee,
                    total = total,
                    status = status,
                    createdAt = createdAt,
                    items = items
                )
            )
        } catch (e: Exception) {
            Log.e("QuickFoodApi", "getOrderStatus error", e)
            Result.failure(e)
        }
    }
}
