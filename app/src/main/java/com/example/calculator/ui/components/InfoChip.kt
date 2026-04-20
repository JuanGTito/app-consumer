package com.example.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.AppColors

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(AppColors.BgScreen)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Icon(icon, null, tint = AppColors.TextSecondary, modifier = Modifier.size(11.dp))
        Spacer(Modifier.width(3.dp))
        Text(text, color = AppColors.TextSecondary, fontSize = 11.sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}