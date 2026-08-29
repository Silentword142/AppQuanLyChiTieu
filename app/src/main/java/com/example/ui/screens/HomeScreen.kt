package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.api.ParsedTransaction
import com.example.data.model.BankAccount
import com.example.data.model.TransactionEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AiParseState
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import coil.compose.SubcomposeAsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.ui.viewmodel.ReportPeriod
import java.text.DecimalFormat
import androidx.compose.foundation.BorderStroke
import java.util.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlinx.coroutines.launch

// Helper currency formatter
fun formatVnd(amount: Double): String {
    val formatter = DecimalFormat("#,###")
    return if (amount >= 0) {
        "${formatter.format(amount)} ₫"
    } else {
        "-${formatter.format(Math.abs(amount))} ₫"
    }
}

// Helper timestamp formatter
fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// Thousands input formatting helper
fun formatInputThousands(input: String): String {
    val clean = input.replace(",", "").replace(".", "")
    val isNegative = clean.startsWith("-")
    val cleanDigits = clean.filter { it.isDigit() }
    if (cleanDigits.isEmpty()) return if (isNegative) "-" else ""
    val number = cleanDigits.toLongOrNull() ?: return cleanDigits
    
    val symbols = java.text.DecimalFormatSymbols(Locale.US)
    symbols.groupingSeparator = ','
    val formatter = DecimalFormat("#,###", symbols)
    val formatted = formatter.format(number)
    return if (isNegative) "-$formatted" else formatted
}

fun uriToBase64(context: android.content.Context, uri: Uri): Pair<String, String>? {
    val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
    return try {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } ?: return null
        
        // Scale down to avoid huge request payloads
        val maxDim = 800
        val srcWidth = bitmap.width
        val srcHeight = bitmap.height
        val scaledBitmap = if (srcWidth > maxDim || srcHeight > maxDim) {
            val ratio = srcWidth.toFloat() / srcHeight.toFloat()
            val (w, h) = if (ratio > 1) {
                Pair(maxDim, (maxDim / ratio).toInt())
            } else {
                Pair((maxDim * ratio).toInt(), maxDim)
            }
            Bitmap.createScaledBitmap(bitmap, w, h, true)
        } else {
            bitmap
        }
        
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        Pair(base64, mimeType)
    } catch (e: Exception) {
        android.util.Log.e("HomeScreen", "Error decoding uri to base64", e)
        null
    }
}

fun parseInputThousands(input: String): Double {
    val clean = input.replace(",", "").replace(".", "")
    return clean.toDoubleOrNull() ?: 0.0
}

