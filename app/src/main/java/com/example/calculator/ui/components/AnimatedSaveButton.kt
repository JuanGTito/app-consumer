package com.example.calculator.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.AppColors
import kotlinx.coroutines.delay

@Composable
fun AnimatedSaveButton(
    isEditing: Boolean, isValid: Boolean, isSaving: Boolean,
    modifier: Modifier = Modifier, onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.93f else 1f, spring(0.38f, 720f), label = "sbS")
    LaunchedEffect(pressed) { if (pressed) { delay(200); pressed = false } }

    Button(
        onClick  = { pressed = true; onClick() },
        modifier = modifier.scale(scale),
        shape    = RoundedCornerShape(14.dp),
        enabled  = isValid && !isSaving,
        colors   = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp)
    ) {
        if (isSaving) {
            CircularProgressIndicator(Modifier.size(18.dp), Color.White, strokeWidth = 2.dp)
        } else {
            Icon(if (isEditing) Icons.Default.Save else Icons.Default.PersonAdd,
                null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (isEditing) "Actualizar" else "Guardar",
                fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}