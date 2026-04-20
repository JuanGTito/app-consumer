package com.example.calculator.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.AppColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    delayMs: Int = 0,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(delayMs.toLong()); visible = true }

    val offsetX by animateFloatAsState(if (visible) 0f else 28f, spring(0.8f, 340f), label = "fX$label")
    val alpha   by animateFloatAsState(if (visible) 1f else 0f, tween(280), label = "fA$label")

    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        leadingIcon = { Icon(icon, null, modifier = Modifier.size(18.dp)) },
        modifier = modifier.graphicsLayer { translationX = offsetX; this.alpha = alpha },
        shape = RoundedCornerShape(14.dp), singleLine = true, isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = AppColors.Primary,
            focusedLabelColor       = AppColors.Primary,
            focusedLeadingIconColor = AppColors.Primary,
            errorBorderColor        = AppColors.Danger
        )
    )
}