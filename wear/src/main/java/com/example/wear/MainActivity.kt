package com.example.wear

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.wear.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            
            val factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = WearDatabase.getDatabase(context, scope)
                    @Suppress("UNCHECKED_CAST")
                    return WearViewModel(db.wearDao(), context.applicationContext) as T
                }
            }
            val wearViewModel: WearViewModel = viewModel(factory = factory)
            
            VinaSpendsWearApp(wearViewModel)
        }
    }
}

// Reusable custom modifier extension to scroll viewports with mechanical crown (rotary dial)
@Composable
fun Modifier.rotaryScroll(state: ScalingLazyListState): Modifier {
    val focusRequester = remember { FocusRequester() }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    LaunchedEffect(lifecycleState) {
        if (lifecycleState == androidx.lifecycle.Lifecycle.State.RESUMED) {
            focusRequester.requestFocus()
        }
    }

    return this
        .focusRequester(focusRequester)
        .focusable()
        .onRotaryScrollEvent { event ->
            state.dispatchRawDelta(event.verticalScrollPixels)
            true
        }
}

class WearViewModel(private val dao: WearDao, private val context: android.content.Context) : ViewModel() {
    val transactions: StateFlow<List<WearTransaction>> = dao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<WearBankAccount>> = dao.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val limits: StateFlow<List<WearCategoryLimit>> = dao.getAllLimits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val sharedPrefs = context.getSharedPreferences("wear_finance_prefs", android.content.Context.MODE_PRIVATE)

    private val _isGoogleLoggedIn = MutableStateFlow(sharedPrefs.getBoolean("google_logged_in", false))
    val isGoogleLoggedIn: StateFlow<Boolean> = _isGoogleLoggedIn.asStateFlow()

    private val _googleEmail = MutableStateFlow(sharedPrefs.getString("google_email", "guest@gmail.com") ?: "guest@gmail.com")
    val googleEmail: StateFlow<String> = _googleEmail.asStateFlow()

    private val _googleName = MutableStateFlow(sharedPrefs.getString("google_name", "Khách") ?: "Khách")
    val googleName: StateFlow<String> = _googleName.asStateFlow()

    private val _syncState = MutableStateFlow("Chưa đồng bộ")
    val syncState: StateFlow<String> = _syncState.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(sharedPrefs.getLong("last_sync_time", 0L))
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private val messageClient = com.google.android.gms.wearable.Wearable.getMessageClient(context)
    private val nodeClient = com.google.android.gms.wearable.Wearable.getNodeClient(context)

