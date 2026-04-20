package com.example.calculator.ui.state

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.copy
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.AppColors

@Composable
fun EmptyStateScreen(hasSearch: Boolean) {
    val inf    = rememberInfiniteTransition(label = "float")
    val floatY by inf.animateFloat(-9f, 9f,
        infiniteRepeatable(tween(1900, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "fY")

    Box(Modifier.fillMaxSize().padding(bottom = 80.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(100.dp).graphicsLayer { translationY = floatY }
                    .clip(CircleShape).background(AppColors.Primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (hasSearch) Icons.Outlined.SearchOff else Icons.Outlined.Group,
                    null, tint = AppColors.Primary.copy(alpha = 0.45f), modifier = Modifier.size(52.dp)
                )
            }
            Spacer(Modifier.height(22.dp))
            Text(
                if (hasSearch) "Sin resultados" else "Sin clientes aún",
                color = AppColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (hasSearch) "Prueba con otro término de búsqueda"
                else "Toca el botón + para agregar el primero",
                color = AppColors.TextSecondary, fontSize = 13.sp,
                textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 40.dp)
            )
        }
    }
}