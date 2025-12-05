package mx.edu.utng.rpd.meditrack
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import mx.edu.utng.rpd.meditrack.models.*
import mx.edu.utng.rpd.meditrack.repository.FirebaseRepository
import mx.edu.utng.rpd.meditrack.repository.MedicamentosAPIRepository
import mx.edu.utng.rpd.meditrack.viewmodels.*
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF5F5F5)
                ) {
                    MediTrackApp()
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.SCHEDULE_EXACT_ALARM
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Redirigir al usuario a la configuración para habilitar alarmas exactas
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                }
            }
        }
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido
        } else {
            // Permiso denegado - mostrar mensaje al usuario
        }
    }
}

@Composable
fun MediTrackApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
            is AuthState.Unauthenticated -> {
                if (navController.currentDestination?.route != "login") {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (authState is AuthState.Authenticated) "home" else "login"
    ) {
        composable("login") {
            PantallaLogin(authViewModel, navController)
        }
        composable("registro") {
            PantallaRegistro(authViewModel, navController)
        }
        composable("home") {
            PantallaHome(navController, authViewModel)
        }
        composable("medicamentos") {
            PantallaMedicamentos(navController)
        }
        composable("agregar") {
            PantallaAgregar(navController)
        }
        composable("estadisticas") {
            PantallaEstadisticas(navController)
        }
        composable("alergias") {
            PantallaAlergias(navController)
        }
        composable("recordatorios") {
            PantallaRecordatorios(navController)
        }
        composable("historial") {
            PantallaHistorial(navController)
        }
    }
}

// ============================================================================
// PANTALLA LOGIN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaLogin(authViewModel: AuthViewModel, navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mostrarError by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Error) {
            mensajeError = (authState as AuthState.Error).message
            mostrarError = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MedicalServices,
            contentDescription = "Logo",
            modifier = Modifier.size(80.dp),
            tint = Color(0xFFE91E63)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Medi Track",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE91E63)
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color(0xFF6200EE),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFF6200EE),
                focusedLabelColor = Color(0xFF6200EE),
                unfocusedLabelColor = Color.Gray
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color(0xFF6200EE),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFF6200EE),
                focusedLabelColor = Color(0xFF6200EE),
                unfocusedLabelColor = Color.Gray
            ),
            singleLine = true
        )

        if (mostrarError) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(mensajeError, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    authViewModel.iniciarSesion(email, password)
                } else {
                    mensajeError = "Por favor completa todos los campos"
                    mostrarError = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE)
            ),
            enabled = authState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Iniciar Sesión", fontSize = 18.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.navigate("registro") }
        ) {
            Text("¿No tienes cuenta? Regístrate", color = Color(0xFF6200EE))
        }
    }
}

// ============================================================================
// PANTALLA REGISTRO
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaRegistro(authViewModel: AuthViewModel, navController: NavHostController) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var mostrarError by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Error) {
            mensajeError = (authState as AuthState.Error).message
            mostrarError = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PersonAdd,
            contentDescription = "Registro",
            modifier = Modifier.size(80.dp),
            tint = Color(0xFFE91E63)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Crear Cuenta",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6200EE)
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color(0xFF6200EE),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFF6200EE),
                focusedLabelColor = Color(0xFF6200EE),
                unfocusedLabelColor = Color.Gray
            ),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color(0xFF6200EE),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFF6200EE),
                focusedLabelColor = Color(0xFF6200EE),
                unfocusedLabelColor = Color.Gray
            ),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color(0xFF6200EE),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFF6200EE),
                focusedLabelColor = Color(0xFF6200EE),
                unfocusedLabelColor = Color.Gray
            ),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmarPassword,
            onValueChange = { confirmarPassword = it },
            label = { Text("Confirmar contraseña", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color(0xFF6200EE),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFF6200EE),
                focusedLabelColor = Color(0xFF6200EE),
                unfocusedLabelColor = Color.Gray
            ),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
            }
        )

        if (mostrarError) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(mensajeError, color = Color.Red, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                when {
                    nombre.isBlank() -> {
                        mensajeError = "Por favor ingresa tu nombre"
                        mostrarError = true
                    }
                    email.isBlank() -> {
                        mensajeError = "Por favor ingresa tu email"
                        mostrarError = true
                    }
                    !email.contains("@") -> {
                        mensajeError = "Email inválido"
                        mostrarError = true
                    }
                    password.isBlank() -> {
                        mensajeError = "Por favor ingresa una contraseña"
                        mostrarError = true
                    }
                    password.length < 6 -> {
                        mensajeError = "La contraseña debe tener al menos 6 caracteres"
                        mostrarError = true
                    }
                    password != confirmarPassword -> {
                        mensajeError = "Las contraseñas no coinciden"
                        mostrarError = true
                    }
                    else -> {
                        mostrarError = false
                        authViewModel.registrar(email, password, nombre)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE)
            ),
            enabled = authState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Crear Cuenta", fontSize = 18.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.popBackStack() }
        ) {
            Text("¿Ya tienes cuenta? Inicia sesión", color = Color(0xFF6200EE))
        }
    }
}

