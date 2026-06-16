package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.database.PalmReading
import com.example.ui.theme.*
import com.example.ui.viewmodel.PalmViewModel
import com.example.ui.viewmodel.ScanUiState
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: PalmViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(0) }
    
    val allReadings by viewModel.allReadings.collectAsStateWithLifecycle()
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val apiKey by viewModel.apiKey.collectAsStateWithLifecycle()

    var userName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var expandedReading by remember { mutableStateOf<PalmReading?>(null) }

    // Result dialog / Full-screen display
    var showResultDialog by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("") }

    // Toast/Alert helpers
    var alertMessage by remember { mutableStateOf<String?>(null) }

    // Launcher for selecting an image from the gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    // Launcher for taking a quick photo
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            // Save bitmap to a temp file to get a Uri
            try {
                val tempFile = File.createTempFile("palm_temp_", ".jpg", context.cacheDir)
                val outStream = FileOutputStream(tempFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream)
                outStream.flush()
                outStream.close()
                selectedImageUri = Uri.fromFile(tempFile)
            } catch (e: Exception) {
                alertMessage = "Lỗi khi lưu hình ảnh từ camera máy ảnh: ${e.message}"
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "XEM CHỈ TAY PHONG THỦY",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = CharcoalText,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Kết Hợp Tri Thức Phương Đông & Trí Tuệ AI",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Light,
                            fontSize = 10.sp,
                            color = SageSecondary,
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Thành viên",
                        tint = GoldAccent,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .clickable {
                                alertMessage = "Ứng dụng giải quyết luận giải chỉ tay phong thủy Á - Âu cổ điển kết hợp mô hình trí tuệ nhân tạo Gemini.\n\nNgười tạo: TRẦN MINH TÂN\nPhiên bản: 1.0"
                            }
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CreamBg
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CreamBg,
                tonalElevation = 4.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Tự Lập Quẻ") },
                    label = { Text("Tự Lập Quẻ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CreamBg,
                        selectedTextColor = GoldAccent,
                        indicatorColor = GoldAccent,
                        unselectedIconColor = SlateGray,
                        unselectedTextColor = SlateGray
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Quét AI") },
                    label = { Text("Quét AI", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CreamBg,
                        selectedTextColor = GoldAccent,
                        indicatorColor = GoldAccent,
                        unselectedIconColor = SlateGray,
                        unselectedTextColor = SlateGray
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Thư Viện Quẻ") },
                    label = { Text("Thư Viện", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CreamBg,
                        selectedTextColor = GoldAccent,
                        indicatorColor = GoldAccent,
                        unselectedIconColor = SlateGray,
                        unselectedTextColor = SlateGray
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Cài Đặt") },
                    label = { Text("Cài Đặt", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CreamBg,
                        selectedTextColor = GoldAccent,
                        indicatorColor = GoldAccent,
                        unselectedIconColor = SlateGray,
                        unselectedTextColor = SlateGray
                    )
                )
            }
        },
        containerColor = CreamBg,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(CreamBg)
        ) {
            when (currentTab) {
                0 -> ManualCreationTab(
                    viewModel = viewModel,
                    userName = userName,
                    onNameChange = { userName = it },
                    onGenerate = {
                        viewModel.generateManualReading(userName)
                    }
                )
                1 -> AiScannerTab(
                    viewModel = viewModel,
                    userName = userName,
                    onNameChange = { userName = it },
                    selectedImageUri = selectedImageUri,
                    onPickGallery = { galleryLauncher.launch("image/*") },
                    onCaptureCamera = { cameraLauncher.launch() },
                    onResetImage = { selectedImageUri = null },
                    onAnalyze = { uri ->
                        viewModel.analyzePalmWithAi(userName, uri)
                    }
                )
                2 -> LibraryTab(
                    readings = allReadings,
                    onSelectReading = {
                        expandedReading = it
                        resultText = it.analysisResult
                        showResultDialog = true
                    },
                    onDeleteReading = { viewModel.deleteReading(it) }
                )
                3 -> SettingsTab(
                    apiKey = apiKey,
                    onKeyChange = { viewModel.updateApiKey(it) }
                )
            }

            // Handle Loading Overlay for API & local generation computations
            if (scanState is ScanUiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SandBg),
                        border = BorderStroke(1.dp, GoldAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = GoldAccent)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Đang bấm quẻ luận giải...",
                                fontFamily = FontFamily.Serif,
                                color = CharcoalText,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Vui lòng giữ kết nối camera mạng ôn hòa",
                                fontSize = 12.sp,
                                color = SlateGray
                            )
                        }
                    }
                }
            }

            // Reaction to Scan success state: open dialog
            LaunchedEffect(scanState) {
                if (scanState is ScanUiState.Success) {
                    resultText = (scanState as ScanUiState.Success).result
                    showResultDialog = true
                    viewModel.resetScanState()
                } else if (scanState is ScanUiState.Error) {
                    alertMessage = (scanState as ScanUiState.Error).message
                    viewModel.resetScanState()
                }
            }

            // Full-Screen Beautiful Result dialog (Paper/Scroll style)
            if (showResultDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showResultDialog = false
                        expandedReading = null
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showResultDialog = false
                                expandedReading = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                        ) {
                            Text("Đóng Hồi Khải", color = Color.White)
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "BÁO CÁO LUẬN GIẢI CHỈ TAY",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText,
                                fontSize = 16.sp
                            )
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Decor",
                                tint = GoldAccent
                            )
                        }
                    },
                    text = {
                        Column(modifier = Modifier.fillMaxHeight(0.75f)) {
                            HorizontalDivider(color = FineBorder, modifier = Modifier.padding(bottom = 8.dp))
                            
                            // If there was an image, display it beautifully
                            if (expandedReading?.imageUriString != null) {
                                AsyncImage(
                                    model = expandedReading?.imageUriString,
                                    contentDescription = "Bàn tay đã chụp",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .padding(bottom = 12.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(0.5.dp, FineBorder, RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (selectedImageUri != null && currentTab == 1) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Bàn tay đang xem",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .padding(bottom = 12.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(0.5.dp, FineBorder, RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(SandBg)
                                    .border(1.dp, FineBorder, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                item {
                                    // Make rich text formatting out of markdown headers gently
                                    resultText.split("\n").forEach { line ->
                                        when {
                                            line.startsWith("# ") -> {
                                                Text(
                                                    text = line.substring(2),
                                                    fontSize = 18.sp,
                                                    fontFamily = FontFamily.Serif,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GoldAccent,
                                                    modifier = Modifier.padding(vertical = 8.dp)
                                                )
                                            }
                                            line.startsWith("## ") -> {
                                                Text(
                                                    text = line.substring(3),
                                                    fontSize = 15.sp,
                                                    fontFamily = FontFamily.Serif,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SageSecondary,
                                                    modifier = Modifier.padding(vertical = 6.dp)
                                                )
                                            }
                                            line.startsWith("### ") -> {
                                                Text(
                                                    text = line.substring(4),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = CharcoalText,
                                                    modifier = Modifier.padding(vertical = 4.dp)
                                                )
                                            }
                                            line.startsWith("* ") || line.startsWith("- ") -> {
                                                Text(
                                                    text = "• " + line.substring(2),
                                                    fontSize = 12.sp,
                                                    color = CharcoalText,
                                                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                                                    lineHeight = 17.sp
                                                )
                                            }
                                            line.isNotBlank() -> {
                                                Text(
                                                    text = line,
                                                    fontSize = 12.sp,
                                                    color = CharcoalText,
                                                    modifier = Modifier.padding(bottom = 6.dp),
                                                    lineHeight = 18.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = PureWhite
                )
            }

            // Universal dialog toast alert helper
            if (alertMessage != null) {
                AlertDialog(
                    onDismissRequest = { alertMessage = null },
                    confirmButton = {
                        TextButton(onClick = { alertMessage = null }) {
                            Text("Chấp Thuận", color = GoldAccent, fontWeight = FontWeight.Bold)
                        }
                    },
                    title = {
                        Text(
                            "Thông Báo Phong Thủy",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    },
                    text = {
                        Text(alertMessage!!, fontSize = 13.sp, color = CharcoalText)
                    },
                    shape = RoundedCornerShape(12.dp),
                    containerColor = PureWhite
                )
            }
        }
    }
}

// ==================== TAB 0: OFFLINE EXPERT SCHEME GENERATOR ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualCreationTab(
    viewModel: PalmViewModel,
    userName: String,
    onNameChange: (String) -> Unit,
    onGenerate: () -> Unit
) {
    val handType by viewModel.selectedHandType.collectAsStateWithLifecycle()
    val sinhDao by viewModel.selectedSinhDao.collectAsStateWithLifecycle()
    val triDao by viewModel.selectedTriDao.collectAsStateWithLifecycle()
    val tamDao by viewModel.selectedTamDao.collectAsStateWithLifecycle()
    val soMenh by viewModel.selectedSoMenh.collectAsStateWithLifecycle()
    val thaiDuong by viewModel.selectedThaiDuong.collectAsStateWithLifecycle()
    val goVenus by viewModel.selectedGoVenus.collectAsStateWithLifecycle()
    val goJupiter by viewModel.selectedGoJupiter.collectAsStateWithLifecycle()
    val goSaturn by viewModel.selectedGoSaturn.collectAsStateWithLifecycle()
    val sign by viewModel.selectedSpecialSign.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SandBg),
                border = BorderStroke(0.5.dp, FineBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "LẬP QUẺ THỦ CÔNG",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Giải định: Nếu bạn không có sẵn ảnh chụp hoặc muốn tự lựa chọn các đặc trưng chỉ tay tìm được để nhận luận toán ngay lập tức.",
                        fontSize = 12.sp,
                        color = SlateGray,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(0.5.dp, FineBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "1. Nhân Thân Bản Mệnh",
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalText,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = userName,
                        onValueChange = onNameChange,
                        label = { Text("Họ và tên người xem", color = SlateGray, fontSize = 12.sp) },
                        placeholder = { Text("Ví dụ: Trần Minh Tân", color = SlateGray.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = FineBorder,
                            focusedTextColor = CharcoalText,
                            unfocusedTextColor = CharcoalText
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(0.5.dp, FineBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "2. Hình Dạng Bàn Tay & Nguyên Tố",
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalText,
                        fontSize = 14.sp
                    )
                    Text(
                        "Xác định dựa trên tỷ lệ ngón tay và hình dáng lòng bàn tay.",
                        fontSize = 11.sp,
                        color = SlateGray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    val handTypes = listOf("Đất (Earth) - Lòng vuông, ngón ngắn", "Nước (Water) - Lòng dài, ngón thon dài mềm", "Khí (Air) - Lòng vuông, ngón dài dẻo dai")
                    CustomSelectionGroup(
                        options = handTypes,
                        selectedOption = handType,
                        onSelected = { viewModel.selectedHandType.value = it }
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(0.5.dp, FineBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "3. Đặc Trưng Các Đường Chỉ Chính",
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalText,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Sinh Đạo
                    Text("Đường Sinh đạo (Sức khoẻ & thọ)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SageSecondary)
                    CustomSelectionGroup(
                        options = listOf("Dài và sâu (Sức khỏe tốt, thọ)", "Ngắn/mờ (Dễ mệt, sức bền kém)", "Gãy hoặc có đảo (Có biến cố, lao đao)", "Nhánh hướng xuống/lên (Du lịch, định cư)"),
                        selectedOption = sinhDao,
                        onSelected = { viewModel.selectedSinhDao.value = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Trí Đạo
                    Text("Đường Trí đạo (Tư duy & óc sáng tạo)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SageSecondary)
                    CustomSelectionGroup(
                        options = listOf("Dài, rõ (Minh mẫn, trí tuệ)", "Ngắn, mờ (Làm theo cảm tính)", "Gãy đoạn (Học vấn ngắt quãng)", "Chia hai nhánh (Sáng tạo vượt bậc)"),
                        selectedOption = triDao,
                        onSelected = { viewModel.selectedTriDao.value = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Tâm Đạo
                    Text("Đường Tâm đạo (Tình ái & giao tế)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SageSecondary)
                    CustomSelectionGroup(
                        options = listOf("Sâu, dài (Yêu thương dạt dào)", "Ngắn, mờ (Ít biểu lộ tình cảm)", "Bị đứt gãy (Nhiều trắc trở sóng gió)", "Nhiều nhánh rẽ (Quảng giao, đào hoa)"),
                        selectedOption = tamDao,
                        onSelected = { viewModel.selectedTamDao.value = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Số Mệnh
                    Text("Đường Số mệnh (Sự nghiệp công danh)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SageSecondary)
                    CustomSelectionGroup(
                        options = listOf("Rõ, sâu (Sự nghiệp vững chắc)", "Nhạt, khó nhìn (Dễ đổi nghề tự do)", "Đứt quãng (Chuyển vai trò bước ngoặt)"),
                        selectedOption = soMenh,
                        onSelected = { viewModel.selectedSoMenh.value = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Thái Dương
                    Text("Đường Thái dương (Tài hoa & may mắn)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SageSecondary)
                    CustomSelectionGroup(
                        options = listOf("Nổi rõ (Có tài hoa, dễ thành đạt)", "Mờ phẳng (Yên bình chất phác)", "Lệch xiên (Thuận lợi nghệ thuật thăng hoa)"),
                        selectedOption = thaiDuong,
                        onSelected = { viewModel.selectedThaiDuong.value = it }
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(0.5.dp, FineBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "4. Địa Thế Các Gò",
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalText,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text("Gò Kim Tinh (Venus - dưới ngón cái)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SageSecondary)
                    CustomSelectionGroup(
                        options = listOf("Đầy đặn (Nhiệt huyết, sinh lực cao)", "Phẳng mờ (Thể chất kém, trầm ngâm)"),
                        selectedOption = goVenus,
                        onSelected = { viewModel.selectedGoVenus.value = it }
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Gò Mộc Tinh (Jupiter - dưới ngón trỏ)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SageSecondary)
                    CustomSelectionGroup(
                        options = listOf("Nhô cao (Tự tin, tham vọng lớn)", "Vừa phải (Lãnh đạo khôn ngoan)", "Thấp dẹt (Ít ham muốn quyền lực)"),
                        selectedOption = goJupiter,
                        onSelected = { viewModel.selectedGoJupiter.value = it }
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Gò Thổ Tinh (Saturn - dưới ngón giữa)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SageSecondary)
                    CustomSelectionGroup(
                        options = listOf("Vừa phải (Nghiêm túc, trách nhiệm)", "Quá nhô (Buồn bã, cầu toàn lo âu)", "Trũng sâu (Dễ gặp thị phi)"),
                        selectedOption = goSaturn,
                        onSelected = { viewModel.selectedGoSaturn.value = it }
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(0.5.dp, FineBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "5. Dấu Ấn Tâm Linh Đặc Biệt",
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalText,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    CustomSelectionGroup(
                        options = listOf(
                            "Ngôi sao (Sự xuất chúng, may mắn)",
                            "Chữ thập (Trở ngại đấu tranh bản lĩnh)",
                            "Tam giác (Năng khiếu học vấn trí tuệ)",
                            "Hình vuông (Tấm khiên bảo vệ cát tinh)",
                            "Hòn đảo (Nguyệt biểu - Hao tổn năng lực tạm thời)"
                        ),
                        selectedOption = sign,
                        onSelected = { viewModel.selectedSpecialSign.value = it }
                    )
                }
            }
        }

        item {
            Button(
                onClick = onGenerate,
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirm",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "LẬP QUẺ LUẬN GIẢI NGAY",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Custom Radio-button like chip selector Group
@Composable
fun CustomSelectionGroup(
    options: List<String>,
    selectedOption: String,
    onSelected: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        options.forEach { option ->
            val isSelected = selectedOption == option
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) SandBg else PureWhite
                ),
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp_or_px() else 0.5.dp_or_px(),
                    color = if (isSelected) GoldAccent else FineBorder
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSelected(option) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelected(option) },
                        colors = RadioButtonDefaults.colors(selectedColor = GoldAccent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = option,
                        fontSize = 11.5.sp,
                        color = if (isSelected) GoldAccent else CharcoalText,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// Ext helper for border size representation
fun Double.dp_or_px() = this.dp
fun Int.dp_or_px() = this.dp

// ==================== TAB 1: AI CAMERA / GALLERY SCANNER ====================
@Composable
fun AiScannerTab(
    viewModel: PalmViewModel,
    userName: String,
    onNameChange: (String) -> Unit,
    selectedImageUri: Uri?,
    onPickGallery: () -> Unit,
    onCaptureCamera: () -> Unit,
    onResetImage: () -> Unit,
    onAnalyze: (Uri) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SandBg),
                border = BorderStroke(0.5.dp, FineBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Kính Chào Bạn Đến Với Máy Quét AI!",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        fontSize = 16.sp,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Chụp ảnh hoặc quét trực tiếp lòng bàn tay để mô hình AI phân tích mẫn tiệp các đường chỉ tay chính, phụ và các gò phong thủy Á - Âu lập tức.",
                        fontSize = 11.5.sp,
                        color = SlateGray,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(0.5.dp, FineBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Nhập Thông Tin Bản Mệnh",
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalText,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = userName,
                        onValueChange = onNameChange,
                        label = { Text("Họ và tên người xem", color = SlateGray, fontSize = 12.sp) },
                        placeholder = { Text("Ví dụ: Trần Minh Tân", color = SlateGray.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = FineBorder,
                            focusedTextColor = CharcoalText,
                            unfocusedTextColor = CharcoalText
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Image container
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(0.5.dp, FineBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Định Dạng Ảnh Lòng Bàn Tay",
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalText,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedImageUri == null) {
                        // High-end minimalist camera placeholder & capture dashboard
                        MinimalistCameraScanner(
                            onCaptureClick = onCaptureCamera,
                            onPickGalleryClick = onPickGallery,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                    } else {
                        // High-end live camera snap preview frame with overlaying aesthetic elements
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CreamBg)
                                .border(1.dp, FineBorder, RoundedCornerShape(16.dp))
                        ) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Bản đồ chỉ tay của bản mệnh",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            
                            // Vignette shading or status overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.3f),
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.5f)
                                            )
                                        )
                                    )
                            )

                            // Camera corner brackets layered neatly over the image 
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                // Top-Left Bracket
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.TopStart)
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth().height(2.5.dp).background(GoldAccent, RoundedCornerShape(1.dp)))
                                    Box(modifier = Modifier.width(2.5.dp).fillMaxHeight().background(GoldAccent, RoundedCornerShape(1.dp)))
                                }
                                // Top-Right Bracket
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.TopEnd)
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth().height(2.5.dp).background(GoldAccent, RoundedCornerShape(1.dp)).align(Alignment.TopEnd))
                                    Box(modifier = Modifier.width(2.5.dp).fillMaxHeight().background(GoldAccent, RoundedCornerShape(1.dp)).align(Alignment.TopEnd))
                                }
                                // Bottom-Left Bracket
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.BottomStart)
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth().height(2.5.dp).background(GoldAccent, RoundedCornerShape(1.dp)).align(Alignment.BottomStart))
                                    Box(modifier = Modifier.width(2.5.dp).fillMaxHeight().background(GoldAccent, RoundedCornerShape(1.dp)).align(Alignment.BottomStart))
                                }
                                // Bottom-Right Bracket
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.BottomEnd)
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth().height(2.5.dp).background(GoldAccent, RoundedCornerShape(1.dp)).align(Alignment.BottomEnd))
                                    Box(modifier = Modifier.width(2.5.dp).fillMaxHeight().background(GoldAccent, RoundedCornerShape(1.dp)).align(Alignment.BottomEnd))
                                }

                                // Centered status indicator badge saying "READY FOR AI"
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.9f)),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.align(Alignment.Center)
                                ) {
                                    Text(
                                        text = "ẢNH ĐÃ SẴN SÀNG",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            // Minimalist Reset button
                            IconButton(
                                onClick = onResetImage,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .size(32.dp)
                                    .background(PureWhite.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Reset",
                                    tint = CharcoalText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onAnalyze(selectedImageUri) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Analyze", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "BẮT ĐẦU PHÂN TÍCH CHỈ TAY AI",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }

        // Checklist for Quality Image
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(0.5.dp, FineBorder),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Hướng Dẫn Chụp Quét Đạt Chuẩn 5 Sao",
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText,
                        fontSize = 13.5.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    val guides = listOf(
                        "Rửa sạch bàn tay & lau khô để gột sạch bụi nhòe vân tay.",
                        "Đặt lòng bàn tay phẳng phẳng trên mặt phẳng sáng, màu đồng nhất (trắng hoặc xám nhạt).",
                        "Các ngón tay xòe mười ngón tách tự nhiên, không bắt chéo.",
                        "Chụp dưới ánh sáng khuếch tán, tránh nắng gắt/bóng đổ mạnh của đèn.",
                        "Giữ điện thoại vuông góc cao ngay phía trên lòng bàn tay.",
                        "Nên chụp cả hai bàn tay (một lần bàn tay thuận, một lần bàn tay trái) để có kết quả mẫn tiệp tốt nhất."
                    )
                    guides.forEachIndexed { idx, guide ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                "(${idx + 1}) ",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                            Text(
                                guide,
                                fontSize = 11.5.sp,
                                color = CharcoalText,
                                lineHeight = 15.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==================== TAB 2: LIBRARY / DATABASE ARCHIVES ====================
@Composable
fun LibraryTab(
    readings: List<PalmReading>,
    onSelectReading: (PalmReading) -> Unit,
    onDeleteReading: (PalmReading) -> Unit
) {
    if (readings.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "No Readings",
                    tint = SlateGray,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Thư Viện Quẻ Đang Trống",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = CharcoalText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Hãy lập quẻ thủ công hoặc quét chỉ tay bằng AI để lưu lại hãm sách lịch sử chiêm nghiệm của bạn.",
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = SlateGray,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "THƯ VIỆN LỊCH SỬ QUẺ",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    fontSize = 15.sp
                )
                Text(
                    "Tổng số: ${readings.size}",
                    fontSize = 12.sp,
                    color = SlateGray,
                    fontWeight = FontWeight.SemiBold
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(readings, key = { it.id }) { reading ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(0.5.dp, FineBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectReading(reading) }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Thumbnail representer or custom sign
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SandBg)
                                    .border(1.dp, FineBorder, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (reading.imageUriString != null) {
                                    AsyncImage(
                                        model = reading.imageUriString,
                                        contentDescription = "Palm Thumb",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (reading.isAiAnalysis) Icons.Default.Star else Icons.Default.Face,
                                        contentDescription = "Manual icon",
                                        tint = GoldAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Middle Info Content
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = reading.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalText
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (reading.isAiAnalysis) "Máy quét AI thông minh" else "Nguyên tố: ${reading.handType}",
                                    fontSize = 11.5.sp,
                                    color = SageSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                Text(
                                    text = sdf.format(Date(reading.timestamp)),
                                    fontSize = 10.sp,
                                    color = SlateGray
                                )
                            }

                            // Right action elements
                            IconButton(
                                onClick = { onDeleteReading(reading) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Red.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== TAB 3: SETTINGS / API & ABOUTS ====================
@Composable
fun SettingsTab(
    apiKey: String,
    onKeyChange: (String) -> Unit
) {
    var keyVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SandBg),
                border = BorderStroke(0.5.dp, FineBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "CÀI ĐẶT CẤU HÌNH PHONG THỦY",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        fontSize = 16.sp,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Quản lý Khóa kết nối API và thông tin tác giả hệ thống.",
                        fontSize = 12.sp,
                        color = SlateGray
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(0.5.dp, FineBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "1. Khóa API Gemini",
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalText,
                        fontSize = 14.sp
                    )
                    Text(
                        "Yêu cầu cho tính năng Quét AI. Bạn có thể sử dụng khóa API Gemini mặc định hoặc ghi đè bằng khóa của bạn.",
                        fontSize = 11.5.sp,
                        color = SlateGray,
                        modifier = Modifier.padding(bottom = 12.dp),
                        lineHeight = 15.sp
                    )

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = onKeyChange,
                        label = { Text("GEMINI_API_KEY", color = SlateGray, fontSize = 12.sp) },
                        placeholder = { Text("Nhập API Key...", color = SlateGray.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = FineBorder,
                            focusedTextColor = CharcoalText,
                            unfocusedTextColor = CharcoalText
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SandBg),
                        border = BorderStroke(0.5.dp, FineBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠ LƯU Ý BẢO MẬT: Khóa API được lưu giữ an toàn bên trong tài khoản và phiên thiết lập hệ thống, giúp bạn thực thi quét ảnh mượt mà.",
                            fontSize = 10.sp,
                            color = SlateGray,
                            lineHeight = 14.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(0.5.dp, FineBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "2. Thông Tin Nhà Kiến Tạo",
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalText,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Người Tạo:", fontSize = 12.sp, color = SlateGray)
                        Text("TRẦN MINH TÂN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Nền tảng phát triển:", fontSize = 12.sp, color = SlateGray)
                        Text("Android - Jetpack Compose", fontSize = 12.sp, color = CharcoalText)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Phong cách thiết kế:", fontSize = 12.sp, color = SlateGray)
                        Text("Minimal Luxury Gold Accent", fontSize = 12.sp, color = CharcoalText)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Phiên bản hỗ trợ:", fontSize = 12.sp, color = SlateGray)
                        Text("1.0.0 (Bản Đầy Đủ)", fontSize = 12.sp, color = CharcoalText)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SandBg),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "\"Tướng tùy tâm sinh, tướng tùy tâm diệt\"",
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Serif,
                        color = SageSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Lòng bàn tay là lược đồ động của cơ thể và ý niệm, hãy nỗ lực hành thiện tích phúc mỗi ngày để khai thông những điều kỳ diệu nhất.",
                        fontSize = 11.sp,
                        color = SlateGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MinimalistCameraScanner(
    onCaptureClick: () -> Unit,
    onPickGalleryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(CreamBg, RoundedCornerShape(16.dp))
            .border(1.dp, FineBorder, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Subtle watermark background scan grids
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 40.dp.toPx()
            val gridColor = FineBorder.copy(alpha = 0.4f)
            // Draw horizontal lines
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = gridColor,
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += gridSpacing
            }
            // Draw vertical lines
            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = gridColor,
                    start = androidx.compose.ui.geometry.Offset(x, 0f),
                    end = androidx.compose.ui.geometry.Offset(x, size.height),
                    strokeWidth = 1f
                )
                x += gridSpacing
            }
        }

        // Camera Corner Brackets (Luxury Style)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top-Left Bracket
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopStart)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(GoldAccent, RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(GoldAccent, RoundedCornerShape(2.dp))
                )
            }

            // Top-Right Bracket
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopEnd)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(GoldAccent, RoundedCornerShape(2.dp))
                        .align(Alignment.TopEnd)
                )
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(GoldAccent, RoundedCornerShape(2.dp))
                        .align(Alignment.TopEnd)
                )
            }

            // Bottom-Left Bracket
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomStart)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(GoldAccent, RoundedCornerShape(2.dp))
                        .align(Alignment.BottomStart)
                )
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(GoldAccent, RoundedCornerShape(2.dp))
                        .align(Alignment.BottomStart)
                )
            }

            // Bottom-Right Bracket
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(GoldAccent, RoundedCornerShape(2.dp))
                        .align(Alignment.BottomEnd)
                )
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(GoldAccent, RoundedCornerShape(2.dp))
                        .align(Alignment.BottomEnd)
                )
            }
        }

        // Center visual guide & scanner line
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Outer circle target frame
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(PureWhite.copy(alpha = 0.8f), RoundedCornerShape(50.dp))
                        .border(1.dp, FineBorder, RoundedCornerShape(50.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Dotted scanner outline
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .border(
                                width = 1.5.dp,
                                color = GoldAccent.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(43.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Iconic scan camera emblem
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Scan target",
                            tint = GoldAccent,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "KHUNG QUÉT CHỈ TAY PHONG THỦY",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = CharcoalText,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Chụp hoặc tải ảnh để AI tự động trích xuất nhân khí",
                    fontSize = 11.sp,
                    color = SlateGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons integrated within the scanner
                Row(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCaptureClick,
                        border = BorderStroke(1.dp, GoldAccent),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Chụp",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CHỤP ẢNH CHỈ TAY",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Button(
                        onClick = onPickGalleryClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Thư viện",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CHỌN TỪ THƯ VIỆN",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Horizontal laser lines running through (simulating real scan beam overlays)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.85f)
                .height(2.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            GoldAccent,
                            GoldAccent.copy(alpha = 0.3f),
                            GoldAccent,
                            Color.Transparent
                        )
                    )
                )
        )
    }
}