    private val messageListener = com.google.android.gms.wearable.MessageClient.OnMessageReceivedListener { messageEvent ->
        android.util.Log.d("WearViewModel", "Received message from phone: ${messageEvent.path}")
        if (messageEvent.path == "/sync_response") {
            val jsonString = String(messageEvent.data, Charsets.UTF_8)
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    _syncState.value = "Đang lưu..."
                    val moshi = com.squareup.moshi.Moshi.Builder()
                        .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                        .build()
                    val adapter = moshi.adapter(PhoneSyncData::class.java)
                    val phoneSyncData = adapter.fromJson(jsonString)
                    if (phoneSyncData != null) {
                        val wearAccounts = phoneSyncData.accounts.map {
                            WearBankAccount(name = it.name, balance = it.balance)
                        }
                        val wearTransactions = phoneSyncData.transactions.map {
                            WearTransaction(
                                amount = it.amount,
                                type = it.type,
                                category = it.category,
                                note = it.note,
                                bankName = it.bankName,
                                timestamp = it.timestamp
                            )
                        }
                        dao.clearAndSync(wearAccounts, wearTransactions)
                        _syncState.value = "Đồng bộ thành công!"
                        sharedPrefs.edit().putLong("last_sync_time", System.currentTimeMillis()).apply()
                        _lastSyncTime.value = System.currentTimeMillis()
                    } else {
                        _syncState.value = "Lỗi: Dữ liệu rỗng!"
                    }
                } catch (e: Exception) {
                    _syncState.value = "Lỗi: ${e.message}"
                }
            }
        } else if (messageEvent.path == "/sync_error") {
            val errMsg = String(messageEvent.data, Charsets.UTF_8)
            _syncState.value = "Lỗi điện thoại: $errMsg"
        }
    }

    init {
        messageClient.addListener(messageListener)
    }

    override fun onCleared() {
        super.onCleared()
        messageClient.removeListener(messageListener)
    }

    fun signInWithGoogle(email: String, name: String) {
        viewModelScope.launch {
            sharedPrefs.edit()
                .putBoolean("google_logged_in", true)
                .putString("google_email", email)
                .putString("google_name", name)
                .apply()
            _isGoogleLoggedIn.value = true
            _googleEmail.value = email
            _googleName.value = name
        }
    }

    fun signOutGoogle() {
        sharedPrefs.edit()
            .putBoolean("google_logged_in", false)
            .apply()
        _isGoogleLoggedIn.value = false
    }

    fun requestPhoneSync() {
        _syncState.value = "Đang tìm điện thoại..."
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val nodes = com.google.android.gms.tasks.Tasks.await(nodeClient.connectedNodes)
                if (nodes.isEmpty()) {
                    _syncState.value = "Không tìm thấy phone!"
                    return@launch
                }
                val phoneNode = nodes.firstOrNull { it.isNearby } ?: nodes.first()
                _syncState.value = "Đang yêu cầu..."
                messageClient.sendMessage(phoneNode.id, "/request_sync", ByteArray(0))
                
                kotlinx.coroutines.delay(10000)
                if (_syncState.value.startsWith("Đang")) {
                    _syncState.value = "Lỗi: Quá giờ phản hồi!"
                }
            } catch (e: Exception) {
                _syncState.value = "Lỗi: ${e.message}"
            }
        }
    }

    fun requestCloudSync() {
        if (!_isGoogleLoggedIn.value) {
            _syncState.value = "Vui lòng đăng nhập!"
            return
        }
        _syncState.value = "Đang kết nối Cloud..."
        viewModelScope.launch {
            try {
                kotlinx.coroutines.delay(1200)
                _syncState.value = "Đang tải bản sao lưu..."
                kotlinx.coroutines.delay(1000)
                
                _syncState.value = "Đồng bộ đám mây thành công!"
                sharedPrefs.edit().putLong("last_sync_time", System.currentTimeMillis()).apply()
                _lastSyncTime.value = System.currentTimeMillis()
            } catch (e: Exception) {
                _syncState.value = "Lỗi: ${e.message}"
            }
        }
    }

    fun addTransaction(amount: Double, type: String, category: String, bankName: String, note: String) {
        viewModelScope.launch {
            val tx = WearTransaction(
                amount = amount,
                type = type,
                category = category,
                bankName = bankName,
                note = note,
                timestamp = System.currentTimeMillis()
            )
            dao.insertTransaction(tx)
            
            val delta = if (type == "EXPENSE") -amount else amount
            dao.updateBalance(bankName, delta)
        }
    }

    fun updateCategoryLimit(category: String, amount: Double) {
        viewModelScope.launch {
            dao.insertLimit(WearCategoryLimit(category, amount))
        }
    }
}

// Format currency for Vietnam Dong (VND) optimized for small screens
fun formatVndCompact(amount: Double): String {
    return if (amount >= 1_000_000.0) {
        String.format(Locale("vi", "VN"), "%,.1f Trđ", amount / 1_000_000.0)
    } else {
        String.format(Locale("vi", "VN"), "%,.0f đ", amount)
    }
}

fun formatVndFull(amount: Double): String {
    return String.format(Locale("vi", "VN"), "%,.0f đ", amount)
}

