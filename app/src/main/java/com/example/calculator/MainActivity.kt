package com.example.calculator

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.calculator.ui.theme.CalculatorTheme
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// ──────────────────────────────────────────
// 1. Modelo de datos
// ──────────────────────────────────────────
data class Customer(
    val id: Int? = null,
    val name: String,
    val lastname: String,
    val age: Int,
    val phone: String
)

// ──────────────────────────────────────────
// 2. Interfaz Retrofit
// ──────────────────────────────────────────
interface ApiService {
    @GET("api/users")
    suspend fun getCustomers(): List<Customer>

    @POST("api/users")
    suspend fun createCustomer(@Body customer: Customer): Customer

    @PUT("api/users/{id}")
    suspend fun updateCustomer(@Path("id") id: Int, @Body customer: Customer): Customer

    @DELETE("api/users/{id}")
    suspend fun deleteCustomer(@Path("id") id: Int): Response<Unit>
}

// ──────────────────────────────────────────
// 3. Cliente Retrofit
// ──────────────────────────────────────────
object RetrofitClient {
    // Si usas EMULADOR: 10.0.2.2 | CELULAR FÍSICO: IP de tu PC
    private const val BASE_URL = "http://192.168.0.10:8080/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

// ──────────────────────────────────────────
// 4. Activity principal
// ──────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorTheme {
                CustomerScreen()
            }
        }
    }
}

