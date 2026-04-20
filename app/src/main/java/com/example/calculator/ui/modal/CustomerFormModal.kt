package com.example.calculator.ui.modal

import androidx.compose.animation.core.copy
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.components.AnimatedSaveButton
import com.example.calculator.ui.theme.AppColors
import com.example.calculator.data.model.Customer
import com.example.calculator.ui.components.AnimatedFormField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerFormModal(
    customer: Customer?,
    isTablet: Boolean,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit
) {
    val isEditing = customer != null
    var name     by remember { mutableStateOf(customer?.name ?: "") }
    var lastname by remember { mutableStateOf(customer?.lastname ?: "") }
    var age      by remember { mutableStateOf(customer?.age?.toString() ?: "") }
    var phone    by remember { mutableStateOf(customer?.phone ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    val ageError = age.isNotBlank() && (age.toIntOrNull() == null || age.toInt() !in 1..120)
    val isValid  = name.isNotBlank() && lastname.isNotBlank() &&
            age.toIntOrNull()?.let { it in 1..120 } == true && phone.isNotBlank()

    // skipPartiallyExpanded → siempre abre completamente expandido
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor   = AppColors.Surface,
        dragHandle = {
            // Handle visual
            Box(
                modifier = Modifier
                    .padding(top = 14.dp, bottom = 6.dp)
                    .width(40.dp).height(4.dp)
                    .clip(CircleShape).background(AppColors.Divider)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // ★ imePadding() hace que el contenido suba exactamente lo que
                //   ocupa el teclado, manteniendo el sheet visible encima
                .imePadding()
                .padding(
                    start  = if (isTablet) 32.dp else 22.dp,
                    end    = if (isTablet) 32.dp else 22.dp,
                    top    = 4.dp,
                    bottom = 28.dp
                )
                .verticalScroll(rememberScrollState())
        ) {
            // Encabezado
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp).clip(RoundedCornerShape(14.dp))
                            .background(AppColors.Primary.copy(.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isEditing) Icons.Default.Edit else Icons.Default.PersonAdd,
                            null, tint = AppColors.Primary, modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            if (isEditing) "Editar Cliente" else "Nuevo Cliente",
                            fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary
                        )
                        Text(
                            if (isEditing) "Modifica los datos" else "Completa el formulario",
                            fontSize = 12.sp, color = AppColors.TextSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = AppColors.TextSecondary)
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = AppColors.Divider)
            Spacer(Modifier.height(18.dp))

            // Campos con animación escalonada
            AnimatedFormField(value = name, onValueChange = { name = it },
                label = "Nombre", icon = Icons.Outlined.Person, delayMs = 0)
            Spacer(Modifier.height(12.dp))
            AnimatedFormField(value = lastname, onValueChange = { lastname = it },
                label = "Apellido", icon = Icons.Outlined.Badge, delayMs = 60)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth()) {
                AnimatedFormField(
                    value = age,
                    onValueChange = { if (it.length <= 3) age = it.filter(Char::isDigit) },
                    label = "Edad", icon = Icons.Outlined.Cake,
                    keyboardType = KeyboardType.Number, isError = ageError,
                    delayMs = 120, modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                AnimatedFormField(
                    value = phone, onValueChange = { phone = it },
                    label = "Teléfono", icon = Icons.Outlined.Phone,
                    keyboardType = KeyboardType.Phone, delayMs = 180,
                    modifier = Modifier.weight(1.5f)
                )
            }
            if (ageError) {
                Text("Edad debe ser entre 1 y 120",
                    color = AppColors.Danger, fontSize = 11.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 3.dp))
            }

            Spacer(Modifier.height(26.dp))

            Row(Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, AppColors.Divider)
                ) { Text("Cancelar", fontWeight = FontWeight.Medium) }

                Spacer(Modifier.width(12.dp))

                AnimatedSaveButton(
                    isEditing = isEditing, isValid = isValid, isSaving = isSaving,
                    modifier  = Modifier.weight(1f).height(50.dp),
                    onClick = {
                        if (!isValid) return@AnimatedSaveButton
                        isSaving = true
                        onSave(Customer(
                            id       = customer?.id,
                            name     = name.trim(),
                            lastname = lastname.trim(),
                            age      = age.toInt(),
                            phone    = phone.trim()
                        ))
                    }
                )
            }
        }
    }
}