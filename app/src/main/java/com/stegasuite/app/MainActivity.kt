package com.stegasuite.app

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StegaApp() }
    }

    @Composable
    private fun StegaApp() {
        val context = LocalContext.current
        var extractMode by remember { mutableStateOf(false) }
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var imageInfo by remember { mutableStateOf("") }
        var payloadUri by remember { mutableStateOf<Uri?>(null) }
        var payloadInfo by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var showPass by remember { mutableStateOf(false) }
        var status by remember { mutableStateOf("آماده") }
        var busy by remember { mutableStateOf(false) }
        
        var pendingHide by remember { mutableStateOf<Triple<Uri, Uri, String>?>(null) }
        var pendingExtract by remember { mutableStateOf<Pair<Uri, String>?>(null) }

        fun getFileName(uri: Uri): String {
            var name = "file"
            context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) name = c.getString(idx) ?: name
                }
            }
            return name
        }
        fun getFileSize(uri: Uri): Long {
            var size = 0L
            context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val idx = c.getColumnIndex(OpenableColumns.SIZE)
                    if (idx >= 0) size = c.getLong(idx)
                }
            }
            if (size == 0L) try { context.contentResolver.openInputStream(uri)?.use { size = it.available().toLong() } } catch (_:Exception){}
            return size
        }

        val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            imageUri = uri
            if (uri != null) {
                try {
                    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                    val cap = if (opts.outWidth > 0) (opts.outWidth.toLong()*opts.outHeight*3/8/1024).toString()+"KB" else "?"
                    imageInfo = "${opts.outWidth}x${opts.outHeight} - ظرفیت حدود $cap"
                    status = "عکس انتخاب شد: $imageInfo"
                } catch (e:Exception) { imageInfo = ""; status = "خطا در خواندن عکس" }
            }
        }
        val pickPayload = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            payloadUri = uri
            if (uri != null) {
                val n = getFileName(uri); val s = getFileSize(uri)
                payloadInfo = "$n - ${s/1024}KB"
                status = "فایل انتخاب شد: $payloadInfo"
                // چک ظرفیت
                imageUri?.let { imgUri ->
                    try {
                        val bmp = context.contentResolver.openInputStream(imgUri)?.use { BitmapFactory.decodeStream(it) }
                        if (bmp != null) {
                            val cap = PngSteganography.capacityBytes(bmp)
                            if (s + 100 > cap) status = "هشدار: فایل بزرگتر از ظرفیت عکس است! عکس بزرگتر انتخاب کن"
                            bmp.recycle()
                        }
                    } catch (_:Exception){}
                }
            }
        }

        val savePng = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri ->
            val pending = pendingHide ?: return@rememberLauncherForActivityResult
            if (uri == null) { busy = false; pendingHide = null; status="لغو شد"; return@rememberLauncherForActivityResult }
            lifecycleScope.launch {
                try {
                    status = "در حال مخفی‌سازی..."
                    val fileName = getFileName(pending.second)
                    val result = withContext(Dispatchers.IO) {
                        val input = context.contentResolver.openInputStream(pending.first)?.use { BitmapFactory.decodeStream(it) }
                            ?: throw IllegalArgumentException("عکس خوانده نشد - فقط PNG بده")
                        val data = context.contentResolver.openInputStream(pending.second)?.use { it.readBytes() }
                            ?: throw IllegalArgumentException("فایل خوانده نشد")
                        PngSteganography.hide(input, data, fileName, pending.third.ifEmpty { null }).also { input.recycle() }
                    }
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { result.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
                            ?: throw IllegalArgumentException("ذخیره نشد")
                        result.recycle()
                    }
                    status = "موفق! عکس جدید ذخیره شد"
                } catch (e: Exception) { status = "خطا: ${e.message}" } 
                finally { busy = false; pendingHide = null }
            }
        }

        val saveFile = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
            val pending = pendingExtract ?: return@rememberLauncherForActivityResult
            if (uri == null) { busy = false; pendingExtract = null; status="لغو شد"; return@rememberLauncherForActivityResult }
            lifecycleScope.launch {
                try {
                    status = "در حال استخراج..."
                    val extracted = withContext(Dispatchers.IO) {
                        val bmp = context.contentResolver.openInputStream(pending.first)?.use { BitmapFactory.decodeStream(it) }
                            ?: throw IllegalArgumentException("عکس خوانده نشد")
                        PngSteganography.extract(bmp, pending.second.ifEmpty { null }).also { bmp.recycle() }
                    }
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { it.write(extracted.bytes) }
                    }
                    status = "استخراج شد: ${extracted.fileName} (${extracted.bytes.size/1024}KB)"
                } catch (e: Exception) { status = "خطا: ${e.message}" }
                finally { busy = false; pendingExtract = null }
            }
        }

        MaterialTheme {
            Surface(Modifier.fillMaxSize()) {
                Column(Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())) {
                    Text("StegaSuite", style = MaterialTheme.typography.headlineLarge)
                    Text("مخفی‌سازی امن داخل عکس PNG", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(!extractMode, { extractMode = false }, label = { Text("مخفی کردن") })
                        FilterChip(extractMode, { extractMode = true }, label = { Text("استخراج") })
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { pickImage.launch("image/png") }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (imageUri == null) "1- انتخاب عکس PNG" else "عکس انتخاب شد ✓")
                    }
                    if (imageInfo.isNotEmpty()) Text(imageInfo, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top=4.dp))

                    if (!extractMode) {
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = { pickPayload.launch("*/*") }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (payloadUri == null) "2- انتخاب فایل برای مخفی کردن" else "فایل انتخاب شد ✓")
                        }
                        if (payloadInfo.isNotEmpty()) Text(payloadInfo, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top=4.dp))
                    }

                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password, onValueChange = { password = it },
                        label = { Text("رمز (اختیاری ولی پیشنهادی)") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = { TextButton(onClick = { showPass = !showPass }) { Text(if(showPass) "مخفی" else "نمایش") } }
                    )
                    Spacer(Modifier.height(16.dp))

                    if (!extractMode) {
                        Button(
                            enabled = imageUri != null && payloadUri != null && !busy,
                            onClick = { busy = true; val n = getFileName(payloadUri!!); pendingHide = Triple(imageUri!!, payloadUri!!, password); savePng.launch("stego_${n}.png") },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (busy) "صبر کن..." else "3- مخفی کن و ذخیره کن") }
                    } else {
                        Button(
                            enabled = imageUri != null && !busy,
                            onClick = { busy = true; pendingExtract = Pair(imageUri!!, password); saveFile.launch("recovered_file") },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (busy) "صبر کن..." else "استخراج کن") }
                    }

                    Spacer(Modifier.height(18.dp))
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("وضعیت", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            Text(status)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("نکته: حتما عکس را PNG ذخیره کن. JPEG خرابش می‌کند. همه چیز آفلاین روی گوشی انجام می‌شود.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