// ──────────────────────────────────────────
// 5. Pantalla principal
// ──────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estado de datos
    var customers by remember { mutableStateOf<List<Customer>>(emptyList()) }
    var isLoadingList by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Estado del modal
    var showModal by remember { mutableStateOf(false) }
    var editingCustomer by remember { mutableStateOf<Customer?>(null) }

    // Estado de confirmación de eliminación
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }

    // Colores del tema personalizado
    val primaryColor = Color(0xFF1A56DB)
    val accentColor = Color(0xFF0E9F6E)
    val bgColor = Color(0xFFF8FAFC)
    val cardColor = Color.White
    val textPrimary = Color(0xFF1E293B)
    val textSecondary = Color(0xFF64748B)

    // Clientes filtrados por búsqueda
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
            try {
                customers = RetrofitClient.apiService.getCustomers()
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoadingList = false
            }
        }
    }

    LaunchedEffect(Unit) { refreshData() }

    // ── Modal de agregar / editar ──
    if (showModal) {
        CustomerFormModal(
            customer = editingCustomer,
            primaryColor = primaryColor,
            onDismiss = {
                showModal = false
                editingCustomer = null
            },
            onSave = { customer ->
                scope.launch {
                    try {
                        if (customer.id == null) {
                            RetrofitClient.apiService.createCustomer(customer)
                            Toast.makeText(context, "Cliente creado con éxito ✓", Toast.LENGTH_SHORT).show()
                        } else {
                            RetrofitClient.apiService.updateCustomer(customer.id, customer)
                            Toast.makeText(context, "Cliente actualizado ✓", Toast.LENGTH_SHORT).show()
                        }
                        showModal = false
                        editingCustomer = null
                        refreshData()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // ── Diálogo de confirmación de eliminación ──
    if (customerToDelete != null) {
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    "Eliminar cliente",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Text(
                    "¿Estás seguro que deseas eliminar a ${customerToDelete!!.name} ${customerToDelete!!.lastname}? Esta acción no se puede deshacer.",
                    color = textSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toDelete = customerToDelete!!
                        customerToDelete = null
                        scope.launch {
                            try {
                                RetrofitClient.apiService.deleteCustomer(toDelete.id!!)
                                refreshData()
                                Toast.makeText(context, "Cliente eliminado", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { customerToDelete = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ── Scaffold principal ──
    Scaffold(
        containerColor = bgColor,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingCustomer = null
                    showModal = true
                },
                containerColor = primaryColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo Cliente", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // ── Header con gradiente ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF1A56DB), Color(0xFF1E40AF))
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.People,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Gestión de Clientes",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${customers.size} registros en total",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Buscador ──
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Buscar por nombre, apellido o teléfono...",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Limpiar",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White.copy(alpha = 0.6f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedContainerColor = Color.White.copy(alpha = 0.15f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                            cursorColor = Color.White
                        ),
                        singleLine = true
                    )
                }
            }

            // ── Lista de clientes ──
            when {
                isLoadingList -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = primaryColor)
                            Spacer(Modifier.height(12.dp))
                            Text("Cargando clientes...", color = textSecondary, fontSize = 14.sp)
                        }
                    }
                }

                filteredCustomers.isEmpty() -> {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                if (searchQuery.isBlank()) Icons.Outlined.Group else Icons.Outlined.SearchOff,
                                contentDescription = null,
                                tint = textSecondary.copy(alpha = 0.4f),
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                if (searchQuery.isBlank()) "No hay clientes registrados"
                                else "No se encontraron resultados",
                                color = textSecondary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                if (searchQuery.isBlank()) "Presiona + para agregar el primero"
                                else "Intenta con otro término de búsqueda",
                                color = textSecondary.copy(alpha = 0.7f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, start = 32.dp, end = 32.dp)
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 100.dp // espacio para el FAB
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Indicador de resultados filtrados
                        if (searchQuery.isNotBlank()) {
                            item {
                                Text(
                                    "${filteredCustomers.size} resultado(s) para \"$searchQuery\"",
                                    color = textSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        items(filteredCustomers, key = { it.id ?: it.hashCode() }) { customer ->
                            CustomerCard(
                                customer = customer,
                                cardColor = cardColor,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                primaryColor = primaryColor,
                                onEdit = {
                                    editingCustomer = customer
                                    showModal = true
                                },
                                onDelete = {
                                    customerToDelete = customer
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────
// 6. Card de cada cliente
// ──────────────────────────────────────────
@Composable
fun CustomerCard(
    customer: Customer,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    primaryColor: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val initials = "${customer.name.firstOrNull() ?: ""}${customer.lastname.firstOrNull() ?: ""}".uppercase()
    val avatarColors = listOf(
        Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFF8B5CF6),
        Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFF06B6D4)
    )
    val avatarColor = avatarColors[(customer.id ?: customer.name.length) % avatarColors.size]

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con iniciales
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.width(14.dp))

            // Info del cliente
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${customer.name} ${customer.lastname}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Cake,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        "${customer.age} años",
                        color = textSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.width(10.dp))
                    Icon(
                        Icons.Outlined.Phone,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        customer.phone,
                        color = textSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Botones editar / eliminar
            Row {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(primaryColor.copy(alpha = 0.1f))
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(6.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.1f))
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────
// 7. Modal de formulario (Agregar / Editar)
// ──────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerFormModal(
    customer: Customer?,
    primaryColor: Color,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit
) {
    val isEditing = customer != null

    var name by remember { mutableStateOf(customer?.name ?: "") }
    var lastname by remember { mutableStateOf(customer?.lastname ?: "") }
    var age by remember { mutableStateOf(customer?.age?.toString() ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    // Validaciones básicas
    val nameError = name.isBlank() && name.length > 0 // muestra error si tocó y dejó vacío
    val ageError = age.isNotBlank() && (age.toIntOrNull() == null || age.toInt() !in 1..120)
    val isValid = name.isNotBlank() && lastname.isNotBlank() &&
            age.isNotBlank() && age.toIntOrNull() != null &&
            age.toInt() in 1..120 && phone.isNotBlank()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                // ── Título del modal ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            if (isEditing) "Editar Cliente" else "Nuevo Cliente",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            if (isEditing) "Modifica los datos del cliente" else "Completa el formulario para registrar",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Campos del formulario ──
                FormField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre *",
                    icon = Icons.Outlined.Person,
                    primaryColor = primaryColor
                )

                Spacer(Modifier.height(12.dp))

                FormField(
                    value = lastname,
                    onValueChange = { lastname = it },
                    label = "Apellido *",
                    icon = Icons.Outlined.Badge,
                    primaryColor = primaryColor
                )

                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth()) {
                    FormField(
                        value = age,
                        onValueChange = { if (it.length <= 3) age = it.filter { c -> c.isDigit() } },
                        label = "Edad *",
                        icon = Icons.Outlined.Cake,
                        keyboardType = KeyboardType.Number,
                        isError = ageError,
                        modifier = Modifier.weight(1f),
                        primaryColor = primaryColor
                    )
                    Spacer(Modifier.width(10.dp))
                    FormField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Teléfono *",
                        icon = Icons.Outlined.Phone,
                        keyboardType = KeyboardType.Phone,
                        modifier = Modifier.weight(1.4f),
                        primaryColor = primaryColor
                    )
                }

                if (ageError) {
                    Text(
                        "Edad inválida (1-120)",
                        color = Color(0xFFEF4444),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ── Botones de acción ──
                Row(Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (!isValid) return@Button
                            isSaving = true
                            onSave(
                                Customer(
                                    id = customer?.id,
                                    name = name.trim(),
                                    lastname = lastname.trim(),
                                    age = age.toInt(),
                                    phone = phone.trim()
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = isValid && !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                if (isEditing) Icons.Default.Save else Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (isEditing) "Actualizar" else "Guardar",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────
// 8. Componente reutilizable de campo
// ──────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    primaryColor: Color,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        leadingIcon = {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = isError,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = primaryColor,
            focusedLabelColor = primaryColor,
            focusedLeadingIconColor = primaryColor
        )
    )
}