package com.example.calculator

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.example.calculator.ui.theme.CalculatorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// ══════════════════════════════════════════════════════════
// 1. Modelo de datos
// ══════════════════════════════════════════════════════════
data class Customer(
    val id: Int? = null,
    val name: String,
    val lastname: String,
    val age: Int,
    val phone: String
)

// ══════════════════════════════════════════════════════════
// 2. Interfaz Retrofit
// ══════════════════════════════════════════════════════════
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

// ══════════════════════════════════════════════════════════
// 3. Cliente Retrofit
// ══════════════════════════════════════════════════════════
object RetrofitClient {
    // EMULADOR → 10.0.2.2 | CELULAR FÍSICO → IP de tu PC
    private const val BASE_URL = "http://192.168.0.10:8080/"
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

// ══════════════════════════════════════════════════════════
// 4. Paleta de colores centralizada
// ══════════════════════════════════════════════════════════
object AppColors {
    val Primary       = Color(0xFF1A56DB)
    val PrimaryDark   = Color(0xFF1E40AF)
    val Danger        = Color(0xFFEF4444)
    val BgScreen      = Color(0xFFF1F5F9)
    val Surface       = Color.White
    val TextPrimary   = Color(0xFF0F172A)
    val TextSecondary = Color(0xFF64748B)
    val Divider       = Color(0xFFE2E8F0)

    val AvatarPalette = listOf(
        Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFF8B5CF6),
        Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFF06B6D4),
        Color(0xFFEC4899), Color(0xFF14B8A6)
    )
}

// ══════════════════════════════════════════════════════════
// 5. Activity
// ══════════════════════════════════════════════════════════
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CalculatorTheme { CustomerScreen() } }
    }
}

// ══════════════════════════════════════════════════════════
// 6. Pantalla principal
// ══════════════════════════════════════════════════════════
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

// ══════════════════════════════════════════════════════════
// 7. Header
// ══════════════════════════════════════════════════════════
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

// ══════════════════════════════════════════════════════════
// 8. FAB animado
// ══════════════════════════════════════════════════════════
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

// ══════════════════════════════════════════════════════════
// 9. Lista con stagger
// ══════════════════════════════════════════════════════════
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
                AnimatedVisibility(visible = true, enter = fadeIn(tween(300)) + slideInHorizontally()) {
                    Text(
                        "${customers.size} resultado(s) para \"$searchQuery\"",
                        color = AppColors.TextSecondary, fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }

        itemsIndexed(customers, key = { _, c -> c.id ?: c.hashCode() }) { index, customer ->
            AnimatedCustomerCard(
                customer = customer, index = index, isTablet = isTablet,
                onEdit = { onEdit(customer) }, onDelete = { onDelete(customer) }
            )
        }
    }
}

// ══════════════════════════════════════════════════════════
// 10. Card con efecto 3D
// ══════════════════════════════════════════════════════════
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

// ══════════════════════════════════════════════════════════
// 11. InfoChip
// ══════════════════════════════════════════════════════════
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

// ══════════════════════════════════════════════════════════
// 12. ActionIconButton con press 3D
// ══════════════════════════════════════════════════════════
@Composable
fun ActionIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color, bgTint: Color, size: Dp, onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.80f else 1f, spring(0.33f, 720f), label = "ais")
    LaunchedEffect(pressed) { if (pressed) { delay(180); pressed = false } }

    Box(
        modifier = Modifier
            .size(size).scale(scale)
            .clip(RoundedCornerShape(10.dp)).background(bgTint)
            .clickable(remember { MutableInteractionSource() }, null) { pressed = true; onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size((size.value * 0.48f).dp))
    }
}

// ══════════════════════════════════════════════════════════
// 13. PulsingLoadingScreen
// ══════════════════════════════════════════════════════════
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

// ══════════════════════════════════════════════════════════
// 14. EmptyStateScreen
// ══════════════════════════════════════════════════════════
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

// ══════════════════════════════════════════════════════════
// 15. DeleteConfirmDialog
// ══════════════════════════════════════════════════════════
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

