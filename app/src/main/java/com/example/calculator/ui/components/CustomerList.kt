package com.example.calculator.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.data.model.Customer
import com.example.calculator.ui.components.AnimatedCustomerCard
import com.example.calculator.ui.theme.AppColors

@Composable
fun CustomerList(
    customers: List<Customer>,
    searchQuery: String,
    horizontalPad: Dp,
    isTablet: Boolean,
    onEdit: (Customer) -> Unit,
    onDelete: (Customer) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = horizontalPad, end = horizontalPad, top = 16.dp, bottom = 110.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (searchQuery.isNotBlank()) {
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(300)) + slideInHorizontally()
                ) {
                    Text(
                        "${customers.size} resultado(s) para \"$searchQuery\"",
                        color = AppColors.TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }

        itemsIndexed(customers, key = { _, c -> c.id ?: c.hashCode() }) { index, customer ->
            AnimatedCustomerCard(
                customer = customer,
                index = index,
                isTablet = isTablet,
                onEdit = { onEdit(customer) },
                onDelete = { onDelete(customer) }
            )
        }
    }
}