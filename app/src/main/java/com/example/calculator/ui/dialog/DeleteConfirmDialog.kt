package com.example.calculator.ui.dialog

import androidx.compose.animation.core.copy
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.AppColors
import com.example.calculator.data.model.Customer

@Composable
fun DeleteConfirmDialog(customer: Customer, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = AppColors.Surface,
        icon = {
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape).background(AppColors.Danger.copy(.1f)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Delete, null, tint = AppColors.Danger, modifier = Modifier.size(26.dp)) }
        },
        title = {
            Text("Eliminar cliente", fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary, textAlign = TextAlign.Center)
        },
        text = {
            Text(
                "¿Eliminar a ${customer.name} ${customer.lastname}? Esta acción no se puede deshacer.",
                color = AppColors.TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Danger),
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
            ) { Text("Sí, eliminar", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss,
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
            ) { Text("Cancelar") }
        }
    )
}