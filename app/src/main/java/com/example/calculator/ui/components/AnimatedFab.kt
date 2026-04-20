package com.example.calculator.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.AppColors
import kotlinx.coroutines.delay

@Composable
fun AnimatedFab(onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale    by animateFloatAsState(if (pressed) 0.89f else 1f, spring(0.38f, 650f), label = "fabS")
    val rotation by animateFloatAsState(if (pressed) 45f else 0f, tween(240, easing = FastOutSlowInEasing), label = "fabR")
    LaunchedEffect(pressed) { if (pressed) { delay(210); pressed = false } }

    FloatingActionButton(
        onClick        = { pressed = true; onClick() },
        containerColor = AppColors.Primary,
        contentColor   = Color.White,
        shape          = RoundedCornerShape(18.dp),
        modifier = Modifier
            .scale(scale)
            .shadow(if (pressed) 2.dp else 10.dp, RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp).rotate(rotation))
            Spacer(Modifier.width(8.dp))
            Text("Nuevo Cliente", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}