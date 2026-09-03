/**
 * StegaSuite - MainActivity
 * © طراحی و اجرا توسط alvandcode - https://github.com/Alvandcode
 * اپ استگانوگرافی با تم گلس مورفیسم، دو زبانه، RTL/LTR
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// دو زبانه ساده - فارسی و انگلیسی - راست‌چین و چپ‌چین
private val fa = mapOf("title" to "استگانوسویت","hide" to "مخفی کردن","extract" to "استخراج","img" to "۱- انتخاب عکس PNG","file" to "۲- انتخاب فایل","pass" to "رمز (پیشنهادی)","hideBtn" to "مخفی کن و ذخیره","extBtn" to "استخراج کن","status" to "وضعیت","dark" to "تیره","light" to "روشن","path" to "مسیر ذخیره:","copyright" to "© طراحی و اجرا توسط alvandcode","contact" to "ارتباط با سازنده")
private val en = mapOf("title" to "StegaSuite","hide" to "Hide","extract" to "Extract","img" to "1- Choose PNG","file" to "2- Choose File","pass" to "Password (optional)","hideBtn" to "Hide & Save","extBtn" to "Extract","status" to "Status","dark" to "Dark","light" to "Light","path" to "Saved to:","copyright" to "© Designed & Developed by alvandcode","contact" to "Contact")

class MainActivity : ComponentActivity(){
 override fun onCreate(b:Bundle?){ super.onCreate(b); setContent{ App() } }
 @Composable fun App(){
  val ctx=LocalContext.current
  var isFa by remember{mutableStateOf(true)}
  var isDark by remember{mutableStateOf(true)}
  val t = if(isFa) fa else en
  val dir = if(isFa) LayoutDirection.Rtl else LayoutDirection.Ltr
  CompositionLocalProvider(LocalLayoutDirection provides dir){
   val bg = if(isDark) Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E3A8A))) else Brush.verticalGradient(listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD)))
   Box(Modifier.fillMaxSize().background(bg).padding(16.dp)){
    var extractMode by remember{mutableStateOf(false)}
    var imgUri by remember{mutableStateOf<Uri?>(null)}
    var imgInfo by remember{mutableStateOf("")}
    var payloadUri by remember{mutableStateOf<Uri?>(null)}
    var payloadInfo by remember{mutableStateOf("")}
    var pass by remember{mutableStateOf("")}
    var showPass by remember{mutableStateOf(false)}
    var status by remember{mutableStateOf(if(isFa) "آماده" else "Ready")}
    var busy by remember{mutableStateOf(false)}
    var lastSavedUri by remember{mutableStateOf<Uri?>(null)}
    var lastSavedName by remember{mutableStateOf("")}
    var pendingHide by remember{mutableStateOf<Triple<Uri,Uri,String>?>(null)}
    var pendingExtract by remember{mutableStateOf<Pair<Uri,String>?>(null)}
    fun getName(u:Uri):String{ var n="file"; ctx.contentResolver.query(u,null,null,null,null)?.use{c-> if(c.moveToFirst()){ val i=c.getColumnIndex(OpenableColumns.DISPLAY_NAME); if(i>=0) n=c.getString(i)?:n }}; return n }
    fun getSize(u:Uri):Long{ var s=0L; ctx.contentResolver.query(u,null,null,null,null)?.use{c-> if(c.moveToFirst()){ val i=c.getColumnIndex(OpenableColumns.SIZE); if(i>=0) s=c.getLong(i)}}; if(s==0L) try{ctx.contentResolver.openInputStream(u)?.use{s=it.available().toLong()}}catch(_:Exception){}; return s }
    fun toStName(orig:String):String{ val dot=orig.lastIndexOf('.'); return if(dot==-1) "$orig(st)" else orig.substring(0,dot)+"(st)"+orig.substring(dot) }

    val pickImg=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){ u-> imgUri=u; if(u!=null){ try{ val o=BitmapFactory.Options().apply{inJustDecodeBounds=true}; ctx.contentResolver.openInputStream(u)?.use{BitmapFactory.decodeStream(it,null,o)}; val cap=o.outWidth.toLong()*o.outHeight*3/8/1024; imgInfo="${o.outWidth}x${o.outHeight} - ~${cap}KB"; status=if(isFa) "عکس: $imgInfo" else "Image: $imgInfo"}catch(_:Exception){} } }
    val pickFile=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){ u-> payloadUri=u; if(u!=null){ val n=getName(u); val s=getSize(u); payloadInfo="$n - ${s/1024}KB"; status=payloadInfo } }
    val savePng=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")){ uri->
     val p=pendingHide?:return@rememberLauncherForActivityResult; if(uri==null){busy=false; pendingHide=null; return@rememberLauncherForActivityResult}
     lifecycleScope.launch{ try{ status=if(isFa) "در حال مخفی‌سازی..." else "Hiding..."; val inName=getName(p.first); val outName=toStName(inName); val res=withContext(Dispatchers.IO){ val inp=ctx.contentResolver.openInputStream(p.first)?.use{BitmapFactory.decodeStream(it)}?:error("PNG err"); val dat=ctx.contentResolver.openInputStream(p.second)?.use{it.readBytes()}?:error("file err"); PngSteganography.hide(inp,dat,getName(p.second),p.third.ifEmpty{null}).also{inp.recycle()}}; withContext(Dispatchers.IO){ ctx.contentResolver.openOutputStream(uri)?.use{res.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}; res.recycle()}; lastSavedUri=uri; lastSavedName=outName; status=if(isFa) "ذخیره شد: $outName" else "Saved: $outName"}catch(e:Exception){status="خطا: ${e.message}"}finally{busy=false; pendingHide=null}}
    }
    val saveFile=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")){ uri->
     val p=pendingExtract?:return@rememberLauncherForActivityResult; if(uri==null){busy=false; pendingExtract=null; return@rememberLauncherForActivityResult}
     lifecycleScope.launch{ try{ status=if(isFa) "در حال استخراج..." else "Extracting..."; val ex=withContext(Dispatchers.IO){ val b=ctx.contentResolver.openInputStream(p.first)?.use{BitmapFactory.decodeStream(it)}?:error("img err"); PngSteganography.extract(b,p.second.ifEmpty{null}).also{b.recycle()}}; withContext(Dispatchers.IO){ctx.contentResolver.openOutputStream(uri)?.use{it.write(ex.bytes)}}; lastSavedUri=uri; lastSavedName=ex.fileName; status=if(isFa) "استخراج شد: ${ex.fileName}" else "Extracted: ${ex.fileName}"}catch(e:Exception){status="خطا: ${e.message}"}finally{busy=false; pendingExtract=null}}
    }

    // گلس مورفیسم کانتینر
    @Composable fun GlassCard(content:@Composable ColumnScope.()->Unit){
     val cardBg = if(isDark) Color.White.copy(alpha=0.08f) else Color.White.copy(alpha=0.6f)
     val border = if(isDark) Color.White.copy(0.15f) else Color.Black.copy(0.05f)
     Card(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)), colors=CardDefaults.cardColors(containerColor=cardBg), border=androidx.compose.foundation.BorderStroke(1.dp, border)){
      Column(Modifier.padding(16.dp), content=content)
     }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement=Arrangement.spacedBy(14.dp)){
     // هدر گلس + سوییچ تم و زبان
     GlassCard{
      Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically){
       Column{ Text(t["title"]!!, style=MaterialTheme.typography.headlineMedium, fontWeight=FontWeight.Bold, color=if(isDark) Color.White else Color(0xFF0F172A)); Text(if(isFa) "مخفی‌سازی امن" else "Secure steganography", color=if(isDark) Color.White.copy(0.7f) else Color.Black.copy(0.6f)) }
       Column(verticalArrangement=Arrangement.spacedBy(6.dp), horizontalAlignment=Alignment.End){
        Row(verticalAlignment=Alignment.CenterVertically){ Text(if(isDark) t["dark"]!! else t["light"]!!, color=if(isDark) Color.White else Color.Black, modifier=Modifier.padding(end=8.dp)); Switch(checked=isDark, onCheckedChange={isDark=it}) }
        Button(onClick={isFa=!isFa}, contentPadding=PaddingValues(horizontal=12.dp, vertical=4.dp)){ Text(if(isFa) "EN" else "فا") }
       }
      }
     }

     GlassCard{
      Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
       FilterChip(selected=!extractMode, onClick={extractMode=false}, label={Text(t["hide"]!!)})
       FilterChip(selected=extractMode, onClick={extractMode=true}, label={Text(t["extract"]!!)})
      }
      Spacer(Modifier.height(12.dp))
      Button(onClick={pickImg.launch("image/png")}, modifier=Modifier.fillMaxWidth(), shape=RoundedCornerShape(14.dp)){ Text(if(imgUri==null) t["img"]!! else "✓ ${getName(imgUri!!)}") }
      if(imgInfo.isNotEmpty()) Text(imgInfo, style=MaterialTheme.typography.bodySmall, color=if(isDark) Color.White.copy(0.7f) else Color.Black.copy(0.6f))
      if(!extractMode){
       Spacer(Modifier.height(8.dp))
       Button(onClick={pickFile.launch("*/*")}, modifier=Modifier.fillMaxWidth(), shape=RoundedCornerShape(14.dp)){ Text(if(payloadUri==null) t["file"]!! else "✓ $payloadInfo") }
       Text(if(isFa) "از همه فرمت‌ها پشتیبانی می‌شود: متن، تصویر، ZIP و ..." else "Supports any file: txt, zip, pdf, ...", style=MaterialTheme.typography.bodySmall, color=if(isDark) Color.White.copy(0.5f) else Color.Black.copy(0.5f))
      }
      Spacer(Modifier.height(12.dp))
      OutlinedTextField(value=pass, onValueChange={pass=it}, label={Text(t["pass"]!!)}, modifier=Modifier.fillMaxWidth(), singleLine=true, visualTransformation=if(showPass) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon={TextButton(onClick={showPass=!showPass}){Text(if(showPass) "🙈" else "👁")}} , shape=RoundedCornerShape(14.dp))
      Spacer(Modifier.height(12.dp))
      if(!extractMode) Button(enabled=imgUri!=null&&payloadUri!=null&&!busy, onClick={busy=true; pendingHide=Triple(imgUri!!,payloadUri!!,pass); val orig=getName(imgUri!!); savePng.launch(toStName(orig))}, modifier=Modifier.fillMaxWidth(), shape=RoundedCornerShape(14.dp)){ Text(if(busy) "..." else t["hideBtn"]!!) }
      else Button(enabled=imgUri!=null&&!busy, onClick={busy=true; pendingExtract=Pair(imgUri!!,pass); saveFile.launch("recovered_file")}, modifier=Modifier.fillMaxWidth(), shape=RoundedCornerShape(14.dp)){ Text(if(busy) "..." else t["extBtn"]!!) }
     }

     GlassCard{
      Text(t["status"]!!, fontWeight=FontWeight.Bold, color=if(isDark) Color.White else Color.Black)
      Spacer(Modifier.height(6.dp))
      Text(status, color=if(isDark) Color.White.copy(0.9f) else Color.Black.copy(0.8f))
      // لینک مسیر ذخیره - کلیک باز می‌کنه و وارد پوشه/فایل میشه
      if(lastSavedUri!=null){
       Spacer(Modifier.height(8.dp))
       Text(t["path"]!!, style=MaterialTheme.typography.labelMedium, color=if(isDark) Color.Cyan.copy(0.8f) else Color(0xFF0EA5E9))
       Text(lastSavedName+"  ↗", color=Color(0xFF38BDF8), modifier=Modifier.clickable{
        try{ val intent=Intent(Intent.ACTION_VIEW).apply{ setData(lastSavedUri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)}; ctx.startActivity(Intent.createChooser(intent, "Open")) }catch(_:Exception){
         try{ ctx.startActivity(Intent(Intent.ACTION_VIEW).apply{ setDataAndType(lastSavedUri, "*/*"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)}) }catch(_:Exception){}
        }
       }, textAlign=TextAlign.Start)
       Text(lastSavedUri.toString(), style=MaterialTheme.typography.bodySmall, color=if(isDark) Color.White.copy(0.5f) else Color.Black.copy(0.5f), maxLines=2, modifier=Modifier.clickable{
        try{ val intent=Intent(Intent.ACTION_VIEW).apply{ setData(lastSavedUri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)}; ctx.startActivity(Intent.createChooser(intent, "Open")) }catch(_:Exception){}
       })
      }
     }

     // فوتر کپی‌رایت و ارتباط با سازنده - گلس مورفیسم
     GlassCard{
      Column(Modifier.fillMaxWidth(), horizontalAlignment=Alignment.CenterHorizontally){
       Text(t["copyright"]!!, style=MaterialTheme.typography.bodySmall, fontWeight=FontWeight.Medium, color=if(isDark) Color.White.copy(0.7f) else Color.Black.copy(0.6f), textAlign=TextAlign.Center)
       Spacer(Modifier.height(6.dp))
       Row(verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.Center){
        Text(t["contact"]!! + ": ", style=MaterialTheme.typography.bodySmall, color=if(isDark) Color.White.copy(0.5f) else Color.Black.copy(0.5f))
        Text("github.com/Alvandcode", color=Color(0xFF38BDF8), style=MaterialTheme.typography.bodySmall, fontWeight=FontWeight.Bold, modifier=Modifier.clickable{
         try{ val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Alvandcode")); ctx.startActivity(i) }catch(_:Exception){}
        })
       }
      }
     }
    }
   }
  }
 }
}
