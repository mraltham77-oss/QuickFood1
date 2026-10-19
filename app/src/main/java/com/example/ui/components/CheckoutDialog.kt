package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderType
import com.example.ui.theme.CharcoalBg
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.EmberLight
import com.example.ui.theme.EmberPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutDialog(
    orderType: OrderType,
    totalAmount: Double,
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (name: String, phone: String, address: String?, notes: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var tableNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var validationError by remember { mutableStateOf<String?>(null) }

    val phoneRegex = remember { Regex("^01\\d{9}$") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CharcoalBg,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "بيانات استلام الطلب",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "نوع الطلب: ${orderType.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmberLight
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .background(CharcoalSurface, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    validationError = null
                },
                label = { Text("اسمك الكريم") },
                placeholder = { Text("مثال: أحمد محمد") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = EmberLight)
                },
                singleLine = true,
                colors = textFieldColors(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkout_name_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Phone
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    validationError = null
                },
                label = { Text("رقم الموبايل (١١ رقم يبدأ بـ ٠١)") },
                placeholder = { Text("01012345678") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = EmberLight)
                },
                singleLine = true,
                colors = textFieldColors(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkout_phone_input")
            )

            // Address if Delivery
            if (orderType == OrderType.DELIVERY) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = {
                        address = it
                        validationError = null
                    },
                    label = { Text("عنوان التوصيل بالتفصيل") },
                    placeholder = { Text("المنطقة، الشارع، رقم العمارة والدور والشقة") },
                    leadingIcon = {
                        Icon(Icons.Default.Home, contentDescription = null, tint = EmberLight)
                    },
                    minLines = 2,
                    colors = textFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_address_input")
                )
            } else if (orderType == OrderType.DINE_IN) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = tableNumber,
                    onValueChange = {
                        tableNumber = it
                        validationError = null
                    },
                    label = { Text("رقم الطاولة في الصالة") },
                    placeholder = { Text("مثال: طاولة رقم ٤") },
                    leadingIcon = {
                        Icon(Icons.Default.TableRestaurant, contentDescription = null, tint = EmberLight)
                    },
                    singleLine = true,
                    colors = textFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_table_input")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Special Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("ملاحظات للشيف (اختياري)") },
                placeholder = { Text("مثال: من غير بصل، صوص زيادة...") },
                leadingIcon = {
                    Icon(Icons.Default.NoteAlt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                minLines = 2,
                colors = textFieldColors(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkout_notes_input")
            )

            // Validation & Server errors
            val errorToShow = validationError ?: errorMessage
            if (errorToShow != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorToShow,
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit order button
            Button(
                onClick = {
                    val cleanName = name.trim()
                    val cleanPhone = normalizePhone(phone)
                    val cleanAddress = address.trim()

                    if (cleanName.length < 2) {
                        validationError = "اكتب اسمك (حرفين على الأقل)"
                        return@Button
                    }
                    if (!phoneRegex.matches(cleanPhone)) {
                        validationError = "رقم الموبايل لازم ١١ رقم يبدأ بـ ٠١ (مثل: 01012345678)"
                        return@Button
                    }
                    if (orderType == OrderType.DELIVERY && cleanAddress.length < 5) {
                        validationError = "اكتب العنوان بالتفصيل عشان نوصلك بسرعة"
                        return@Button
                    }

                    val finalAddress = if (orderType == OrderType.DELIVERY) cleanAddress else tableNumber.trim()
                    onSubmit(cleanName, cleanPhone, finalAddress, notes.trim())
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmberPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_order_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "جاري إرسال الطلب للسيرفر...",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                } else {
                    Text(
                        text = "تأكيد وإرسال الطلب · ${totalAmount.toInt()} ج.م",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = CharcoalSurface,
    unfocusedContainerColor = CharcoalSurface,
    focusedBorderColor = EmberPrimary,
    unfocusedBorderColor = CharcoalBorder,
    focusedLabelColor = EmberLight,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)

private fun normalizePhone(raw: String): String {
    val sb = StringBuilder()
    for (ch in raw.trim()) {
        when (ch) {
            '٠' -> sb.append('0')
            '١' -> sb.append('1')
            '٢' -> sb.append('2')
            '٣' -> sb.append('3')
            '٤' -> sb.append('4')
            '٥' -> sb.append('5')
            '٦' -> sb.append('6')
            '٧' -> sb.append('7')
            '٨' -> sb.append('8')
            '٩' -> sb.append('9')
            in '0'..'9' -> sb.append(ch)
        }
    }
    var res = sb.toString()
    if (res.startsWith("20") && res.length == 13) {
        res = res.substring(2)
    } else if (res.startsWith("+20") && res.length == 13) {
        res = res.substring(3)
    } else if (res.startsWith("0020") && res.length == 15) {
        res = res.substring(4)
    }
    return res
}