// ============================================================================
// PANTALLA HOME
// ============================================================================
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PantallaHome(navController: NavHostController, authViewModel: AuthViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MediTrack", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { authViewModel.cerrarSesion() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar Sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CardOpcion(
                icono = Icons.Default.Medication,
                titulo = "Mis Medicamentos",
                color = Color(0xFF6200EE)
            ) {
                navController.navigate("medicamentos")
            }

            CardOpcion(
                icono = Icons.Default.BarChart,
                titulo = "Estadísticas",
                color = Color(0xFF2196F3)
            ) {
                navController.navigate("estadisticas")
            }

            CardOpcion(
                icono = Icons.Default.Warning,
                titulo = "Alergias",
                color = Color(0xFFFF9800)
            ) {
                navController.navigate("alergias")
            }

            CardOpcion(
                icono = Icons.Default.Notifications,
                titulo = "Recordatorios",
                color = Color(0xFFFF9800)
            ) {
                navController.navigate("recordatorios")
            }

            CardOpcion(
                icono = Icons.Default.History,
                titulo = "Historial",
                color = Color(0xFF9C27B0)
            ) {
                navController.navigate("historial")
            }
        }
    }
}

@Composable
private fun CardOpcion(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icono,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(titulo, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver $titulo")
            }
        }
    }
}