// ══════════════════════════════════════════════════════════
// 16. Modal — ModalBottomSheet nativo (sube sobre el teclado)
// ══════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerFormModal(
    customer: Customer?,
    isTablet: Boolean,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit
) {
    val isEditing = customer != null
    var name     by remember { mutableStateOf(customer?.name ?: "") }
    var lastname by remember { mutableStateOf(customer?.lastname ?: "") }
    var age      by remember { mutableStateOf(customer?.age?.toString() ?: "") }
    var phone    by remember { mutableStateOf(customer?.phone ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    val ageError = age.isNotBlank() && (age.toIntOrNull() == null || age.toInt() !in 1..120)
    val isValid  = name.isNotBlank() && lastname.isNotBlank() &&
            age.toIntOrNull()?.let { it in 1..120 } == true && phone.isNotBlank()

    // skipPartiallyExpanded → siempre abre completamente expandido
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor   = AppColors.Surface,
        dragHandle = {
            // Handle visual
            Box(
                modifier = Modifier
                    .padding(top = 14.dp, bottom = 6.dp)
                    .width(40.dp).height(4.dp)
                    .clip(CircleShape).background(AppColors.Divider)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // ★ imePadding() hace que el contenido suba exactamente lo que
                //   ocupa el teclado, manteniendo el sheet visible encima
                .imePadding()
                .padding(
                    start  = if (isTablet) 32.dp else 22.dp,
                    end    = if (isTablet) 32.dp else 22.dp,
                    top    = 4.dp,
                    bottom = 28.dp
                )
                .verticalScroll(rememberScrollState())
        ) {
            // Encabezado
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp).clip(RoundedCornerShape(14.dp))
                            .background(AppColors.Primary.copy(.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isEditing) Icons.Default.Edit else Icons.Default.PersonAdd,
                            null, tint = AppColors.Primary, modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            if (isEditing) "Editar Cliente" else "Nuevo Cliente",
                            fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary
                        )
                        Text(
                            if (isEditing) "Modifica los datos" else "Completa el formulario",
                            fontSize = 12.sp, color = AppColors.TextSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = AppColors.TextSecondary)
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = AppColors.Divider)
            Spacer(Modifier.height(18.dp))

            // Campos con animación escalonada
            AnimatedFormField(value = name, onValueChange = { name = it },
                label = "Nombre", icon = Icons.Outlined.Person, delayMs = 0)
            Spacer(Modifier.height(12.dp))
            AnimatedFormField(value = lastname, onValueChange = { lastname = it },
                label = "Apellido", icon = Icons.Outlined.Badge, delayMs = 60)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth()) {
                AnimatedFormField(
                    value = age,
                    onValueChange = { if (it.length <= 3) age = it.filter(Char::isDigit) },
                    label = "Edad", icon = Icons.Outlined.Cake,
                    keyboardType = KeyboardType.Number, isError = ageError,
                    delayMs = 120, modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                AnimatedFormField(
                    value = phone, onValueChange = { phone = it },
                    label = "Teléfono", icon = Icons.Outlined.Phone,
                    keyboardType = KeyboardType.Phone, delayMs = 180,
                    modifier = Modifier.weight(1.5f)
                )
            }
            if (ageError) {
                Text("Edad debe ser entre 1 y 120",
                    color = AppColors.Danger, fontSize = 11.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 3.dp))
            }

            Spacer(Modifier.height(26.dp))

            Row(Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, AppColors.Divider)
                ) { Text("Cancelar", fontWeight = FontWeight.Medium) }

                Spacer(Modifier.width(12.dp))

                AnimatedSaveButton(
                    isEditing = isEditing, isValid = isValid, isSaving = isSaving,
                    modifier  = Modifier.weight(1f).height(50.dp),
                    onClick = {
                        if (!isValid) return@AnimatedSaveButton
                        isSaving = true
                        onSave(Customer(
                            id       = customer?.id,
                            name     = name.trim(),
                            lastname = lastname.trim(),
                            age      = age.toInt(),
                            phone    = phone.trim()
                        ))
                    }
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// 17. AnimatedFormField
// ══════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    delayMs: Int = 0,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(delayMs.toLong()); visible = true }

    val offsetX by animateFloatAsState(if (visible) 0f else 28f, spring(0.8f, 340f), label = "fX$label")
    val alpha   by animateFloatAsState(if (visible) 1f else 0f, tween(280), label = "fA$label")

    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        leadingIcon = { Icon(icon, null, modifier = Modifier.size(18.dp)) },
        modifier = modifier.graphicsLayer { translationX = offsetX; this.alpha = alpha },
        shape = RoundedCornerShape(14.dp), singleLine = true, isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = AppColors.Primary,
            focusedLabelColor       = AppColors.Primary,
            focusedLeadingIconColor = AppColors.Primary,
            errorBorderColor        = AppColors.Danger
        )
    )
}

// ══════════════════════════════════════════════════════════
// 18. AnimatedSaveButton
// ══════════════════════════════════════════════════════════
@Composable
fun AnimatedSaveButton(
    isEditing: Boolean, isValid: Boolean, isSaving: Boolean,
    modifier: Modifier = Modifier, onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.93f else 1f, spring(0.38f, 720f), label = "sbS")
    LaunchedEffect(pressed) { if (pressed) { delay(200); pressed = false } }

    Button(
        onClick  = { pressed = true; onClick() },
        modifier = modifier.scale(scale),
        shape    = RoundedCornerShape(14.dp),
        enabled  = isValid && !isSaving,
        colors   = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp)
    ) {
        if (isSaving) {
            CircularProgressIndicator(Modifier.size(18.dp), Color.White, strokeWidth = 2.dp)
        } else {
            Icon(if (isEditing) Icons.Default.Save else Icons.Default.PersonAdd,
                null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (isEditing) "Actualizar" else "Guardar",
                fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}