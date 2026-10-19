package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CartItem
import com.example.data.model.CartTotals
import com.example.data.model.OrderType
import com.example.ui.theme.CharcoalBg
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.EmberLight
import com.example.ui.theme.EmberPrimary
import com.example.ui.theme.PaperReceiptBg
import com.example.ui.theme.PaperReceiptBorder
import com.example.ui.theme.PaperReceiptText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartSheet(
    cartItems: List<CartItem>,
    cartTotals: CartTotals,
    orderType: OrderType,
    coupon: String?,
    couponError: String?,
    onDismiss: () -> Unit,
    onUpdateQty: (String, Int) -> Unit,
    onApplyCoupon: (String) -> Unit,
    onRemoveCoupon: () -> Unit,
    onProceedToCheckout: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var couponInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CharcoalBg,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "طلبك الحالي",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (cartItems.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.RemoveShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "لسه مفيش حاجة…",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "اختار من المنيو وهتلاقيها هنا",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Paper receipt card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(PaperReceiptBg)
                        .border(1.dp, PaperReceiptBorder, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    // Receipt header
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "QuickFood",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            ),
                            color = PaperReceiptText
                        )
                        Text(
                            text = "${orderType.label} · فاتورة مبدئية",
                            style = MaterialTheme.typography.labelSmall,
                            color = PaperReceiptText.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    DashedDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Items list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(cartItems, key = { it.key }) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = item.image,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = PaperReceiptText
                                    )

                                    val optionsText = buildList {
                                        if (item.sizeLabel.isNotBlank()) add(item.sizeLabel)
                                        addAll(item.extraLabels)
                                    }.joinToString(" · ")

                                    if (optionsText.isNotBlank()) {
                                        Text(
                                            text = optionsText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PaperReceiptText.copy(alpha = 0.65f),
                                            maxLines = 1
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Stepper
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .border(1.dp, PaperReceiptText.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        IconButton(
                                            onClick = { onUpdateQty(item.key, item.qty - 1) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "تقليل",
                                                tint = PaperReceiptText,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        Text(
                                            text = "${item.qty}",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = PaperReceiptText,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )

                                        IconButton(
                                            onClick = { onUpdateQty(item.key, item.qty + 1) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "زيادة",
                                                tint = PaperReceiptText,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "${(item.unitPrice * item.qty).toInt()} ج.م",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PaperReceiptText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    DashedDivider()
                    Spacer(modifier = Modifier.height(14.dp))

                    // Coupon form
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PaperReceiptText.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = null,
                            tint = EmberPrimary,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        OutlinedTextField(
                            value = couponInput,
                            onValueChange = { couponInput = it },
                            placeholder = {
                                Text(
                                    text = if (coupon != null) "مفعّل: $coupon" else "كود خصم (جرّب QUICK10)",
                                    fontSize = 12.sp,
                                    color = PaperReceiptText.copy(alpha = 0.5f)
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = PaperReceiptText,
                                unfocusedTextColor = PaperReceiptText
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        if (coupon != null) {
                            TextButton(onClick = onRemoveCoupon) {
                                Text(text = "إلغاء", color = Color(0xFFEF4444), fontSize = 12.sp)
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (couponInput.isNotBlank()) {
                                        onApplyCoupon(couponInput)
                                        couponInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmberPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("تطبيق", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (couponError != null) {
                        Text(
                            text = couponError,
                            color = Color(0xFFDC2626),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Financial breakdown
                    ReceiptRow(label = "المجموع", value = "${cartTotals.subtotal.toInt()} ج.م")

                    if (cartTotals.discount > 0) {
                        ReceiptRow(
                            label = "خصم كوبون",
                            value = "− ${cartTotals.discount.toInt()} ج.م",
                            highlight = true
                        )
                    }

                    ReceiptRow(label = "ضريبة ١٤٪", value = "${cartTotals.tax.toInt()} ج.م")

                    if (cartTotals.deliveryFee > 0) {
                        ReceiptRow(label = "خدمة التوصيل", value = "${cartTotals.deliveryFee.toInt()} ج.م")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    DashedDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    // Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الإجمالي المطلوب",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = PaperReceiptText
                        )
                        Text(
                            text = "${cartTotals.total.toInt()} ج.م",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            ),
                            color = EmberPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Checkout button
                Button(
                    onClick = onProceedToCheckout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmberPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("checkout_button")
                ) {
                    Text(
                        text = "كمّل الطلب · ${cartTotals.total.toInt()} ج.م",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (highlight) EmberPrimary else PaperReceiptText.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = if (highlight) EmberPrimary else PaperReceiptText
        )
    }
}

@Composable
private fun DashedDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .drawBehind {
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                drawLine(
                    color = PaperReceiptText.copy(alpha = 0.25f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2f,
                    pathEffect = pathEffect
                )
            }
    )
}