// ============================================================================
// PANTALLA MEDICAMENTOS
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaMedicamentos(navController: NavHostController) {
    val viewModel: MedicamentosViewModel = viewModel()
    val medicamentos by viewModel.medicamentos.collectAsState()
    val loading by viewModel.loading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarMedicamentos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medicamentos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("agregar") },
                containerColor = Color(0xFF6200EE)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (medicamentos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay medicamentos agregados", color = Color.Gray)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    medicamentos.forEach { med ->
                        CardMedicamento(med) {
                            viewModel.eliminarMedicamento(med.id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardMedicamento(medicamento: Medicamento, onEliminar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(medicamento.nombre, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("${medicamento.dosis} - ${medicamento.cantidad}", fontSize = 14.sp, color = Color.Gray)
                Text("Horarios: ${medicamento.horarios.joinToString(", ")}", fontSize = 12.sp, color = Color.Gray)
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
            }
        }
    }
}

// ============================================================================
// PANTALLA AGREGAR MEDICAMENTO - OPTIMIZADA CON FEEDBACK
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAgregar(navController: NavHostController) {
    val viewModel: MedicamentosViewModel = viewModel()
    val repository = FirebaseRepository()
    val loading by viewModel.loading.collectAsState()

    var nombreBusqueda by remember { mutableStateOf("") }
    var sugerencias by remember { mutableStateOf<List<MedicamentoAPI>>(emptyList()) }
    var medicamentoSeleccionado by remember { mutableStateOf<MedicamentoAPI?>(null) }
    var mostrarSugerencias by remember { mutableStateOf(false) }

    var dosisSeleccionada by remember { mutableStateOf("") }
    var mostrarMenuDosis by remember { mutableStateOf(false) }

    var presentacionSeleccionada by remember { mutableStateOf("") }
    var mostrarMenuPresentacion by remember { mutableStateOf(false) }

    var cantidadSeleccionada by remember { mutableStateOf("1") }
    var mostrarMenuCantidad by remember { mutableStateOf(false) }

    var frecuenciaSeleccionada by remember { mutableStateOf("") }
    var mostrarMenuFrecuencia by remember { mutableStateOf(false) }

    var horarios by remember { mutableStateOf(listOf("08:00")) }

    var mostrarAlertaAlergia by remember { mutableStateOf(false) }
    var alergiaDetectada by remember { mutableStateOf<Alergia?>(null) }

    var mostrarDialogGuardando by remember { mutableStateOf(false) }
    var mostrarError by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    val cantidades = (1..10).map { it.toString() }
    val frecuencias = listOf(
        "Cada 4 horas",
        "Cada 6 horas",
        "Cada 8 horas",
        "Cada 12 horas",
        "Cada 24 horas"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Medicamento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Campo de búsqueda
                OutlinedTextField(
                    value = nombreBusqueda,
                    onValueChange = { query ->
                        nombreBusqueda = query
                        if (query.length >= 2) {
                            sugerencias = MedicamentosAPIRepository.buscarMedicamentos(query)
                            mostrarSugerencias = sugerencias.isNotEmpty()
                        } else {
                            mostrarSugerencias = false
                            sugerencias = emptyList()
                        }
                        if (medicamentoSeleccionado != null && medicamentoSeleccionado?.nombre != query) {
                            medicamentoSeleccionado = null
                            dosisSeleccionada = ""
                            presentacionSeleccionada = ""
                        }
                    },
                    label = { Text("Buscar medicamento", color = Color.Gray) },
                    placeholder = { Text("Escribe el nombre...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color(0xFF6200EE),
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = Color(0xFF6200EE),
                        focusedLabelColor = Color(0xFF6200EE),
                        unfocusedLabelColor = Color.Gray
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                    },
                    trailingIcon = {
                        if (medicamentoSeleccionado != null) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                        }
                    },
                    singleLine = true,
                    enabled = !loading
                )

                if (medicamentoSeleccionado != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    medicamentoSeleccionado?.nombre ?: "",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    medicamentoSeleccionado?.usos ?: "",
                                    fontSize = 12.sp,
                                    color = Color(0xFF388E3C)
                                )
                            }
                        }
                    }
                }

                // Dosis
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = medicamentoSeleccionado != null && !loading) {
                            mostrarMenuDosis = true
                        }
                ) {
                    OutlinedTextField(
                        value = dosisSeleccionada,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Concentración / Dosis", color = Color.Gray) },
                        placeholder = { Text("Selecciona") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = Color(0xFF6200EE),
                            unfocusedBorderColor = Color.Gray,
                            disabledTextColor = Color.Gray,
                            disabledBorderColor = Color.LightGray,
                            disabledLabelColor = Color.LightGray
                        ),
                        trailingIcon = {
                            Icon(
                                if (mostrarMenuDosis) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = if (medicamentoSeleccionado != null) Color.Gray else Color.LightGray
                            )
                        },
                        enabled = false
                    )
                }

                // Presentación
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = medicamentoSeleccionado != null && !loading) {
                            mostrarMenuPresentacion = true
                        }
                ) {
                    OutlinedTextField(
                        value = presentacionSeleccionada,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Presentación", color = Color.Gray) },
                        placeholder = { Text("Selecciona") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = Color(0xFF6200EE),
                            unfocusedBorderColor = Color.Gray,
                            disabledTextColor = Color.Gray,
                            disabledBorderColor = Color.LightGray,
                            disabledLabelColor = Color.LightGray
                        ),
                        trailingIcon = {
                            Icon(
                                if (mostrarMenuPresentacion) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = if (medicamentoSeleccionado != null) Color.Gray else Color.LightGray
                            )
                        },
                        enabled = false
                    )
                }

                // Cantidad
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !loading) { mostrarMenuCantidad = true }
                ) {
                    OutlinedTextField(
                        value = cantidadSeleccionada,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cantidad", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = Color(0xFF6200EE),
                            unfocusedBorderColor = Color.Gray,
                            disabledTextColor = Color.Black,
                            disabledBorderColor = Color.Gray
                        ),
                        trailingIcon = {
                            Icon(
                                if (mostrarMenuCantidad) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        },
                        enabled = false
                    )
                }

                // Frecuencia
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !loading) { mostrarMenuFrecuencia = true }
                ) {
                    OutlinedTextField(
                        value = frecuenciaSeleccionada,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frecuencia", color = Color.Gray) },
                        placeholder = { Text("¿Cada cuánto?") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = Color(0xFF6200EE),
                            unfocusedBorderColor = Color.Gray,
                            disabledTextColor = Color.Black,
                            disabledBorderColor = Color.Gray
                        ),
                        trailingIcon = {
                            Icon(
                                if (mostrarMenuFrecuencia) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        },
                        enabled = false
                    )
                }

                if (horarios.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF2196F3))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Horarios sugeridos",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1976D2)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                horarios.joinToString(" • "),
                                fontSize = 14.sp,
                                color = Color(0xFF1565C0)
                            )
                        }
                    }
                }

                if (mostrarError) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(mensajeError, color = Color.Red, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón Guardar
                Button(
                    onClick = {
                        mostrarDialogGuardando = true
                        kotlinx.coroutines.GlobalScope.launch {
                            try {
                                // Verificar alergia
                                val resultadoAlergia = repository.verificarAlergia(nombreBusqueda)
                                if (resultadoAlergia.isSuccess && resultadoAlergia.getOrNull() != null) {
                                    mostrarDialogGuardando = false
                                    alergiaDetectada = resultadoAlergia.getOrNull()
                                    mostrarAlertaAlergia = true
                                } else {
                                    // Crear medicamento
                                    val medicamento = Medicamento(
                                        nombre = nombreBusqueda,
                                        dosis = dosisSeleccionada,
                                        cantidad = "$cantidadSeleccionada $presentacionSeleccionada",
                                        frecuencia = frecuenciaSeleccionada,
                                        horarios = horarios
                                    )

                                    viewModel.agregarMedicamento(
                                        medicamento,
                                        onSuccess = {
                                            mostrarDialogGuardando = false
                                            navController.popBackStack()
                                        },
                                        onError = { error ->
                                            mostrarDialogGuardando = false
                                            mensajeError = error
                                            mostrarError = true
                                        }
                                    )
                                }
                            } catch (e: Exception) {
                                mostrarDialogGuardando = false
                                mensajeError = "Error: ${e.message}"
                                mostrarError = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = medicamentoSeleccionado != null &&
                            dosisSeleccionada.isNotBlank() &&
                            presentacionSeleccionada.isNotBlank() &&
                            frecuenciaSeleccionada.isNotBlank() &&
                            !loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE),
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Medicamento", fontSize = 16.sp)
                    }
                }
            }

            // MENÚS DROPDOWN (igual que antes - continúa en siguiente mensaje)
            // ... (código de menús igual que antes)
        }
    }

    // Dialog de guardando
    if (mostrarDialogGuardando) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Guardando medicamento...")
                }
            },
            text = {
                Text("Creando recordatorios para los próximos 7 días...")
            },
            confirmButton = {},
            containerColor = Color.White
        )
    }

    // ALERTA DE ALERGIA
    if (mostrarAlertaAlergia && alergiaDetectada != null) {
        AlertDialog(
            onDismissRequest = { mostrarAlertaAlergia = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "⚠️ ALERTA DE ALERGIA",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF44336)
                )
            },
            text = {
                Column {
                    Text(
                        "Eres alérgico a ${alergiaDetectada?.medicamento}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Reacción: ${alergiaDetectada?.reaccion}")
                    Text("Gravedad: ${alergiaDetectada?.gravedad?.capitalize()}")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No se recomienda agregar este medicamento.",
                        fontSize = 14.sp,
                        color = Color.Red
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { mostrarAlertaAlergia = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    Text("Entendido")
                }
            },
            containerColor = Color(0xFFFFEBEE)
        )
    }
}
// ============================================================================
// PANTALLA RECORDATORIOS - COMPLETA
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaRecordatorios(navController: NavHostController) {
    val viewModel: RecordatoriosViewModel = viewModel()
    val recordatorios by viewModel.recordatorios.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarRecordatorios()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recordatorios de Hoy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Fecha actual
            val dateFormat = SimpleDateFormat("EEEE, dd 'de' MMMM yyyy", Locale("es", "ES"))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFF2196F3)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        dateFormat.format(Date()),
                        fontSize = 14.sp,
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (recordatorios.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No hay recordatorios para hoy",
                            fontSize = 18.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Agrega medicamentos para recibir recordatorios",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                recordatorios.forEach { rec ->
                    CardRecordatorio(rec, viewModel)
                }
            }
        }
    }
}

