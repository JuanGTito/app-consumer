package com.example.calculator.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.copy
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.size
import androidx.compose.ui.unit.sp
import com.example.calculator.data.model.Customer
import com.example.calculator.ui.theme.AppColors
import kotlinx.coroutines.delay

@Composable
fun AnimatedCustomerCard(
    customer: Customer,
    index: Int,
    isTablet: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay((index * 55L).coerceAtMost(380L)); visible = true }

    val animSpec   = spring<Float>(dampingRatio = 0.72f, stiffness = 260f)
    val offsetY    by animateFloatAsState(if (visible) 0f else 64f, animSpec, label = "cY$index")
    val alpha      by animateFloatAsState(if (visible) 1f else 0f, tween(300), label = "cA$index")
    val rotX       by animateFloatAsState(if (visible) 0f else -14f, animSpec, label = "cRX$index")
    var pressed    by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(if (pressed) 0.97f else 1f, spring(0.4f, 600f), label = "pS$index")
    val pressShadow by animateDpAsState(if (pressed) 1.dp else 4.dp, tween(150), label = "pSh$index")
    val pressRotX   by animateFloatAsState(if (pressed) 2.5f else 0f, spring(0.5f, 400f), label = "pRX$index")

    val initials    = "${customer.name.firstOrNull() ?: ""}${customer.lastname.firstOrNull() ?: ""}".uppercase()
    val avatarColor = AppColors.AvatarPalette[(customer.id ?: customer.name.length) % AppColors.AvatarPalette.size]

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY   = offsetY
                this.alpha     = alpha
                rotationX      = rotX + pressRotX
                cameraDistance = 10f * density
                scaleX         = pressScale
                scaleY         = pressScale
            }
    ) {
        Card(
            modifier  = Modifier.fillMaxWidth().shadow(pressShadow, RoundedCornerShape(18.dp)),
            shape     = RoundedCornerShape(18.dp),
            colors    = CardDefaults.cardColors(containerColor = AppColors.Surface),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(if (isTablet) 20.dp else 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isTablet) 54.dp else 46.dp)
                        .shadow(8.dp, CircleShape, spotColor = avatarColor.copy(alpha = 0.45f))
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(avatarColor, avatarColor.copy(alpha = 0.7f)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, color = Color.White, fontWeight = FontWeight.Bold,
                        fontSize = if (isTablet) 18.sp else 15.sp)
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${customer.name} ${customer.lastname}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = if (isTablet) 16.sp else 14.sp,
                        color = AppColors.TextPrimary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(5.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InfoChip(Icons.Outlined.Cake, "${customer.age} años")
                        Spacer(Modifier.width(8.dp))
                        InfoChip(Icons.Outlined.Phone, customer.phone)
                    }
                }

                Spacer(Modifier.width(8.dp))

                Row {
                    ActionIconButton(Icons.Default.Edit, AppColors.Primary, AppColors.Primary.copy(.1f),
                        if (isTablet) 40.dp else 34.dp, onEdit)
                    Spacer(Modifier.width(6.dp))
                    ActionIconButton(Icons.Default.Delete, AppColors.Danger, AppColors.Danger.copy(.1f),
                        if (isTablet) 40.dp else 34.dp, onDelete)
                }
            }
        }
    }
}