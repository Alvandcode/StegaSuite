/**
 * StegaSuite - MainActivity
 * © Designed by alvandcode - https://github.com/Alvandcode
 * Modern purple-themed steganography app with glassmorphism
 */
package com.stegasuite.app
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Purple50 = Color(0xFFF3E5F5)
private val Purple100 = Color(0xFFE1BEE7)
private val Purple200 = Color(0xFFCE93D8)
private val Purple400 = Color(0xFFAB47BC)
private val Purple500 = Color(0xFF9C27B0)
private val Purple600 = Color(0xFF8E24AA)
private val Purple700 = Color(0xFF7B1FA2)
private val Purple800 = Color(0xFF6A1B9A)
private val Purple900 = Color(0xFF4A148C)
private val DarkBg = Color(0xFF1A1025)
private val CardLight = Color(0xFFFAFAFA)
private val AccentPink = Color(0xFFE91E63)
private val AccentGradientStart = Color(0xFFAB47BC)
private val AccentGradientEnd = Color(0xFFE91E63)

private val fa = mapOf(
    "title" to "استگانوسویت",
    "subtitle" to "مخفی‌سازی امن فایل‌ها",
    "hide" to "مخفی کردن",
    "extract" to "استخراج",
    "selectCarrier" to "انتخاب فایل حامل",
    "selectFile" to "انتخاب فایل مخفی",
    "password" to "رمز عبور",
    "passwordHint" to "اختیاری - برای رمزنگاری",
    "hideBtn" to "مخفی کن و ذخیره کن",
    "extBtn" to "استخراج فایل مخفی",
    "status" to "وضعیت",
    "ready" to "آماده",
    "hiding" to "در حال مخفی‌سازی...",
    "extracting" to "در حال استخراج...",
    "saved" to "ذخیره شد",
    "extracted" to "استخراج شد",
    "error" to "خطا",
    "supported" to "همه فرمت‌ها پشتیبانی می‌شوند",
    "warning" to "فرمت‌های فشرده ممکنه داده رو خراب کنن",
    "copyright" to "طراحی و اجرا توسط alvandcode",
    "contact" to "ارتباط با سازنده",
    "mode" to "حالت",
    "carrierInfo" to "اطلاعات فایل حامل",
    "capacity" to "ظرفیت",
    "fileName" to "نام فایل",
    "size" to "حجم"
)
private val en = mapOf(
    "title" to "StegaSuite",
    "subtitle" to "Secure File Steganography",
    "hide" to "Hide",
    "extract" to "Extract",
    "selectCarrier" to "Select carrier file",
    "selectFile" to "Select file to hide",
    "password" to "Password",
    "passwordHint" to "Optional - for encryption",
    "hideBtn" to "Hide & Save",
    "extBtn" to "Extract Hidden File",
    "status" to "Status",
    "ready" to "Ready",
    "hiding" to "Hiding...",
    "extracting" to "Extracting...",
    "saved" to "Saved",
    "extracted" to "Extracted",
    "error" to "Error",
    "supported" to "All formats supported",
    "warning" to "Lossy formats may corrupt data",
    "copyright" to "Designed by alvandcode",
    "contact" to "Contact Developer",
    "mode" to "Mode",
    "carrierInfo" to "Carrier Info",
    "capacity" to "Capacity",
    "fileName" to "File Name",
    "size" to "Size"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContent { App() }
    }

    @Composable
    fun App() {
        val ctx = LocalContext.current
        var isFa by remember { mutableStateOf(true) }
        var isDark by remember { mutableStateOf(true) }
        val t = if (isFa) fa else en
        val dir = if (isFa) LayoutDirection.Rtl else LayoutDirection.Ltr

        CompositionLocalProvider(LocalLayoutDirection provides dir) {
            val bgColor = if (isDark) DarkBg else Color(0xFFF5F0FA)
            val textColor = if (isDark) Color.White else Color(0xFF1A1025)
            val subtextColor = if (isDark) Color.White.copy(0.6f) else Color(0xFF666666)
            val cardBg = if (isDark) Color.White.copy(0.06f) else Color.White
            val cardBorder = if (isDark) Color.White.copy(0.08f) else Color(0xFFE0E0E0)

            Box(
                Modifier
                    .fillMaxSize()
                    .background(bgColor)
            ) {
                var extractMode by remember { mutableStateOf(false) }
                var carrierUri by remember { mutableStateOf<Uri?>(null) }
                var carrierInfo by remember { mutableStateOf("") }
                var carrierType by remember { mutableStateOf<CarrierType>(CarrierType.UNKNOWN) }
                var carrierData by remember { mutableStateOf<ByteArray?>(null) }
                var payloadUri by remember { mutableStateOf<Uri?>(null) }
                var payloadInfo by remember { mutableStateOf("")}
                var pass by remember { mutableStateOf("") }
                var showPass by remember { mutableStateOf(false) }
                var status by remember { mutableStateOf(t["ready"]!!) }
                var busy by remember { mutableStateOf(false) }
                var lastSavedUri by remember { mutableStateOf<Uri?>(null) }
                var lastSavedName by remember { mutableStateOf("") }
                var pendingHide by remember { mutableStateOf<Triple<ByteArray, Uri, String>?>(null) }
                var pendingExtract by remember { mutableStateOf<Pair<Uri, String>?>(null) }

                fun getName(u: Uri): String {
                    var n = "file"
                    ctx.contentResolver.query(u, null, null, null, null)?.use { c ->
                        if (c.moveToFirst()) {
                            val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (i >= 0) n = c.getString(i) ?: n
                        }
                    }
                    return n
                }

                fun getSize(u: Uri): Long {
                    var s = 0L
                    ctx.contentResolver.query(u, null, null, null, null)?.use { c ->
                        if (c.moveToFirst()) {
                            val i = c.getColumnIndex(OpenableColumns.SIZE)
                            if (i >= 0) s = c.getLong(i)
                        }
                    }
                    if (s == 0L) try {
                        ctx.contentResolver.openInputStream(u)?.use { s = it.available().toLong() }
                    } catch (_: Exception) {}
                    return s
                }

                fun toStName(orig: String): String {
                    val dot = orig.lastIndexOf('.')
                    return if (dot == -1) "$orig(st)" else orig.substring(0, dot) + "(st)" + orig.substring(dot)
                }

                fun getCarrierTypeLabel(type: CarrierType): String = when (type) {
                    CarrierType.PNG, CarrierType.BMP, CarrierType.TIFF, CarrierType.WEBP -> if (isFa) "عکس" else "Image"
                    CarrierType.WAV -> if (isFa) "صوت" else "Audio"
                    else -> if (isFa) "فایل" else "File"
                }

                val pickCarrier = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { u ->
                    carrierUri = u; carrierData = null; carrierType = CarrierType.UNKNOWN
                    if (u != null) {
                        try {
                            val name = getName(u)
                            val size = getSize(u)
                            val data = ctx.contentResolver.openInputStream(u)?.use { it.readBytes() }
                            if (data != null) {
                                carrierData = data
                                carrierType = PngSteganography.detectCarrierType(name, data)
                                val cap = PngSteganography.capacityBytesForType(carrierType, data)
                                val typeLabel = getCarrierTypeLabel(carrierType)
                                carrierInfo = "$typeLabel • ${size / 1024}KB • ${t["capacity"]}: ~${cap / 1024}KB"
                                status = carrierInfo
                            }
                        } catch (_: Exception) {}
                    }
                }

                val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { u ->
                    payloadUri = u
                    if (u != null) {
                        val n = getName(u)
                        val s = getSize(u)
                        payloadInfo = "$n • ${s / 1024}KB"
                        status = payloadInfo
                    }
                }

                val saveCarrier = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
                    val p = pendingHide ?: return@rememberLauncherForActivityResult
                    if (uri == null) { busy = false; pendingHide = null; return@rememberLauncherForActivityResult }
                    lifecycleScope.launch {
                        try {
                            status = t["hiding"]!!
                            val carrierBytes = p.first
                            val payloadBytes = withContext(Dispatchers.IO) {
                                ctx.contentResolver.openInputStream(p.second)?.use { it.readBytes() } ?: error("file read error")
                            }
                            val payloadName = getName(p.second)
                            val ct = PngSteganography.detectCarrierType(getName(carrierUri!!), carrierBytes)
                            val result = withContext(Dispatchers.IO) {
                                if (ct == CarrierType.PNG || ct == CarrierType.BMP) {
                                    val bmp = BitmapFactory.decodeByteArray(carrierBytes, 0, carrierBytes.size) ?: error("Cannot decode image")
                                    val out = PngSteganography.hide(bmp, payloadBytes, payloadName, p.third.ifEmpty { null })
                                    val baos = java.io.ByteArrayOutputStream()
                                    out.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, baos)
                                    bmp.recycle(); out.recycle()
                                    baos.toByteArray()
                                } else {
                                    PngSteganography.hideGeneric(carrierBytes, payloadBytes, payloadName, p.third.ifEmpty { null }, ct)
                                }
                            }
                            withContext(Dispatchers.IO) { ctx.contentResolver.openOutputStream(uri)?.use { it.write(result) } }
                            val outName = toStName(getName(carrierUri!!))
                            lastSavedUri = uri; lastSavedName = outName
                            status = "${t["saved"]}: $outName"
                        } catch (e: Exception) {
                            status = "${t["error"]}: ${e.message}"
                        } finally {
                            busy = false; pendingHide = null
                        }
                    }
                }

                val saveFile = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
                    val p = pendingExtract ?: return@rememberLauncherForActivityResult
                    if (uri == null) { busy = false; pendingExtract = null; return@rememberLauncherForActivityResult }
                    lifecycleScope.launch {
                        try {
                            status = t["extracting"]!!
                            val carrierBytes = withContext(Dispatchers.IO) {
                                ctx.contentResolver.openInputStream(p.first)?.use { it.readBytes() } ?: error("file read error")
                            }
                            val ex = withContext(Dispatchers.IO) {
                                val magicPng = carrierBytes.size >= 4 && carrierBytes[0] == 0x89.toByte() && carrierBytes[1] == 0x50.toByte()
                                val magicBmp = carrierBytes.size >= 2 && carrierBytes[0] == 0x42.toByte() && carrierBytes[1] == 0x4D.toByte()
                                if (magicPng || magicBmp) {
                                    val bmp = BitmapFactory.decodeByteArray(carrierBytes, 0, carrierBytes.size)
                                    if (bmp != null) {
                                        PngSteganography.extract(bmp, p.second.ifEmpty { null }).also { bmp.recycle() }
                                    } else {
                                        PngSteganography.extractGeneric(carrierBytes, p.second.ifEmpty { null }, PngSteganography.detectCarrierType(getName(p.first), carrierBytes))
                                    }
                                } else {
                                    PngSteganography.extractGeneric(carrierBytes, p.second.ifEmpty { null }, PngSteganography.detectCarrierType(getName(p.first), carrierBytes))
                                }
                            }
                            withContext(Dispatchers.IO) { ctx.contentResolver.openOutputStream(uri)?.use { it.write(ex.bytes) } }
                            lastSavedUri = uri; lastSavedName = ex.fileName
                            status = "${t["extracted"]}: ${ex.fileName}"
                        } catch (e: Exception) {
                            status = "${t["error"]}: ${e.message}"
                        } finally {
                            busy = false; pendingExtract = null
                        }
                    }
                }

                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                t["title"]!!,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Purple900,
                                fontSize = 28.sp
                            )
                            Text(
                                t["subtitle"]!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = subtextColor
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Language toggle
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) Color.White.copy(0.1f) else Purple50)
                                    .clickable { isFa = !isFa }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    if (isFa) "EN" else "فا",
                                    color = if (isDark) Color.White else Purple700,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                            // Theme toggle
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) Color.White.copy(0.1f) else Purple50)
                                    .clickable { isDark = !isDark }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    if (isDark) "☀️" else "🌙",
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Mode Selector - Pill style
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) Color.White.copy(0.06f) else Purple50)
                            .padding(4.dp)
                    ) {
                        Row(Modifier.fillMaxWidth()) {
                            Box(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (!extractMode) Brush.horizontalGradient(listOf(AccentGradientStart, AccentGradientEnd))
                                        else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                    )
                                    .clickable { extractMode = false }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Text(t["hide"]!!, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                }
                            }
                            Box(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (extractMode) Brush.horizontalGradient(listOf(AccentGradientStart, AccentGradientEnd))
                                        else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                    )
                                    .clickable { extractMode = true }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Text(t["extract"]!!, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                }
                            }
                        }
                    }

                    // Carrier File Card
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(cardBorder, cardBorder)))
                    ) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Brush.linearGradient(listOf(Purple600, AccentPink))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Text(t["selectCarrier"]!!, fontWeight = FontWeight.SemiBold, color = textColor, fontSize = 16.sp)
                            }

                            Button(
                                onClick = { pickCarrier.launch("*/*") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDark) Color.White.copy(0.08f) else Purple50,
                                    contentColor = if (isDark) Color.White else Purple700
                                ),
                                contentPadding = PaddingValues(vertical = 14.dp)
                            ) {
                                if (carrierUri == null) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(t["selectCarrier"]!!, fontWeight = FontWeight.Medium)
                                } else {
                                    Text("✓ ${getCarrierTypeLabel(carrierType)} • ${getName(carrierUri!!)}", fontWeight = FontWeight.Medium)
                                }
                            }

                            if (carrierInfo.isNotEmpty()) {
                                Text(carrierInfo, style = MaterialTheme.typography.bodySmall, color = subtextColor)
                            }

                            if (carrierType == CarrierType.GENERIC && carrierUri != null) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFFF3E0))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("⚠️", fontSize = 14.sp)
                                    Text(t["warning"]!!, style = MaterialTheme.typography.bodySmall, color = Color(0xFFE65100), fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    // Payload File Card (only in hide mode)
                    AnimatedVisibility(visible = !extractMode, enter = fadeIn() + slideInVertically()) {
                        Card(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(cardBorder, cardBorder)))
                        ) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(
                                        Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Brush.linearGradient(listOf(Purple400, Purple600))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                    Text(t["selectFile"]!!, fontWeight = FontWeight.SemiBold, color = textColor, fontSize = 16.sp)
                                }

                                Button(
                                    onClick = { pickFile.launch("*/*") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isDark) Color.White.copy(0.08f) else Purple50,
                                        contentColor = if (isDark) Color.White else Purple700
                                    ),
                                    contentPadding = PaddingValues(vertical = 14.dp)
                                ) {
                                    if (payloadUri == null) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(t["selectFile"]!!, fontWeight = FontWeight.Medium)
                                    } else {
                                        Text("✓ $payloadInfo", fontWeight = FontWeight.Medium)
                                    }
                                }

                                Text(
                                    if (isFa) "همه فرمت‌ها: متن، عکس، صوت، ویدیو، PDF, ZIP..." else "All formats: text, images, audio, video, PDF, ZIP...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = subtextColor
                                )
                            }
                        }
                    }

                    // Password Card
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(cardBorder, cardBorder)))
                    ) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Brush.linearGradient(listOf(Purple700, Purple900))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🔑", fontSize = 16.sp)
                                }
                                Text(t["password"]!!, fontWeight = FontWeight.SemiBold, color = textColor, fontSize = 16.sp)
                            }

                            OutlinedTextField(
                                value = pass,
                                onValueChange = { pass = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text(t["passwordHint"]!!, color = subtextColor) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Purple500,
                                    unfocusedBorderColor = cardBorder,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showPass = !showPass }) {
                                        Icon(
                                            if (showPass) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = subtextColor
                                        )
                                    }
                                }
                            )
                        }
                    }

                    // Action Button
                    Button(
                        enabled = carrierUri != null && !extractMode && payloadUri != null && !busy ||
                                carrierUri != null && extractMode && !busy,
                        onClick = {
                            busy = true
                            if (!extractMode) {
                                pendingHide = Triple(carrierData!!, payloadUri!!, pass)
                                saveCarrier.launch(toStName(getName(carrierUri!!)))
                            } else {
                                pendingExtract = Pair(carrierUri!!, pass)
                                saveFile.launch("recovered_file")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(
                                    if (carrierUri != null && !busy)
                                        Brush.horizontalGradient(listOf(AccentGradientStart, AccentGradientEnd))
                                    else
                                        Brush.horizontalGradient(listOf(Color.Gray.copy(0.3f), Color.Gray.copy(0.3f)))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (busy) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    if (!extractMode) t["hideBtn"]!! else t["extBtn"]!!,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    // Status Card
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(cardBorder, cardBorder)))
                    ) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(t["status"]!!, fontWeight = FontWeight.SemiBold, color = textColor, fontSize = 14.sp)
                            Text(status, color = Purple400, fontSize = 14.sp)

                            if (lastSavedUri != null) {
                                Spacer(Modifier.height(4.dp))
                                Divider(color = cardBorder)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    lastSavedName,
                                    color = AccentPink,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.clickable {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                setData(lastSavedUri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            ctx.startActivity(Intent.createChooser(intent, "Open"))
                                        } catch (_: Exception) {}
                                    }
                                )
                            }
                        }
                    }

                    // Footer
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            t["copyright"]!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = subtextColor,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "github.com/Alvandcode",
                            style = MaterialTheme.typography.bodySmall,
                            color = Purple400,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                try {
                                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Alvandcode")))
                                } catch (_: Exception) {}
                            }
                        )
                    }
                }
            }
        }
    }
}