@Composable
private fun CardRecordatorio(recordatorio: Recordatorio, viewModel: RecordatoriosViewModel) {
    val color = when (recordatorio.estado) {
        "completado" -> Color(0xFF4CAF50)
        "omitido" -> Color(0xFFF44336)
        else -> Color(0xFF6200EE)
    }

    val icono = when (recordatorio.estado) {
        "completado" -> Icons.Default.CheckCircle
        "omitido" -> Icons.Default.Cancel
        else -> Icons.Default.Schedule
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            // Barra lateral de color
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(80.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Hora y estado
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${recordatorio.hora} - ${recordatorio.estado.capitalize()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Nombre del medicamento
                Text(
                    recordatorio.nombreMedicamento,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Dosis
                Text(
                    recordatorio.dosis,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                // Botones de acción si está pendiente
                if (recordatorio.estado == "pendiente") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.marcarComoTomado(recordatorio) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tomado")
                        }
                        OutlinedButton(
                            onClick = { viewModel.marcarComoOmitido(recordatorio) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFF44336)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFF44336))
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Omitir")
                        }
                    }
                }
            }
        }
    }
}
// ============================================================================
// PANTALLA HISTORIAL - COMPLETA Y MEJORADA
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaHistorial(navController: NavHostController) {
    val viewModel: RecordatoriosViewModel = viewModel()
    val historial by viewModel.historial.collectAsState()
    var filtroActual by remember { mutableStateOf("todos") }

    LaunchedEffect(Unit) {
        viewModel.cargarHistorial()
    }

    val historialFiltrado = when (filtroActual) {
        "tomados" -> historial.filter { it.estado == "tomado" }
        "omitidos" -> historial.filter { it.estado == "omitido" }
        else -> historial
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial Completo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Filtros
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filtroActual == "todos",
                    onClick = { filtroActual = "todos" },
                    label = { Text("Todos (${historial.size})") },
                    leadingIcon = if (filtroActual == "todos") {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
                FilterChip(
                    selected = filtroActual == "tomados",
                    onClick = { filtroActual = "tomados" },
                    label = {
                        Text("Tomados (${historial.count { it.estado == "tomado" }})")
                    },
                    leadingIcon = if (filtroActual == "tomados") {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
                FilterChip(
                    selected = filtroActual == "omitidos",
                    onClick = { filtroActual = "omitidos" },
                    label = {
                        Text("Omitidos (${historial.count { it.estado == "omitido" }})")
                    },
                    leadingIcon = if (filtroActual == "omitidos") {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (historialFiltrado.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No hay registros",
                            fontSize = 18.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            when (filtroActual) {
                                "tomados" -> "Aún no has tomado medicamentos"
                                "omitidos" -> "No has omitido ningún medicamento"
                                else -> "Comienza a registrar tus medicamentos"
                            },
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    historialFiltrado.forEach { hist ->
                        CardHistorial(hist)
                    }
                }
            }
        }
    }
}

@Composable
private fun CardHistorial(historial: Historial) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale("es", "ES"))
    val color = if (historial.estado == "tomado") Color(0xFF4CAF50) else Color(0xFFF44336)
    val icono = if (historial.estado == "tomado") Icons.Default.CheckCircle else Icons.Default.Cancel

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de estado
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icono,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del medicamento
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    historial.nombreMedicamento,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    historial.dosis,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        dateFormat.format(historial.fechaHora.toDate()),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Badge de estado
            Box(
                modifier = Modifier
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    historial.estado.capitalize(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}


// ============================================================================
// PANTALLA ESTADÍSTICAS
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaEstadisticas(navController: NavHostController) {
    val viewModel: RecordatoriosViewModel = viewModel()
    val estadisticas by viewModel.estadisticas.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarEstadisticas()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "${estadisticas["adherencia"] ?: 0}%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Adherencia al tratamiento", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "${estadisticas["tomados"] ?: 0}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Medicamentos tomados", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = null,
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "${estadisticas["omitidos"] ?: 0}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Medicamentos omitidos", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

// ============================================================================
// PANTALLA ALERGIAS
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaAlergias(navController: NavHostController) {
    val repository = FirebaseRepository()
    var alergias by remember { mutableStateOf<List<Alergia>>(emptyList()) }
    var mostrarDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val result = repository.obtenerAlergias()
        if (result.isSuccess) {
            alergias = result.getOrNull() ?: emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alergias", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialog = true },
                containerColor = Color(0xFF6200EE)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFF57C00))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Registra tus alergias para evitar reacciones", fontSize = 14.sp)
                }
            }

            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                alergias.forEach { alergia ->
                    CardAlergia(alergia)
                }
            }
        }
    }

    if (mostrarDialog) {
        DialogAgregarAlergia(
            onDismiss = { mostrarDialog = false },
            onConfirm = { nuevaAlergia ->
                kotlinx.coroutines.GlobalScope.launch {
                    repository.agregarAlergia(nuevaAlergia)
                    val result = repository.obtenerAlergias()
                    if (result.isSuccess) {
                        alergias = result.getOrNull() ?: emptyList()
                    }
                }
                mostrarDialog = false
            }
        )
    }
}