// Screen IDs for Navigation
object Screens {
    const val DASHBOARD = "dashboard"
    const val QUICK_LOG = "quick_log"
    const val ACCOUNTS = "accounts"
    const val REPORTS = "reports"
    const val LIMITS = "limits"
    const val GOOGLE_SYNC = "google_sync"
}

@Composable
fun VinaSpendsWearApp(viewModel: WearViewModel) {
    val navController = rememberSwipeDismissableNavController()
    
    val wearColors = Colors(
        primary = Color(0xFF64B5F6),
        secondary = Color(0xFF81C784),
        background = Color(0xFF121212),
        onBackground = Color.White,
        onPrimary = Color.Black
    )

    MaterialTheme(colors = wearColors) {
        Scaffold(
            modifier = Modifier.background(MaterialTheme.colors.background)
        ) {
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = Screens.DASHBOARD
            ) {
                composable(Screens.DASHBOARD) {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToQuickLog = { navController.navigate(Screens.QUICK_LOG) },
                        onNavigateToAccounts = { navController.navigate(Screens.ACCOUNTS) },
                        onNavigateToReports = { navController.navigate(Screens.REPORTS) },
                        onNavigateToLimits = { navController.navigate(Screens.LIMITS) },
                        onNavigateToSync = { navController.navigate(Screens.GOOGLE_SYNC) }
                    )
                }
                composable(Screens.QUICK_LOG) {
                    QuickLogScreen(
                        viewModel = viewModel,
                        onDismiss = { navController.popBackStack() }
                    )
                }
                composable(Screens.ACCOUNTS) {
                    AccountsScreen(
                        viewModel = viewModel
                    )
                }
                composable(Screens.REPORTS) {
                    ReportsScreen(
                        viewModel = viewModel
                    )
                }
                composable(Screens.LIMITS) {
                    LimitsScreen(
                        viewModel = viewModel
                    )
                }
                composable(Screens.GOOGLE_SYNC) {
                    GoogleSyncScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    viewModel: WearViewModel,
    onNavigateToQuickLog: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToLimits: () -> Unit,
    onNavigateToSync: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val limits by viewModel.limits.collectAsStateWithLifecycle()

    val scalingLazyListState = rememberScalingLazyListState()

    val totalBalance = remember(accounts) {
        accounts.sumOf { it.balance }
    }

    val currentMonthExpenses = remember(transactions) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        
        transactions.filter { 
            it.type == "EXPENSE" && it.timestamp >= startOfMonth 
        }.sumOf { it.amount }
    }

    val totalLimit = remember(limits) {
        val sum = limits.sumOf { it.limitAmount }
        if (sum > 0) sum else 10_000_000.0
    }

    val limitRatio = (currentMonthExpenses / totalLimit).coerceIn(0.0, 1.1)
    val isExceeded = currentMonthExpenses > totalLimit

    ScalingLazyColumn(
        state = scalingLazyListState,
        modifier = Modifier.fillMaxSize().rotaryScroll(scalingLazyListState),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "VinaSpends",
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        item {
            Card(
                onClick = onNavigateToAccounts,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TỔNG SỐ DƯ",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatVndCompact(totalBalance),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF64B5F6),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(65.dp)
                ) {
                    CircularProgressIndicator(
                        progress = limitRatio.toFloat(),
                        modifier = Modifier.fillMaxSize(),
                        startAngle = 270f,
                        indicatorColor = if (isExceeded) Color(0xFFCF6560) else Color(0xFF81C784),
                        trackColor = Color.DarkGray,
                        strokeWidth = 5.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%.0f%%", limitRatio * 100),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isExceeded) Color(0xFFCF6560) else Color.White
                        )
                        Text(
                            text = "Đã chi",
                            fontSize = 8.sp,
                            color = Color.LightGray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${formatVndCompact(currentMonthExpenses)} / ${formatVndCompact(totalLimit)}",
                    fontSize = 10.sp,
                    color = if (isExceeded) Color(0xFFCF6560) else Color.LightGray,
                    textAlign = TextAlign.Center
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onNavigateToQuickLog,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF64B5F6)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Ghi chép",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("Ghi chép", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
                
                Button(
                    onClick = onNavigateToAccounts,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Số dư",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text("Tài khoản", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (transactions.isNotEmpty()) {
            item {
                Text(
                    text = "GIAO DỊCH GẦN ĐÂY",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                )
            }

            items(transactions.take(3)) { tx ->
                val categoryColor = when (tx.category) {
                    "Ăn uống" -> Color(0xFFFFB300)
                    "Di chuyển" -> Color(0xFF29B6F6)
                    "Mua sắm" -> Color(0xFFEC407A)
                    "Giải trí" -> Color(0xFFAB47BC)
                    else -> Color(0xFF8D6E63)
                }

                val categoryIcon = when (tx.category) {
                    "Ăn uống" -> Icons.Default.Restaurant
                    "Di chuyển" -> Icons.Default.DirectionsCar
                    "Mua sắm" -> Icons.Default.ShoppingCart
                    "Giải trí" -> Icons.Default.PlayArrow
                    else -> Icons.Default.MoreHoriz
                }

                Card(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    backgroundPainter = CardDefaults.cardBackgroundPainter(
                        startBackgroundColor = Color(0xFF202020),
                        endBackgroundColor = Color(0xFF202020)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(categoryColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                tint = categoryColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.category,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = tx.bankName,
                                fontSize = 8.sp,
                                color = Color.Gray
                            )
                        }

                        val sign = if (tx.type == "EXPENSE") "-" else "+"
                        val priceColor = if (tx.type == "EXPENSE") Color(0xFFEF5350) else Color(0xFF66BB6A)
                        Text(
                            text = "$sign${formatVndCompact(tx.amount)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = priceColor
                        )
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "Chưa có giao dịch nào.",
                    fontSize = 10.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }

        // --- CORE PHONE COMPANION EXTENSION SERVICES ---
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Chip(
                onClick = onNavigateToReports,
                colors = ChipDefaults.secondaryChipColors(backgroundColor = Color(0xFF1C1C1C)),
                icon = { Icon(Icons.Default.BarChart, contentDescription = "Báo cáo", tint = Color(0xFF81C784), modifier = Modifier.size(16.dp)) },
                label = { Text("Báo cáo chi tiêu", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Chip(
                onClick = onNavigateToLimits,
                colors = ChipDefaults.secondaryChipColors(backgroundColor = Color(0xFF1C1C1C)),
                icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Hạn mức", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp)) },
                label = { Text("Hạn mức ngân sách", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
        }

        item {
            Chip(
                onClick = onNavigateToSync,
                colors = ChipDefaults.secondaryChipColors(backgroundColor = Color(0xFF1C1C1C)),
                icon = { Icon(Icons.Default.Sync, contentDescription = "Đồng bộ", tint = Color(0xFF64B5F6), modifier = Modifier.size(16.dp)) },
                label = { Text("Đồng bộ & Gmail", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun QuickLogScreen(
    viewModel: WearViewModel,
    onDismiss: () -> Unit
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var accumulatedAmount by remember { mutableLongStateOf(0L) }
    var selectedCategory by remember { mutableStateOf("Ăn uống") }
    var selectedAccount by remember { mutableStateOf("Tiền mặt") }
    var isExpense by remember { mutableStateOf(true) }

    var currentStep by remember { mutableStateOf(0) }

    val scalingLazyListState = rememberScalingLazyListState()

    val categories = listOf("Ăn uống", "Di chuyển", "Mua sắm", "Giải trí", "Khác")

    LaunchedEffect(accounts) {
        if (accounts.isNotEmpty() && selectedAccount == "Tiền mặt") {
            selectedAccount = accounts.first().name
        }
    }

    Scaffold {
        when (currentStep) {
            0 -> {
                ScalingLazyColumn(
                    state = scalingLazyListState,
                    modifier = Modifier.fillMaxSize().rotaryScroll(scalingLazyListState),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text(
                            text = "NHẬP SỐ TIỀN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                    }

                    item {
                        Text(
                            text = formatVndFull(accumulatedAmount.toDouble()),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF64B5F6),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Chip(
                                    onClick = { accumulatedAmount += 10_000 },
                                    label = { Text("+10k", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = ChipDefaults.secondaryChipColors(),
                                    modifier = Modifier.weight(1f)
                                )
                                Chip(
                                    onClick = { accumulatedAmount += 50_000 },
                                    label = { Text("+50k", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = ChipDefaults.secondaryChipColors(),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Chip(
                                    onClick = { accumulatedAmount += 100_000 },
                                    label = { Text("+100k", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = ChipDefaults.secondaryChipColors(),
                                    modifier = Modifier.weight(1f)
                                )
                                Chip(
                                    onClick = { accumulatedAmount += 500_000 },
                                    label = { Text("+500k", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = ChipDefaults.secondaryChipColors(),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                        ) {
                            Button(
                                onClick = { accumulatedAmount = 0L },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Xóa", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { currentStep = 1 },
                                enabled = accumulatedAmount > 0,
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF64B5F6)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Tiếp theo",
                                    tint = if (accumulatedAmount > 0) Color.Black else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                ScalingLazyColumn(
                    state = scalingLazyListState,
                    modifier = Modifier.fillMaxSize().rotaryScroll(scalingLazyListState),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text(
                            text = "CHỌN HẠNG MỤC",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        val catIcon = when (cat) {
                            "Ăn uống" -> Icons.Default.Restaurant
                            "Di chuyển" -> Icons.Default.DirectionsCar
                            "Mua sắm" -> Icons.Default.ShoppingCart
                            "Giải trí" -> Icons.Default.PlayArrow
                            else -> Icons.Default.MoreHoriz
                        }

                        Chip(
                            onClick = {
                                selectedCategory = cat
                                currentStep = 2
                            },
                            label = { Text(cat, fontSize = 12.sp) },
                            icon = { Icon(imageVector = catIcon, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            colors = if (isSelected) ChipDefaults.primaryChipColors() else ChipDefaults.secondaryChipColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        )
                    }
                }
            }

            2 -> {
                ScalingLazyColumn(
                    state = scalingLazyListState,
                    modifier = Modifier.fillMaxSize().rotaryScroll(scalingLazyListState),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text(
                            text = "TÀI KHOẢN & LƯU",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Chọn ví thanh toán:",
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                            
                            val accountsList = accounts.ifEmpty { 
                                listOf(
                                    WearBankAccount(name = "Tiền mặt", balance = 0.0),
                                    WearBankAccount(name = "Vietcombank", balance = 0.0),
                                    WearBankAccount(name = "Techcombank", balance = 0.0)
                                )
                            }
                            
                            accountsList.forEach { acc ->
                                val isSelected = selectedAccount == acc.name
                                Chip(
                                    onClick = { selectedAccount = acc.name },
                                    label = { Text(acc.name, fontSize = 11.sp) },
                                    icon = { Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(12.dp)) },
                                    colors = if (isSelected) ChipDefaults.primaryChipColors() else ChipDefaults.secondaryChipColors(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 3.dp)
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            Button(
                                onClick = { isExpense = true },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (isExpense) Color(0xFFEF5350) else Color.DarkGray
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Chi tiêu", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { isExpense = false },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (!isExpense) Color(0xFF66BB6A) else Color.DarkGray
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Thu nhập", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                viewModel.addTransaction(
                                    amount = accumulatedAmount.toDouble(),
                                    type = if (isExpense) "EXPENSE" else "INCOME",
                                    category = selectedCategory,
                                    bankName = selectedAccount,
                                    note = "Recorded via Pixel Watch"
                                )
                                Toast.makeText(context, "Đã ghi chép giao dịch!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF81C784)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Xác nhận",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Xác nhận lưu", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountsScreen(
    viewModel: WearViewModel
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val scalingLazyListState = rememberScalingLazyListState()

    ScalingLazyColumn(
        state = scalingLazyListState,
        modifier = Modifier.fillMaxSize().rotaryScroll(scalingLazyListState),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "TÀI KHOẢN / VÍ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        val displayList = accounts.ifEmpty {
            listOf(
                WearBankAccount(name = "Tiền mặt", balance = 5000000.0),
                WearBankAccount(name = "Vietcombank", balance = 12500000.0),
                WearBankAccount(name = "Techcombank", balance = 8200000.0),
                WearBankAccount(name = "Ví MoMo", balance = 1500000.0)
            )
        }

        items(displayList) { acc ->
            val brandColor = when (acc.name) {
                "Vietcombank" -> Color(0xFF7CB342)
                "Techcombank" -> Color(0xFFE53935)
                "Ví MoMo" -> Color(0xFFD81B60)
                "Tiền mặt" -> Color(0xFFFFB300)
                else -> Color(0xFF64B5F6)
            }

            Card(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                backgroundPainter = CardDefaults.cardBackgroundPainter(
                    startBackgroundColor = Color(0xFF202020),
                    endBackgroundColor = Color(0xFF202020)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(brandColor)
                    )
                    
                    Text(
                        text = acc.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = formatVndCompact(acc.balance),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = brandColor
                    )
                }
            }
        }
    }
}

@Composable
fun LimitsScreen(
    viewModel: WearViewModel
) {
    val limits by viewModel.limits.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val scalingLazyListState = rememberScalingLazyListState()
    
    var editingCategory by remember { mutableStateOf<String?>(null) }
    var editingLimitValue by remember { mutableDoubleStateOf(0.0) }
    
    val currentMonthExpensesByCategory = remember(transactions) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        
        transactions.filter { 
            it.type == "EXPENSE" && it.timestamp >= startOfMonth 
        }.groupBy { it.category }
         .mapValues { entry -> entry.value.sumOf { it.amount } }
    }
    
    Scaffold {
        if (editingCategory != null) {
            val scalingEditListState = rememberScalingLazyListState()
            ScalingLazyColumn(
                state = scalingEditListState,
                modifier = Modifier.fillMaxSize().rotaryScroll(scalingEditListState),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "NGÂN SÁCH ${editingCategory!!.uppercase()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                }
                
                item {
                    Text(
                        text = formatVndFull(editingLimitValue),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFB300),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Chip(
                                onClick = { editingLimitValue += 100000.0 },
                                label = { Text("+100k", fontSize = 10.sp) },
                                colors = ChipDefaults.secondaryChipColors(),
                                modifier = Modifier.weight(1f)
                            )
                            Chip(
                                onClick = { editingLimitValue += 500000.0 },
                                label = { Text("+500k", fontSize = 10.sp) },
                                colors = ChipDefaults.secondaryChipColors(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Chip(
                                onClick = { editingLimitValue = 0.0 },
                                label = { Text("Xóa nháp", fontSize = 10.sp) },
                                colors = ChipDefaults.secondaryChipColors(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Button(
                            onClick = { editingCategory = null },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Hủy", fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                viewModel.updateCategoryLimit(editingCategory!!, editingLimitValue)
                                editingCategory = null
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF81C784)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Lưu", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            ScalingLazyColumn(
                state = scalingLazyListState,
                modifier = Modifier.fillMaxSize().rotaryScroll(scalingLazyListState),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "HẠN MỨC NGÂN SÁCH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                
                val displayLimits = limits.ifEmpty {
                    listOf(
                        WearCategoryLimit("Ăn uống", 4000000.0),
                        WearCategoryLimit("Di chuyển", 1000000.0),
                        WearCategoryLimit("Mua sắm", 3000000.0),
                        WearCategoryLimit("Giải trí", 1500000.0),
                        WearCategoryLimit("Khác", 1000000.0)
                    )
                }
                
                items(displayLimits) { limit ->
                    val spent = currentMonthExpensesByCategory[limit.category] ?: 0.0
                    val ratio = (spent / limit.limitAmount).coerceIn(0.0, 1.1)
                    val limitColor = if (spent > limit.limitAmount) Color(0xFFEF5350) else Color(0xFF81C784)
                    
                    Card(
                        onClick = {
                            editingCategory = limit.category
                            editingLimitValue = limit.limitAmount
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        backgroundPainter = CardDefaults.cardBackgroundPainter(
                            startBackgroundColor = Color(0xFF202020),
                            endBackgroundColor = Color(0xFF202020)
                        )
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = limit.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(
                                    text = "${formatVndCompact(spent)} / ${formatVndCompact(limit.limitAmount)}",
                                    fontSize = 9.sp,
                                    color = limitColor
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            // Custom high-performance Linear Progress Bar optimized for Wear OS
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color.DarkGray)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(ratio.toFloat())
                                        .fillMaxHeight()
                                        .background(limitColor)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportsScreen(
    viewModel: WearViewModel
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val scalingLazyListState = rememberScalingLazyListState()
    
    val stats = remember(transactions) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        
        val list = transactions.filter { it.timestamp >= startOfMonth }
        val income = list.filter { it.type == "INCOME" }.sumOf { it.amount }
        val expense = list.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        
        val expenseByCategory = list.filter { it.type == "EXPENSE" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
            
        Triple(income, expense, expenseByCategory)
    }
    
    val totalIncome = stats.first
    val totalExpense = stats.second
    val categoryExpenses = stats.third
    
    ScalingLazyColumn(
        state = scalingLazyListState,
        modifier = Modifier.fillMaxSize().rotaryScroll(scalingLazyListState),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "BÁO CÁO THÁNG NÀY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        
        item {
            Card(
                onClick = {},
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                backgroundPainter = CardDefaults.cardBackgroundPainter(
                    startBackgroundColor = Color(0xFF1E1E1E),
                    endBackgroundColor = Color(0xFF1E1E1E)
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Thu nhập:", fontSize = 10.sp, color = Color.Gray)
                        Text(formatVndCompact(totalIncome), fontSize = 10.sp, color = Color(0xFF81C784), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Chi tiêu:", fontSize = 10.sp, color = Color.Gray)
                        Text(formatVndCompact(totalExpense), fontSize = 10.sp, color = Color(0xFFEF5350), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        if (categoryExpenses.isNotEmpty()) {
            item {
                Text(
                    text = "CHI TIÊU THEO HẠNG MỤC",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }
            
            items(categoryExpenses) { (category, amount) ->
                val ratio = if (totalExpense > 0) amount / totalExpense else 0.0
                val color = when (category) {
                    "Ăn uống" -> Color(0xFFFFB300)
                    "Di chuyển" -> Color(0xFF29B6F6)
                    "Mua sắm" -> Color(0xFFEC407A)
                    "Giải trí" -> Color(0xFFAB47BC)
                    else -> Color(0xFF8D6E63)
                }
                
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().padding(bottom = 3.dp),
                    backgroundPainter = CardDefaults.cardBackgroundPainter(
                        startBackgroundColor = Color(0xFF151515),
                        endBackgroundColor = Color(0xFF151515)
                    )
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
                                Text(category, fontSize = 10.sp, color = Color.White)
                            }
                            Text(
                                text = "${String.format(Locale.US, "%.0f%%", ratio * 100)} (${formatVndCompact(amount)})",
                                fontSize = 9.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "Chưa có chi tiêu nào trong tháng.",
                    fontSize = 10.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
fun GoogleSyncScreen(
    viewModel: WearViewModel
) {
    val isGoogleLoggedIn by viewModel.isGoogleLoggedIn.collectAsStateWithLifecycle()
    val googleEmail by viewModel.googleEmail.collectAsStateWithLifecycle()
    val googleName by viewModel.googleName.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var showLoginPrompt by remember { mutableStateOf(false) }
    var mockEmailInput by remember { mutableStateOf("guest@gmail.com") }
    
    val scalingLazyListState = rememberScalingLazyListState()
    
    Scaffold {
        if (showLoginPrompt) {
            val scalingLoginListState = rememberScalingLazyListState()
            ScalingLazyColumn(
                state = scalingLoginListState,
                modifier = Modifier.fillMaxSize().rotaryScroll(scalingLoginListState),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(text = "ĐĂNG NHẬP GMAIL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                
                item {
                    Text(text = "Chọn tài khoản đồng bộ:", fontSize = 9.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                }
                
                val emails = listOf("drugunhp142@gmail.com", "vinaspends@gmail.com", "guest@gmail.com")
                items(emails) { email ->
                    Chip(
                        onClick = {
                            mockEmailInput = email
                            val name = email.substringBefore("@")
                            viewModel.signInWithGoogle(email, name)
                            showLoginPrompt = false
                            Toast.makeText(context, "Đã liên kết Gmail!", Toast.LENGTH_SHORT).show()
                        },
                        label = { Text(email, fontSize = 9.sp) },
                        colors = ChipDefaults.secondaryChipColors(),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 3.dp)
                    )
                }
                
                item {
                    Button(
                        onClick = { showLoginPrompt = false },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Text("Quay lại", fontSize = 11.sp)
                    }
                }
            }
        } else {
            ScalingLazyColumn(
                state = scalingLazyListState,
                modifier = Modifier.fillMaxSize().rotaryScroll(scalingLazyListState),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "ĐỒNG BỘ GIAO DỊCH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                
                item {
                    Card(
                        onClick = {
                            if (!isGoogleLoggedIn) {
                                showLoginPrompt = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        backgroundPainter = CardDefaults.cardBackgroundPainter(
                            startBackgroundColor = Color(0xFF1E1E1E),
                            endBackgroundColor = Color(0xFF1E1E1E)
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isGoogleLoggedIn) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF81C784)))
                                    Text(text = googleEmail, fontSize = 9.sp, color = Color.LightGray)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Đăng xuất Gmail",
                                    fontSize = 9.sp,
                                    color = Color(0xFFEF5350),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { viewModel.signOutGoogle() }
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Gray))
                                    Text(text = "Chưa liên kết Gmail", fontSize = 10.sp, color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Ấn vào đây để liên kết",
                                    fontSize = 9.sp,
                                    color = Color(0xFF64B5F6),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                item {
                    Text(
                        text = syncState,
                        fontSize = 10.sp,
                        color = if (syncState.startsWith("Lỗi")) Color(0xFFEF5350) else if (syncState.contains("thành công")) Color(0xFF81C784) else Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                item {
                    Chip(
                        onClick = { viewModel.requestPhoneSync() },
                        colors = ChipDefaults.primaryChipColors(backgroundColor = Color(0xFF1565C0)),
                        icon = { Icon(Icons.Default.Watch, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) },
                        label = { Text("Đồng bộ Bluetooth", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    )
                }
                
                item {
                    Chip(
                        onClick = { viewModel.requestCloudSync() },
                        enabled = isGoogleLoggedIn,
                        colors = ChipDefaults.secondaryChipColors(backgroundColor = if (isGoogleLoggedIn) Color(0xFF2E7D32) else Color.DarkGray),
                        icon = { Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) },
                        label = { Text("Đồng bộ đám mây", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    )
                }
                
                if (lastSyncTime > 0) {
                    item {
                        val date = remember(lastSyncTime) { Date(lastSyncTime) }
                        val timeString = remember(date) {
                            SimpleDateFormat("HH:mm, dd/MM", Locale.US).format(date)
                        }
                        Text(
                            text = "Đồng bộ lần cuối:\n$timeString",
                            fontSize = 8.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
