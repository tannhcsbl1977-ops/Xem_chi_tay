package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.InlineData
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.database.AppDatabase
import com.example.data.database.PalmReading
import com.example.data.repository.PalmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

sealed interface ScanUiState {
    object Idle : ScanUiState
    object Loading : ScanUiState
    data class Success(val result: String, val savedId: Long) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

class PalmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PalmRepository
    val allReadings: StateFlow<List<PalmReading>>

    // Custom API Key from settings (defaults to BuildConfig key)
    private val _apiKey = MutableStateFlow(BuildConfig.GEMINI_API_KEY)
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    // Temporary values for local manual quẻ selection
    val selectedHandType = MutableStateFlow("Đất (Earth)")
    val selectedSinhDao = MutableStateFlow("Dài và sâu (Sức khỏe tốt, thọ)")
    val selectedTriDao = MutableStateFlow("Dài, rõ (Minh mẫn, trí tuệ)")
    val selectedTamDao = MutableStateFlow("Sâu, dài (Yêu thương dạt dào)")
    val selectedSoMenh = MutableStateFlow("Rõ, sâu (Sự nghiệp vững chắc)")
    val selectedThaiDuong = MutableStateFlow("Nổi rõ (Có tài hoa, dễ thành đạt)")
    
    val selectedGoVenus = MutableStateFlow("Đầy đặn (Nhiệt huyết, sinh lực cao)")
    val selectedGoJupiter = MutableStateFlow("Nhô cao (Tự tin, tham vọng lớn)")
    val selectedGoSaturn = MutableStateFlow("Vừa phải (Nghiêm túc, trách nhiệm)")
    
    val selectedSpecialSign = MutableStateFlow("Ngôi sao (Sự xuất chúng, may mắn)")

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PalmRepository(database.palmReadingDao())
        allReadings = repository.allReadings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun updateApiKey(key: String) {
        _apiKey.value = key
    }

    fun resetScanState() {
        _scanState.value = ScanUiState.Idle
    }

