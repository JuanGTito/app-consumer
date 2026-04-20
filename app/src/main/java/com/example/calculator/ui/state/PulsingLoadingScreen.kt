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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.AppColors

@Composable
fun PulsingLoadingScreen() {
    val inf   = rememberInfiniteTransition(label = "pulse")
    val pulse by inf.animateFloat(0.82f, 1.12f,
        infiniteRepeatable(tween(850, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pS")
    val alpha by inf.animateFloat(0.45f, 1f,
        infiniteRepeatable(tween(850), RepeatMode.Reverse), label = "pA")

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(74.dp).scale(pulse).clip(CircleShape)
                    .background(AppColors.Primary.copy(alpha = 0.11f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = AppColors.Primary,
                    modifier = Modifier.size(38.dp).graphicsLayer { this.alpha = alpha },
                    strokeWidth = 3.dp
                )
            }
            Spacer(Modifier.height(18.dp))
            Text("Cargando clientes...", color = AppColors.TextSecondary, fontSize = 14.sp)
        }
    }
}