fun formatTextFieldValue(input: TextFieldValue, allowNegative: Boolean = false): TextFieldValue {
    val rawText = input.text
    val isNegative = allowNegative && rawText.startsWith("-")
    
    // Count digits before cursor
    var digitsBeforeCursor = 0
    val selectionStart = input.selection.start
    for (i in 0 until selectionStart.coerceAtMost(rawText.length)) {
        if (rawText[i].isDigit()) {
            digitsBeforeCursor++
        }
    }
    
    // Filter digits
    val digits = rawText.filter { it.isDigit() }
    if (digits.isEmpty()) {
        val blankText = if (isNegative) "-" else ""
        return TextFieldValue(text = blankText, selection = TextRange(blankText.length))
    }
    
    // Group any digit string from right to left with commas to preserve leading zeros/digits position
    val sb = StringBuilder()
    val len = digits.length
    for (i in 0 until len) {
        if (i > 0 && i % 3 == 0) {
            sb.append(',')
        }
        sb.append(digits[len - 1 - i])
    }
    val formattedDigits = sb.reverse().toString()
    val formattedText = if (isNegative) "-$formattedDigits" else formattedDigits
    
    // Find new cursor position
    var newCursorPos = 0
    var digitsSeen = 0
    while (newCursorPos < formattedText.length && digitsSeen < digitsBeforeCursor) {
        if (formattedText[newCursorPos].isDigit()) {
            digitsSeen++
        }
        newCursorPos++
    }
    
    if (isNegative && selectionStart > 0 && newCursorPos == 0) {
        newCursorPos = 1
    }
    
    return TextFieldValue(
        text = formattedText,
        selection = TextRange(newCursorPos)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val filteredTxs by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val summary by viewModel.summaryReport.collectAsStateWithLifecycle()
    val aiState by viewModel.aiParseState.collectAsStateWithLifecycle()
    val isSyncingBank by viewModel.isSyncingBank.collectAsStateWithLifecycle()
    val categoriesList by viewModel.allCategories.collectAsStateWithLifecycle(initialValue = viewModel.defaultCategories)
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val categoryLimits by viewModel.categoryLimits.collectAsStateWithLifecycle()
    val categoryParentMap by viewModel.categoryParentMap.collectAsStateWithLifecycle()

    val currentMonthExpensesByCategory = remember(transactions, categoryParentMap) {
        val now = java.util.Calendar.getInstance()
        now.set(java.util.Calendar.DAY_OF_MONTH, 1)
        now.set(java.util.Calendar.HOUR_OF_DAY, 0)
        now.set(java.util.Calendar.MINUTE, 0)
        now.set(java.util.Calendar.SECOND, 0)
        now.set(java.util.Calendar.MILLISECOND, 0)
        val startOfMonth = now.timeInMillis
        
        val map = mutableMapOf<String, Double>()
        transactions.filter { 
            it.type == "EXPENSE" && it.timestamp >= startOfMonth 
        }.forEach { tx ->
            val targetCategory = categoryParentMap[tx.category] ?: tx.category
            map[targetCategory] = (map[targetCategory] ?: 0.0) + tx.amount
        }
        map
    }

    BackHandler(enabled = selectedCategory != "Tất cả") {
        viewModel.setSelectedCategory("Tất cả")
    }

    var activeTab by remember { mutableStateOf(0) } // 0: Overview, 1: Reports, 2: Linking/AI
    var showAddManualDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var editingAccountForBalance by remember { mutableStateOf<BankAccount?>(null) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var topCategoryToCustomize by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.horizontalGradient(listOf(VintageSageGreen, VintageDuskyRose)))
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FilterVintage,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = "VinaSpends",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = "Quản lý tài chính tối ưu",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            showThemeDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Cài đặt Giao diện & Chủ đề",
                            tint = SleekDeepPurple
                        )
                    }
                    IconButton(
                        onClick = {
                            showSettingsDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Cấu hình API Gemini",
                            tint = SleekDeepPurple
                        )
                    }
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Trình kiểm tra bảo mật VietQR & NAPAS 247 an toàn.", Toast.LENGTH_LONG).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Security,
                            contentDescription = "Bảo mật",
                            tint = EmeraldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Tổng quan") },
                    label = { Text("Tổng quan", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SleekDeepPurple,
                        selectedTextColor = SleekDeepPurple,
                        indicatorColor = SleekLightLavender,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(imageVector = Icons.Default.BarChart, contentDescription = "Báo cáo") },
                    label = { Text("Báo cáo", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SleekDeepPurple,
                        selectedTextColor = SleekDeepPurple,
                        indicatorColor = SleekLightLavender,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    icon = { Icon(imageVector = Icons.Default.Category, contentDescription = "Hạng mục") },
                    label = { Text("Hạng mục", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SleekDeepPurple,
                        selectedTextColor = SleekDeepPurple,
                        indicatorColor = SleekLightLavender,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Quản lý Ví/AI") },
                    label = { Text("Ví & Trợ lý AI", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SleekDeepPurple,
                        selectedTextColor = SleekDeepPurple,
                        indicatorColor = SleekLightLavender,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        },
        floatingActionButton = {
            if (activeTab == 0 || activeTab == 1) {
                FloatingActionButton(
                    onClick = { showAddManualDialog = true },
                    containerColor = EmeraldPrimary,
                    contentColor = Color.Black,
                    modifier = Modifier.testTag("add_transaction_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm chi tiêu")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)) { width -> width } + fadeIn(animationSpec = spring()))
                            .togetherWith(slideOutHorizontally(animationSpec = spring()) { width -> -width } + fadeOut())
                    } else {
                        (slideInHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)) { width -> -width } + fadeIn(animationSpec = spring()))
                            .togetherWith(slideOutHorizontally(animationSpec = spring()) { width -> width } + fadeOut())
                    }
                },
                label = "TabContentAnimation"
            ) { targetTab ->
                when (targetTab) {
                    0 -> OverviewTabContent(
                        transactions = transactions,
                        accounts = accounts,
                        viewModel = viewModel,
                        categoryLimits = categoryLimits,
                        currentMonthExpensesByCategory = currentMonthExpensesByCategory,
                        onAccountClick = { editingAccountForBalance = it },
                        onEditTransaction = { editingTransaction = it },
                        onDeleteTransaction = { transactionToDelete = it }
                    )
                    1 -> ReportsTabContent(
                        filteredTransactions = filteredTxs,
                        summary = summary,
                        selectedPeriod = selectedPeriod,
                        viewModel = viewModel,
                        onEditTransaction = { editingTransaction = it },
                        onDeleteTransaction = { transactionToDelete = it }
                    )
                    2 -> LinkingTabContent(
                        accounts = accounts,
                        viewModel = viewModel,
                        aiState = aiState
                    )
                    3 -> CategoryManagementTabContent(
                        categories = categoriesList,
                        viewModel = viewModel,
                        categoryLimits = categoryLimits,
                        currentMonthExpensesByCategory = currentMonthExpensesByCategory,
                        onCustomizeCategory = { topCategoryToCustomize = it }
                    )
                }
            }
        }

        // Deletion Confirmation Dialog
        if (transactionToDelete != null) {
            AlertDialog(
                onDismissRequest = { transactionToDelete = null },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = Color(0xFFCF6560)
                        )
                        Text(
                            text = "Xác nhận xóa?",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }
                },
                text = {
                    Text(
                        text = "Bạn có chắc chắn muốn xóa vĩnh viễn giao dịch này không? Thao tác này sẽ cập nhật lại số dư ví tương ứng và không thể hoàn tác.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            transactionToDelete?.let { tx ->
                                viewModel.deleteTransaction(tx)
                                Toast.makeText(context, "Đã xóa giao dịch thành công!", Toast.LENGTH_SHORT).show()
                            }
                            transactionToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCF6560)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Xóa", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { transactionToDelete = null }
                    ) {
                        Text("Hủy bỏ", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Add Manual Expense/Income Dialog
        if (showAddManualDialog) {
            AddManualTransactionDialog(
                accounts = accounts,
                categories = categoriesList,
                viewModel = viewModel,
                onAddCustomCategory = { viewModel.addCustomCategory(it) },
                onDismiss = { showAddManualDialog = false },
                onConfirm = { amount, type, category, note, bankName ->
                    viewModel.addTransaction(amount, type, category, note, bankName)
                    showAddManualDialog = false
                    Toast.makeText(context, "Đã ghi nhận giao dịch thành công!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Custom Gemini API Key Configuration Dialog
        if (showSettingsDialog) {
            var inputKey by remember { mutableStateOf(viewModel.getCustomApiKey()) }
            var isKeyVisible by remember { mutableStateOf(false) }
            
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = SleekDeepPurple
                        )
                        Text(
                            text = "Cấu hình API Gemini",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekDeepPurple
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Giải pháp xử lý triệt để lỗi nghẽn tần suất (HTTP 429):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "Do khóa API dùng chung bị giới hạn tần suất bởi Google, việc tự cấu hình khóa API Gemini cá nhân sẽ giúp bạn bóc tách ảnh hóa đơn và SMS siêu tốc và hoàn toàn không bị gián đoạn.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                        
                        Text(
                            text = "Mẹo: Hoàn toàn MIỄN PHÍ! Bạn có thể tạo khóa trong 10 giây từ Google AI Studio trên điện thoại Pixel 9 Pro XL của bạn.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = EmeraldPrimary,
                            lineHeight = 16.sp
                        )
                        
                        OutlinedTextField(
                            value = inputKey,
                            onValueChange = { inputKey = it },
                            label = { Text("Gemini API Key cá nhân của bạn") },
                            placeholder = { Text("AIzaSy...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_api_key_input"),
                            visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                    Icon(
                                        imageVector = if (isKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Hiển thị khóa"
                                    )
                                }
                            }
                        )
                        
                        Button(
                            onClick = {
                                try {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse("https://aistudio.google.com/app/apikey")
                                    )
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Không thể mở trình duyệt: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleekLightLavender,
                                contentColor = SleekDeepPurple
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lấy khóa API Miễn Phí", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Clear Custom Key Button if exists
                        if (viewModel.getCustomApiKey().isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    viewModel.saveCustomApiKey("")
                                    inputKey = ""
                                    Toast.makeText(context, "Đã xóa khóa API rác và khôi phục khóa mặc định!", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Dùng khóa gốc")
                            }
                        }
                        
                        Button(
                            onClick = {
                                viewModel.saveCustomApiKey(inputKey)
                                showSettingsDialog = false
                                if (inputKey.trim().isNotEmpty()) {
                                    Toast.makeText(context, "Cấu hình khóa cá nhân thành công!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Đã khôi phục khóa mặc định!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Lưu Khóa")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSettingsDialog = false }) {
                        Text("Đóng", color = TextSecondary)
                    }
                }
            )
        }

        if (showThemeDialog) {
            ThemeSelectionDialog(
                viewModel = viewModel,
                onDismiss = { showThemeDialog = false }
            )
        }

        // Edit Transaction Dialog
        if (editingTransaction != null) {
            EditTransactionDialog(
                transaction = editingTransaction!!,
                accounts = accounts,
                categories = categoriesList,
                viewModel = viewModel,
                onAddCustomCategory = { viewModel.addCustomCategory(it) },
                onDismiss = { editingTransaction = null },
                onConfirm = { updatedTx ->
                    viewModel.updateTransaction(editingTransaction!!, updatedTx)
                    editingTransaction = null
                    Toast.makeText(context, "Cập nhật giao dịch thành công!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Top-level Edit Category Customization Dialog
        if (topCategoryToCustomize != null) {
            EditCategoryDialog(
                category = topCategoryToCustomize!!,
                viewModel = viewModel,
                onDismiss = { topCategoryToCustomize = null }
            )
        }

        // Edit Offline Wallet Dialog
        if (editingAccountForBalance != null) {
            EditWalletDialog(
                account = editingAccountForBalance!!,
                onDismiss = { editingAccountForBalance = null },
                onConfirm = { name, accNo, balance, accType, creditLimit, creditSpent ->
                    viewModel.updateBankAccount(editingAccountForBalance!!.id, name, accNo, balance, accType, creditLimit, creditSpent)
                    editingAccountForBalance = null
                    Toast.makeText(context, "Cập nhật thông tin ví thành công!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

// ----------------------------------------------------
// TAB 1: OVERVIEW SCREEN
// ----------------------------------------------------
@Composable
fun OverviewTabContent(
    transactions: List<TransactionEntity>,
    accounts: List<BankAccount>,
    viewModel: FinanceViewModel,
    categoryLimits: Map<String, Double>,
    currentMonthExpensesByCategory: Map<String, Double>,
    onAccountClick: (BankAccount) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    BackHandler(enabled = isExpanded) {
        isExpanded = false
    }

    val totalOwned = remember(accounts) {
        accounts.filter { it.accountType != "TIN_DUNG" }.sumOf { it.balance }
    }
    val totalCreditLimit = remember(accounts) {
        accounts.filter { it.accountType == "TIN_DUNG" }.sumOf { it.creditLimit }
    }
    val totalCreditSpent = remember(accounts) {
        accounts.filter { it.accountType == "TIN_DUNG" }.sumOf { it.creditSpent }
    }
    val netWealth = totalOwned - totalCreditSpent

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Total Wealth Glassmorphic Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(SleekDeepPurple, SleekRoyalPurple.copy(alpha = 0.9f))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TỔNG TÀI SẢN THỰC TẾ (SỞ HỮU - NỢ)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Net wealth total
                            Text(
                                text = formatVnd(netWealth),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 28.sp
                                )
                            )
                        }
                        
                        // Safety Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "AES-256 mã hoá",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Divider
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.15f),
                        thickness = 1.dp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Column / Row for individual categories: SỞ HỮU vs CREDIT
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Left subcard: TÀI SẢN SỞ HỮU
                        Card(
                            modifier = Modifier
                                .weight(1.2f)
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.12f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Savings,
                                        contentDescription = null,
                                        tint = IncomeGreen, // Green for positive/owned assets
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "TÀI SẢN SỞ HỮU",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = formatVnd(totalOwned),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Right subcard: TÀI SẢN CREDIT
                        Card(
                            modifier = Modifier
                                .weight(1.5f)
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.12f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = SleekHighlightLavender, // lavender for credit
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "TÀI SẢN CREDIT",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Còn lại: " + formatVnd(totalCreditLimit - totalCreditSpent),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Nợ: ${formatVnd(totalCreditSpent)} / HM: ${formatVnd(totalCreditLimit)}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 9.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Exceeded Budgets Warning Banner
        item {
            val exceededBudgetsList = remember(currentMonthExpensesByCategory, categoryLimits) {
                categoryLimits.filter { (category, limit) ->
                    limit > 0.0 && (currentMonthExpensesByCategory[category] ?: 0.0) > limit
                }.toList()
            }
            
            if (exceededBudgetsList.isNotEmpty()) {
                var isBannerVisible by remember { mutableStateOf(true) }
                androidx.compose.animation.AnimatedVisibility(
                    visible = isBannerVisible,
                    enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF1F1) // Soft premium warning red-pink
                        ),
                        border = BorderStroke(1.2.dp, Color(0xFFF9D5D5))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFCF6560)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Cảnh báo vượt hạn mức",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = "Vượt Hạn Mức Chi Tiêu Tháng Này!",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFCF6560),
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { isBannerVisible = false },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Đóng cảnh báo",
                                        tint = Color.Gray.copy(alpha = 0.7f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                exceededBudgetsList.forEach { (cat, limit) ->
                                    val spent = currentMonthExpensesByCategory[cat] ?: 0.0
                                    val overAmount = spent - limit
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val icon = rememberCategoryIcon(category = cat)
                                            val color = rememberCategoryColor(category = cat)
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(color.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = color,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                            Text(
                                                text = cat,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = Color.Black
                                            )
                                        }
                                        
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "Vượt +${formatVnd(overAmount)}",
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFCF6560),
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "Đã chi ${formatVnd(spent)} / Hạn mức ${formatVnd(limit)}",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bank accounts Title & horizontal slide list
        item {
            Column {
                Text(
                    text = "Danh sách Ví & Tài khoản ngoại tuyến",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "Lưu dữ liệu an toàn. Nhấn sửa để cập nhật số dư mong muốn.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(accounts, key = { it.id }) { account ->
                        BankCard(account = account) {
                            onAccountClick(account)
                        }
                    }
                }
            }
        }

        // Ledger list Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Danh sách toàn bộ giao dịch" else "Lịch sử giao dịch gần đây",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                if (transactions.size > 10) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isExpanded = !isExpanded }
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isExpanded) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Thu gọn",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Thu gọn",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        } else {
                            Text(
                                text = "Xem tất cả (${transactions.size})",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }

        // Standard Empty state or Ledger List
        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Chưa có giao dịch ghi nhận",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Hãy thêm giao dịch thủ công hoặc bật lắng nghe AI bóc tách thông báo tự động ở mục Ví & Trợ lý AI.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        } else {
            val displayTransactions = if (isExpanded) transactions else transactions.take(10)
            items(displayTransactions, key = { it.id }) { tx ->
                TransactionRow(
                    transaction = tx,
                    onEdit = { onEditTransaction(tx) },
                    onDelete = { onDeleteTransaction(tx) }
                )
            }
        }
    }
}

// Bank account visual card Composable
@Composable
fun BankCard(
    account: BankAccount,
    onEditBalanceClick: () -> Unit
) {
    val isCredit = account.accountType == "TIN_DUNG"
    val (brandColor, brandMini) = getBankInfo(account.name)
    val bankColor = if (isCredit && account.name == "Khác") Color(0xFF5E35B1) else brandColor

    Box(
        modifier = Modifier
            .width(185.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bankColor)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = account.name,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 110.dp)
                        )
                        if (isCredit) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFFB300))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "CREDIT",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 7.sp
                                )
                            }
                        }
                    }
                    Text(
                        text = account.accountNo.ifBlank { "N/A" },
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }
                // Real high-res Web Logo Badge
                BankLogo(
                    bankName = account.name,
                    bankId = account.id,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            if (isCredit) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "HẠN MỨC",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = formatVnd(account.creditLimit),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "ĐÃ TIÊU",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = formatVnd(account.creditSpent),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "KHẢ DỤNG CÒN LẠI",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatVnd(account.balance),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            } else {
                Text(
                    text = "SỐ DƯ KHẢ DỤNG",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatVnd(account.balance),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Precise sync updated time indicator (seconds included)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.55f),
                    modifier = Modifier.size(10.dp)
                )
                Text(
                    text = "Cập nhật: ${formatExactSyncedTime(account.lastSynced)}",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { onEditBalanceClick() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Sửa ví",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Transaction Row item with easy Swipe-to-Delete and Edit capabilities
@Composable
fun TransactionRow(
    transaction: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_item_${transaction.id}")
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Indicator with customized colors
            val catColor = rememberCategoryColor(transaction.category)
            val catIcon = rememberCategoryIcon(transaction.category)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(catColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = catIcon,
                    contentDescription = null,
                    tint = catColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = transaction.note.ifBlank { "Không ghi chú" },
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    // Eye-catching category badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(catColor.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = transaction.category,
                            color = catColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Real web-based mini logotype
                    BankLogo(
                        bankName = transaction.bankName,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = transaction.bankName,
                        color = TextPrimary.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(TextSecondary.copy(alpha = 0.5f))
                    )
                    Text(
                        text = formatDateTime(transaction.timestamp),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                horizontalAlignment = Alignment.End
            ) {
                val flowColor = if (transaction.type == "INCOME") IncomeGreen else ExpenseRed
                val sign = if (transaction.type == "INCOME") "+" else "-"
                Text(
                    text = "$sign ${formatVnd(transaction.amount)}",
                    color = flowColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Sửa",
                        tint = TextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onEdit() }
                    )
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa",
                        tint = TextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onDelete() }
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 2: REPORTS VIEW SCREEN
// ----------------------------------------------------
@Composable
fun FilterDropdownSelector(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$label: $selectedValue",
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontSize = 13.sp, color = TextPrimary) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ReportsTabContent(
    filteredTransactions: List<TransactionEntity>,
    summary: com.example.ui.viewmodel.PeriodSummary,
    selectedPeriod: ReportPeriod,
    viewModel: FinanceViewModel,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    var categoryToCustomize by remember { mutableStateOf<String?>(null) }
    var isListExpanded by remember { mutableStateOf(false) }
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedAccount by viewModel.selectedAccount.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedType.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val customStartDate by viewModel.customStartDate.collectAsStateWithLifecycle()
    val customEndDate by viewModel.customEndDate.collectAsStateWithLifecycle()
    val categoryParentMap by viewModel.categoryParentMap.collectAsStateWithLifecycle()
    
    var drillDownParentCategory by remember { mutableStateOf<String?>(null) }
    
    val parentShares = remember(summary.categoryBreakdown, categoryParentMap) {
        val groups = mutableMapOf<String, Double>()
        summary.categoryBreakdown.forEach { share ->
            val parent = categoryParentMap[share.category] ?: share.category
            groups[parent] = (groups[parent] ?: 0.0) + share.amount
        }
        groups.toList().sortedByDescending { it.second }
    }
    
    val sdf = remember { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()) }
    fun formatDate(millis: Long): String = sdf.format(java.util.Date(millis))

    if (drillDownParentCategory != null) {
        CategoryDrillDownScreen(
            parentCategory = drillDownParentCategory!!,
            filteredTransactions = filteredTransactions,
            viewModel = viewModel,
            onBack = { drillDownParentCategory = null },
            onEditTransaction = onEditTransaction,
            onDeleteTransaction = onDeleteTransaction
        )
    } else {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Separate Income and Expense tabs (tách rõ 2 mục thu và chi)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                // "Chi tiêu" Tab
                val isExpenseSelected = selectedType == "Chi tiêu"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isExpenseSelected) ExpenseRed.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            width = if (isExpenseSelected) 1.5.dp else 0.dp,
                            color = if (isExpenseSelected) ExpenseRed else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { viewModel.setSelectedType("Chi tiêu") }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ExpenseRed)
                        )
                        Text(
                            text = "KHOẢN CHI TIÊU",
                            color = if (isExpenseSelected) TextPrimary else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // "Thu nhập" Tab
                val isIncomeSelected = selectedType == "Thu nhập"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isIncomeSelected) IncomeGreen.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            width = if (isIncomeSelected) 1.5.dp else 0.dp,
                            color = if (isIncomeSelected) IncomeGreen else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { viewModel.setSelectedType("Thu nhập") }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(IncomeGreen)
                        )
                        Text(
                            text = "KHOẢN THU NHẬP",
                            color = if (isIncomeSelected) TextPrimary else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // 2. Main Chart Card with Period selectors on the Left
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedType == "Chi tiêu") "Cơ cấu Chi tiêu Nhóm lớn" else "Cơ cấu Thu nhập Nhóm lớn",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = if (selectedType == "Chi tiêu") formatVnd(summary.totalExpense) else formatVnd(summary.totalIncome),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = if (selectedType == "Chi tiêu") ExpenseRed else IncomeGreen
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    val context = LocalContext.current
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Left side: Period buttons stack
                        Column(
                            modifier = Modifier.width(80.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "KỲ BÁO CÁO",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            
                            // "Ngày" button (activates CUSTOM range)
                            val isDaySelected = selectedPeriod == ReportPeriod.CUSTOM || selectedPeriod == ReportPeriod.DAY
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDaySelected) EmeraldPrimary else Color.White.copy(alpha = 0.04f))
                                    .clickable {
                                        viewModel.setPeriod(ReportPeriod.CUSTOM)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Ngày",
                                    color = if (isDaySelected) Color.Black else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // "Tháng" button
                            val isMonthSelected = selectedPeriod == ReportPeriod.MONTH
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isMonthSelected) EmeraldPrimary else Color.White.copy(alpha = 0.04f))
                                    .clickable {
                                        viewModel.setPeriod(ReportPeriod.MONTH)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tháng",
                                    color = if (isMonthSelected) Color.Black else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // "Năm" button
                            val isYearSelected = selectedPeriod == ReportPeriod.YEAR
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isYearSelected) EmeraldPrimary else Color.White.copy(alpha = 0.04f))
                                    .clickable {
                                        viewModel.setPeriod(ReportPeriod.YEAR)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Năm",
                                    color = if (isYearSelected) Color.Black else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Right side: Solid Pie Chart for parent categories
                        
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            SolidPieChart(
                                shares = parentShares,
                                selectedCategory = null,
                                onCategorySelect = { parentName ->
                                    drillDownParentCategory = parentName
                                },
                                modifier = Modifier.size(190.dp)
                            )
                        }
                    }
                    
                    // From/To Date Pickers below chart if custom/day is active
                    if (selectedPeriod == ReportPeriod.CUSTOM) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val customStartText = customStartDate?.let { formatDate(it) } ?: "Từ ngày"
                            val customEndText = customEndDate?.let { formatDate(it) } ?: "Đến ngày"
                            
                            // Start date trigger
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable {
                                        val cal = java.util.Calendar.getInstance()
                                        if (customStartDate != null) cal.timeInMillis = customStartDate!!
                                        android.app.DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                val sCal = java.util.Calendar.getInstance()
                                                sCal.set(java.util.Calendar.YEAR, year)
                                                sCal.set(java.util.Calendar.MONTH, month)
                                                sCal.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
                                                sCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                                                sCal.set(java.util.Calendar.MINUTE, 0)
                                                sCal.set(java.util.Calendar.SECOND, 0)
                                                viewModel.setCustomDateRange(sCal.timeInMillis, customEndDate)
                                            },
                                            cal.get(java.util.Calendar.YEAR),
                                            cal.get(java.util.Calendar.MONTH),
                                            cal.get(java.util.Calendar.DAY_OF_MONTH)
                                        ).show()
                                    }
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                                    Text(text = customStartText, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Text(text = "đến", fontSize = 11.sp, color = TextSecondary)
                            
                            // End date trigger
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable {
                                        val cal = java.util.Calendar.getInstance()
                                        if (customEndDate != null) cal.timeInMillis = customEndDate!!
                                        android.app.DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                val eCal = java.util.Calendar.getInstance()
                                                eCal.set(java.util.Calendar.YEAR, year)
                                                eCal.set(java.util.Calendar.MONTH, month)
                                                eCal.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
                                                eCal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                                                eCal.set(java.util.Calendar.MINUTE, 59)
                                                eCal.set(java.util.Calendar.SECOND, 59)
                                                viewModel.setCustomDateRange(customStartDate ?: System.currentTimeMillis(), eCal.timeInMillis)
                                            },
                                            cal.get(java.util.Calendar.YEAR),
                                            cal.get(java.util.Calendar.MONTH),
                                            cal.get(java.util.Calendar.DAY_OF_MONTH)
                                        ).show()
                                    }
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                                    Text(text = customEndText, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Account/Wallet & Search filters card for report context
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Tìm ghi chú, ví, lĩnh vực...", fontSize = 12.sp, color = TextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Xóa", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = TextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Account filter
                        val accountOptions = listOf("Tất cả") + allAccounts.map { it.name }
                        FilterDropdownSelector(
                            label = "Bộ lọc ví",
                            selectedValue = selectedAccount,
                            options = accountOptions,
                            onSelect = { viewModel.setSelectedAccount(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    if (selectedAccount != "Tất cả" || searchQuery.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Xóa bộ lọc nâng cao",
                            color = EmeraldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.End)
                                .clickable {
                                    viewModel.setSelectedAccount("Tất cả")
                                    viewModel.setSearchQuery("")
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }

        // 4. Net balance status overview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TỔNG QUAN KỲ NÀY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Thu nhập (+)", color = TextSecondary, fontSize = 12.sp)
                            Text(text = formatVnd(summary.totalIncome), color = IncomeGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Chi tiêu (-)", color = TextSecondary, fontSize = 12.sp)
                            Text(text = formatVnd(summary.totalExpense), color = ExpenseRed, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Số dư Ròng", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                text = formatVnd(summary.netBalance),
                                color = if (summary.netBalance >= 0) AccentBlue else ExpenseRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // 5. Parent Categories (Mục lớn) Drill-Down List
        item {
            Text(
                text = "DANH SÁCH NHÓM MỤC LỚN (Bấm để xem mục nhỏ)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        if (parentShares.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có phát sinh trong kỳ",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            val overallTotal = parentShares.sumOf { it.second }
            items(parentShares) { (parent, amount) ->
                val percentage = if (overallTotal > 0) (amount / overallTotal * 100f).toFloat() else 0f
                val catColor = rememberCategoryColor(parent)
                val catIcon = rememberCategoryIcon(parent)
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { drillDownParentCategory = parent },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(catColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = catIcon,
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            Column {
                                Text(
                                    text = parent,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val subCount = categoryParentMap.filter { it.value == parent }.size
                                Text(
                                    text = if (subCount > 0) "$subCount mục nhỏ" else "Không có mục nhỏ",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatVnd(amount),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", percentage)}%",
                                    fontSize = 10.sp,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Xem",
                                tint = TextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. Detailed transaction list matching filters
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Giao dịch phát sinh (${filteredTransactions.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                if (filteredTransactions.size > 10) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isListExpanded = !isListExpanded }
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isListExpanded) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Thu gọn", tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                            Text(text = "Thu gọn", style = MaterialTheme.typography.bodySmall.copy(color = EmeraldPrimary, fontWeight = FontWeight.SemiBold))
                        } else {
                            Text(text = "Xem tất cả (${filteredTransactions.size})", style = MaterialTheme.typography.bodySmall.copy(color = EmeraldPrimary, fontWeight = FontWeight.SemiBold))
                        }
                    }
                }
            }
        }

        if (filteredTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không tìm thấy giao dịch nào phù hợp.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            val displayTransactions = if (isListExpanded) filteredTransactions else filteredTransactions.take(10)
            items(displayTransactions) { tx ->
                TransactionRow(
                    transaction = tx,
                    onEdit = { onEditTransaction(tx) },
                    onDelete = { onDeleteTransaction(tx) }
                )
            }
        }
    }
    } // Closes the "else" block of drillDownParentCategory

    if (categoryToCustomize != null) {
        EditCategoryDialog(
            category = categoryToCustomize!!,
            viewModel = viewModel,
            onDismiss = { categoryToCustomize = null }
        )
    }
}

@Composable
fun PieChart(
    shares: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    SolidPieChart(
        shares = shares,
        selectedCategory = null,
        onCategorySelect = {},
        modifier = modifier
    )
}

@Composable
fun SolidPieChart(
    shares: List<Pair<String, Double>>,
    selectedCategory: String?,
    onCategorySelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val total = remember(shares) { shares.sumOf { it.second } }
    if (total <= 0) {
        Box(
            modifier = modifier.fillMaxWidth().height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Không có dữ liệu hiển thị", color = TextSecondary, fontSize = 12.sp)
        }
        return
    }

    val colors = shares.map { rememberCategoryColor(it.first) }
    val sweepAngles = remember(shares, total) {
        shares.map { (it.second / total * 360f).toFloat() }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Centered solid pie chart box
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(shares, sweepAngles) {
                        detectTapGestures { offset ->
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val dx = offset.x - cx
                            val dy = offset.y - cy
                            val distance = Math.hypot(dx.toDouble(), dy.toDouble())
                            val maxRadius = minOf(size.width, size.height) / 2f
                            
                            if (distance <= maxRadius) {
                                var angle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble()))
                                if (angle < 0) angle += 360f
                                
                                // startAngle is -90f
                                var targetAngle = angle - (-90f)
                                if (targetAngle < 0) targetAngle += 360f
                                targetAngle %= 360f
                                
                                var currentAngleSum = 0f
                                var clickedIndex = -1
                                for (i in sweepAngles.indices) {
                                    val sweep = sweepAngles[i]
                                    if (targetAngle >= currentAngleSum && targetAngle < currentAngleSum + sweep) {
                                        clickedIndex = i
                                        break
                                    }
                                    currentAngleSum += sweep
                                }
                                
                                if (clickedIndex in shares.indices) {
                                    onCategorySelect(shares[clickedIndex].first)
                                }
                            }
                        }
                    }
            ) {
                var startAngle = -90f
                val radius = size.minDimension / 2f

                sweepAngles.forEachIndexed { index, sweepAngle ->
                    val category = shares[index].first
                    val isSelected = category == selectedCategory
                    
                    // If selected, offset the drawing bounds outward in the direction of the middle angle
                    val middleAngleRad = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())
                    val offsetDist = if (isSelected) 12.dp.toPx() else 0f
                    val dx = (offsetDist * Math.cos(middleAngleRad)).toFloat()
                    val dy = (offsetDist * Math.sin(middleAngleRad)).toFloat()

                    // Draw the solid wedge slice
                    drawArc(
                        color = colors[index],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        style = androidx.compose.ui.graphics.drawscope.Fill,
                        topLeft = androidx.compose.ui.geometry.Offset(dx, dy)
                    )

                    // Draw a subtle border around the slice for extreme premium separation
                    drawArc(
                        color = Color.Black.copy(alpha = 0.15f),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
                        topLeft = androidx.compose.ui.geometry.Offset(dx, dy)
                    )

                    // Display percentage inside segment if segment is large enough (> 4%)
                    val percentage = (sweepAngle / 360f * 100)
                    if (percentage >= 4f) {
                        // Place label at radius * 0.62f
                        val textRadius = radius * 0.62f
                        val textX = (size.width / 2f) + dx + (textRadius * Math.cos(middleAngleRad)).toFloat()
                        val textY = (size.height / 2f) + dy + (textRadius * Math.sin(middleAngleRad)).toFloat()

                        val textPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = if (isSelected) 30f else 25f
                            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                            textAlign = android.graphics.Paint.Align.CENTER
                        }

                        val fontMetrics = textPaint.fontMetrics
                        val drawY = textY - (fontMetrics.descent + fontMetrics.ascent) / 2f

                        textPaint.setShadowLayer(4f, 0f, 2f, android.graphics.Color.BLACK)

                        drawContext.canvas.nativeCanvas.drawText(
                            "${String.format(Locale.US, "%.0f", percentage)}%",
                            textX,
                            drawY,
                            textPaint
                        )
                    }

                    startAngle += sweepAngle
                }
            }
        }

        // Legends below structured in a beautiful responsive grid of legend chips
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chunks = shares.chunked(2)
            chunks.forEach { rowShares ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowShares.forEach { (category, amount) ->
                        val index = shares.indexOfFirst { it.first == category }
                        val percentage = (amount / total * 100).toFloat()
                        val color = colors.getOrElse(index) { Color.Gray }
                        val isSelected = category == selectedCategory
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) color.copy(alpha = 0.15f) else color.copy(alpha = 0.04f)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) color else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onCategorySelect(category) }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Text(
                                text = "$category (${String.format(Locale.US, "%.1f", percentage)}%)",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (rowShares.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryDrillDownScreen(
    parentCategory: String,
    filteredTransactions: List<TransactionEntity>,
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    val categoryParentMap by viewModel.categoryParentMap.collectAsStateWithLifecycle()
    val sdf = remember { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()) }
    fun formatDate(millis: Long): String = sdf.format(java.util.Date(millis))
    
    // 1. Get all transactions under this parent category matching the filters
    val parentTxs = remember(filteredTransactions, parentCategory, categoryParentMap) {
        filteredTransactions.filter { tx ->
            tx.category == parentCategory || categoryParentMap[tx.category] == parentCategory
        }
    }
    
    // 2. Group these transactions into subcategories
    val subcategoryGroups = remember(parentTxs, parentCategory) {
        val groups = mutableMapOf<String, MutableList<TransactionEntity>>()
        parentTxs.forEach { tx ->
            if (tx.category == parentCategory) {
                groups.getOrPut("Khác") { mutableListOf() }.add(tx)
            } else {
                groups.getOrPut(tx.category) { mutableListOf() }.add(tx)
            }
        }
        groups
    }
    
    // 3. Compute totals and percentages for each subcategory
    val parentTotalAmount = remember(parentTxs) { parentTxs.sumOf { it.amount } }
    val subcategoryShares = remember(subcategoryGroups) {
        subcategoryGroups.map { (subName, txs) ->
            val totalAmount = txs.sumOf { it.amount }
            subName to totalAmount
        }.sortedByDescending { it.second }
    }
    
    // 4. Track which subcategory is expanded to show its transactions
    var expandedSubcategory by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top navigation bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Chi tiết Lĩnh vực",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "Mục lớn: $parentCategory",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Parent summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = rememberCategoryColor(parentCategory).copy(alpha = 0.08f)
            ),
            border = BorderStroke(1.dp, rememberCategoryColor(parentCategory).copy(alpha = 0.25f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val catColor = rememberCategoryColor(parentCategory)
                val catIcon = rememberCategoryIcon(parentCategory)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(catColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = catIcon,
                        contentDescription = null,
                        tint = catColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = parentCategory,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tổng giao dịch: ${formatVnd(parentTotalAmount)}",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // List of Subcategories (mục nhỏ)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "DANH SÁCH MỤC NHỎ CHI TIẾT (Bấm để xem giao dịch)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            
            if (subcategoryShares.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Chưa có giao dịch phát sinh nào cho nhóm này",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            } else {
                items(subcategoryShares) { (subName, subAmount) ->
                    val isExpanded = expandedSubcategory == subName
                    val subPercentage = if (parentTotalAmount > 0) (subAmount / parentTotalAmount * 100f).toFloat() else 0f
                    val subColor = if (subName == "Khác") rememberCategoryColor(parentCategory) else rememberCategoryColor(subName)
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedSubcategory = if (isExpanded) null else subName
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isExpanded) {
                                subColor.copy(alpha = 0.05f)
                            } else {
                                Color.White.copy(alpha = 0.02f)
                            }
                        ),
                        border = if (isExpanded) {
                            BorderStroke(1.2.dp, subColor.copy(alpha = 0.4f))
                        } else null
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(subColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (subName == "Khác") Icons.Default.MoreHoriz else rememberCategoryIcon(subName),
                                            contentDescription = null,
                                            tint = subColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    
                                    Column {
                                        Text(
                                            text = subName,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${subcategoryGroups[subName]?.size ?: 0} giao dịch",
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = formatVnd(subAmount),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", subPercentage)}% mục lớn",
                                            fontSize = 10.sp,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ChevronRight,
                                        contentDescription = "Chi tiết",
                                        tint = TextSecondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            LinearProgressIndicator(
                                progress = { subPercentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(2.5.dp)),
                                color = subColor,
                                trackColor = Color.White.copy(alpha = 0.08f)
                            )
                            
                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                val txsList = subcategoryGroups[subName] ?: emptyList()
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    txsList.sortedByDescending { it.timestamp }.forEach { tx ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = if (tx.note.isNotEmpty()) tx.note else "Không có ghi chú",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = TextPrimary,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = formatDate(tx.timestamp),
                                                            fontSize = 9.sp,
                                                            color = TextSecondary
                                                        )
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(Color.White.copy(alpha = 0.08f))
                                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                                        ) {
                                                            Text(
                                                                text = tx.bankName,
                                                                fontSize = 8.sp,
                                                                color = TextSecondary,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                                
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = formatVnd(tx.amount),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = if (tx.type == "INCOME") IncomeGreen else ExpenseRed
                                                    )
                                                    
                                                    IconButton(
                                                        onClick = { onEditTransaction(tx) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = "Sửa",
                                                            tint = EmeraldPrimary,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                    
                                                    IconButton(
                                                        onClick = { onDeleteTransaction(tx) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Xóa",
                                                            tint = ExpenseRed,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
fun CategoryDetailedAnalysisScreen(
    filteredTransactions: List<TransactionEntity>,
    summary: com.example.ui.viewmodel.PeriodSummary,
    selectedPeriod: ReportPeriod,
    viewModel: FinanceViewModel,
    onClose: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    var selectedType by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "INCOME"
    val categoryParentMap by viewModel.categoryParentMap.collectAsStateWithLifecycle()
    
    // Choose the data based on selectedType
    val currentBreakdown = if (selectedType == "EXPENSE") {
        summary.expenseBreakdown
    } else {
        summary.incomeBreakdown
    }
    
    val totalSum = if (selectedType == "EXPENSE") {
        summary.totalExpense
    } else {
        summary.totalIncome
    }
    
    // Group by parent category
    val parentGroups = remember(currentBreakdown, categoryParentMap) {
        val groups = mutableMapOf<String, MutableList<com.example.ui.viewmodel.CategoryShare>>()
        currentBreakdown.forEach { share ->
            val parent = categoryParentMap[share.category]
            if (parent != null) {
                groups.getOrPut(parent) { mutableListOf() }.add(share)
            } else {
                groups.getOrPut(share.category) { mutableListOf() }.add(share)
            }
        }
        groups
    }
    
    // Map parent to total group amount
    val parentShares = remember(parentGroups) {
        parentGroups.map { (parent, children) ->
            val totalAmount = children.sumOf { it.amount }
            parent to totalAmount
        }.sortedByDescending { it.second }
    }
    
    // State for selected category in the list & pie chart
    var activePieSelectedCategory by remember { mutableStateOf<String?>(null) }
    
    // Auto select first category if none selected
    LaunchedEffect(parentShares) {
        if (activePieSelectedCategory == null && parentShares.isNotEmpty()) {
            activePieSelectedCategory = parentShares.first().first
        } else if (parentShares.isEmpty()) {
            activePieSelectedCategory = null
        }
    }
    
    // Keep track of which parent categories are expanded to show subcategories/transactions
    var expandedParentsInPage by remember { mutableStateOf(setOf<String>()) }
    // Keep track of which subcategories (or parent categories without kids) are showing transaction lists
    var expandedCategoryTransactions by remember { mutableStateOf(setOf<String>()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Elegant top navigation bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Phân tích & Thống kê nhóm",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = "Kỳ báo cáo: " + when (selectedPeriod) {
                            ReportPeriod.DAY -> "Hôm nay"
                            ReportPeriod.WEEK -> "Tuần này"
                            ReportPeriod.MONTH -> "Tháng này"
                            ReportPeriod.YEAR -> "Năm nay"
                            ReportPeriod.CUSTOM -> "Tùy chọn"
                        },
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Beautiful separated switcher for "Thu nhập" and "Chi tiêu"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                // Chi tiêu Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedType == "EXPENSE") ExpenseRed.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            width = if (selectedType == "EXPENSE") 1.5.dp else 0.dp,
                            color = if (selectedType == "EXPENSE") ExpenseRed else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            selectedType = "EXPENSE"
                            activePieSelectedCategory = null
                            expandedParentsInPage = emptySet()
                            expandedCategoryTransactions = emptySet()
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ExpenseRed)
                        )
                        Text(
                            text = "KHOẢN CHI TIÊU",
                            color = if (selectedType == "EXPENSE") TextPrimary else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Thu nhập Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedType == "INCOME") IncomeGreen.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            width = if (selectedType == "INCOME") 1.5.dp else 0.dp,
                            color = if (selectedType == "INCOME") IncomeGreen else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            selectedType = "INCOME"
                            activePieSelectedCategory = null
                            expandedParentsInPage = emptySet()
                            expandedCategoryTransactions = emptySet()
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(IncomeGreen)
                        )
                        Text(
                            text = "KHOẢN THU NHẬP",
                            color = if (selectedType == "INCOME") TextPrimary else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Huge Solid Pie Chart Section (takes up ~ 1/3 height)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (selectedType == "EXPENSE") "Phân bổ Chi tiêu" else "Phân bổ Thu nhập",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Tổng: " + formatVnd(totalSum),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (selectedType == "EXPENSE") ExpenseRed else IncomeGreen
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            SolidPieChart(
                                shares = parentShares,
                                selectedCategory = activePieSelectedCategory,
                                onCategorySelect = { cat ->
                                    activePieSelectedCategory = cat
                                    // Expand this parent so the user immediately sees details below
                                    expandedParentsInPage = expandedParentsInPage + cat
                                }
                            )
                        }
                    }
                }

                // Header for Drill-Down
                item {
                    Text(
                        text = "DANH SÁCH LĨNH VỰC LỚN & NHỎ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                }

                if (parentShares.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (selectedType == "EXPENSE") "Chưa có chi tiêu phát sinh" else "Chưa có thu nhập phát sinh",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }

                // Interactive list of parent categories with chevrons and drill-downs
                parentShares.forEach { (parent, totalAmount) ->
                    val children = parentGroups[parent] ?: emptyList()
                    val isExpanded = expandedParentsInPage.contains(parent)
                    val hasChildren = children.size > 1 || (children.size == 1 && children[0].category != parent)
                    val percentage = if (totalSum > 0) (totalAmount / totalSum * 100f).toFloat() else 0f
                    val isSelectedInChart = parent == activePieSelectedCategory

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    activePieSelectedCategory = parent
                                    // Toggle expanded state
                                    expandedParentsInPage = if (isExpanded) {
                                        expandedParentsInPage - parent
                                    } else {
                                        expandedParentsInPage + parent
                                    }
                                    
                                    // If it has NO subcategories, toggle its transaction history expand state
                                    if (!hasChildren) {
                                        expandedCategoryTransactions = if (expandedCategoryTransactions.contains(parent)) {
                                            expandedCategoryTransactions - parent
                                        } else {
                                            expandedCategoryTransactions + parent
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelectedInChart) {
                                    rememberCategoryColor(parent).copy(alpha = 0.08f)
                                } else {
                                    Color.White.copy(alpha = 0.03f)
                                }
                            ),
                            border = BorderStroke(
                                width = if (isSelectedInChart) 1.5.dp else 1.dp,
                                color = if (isSelectedInChart) rememberCategoryColor(parent) else Color.White.copy(alpha = 0.06f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Top row of Parent Item
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        val catColor = rememberCategoryColor(parent)
                                        val catIcon = rememberCategoryIcon(parent)
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(catColor.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = catIcon,
                                                contentDescription = null,
                                                tint = catColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = parent,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = if (hasChildren) "${children.size} nhóm con" else "Lĩnh vực chính",
                                                fontSize = 10.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = formatVnd(totalAmount),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                color = if (selectedType == "EXPENSE") ExpenseRed else IncomeGreen
                                            )
                                            Text(
                                                text = "${String.format(Locale.US, "%.1f", percentage)}%",
                                                fontSize = 11.sp,
                                                color = rememberCategoryColor(parent),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        
                                        // Trailing Navigation Icon Arrow as explicitly requested
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ChevronRight,
                                            contentDescription = "Chi tiết",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                LinearProgressIndicator(
                                    progress = { percentage / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = rememberCategoryColor(parent),
                                    trackColor = Color.White.copy(alpha = 0.08f)
                                )

                                // Nested children list if expanded
                                if (isExpanded) {
                                    if (hasChildren) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.padding(start = 10.dp)
                                        ) {
                                            children.sortedByDescending { it.amount }.forEach { child ->
                                                val childPercentageOfParent = if (totalAmount > 0) (child.amount / totalAmount * 100).toFloat() else 0f
                                                val isChildTxExpanded = expandedCategoryTransactions.contains(child.category)
                                                
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color.White.copy(alpha = 0.02f))
                                                        .border(
                                                            width = if (isChildTxExpanded) 1.dp else 0.dp,
                                                            color = if (isChildTxExpanded) rememberCategoryColor(child.category).copy(alpha = 0.3f) else Color.Transparent,
                                                            shape = RoundedCornerShape(10.dp)
                                                        )
                                                        .clickable {
                                                            // Toggle transaction history list for this child category
                                                            expandedCategoryTransactions = if (isChildTxExpanded) {
                                                                expandedCategoryTransactions - child.category
                                                            } else {
                                                                expandedCategoryTransactions + child.category
                                                            }
                                                        }
                                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(6.dp)
                                                                    .clip(CircleShape)
                                                                    .background(rememberCategoryColor(child.category))
                                                            )
                                                            Text(
                                                                text = child.category,
                                                                color = TextPrimary,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }

                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = formatVnd(child.amount),
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = TextPrimary
                                                            )
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(4.dp))
                                                                    .background(rememberCategoryColor(child.category).copy(alpha = 0.12f))
                                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = "${String.format(Locale.US, "%.0f", childPercentageOfParent)}%",
                                                                    fontSize = 9.sp,
                                                                    color = rememberCategoryColor(child.category),
                                                                    fontWeight = FontWeight.ExtraBold
                                                                )
                                                            }
                                                            Icon(
                                                                imageVector = if (isChildTxExpanded) Icons.Default.ExpandLess else Icons.Default.ChevronRight,
                                                                contentDescription = "Giao dịch",
                                                                tint = TextSecondary,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                    }
                                                    
                                                    // Show transactions for this child category if expanded
                                                    if (isChildTxExpanded) {
                                                        CategoryTransactionsHistoryList(
                                                            categoryName = child.category,
                                                            selectedType = selectedType,
                                                            filteredTransactions = filteredTransactions,
                                                            categoryParentMap = categoryParentMap,
                                                            onEditTransaction = onEditTransaction,
                                                            onDeleteTransaction = onDeleteTransaction
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // Parent has no children. Show its transactions list here
                                        val isParentTxExpanded = expandedCategoryTransactions.contains(parent)
                                        if (isParentTxExpanded) {
                                            CategoryTransactionsHistoryList(
                                                categoryName = parent,
                                                selectedType = selectedType,
                                                filteredTransactions = filteredTransactions,
                                                categoryParentMap = categoryParentMap,
                                                onEditTransaction = onEditTransaction,
                                                onDeleteTransaction = onDeleteTransaction
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Add a bottom spacing item
                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
fun CategoryTransactionsHistoryList(
    categoryName: String,
    selectedType: String,
    filteredTransactions: List<TransactionEntity>,
    categoryParentMap: Map<String, String>,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    val txs = remember(filteredTransactions, categoryName, selectedType, categoryParentMap) {
        filteredTransactions.filter { tx ->
            val isIncome = tx.type == "INCOME"
            val typeMatches = (selectedType == "EXPENSE" && !isIncome) || (selectedType == "INCOME" && isIncome)
            val categoryMatches = tx.category == categoryName
            typeMatches && categoryMatches
        }.sortedByDescending { it.timestamp }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Lịch sử giao dịch (${txs.size}):",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        
        if (txs.isEmpty()) {
            Text(
                text = "Chưa có giao dịch nào.",
                fontSize = 11.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )
        } else {
            txs.forEach { tx ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (tx.note.isNotEmpty()) tx.note else "Không có ghi chú",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = formatDateTime(tx.timestamp),
                                        fontSize = 9.sp,
                                        color = TextSecondary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = tx.bankName,
                                            fontSize = 8.sp,
                                            color = TextSecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = formatVnd(tx.amount),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (tx.type == "INCOME") IncomeGreen else ExpenseRed
                                )
                                
                                // Quick action buttons
                                IconButton(
                                    onClick = { onEditTransaction(tx) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Sửa",
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                
                                IconButton(
                                    onClick = { onDeleteTransaction(tx) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Xóa",
                                        tint = ExpenseRed,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 3: LINKING & AUTOMATIC AI SCREEN
// ----------------------------------------------------
@Composable
fun LinkingTabContent(
    accounts: List<BankAccount>,
    viewModel: FinanceViewModel,
    aiState: AiParseState
) {
    val context = LocalContext.current
    var inputSmsText by remember { mutableStateOf("") }
    var showLinkNewBankDialog by remember { mutableStateOf(false) }
    var editingAccountInLinkingTab by remember { mutableStateOf<BankAccount?>(null) }

    // Google Sign-In & Clouds states
    val isGoogleLoggedIn by viewModel.isGoogleLoggedIn.collectAsStateWithLifecycle()
    val googleEmail by viewModel.googleEmail.collectAsStateWithLifecycle()
    val googleName by viewModel.googleName.collectAsStateWithLifecycle()
    val lastBackupTime by viewModel.lastBackupTime.collectAsStateWithLifecycle()
    val isBackupInProgress by viewModel.isBackupInProgress.collectAsStateWithLifecycle()
    var showGmailLoginDialog by remember { mutableStateOf(false) }

    var subTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Segments Control (iOS Style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (subTab == 0) SleekDeepPurple else Color.Transparent)
                    .clickable { subTab = 0 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = if (subTab == 0) Color.White else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "Ví & Liên kết",
                        color = if (subTab == 0) Color.White else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (subTab == 1) SleekDeepPurple else Color.Transparent)
                    .clickable { subTab = 1 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContactPage,
                        contentDescription = null,
                        tint = if (subTab == 1) Color.White else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "Sổ vay nợ cá nhân",
                        color = if (subTab == 1) Color.White else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (subTab == 0) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
        // GOOGLE ACCOUNT & GMAIL BACKUP CONTROL CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                // Simple elegant mock Google G logo
                                Text(
                                    "G",
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF4285F4),
                                    fontSize = 18.sp
                                )
                            }
                            Column {
                                Text(
                                    text = "Đồng bộ đám mây",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Sao lưu lịch sử ví an toàn",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        if (isGoogleLoggedIn) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EmeraldPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "ĐÃ LIÊN KẾT",
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 8.sp
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Gray.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "NGOẠI TUYẾN",
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }

                    if (!isGoogleLoggedIn) {
                        Text(
                            text = "Đăng nhập tài khoản Gmail để sao lưu toàn bộ dữ liệu giao dịch và số dư các ví, phục hồi tức thì khi đổi máy hay cài lại app.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Button(
                            onClick = { showGmailLoginDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.5.dp, Color.LightGray)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEA4335)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mail,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                                Text(
                                    "Đăng nhập bằng tài khoản Gmail",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        // User Profile summary
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1565C0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = googleName.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                }
                                Column {
                                    Text(
                                        text = googleName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = googleEmail,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Divider(color = TextSecondary.copy(alpha = 0.15f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Đồng bộ lần cuối:",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = if (lastBackupTime > 0L) formatExactSyncedTime(lastBackupTime) else "Chưa từng sao lưu",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (lastBackupTime > 0L) EmeraldPrimary else Color.Gray
                                )
                            }
                        }

                        if (isBackupInProgress) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = EmeraldPrimary,
                                    trackColor = Color.Gray.copy(alpha = 0.2f)
                                )
                                Text(
                                    "Đang trao đổi dữ liệu với Google Drive...",
                                    fontSize = 9.sp,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.backupToGoogleDrive { success, msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldPrimary.copy(alpha = 0.2f),
                                        contentColor = EmeraldPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sao Lưu", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.restoreFromGoogleDrive { success, msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1E88E5).copy(alpha = 0.2f),
                                        contentColor = Color(0xFF90CAF9)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Khôi Phục", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                TextButton(
                                    onClick = { viewModel.signOutGoogle() }
                                ) {
                                    Text(
                                        "Đăng xuất",
                                        color = Color.Red.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Glowing AI Header Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                EmeraldPrimary.copy(alpha = 0.25f),
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Trợ lý AI Đọc Biến Động",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dán thông báo SMS / App ngân hàng tại đây. Trí tuệ nhân tạo Gemini 3.5 sẽ tự động bóc tách số tiền, danh mục chính xác và nạp số dư.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // SMS input Area
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Nội dung tin nhắn biến động",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = inputSmsText,
                    onValueChange = { inputSmsText = it },
                    placeholder = {
                        Text(
                            "Ví dụ: GD: -25,000VN gia han momo vao 08/06/2026. Số dư...",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.1f),
                        focusedIndicatorColor = EmeraldPrimary,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Suggestion Quick templates in Vietnam
                Text(
                    text = "Mẫu tin nhắn thử nghiệm nhanh:",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .clickable {
                                inputSmsText =
                                    "Vietcombank GD: -120,000 VND luc 08-06-2026 12:45. ND: an trua bun cha hang quat. So du: 15,230,000VND."
                            }
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Vietcombank (-120k)",
                            color = TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .clickable {
                                inputSmsText =
                                    "MB Bank: +4,500,000 VND vao 08/06/2026 09:00. ND: CONG TY HO CHAU CHUYEN KHOAN LUONG THANG 5. So du: 13,400,000 VND."
                            }
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "MB Bank (+4.5M)",
                            color = TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (inputSmsText.isNotBlank()) {
                            viewModel.parseSmsWithGemini(inputSmsText)
                        } else {
                            Toast.makeText(context, "Vui lòng nhập hoặc chọn mẫu tin nhắn!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
                ) {
                    Icon(imageVector = Icons.Default.SmartToy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Phân tích bằng Gemini AI", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Handle AI parsing visual feedback states
        item {
            AnimatedContent(targetState = aiState, label = "AI State") { state ->
                when (state) {
                    AiParseState.Idle -> Spacer(modifier = Modifier.height(0.dp))
                    AiParseState.Loading -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = EmeraldPrimary)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Đang xử lý phân tích logic qua Gemini...",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    is AiParseState.Error -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Lỗi",
                                    tint = ExpenseRed
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    color = ExpenseRed,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = { viewModel.clearAiParseState() }) {
                                    Text("Đóng bớt báo lỗi", color = TextSecondary)
                                }
                            }
                        }
                    }
                    is AiParseState.Success -> {
                        val parsed = state.transaction
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.15f)),
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "KẾT QUẢ PHÂN TÍCH AI CHÍNH XÁC",
                                        color = EmeraldPrimary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.sp
                                    )
                                    IconButton(onClick = { viewModel.clearAiParseState() }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            tint = TextSecondary
                                        )
                                    }
                                }
                                Divider(
                                    color = EmeraldPrimary.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Ngân hàng nhận diện:", color = TextSecondary, fontSize = 12.sp)
                                    Text(parsed.bankName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Số tiền chi tiêu:", color = TextSecondary, fontSize = 12.sp)
                                    Text(
                                        text = "${if (parsed.type == "INCOME") "+" else "-"} ${formatVnd(parsed.amount)}",
                                        color = if (parsed.type == "INCOME") IncomeGreen else ExpenseRed,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Danh mục tương ứng:", color = TextSecondary, fontSize = 12.sp)
                                    val parsedColor = rememberCategoryColor(parsed.category)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(parsedColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            parsed.category,
                                            color = parsedColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Ghi chú bóc tách:", color = TextSecondary, fontSize = 12.sp)
                                    Text(
                                        parsed.note.ifBlank { "Không ghi chú" },
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        viewModel.addTransaction(
                                            amount = parsed.amount,
                                            type = parsed.type,
                                            category = parsed.category,
                                            note = parsed.note,
                                            bankName = parsed.bankName,
                                            rawSms = inputSmsText
                                        )
                                        viewModel.clearAiParseState()
                                        inputSmsText = ""
                                        Toast.makeText(context, "AI đã lưu giao dịch & cập nhật số dư thành công!", Toast.LENGTH_LONG).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldPrimary,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Đồng ý & Ghi nhận vào ví", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Automated Notification Reader Panel
        item {
            var isPermissionEnabled by remember {
                mutableStateOf(
                    try {
                        val flat = android.provider.Settings.Secure.getString(
                            context.contentResolver,
                            "enabled_notification_listeners"
                        )
                        flat != null && flat.contains(context.packageName)
                    } catch (e: Exception) {
                        false
                    }
                )
            }

            // A lifecycle observer or dynamic check on resume
            androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle.addObserver(
                object : androidx.lifecycle.DefaultLifecycleObserver {
                    override fun onResume(owner: androidx.lifecycle.LifecycleOwner) {
                        try {
                            val flat = android.provider.Settings.Secure.getString(
                                context.contentResolver,
                                "enabled_notification_listeners"
                            )
                            isPermissionEnabled = flat != null && flat.contains(context.packageName)
                        } catch (e: Exception) {
                            // fallback
                        }
                    }
                }
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isPermissionEnabled) SleekRoyalPurple.copy(alpha = 0.25f) else ExpenseRed.copy(alpha = 0.3f),
                        RoundedCornerShape(24.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPermissionEnabled) SleekLightLavender.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPermissionEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isPermissionEnabled) IncomeGreen else ExpenseRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Tự Động Đọc Thông Báo Ngân Hàng",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = SleekDeepPurple
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Giải pháp bảo mật cao: Ứng dụng VinaSpends tự động lắng nghe biến động số dư từ thanh trạng thái Notification trên điện thoại của bạn, tự động bóc tách số dư & giao dịch ngoại tuyến với AI mà không cần login tài khoản ngân hàng mật khẩu của bạn.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextPrimary,
                            fontSize = 11.5.sp,
                            lineHeight = 17.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    if (isPermissionEnabled) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(IncomeGreen.copy(alpha = 0.08f))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(IncomeGreen)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Dịch vụ đang hoạt động an toàn ngầm",
                                    color = IncomeGreen,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Hệ thống đang sẵn sàng tự động bóc tách thông báo từ Vietcombank, Techcombank, MB Bank, ACB, VPBank, VIB, HSBC, Sacombank, Ví MoMo, ZaloPay, SMS ngân hàng, Gmail...",
                                color = SleekDeepPurple.copy(alpha = 0.9f),
                                fontSize = 10.5.sp,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.foundation.text.ClickableText(
                                text = androidx.compose.ui.text.AnnotatedString("💡 MẸO SỬA LỖI: Nếu thiết bị có thông báo mới nhưng app không tự phân tích, hãy click vào dòng chữ này để mở Cài đặt hệ thống, tắt quyền đọc thông báo đi rồi bật lại (hoặc khởi động lại máy) để Android kích hoạt lại dịch vụ bị đơ nhé!"),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SleekRoyalPurple,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                ),
                                onClick = {
                                    try {
                                        context.startActivity(
                                            android.content.Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                                        )
                                    } catch (e: Exception) {}
                                }
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                try {
                                    context.startActivity(
                                        android.content.Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                                    )
                                    Toast.makeText(context, "Vui lòng cho phép 'VinaSpends' truy cập thông báo!", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Thiết bị không hỗ trợ phím tắt này. Bạn có thể tìm trong Cài đặt hệ thống.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleekRoyalPurple,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("BẬT LẮNG NGHE THÔNG BÁO TỰ ĐỘNG", fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // AI Notification Simulator Panel (Demonstrates real-time extraction)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        EmeraldPrimary.copy(alpha = 0.25f),
                        RoundedCornerShape(24.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Giả Lập Nhận Thông Báo AI",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldPrimary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Do việc cấu hình quyền đọc thông báo hệ điều hành cần bước kích hoạt thủ công, bạn hãy bấm vào các nút dưới đây để GIẢ LẬP ứng dụng nhận được SMS/Notification ngân hàng thực tế. Hệ thống AI bóc tách sẽ ngay lập tức chạy ngầm phân tích và cộng/trừ số dư ví ngoại tuyến tương ứng:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextPrimary,
                            fontSize = 11.5.sp,
                            lineHeight = 17.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    var isProcessingSim by remember { mutableStateOf(false) }
                    
                    if (isProcessingSim) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(EmeraldPrimary.copy(alpha = 0.15f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(color = EmeraldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Trợ lý AI Gemini đang bóc tách giao dịch...",
                                color = EmeraldPrimary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    } else {
                        val simOptions = listOf(
                            "Chi tiêu thẻ tín dụng MB Visa" to "MB Bank: GD Card phat sinh GD chi tieu: -950,000 VND tu the MB Visa *1222 tai CGV Cinema. So du han muc con lai: 29,050,000 VND.",
                            "Thanh toán hoàn trả dư nợ MB Credit" to "MB Bank: Tai khoan *1222 duoc nhan +5,000,000 VND tu Đăng Khoa thanh toan du no the tin dung.",
                            "Nhận lương chuyển khoản MB" to "MB Bank: +15,000,000 VND vao 08/06/2026. ND: CT TNHH CÔNG NGHỆ VINASPENDS CHUYEN KHOAN LUONG THANG 5.",
                            "Mua trà sữa GongCha Vietcombank" to "Vietcombank GD: -120,000 VND luc 08-06-2026 15:45. ND: Mua tra sua gongcha de thuong.",
                            "Ví MoMo đi Shopee" to "Ví MoMo thanh toan thành công don hang shopee: -65,000 VND. So du trong vi 2,400,000 VND."
                        )
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            simOptions.forEach { (label, smsText) ->
                                Button(
                                    onClick = {
                                        isProcessingSim = true
                                        viewModel.simulateIncomingNotification(smsText) { result ->
                                            isProcessingSim = false
                                            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f),
                                        contentColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = label,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = EmeraldPrimary
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // List of Available Banks in Vietnam
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quản lý Danh sách Ví & Tài khoản",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                TextButton(onClick = { showLinkNewBankDialog = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Thêm ví/tài khoản", color = EmeraldPrimary)
                }
            }
        }

        // Vietnam Bank options mapping
        items(accounts) { linkedBank ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BankLogo(
                        bankName = linkedBank.name,
                        bankId = linkedBank.id,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = linkedBank.name,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Số tài khoản: ${linkedBank.accountNo}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = formatVnd(linkedBank.balance),
                            color = EmeraldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        IconButton(onClick = { editingAccountInLinkingTab = linkedBank }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Sửa ví",
                                tint = SleekRoyalPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = { viewModel.removeBankAccount(linkedBank.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Xóa ví",
                                tint = ExpenseRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showGmailLoginDialog) {
        var emailInput by remember { mutableStateOf("drugunhp142@gmail.com") }
        var nameInput by remember { mutableStateOf("Đăng Khoa") }
        var isLoggingIn by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { if (!isLoggingIn) showGmailLoginDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEA4335)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mail,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "ĐĂNG NHẬP GMAIL",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        )
                    }

                    Text(
                        text = "VinaSpends sử dụng mô phỏng OAuth2 bảo mật để đăng nhập Gmail và cấp quyền lưu tệp backup trên Google Drive cá nhân của bạn.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Địa chỉ Gmail đăng nhập", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Họ & Tên của bạn", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    if (isLoggingIn) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(color = EmeraldPrimary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Đang xác thực bảo mật OAuth2...",
                                fontSize = 11.sp,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TextButton(
                                onClick = { showGmailLoginDialog = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("HUỶ BỎ", color = TextSecondary, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    if (emailInput.isBlank() || !emailInput.contains("@")) {
                                        Toast.makeText(context, "Vui lòng nhập địa chỉ Gmail hợp lệ!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (nameInput.isBlank()) {
                                        Toast.makeText(context, "Vui lòng nhập họ tên của bạn!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isLoggingIn = true
                                    viewModel.signInWithGoogle(emailInput, nameInput)
                                    // Simulation delay for auth
                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        isLoggingIn = false
                                        showGmailLoginDialog = false
                                        Toast.makeText(context, "Đăng nhập Google thành công: Chào mừng $nameInput!", Toast.LENGTH_SHORT).show()
                                    }, 1200)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("ĐĂNG NHẬP", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
        } else {
            Box(modifier = Modifier.weight(1f)) {
                DebtsSubTabContent(viewModel = viewModel)
            }
        }
    }

    if (showLinkNewBankDialog) {
        LinkNewBankDialog(
            onDismiss = { showLinkNewBankDialog = false },
            onConfirm = { bankName, bankAccNo, initialBalance, accountType, creditLimit, creditSpent ->
                viewModel.addBankAccount(bankName, bankAccNo, initialBalance, accountType, creditLimit, creditSpent)
                showLinkNewBankDialog = false
                Toast.makeText(context, "Thêm ví/tài khoản mới thành công!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (editingAccountInLinkingTab != null) {
        EditWalletDialog(
            account = editingAccountInLinkingTab!!,
            onDismiss = { editingAccountInLinkingTab = null },
            onConfirm = { name, accNo, balance, accType, creditLimit, creditSpent ->
                viewModel.updateBankAccount(editingAccountInLinkingTab!!.id, name, accNo, balance, accType, creditLimit, creditSpent)
                editingAccountInLinkingTab = null
                Toast.makeText(context, "Cập nhật ví thành công!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// ----------------------------------------------------
// POPUPS AND DIALOG COMPOSABLES
// ----------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWalletDialog(
    account: BankAccount,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String, Double, Double) -> Unit
) {
    val context = LocalContext.current
    var walletName by remember { mutableStateOf(account.name) }
    var accountNo by remember { mutableStateOf(account.accountNo) }
    val initialLogo = remember(account.id, account.name) {
        val prefs = context.getSharedPreferences("vinaspends_prefs", android.content.Context.MODE_PRIVATE)
        prefs.getString("wallet_logo_${account.id}", account.name) ?: (prefs.getString("wallet_logo_${account.name}", account.name) ?: account.name)
    }
    var selectedLogoName by remember { mutableStateOf(initialLogo) }
    val initialBalance = formatInputThousands(account.balance.toLong().toString())
    var balanceValue by remember {
        mutableStateOf(TextFieldValue(text = initialBalance, selection = TextRange(initialBalance.length)))
    }
    val initialLimit = formatInputThousands(account.creditLimit.toLong().toString())
    var creditLimitValue by remember {
        mutableStateOf(TextFieldValue(text = initialLimit, selection = TextRange(initialLimit.length)))
    }
    val initialSpent = formatInputThousands(account.creditSpent.toLong().toString())
    var creditSpentValue by remember {
        mutableStateOf(TextFieldValue(text = initialSpent, selection = TextRange(initialSpent.length)))
    }
    var accountType by remember { mutableStateOf(account.accountType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Chỉnh Sửa Ví / Tài Khoản",
                fontWeight = FontWeight.ExtraBold,
                color = SleekDeepPurple,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Wallet Name
                OutlinedTextField(
                    value = walletName,
                    onValueChange = { walletName = it },
                    label = { Text("Tên Ví / Ngân hàng/ Ký hiệu", color = SleekRoyalPurple) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekRoyalPurple,
                        focusedLabelColor = SleekRoyalPurple,
                        cursorColor = SleekRoyalPurple
                    ),
                    singleLine = true
                )

                // Wallet Type selection
                Text(
                    text = "Loại Ví / Tài khoản",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekDeepPurple
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (accountType == "THUONG") SleekLightLavender else SleekSurfaceVariant)
                            .clickable { accountType = "THUONG" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Ví Thường (Debit)",
                            color = if (accountType == "THUONG") SleekDeepPurple else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (accountType == "TIN_DUNG") SleekLightLavender else SleekSurfaceVariant)
                            .clickable { accountType = "TIN_DUNG" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Thẻ Tín Dụng (Credit)",
                            color = if (accountType == "TIN_DUNG") SleekDeepPurple else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Account No
                OutlinedTextField(
                    value = accountNo,
                    onValueChange = { accountNo = it },
                    label = { Text("Số tài khoản / Ký hiệu ví", color = SleekRoyalPurple) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekRoyalPurple,
                        focusedLabelColor = SleekRoyalPurple,
                        cursorColor = SleekRoyalPurple
                    ),
                    singleLine = true
                )

                // Current balance / limit / spent
                if (accountType == "THUONG") {
                    OutlinedTextField(
                        value = balanceValue,
                        onValueChange = { input ->
                            balanceValue = formatTextFieldValue(input, allowNegative = true)
                        },
                        label = { 
                            Text("Số dư thực tế hiện tại (VND)", color = SleekRoyalPurple) 
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekRoyalPurple,
                            focusedLabelColor = SleekRoyalPurple,
                            cursorColor = SleekRoyalPurple
                        ),
                        singleLine = true
                    )
                } else {
                    OutlinedTextField(
                        value = creditLimitValue,
                        onValueChange = { input ->
                            creditLimitValue = formatTextFieldValue(input, allowNegative = false)
                        },
                        label = { 
                            Text("Hạn mức tín dụng / Thẻ (VND)", color = SleekRoyalPurple) 
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekRoyalPurple,
                            focusedLabelColor = SleekRoyalPurple,
                            cursorColor = SleekRoyalPurple
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = creditSpentValue,
                        onValueChange = { input ->
                            creditSpentValue = formatTextFieldValue(input, allowNegative = false)
                        },
                        label = { 
                            Text("Số tiền đã chi tiêu (VND)", color = SleekRoyalPurple) 
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekRoyalPurple,
                            focusedLabelColor = SleekRoyalPurple,
                            cursorColor = SleekRoyalPurple
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Selector for custom logo brand
                BankLogoSelector(
                    selectedLogo = selectedLogoName,
                    onLogoSelected = { selectedLogoName = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = if (accountType == "TIN_DUNG") parseInputThousands(creditLimitValue.text) else 0.0
                    val spent = if (accountType == "TIN_DUNG") parseInputThousands(creditSpentValue.text) else 0.0
                    val finalBalance = if (accountType == "TIN_DUNG") (limit - spent) else parseInputThousands(balanceValue.text)

                    // Save custom logo brand mapping for this edited wallet
                    context.getSharedPreferences("vinaspends_prefs", android.content.Context.MODE_PRIVATE)
                        .edit()
                        .putString("wallet_logo_${account.id}", selectedLogoName)
                        .putString("wallet_logo_${walletName}", selectedLogoName)
                        .apply()

                    onConfirm(walletName, accountNo, finalBalance, accountType, limit, spent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekRoyalPurple,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Lưu Thay Đổi", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Huỷ", color = TextSecondary)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}

// ----------------------------------------------------
// TAB 4: CATEGORY MANAGEMENT TAB SCREEN
// ----------------------------------------------------
@Composable
fun CategoryManagementTabContent(
    categories: List<String>,
    viewModel: FinanceViewModel,
    categoryLimits: Map<String, Double>,
    currentMonthExpensesByCategory: Map<String, Double>,
    onCustomizeCategory: (String) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("vinaspends_prefs", android.content.Context.MODE_PRIVATE) }
    
    val deletedCats by viewModel.deletedCategories.collectAsStateWithLifecycle()
    val parentMap by viewModel.categoryParentMap.collectAsStateWithLifecycle()
    
    var newCatName by remember { mutableStateOf("") }
    var selectedIconName by remember { mutableStateOf("Coffee") }
    var selectedColorHex by remember { mutableStateOf("#FB8C00") }
    
    var searchQuery by remember { mutableStateOf("") }
    var showIconDropdown by remember { mutableStateOf(false) }
    var expandedParents by remember { mutableStateOf(setOf<String>()) }
    var showSetLimitDialog by remember { mutableStateOf(false) }
    var categoryToSetLimitFor by remember { mutableStateOf<String?>(null) }
    
    val categoryIconOptions = listOf(
        // Eating & Drinking
        "Coffee" to Icons.Outlined.Coffee,
        "Restaurant" to Icons.Outlined.Restaurant,
        "LocalDrink" to Icons.Outlined.LocalDrink,
        "LocalBar" to Icons.Outlined.LocalBar,
        "BakeryDining" to Icons.Outlined.BakeryDining,
        "Icecream" to Icons.Outlined.Icecream,
        "LocalPizza" to Icons.Outlined.LocalPizza,
        "Fastfood" to Icons.Outlined.Fastfood,
        
        // Shopping & Store
        "ShoppingBag" to Icons.Outlined.ShoppingBag,
        "ShoppingCart" to Icons.Outlined.ShoppingCart,
        "Storefront" to Icons.Outlined.Storefront,
        
        // Transports & Vehicles
        "TwoWheeler" to Icons.Outlined.TwoWheeler,
        "DirectionsCar" to Icons.Outlined.DirectionsCar,
        "DirectionsBus" to Icons.Outlined.DirectionsBus,
        "DirectionsTransit" to Icons.Outlined.DirectionsTransit,
        "Explore" to Icons.Outlined.Explore,
        
        // Home, Bills, Utilities
        "Cottage" to Icons.Outlined.Cottage,
        "HistoryEdu" to Icons.Outlined.HistoryEdu,
        "Build" to Icons.Outlined.Build,
        "FlashOn" to Icons.Outlined.FlashOn,
        "WaterDrop" to Icons.Outlined.WaterDrop,
        "Wifi" to Icons.Outlined.Wifi,
        "Call" to Icons.Outlined.Call,
        "LocalShipping" to Icons.Outlined.LocalShipping,
        
        // Entertainment & Hobby
        "Gamepad" to Icons.Outlined.Gamepad,
        "MusicNote" to Icons.Outlined.MusicNote,
        "Movie" to Icons.Outlined.Movie,
        "TheaterComedy" to Icons.Outlined.TheaterComedy,
        "CameraAlt" to Icons.Outlined.CameraAlt,
        
        // Sports & Wellness
        "SelfImprovement" to Icons.Outlined.SelfImprovement,
        "Spa" to Icons.Outlined.Spa,
        "FitnessCenter" to Icons.Outlined.FitnessCenter,
        "SportsSoccer" to Icons.Outlined.SportsSoccer,
        
        // Health
        "LocalPharmacy" to Icons.Outlined.LocalPharmacy,
        "Favorite" to Icons.Outlined.Favorite,
        
        // Wealth & Work
        "Payments" to Icons.Outlined.Payments,
        "Savings" to Icons.Outlined.Savings,
        "QueryStats" to Icons.Outlined.QueryStats,
        "MonetizationOn" to Icons.Outlined.MonetizationOn,
        "CreditCard" to Icons.Outlined.CreditCard,
        
        // Education & Info
        "AutoStories" to Icons.Outlined.AutoStories,
        "School" to Icons.Outlined.School,
        "LaptopMac" to Icons.Outlined.LaptopMac,
        "Class" to Icons.Outlined.Class,
        "Description" to Icons.Outlined.Description,
        
        // People & Celebration
        "Groups" to Icons.Outlined.Groups,
        "Cake" to Icons.Outlined.Cake,
        "VolunteerActivism" to Icons.Outlined.VolunteerActivism,
        "Celebration" to Icons.Outlined.Celebration,
        "ChildCare" to Icons.Outlined.ChildCare,
        "ChildFriendly" to Icons.Outlined.ChildFriendly,
        
        // Nature & Decor
        "Pets" to Icons.Outlined.Pets,
        "WbSunny" to Icons.Outlined.WbSunny,
        "Nature" to Icons.Outlined.Nature,
        
        // Other
        "AutoAwesome" to Icons.Outlined.AutoAwesome,
        "Shield" to Icons.Outlined.Shield,
        "Devices" to Icons.Outlined.Devices,
        "LocalOffer" to Icons.Outlined.LocalOffer,
        "PushPin" to Icons.Outlined.PushPin,
        "Map" to Icons.Outlined.Map
    )
    
    val categoryColorOptions = listOf(
        Color(0xFFFB8C00), Color(0xFF03A9F4), Color(0xFFEC407A), Color(0xFFAB47BC),
        Color(0xFF4CAF50), Color(0xFF8D6E63), Color(0xFFEF5350), Color(0xFF5C6BC0),
        Color(0xFF78909C), Color(0xFF26A69A), Color(0xFF9CCC65), Color(0xFF26C6DA),
        Color(0xFFFF7043), Color(0xFFE91E63), Color(0xFF9E9E9E)
    )
    
    val customCategories by viewModel.customCategories.collectAsStateWithLifecycle()
    val parentCategories = remember(categories, parentMap) {
        categories.filter { !parentMap.containsKey(it) }
    }
    val subCategoriesGrouped = remember(categories, parentMap) {
        categories.filter { parentMap.containsKey(it) }.groupBy { parentMap[it]!! }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero banner card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SleekRoyalPurple)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "HẠNG MỤC CHI TIÊU",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tự do tạo, xóa hạng mục cá nhân hóa, trang bị bộ icon đa dạng và thay đổi màu sắc trực quan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 16.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // Section: Add Custom Category Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = SleekCardBg)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Thêm Hạng Mục Mới",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekDeepPurple
                    )
                    
                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        label = { Text("Tên hạng mục chi tiêu", fontSize = 12.sp) },
                        placeholder = { Text("Ví dụ: Sách vở, Sân bóng, Cafe...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.6f)
                        )
                    )

                    // Icon & Color Customization inside the creation card!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon Picker Trigger Button
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Chọn Icon",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .border(
                                            BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.6f)),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { showIconDropdown = true }
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = getIconFromName(selectedIconName),
                                        contentDescription = selectedIconName,
                                        tint = Color(android.graphics.Color.parseColor(selectedColorHex))
                                    )
                                    Text(
                                        text = selectedIconName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Color.Gray
                                    )
                                }

                                DropdownMenu(
                                    expanded = showIconDropdown,
                                    onDismissRequest = { showIconDropdown = false },
                                    modifier = Modifier.width(180.dp).height(240.dp)
                                ) {
                                    categoryIconOptions.forEach { (name, icon) ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Icon(imageVector = icon, contentDescription = name, modifier = Modifier.size(18.dp))
                                                    Text(name, fontSize = 13.sp)
                                                }
                                            },
                                            onClick = {
                                                selectedIconName = name
                                                showIconDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Color selection preview
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(
                                text = "Chọn Màu Sắc",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            LazyRow(
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(categoryColorOptions) { color ->
                                    val hexStr = String.format("#%06X", 0xFFFFFF and color.toArgb())
                                    val isSelected = selectedColorHex == hexStr
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = if (isSelected) 2.5.dp else 0.dp,
                                                color = SleekDeepPurple,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedColorHex = hexStr },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            val name = newCatName.trim()
                            if (name.isEmpty()) {
                                Toast.makeText(context, "Vui lòng nhập tên hạng mục!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (categories.contains(name)) {
                                Toast.makeText(context, "Hạng mục này đã tồn tại!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            // Save category icon/color customization BEFORE creating it
                            prefs.edit()
                                .putString("category_icon_$name", selectedIconName)
                                .putString("category_color_$name", selectedColorHex)
                                .apply()
                                
                            viewModel.addCustomCategory(name)
                            newCatName = ""
                            Toast.makeText(context, "Đã thêm hạng mục '$name' thành công!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("THÊM HẠNG MỤC", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Section: Budget Limits Management Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = SleekCardBg)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = SleekDeepPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Hạn Mức Chi Tiêu Tháng Này",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekDeepPurple
                            )
                        }
                        
                        Button(
                            onClick = {
                                categoryToSetLimitFor = null
                                showSetLimitDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekLightLavender, contentColor = SleekDeepPurple),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("+ Đặt hạn mức", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (categoryLimits.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Chưa thiết lập hạn mức nào",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Hãy thiết lập để nhận cảnh báo chi tiêu thông minh.",
                                    fontSize = 10.sp,
                                    color = TextSecondary.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Infinite transition for pulsing warning badges
                            val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "warningPulse")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 0.95f,
                                targetValue = 1.05f,
                                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                    animation = androidx.compose.animation.core.tween(1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                                ),
                                label = "warningPulseScale"
                            )

                            categoryLimits.forEach { (cat, limit) ->
                                val spent = currentMonthExpensesByCategory[cat] ?: 0.0
                                val isExceeded = spent > limit
                                val progressFraction = if (limit > 0.0) (spent / limit).toFloat().coerceIn(0f, 1f) else 0f
                                val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
                                    targetValue = progressFraction,
                                    animationSpec = androidx.compose.animation.core.spring(
                                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                                    ),
                                    label = "BudgetProgressAnimation"
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                                        .border(1.dp, Color.LightGray.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f, fill = false)
                                        ) {
                                            val icon = rememberCategoryIcon(category = cat)
                                            val color = rememberCategoryColor(category = cat)
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(color.copy(alpha = 0.12f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = color,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = cat,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${formatVnd(spent)} / ${formatVnd(limit)}",
                                                    fontSize = 11.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (isExceeded) {
                                                Box(
                                                    modifier = Modifier
                                                        .scale(pulseScale)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFFCF6560))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "VƯỢT HẠN MỨC",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 8.sp
                                                    )
                                                }
                                            } else {
                                                val pct = (progressFraction * 100).toInt()
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(EmeraldPrimary.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "$pct%",
                                                        color = EmeraldPrimary,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp
                                                    )
                                                }
                                            }

                                            IconButton(
                                                onClick = {
                                                    categoryToSetLimitFor = cat
                                                    showSetLimitDialog = true
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Sửa hạn mức", tint = SleekDeepPurple, modifier = Modifier.size(14.dp))
                                            }

                                            IconButton(
                                                onClick = {
                                                    viewModel.setCategoryLimit(cat, 0.0)
                                                    Toast.makeText(context, "Đã xóa hạn mức cho '$cat'", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Xóa hạn mức", tint = ExpenseRed.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }

                                    LinearProgressIndicator(
                                        progress = { animatedProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (isExceeded) Color(0xFFCF6560) else EmeraldPrimary,
                                        trackColor = Color.LightGray.copy(alpha = 0.15f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Search category bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Tất Cả Hạng Mục (${categories.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekDeepPurple
                )
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm kiếm nhanh hạng mục...", fontSize = 13.sp) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                    )
                )
            }
        }

        // Render categories items list
        if (searchQuery.isNotEmpty()) {
            val filtered = categories.filter { it.contains(searchQuery, ignoreCase = true) }
            if (filtered.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Không tìm thấy hạng mục nào phù hợp.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                items(filtered) { cat ->
                    CategoryRowItem(
                        cat = cat,
                        isCustom = customCategories.contains(cat),
                        parentCat = parentMap[cat],
                        onCustomize = onCustomizeCategory,
                        onDelete = { viewModel.deleteCategory(cat) },
                        indent = false,
                        hasChildren = false,
                        isExpanded = false,
                        onToggleExpand = {}
                    )
                }
            }
        } else {
            // Render hierarchical structure
            if (parentCategories.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Không tìm thấy hạng mục nào phù hợp.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                parentCategories.forEach { parentCat ->
                    val subs = subCategoriesGrouped[parentCat] ?: emptyList()
                    val isExpanded = expandedParents.contains(parentCat)
                    
                    item {
                        CategoryRowItem(
                            cat = parentCat,
                            isCustom = customCategories.contains(parentCat),
                            parentCat = null,
                            onCustomize = onCustomizeCategory,
                            onDelete = { viewModel.deleteCategory(parentCat) },
                            indent = false,
                            hasChildren = subs.isNotEmpty(),
                            isExpanded = isExpanded,
                            onToggleExpand = {
                                expandedParents = if (isExpanded) {
                                    expandedParents - parentCat
                                } else {
                                    expandedParents + parentCat
                                }
                            }
                        )
                    }
                    
                    if (isExpanded && subs.isNotEmpty()) {
                        items(subs) { subCat ->
                            CategoryRowItem(
                                cat = subCat,
                                isCustom = customCategories.contains(subCat),
                                parentCat = parentCat,
                                onCustomize = onCustomizeCategory,
                                onDelete = { viewModel.deleteCategory(subCat) },
                                indent = true,
                                hasChildren = false,
                                isExpanded = false,
                                onToggleExpand = {}
                            )
                        }
                    }
                }
            }
        }


    }

    if (showSetLimitDialog) {
        SetCategoryLimitDialog(
            categories = categories,
            currentLimits = categoryLimits,
            parentMap = parentMap,
            initialCategory = categoryToSetLimitFor,
            onDismiss = { showSetLimitDialog = false },
            onSave = { cat, limit ->
                viewModel.setCategoryLimit(cat, limit)
                showSetLimitDialog = false
                Toast.makeText(context, "Đã lưu hạn mức chi tiêu cho '$cat'!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetCategoryLimitDialog(
    categories: List<String>,
    currentLimits: Map<String, Double>,
    parentMap: Map<String, String>,
    initialCategory: String? = null,
    onDismiss: () -> Unit,
    onSave: (String, Double) -> Unit
) {
    var selectedCat by remember { mutableStateOf(initialCategory ?: categories.firstOrNull() ?: "") }
    var limitInput by remember { mutableStateOf(
        initialCategory?.let { currentLimits[it]?.let { l -> formatInputThousands(l.toLong().toString()) } } ?: ""
    ) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var expandedParents by remember { mutableStateOf(setOf<String>()) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SleekCardBg),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "THIẾT LẬP HẠN MỨC",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = SleekDeepPurple,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "Hạn mức này sẽ áp dụng để cảnh báo chi tiêu vượt ngưỡng trong tháng hiện tại.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Selection Dropdown
                Column {
                    Text(
                        text = "Chọn hạng mục",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable { categoryDropdownExpanded = true }
                            .padding(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val icon = rememberCategoryIcon(category = selectedCat)
                                val color = rememberCategoryColor(category = selectedCat)
                                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                                Text(text = selectedCat.ifEmpty { "Chưa chọn" }, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                        }
                        
                        DropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f).heightIn(max = 320.dp).background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            val parentCategories = remember(categories, parentMap) {
                                categories.filter { !parentMap.containsKey(it) }
                            }
                            val subCategoriesGrouped = remember(categories, parentMap) {
                                categories.filter { parentMap.containsKey(it) }.groupBy { parentMap[it]!! }
                            }

                            parentCategories.forEach { parentCat ->
                                val subs = subCategoriesGrouped[parentCat] ?: emptyList()
                                val isExpanded = expandedParents.contains(parentCat)
                                val parentColor = rememberCategoryColor(parentCat)
                                val parentIcon = rememberCategoryIcon(parentCat)

                                if (subs.isNotEmpty()) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(26.dp)
                                                        .clip(CircleShape)
                                                        .background(parentColor.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = parentIcon,
                                                        contentDescription = null,
                                                        tint = parentColor,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                                Text(
                                                    text = parentCat,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = TextPrimary,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Icon(
                                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = null,
                                                    tint = TextSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        },
                                        onClick = {
                                            expandedParents = if (isExpanded) {
                                                expandedParents - parentCat
                                            } else {
                                                expandedParents + parentCat
                                            }
                                        }
                                    )

                                    if (isExpanded) {
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.padding(start = 16.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = parentColor,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text(
                                                        text = "Chọn cả \"$parentCat\"",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TextSecondary
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedCat = parentCat
                                                categoryDropdownExpanded = false
                                                val existing = currentLimits[parentCat]
                                                if (existing != null) {
                                                    limitInput = formatInputThousands(existing.toLong().toString())
                                                } else {
                                                    limitInput = ""
                                                }
                                            }
                                        )

                                        subs.forEach { subCat ->
                                            val subColor = rememberCategoryColor(subCat)
                                            val subIcon = rememberCategoryIcon(subCat)
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        modifier = Modifier.padding(start = 16.dp)
                                                    ) {
                                                        Text(
                                                            text = "└──",
                                                            color = TextSecondary.copy(alpha = 0.5f),
                                                            fontSize = 12.sp
                                                        )
                                                        Box(
                                                            modifier = Modifier
                                                                .size(20.dp)
                                                                .clip(CircleShape)
                                                                .background(subColor.copy(alpha = 0.15f)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = subIcon,
                                                                contentDescription = null,
                                                                tint = subColor,
                                                                modifier = Modifier.size(10.dp)
                                                            )
                                                        }
                                                        Text(
                                                            text = subCat,
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 12.sp,
                                                            color = TextPrimary
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    selectedCat = subCat
                                                    categoryDropdownExpanded = false
                                                    val existing = currentLimits[subCat]
                                                    if (existing != null) {
                                                        limitInput = formatInputThousands(existing.toLong().toString())
                                                    } else {
                                                        limitInput = ""
                                                    }
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(26.dp)
                                                        .clip(CircleShape)
                                                        .background(parentColor.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = parentIcon,
                                                        contentDescription = null,
                                                        tint = parentColor,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                                Text(
                                                    text = parentCat,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = TextPrimary
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedCat = parentCat
                                            categoryDropdownExpanded = false
                                            val existing = currentLimits[parentCat]
                                            if (existing != null) {
                                                limitInput = formatInputThousands(existing.toLong().toString())
                                            } else {
                                                limitInput = ""
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Limit Amount Input
                Column {
                    Text(
                        text = "Số tiền hạn mức chi tiêu (VND)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = { input ->
                            val clean = input.replace(",", "").replace(".", "")
                            if (clean.all { it.isDigit() }) {
                                if (clean.isEmpty()) {
                                    limitInput = ""
                                } else {
                                    limitInput = formatInputThousands(clean)
                                }
                            }
                        },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        placeholder = { Text("Ví dụ: 2,000,000") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                        ),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = SleekDeepPurple, modifier = Modifier.size(18.dp))
                        }
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Hủy", color = TextSecondary)
                    }
                    
                    Button(
                        onClick = {
                            val limitDouble = limitInput.replace(",", "").replace(".", "").toDoubleOrNull() ?: 0.0
                            if (selectedCat.isNotEmpty() && limitDouble > 0) {
                                onSave(selectedCat, limitDouble)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Lưu", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryRowItem(
    cat: String,
    isCustom: Boolean,
    parentCat: String?,
    onCustomize: (String) -> Unit,
    onDelete: () -> Unit,
    indent: Boolean,
    hasChildren: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val context = LocalContext.current
    val catIcon = rememberCategoryIcon(category = cat)
    val catColor = rememberCategoryColor(category = cat)
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(if (isCustom) "Xóa Hạng Mục?" else "Ẩn Hạng Mục?") },
            text = { 
                Text(
                    if (isCustom) 
                        "Bạn có chắc chắn muốn xóa hạng mục tùy chỉnh '$cat'? Tất cả cấu hình liên quan sẽ bị xóa."
                    else 
                        "Bạn có chắc chắn muốn ẩn hạng mục mặc định '$cat'? Bạn có thể khôi phục hạng mục này ở mục danh sách đã ẩn ở cuối trang."
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                        Toast.makeText(context, if (isCustom) "Đã xóa hạng mục '$cat'!" else "Đã ẩn hạng mục '$cat'!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed, contentColor = Color.White)
                ) {
                    Text(if (isCustom) "Xóa" else "Ẩn")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (indent) 24.dp else 0.dp)
            .clickable { 
                if (hasChildren) {
                    onToggleExpand()
                } else {
                    onCustomize(cat) 
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (indent) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, if (indent) Color.LightGray.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (indent) {
                Text(
                    text = "└──",
                    color = TextSecondary.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Icon circle
            Box(
                modifier = Modifier
                    .size(if (indent) 32.dp else 40.dp)
                    .clip(CircleShape)
                    .background(catColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = catIcon,
                    contentDescription = null,
                    tint = catColor,
                    modifier = Modifier.size(if (indent) 16.dp else 20.dp)
                )
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = cat,
                        fontWeight = if (indent) FontWeight.SemiBold else FontWeight.Bold,
                        fontSize = if (indent) 13.sp else 14.sp,
                        color = TextPrimary
                    )
                    if (parentCat != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SleekLightLavender)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Hạng mục con",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekDeepPurple
                            )
                        }
                    } else if (hasChildren) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EmeraldPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Hạng mục lớn",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                    }
                }
                Text(
                    text = if (isCustom) "Hạng mục tùy chỉnh" else "Mặc định hệ thống",
                    fontSize = 10.sp,
                    color = if (isCustom) EmeraldSecondary else TextSecondary
                )
            }

            // Expand/Collapse Chevron for parent categories with subcategories
            if (hasChildren) {
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Thu phóng hạng mục con",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Customize trigger indicator
            IconButton(onClick = { onCustomize(cat) }) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Tùy chỉnh",
                    tint = Color.LightGray,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Delete button for all categories (custom completely removed, default hidden)
            IconButton(onClick = { showDeleteConfirmation = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Xóa hạng mục",
                    tint = ExpenseRed.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddManualTransactionDialog(
    accounts: List<BankAccount>,
    categories: List<String>,
    viewModel: FinanceViewModel,
    onAddCustomCategory: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String, String, String) -> Unit
) {
    var categoryToCustomize by remember { mutableStateOf<String?>(null) }
    var amountValue by remember { mutableStateOf(TextFieldValue("")) }
    var note by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("Ăn uống") }
    val parentMap by viewModel.categoryParentMap.collectAsStateWithLifecycle()
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val categoryLimits by viewModel.categoryLimits.collectAsStateWithLifecycle()
    
    val currentMonthExpensesByCategory = remember(transactions, parentMap) {
        val now = java.util.Calendar.getInstance()
        now.set(java.util.Calendar.DAY_OF_MONTH, 1)
        now.set(java.util.Calendar.HOUR_OF_DAY, 0)
        now.set(java.util.Calendar.MINUTE, 0)
        now.set(java.util.Calendar.SECOND, 0)
        now.set(java.util.Calendar.MILLISECOND, 0)
        val startOfMonth = now.timeInMillis
        
        val map = mutableMapOf<String, Double>()
        transactions.filter { 
            it.type == "EXPENSE" && it.timestamp >= startOfMonth 
        }.forEach { tx ->
            val targetCategory = parentMap[tx.category] ?: tx.category
            map[targetCategory] = (map[targetCategory] ?: 0.0) + tx.amount
        }
        map
    }
    var expandedParents by remember { mutableStateOf(setOf<String>()) }
    var selectedBank by remember { mutableStateOf(accounts.firstOrNull()?.name ?: "Tiền mặt") } // manual_add_selected_bank_state

    var categoryExpanded by remember { mutableStateOf(false) }
    var bankExpanded by remember { mutableStateOf(false) }
    var showAddNewCategoryField by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                isScanning = true
                coroutineScope.launch {
                    try {
                        val rawText = com.example.util.LocalOcrParser.recognizeTextFromUri(context, uri)
                        val parsed = com.example.util.LocalOcrParser.parseOcrText(rawText)
                        isScanning = false
                        if (parsed.isValidTransaction) {
                            amountValue = formatTextFieldValue(TextFieldValue(parsed.amount.toLong().toString()))
                            isExpense = parsed.type == "EXPENSE"
                            if (categories.contains(parsed.category)) {
                                selectedCategory = parsed.category
                            } else {
                                onAddCustomCategory(parsed.category)
                                selectedCategory = parsed.category
                            }
                            note = parsed.note
                            
                            val parsedBankLower = parsed.bankName.lowercase(Locale.ROOT)
                            val txBrand = when {
                                parsedBankLower.contains("techcom") || parsedBankLower.contains("tcb") -> "tcb"
                                parsedBankLower.contains("vietcom") || parsedBankLower.contains("vcb") -> "vcb"
                                parsedBankLower.contains("mbbank") || parsedBankLower.contains("mb") || parsedBankLower.contains("milit") -> "mb"
                                parsedBankLower.contains("vp") || parsedBankLower.contains("vpbank") -> "vp"
                                parsedBankLower.contains("tp") || parsedBankLower.contains("tpb") -> "tp"
                                parsedBankLower.contains("vib") -> "vib"
                                parsedBankLower.contains("sacom") || parsedBankLower.contains("stb") || parsedBankLower.contains("sacombank") -> "stb"
                                parsedBankLower.contains("acb") -> "acb"
                                parsedBankLower.contains("bidv") -> "bidv"
                                parsedBankLower.contains("agri") || parsedBankLower.contains("agribank") -> "agribank"
                                parsedBankLower.contains("momo") -> "momo"
                                parsedBankLower.contains("vietin") || parsedBankLower.contains("ctg") -> "ctg"
                                parsedBankLower.contains("shinhan") -> "shinhan"
                                else -> parsedBankLower
                            }

                            val sameBrandAccounts = accounts.filter { acc ->
                                val accNorm = acc.name.lowercase(Locale.ROOT)
                                val accBrand = when {
                                    accNorm.contains("techcom") || accNorm.contains("tcb") -> "tcb"
                                    accNorm.contains("vietcom") || accNorm.contains("vcb") -> "vcb"
                                    accNorm.contains("mbbank") || accNorm.contains("mb") || accNorm.contains("milit") -> "mb"
                                    accNorm.contains("vp") || accNorm.contains("vpbank") -> "vp"
                                    accNorm.contains("tp") || accNorm.contains("tpb") -> "tp"
                                    accNorm.contains("vib") -> "vib"
                                    accNorm.contains("sacom") || accNorm.contains("stb") || accNorm.contains("sacombank") -> "stb"
                                    accNorm.contains("acb") -> "acb"
                                    accNorm.contains("bidv") -> "bidv"
                                    accNorm.contains("agri") || accNorm.contains("agribank") -> "agribank"
                                    accNorm.contains("momo") -> "momo"
                                    accNorm.contains("vietin") || accNorm.contains("ctg") -> "ctg"
                                    accNorm.contains("shinhan") -> "shinhan"
                                    else -> accNorm
                                }
                                accBrand == txBrand
                            }

                            var matchedBank: String? = null
                            val txAccNo = parsed.accountNo?.trim()?.replace(" ", "") ?: ""

                            if (txAccNo.isNotEmpty() && txAccNo != "N/A" && txAccNo != "AUTO") {
                                // Suffix-based smart matching
                                val bestMatch = sameBrandAccounts.firstOrNull { acc ->
                                    val accClean = acc.accountNo.trim().replace(" ", "")
                                    if (accClean.isEmpty() || accClean.lowercase(Locale.ROOT) == "n/a") {
                                        false
                                    } else {
                                        val clean1 = accClean.filter { it.isDigit() }
                                        val clean2 = txAccNo.filter { it.isDigit() }
                                        if (clean1.isEmpty() || clean2.isEmpty()) {
                                            false
                                        } else if (clean1.length < 3 || clean2.length < 3) {
                                            clean1 == clean2
                                        } else if (clean1.length >= 4 && clean2.length >= 4) {
                                            clean1.takeLast(4) == clean2.takeLast(4)
                                        } else {
                                            clean1.endsWith(clean2) || clean2.endsWith(clean1)
                                        }
                                    }
                                }
                                if (bestMatch != null) {
                                    matchedBank = bestMatch.name
                                }
                            }

                            if (matchedBank == null && sameBrandAccounts.isNotEmpty()) {
                                // Match by type preference (Credit "TIN_DUNG" first if card keywords are found)
                                val noteLower = parsed.note.lowercase(Locale.ROOT)
                                val isCreditCardTx = noteLower.contains("tín dụng") || 
                                                     noteLower.contains("tin dung") || 
                                                     noteLower.contains("visa") || 
                                                     noteLower.contains("mastercard") || 
                                                     noteLower.contains("jcb") || 
                                                     noteLower.contains("credit") || 
                                                     noteLower.contains("card") || 
                                                     noteLower.contains("thẻ và") || 
                                                     noteLower.contains("thẻ tín dụng") ||
                                                     (txAccNo.contains("*") || txAccNo.contains("x") || txAccNo.contains("X") || txAccNo.contains("."))
                                
                                val preferred = sameBrandAccounts.firstOrNull { acc ->
                                    if (isCreditCardTx) acc.accountType == "TIN_DUNG" else acc.accountType != "TIN_DUNG"
                                }
                                matchedBank = preferred?.name ?: sameBrandAccounts.firstOrNull()?.name
                            }

                            if (matchedBank == null) {
                                matchedBank = accounts.firstOrNull { acc ->
                                    acc.name.lowercase(Locale.ROOT).contains(parsed.bankName.lowercase(Locale.ROOT)) ||
                                    parsed.bankName.lowercase(Locale.ROOT).contains(acc.name.lowercase(Locale.ROOT))
                                }?.name ?: (if (parsed.bankName.lowercase(Locale.ROOT).contains("momo")) "Ví MoMo" else parsed.bankName)
                            }
                            selectedBank = matchedBank
                            
                            Toast.makeText(context, "Quét hóa đơn bằng OCR nội bộ thành công!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Mẹo: Hãy chọn ảnh hóa đơn rõ nét hơn để OCR nhận diện!", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        isScanning = false
                        Toast.makeText(context, "Lỗi nhận diện ảnh cục bộ: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            if (isScanning) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = SleekDeepPurple)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Trợ lý AI đang đọc giao dịch qua ảnh...",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Đang phân tích số tiền, hạng mục, nguồn tiền & mô tả của hóa đơn / ảnh chụp thanh toán.",
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "GHI CHÉP GIAO DỊCH",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    )

                    // Button scan AI
                    Button(
                        onClick = { photoLauncher.launch("image/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SleekLightLavender,
                            contentColor = SleekDeepPurple
                        ),
                        border = BorderStroke(1.dp, SleekDeepPurple.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Thêm qua ảnh bằng AI",
                            tint = SleekDeepPurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "TỰ ĐỘNG ĐIỀN QUA ẢNH GIAO DỊCH",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = SleekDeepPurple
                            )
                        )
                    }

                // Expense vs Income toggle UI
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isExpense) ExpenseRed else Color.Transparent)
                            .clickable { isExpense = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "CHI TIÊU",
                            color = if (isExpense) Color.White else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!isExpense) IncomeGreen else Color.Transparent)
                            .clickable { isExpense = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "THU NHẬP",
                            color = if (!isExpense) Color.Black else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Amount text input
                OutlinedTextField(
                    value = amountValue,
                    onValueChange = { input ->
                        amountValue = formatTextFieldValue(input, allowNegative = false)
                    },
                    label = { Text("Số tiền (VND)", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_amount_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )

                // Quick amount suggestion row
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    val suggestions = listOf(
                        "+10K" to 10000L,
                        "+20K" to 20000L,
                        "+50K" to 50000L,
                        "+100K" to 100000L,
                        "+200K" to 200000L,
                        "+500K" to 500000L,
                        "+1M" to 1000000L
                    )
                    items(suggestions) { (label, value) ->
                        SuggestionChip(
                            onClick = {
                                val currentLong = amountValue.text.filter { it.isDigit() }.toLongOrNull() ?: 0L
                                val newLong = currentLong + value
                                val formatted = formatInputThousands(newLong.toString())
                                amountValue = TextFieldValue(text = formatted, selection = TextRange(formatted.length))
                            },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            border = null
                        )
                    }
                }

                // Category selector dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        label = { Text("Hạng mục chi tiêu", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryExpanded = true },
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = TextPrimary,
                            disabledBorderColor = TextSecondary.copy(alpha = 0.5f),
                            disabledLabelColor = TextSecondary
                        ),
                        leadingIcon = {
                            IconButton(onClick = { categoryToCustomize = selectedCategory }) {
                                Icon(
                                    imageVector = rememberCategoryIcon(selectedCategory),
                                    contentDescription = "Chỉnh sửa biểu tượng hạng mục",
                                    tint = rememberCategoryColor(selectedCategory)
                                )
                            }
                        },
                        trailingIcon = {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    )
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.7f).heightIn(max = 320.dp)
                    ) {
                        val parentCategories = remember(categories, parentMap) {
                            categories.filter { !parentMap.containsKey(it) }
                        }
                        val subCategoriesGrouped = remember(categories, parentMap) {
                            categories.filter { parentMap.containsKey(it) }.groupBy { parentMap[it]!! }
                        }

                        parentCategories.forEach { parentCat ->
                            val subs = subCategoriesGrouped[parentCat] ?: emptyList()
                            val isExpanded = expandedParents.contains(parentCat)
                            val parentColor = rememberCategoryColor(parentCat)
                            val parentIcon = rememberCategoryIcon(parentCat)

                            if (subs.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(CircleShape)
                                                    .background(parentColor.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = parentIcon,
                                                    contentDescription = null,
                                                    tint = parentColor,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            Text(
                                                text = parentCat,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = TextPrimary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = TextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    onClick = {
                                        expandedParents = if (isExpanded) {
                                            expandedParents - parentCat
                                        } else {
                                            expandedParents + parentCat
                                        }
                                    }
                                )

                                if (isExpanded) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.padding(start = 16.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = parentColor,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = "Chọn cả \"$parentCat\"",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextSecondary
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedCategory = parentCat
                                            categoryExpanded = false
                                        }
                                    )

                                    subs.forEach { subCat ->
                                        val subColor = rememberCategoryColor(subCat)
                                        val subIcon = rememberCategoryIcon(subCat)
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.padding(start = 16.dp)
                                                ) {
                                                    Text(
                                                        text = "└──",
                                                        color = TextSecondary.copy(alpha = 0.5f),
                                                        fontSize = 12.sp
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .size(20.dp)
                                                            .clip(CircleShape)
                                                            .background(subColor.copy(alpha = 0.15f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = subIcon,
                                                            contentDescription = null,
                                                            tint = subColor,
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = subCat,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 12.sp,
                                                        color = TextPrimary
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedCategory = subCat
                                                categoryExpanded = false
                                            }
                                        )
                                    }
                                }
                            } else {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(CircleShape)
                                                    .background(parentColor.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = parentIcon,
                                                    contentDescription = null,
                                                    tint = parentColor,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            Text(
                                                text = parentCat,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = TextPrimary
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedCategory = parentCat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                        Divider()
                        DropdownMenuItem(
                            text = { Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Thêm hạng mục...", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }},
                            onClick = {
                                showAddNewCategoryField = true
                                categoryExpanded = false
                            }
                        )
                    }
                }

                // Real-time Budget Limit Warning
                val selectedParentCat = parentMap[selectedCategory] ?: selectedCategory
                val selectedLimit = categoryLimits[selectedParentCat] ?: 0.0
                if (isExpense && selectedLimit > 0.0) {
                    val spentInSelected = currentMonthExpensesByCategory[selectedParentCat] ?: 0.0
                    val inputtedVal = amountValue.text.replace(",", "").replace(".", "").toDoubleOrNull() ?: 0.0
                    if (spentInSelected + inputtedVal > selectedLimit) {
                        val overAmount = (spentInSelected + inputtedVal) - selectedLimit
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFF1F1))
                                .border(1.dp, Color(0xFFF9D5D5), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFCF6560),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Hạn mức tháng này: Đã chi ${formatVnd(spentInSelected)} / ${formatVnd(selectedLimit)}. Giao dịch này sẽ vượt thêm ${formatVnd(overAmount)}!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFCF6560),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Bank Account selector dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedBank,
                        onValueChange = {},
                        label = { Text("Nguồn tiền (Ngân hàng/Ví)", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { bankExpanded = true },
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = TextPrimary,
                            disabledBorderColor = TextSecondary.copy(alpha = 0.5f),
                            disabledLabelColor = TextSecondary
                        ),
                        trailingIcon = {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    )
                    DropdownMenu(
                        expanded = bankExpanded,
                        onDismissRequest = { bankExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.75f)
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        BankLogo(
                                            bankName = acc.name,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(acc.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                    }
                                },
                                onClick = {
                                    selectedBank = acc.name
                                    bankExpanded = false
                                }
                            )
                        }
                    }
                }

                // Note description input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú miêu tả / Lời nhắn", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f)
                    ),
                    singleLine = false,
                    minLines = 4,
                    maxLines = 10
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("HUỶ BỎ", color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val amount = parseInputThousands(amountValue.text)
                            if (amount > 0) {
                                onConfirm(
                                    amount,
                                    if (isExpense) "EXPENSE" else "INCOME",
                                    selectedCategory,
                                    note,
                                    selectedBank
                                )
                            }
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
                    ) {
                        Text("GHI SỔ KHUYA", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

    if (showAddNewCategoryField) {
        var newCatName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddNewCategoryField = false },
            title = { Text("Thêm hạng mục chi tiêu") },
            text = {
                OutlinedTextField(
                    value = newCatName,
                    onValueChange = { newCatName = it },
                    label = { Text("Tên hạng mục mới") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newCatName.trim()
                        if (trimmed.isNotEmpty()) {
                            onAddCustomCategory(trimmed)
                            selectedCategory = trimmed
                        }
                        showAddNewCategoryField = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
                ) {
                    Text("THÊM", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNewCategoryField = false }) {
                    Text("HỦY", color = TextSecondary)
                }
            }
        )
    }

    if (categoryToCustomize != null) {
        EditCategoryDialog(
            category = categoryToCustomize!!,
            viewModel = viewModel,
            onDismiss = { categoryToCustomize = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transaction: TransactionEntity,
    accounts: List<BankAccount>,
    categories: List<String>,
    viewModel: FinanceViewModel,
    onAddCustomCategory: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (TransactionEntity) -> Unit
) {
    var categoryToCustomize by remember { mutableStateOf<String?>(null) }
    val initialAmount = formatInputThousands(transaction.amount.toLong().toString())
    var amountValue by remember {
        mutableStateOf(TextFieldValue(text = initialAmount, selection = TextRange(initialAmount.length)))
    }
    var note by remember { mutableStateOf(transaction.note) }
    var isExpense by remember { mutableStateOf(transaction.type == "EXPENSE") }
    var selectedCategory by remember { mutableStateOf(transaction.category) }
    val parentMap by viewModel.categoryParentMap.collectAsStateWithLifecycle()
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val categoryLimits by viewModel.categoryLimits.collectAsStateWithLifecycle()
    
    val currentMonthExpensesByCategory = remember(transactions, parentMap) {
        val now = java.util.Calendar.getInstance()
        now.set(java.util.Calendar.DAY_OF_MONTH, 1)
        now.set(java.util.Calendar.HOUR_OF_DAY, 0)
        now.set(java.util.Calendar.MINUTE, 0)
        now.set(java.util.Calendar.SECOND, 0)
        now.set(java.util.Calendar.MILLISECOND, 0)
        val startOfMonth = now.timeInMillis
        
        val map = mutableMapOf<String, Double>()
        transactions.filter { 
            it.type == "EXPENSE" && it.timestamp >= startOfMonth 
        }.forEach { tx ->
            val targetCategory = parentMap[tx.category] ?: tx.category
            map[targetCategory] = (map[targetCategory] ?: 0.0) + tx.amount
        }
        map
    }
    var expandedParents by remember { mutableStateOf(setOf<String>()) }
    var editSelectedBank by remember { mutableStateOf(transaction.bankName) } // edit_selected_bank_state
    
    val context = LocalContext.current
    var txTimestamp by remember { mutableStateOf(transaction.timestamp) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var bankExpanded by remember { mutableStateOf(false) }
    var showAddNewCategoryField by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "SỬA LỊCH SỬ GIAO DỊCH",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                )

                // Expense vs Income toggle UI
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isExpense) ExpenseRed else Color.Transparent)
                            .clickable { isExpense = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "CHI TIÊU",
                            color = if (isExpense) Color.White else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!isExpense) IncomeGreen else Color.Transparent)
                            .clickable { isExpense = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "THU NHẬP",
                            color = if (!isExpense) Color.Black else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Amount text input
                OutlinedTextField(
                    value = amountValue,
                    onValueChange = { input ->
                        amountValue = formatTextFieldValue(input, allowNegative = false)
                    },
                    label = { Text("Số tiền (VND)", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_transaction_amount_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )

                // Quick amount suggestion row
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    val suggestions = listOf(
                        "+10K" to 10000L,
                        "+20K" to 20000L,
                        "+50K" to 50000L,
                        "+100K" to 100000L,
                        "+200K" to 200000L,
                        "+500K" to 500000L,
                        "+1M" to 1000000L
                    )
                    items(suggestions) { (label, value) ->
                        SuggestionChip(
                            onClick = {
                                val currentLong = amountValue.text.filter { it.isDigit() }.toLongOrNull() ?: 0L
                                val newLong = currentLong + value
                                val formatted = formatInputThousands(newLong.toString())
                                amountValue = TextFieldValue(text = formatted, selection = TextRange(formatted.length))
                            },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            border = null
                        )
                    }
                }

                // Category selector dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        label = { Text("Hạng mục chi tiêu", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryExpanded = true },
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = TextPrimary,
                            disabledBorderColor = TextSecondary.copy(alpha = 0.5f),
                            disabledLabelColor = TextSecondary
                        ),
                        leadingIcon = {
                            IconButton(onClick = { categoryToCustomize = selectedCategory }) {
                                Icon(
                                    imageVector = rememberCategoryIcon(selectedCategory),
                                    contentDescription = "Chỉnh sửa biểu tượng hạng mục",
                                    tint = rememberCategoryColor(selectedCategory)
                                )
                            }
                        },
                        trailingIcon = {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    )
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.7f).heightIn(max = 320.dp)
                    ) {
                        val parentCategories = remember(categories, parentMap) {
                            categories.filter { !parentMap.containsKey(it) }
                        }
                        val subCategoriesGrouped = remember(categories, parentMap) {
                            categories.filter { parentMap.containsKey(it) }.groupBy { parentMap[it]!! }
                        }

                        parentCategories.forEach { parentCat ->
                            val subs = subCategoriesGrouped[parentCat] ?: emptyList()
                            val isExpanded = expandedParents.contains(parentCat)
                            val parentColor = rememberCategoryColor(parentCat)
                            val parentIcon = rememberCategoryIcon(parentCat)

                            if (subs.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(CircleShape)
                                                    .background(parentColor.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = parentIcon,
                                                    contentDescription = null,
                                                    tint = parentColor,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            Text(
                                                text = parentCat,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = TextPrimary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = TextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    onClick = {
                                        expandedParents = if (isExpanded) {
                                            expandedParents - parentCat
                                        } else {
                                            expandedParents + parentCat
                                        }
                                    }
                                )

                                if (isExpanded) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.padding(start = 16.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = parentColor,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = "Chọn cả \"$parentCat\"",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextSecondary
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedCategory = parentCat
                                            categoryExpanded = false
                                        }
                                    )

                                    subs.forEach { subCat ->
                                        val subColor = rememberCategoryColor(subCat)
                                        val subIcon = rememberCategoryIcon(subCat)
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.padding(start = 16.dp)
                                                ) {
                                                    Text(
                                                        text = "└──",
                                                        color = TextSecondary.copy(alpha = 0.5f),
                                                        fontSize = 12.sp
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .size(20.dp)
                                                            .clip(CircleShape)
                                                            .background(subColor.copy(alpha = 0.15f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = subIcon,
                                                            contentDescription = null,
                                                            tint = subColor,
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = subCat,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 12.sp,
                                                        color = TextPrimary
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedCategory = subCat
                                                categoryExpanded = false
                                            }
                                        )
                                    }
                                }
                            } else {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(CircleShape)
                                                    .background(parentColor.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = parentIcon,
                                                    contentDescription = null,
                                                    tint = parentColor,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            Text(
                                                text = parentCat,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = TextPrimary
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedCategory = parentCat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                        Divider()
                        DropdownMenuItem(
                            text = { Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Thêm hạng mục...", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }},
                            onClick = {
                                showAddNewCategoryField = true
                                categoryExpanded = false
                            }
                        )
                    }
                }

                // Real-time Budget Limit Warning
                val selectedParentCat = parentMap[selectedCategory] ?: selectedCategory
                val selectedLimit = categoryLimits[selectedParentCat] ?: 0.0
                if (isExpense && selectedLimit > 0.0) {
                    val spentInSelected = currentMonthExpensesByCategory[selectedParentCat] ?: 0.0
                    val inputtedVal = amountValue.text.replace(",", "").replace(".", "").toDoubleOrNull() ?: 0.0
                    val originalTxMonthAndCat = remember(transaction, selectedParentCat, parentMap) {
                        val now = java.util.Calendar.getInstance()
                        now.set(java.util.Calendar.DAY_OF_MONTH, 1)
                        now.set(java.util.Calendar.HOUR_OF_DAY, 0)
                        now.set(java.util.Calendar.MINUTE, 0)
                        now.set(java.util.Calendar.SECOND, 0)
                        now.set(java.util.Calendar.MILLISECOND, 0)
                        val startOfMonth = now.timeInMillis
                        
                        transaction.type == "EXPENSE" && 
                        transaction.timestamp >= startOfMonth && 
                        (parentMap[transaction.category] ?: transaction.category) == selectedParentCat
                    }
                    val adjustedSpent = if (originalTxMonthAndCat) {
                        (spentInSelected - transaction.amount).coerceAtLeast(0.0)
                    } else {
                        spentInSelected
                    }

                    if (adjustedSpent + inputtedVal > selectedLimit) {
                        val overAmount = (adjustedSpent + inputtedVal) - selectedLimit
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFF1F1))
                                .border(1.dp, Color(0xFFF9D5D5), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFCF6560),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Hạn mức tháng này: Đã chi ${formatVnd(spentInSelected)} / ${formatVnd(selectedLimit)}. Giao dịch này sẽ vượt thêm ${formatVnd(overAmount)}!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFCF6560),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Bank Account selector dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editSelectedBank,
                        onValueChange = {},
                        label = { Text("Nguồn tiền (Ngân hàng/Ví)", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { bankExpanded = true },
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = TextPrimary,
                            disabledBorderColor = TextSecondary.copy(alpha = 0.5f),
                            disabledLabelColor = TextSecondary
                        ),
                        trailingIcon = {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    )
                    DropdownMenu(
                        expanded = bankExpanded,
                        onDismissRequest = { bankExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.75f)
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        BankLogo(
                                            bankName = acc.name,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(acc.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                    }
                                },
                                onClick = {
                                    editSelectedBank = acc.name
                                    bankExpanded = false
                                }
                            )
                        }
                    }
                }

                // Note description input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú miêu tả / Lời nhắn", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f)
                    ),
                    singleLine = false,
                    minLines = 4,
                    maxLines = 10
                )

                // Date Time Field picker
                val calendar = remember(txTimestamp) { java.util.Calendar.getInstance().apply { timeInMillis = txTimestamp } }
                val formattedTimestamp = remember(txTimestamp) { formatDateTime(txTimestamp) }
                OutlinedTextField(
                    value = formattedTimestamp,
                    onValueChange = {},
                    label = { Text("Ngày giờ giao dịch", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val year = calendar.get(java.util.Calendar.YEAR)
                            val month = calendar.get(java.util.Calendar.MONTH)
                            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                            
                            android.app.DatePickerDialog(context, { _, y, m, d ->
                                val selectedCal = java.util.Calendar.getInstance().apply {
                                    timeInMillis = txTimestamp
                                    set(java.util.Calendar.YEAR, y)
                                    set(java.util.Calendar.MONTH, m)
                                    set(java.util.Calendar.DAY_OF_MONTH, d)
                                }
                                
                                val hour = selectedCal.get(java.util.Calendar.HOUR_OF_DAY)
                                val minute = selectedCal.get(java.util.Calendar.MINUTE)
                                
                                android.app.TimePickerDialog(context, { _, hh, mm ->
                                    selectedCal.set(java.util.Calendar.HOUR_OF_DAY, hh)
                                    selectedCal.set(java.util.Calendar.MINUTE, mm)
                                    txTimestamp = selectedCal.timeInMillis
                                }, hour, minute, true).show()
                            }, year, month, day).show()
                        },
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = TextPrimary,
                        disabledBorderColor = TextSecondary.copy(alpha = 0.5f),
                        disabledLabelColor = TextSecondary
                    ),
                    trailingIcon = {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Chọn ngày giờ", tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("HUỶ BỎ", color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val amount = parseInputThousands(amountValue.text)
                            if (amount > 0) {
                                val updatedTx = transaction.copy(
                                    amount = amount,
                                    type = if (isExpense) "EXPENSE" else "INCOME",
                                    category = selectedCategory,
                                    note = note,
                                    bankName = editSelectedBank,
                                    timestamp = txTimestamp
                                )
                                onConfirm(updatedTx)
                            }
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
                    ) {
                        Text("CẬP NHẬT", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }

    if (showAddNewCategoryField) {
        var newCatName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddNewCategoryField = false },
            title = { Text("Thêm hạng mục chi tiêu") },
            text = {
                OutlinedTextField(
                    value = newCatName,
                    onValueChange = { newCatName = it },
                    label = { Text("Tên hạng mục mới") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newCatName.trim()
                        if (trimmed.isNotEmpty()) {
                            onAddCustomCategory(trimmed)
                            selectedCategory = trimmed
                        }
                        showAddNewCategoryField = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
                ) {
                    Text("THÊM", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNewCategoryField = false }) {
                    Text("HỦY", color = TextSecondary)
                }
            }
        )
    }

    if (categoryToCustomize != null) {
        EditCategoryDialog(
            category = categoryToCustomize!!,
            viewModel = viewModel,
            onDismiss = { categoryToCustomize = null }
        )
    }
}

@Composable
fun LinkNewBankDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String, Double, Double) -> Unit
) {
    val context = LocalContext.current
    var selectedBank by remember { mutableStateOf("Vietcombank") }
    var selectedLogoName by remember { mutableStateOf("Vietcombank") }
    LaunchedEffect(selectedBank) {
        selectedLogoName = selectedBank
    }
    var isCustomName by remember { mutableStateOf(false) }
    var customBankName by remember { mutableStateOf("") }
    var accountNo by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf("THUONG") }

    val initialBalanceStr = formatInputThousands("5000000")
    var balanceValue by remember {
        mutableStateOf(TextFieldValue(text = initialBalanceStr, selection = TextRange(initialBalanceStr.length)))
    }
    val initialLimitStr = formatInputThousands("10000000")
    var creditLimitValue by remember {
        mutableStateOf(TextFieldValue(text = initialLimitStr, selection = TextRange(initialLimitStr.length)))
    }
    val initialSpentStr = formatInputThousands("0")
    var creditSpentValue by remember {
        mutableStateOf(TextFieldValue(text = initialSpentStr, selection = TextRange(initialSpentStr.length)))
    }
 
    val bankOptions = listOf(
        "Vietcombank", "Techcombank", "MB Bank", "TPBank", "VPBank", 
        "Agribank", "BIDV", "VietinBank", "Sacombank", "ACB", "VIB", "Shinhan Bank",
        "Ví MoMo", "ZaloPay", "Ví ShopeePay", "Viettel Money", "Tiền mặt"
    )
    var bankExpanded by remember { mutableStateOf(false) }
 
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, tint = EmeraldPrimary)
                    Text(
                        text = "KHỞI TẠO VÍ / TÀI KHOẢN MỚI",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                    )
                }
                Text(
                    text = "Không cần liên kết password mật khẩu ngân hàng. Dữ liệu số dư và biến động được lưu trữ 100% ngoại tuyến, tự động cập nhật qua AI khi có thông báo về máy.",
                    color = TextSecondary,
                    fontSize = 10.5.sp,
                    lineHeight = 14.sp
                )
 
                // Wallet Type selection UI
                Text(
                    text = "Phân Loại Ví / Thẻ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekDeepPurple
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (accountType == "THUONG") EmeraldPrimary.copy(alpha = 0.2f) else SleekSurfaceVariant)
                            .clickable { accountType = "THUONG" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Ví Thường (Debit)",
                            color = if (accountType == "THUONG") EmeraldPrimary else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (accountType == "TIN_DUNG") EmeraldPrimary.copy(alpha = 0.2f) else SleekSurfaceVariant)
                            .clickable { accountType = "TIN_DUNG" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Thẻ Tín Dụng (Credit)",
                            color = if (accountType == "TIN_DUNG") EmeraldPrimary else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tên Ví / Ngân hàng",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekDeepPurple
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isCustomName = !isCustomName }
                    ) {
                        Checkbox(
                            checked = isCustomName,
                            onCheckedChange = { isCustomName = it },
                            colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
                        )
                        Text("Tự nhập tên riêng", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (isCustomName) {
                    OutlinedTextField(
                        value = customBankName,
                        onValueChange = { customBankName = it },
                        label = { Text("Nhập tên ví tự chọn (v.d. Heo Đất, Thẻ VIB)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )
                } else {
                    // Select Bank Dropdown selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedBank,
                            onValueChange = {},
                            label = { Text("Đơn vị phát hành", fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { bankExpanded = true },
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = TextPrimary,
                                disabledBorderColor = TextSecondary.copy(alpha = 0.5f),
                                disabledLabelColor = TextSecondary
                            ),
                            leadingIcon = {
                                Box(modifier = Modifier.padding(start = 6.dp)) {
                                    BankLogo(bankName = selectedBank, modifier = Modifier.size(26.dp))
                                }
                            },
                            trailingIcon = {
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        )
                        DropdownMenu(
                            expanded = bankExpanded,
                            onDismissRequest = { bankExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.75f)
                        ) {
                            bankOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            BankLogo(
                                                bankName = opt,
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Text(
                                                text = opt,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = TextPrimary
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedBank = opt
                                        bankExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
  
                // Account Number Input
                OutlinedTextField(
                    value = accountNo,
                    onValueChange = { accountNo = it },
                    label = { Text("Số tài khoản / Ký hiệu ví (Không bắt buộc)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )
  
                // Dynamic inputs based on Account Type
                if (accountType == "THUONG") {
                    OutlinedTextField(
                        value = balanceValue,
                        onValueChange = { input ->
                            balanceValue = formatTextFieldValue(input, allowNegative = true)
                        },
                        label = { Text("Số dư thực tế hiện tại (VND)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )
                } else {
                    OutlinedTextField(
                        value = creditLimitValue,
                        onValueChange = { input ->
                            creditLimitValue = formatTextFieldValue(input, allowNegative = false)
                        },
                        label = { Text("Hạn mức tín dụng / Thẻ (VND)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = creditSpentValue,
                        onValueChange = { input ->
                            creditSpentValue = formatTextFieldValue(input, allowNegative = false)
                        },
                        label = { Text("Số tiền đã chi tiêu (VND)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )
                }
  
                Spacer(modifier = Modifier.height(8.dp))

                // Selector for display logo
                BankLogoSelector(
                    selectedLogo = selectedLogoName,
                    onLogoSelected = { selectedLogoName = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("HUỶ BỎ", color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val finalAccNo = accountNo.ifBlank { "N/A" }
                            val finalBankName = if (isCustomName) {
                                if (customBankName.isNotBlank()) customBankName else "Ví tự tạo"
                            } else {
                                selectedBank
                            }
                            val limit = if (accountType == "TIN_DUNG") parseInputThousands(creditLimitValue.text) else 0.0
                            val spent = if (accountType == "TIN_DUNG") parseInputThousands(creditSpentValue.text) else 0.0
                            val finalBalance = if (accountType == "TIN_DUNG") (limit - spent) else parseInputThousands(balanceValue.text)

                            // Save custom logo brand mapping for this new wallet
                            val cleanAccCheck = finalAccNo.trim().replace(" ", "")
                            context.getSharedPreferences("vinaspends_prefs", android.content.Context.MODE_PRIVATE)
                                .edit()
                                .putString("wallet_logo_${finalBankName}", selectedLogoName)
                                .putString("wallet_logo_${finalBankName}_${cleanAccCheck}", selectedLogoName)
                                .apply()

                            onConfirm(
                                finalBankName,
                                finalAccNo,
                                finalBalance,
                                accountType,
                                limit,
                                spent
                            )
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
                    ) {
                        Text("KHỞI TẠO NGAY", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Map categories to visual Material symbols with customized rich icons
fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Ăn uống" -> Icons.Outlined.Coffee
        "Di chuyển" -> Icons.Outlined.TwoWheeler
        "Mua sắm" -> Icons.Outlined.ShoppingBag
        "Giải trí" -> Icons.Outlined.Gamepad
        "Lương" -> Icons.Outlined.Payments
        "Nhà cửa" -> Icons.Outlined.Cottage
        "Sức khỏe" -> Icons.Outlined.SelfImprovement
        "Học tập" -> Icons.Outlined.AutoStories
        "Hóa đơn" -> Icons.Outlined.HistoryEdu
        "Đầu tư" -> Icons.Outlined.QueryStats
        "Tiết kiệm" -> Icons.Outlined.Savings
        "Du lịch" -> Icons.Outlined.Explore
        "Làm đẹp" -> Icons.Outlined.Spa
        "Quà tặng" -> Icons.Outlined.Cake
        "Gia đình" -> Icons.Outlined.Groups
        "Thú cưng" -> Icons.Outlined.Pets
        "Mua xe" -> Icons.Outlined.DirectionsCar
        "Bảo hiểm" -> Icons.Outlined.Shield
        "Thuế" -> Icons.Outlined.Description
        "Thể thao" -> Icons.Outlined.SportsSoccer
        "Từ thiện" -> Icons.Outlined.VolunteerActivism
        "Thiết bị số" -> Icons.Outlined.Devices
        "Sửa chữa" -> Icons.Outlined.Build
        "Ăn vặt" -> Icons.Outlined.BakeryDining
        "Khác" -> Icons.Outlined.AutoAwesome
        else -> Icons.Outlined.AutoAwesome
    }
}

// Map categories to visual color schemes with high-contrast, beautiful vintage tones
fun getCategoryColor(category: String): Color {
    return when (category) {
        "Ăn uống" -> Color(0xFFC76C3C)        // Rich Terracotta Orange
        "Di chuyển" -> Color(0xFF1E6C7D)       // Broad Sea Blue
        "Mua sắm" -> Color(0xFFB54E69)         // Rosewood Pink
        "Giải trí" -> Color(0xFF8651A3)        // Deep Purple Indigo
        "Lương" -> Color(0xFF1A7332)           // Forest Emerald Green
        "Nhà cửa" -> Color(0xFF8E5C43)         // Autumn Brown Timber
        "Sức khỏe" -> Color(0xFFBF3641)        // Crimson Coral
        "Học tập" -> Color(0xFF1F519C)         // Deep Cobalt Blue
        "Hóa đơn" -> Color(0xFF5D6D7E)         // Slate Steel Gray
        "Đầu tư" -> Color(0xFF167B6E)          // Deep Teal
        "Tiết kiệm" -> Color(0xFF6F8F1E)       // Vibrant Leaf Yellow-Green
        "Du lịch" -> Color(0xFF14858F)         // Clear Deep Turquoise
        "Làm đẹp" -> Color(0xFFAD3B82)         // Rich Orchid Velvet Magenta
        "Quà tặng" -> Color(0xFFB06F23)         // Warm Honey Amber
        "Gia đình" -> Color(0xFF18648C)         // Marine Blue
        "Thú cưng" -> Color(0xFF9E5630)         // Brick Rust
        "Mua xe" -> Color(0xFF1E5D61)          // Dark Jade
        "Bảo hiểm" -> Color(0xFF2C6B4E)         // Evergreen Shield
        "Thuế" -> Color(0xFF705234)            // Muted Clay Copper
        "Thể thao" -> Color(0xFF537A1B)         // Sportive Grass Green
        "Từ thiện" -> Color(0xFFBC355B)         // Deep Carmine Lotus
        "Thiết bị số" -> Color(0xFF1E5175)       // Midnight Prussian Blue
        "Sửa chữa" -> Color(0xFF91511A)         // Warm Clay
        "Ăn vặt" -> Color(0xFFA63528)          // Roasted Red Chili
        "Khác" -> Color(0xFF515A5A)            // Muted Charcoal Slate
        else -> {
            // Generate a consistent, beautiful color for custom dynamic categories based on their hashcode
            val hash = kotlin.math.abs(category.hashCode())
            val hue = (hash % 360).toFloat()
            // High-readability saturation (0.65f) and solid luminosity (0.75f)
            Color.hsv(hue, 0.65f, 0.75f)
        }
    }
}

// Map bank names to beautiful branding colors and abbreviations
fun getBankInfo(bankName: String): Pair<Color, String> {
    val norm = bankName.lowercase().replace(" ", "")
    return when {
        norm.contains("vietcombank") || norm.contains("vcb") -> Pair(Color(0xFF2E7D32), "VCB")     // Green
        norm.contains("techcombank") || norm.contains("tcb") -> Pair(Color(0xFFD32F2F), "TCB")     // Red
        norm.contains("mbbank") || norm.contains("mbb") || (norm.contains("mb") && !norm.contains("momo")) -> Pair(Color(0xFF1565C0), "MBB")         // Blue
        norm.contains("tpbank") || norm.contains("tpb") -> Pair(Color(0xFF7B1FA2), "TPB")          // Purple
        norm.contains("vpbank") || norm.contains("vpb") -> Pair(Color(0xFF00833F), "VPB")          // Jade Green
        norm.contains("agribank") || norm.contains("agr") -> Pair(Color(0xFF8B1E0F), "AGR")        // Dark red/brown
        norm.contains("bidv") -> Pair(Color(0xFF007A87), "BIDV")           // Teal/Cyan
        norm.contains("vietinbank") || norm.contains("ctg") || norm.contains("icb") -> Pair(Color(0xFF0072BC), "VTB")      // Ocean blue
        norm.contains("sacombank") || norm.contains("stb") -> Pair(Color(0xFF005691), "SCB")       // Royal blue
        norm.contains("acb") -> Pair(Color(0xFF0072C6), "ACB")             // Bright blue
        norm.contains("vib") -> Pair(Color(0xFFF05A28), "VIB")             // Orange
        norm.contains("shinhan") -> Pair(Color(0xFF002C77), "SHB")    // Deep Samsung blue
        norm.contains("momo") -> Pair(Color(0xFFC2185B), "MOMO")        // Pink
        norm.contains("zalopay") || norm.contains("zalo") -> Pair(Color(0xFF0084FF), "ZALO")        // Sky blue
        norm.contains("shopeepay") || norm.contains("spp") -> Pair(Color(0xFFEE4D2D), "SPP")    // ShopeePay Orange
        norm.contains("viettelmoney") || norm.contains("vtm") -> Pair(Color(0xFFDD2C00), "VTM")   // Bright orange-red
        norm.contains("tiềnmặt") || norm.contains("tienmat") || norm.contains("cash") -> Pair(Color(0xFF455A64), "TIỀN")       // Slate grey
        else -> Pair(Color(0xFF37474F), bankName.take(4).uppercase()) // Default
    }
}

// Format precise last updated date & time of the wallet balance
fun formatExactSyncedTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss dd/MM", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun getBankLogoUrl(bankName: String): String? {
    val norm = bankName.lowercase().replace(" ", "")
    return when {
        norm.contains("vietcombank") || norm.contains("vcb") -> "https://api.vietqr.io/img/VCB.png"
        norm.contains("techcombank") || norm.contains("tcb") -> "https://api.vietqr.io/img/TCB.png"
        norm.contains("mbbank") || norm.contains("mbb") || (norm.contains("mb") && !norm.contains("momo")) -> "https://api.vietqr.io/img/MB.png"
        norm.contains("tpbank") || norm.contains("tpb") -> "https://api.vietqr.io/img/TPB.png"
        norm.contains("vpbank") || norm.contains("vpb") -> "https://api.vietqr.io/img/VPB.png"
        norm.contains("agribank") || norm.contains("agr") -> "https://api.vietqr.io/img/AGR.png"
        norm.contains("bidv") -> "https://api.vietqr.io/img/BIDV.png"
        norm.contains("vietinbank") || norm.contains("ctg") || norm.contains("icb") -> "https://api.vietqr.io/img/ICB.png"
        norm.contains("sacombank") || norm.contains("stb") -> "https://api.vietqr.io/img/STB.png"
        norm.contains("acb") -> "https://api.vietqr.io/img/ACB.png"
        norm.contains("vib") -> "https://api.vietqr.io/img/VIB.png"
        norm.contains("shinhan") -> "https://api.vietqr.io/img/SHB.png"
        norm.contains("momo") -> "https://cdn.haitrieu.com/wp-content/uploads/2022/10/Logo-MoMo-Square.png"
        norm.contains("zalopay") || norm.contains("zalo") -> "https://cdn.haitrieu.com/wp-content/uploads/2022/10/Logo-ZaloPay-Square.png"
        norm.contains("shopeepay") || norm.contains("spp") -> "https://cdn.haitrieu.com/wp-content/uploads/2022/10/Logo-ShopeePay-Orange.png"
        norm.contains("viettelmoney") || norm.contains("vtm") -> "https://cdn.haitrieu.com/wp-content/uploads/2022/10/Logo-Viettel-Money.png"
        else -> null
    }
}

@Composable
fun BankLogo(bankName: String, modifier: Modifier = Modifier, bankId: Int? = null) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("vinaspends_prefs", android.content.Context.MODE_PRIVATE) }
    
    val mappedBankName = remember(bankName, bankId) {
        if (bankId != null) {
            prefs.getString("wallet_logo_$bankId", bankName) ?: bankName
        } else {
            // Check formatted details search
            val cleanName = bankName.trim()
            prefs.getString("wallet_logo_${cleanName}", bankName) ?: bankName
        }
    }

    val logoUrl = getBankLogoUrl(mappedBankName)
    val (bColor, bMini) = getBankInfo(mappedBankName)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White)
            .border(0.5.dp, Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (mappedBankName == "Tiền mặt") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Payments,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.fillMaxSize(0.6f)
                )
            }
        } else {
            androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val calculatedFontSize = (maxWidth.value * 0.35f).sp
                
                if (logoUrl != null) {
                    SubcomposeAsyncImage(
                        model = logoUrl,
                        contentDescription = mappedBankName,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(1.5.dp),
                        contentScale = ContentScale.Fit,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(bColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = bMini,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = calculatedFontSize,
                                    textAlign = TextAlign.Center
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(bColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = bMini,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = calculatedFontSize,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(bColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = bMini,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = calculatedFontSize,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

fun getIconFromName(name: String): ImageVector {
    return when (name) {
        "Coffee" -> Icons.Outlined.Coffee
        "TwoWheeler" -> Icons.Outlined.TwoWheeler
        "ShoppingBag" -> Icons.Outlined.ShoppingBag
        "Gamepad" -> Icons.Outlined.Gamepad
        "Payments" -> Icons.Outlined.Payments
        "Cottage" -> Icons.Outlined.Cottage
        "SelfImprovement" -> Icons.Outlined.SelfImprovement
        "AutoStories" -> Icons.Outlined.AutoStories
        "HistoryEdu" -> Icons.Outlined.HistoryEdu
        "QueryStats" -> Icons.Outlined.QueryStats
        "Savings" -> Icons.Outlined.Savings
        "Explore" -> Icons.Outlined.Explore
        "Spa" -> Icons.Outlined.Spa
        "Cake" -> Icons.Outlined.Cake
        "AutoAwesome" -> Icons.Outlined.AutoAwesome
        "Pets" -> Icons.Outlined.Pets
        "Groups" -> Icons.Outlined.Groups
        "DirectionsCar" -> Icons.Outlined.DirectionsCar
        "Shield" -> Icons.Outlined.Shield
        "Description" -> Icons.Outlined.Description
        "SportsSoccer" -> Icons.Outlined.SportsSoccer
        "VolunteerActivism" -> Icons.Outlined.VolunteerActivism
        "Devices" -> Icons.Outlined.Devices
        "Build" -> Icons.Outlined.Build
        "BakeryDining" -> Icons.Outlined.BakeryDining
        
        // Premium Pack Additions
        "Restaurant" -> Icons.Outlined.Restaurant
        "LocalDrink" -> Icons.Outlined.LocalDrink
        "LocalBar" -> Icons.Outlined.LocalBar
        "Icecream" -> Icons.Outlined.Icecream
        "LocalPizza" -> Icons.Outlined.LocalPizza
        "Fastfood" -> Icons.Outlined.Fastfood
        "ShoppingCart" -> Icons.Outlined.ShoppingCart
        "Storefront" -> Icons.Outlined.Storefront
        "DirectionsBus" -> Icons.Outlined.DirectionsBus
        "DirectionsTransit" -> Icons.Outlined.DirectionsTransit
        "FlashOn" -> Icons.Outlined.FlashOn
        "WaterDrop" -> Icons.Outlined.WaterDrop
        "Wifi" -> Icons.Outlined.Wifi
        "Call" -> Icons.Outlined.Call
        "LocalShipping" -> Icons.Outlined.LocalShipping
        "MusicNote" -> Icons.Outlined.MusicNote
        "Movie" -> Icons.Outlined.Movie
        "TheaterComedy" -> Icons.Outlined.TheaterComedy
        "CameraAlt" -> Icons.Outlined.CameraAlt
        "FitnessCenter" -> Icons.Outlined.FitnessCenter
        "LocalPharmacy" -> Icons.Outlined.LocalPharmacy
        "Favorite" -> Icons.Outlined.Favorite
        "MonetizationOn" -> Icons.Outlined.MonetizationOn
        "CreditCard" -> Icons.Outlined.CreditCard
        "School" -> Icons.Outlined.School
        "LaptopMac" -> Icons.Outlined.LaptopMac
        "Class" -> Icons.Outlined.Class
        "Celebration" -> Icons.Outlined.Celebration
        "ChildCare" -> Icons.Outlined.ChildCare
        "ChildFriendly" -> Icons.Outlined.ChildFriendly
        "WbSunny" -> Icons.Outlined.WbSunny
        "Nature" -> Icons.Outlined.Nature
        "LocalOffer" -> Icons.Outlined.LocalOffer
        "PushPin" -> Icons.Outlined.PushPin
        "Map" -> Icons.Outlined.Map

        // Backward compatibility mappings
        "LocalActivity" -> Icons.Outlined.Gamepad
        "Home" -> Icons.Outlined.Cottage
        "FavoriteBorder" -> Icons.Outlined.Favorite
        "ReceiptLong" -> Icons.Outlined.HistoryEdu
        "TrendingUp" -> Icons.Outlined.QueryStats
        "Flight" -> Icons.Outlined.Explore
        "Brush" -> Icons.Outlined.Spa
        "CardGiftcard" -> Icons.Outlined.Cake
        "Category" -> Icons.Outlined.AutoAwesome
        "LocalMall" -> Icons.Outlined.ShoppingBag
        "SportsEsports" -> Icons.Outlined.Gamepad
        "MedicalServices" -> Icons.Outlined.LocalPharmacy
        "LocalGasStation" -> Icons.Outlined.TwoWheeler
        "Work" -> Icons.Outlined.HistoryEdu
        "Handshake" -> Icons.Outlined.Payments
        "PhoneAndroid" -> Icons.Outlined.Devices
        "Tv" -> Icons.Outlined.Tv
        "Group" -> Icons.Outlined.Groups
        "Event" -> Icons.Outlined.Celebration
        else -> Icons.Outlined.AutoAwesome
    }
}

@Composable
fun rememberCategoryIcon(category: String): ImageVector {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("vinaspends_prefs", android.content.Context.MODE_PRIVATE) }
    var currentIconName by remember(category) { mutableStateOf(prefs.getString("category_icon_$category", null)) }
    
    DisposableEffect(category) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "category_icon_$category") {
                currentIconName = prefs.getString("category_icon_$category", null)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
    
    return if (currentIconName != null) getIconFromName(currentIconName!!) else getCategoryIcon(category)
}

@Composable
fun rememberCategoryColor(category: String): Color {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("vinaspends_prefs", android.content.Context.MODE_PRIVATE) }
    var currentColorHex by remember(category) { mutableStateOf(prefs.getString("category_color_$category", null)) }
    
    DisposableEffect(category) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "category_color_$category") {
                currentColorHex = prefs.getString("category_color_$category", null)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
    
    return if (currentColorHex != null) {
        try {
            Color(android.graphics.Color.parseColor(currentColorHex))
        } catch (e: Exception) {
            getCategoryColor(category)
        }
    } else {
        getCategoryColor(category)
    }
}

@Composable
fun EditCategoryDialog(
    category: String,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("vinaspends_prefs", android.content.Context.MODE_PRIVATE) }
    
    val initialIcon = remember(category) { prefs.getString("category_icon_$category", null) ?: "" }
    val initialColor = remember(category) { prefs.getString("category_color_$category", null) ?: "" }
    
    var selectedIconName by remember { mutableStateOf(initialIcon) }
    var selectedColorHex by remember { mutableStateOf(initialColor) }
    
    val categoryIconOptions = listOf(
        // Eating & Drinking
        "Coffee" to Icons.Outlined.Coffee,
        "Restaurant" to Icons.Outlined.Restaurant,
        "LocalDrink" to Icons.Outlined.LocalDrink,
        "LocalBar" to Icons.Outlined.LocalBar,
        "BakeryDining" to Icons.Outlined.BakeryDining,
        "Icecream" to Icons.Outlined.Icecream,
        "LocalPizza" to Icons.Outlined.LocalPizza,
        "Fastfood" to Icons.Outlined.Fastfood,
        
        // Shopping & Store
        "ShoppingBag" to Icons.Outlined.ShoppingBag,
        "ShoppingCart" to Icons.Outlined.ShoppingCart,
        "Storefront" to Icons.Outlined.Storefront,
        
        // Transports & Vehicles
        "TwoWheeler" to Icons.Outlined.TwoWheeler,
        "DirectionsCar" to Icons.Outlined.DirectionsCar,
        "DirectionsBus" to Icons.Outlined.DirectionsBus,
        "DirectionsTransit" to Icons.Outlined.DirectionsTransit,
        "Explore" to Icons.Outlined.Explore,
        
        // Home, Bills, Utilities
        "Cottage" to Icons.Outlined.Cottage,
        "HistoryEdu" to Icons.Outlined.HistoryEdu,
        "Build" to Icons.Outlined.Build,
        "FlashOn" to Icons.Outlined.FlashOn,
        "WaterDrop" to Icons.Outlined.WaterDrop,
        "Wifi" to Icons.Outlined.Wifi,
        "Call" to Icons.Outlined.Call,
        "LocalShipping" to Icons.Outlined.LocalShipping,
        
        // Entertainment & Hobby
        "Gamepad" to Icons.Outlined.Gamepad,
        "MusicNote" to Icons.Outlined.MusicNote,
        "Movie" to Icons.Outlined.Movie,
        "TheaterComedy" to Icons.Outlined.TheaterComedy,
        "CameraAlt" to Icons.Outlined.CameraAlt,
        
        // Sports & Wellness
        "SelfImprovement" to Icons.Outlined.SelfImprovement,
        "Spa" to Icons.Outlined.Spa,
        "FitnessCenter" to Icons.Outlined.FitnessCenter,
        "SportsSoccer" to Icons.Outlined.SportsSoccer,
        
        // Health
        "LocalPharmacy" to Icons.Outlined.LocalPharmacy,
        "Favorite" to Icons.Outlined.Favorite,
        
        // Wealth & Work
        "Payments" to Icons.Outlined.Payments,
        "Savings" to Icons.Outlined.Savings,
        "QueryStats" to Icons.Outlined.QueryStats,
        "MonetizationOn" to Icons.Outlined.MonetizationOn,
        "CreditCard" to Icons.Outlined.CreditCard,
        
        // Education & Info
        "AutoStories" to Icons.Outlined.AutoStories,
        "School" to Icons.Outlined.School,
        "LaptopMac" to Icons.Outlined.LaptopMac,
        "Class" to Icons.Outlined.Class,
        "Description" to Icons.Outlined.Description,
        
        // People & Celebration
        "Groups" to Icons.Outlined.Groups,
        "Cake" to Icons.Outlined.Cake,
        "VolunteerActivism" to Icons.Outlined.VolunteerActivism,
        "Celebration" to Icons.Outlined.Celebration,
        "ChildCare" to Icons.Outlined.ChildCare,
        "ChildFriendly" to Icons.Outlined.ChildFriendly,
        
        // Nature & Decor
        "Pets" to Icons.Outlined.Pets,
        "WbSunny" to Icons.Outlined.WbSunny,
        "Nature" to Icons.Outlined.Nature,
        
        // Other
        "AutoAwesome" to Icons.Outlined.AutoAwesome,
        "Shield" to Icons.Outlined.Shield,
        "Devices" to Icons.Outlined.Devices,
        "LocalOffer" to Icons.Outlined.LocalOffer,
        "PushPin" to Icons.Outlined.PushPin,
        "Map" to Icons.Outlined.Map
    )
    
    val categoryColorOptions = listOf(
        Color(0xFFFB8C00), Color(0xFF03A9F4), Color(0xFFEC407A), Color(0xFFAB47BC),
        Color(0xFF4CAF50), Color(0xFF8D6E63), Color(0xFFEF5350), Color(0xFF5C6BC0),
        Color(0xFF78909C), Color(0xFF26A69A), Color(0xFF9CCC65), Color(0xFF26C6DA),
        Color(0xFFFF7043), Color(0xFFE91E63), Color(0xFF9E9E9E)
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            var categoryName by remember { mutableStateOf(category) }

            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = EmeraldPrimary)
                    Text(
                        text = "TÙY CHỈNH HẠNG MỤC",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    )
                }
                
                Text(
                    text = "Thay đổi tên, biểu tượng và màu sắc cho mục này:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Tên hạng mục", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedLabelColor = EmeraldPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Divider(color = Color.LightGray.copy(alpha = 0.2f))
                
                // Icon Picker Grid
                Text(
                    text = "Chọn Biểu Tượng (Icon)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekDeepPurple
                )
                
                Box(modifier = Modifier.height(200.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(categoryIconOptions) { (name, icon) ->
                            val isSelected = selectedIconName == name
                            val previewColor = if (selectedColorHex.isNotEmpty()) {
                                try { Color(android.graphics.Color.parseColor(selectedColorHex)) } catch (e: Exception) { EmeraldPrimary }
                            } else {
                                EmeraldPrimary
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) previewColor.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) previewColor else Color.LightGray.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedIconName = name },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = name,
                                    tint = if (isSelected) previewColor else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Color Picker Row
                Text(
                    text = "Chọn Màu Sắc",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekDeepPurple
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categoryColorOptions) { color ->
                        val hexStr = String.format("#%06X", 0xFFFFFF and color.toArgb())
                        val isSelected = selectedColorHex == hexStr
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hexStr },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.LightGray.copy(alpha = 0.2f))
                
                val parentMap by viewModel.categoryParentMap.collectAsStateWithLifecycle()
                var isSubcategory by remember(category, parentMap) { mutableStateOf(parentMap.containsKey(category)) }
                var selectedParentCategory by remember(category, parentMap) { mutableStateOf(parentMap[category] ?: "") }
                var parentDropdownExpanded by remember { mutableStateOf(false) }

                Text(
                    text = "Phân Cấp Hạng Mục",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekDeepPurple
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Option 1: Standalone
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isSubcategory = false }
                    ) {
                        RadioButton(
                            selected = !isSubcategory,
                            onClick = { isSubcategory = false },
                            colors = RadioButtonDefaults.colors(selectedColor = SleekDeepPurple)
                        )
                        Text(
                            text = "Hạng mục lớn",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    
                    // Option 2: Subcategory
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isSubcategory = true }
                    ) {
                        RadioButton(
                            selected = isSubcategory,
                            onClick = { isSubcategory = true },
                            colors = RadioButtonDefaults.colors(selectedColor = SleekDeepPurple)
                        )
                        Text(
                            text = "Hạng mục con",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
                
                if (isSubcategory) {
                    val activeCategories = viewModel.allCategories.collectAsStateWithLifecycle().value
                    val eligibleParents = remember(category, activeCategories, parentMap) {
                        activeCategories.filter { it != category && !parentMap.containsKey(it) }
                    }
                    
                    if (selectedParentCategory.isEmpty() && eligibleParents.isNotEmpty()) {
                        selectedParentCategory = eligibleParents.first()
                    }
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedParentCategory,
                            onValueChange = {},
                            label = { Text("Thuộc hạng mục lớn nào?", fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { parentDropdownExpanded = true },
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = TextPrimary,
                                disabledBorderColor = TextSecondary.copy(alpha = 0.5f),
                                disabledLabelColor = TextSecondary
                            ),
                            trailingIcon = {
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        )
                        DropdownMenu(
                            expanded = parentDropdownExpanded,
                            onDismissRequest = { parentDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.6f).heightIn(max = 240.dp)
                        ) {
                            eligibleParents.forEach { parentCat ->
                                val parentColor = rememberCategoryColor(parentCat)
                                val parentIcon = rememberCategoryIcon(parentCat)
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(parentColor.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = parentIcon,
                                                    contentDescription = null,
                                                    tint = parentColor,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                            Text(parentCat, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        }
                                    },
                                    onClick = {
                                        selectedParentCategory = parentCat
                                        parentDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // CTA Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Reset Button
                    OutlinedButton(
                        onClick = {
                            prefs.edit()
                                .remove("category_icon_$category")
                                .remove("category_color_$category")
                                .apply()
                            viewModel.setCategoryParent(category, null)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                    ) {
                        Text("Khôi Phục", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    // Save Button
                    Button(
                        onClick = {
                            val finalCategoryName = categoryName.trim()
                            if (finalCategoryName.isEmpty()) {
                                Toast.makeText(context, "Tên hạng mục không được để trống!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            if (finalCategoryName != category) {
                                // Rename first (this migrates transactions & existing pref values)
                                viewModel.renameCategory(category, finalCategoryName)
                            }
                            
                            // Save customized icon & color for the final category name
                            val editor = prefs.edit()
                            if (selectedIconName.isNotEmpty()) {
                                editor.putString("category_icon_$finalCategoryName", selectedIconName)
                            } else {
                                editor.remove("category_icon_$finalCategoryName")
                            }
                            if (selectedColorHex.isNotEmpty()) {
                                editor.putString("category_color_$finalCategoryName", selectedColorHex)
                            } else {
                                editor.remove("category_color_$finalCategoryName")
                            }
                            editor.apply()
                            
                            // Save hierarchy parent-child mapping
                            if (isSubcategory && selectedParentCategory.isNotEmpty()) {
                                viewModel.setCategoryParent(finalCategoryName, selectedParentCategory)
                            } else {
                                viewModel.setCategoryParent(finalCategoryName, null)
                            }
                            
                            Toast.makeText(context, "Đã lưu thay đổi!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Lưu Thay Đổi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val isCustom = viewModel.customCategories.collectAsStateWithLifecycle().value.contains(category)
                var showConfirmDeleteDialog by remember { mutableStateOf(false) }

                if (showConfirmDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showConfirmDeleteDialog = false },
                        title = { Text(if (isCustom) "Xóa Hạng Mục?" else "Ẩn Hạng Mục?") },
                        text = {
                            Text(
                                if (isCustom)
                                    "Bạn có chắc muốn xóa hoàn toàn hạng mục tùy chỉnh '$category' không?"
                                else
                                    "Bạn có chắc muốn ẩn hạng mục mặc định '$category' không? Hạng mục này có thể được khôi phục bất cứ lúc nào ở mục danh mục chi tiêu."
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.deleteCategory(category)
                                    showConfirmDeleteDialog = false
                                    onDismiss()
                                    Toast.makeText(context, if (isCustom) "Đã xóa hạng mục '$category'!" else "Đã ẩn hạng mục '$category'!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed, contentColor = Color.White)
                            ) {
                                Text(if (isCustom) "Xóa" else "Ẩn")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showConfirmDeleteDialog = false }) {
                                Text("Hủy")
                            }
                        }
                    )
                }

                Button(
                    onClick = { showConfirmDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed.copy(alpha = 0.1f), contentColor = ExpenseRed)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(if (isCustom) "Xóa Hạng Mục" else "Ẩn Hạng Mục", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BankLogoSelector(
    selectedLogo: String,
    onLogoSelected: (String) -> Unit
) {
    val logos = listOf(
        "Vietcombank", "Techcombank", "MB Bank", "TPBank", "VPBank", 
        "Agribank", "BIDV", "VietinBank", "Sacombank", "ACB", "VIB", "Shinhan Bank",
        "Ví MoMo", "ZaloPay", "Ví ShopeePay", "Viettel Money", "Tiền mặt"
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Chọn Logo Thương Hiệu",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SleekDeepPurple
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(logos) { logoName ->
                val isSelected = selectedLogo == logoName
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) SleekLightLavender else Color.Transparent)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) SleekDeepPurple else Color.LightGray.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onLogoSelected(logoName) },
                    contentAlignment = Alignment.Center
                ) {
                    BankLogo(bankName = logoName, modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@Composable
fun ThemeSelectionDialog(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    val currentTheme by viewModel.themeChoice.collectAsStateWithLifecycle()
    val currentMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = SleekDeepPurple
                )
                Text(
                    text = "Giao Diện & Chủ Đề",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SECTION 1: DISPLAY MODE
                Text(
                    text = "Chế độ hiển thị",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SleekTextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf(
                        Triple("light", "Sáng", Icons.Default.LightMode),
                        Triple("dark", "Tối", Icons.Default.DarkMode),
                        Triple("system", "Hệ thống", Icons.Default.Settings)
                    )

                    modes.forEach { (mode, label, icon) ->
                        val isSelected = currentMode == mode
                        Button(
                            onClick = { viewModel.setThemeMode(mode) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) SleekDeepPurple else SleekLightLavender,
                                contentColor = if (isSelected) Color.White else SleekTextPrimary
                            ),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(SleekBorder.copy(alpha = 0.3f))
                )

                // SECTION 2: COLOR SCHEME
                Text(
                    text = "Chủ đề màu sắc",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SleekTextPrimary
                )

                val themes = listOf(
                    Triple("ocean", "Đại dương Cổ điển", listOf(Color(0xFF1B4965), Color(0xFFB75B53), Color(0xFFEAF3F9))),
                    Triple("emerald", "Lục bảo Ngọc", listOf(Color(0xFF1B5E20), Color(0xFFD84315), Color(0xFFE8F5E9))),
                    Triple("terracotta", "Đất nung Ấm áp", listOf(Color(0xFFA54B1A), Color(0xFF1D5C5A), Color(0xFFFFF8F2))),
                    Triple("purple", "Thạch anh Hoàng gia", listOf(Color(0xFF4A148C), Color(0xFF00796B), Color(0xFFFBF4FC))),
                    Triple("minimalist", "Tối giản Đen Trắng", listOf(Color(0xFF111111), Color(0xFF444444), Color(0xFFFAF9F8)))
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    themes.forEach { (themeId, label, colors) ->
                        val isSelected = currentTheme.lowercase() == themeId
                        Surface(
                            onClick = { viewModel.setThemeChoice(themeId) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) SleekLightLavender else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) SleekDeepPurple else SleekBorder.copy(alpha = 0.3f)
                            ),
                            shadowElevation = if (isSelected) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) SleekDeepPurple else SleekTextPrimary
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        colors.forEach { color ->
                                            Box(
                                                modifier = Modifier
                                                    .size(width = 24.dp, height = 8.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(color)
                                            )
                                        }
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Đã chọn",
                                        tint = SleekDeepPurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SleekDeepPurple)
            ) {
                Text("Đóng", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun DebtsSubTabContent(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val allDebts by viewModel.allDebts.collectAsStateWithLifecycle()
    var showAddDebtDialog by remember { mutableStateOf(false) }

    val unpaidDebts = remember(allDebts) { allDebts.filter { !it.isPaid } }
    val paidDebts = remember(allDebts) { allDebts.filter { it.isPaid } }

    val totalBorrow = remember(unpaidDebts) { unpaidDebts.filter { it.type == "VAY" }.sumOf { it.amount } }
    val totalLend = remember(unpaidDebts) { unpaidDebts.filter { it.type == "CHO_VAY" }.sumOf { it.amount } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Debt & Loan Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Borrow summary card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = ExpenseRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Tôi đi vay (Cần trả)", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    Text(
                        text = String.format("%,.0fđ", totalBorrow),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ExpenseRed
                    )
                }
            }

            // Lend summary card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Tôi cho vay (Cần đòi)", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    Text(
                        text = String.format("%,.0fđ", totalLend),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldPrimary
                    )
                }
            }
        }

        // Add debt record row
        Button(
            onClick = { showAddDebtDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = SleekDeepPurple, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(imageVector = Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Text("TẠO MỚI KHOẢN VAY / NỢ", fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 0.5.sp)
            }
        }

        // UNPAID LIST
        Text(
            text = "KHOẢN NỢ ĐANG HOẠT ĐỘNG (${unpaidDebts.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.5.sp
        )

        if (unpaidDebts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(36.dp))
                    Text("Tuyệt vời! Không có khoản vay nợ nào chưa thanh toán.", fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center)
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                unpaidDebts.forEach { debt ->
                    DebtRowItem(debt = debt, viewModel = viewModel)
                }
            }
        }

        // PAID LIST
        if (paidDebts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "LỊCH SỬ ĐÃ THANH TOÁN (${paidDebts.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                paidDebts.forEach { debt ->
                    DebtRowItem(debt = debt, viewModel = viewModel)
                }
            }
        }
    }

    if (showAddDebtDialog) {
        AddDebtDialog(
            onDismiss = { showAddDebtDialog = false },
            onConfirm = { title, amount, type, note, dueDate, enableReminder ->
                viewModel.addDebt(title, amount, type, note, dueDate, enableReminder)
                showAddDebtDialog = false
                Toast.makeText(context, "Đã thêm khoản ghi chép vay nợ cá nhân mới!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun DebtRowItem(
    debt: com.example.data.model.DebtEntity,
    viewModel: FinanceViewModel
) {
    val context = LocalContext.current
    val colorAccent = if (debt.type == "VAY") ExpenseRed else EmeraldPrimary
    val isOverdue = !debt.isPaid && debt.dueDate > 0 && debt.dueDate < System.currentTimeMillis()
    val isDueToday = !debt.isPaid && debt.dueDate > 0 && android.text.format.DateUtils.isToday(debt.dueDate)

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (debt.isPaid) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) 
                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        ),
        border = if (isOverdue) BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.5f)) 
                 else if (isDueToday) BorderStroke(1.dp, Color(0xFFFBC02D))
                 else null
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox/Paid action
            IconButton(
                onClick = {
                    viewModel.markDebtAsPaid(debt, !debt.isPaid)
                    val toastMsg = if (!debt.isPaid) "Đã đánh dấu hoàn thành trả nợ!" else "Đã hoàn tác trạng thái thanh toán!"
                    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(
                    imageVector = if (debt.isPaid) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Trạng thái trả nợ",
                    tint = if (debt.isPaid) EmeraldPrimary else colorAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Debt info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(colorAccent.copy(alpha = 0.15f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (debt.type == "VAY") "BÊN ĐI VAY" else "CHO VAY",
                            color = colorAccent,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    if (debt.dueDate > 0 && !debt.isPaid) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Có hẹn thông báo",
                            tint = SleekDeepPurple,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = debt.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (debt.isPaid) TextSecondary else TextPrimary,
                    style = if (debt.isPaid) androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else androidx.compose.ui.text.TextStyle.Default
                )

                if (debt.note.isNotEmpty()) {
                    Text(
                        text = debt.note,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 2
                    )
                }

                // Due date tag
                if (debt.dueDate > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val dateStr = remember(debt.dueDate) {
                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = debt.dueDate }
                        String.format("%02d/%02d/%04d", cal.get(java.util.Calendar.DAY_OF_MONTH), cal.get(java.util.Calendar.MONTH) + 1, cal.get(java.util.Calendar.YEAR))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Hạn trả: $dateStr",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOverdue) ExpenseRed else if (isDueToday) Color(0xFFF57F17) else TextSecondary
                        )

                        if (!debt.isPaid) {
                            val diffDays = remember(debt.dueDate) {
                                val diffMs = debt.dueDate - System.currentTimeMillis()
                                (diffMs / (1000 * 60 * 60 * 24)).toInt()
                            }
                            val countdownText = if (isOverdue) {
                                "Quá hạn ${-diffDays} ngày"
                            } else if (isDueToday) {
                                "Đến hạn hôm nay!"
                            } else {
                                "Còn ${diffDays + 1} ngày"
                            }

                            Text(
                                text = "($countdownText)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOverdue) ExpenseRed else if (isDueToday) Color(0xFFF57F17) else SleekDeepPurple
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount & Delete Button
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%,.0fđ", debt.amount),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = if (debt.isPaid) TextSecondary else colorAccent
                )

                IconButton(
                    onClick = {
                        viewModel.deleteDebt(debt)
                        Toast.makeText(context, "Đã xoá vĩnh viễn ghi chép nợ!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Xoá khoản nợ",
                        tint = TextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, String, Long, Boolean) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isBorrow by remember { mutableStateOf(true) }
    var enableReminder by remember { mutableStateOf(true) }

    var dueDateMs by remember { mutableStateOf(0L) }
    val calendar = remember { java.util.Calendar.getInstance() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "THÊM KHOẢN VAY / NỢ MỚI",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = SleekDeepPurple
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isBorrow) ExpenseRed else Color.Transparent)
                            .clickable { isBorrow = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "TÔI ĐI VAY (CẦN TRẢ)",
                            color = if (isBorrow) Color.White else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!isBorrow) EmeraldPrimary else Color.Transparent)
                            .clickable { isBorrow = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "TÔI CHO VAY (CẦN ĐÒI)",
                            color = if (!isBorrow) Color.Black else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tên người giao dịch / Nội dung", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekDeepPurple,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = formatTextFieldValue(TextFieldValue(it)).text },
                    label = { Text("Số tiền vay nợ", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekDeepPurple,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú chi tiết", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekDeepPurple,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f)
                    ),
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4
                )

                val dateLabel = if (dueDateMs == 0L) {
                    "Chưa chọn ngày hẹn thanh toán"
                } else {
                    val cal = java.util.Calendar.getInstance().apply { timeInMillis = dueDateMs }
                    String.format("%02d/%02d/%04d", cal.get(java.util.Calendar.DAY_OF_MONTH), cal.get(java.util.Calendar.MONTH) + 1, cal.get(java.util.Calendar.YEAR))
                }

                OutlinedTextField(
                    value = dateLabel,
                    onValueChange = {},
                    label = { Text("Hạn thanh toán", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val year = calendar.get(java.util.Calendar.YEAR)
                            val month = calendar.get(java.util.Calendar.MONTH)
                            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                            android.app.DatePickerDialog(context, { _, y, m, d ->
                                val selectedCal = java.util.Calendar.getInstance().apply {
                                    set(java.util.Calendar.YEAR, y)
                                    set(java.util.Calendar.MONTH, m)
                                    set(java.util.Calendar.DAY_OF_MONTH, d)
                                    set(java.util.Calendar.HOUR_OF_DAY, 9)
                                    set(java.util.Calendar.MINUTE, 0)
                                    set(java.util.Calendar.SECOND, 0)
                                }
                                dueDateMs = selectedCal.timeInMillis
                            }, year, month, day).show()
                        },
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = TextPrimary,
                        disabledBorderColor = TextSecondary.copy(alpha = 0.5f),
                        disabledLabelColor = TextSecondary
                    ),
                    trailingIcon = {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { enableReminder = !enableReminder }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = enableReminder,
                        onCheckedChange = { enableReminder = it },
                        colors = CheckboxDefaults.colors(checkedColor = SleekDeepPurple)
                    )
                    Column {
                        Text("Thông báo nhắc nhở nợ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                        Text("Tự động gửi thông báo hệ thống khi đến ngày hẹn", fontSize = 10.sp, color = TextSecondary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = parseInputThousands(amountText)
                    if (title.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập tên người giao dịch!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (amt <= 0) {
                        Toast.makeText(context, "Vui lòng nhập số tiền hợp lệ!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (dueDateMs == 0L) {
                        Toast.makeText(context, "Vui lòng chọn hạn thanh toán nợ!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onConfirm(
                        title,
                        amt,
                        if (isBorrow) "VAY" else "CHO_VAY",
                        note,
                        dueDateMs,
                        enableReminder
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SleekDeepPurple, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("GHI SỔ", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("HUỶ BỎ", color = TextSecondary)
            }
        }
    )
}

