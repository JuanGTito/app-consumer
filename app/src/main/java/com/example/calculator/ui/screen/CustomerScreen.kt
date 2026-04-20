package com.example.calculator.ui.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.calculator.data.model.Customer
import com.example.calculator.data.remote.RetrofitClient
import com.example.calculator.ui.components.AnimatedFab
import com.example.calculator.ui.components.AppHeader
import com.example.calculator.ui.components.CustomerList
import com.example.calculator.ui.dialog.DeleteConfirmDialog
import com.example.calculator.ui.modal.CustomerFormModal
import com.example.calculator.ui.state.EmptyStateScreen
import com.example.calculator.ui.state.PulsingLoadingScreen
import com.example.calculator.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CustomerScreen() {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val config  = LocalConfiguration.current

    val isTablet      = config.screenWidthDp >= 600
    val horizontalPad = if (isTablet) 32.dp else 16.dp

    var customers        by remember { mutableStateOf<List<Customer>>(emptyList()) }
    var isLoadingList    by remember { mutableStateOf(false) }
    var searchQuery      by remember { mutableStateOf("") }
    var showModal        by remember { mutableStateOf(false) }
    var editingCustomer  by remember { mutableStateOf<Customer?>(null) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }

    var headerVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); headerVisible = true }

    val filteredCustomers = remember(customers, searchQuery) {
        if (searchQuery.isBlank()) customers
        else customers.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.lastname.contains(searchQuery, ignoreCase = true) ||
                    it.phone.contains(searchQuery, ignoreCase = true)
        }
    }

    fun refreshData() {
        scope.launch {
            isLoadingList = true
            try { customers = RetrofitClient.apiService.getCustomers() }
            catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally { isLoadingList = false }
        }
    }

    LaunchedEffect(Unit) { refreshData() }

    // ── ModalBottomSheet para agregar / editar ──
    if (showModal) {
        CustomerFormModal(
            customer = editingCustomer,
            isTablet = isTablet,
            onDismiss = { showModal = false; editingCustomer = null },
            onSave = { customer ->
                scope.launch {
                    try {
                        if (customer.id == null) {
                            RetrofitClient.apiService.createCustomer(customer)
                            Toast.makeText(context, "Cliente creado ✓", Toast.LENGTH_SHORT).show()
                        } else {
                            RetrofitClient.apiService.updateCustomer(customer.id, customer)
                            Toast.makeText(context, "Actualizado ✓", Toast.LENGTH_SHORT).show()
                        }
                        showModal = false; editingCustomer = null; refreshData()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error al guardar", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // ── Diálogo eliminar ──
    customerToDelete?.let { pending ->
        DeleteConfirmDialog(
            customer = pending,
            onConfirm = {
                customerToDelete = null
                scope.launch {
                    try {
                        RetrofitClient.apiService.deleteCustomer(pending.id!!)
                        refreshData()
                        Toast.makeText(context, "Eliminado", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = { customerToDelete = null }
        )
    }

    Scaffold(
        containerColor = AppColors.BgScreen,
        floatingActionButton = {
            AnimatedFab(onClick = { editingCustomer = null; showModal = true })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedVisibility(
                visible = headerVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = spring(dampingRatio = 0.72f, stiffness = 280f)
                ) + fadeIn(tween(400))
            ) {
                AppHeader(
                    totalCount     = customers.size,
                    searchQuery    = searchQuery,
                    onSearchChange = { searchQuery = it },
                    onClearSearch  = { searchQuery = "" },
                    horizontalPad  = horizontalPad
                )
            }

            when {
                isLoadingList               -> PulsingLoadingScreen()
                filteredCustomers.isEmpty() -> EmptyStateScreen(hasSearch = searchQuery.isNotBlank())
                else -> CustomerList(
                    customers     = filteredCustomers,
                    searchQuery   = searchQuery,
                    horizontalPad = horizontalPad,
                    isTablet      = isTablet,
                    onEdit        = { c -> editingCustomer = c; showModal = true },
                    onDelete      = { c -> customerToDelete = c }
                )
            }
        }
    }
}