    // Direct delete from history
    fun deleteReading(reading: PalmReading) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteReading(reading)
        }
    }

    /**
     * Resizes the selected Image to prevent memory issues and converts to Base64
     */
    private suspend fun uriToBase64(uri: Uri): Pair<String, String>? = withContext(Dispatchers.IO) {
        try {
            val context = getApplication<Application>().applicationContext
            val contentResolver = context.contentResolver
            
            // Get mime type
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            
            // Decode image with bounds check (resize if larger than 1200px)
            var inputStream: InputStream? = contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            var width = options.outWidth
            var height = options.outHeight
            var scale = 1
            val maxDimension = 1000
            if (width > maxDimension || height > maxDimension) {
                val larger = maxOf(width, height)
                scale = (larger / maxDimension) + 1
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = scale
            }
            inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (bitmap == null) return@withContext null

            // Compress to JPEG
            val byteStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteStream)
            val byteArray = byteStream.toByteArray()
            val base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            
            Pair(base64, "image/jpeg")
        } catch (e: Exception) {
            Log.e("PalmViewModel", "Error converting image", e)
            null
        }
    }

    /**
     * Conduct Gemini Multimodal palm analysis
     */
    fun analyzePalmWithAi(name: String, imageUri: Uri) {
        _scanState.value = ScanUiState.Loading
        viewModelScope.launch {
            try {
                val key = _apiKey.value.trim()
                if (key.isEmpty() || key == "MY_GEMINI_API_KEY") {
                    _scanState.value = ScanUiState.Error("Vui lòng cấu hình GEMINI_API_KEY chính xác trong tab Cài đặt hoặc trong secrets.")
                    return@launch
                }

                // Convert Image
                val imgData = uriToBase64(imageUri)
                if (imgData == null) {
                    _scanState.value = ScanUiState.Error("Không thể đọc và tải hình ảnh từ thiết bị này. Hãy thử lại.")
                    return@launch
                }

                val (base64Data, mimeType) = imgData

                // Comprehensive System Instructions containing all guidelines specified by the user
                val systemInstructionPrompt = """
                    Bạn là một nhà tử vi, phong thủy và chuyên gia nhân tướng học (xem chỉ tay) lâu năm theo phương pháp cổ truyền Á - Âu kết hợp (trọng tâm là tư liệu phong thủy Việt - Trung). 
                    Hãy phân tích ảnh lòng bàn tay của của khách hàng tên là: $name.
                    
                    Yêu cầu phân tích chi tiết dựa trên các điểm sau:
                    1. HÌNH DẠNG BÀN TAY (Đất, Nước, Khí, Hoặc hỗn hợp): Phân loại và phân tích tính cách, sở trường.
                    2. CÁC ĐƯỜNG CHỈ TAY CHÍNH:
                       - Sinh đạo (Life line): Sức khỏe, sinh lực, tuổi thọ, biến thể dài/ngắn, gãy/đứt, đảo, nhánh...
                       - Trí đạo (Head line): Trí tuệ, khả năng tư duy, gãy/đứt, chữ thập, nhánh...
                       - Tâm đạo (Heart line): Tình cảm, xu hướng giao tiếp, các chữ X hay nhánh...
                       - Số mệnh (Fate line): Sự nghiệp, thăng tiến, gián đoạn...
                       - Thái dương (Sun line): Tài năng, danh tiếng, nghệ thuật...
                    3. CÁC GÒ TRÊN BÀN TAY: Gò Kim Tinh, Gò Mộc Tinh, Gò Thổ Tinh, Gò Thái Dương, Gò Thủy Tinh, Gò Hỏa Tinh (âm/dương), Gò Thái Âm, Gò Địa. Mô tả tính trạng xẹp, đầy, nhô cao.
                    4. DẤU HIỆU ĐẶC BIỆT: Ngôi sao (Star), Chữ thập (Cross), Tam giác (Triangle), Hình vuông (Square), Hòn đảo (Island).
                    
                    Phong cách viết báo cáo:
                    - Tiêu đề chính rõ ràng, phân vùng rõ rệt bằng Markdown.
                    - Sử dụng ngôn từ tinh tế, sâu sắc, sang trọng, mang phong vị tâm linh huyền học Á Đông cổ kính kết hợp khoa học hiện đại.
                    - Đưa ra lời khuyên cụ thể, hữu ích để cải thiện bản thân, vận mệnh và giữ gìn sức khỏe.
                    - Ở đầu hoặc cuối báo cáo, luôn ghi dòng chữ: "Ứng dụng được tạo bởi TRẦN MINH TÂN".
                    - Ngôn ngữ: Tiếng Việt 100%.
                """.trimIndent()

                val prompt = "Xin hãy phân tích ảnh lòng bàn tay này cho khách hàng tên $name và xuất ra kết quả phân dịch chi tiết theo đúng hướng dẫn."

                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(text = prompt),
                                Part(inlineData = InlineData(mimeType = mimeType, data = base64Data))
                            )
                        )
                    ),
                    systemInstruction = Content(
                        parts = listOf(Part(text = systemInstructionPrompt))
                    )
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.service.generateContent(key, request)
                }

                val textResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (textResponse != null) {
                    // Create and save to local Room base
                    val reading = PalmReading(
                        name = if (name.isBlank()) "Khách danh" else name,
                        handType = "AI Phân Tích",
                        linesDescription = "Được phân tích thông qua camera/ảnh quét bằng AI",
                        mountsDescription = "Gò bàn tay quét tự động",
                        specialSigns = "Dấu hiệu phát hiện bởi AI",
                        analysisResult = textResponse,
                        imageUriString = imageUri.toString(),
                        isAiAnalysis = true
                    )

                    val savedId = withContext(Dispatchers.IO) {
                        repository.insertReading(reading)
                    }

                    _scanState.value = ScanUiState.Success(textResponse, savedId)
                } else {
                    _scanState.value = ScanUiState.Error("Không nhận được câu trả lời từ máy chủ Gemini. Hay kiểm tra lại khóa API của bạn.")
                }

            } catch (e: Exception) {
                Log.e("PalmViewModel", "AI Process failed", e)
                _scanState.value = ScanUiState.Error("Lỗi kết nối hoặc xử lý: ${e.localizedMessage ?: "Vui lòng kiểm tra lại mạng và API Key."}")
            }
        }
    }

    /**
     * Conduct high-quality offline manual generation based strictly on the provided tables
     */
    fun generateManualReading(name: String) {
        _scanState.value = ScanUiState.Loading
        viewModelScope.launch {
            try {
                val inputName = if (name.isBlank()) "Khách danh" else name
                
                // Formulate static analysis matching tables from guideline
                val type = selectedHandType.value
                val sDao = selectedSinhDao.value
                val tDao = selectedTriDao.value
                val tamDao = selectedTamDao.value
                val sMenh = selectedSoMenh.value
                val tDuongs = selectedThaiDuong.value
                
                val gVenus = selectedGoVenus.value
                val gJupiter = selectedGoJupiter.value
                val gSaturn = selectedGoSaturn.value
                val sign = selectedSpecialSign.value

                // Simple interpretation rule engines mapping parameters
                val handTypeReport = when {
                    type.contains("Đất") -> """
                        • **Loại bàn tay: NGUYÊN TỐ ĐẤT (Earth)**
                          - Bàn tay có lòng bàn tay vuông vức, dày dặn và các ngón tay ngắn hơn so với chiều dài lòng bàn tay. Da thường ráp, nhiều nếp nhăn; các đường chỉ ít và rõ ràng.
                          - **Tính cách**: Bạn là người thực tế, kiên định và rất bền bỉ. Bạn có sức khỏe dồi dào và sức chịu đựng cao. Bạn trân trọng truyền thống, thận trọng, có năng lực làm việc ổn định và cực kỳ đáng tin cậy. Bạn điềm đạm và giữ vững lập trường của mình.
                    """.trimIndent()
                    type.contains("Nước") -> """
                        • **Loại bàn tay: NGUYÊN TỐ NƯỚC (Water)**
                          - Bàn tay có lòng bàn tay dài, hẹp (hình chủ nhật) với các ngón tay dài và rất mềm dẻo. Da mịn màng; chứa nhiều đường chỉ tay nhỏ chằng chịt.
                          - **Tính cách**: Bạn rất nhạy cảm, giàu trí tưởng tượng, có trực giác mạnh mẽ và thấu hiểu lòng người. Bạn dễ xúc động, hòa nhã, luôn quan tâm người xung quanh và mang tâm hồn nghệ sĩ đích thực. Tuy nhiên, bạn dễ rơi vào mộng tưởng hoặc lo lắng thái quá.
                    """.trimIndent()
                    else -> """
                        • **Loại bàn tay: NGUYÊN TỐ KHÍ (Air)**
                          - Lòng bàn tay phần lớn vuông nhưng mềm mại, linh hoạt; ngón tay thon dài hơn chiều rộng của lòng bàn tay. Da mịn và ấm.
                          - **Tính cách**: Bạn thông minh vượt trội, có óc phân tích, cởi mở và giao tiếp vô cùng hoạt bát. Bạn yêu thích tìm tòi tri thức mới, có khả năng thích ứng cao với hoàn cảnh và tư duy rất logic, sắc bén.
                    """.trimIndent()
                }

                val linesReport = """
                    ### ✦ PHÂN TÍCH TIỂU TIẾT CÁC ĐƯỜNG CHỈ TAY
                    
                    * **Sinh Đạo (Life Line) - $sDao**:
                      Thể hiện sinh lực, sức khỏe và tính ổn định. Với đặc điểm này, ${getSinhDaoInterpretation(sDao)}
                      
                    * **Trí Đạo (Head Line) - $tDao**:
                      Đại diện cho tư duy, tầm nhìn và học vấn. Điều này chỉ ra rằng ${getTriDaoInterpretation(tDao)}
                      
                    * **Tâm Đạo (Heart Line) - $tamDao**:
                      Nói về chuyện tình cảm, mối quan hệ và nội tâm. Định vị này tiết lộ ${getTamDaoInterpretation(tamDao)}
                      
                    * **Số Mệnh (Fate Line) - $sMenh**:
                      Chi phối con đường lập nghiệp và công danh. Trạng thái chỉ tay chỉ ra rằng ${getSoMenhInterpretation(sMenh)}
                      
                    * **Thái Dương (Sun Line) - $tDuongs**:
                      Liên quan trực tiếp đến may mắn, tài hoa nghệ thuật và danh vọng. Đặc điểm này báo hiệu ${getThaiDuongInterpretation(tDuongs)}
                """.trimIndent()

                val mountsReport = """
                    ### ✦ LUẬN ĐOÁN CÁC GÒ VÙNG NỔI BẬT
                    
                    * **Gò Kim Tinh (Venus) - $gVenus**:
                      Nằm dưới ngón tay cái, liên can đến nhiệt lượng sống và tình bằng hữu. ${getGoVenusInterpretation(gVenus)}
                      
                    * **Gò Mộc Tinh (Jupiter) - $gJupiter**:
                      Dưới ngón tay trỏ, đại diện cho ý chí thống lĩnh và uy tín. ${getGoJupiterInterpretation(gJupiter)}
                      
                    * **Gò Thổ Tinh (Saturn) - $gSaturn**:
                      Dưới ngón giữa, liên quan đến tinh thần trách nhiệm và kỷ cương. ${getGoSaturnInterpretation(gSaturn)}
                      
                    * **Gò phụ trợ khác**:
                      - *Gò Thái Dương & Thủy Tinh*: Bình ổn, khéo léo trong tính toán kinh tài và giao tế bằng hữu.
                      - *Gò Thái Âm & Gò Địa*: Trực giác vẹn toàn, sức hấp dẫn tự nhiên bổ trợ hữu hiệu cho sự nghiệp.
                """.trimIndent()

                val signsReport = """
                    ### ✦ DẤU ẤN ĐẶC BIỆT TRONG LÒNG BÀN TAY
                    
                    * **Dấu hiệu thu hoạch được: $sign**:
                      ${getSpecialSignInterpretation(sign)}
                """.trimIndent()

                val totalReport = """
                    # ✺ QUẺ LUẬN GIẢI CHỈ TAY PHONG THỦY CỔ TRUYỀN Á - ÂU
                    ### Khách xem quẻ: $inputName | Ngày luận quẻ: 2026-06-16
                    
                    ---
                    
                    $handTypeReport
                    
                    $linesReport
                    
                    $mountsReport
                    
                    $signsReport
                    
                    ---
                    ### ❂ LỜI KHUYÊN TƯỞNG SỐ & PHONG THỦY CẢI MỆNH
                    Mỗi đường chỉ tay là bức họa động thế tự nhiên của tâm tính và thể lực. Mặc dù bẩm sinh cấu thành một phần tính cách thông qua ba nguyên tố Đất - Khí - Nước, các đường nét này dứt khoát có thể thay đổi dựa trên nỗ lực, thói quen sinh hoạt và cách đối nhân xử thế hàng ngày của bạn ("Tướng tùy tâm sinh"). Hãy rèn luyện tâm trí bình thản, gieo mầm phúc đức thiện lương và nỗ lực nâng cao tri thức để biến mọi thử thách (như nét đứt hay chữ thập) thành cơ hội thành quang rực rỡ nhất.
                    
                    *Ý kiến giải luận dựa trên tài liệu chọn lọc Nhân tướng học Á-Âu cổ điển.*
                    
                    **NGƯỜI TẠO ỨNG DỤNG: TRẦN MINH TÂN**
                """.trimIndent()

                val reading = PalmReading(
                    name = inputName,
                    handType = type,
                    linesDescription = "Sinh đạo: $sDao | Trí đạo: $tDao | Tâm đạo: $tamDao",
                    mountsDescription = "Kim Tinh: $gVenus | Mộc Tinh: $gJupiter | Thổ Tinh: $gSaturn",
                    specialSigns = sign,
                    analysisResult = totalReport,
                    isAiAnalysis = false
                )

                val savedId = withContext(Dispatchers.IO) {
                    repository.insertReading(reading)
                }

                _scanState.value = ScanUiState.Success(totalReport, savedId)

            } catch (e: Exception) {
                _scanState.value = ScanUiState.Error("Có lỗi xảy ra khi tạo quẻ thủ công: ${e.message}")
            }
        }
    }

    // INTERPRETATION UTILS FOR OFFLINE ENGINE
    private fun getSinhDaoInterpretation(v: String): String = when {
        v.contains("Dài") -> "bạn sở hữu sức khoẻ viên mãn, dẻo dai và có tiềm năng trường thọ vượt bậc. Tránh được nhiều bệnh tật nguy hiểm."
        v.contains("Ngắn") -> "thể lực của bạn ở mức trung bình, dễ bị mệt mỏi trong công việc dài hơi. Bạn cần rèn luyện dưỡng sinh và giữ chế độ ăn uống lành mạnh."
        v.contains("Đứt") -> "báo hiệu có một số giai đoạn cuộc đời biến động về nơi cư trú, chuyển dời môi trường sống hoặc có thử thách sức khoẻ lớn cần chú ý vượt qua."
        v.contains("nhánh") -> "đời bạn sẽ chứng kiến nhiều chuyến đi xa, du lịch hoặc định cư tại nước ngoài với nhiều cơ duyên thú vị phát đạt."
        else -> "mức độ phòng ngự sức đề kháng có lúc lao đao, khuyên bạn giữ tinh thần an ổn tránh ưu lo phiền muộn hại thân."
    }

    private fun getTriDaoInterpretation(v: String): String = when {
        v.contains("Dài") -> "bạn là người nhạy bén, óc phán đoán sắc sảo, tính quyết đoán cao độ và hiếm khi bị cảm xúc chi phối khi đưa ra quyết định."
        v.contains("Ngắn") -> "tư duy của bạn thăng hoa theo trực giác, thiên về cảm xúc nhiều hơn lý tính, mang hơi thở nghệ thuật bộc phát."
        v.contains("Gãy") -> "có thể có những khúc quanh đột ngột trong con đường học vấn hoặc học thuật, hoặc bạn có lối rẽ tư duy rất độc đáo."
        v.contains("nhánh") -> "đầu óc bạn vô cùng đa nhiệm, óc sáng tạo phong phú đa chiều, tuy nhiên đôi lúc cần rèn sự tập trung cao độ tránh xao nhãng."
        else -> "khả năng tĩnh tâm suy nghĩ đôi khi bấp bênh, cần hạn chế stress căng thẳng trong công việc."
    }

    private fun getTamDaoInterpretation(v: String): String = when {
        v.contains("Sâu") -> "tình cảm phong phú dạt dào, chung thủy sắc son và có cuộc sống gia đạo êm ấm viên mãn."
        v.contains("Ngắn") -> "bạn thuộc tuýp người kín đáo, trầm tĩnh, kiểm soát cảm xúc tốt và ít khi thổ lộ tâm can cho người khác."
        v.contains("Đứt") -> "chuyện tình duyên của bạn dễ gặp thử thách thử thách gãy đổ buổi ban đầu, song nếu kiên nhẫn vượt qua sẽ thấu hiểu sâu sắc."
        v.contains("nhánh") -> "bạn có nhân duyên cực tốt, quảng giao nhiều bằng hữu quý và luôn hết lòng giúp đỡ mọi người xung quanh."
        else -> "nội tâm dồi dào lòng bao dung nhưng đôi khi lo âu thái quá khiến chuyện tình cảm xuất hiện gợn sóng."
    }

    private fun getSoMenhInterpretation(v: String): String = when {
        v.contains("Rõ") -> "con đường sự nghiệp vô cùng vững chắc, mục tiêu rõ ràng, định hình bản thân xuất sắc và dễ đạt được danh vọng."
        v.contains("Nhạt") -> "hướng đi nghề nghiệp có phần linh hoạt, tự do tự tại, đổi nghề để tìm chân giá trị chứ không chịu bó buộc."
        v.contains("Đứt") -> "có một số gạch nối đứt quãng chuyển đổi công việc quan trọng, là bước đệm lớn hướng tới phồn vinh hơn."
        else -> "sự tự lập tự cường cao, có thiên hướng độc lập tác chiến và tự do làm chủ cuộc đời."
    }

    private fun getThaiDuongInterpretation(v: String): String = when {
        v.contains("Nổi") -> "bạn được ban tặng óc sáng tạo tuyệt vời, dễ tỏa sáng trong tập thể và được đông đảo mọi người trợ lực, yêu mến."
        v.contains("Thấp") -> "cuộc sống trầm ổn yên bình, dĩ hòa vi quý, không màng danh tiếng mà tìm niềm vui trong lao động gia đình giản dị."
        else -> "khả năng cảm thụ nghệ thuật xuất sắc, có quý nhân phò trợ lúc gieo neo."
    }

    private fun getGoVenusInterpretation(v: String): String = when {
        v.contains("Đầy") -> "Biểu thị là con người dạt dào tình cảm, mến khách, đầy ắp ham muốn cống hiến và tràn trề năng lượng sinh học dồi dào."
        else -> "Sức khoẻ ổn định, hiền hòa, tuy nhiên cần chăm chút bồi bổ năng lượng tích cực tránh hao tổn nguyên khí."
    }

    private fun getGoJupiterInterpretation(v: String): String = when {
        v.contains("Nhô") -> "Thể hiện bản lĩnh kiên cường, ý chí vươn lên mạnh mẽ dứt khoát đứng ở vị trí lãnh đạo, tham vọng phát triển sự nghiệp đỉnh cao."
        else -> "Bản tính khiêm nhường mộc mạc, biết ứng xử dịu dàng giữ hòa khí tối đa trong đội ngũ."
    }

    private fun getGoSaturnInterpretation(v: String): String = when {
        v.contains("Vừa") -> "Là biểu hiện của người có bổn phận, làm việc chu toàn ngăn nắp, trầm tĩnh chín chắn trước khó khăn."
        else -> "Thích sự bay bổng tự do, phóng khoáng, đôi lúc không quá khuôn mẫu gò bó kỷ cương thép."
    }

    private fun getSpecialSignInterpretation(v: String): String = when {
        v.contains("Ngôi sao") -> "Cực kỳ may mắn! Sao dưới gò trỏ đem lại vận may đột xuất, công thành danh toại hoặc gặp được ý trung nhân trăm năm viên mãn."
        v.contains("Chữ thập") -> "Cảnh báo một vài chướng ngại gập ghềnh đòi hỏi bản lĩnh kiên trì. Nếu chữ thập này nằm trên gò Mộc Tinh lại là cát tường đại cát bất ngờ!"
        v.contains("Tam giác") -> "Chỉ năng khiếu trí tuệ cao, tư duy nghiên cứu phân tích khoa học kỹ thuật mẫn tiệp hoặc có linh giác thần bí cực nhạy."
        v.contains("Hình vuông") -> "Dấu hiệu an toàn tuyệt đối. Được xem là lá bùa bảo hộ vô hình tự nhiên, luôn có quý nhân đỡ đầu, tránh hiểm nghèo và chuyển hung thành cát."
        else -> "Vùng trũng tạm thời của năng lượng dòng chảy. Khuyên bạn nên chú trọng nghỉ ngơi dưỡng tâm tính để phục hồi phong độ vốn có."
    }
}
