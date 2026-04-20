package com.example.calculator.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.AppColors

@Composable
fun AppHeader(
    totalCount: Int,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    horizontalPad: Dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(AppColors.Primary, AppColors.PrimaryDark),
                    start  = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end    = androidx.compose.ui.geometry.Offset(1000f, 400f)
                )
            )
            .padding(horizontal = horizontalPad, vertical = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .size(170.dp).offset(x = 220.dp, y = (-45).dp)
                .clip(CircleShape).background(Color.White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(90.dp).offset(x = 290.dp, y = 28.dp)
                .clip(CircleShape).background(Color.White.copy(alpha = 0.04f))
        )

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp).clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PeopleAlt, null, tint = Color.White, modifier = Modifier.size(25.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        "Gestión de Clientes",
                        color = Color.White, fontSize = 20.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 0.2.sp
                    )
                    AnimatedContent(
                        targetState = totalCount,
                        transitionSpec = {
                            (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
                        },
                        label = "count"
                    ) { count ->
                        Text(
                            "$count ${if (count == 1) "registro" else "registros"}",
                            color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text("Buscar nombre, apellido o teléfono...",
                        color = Color.White.copy(alpha = 0.55f), fontSize = 13.sp)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, null,
                        tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = searchQuery.isNotBlank(),
                        enter = scaleIn(tween(150)) + fadeIn(),
                        exit  = scaleOut(tween(150)) + fadeOut()
                    ) {
                        IconButton(onClick = onClearSearch, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, null,
                                tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(17.dp))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor        = Color.White,
                    unfocusedTextColor      = Color.White,
                    focusedBorderColor      = Color.White.copy(alpha = 0.7f),
                    unfocusedBorderColor    = Color.White.copy(alpha = 0.3f),
                    focusedContainerColor   = Color.White.copy(alpha = 0.15f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                    cursorColor             = Color.White
                )
            )
        }
    }
}