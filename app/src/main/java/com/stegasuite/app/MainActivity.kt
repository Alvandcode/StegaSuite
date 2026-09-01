package com.stegasuite.app

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
        var extractMode by remember { mutableStateOf(false) }
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var payloadUri by remember { mutableStateOf<Uri?>(null) }
        var password by remember { mutableStateOf("") }
        var status by remember { mutableStateOf("Ready") }
        var busy by remember { mutableStateOf(false) }

        val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
            imageUri = it
        }
        val pickPayload = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
            payloadUri = it
        }
        val saveFile = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
            if (uri != null && busy) {
                lifecycleScope.launch {
                    try {
                        val data = withContext(Dispatchers.IO) {
                            val bmp = contentResolver.openInputStream(imageUri!!).use { BitmapFactory.decodeStream(it) }
                            requireNotNull(bmp) { "Could not read image." }
                            PngSteganography.extract(bmp, password.ifEmpty { null })
                        }
                        withContext(Dispatchers.IO) {
                            contentResolver.openOutputStream(uri).use { it!!.write(data) }
                        }
                        status = "Extracted ${data.size} bytes successfully."
                    } catch (e: Exception) {
                        status = "Error: ${e.message}"
                    } finally { busy = false }
                }
            }
        }
        val savePng = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri ->
            if (uri != null && busy) {
                lifecycleScope.launch {
                    try {
                        val result = withContext(Dispatchers.IO) {
                            val input = contentResolver.openInputStream(imageUri!!).use { BitmapFactory.decodeStream(it) }
                            requireNotNull(input) { "Could not read PNG." }
                            val data = contentResolver.openInputStream(payloadUri!!).use { it!!.readBytes() }
                            PngSteganography.hide(input, data, password.ifEmpty { null })
                        }
                        withContext(Dispatchers.IO) {
                            contentResolver.openOutputStream(uri).use { result.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it!!) }
                        }
                        status = "Hidden successfully."
                    } catch (e: Exception) {
                        status = "Error: ${e.message}"
                    } finally { busy = false }
                }
            }
        }

        MaterialTheme {
            Surface(Modifier.fillMaxSize()) {
                Column(
                    Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())
                ) {
                    Text("StegaSuite", style = MaterialTheme.typography.headlineLarge)
                    Text("Secure data hiding", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(!extractMode, { extractMode = false }, label = { Text("Hide") })
                        FilterChip(extractMode, { extractMode = true }, label = { Text("Extract") })
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { pickImage.launch("image/png") },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (imageUri == null) "Choose PNG image" else "PNG selected") }

                    if (!extractMode) {
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { pickPayload.launch("*/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (payloadUri == null) "Choose file to hide" else "Payload selected") }
                    }

                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(16.dp))

                    if (!extractMode) {
                        Button(
                            enabled = imageUri != null && payloadUri != null && !busy,
                            onClick = { busy = true; savePng.launch("stegasuite.png") },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (busy) "Working..." else "Hide file") }
                    } else {
                        Button(
                            enabled = imageUri != null && !busy,
                            onClick = { busy = true; saveFile.launch("recovered_file") },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (busy) "Working..." else "Extract file") }
                    }

                    Spacer(Modifier.height(18.dp))
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Status", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            Text(status)
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(
                        "All processing happens locally on the phone. PNG is used because JPEG compression can destroy hidden LSB data.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