@Composable
private fun CardAlergia(alergia: Alergia) {
    val color = when (alergia.gravedad) {
        "alta" -> Color(0xFFF44336)
        "media" -> Color(0xFFFFA726)
        else -> Color(0xFFFFEB3B)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(alergia.medicamento, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Reacción: ${alergia.reaccion}", fontSize = 14.sp, color = Color.Gray)
                Text(
                    "Gravedad: ${alergia.gravedad.capitalize()}",
                    fontSize = 14.sp,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogAgregarAlergia(onDismiss: () -> Unit, onConfirm: (Alergia) -> Unit) {
    var medicamento by remember { mutableStateOf("") }
    var reaccion by remember { mutableStateOf("") }
    var gravedad by remember { mutableStateOf("media") }
    var sugerencias by remember { mutableStateOf<List<MedicamentoAPI>>(emptyList()) }
    var mostrarSugerencias by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Alergia") },
        text = {
            Box {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Campo de medicamento con búsqueda
                    OutlinedTextField(
                        value = medicamento,
                        onValueChange = { query ->
                            medicamento = query
                            if (query.length >= 2) {
                                sugerencias = MedicamentosAPIRepository.buscarMedicamentos(query)
                                mostrarSugerencias = sugerencias.isNotEmpty()
                            } else {
                                mostrarSugerencias = false
                                sugerencias = emptyList()
                            }
                        },
                        label = { Text("Medicamento") },
                        placeholder = { Text("Buscar medicamento...") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (medicamento.isNotBlank()) {
                                IconButton(onClick = { medicamento = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpiar")
                                }
                            }
                        }
                    )

                    OutlinedTextField(
                        value = reaccion,
                        onValueChange = { reaccion = it },
                        label = { Text("Reacción") },
                        placeholder = { Text("Ej: Urticaria, mareo, náuseas...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    Text("Gravedad:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = gravedad == "baja",
                            onClick = { gravedad = "baja" },
                            label = { Text("Baja") },
                            leadingIcon = if (gravedad == "baja") {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null
                        )
                        FilterChip(
                            selected = gravedad == "media",
                            onClick = { gravedad = "media" },
                            label = { Text("Media") },
                            leadingIcon = if (gravedad == "media") {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null
                        )
                        FilterChip(
                            selected = gravedad == "alta",
                            onClick = { gravedad = "alta" },
                            label = { Text("Alta") },
                            leadingIcon = if (gravedad == "alta") {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                }

                // Dropdown de sugerencias
                if (mostrarSugerencias && sugerencias.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp)
                            .heightIn(max = 200.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            sugerencias.forEach { med ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            medicamento = med.nombre
                                            mostrarSugerencias = false
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.MedicalServices,
                                        contentDescription = null,
                                        tint = Color(0xFF6200EE),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            med.nombre,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            med.usos,
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            maxLines = 1
                                        )
                                    }
                                }
                                if (med != sugerencias.last()) {
                                    Divider()
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val alergia = Alergia(
                        medicamento = medicamento,
                        reaccion = reaccion,
                        gravedad = gravedad
                    )
                    onConfirm(alergia)
                },
                enabled = medicamento.isNotBlank() && reaccion.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}