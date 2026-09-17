/**
 * StegaSuite - MainActivity
 * © Designed by alvandcode - https://github.com/Alvandcode
 * Modern multi-theme steganography app with glassmorphism
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ── Theme System ──
internal data class AppTheme(
    val bg: Color, val text: Color, val subtext: Color,
    val cardBg: Color, val cardBorder: Color,
    val accentStart: Color, val accentEnd: Color, val accent: Color,
    val label: String, val labelEn: String, val dark: Boolean
)

private val themes = listOf(
    AppTheme(Color(0xFF1A1025), Color.White, Color.White.copy(0.6f), Color.White.copy(0.06f), Color.White.copy(0.08f), Color(0xFFAB47BC), Color(0xFFE91E63), Color(0xFF9C27B0), "بنفش", "Purple", true),
    AppTheme(Color(0xFF0A0E1A), Color.White, Color.White.copy(0.6f), Color.White.copy(0.06f), Color.White.copy(0.08f), Color(0xFF00BCD4), Color(0xFF2196F3), Color(0xFF00ACC1), "آبی", "Blue", true),
    AppTheme(Color(0xFF0F0A0A), Color.White, Color.White.copy(0.6f), Color.White.copy(0.06f), Color.White.copy(0.08f), Color(0xFFFF1744), Color(0xFFFF6D00), Color(0xFFD50000), "قرمز", "Red", true),
    AppTheme(Color(0xFFFFF0F3), Color(0xFF2D1B2E), Color(0xFF666666), Color.White, Color(0xFFE8D0D8), Color(0xFFE91E63), Color(0xFFFF80AB), Color(0xFFC2185B), "صورتی", "Rose", false),
    AppTheme(Color(0xFFF0FFF4), Color(0xFF1B2D20), Color(0xFF666666), Color.White, Color(0xFFC8E6C9), Color(0xFF26A69A), Color(0xFF80CBC4), Color(0xFF00897B), "سبز", "Mint", false),
    AppTheme(Color(0xFFF3F0FF), Color(0xFF1B1B2D), Color(0xFF666666), Color.White, Color(0xFFD1C4E9), Color(0xFF7C4DFF), Color(0xFFB388FF), Color(0xFF651FFF), "بنفش روشن", "Lavender", false)
)

// ── Language System ──
private val languages = listOf("fa", "ar", "en", "ru", "zh")
private val langLabels = mapOf("fa" to "فارسی", "ar" to "العربیه", "en" to "English", "ru" to "Русский", "zh" to "中文")
private val langShort = mapOf("fa" to "فا", "ar" to "ع", "en" to "EN", "ru" to "РУ", "zh" to "中")
private val rtlLangs = setOf("fa", "ar")

// ── Translations ──
private val tr = mapOf(
    "fa" to mapOf(
        "title" to "استگانوسویت", "subtitle" to "مخفی‌سازی امن فایل‌ها",
        "hide" to "مخفی کردن", "extract" to "استخراج",
        "selectCarrier" to "انتخاب فایل حامل", "selectFile" to "انتخاب فایل مخفی",
        "password" to "رمز عبور", "passwordHint" to "اختیاری - برای رمزنگاری",
        "hideBtn" to "مخفی کن و ذخیره کن", "extBtn" to "استخراج فایل مخفی",
        "status" to "وضعیت", "ready" to "آماده",
        "hiding" to "در حال مخفی‌سازی...", "extracting" to "در حال استخراج...",
        "saved" to "ذخیره شد", "extracted" to "استخراج شد", "error" to "خطا",
        "supported" to "همه فرمت‌ها: متن، عکس، صوت، ویدیو، PDF, ZIP...",
        "warning" to "فرمت‌های فشرده ممکنه داده رو خراب کنن",
        "copyright" to "طراحی و اجرا توسط alvandcode",
        "settings" to "تنظیمات", "about" to "درباره", "support" to "حمایت", "contact" to "تماس",
        "language" to "زبان", "theme" to "تم", "darkMode" to "حالت تیره",
        "aboutTitle" to "درباره استگانوسویت", "aboutDesc" to "استگانوسویت یک ابزار امن مخفی‌سازی فایل است که داده‌های شما را درون فایل‌های دیگر (عکس، صوت، ویدیو، PDF و...) پنهان می‌کند.",
        "aboutFeatures" to "ویژگی‌های کلیدی:", "aboutF1" to "رمزنگاری AES-256-GCM با PBKDF2 (600K iteration)",
        "aboutF2" to "پشتیبانی از تمام فرمت‌ها به عنوان حامل", "aboutF3" to "رابط کاربری مدرن با ۶ تم و ۵ زبان",
        "aboutF4" to "بدون نیاز به اینترنت - کاملاً آفلاین", "aboutTutorial" to "آموزش کامل: ",
        "supportTitle" to "حمایت از پروژه", "supportStar" to "اگر از برنامه خوشتون اومده، ستاره بدید!",
        "supportStarBtn" to "ستاره دادن در GitHub", "supportCrypto" to "حمایت مالی با ارز دیجیتال",
        "supportWallet" to "آدرس کیف پول (TON):", "supportCopy" to "کپی آدرس",
        "supportConnect" to "اتصال به کیف پول",
        "contactTitle" to "تماس با ما", "contactGithub" to "GitHub", "contactTelegram" to "کانال تلگرام",
        "contactWebsite" to "وبسایت",
        "mode" to "حالت", "carrierInfo" to "اطلاعات فایل حامل", "capacity" to "ظرفیت",
        "fileName" to "نام فایل", "size" to "حجم",
        "navHome" to "خانه", "navSettings" to "تنظیمات", "navAbout" to "درباره", "navSupport" to "حمایت", "navContact" to "تماس"
    ),
    "ar" to mapOf(
        "title" to "ستيغاسويت", "subtitle" to "إخفاء الملفات بأمان",
        "hide" to "إخفاء", "extract" to "استخراج",
        "selectCarrier" to "اختر ملف الحامل", "selectFile" to "اختر الملف المخفي",
        "password" to "كلمة المرور", "passwordHint" to "اختياري - للتشفير",
        "hideBtn" to "إخفاء وحفظ", "extBtn" to "استخراج الملف المخفي",
        "status" to "الحالة", "ready" to "جاهز",
        "hiding" to "جارٍ الإخفاء...", "extracting" to "جارٍ الاستخراج...",
        "saved" to "تم الحفظ", "extracted" to "تم الاستخراج", "error" to "خطأ",
        "supported" to "جميع التنسيقات: نص، صورة، صوت، فيديو، PDF, ZIP...",
        "warning" to "التنسيقات المضغوطة قد تُتلف البيانات",
        "copyright" to "تصميم بواسطة alvandcode",
        "settings" to "الإعدادات", "about" to "حول", "support" to "الدعم", "contact" to "اتصال",
        "language" to "اللغة", "theme" to "السمة", "darkMode" to "الوضع الداكن",
        "aboutTitle" to "حول ستيغاسويت", "aboutDesc" to "ستيغاسويت أداة آمنة لإخفاء الملفات تقوم بإخفاء بياناتك داخل ملفات أخرى (صور، صوت، فيديو، PDF...).",
        "aboutFeatures" to "الميزات الرئيسية:", "aboutF1" to "تشفير AES-256-GCM مع PBKDF2 (600K)",
        "aboutF2" to "دعم جميعتنسيقات كحامل", "aboutF3" to "واجهة عصرية مع 6 سمات و 5 لغات",
        "aboutF4" to "يعمل بدون إنترنت - تماماً أوفلاين", "aboutTutorial" to "الدليل الكامل: ",
        "supportTitle" to "ادعم المشروع", "supportStar" to "إذا أعجبك التطبيق، امنحه نجمة!",
        "supportStarBtn" to "نجمة على GitHub", "supportCrypto" to "الدعم المالي بالعملات الرقمية",
        "supportWallet" to "عنوان المحفظة (TON):", "supportCopy" to "نسخ العنوان",
        "supportConnect" to "الاتصال بالمحفظة",
        "contactTitle" to "اتصل بنا", "contactGithub" to "GitHub", "contactTelegram" to "قناة تيليجرام",
        "contactWebsite" to "الموقع الإلكتروني",
        "mode" to "الوضع", "carrierInfo" to "معلومات الحامل", "capacity" to "السعة",
        "fileName" to "اسم الملف", "size" to "الحجم",
        "navHome" to "الرئيسية", "navSettings" to "الإعدادات", "navAbout" to "حول", "navSupport" to "الدعم", "navContact" to "اتصال"
    ),
    "en" to mapOf(
        "title" to "StegaSuite", "subtitle" to "Secure File Steganography",
        "hide" to "Hide", "extract" to "Extract",
        "selectCarrier" to "Select carrier file", "selectFile" to "Select file to hide",
        "password" to "Password", "passwordHint" to "Optional - for encryption",
        "hideBtn" to "Hide & Save", "extBtn" to "Extract Hidden File",
        "status" to "Status", "ready" to "Ready",
        "hiding" to "Hiding...", "extracting" to "Extracting...",
        "saved" to "Saved", "extracted" to "Extracted", "error" to "Error",
        "supported" to "All formats: text, images, audio, video, PDF, ZIP...",
        "warning" to "Lossy formats may corrupt data",
        "copyright" to "Designed by alvandcode",
        "settings" to "Settings", "about" to "About", "support" to "Support", "contact" to "Contact",
        "language" to "Language", "theme" to "Theme", "darkMode" to "Dark Mode",
        "aboutTitle" to "About StegaSuite", "aboutDesc" to "StegaSuite is a secure file steganography tool that hides your data inside other files (images, audio, video, PDF and more).",
        "aboutFeatures" to "Key Features:", "aboutF1" to "AES-256-GCM encryption with PBKDF2 (600K iterations)",
        "aboutF2" to "Supports all formats as carrier", "aboutF3" to "Modern UI with 6 themes and 5 languages",
        "aboutF4" to "Fully offline - no internet required", "aboutTutorial" to "Full tutorial: ",
        "supportTitle" to "Support the Project", "supportStar" to "If you like the app, give it a star!",
        "supportStarBtn" to "Star on GitHub", "supportCrypto" to "Crypto Donation",
        "supportWallet" to "Wallet Address (TON):", "supportCopy" to "Copy Address",
        "supportConnect" to "Connect Wallet",
        "contactTitle" to "Contact Us", "contactGithub" to "GitHub", "contactTelegram" to "Telegram Channel",
        "contactWebsite" to "Website",
        "mode" to "Mode", "carrierInfo" to "Carrier Info", "capacity" to "Capacity",
        "fileName" to "File Name", "size" to "Size",
        "navHome" to "Home", "navSettings" to "Settings", "navAbout" to "About", "navSupport" to "Support", "navContact" to "Contact"
    ),
    "ru" to mapOf(
        "title" to "StegaSuite", "subtitle" to "Безопасная стеганография файлов",
        "hide" to "Скрыть", "extract" to "Извлечь",
        "selectCarrier" to "Выберите файл-носитель", "selectFile" to "Выберите файл для скрытия",
        "password" to "Пароль", "passwordHint" to "Необязательно - для шифрования",
        "hideBtn" to "Скрыть и сохранить", "extBtn" to "Извлечь скрытый файл",
        "status" to "Статус", "ready" to "Готово",
        "hiding" to "Скрытие...", "extracting" to "Извлечение...",
        "saved" to "Сохранено", "extracted" to "Извлечено", "error" to "Ошибка",
        "supported" to "Все форматы: текст, изображения, аудио, видео, PDF, ZIP...",
        "warning" to "Сжатые форматы могут повредить данные",
        "copyright" to "Разработано alvandcode",
        "settings" to "Настройки", "about" to "О нас", "support" to "Поддержка", "contact" to "Контакты",
        "language" to "Язык", "theme" to "Тема", "darkMode" to "Тёмный режим",
        "aboutTitle" to "О StegaSuite", "aboutDesc" to "StegaSuite - это безопасный инструмент стеганографии, который скрывает ваши данные внутри других файлов (изображения, аудио, видео, PDF и др.).",
        "aboutFeatures" to "Ключевые особенности:", "aboutF1" to "Шифрование AES-256-GCM с PBKDF2 (600K итераций)",
        "aboutF2" to "Поддержка всех форматов как носитель", "aboutF3" to "Современный интерфейс с 6 темами и 5 языками",
        "aboutF4" to "Полностью офлайн - не требует интернета", "aboutTutorial" to "Полное руководство: ",
        "supportTitle" to "Поддержите проект", "supportStar" to "Нравится приложение? Поставьте звезду!",
        "supportStarBtn" to "Звезда на GitHub", "supportCrypto" to "Крипто пожертвование",
        "supportWallet" to "Адрес кошелька (TON):", "supportCopy" to "Копировать адрес",
        "supportConnect" to "Подключить кошелёк",
        "contactTitle" to "Связаться с нами", "contactGithub" to "GitHub", "contactTelegram" to "Канал Telegram",
        "contactWebsite" to "Сайт",
        "mode" to "Режим", "carrierInfo" to "Информация о носителе", "capacity" to "Ёмкость",
        "fileName" to "Имя файла", "size" to "Размер",
        "navHome" to "Главная", "navSettings" to "Настройки", "navAbout" to "О нас", "navSupport" to "Поддержка", "navContact" to "Контакты"
    ),
    "zh" to mapOf(
        "title" to "StegaSuite", "subtitle" to "安全文件隐写术",
        "hide" to "隐藏", "extract" to "提取",
        "selectCarrier" to "选择载体文件", "selectFile" to "选择要隐藏的文件",
        "password" to "密码", "passwordHint" to "可选 - 用于加密",
        "hideBtn" to "隐藏并保存", "extBtn" to "提取隐藏文件",
        "status" to "状态", "ready" to "就绪",
        "hiding" to "正在隐藏...", "extracting" to "正在提取...",
        "saved" to "已保存", "extracted" to "已提取", "error" to "错误",
        "supported" to "所有格式：文本、图片、音频、视频、PDF、ZIP...",
        "warning" to "有损格式可能会损坏数据",
        "copyright" to "由 alvandcode 设计",
        "settings" to "设置", "about" to "关于", "support" to "支持", "contact" to "联系",
        "language" to "语言", "theme" to "主题", "darkMode" to "深色模式",
        "aboutTitle" to "关于 StegaSuite", "aboutDesc" to "StegaSuite 是一款安全的文件隐写工具，可以将您的数据隐藏在其他文件中（图片、音频、视频、PDF等）。",
        "aboutFeatures" to "主要特点:", "aboutF1" to "AES-256-GCM 加密配合 PBKDF2（600K 次迭代）",
        "aboutF2" to "支持所有格式作为载体", "aboutF3" to "现代界面，6种主题和5种语言",
        "aboutF4" to "完全离线 - 无需网络", "aboutTutorial" to "完整教程: ",
        "supportTitle" to "支持项目", "supportStar" to "喜欢这个应用？给个星标吧！",
        "supportStarBtn" to "在 GitHub 上标星", "supportCrypto" to "加密货币捐赠",
        "supportWallet" to "钱包地址 (TON):", "supportCopy" to "复制地址",
        "supportConnect" to "连接钱包",
        "contactTitle" to "联系我们", "contactGithub" to "GitHub", "contactTelegram" to "Telegram 频道",
        "contactWebsite" to "网站",
        "mode" to "模式", "carrierInfo" to "载体信息", "capacity" to "容量",
        "fileName" to "文件名", "size" to "大小",
        "navHome" to "首页", "navSettings" to "设置", "navAbout" to "关于", "navSupport" to "支持", "navContact" to "联系"
    )
)

private const val WALLET_ADDRESS = "UQCB9rzvwmq0FJDaBkHVdBgbfZPb06FWdKco3woAHH6AXuUt"
private const val TUTORIAL_URL = "https://alvandcode.github.io/StegaSuite/tutorial.html"
private const val GITHUB_URL = "https://github.com/Alvandcode/StegaSuite"
private const val TELEGRAM_URL = "https://t.me/alvandcode"
private const val WEBSITE_URL = "https://alvandcode.github.io/StegaSuite"

class MainActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContent { App() }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun App() {
        val ctx = LocalContext.current
        var langIndex by remember { mutableIntStateOf(0) }
        val lang = languages[langIndex]
        var themeIndex by remember { mutableIntStateOf(0) }
        val theme = themes[themeIndex]
        val t = tr[lang]!!
        val dir = if (lang in rtlLangs) LayoutDirection.Rtl else LayoutDirection.Ltr
        var currentPage by remember { mutableStateOf("main") }
        var menuExpanded by remember { mutableStateOf(false) }

        CompositionLocalProvider(LocalLayoutDirection provides dir) {
            Surface(modifier = Modifier.fillMaxSize(), color = theme.bg) {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Top Navigation ──
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            t["title"]!!,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = theme.accent,
                            fontSize = 22.sp,
                            modifier = Modifier.clickable { currentPage = "main" }
                        )
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = null, tint = theme.text, modifier = Modifier.size(24.dp))
                            }
                            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, containerColor = theme.bg) {
                                DropdownMenuItem(
                                    text = { Text(t["navHome"] ?: "خانه", color = theme.text) },
                                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = theme.accent) },
                                    onClick = { currentPage = "main"; menuExpanded = false }
                                )
                                HorizontalDivider(color = theme.cardBorder)
                                listOf(
                                    Triple("settings", t["navSettings"] ?: "تنظیمات", Icons.Default.Settings),
                                    Triple("about", t["navAbout"] ?: "درباره", Icons.Default.Info),
                                    Triple("support", t["navSupport"] ?: "حمایت", Icons.Default.Favorite),
                                    Triple("contact", t["navContact"] ?: "تماس", Icons.Default.Email)
                                ).forEach { (page, label, icon) ->
                                    DropdownMenuItem(
                                        text = { Text(label, color = theme.text) },
                                        leadingIcon = { Icon(icon, contentDescription = null, tint = theme.accent) },
                                        onClick = { currentPage = page; menuExpanded = false }
                                    )
                                }
                            }
                        }
                    }

                    when (currentPage) {
                        "main" -> MainPage(t, theme, lang, langIndex, { langIndex = it }, themeIndex, { themeIndex = it }, ctx)
                        "settings" -> SettingsPage(t, theme, lang, langIndex, { langIndex = it }, themeIndex, { themeIndex = it })
                        "about" -> AboutPage(t, theme)
                        "support" -> SupportPage(t, theme, ctx)
                        "contact" -> ContactPage(t, theme, ctx)
                    }

                    // Footer
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(t["copyright"]!!, style = MaterialTheme.typography.bodySmall, color = theme.subtext, textAlign = TextAlign.Center)
                        Text("github.com/Alvandcode", style = MaterialTheme.typography.bodySmall, color = theme.accent, fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL))) } catch (_: Exception) {} })
                    }
                }
            }
        }
    }

    // ── Main Page (Hide/Extract) ──
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainPage(t: Map<String, String>, theme: AppTheme, lang: String, langIndex: Int, setLang: (Int) -> Unit, themeIndex: Int, setTheme: (Int) -> Unit, ctx: android.content.Context) {
        var extractMode by remember { mutableStateOf(false) }
        var carrierUri by remember { mutableStateOf<Uri?>(null) }
        var carrierInfo by remember { mutableStateOf("") }
        var carrierType by remember { mutableStateOf<CarrierType>(CarrierType.UNKNOWN) }
        var carrierData by remember { mutableStateOf<ByteArray?>(null) }
        var payloadUri by remember { mutableStateOf<Uri?>(null) }
        var payloadInfo by remember { mutableStateOf("") }
        var pass by remember { mutableStateOf("") }
        var showPass by remember { mutableStateOf(false) }
        var status by remember { mutableStateOf(t["ready"]!!) }
        var busy by remember { mutableStateOf(false) }
        var lastSavedUri by remember { mutableStateOf<Uri?>(null) }
        var lastSavedName by remember { mutableStateOf("") }
        var pendingHide by remember { mutableStateOf<Triple<ByteArray, Uri, String>?>(null) }
        var pendingExtractResult by remember { mutableStateOf<Pair<ByteArray, String>?>(null) }

        fun getName(u: Uri): String {
            var n = "file"
            ctx.contentResolver.query(u, null, null, null, null)?.use { c ->
                if (c.moveToFirst()) { val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME); if (i >= 0) n = c.getString(i) ?: n }
            }
            return n
        }
        fun getSize(u: Uri): Long {
            var s = 0L
            ctx.contentResolver.query(u, null, null, null, null)?.use { c ->
                if (c.moveToFirst()) { val i = c.getColumnIndex(OpenableColumns.SIZE); if (i >= 0) s = c.getLong(i) }
            }
            if (s == 0L) try { ctx.contentResolver.openInputStream(u)?.use { s = it.available().toLong() } } catch (_: Exception) {}
            return s
        }
        fun toStName(orig: String): String { val dot = orig.lastIndexOf('.'); return if (dot == -1) "$orig(st)" else orig.substring(0, dot) + "(st)" + orig.substring(dot) }
        fun getCarrierTypeLabel(type: CarrierType): String {
            val labels = mapOf("fa" to mapOf("img" to "عکس", "audio" to "صوت", "file" to "فایل"), "ar" to mapOf("img" to "صورة", "audio" to "صوت", "file" to "ملف"), "en" to mapOf("img" to "Image", "audio" to "Audio", "file" to "File"), "ru" to mapOf("img" to "Изображение", "audio" to "Аудио", "file" to "Файл"), "zh" to mapOf("img" to "图片", "audio" to "音频", "file" to "文件"))
            val key = when (type) { CarrierType.PNG, CarrierType.BMP, CarrierType.TIFF, CarrierType.WEBP -> "img"; CarrierType.WAV -> "audio"; else -> "file" }
            return labels[lang]?.get(key) ?: "File"
        }

        val pickCarrier = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { u ->
            carrierUri = u; carrierData = null; carrierType = CarrierType.UNKNOWN
            if (u != null) { try { val name = getName(u); val size = getSize(u); val data = ctx.contentResolver.openInputStream(u)?.use { it.readBytes() }; if (data != null) { carrierData = data; carrierType = PngSteganography.detectCarrierType(name, data); val cap = PngSteganography.capacityBytesForType(carrierType, data); carrierInfo = "${getCarrierTypeLabel(carrierType)} \u2022 ${size / 1024}KB \u2022 ${t["capacity"]}: ~${cap / 1024}KB"; status = carrierInfo } } catch (_: Exception) {} }
        }
        val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { u ->
            payloadUri = u; if (u != null) { payloadInfo = "${getName(u)} \u2022 ${getSize(u) / 1024}KB"; status = payloadInfo }
        }
        val saveCarrier = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
            val p = pendingHide ?: return@rememberLauncherForActivityResult; if (uri == null) { busy = false; pendingHide = null; return@rememberLauncherForActivityResult }
            (ctx as? ComponentActivity)?.lifecycleScope?.launch {
                try { status = t["hiding"]!!; val ct = PngSteganography.detectCarrierType(getName(carrierUri!!), p.first); val payloadBytes = withContext(Dispatchers.IO) { ctx.contentResolver.openInputStream(p.second)?.use { it.readBytes() } ?: error("file read error") }; val result = withContext(Dispatchers.IO) { if (ct == CarrierType.PNG || ct == CarrierType.BMP) { val bmp = BitmapFactory.decodeByteArray(p.first, 0, p.first.size) ?: error("Cannot decode image"); val out = PngSteganography.hide(bmp, payloadBytes, getName(p.second), p.third.ifEmpty { null }); val baos = java.io.ByteArrayOutputStream(); out.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, baos); bmp.recycle(); out.recycle(); baos.toByteArray() } else { PngSteganography.hideGeneric(p.first, payloadBytes, getName(p.second), p.third.ifEmpty { null }, ct) } }; withContext(Dispatchers.IO) { ctx.contentResolver.openOutputStream(uri)?.use { it.write(result) } }; lastSavedUri = uri; lastSavedName = toStName(getName(carrierUri!!)); status = "${t["saved"]}: $lastSavedName" } catch (e: Exception) { status = "${t["error"]}: ${e.message}" } finally { busy = false; pendingHide = null }
            }
        }
        val saveFile = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
            val result = pendingExtractResult ?: return@rememberLauncherForActivityResult; if (uri == null) { busy = false; pendingExtractResult = null; return@rememberLauncherForActivityResult }
            (ctx as? ComponentActivity)?.lifecycleScope?.launch {
                try { withContext(Dispatchers.IO) { ctx.contentResolver.openOutputStream(uri)?.use { it.write(result.first) } }; lastSavedUri = uri; lastSavedName = result.second; status = "${t["extracted"]}: ${result.second}" } catch (e: Exception) { status = "${t["error"]}: ${e.message}" } finally { busy = false; pendingExtractResult = null }
            }
        }

        // Mode Selector
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(theme.cardBg).padding(4.dp)) {
            Row(Modifier.fillMaxWidth()) {
                listOf(false to t["hide"]!!, true to t["extract"]!!).forEach { (mode, label) ->
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (extractMode == mode) Brush.horizontalGradient(listOf(theme.accentStart, theme.accentEnd)) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))).clickable { extractMode = mode }.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(if (mode) Icons.Default.LockOpen else Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }

        // Carrier Card
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = theme.cardBg), border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(theme.cardBorder, theme.cardBorder)))) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(theme.accentStart, theme.accent))), contentAlignment = Alignment.Center) { Text("1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    Text(t["selectCarrier"]!!, fontWeight = FontWeight.SemiBold, color = theme.text, fontSize = 16.sp)
                }
                Button(onClick = { pickCarrier.launch("*/*") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = theme.cardBg, contentColor = theme.accent), contentPadding = PaddingValues(vertical = 14.dp)) {
                    if (carrierUri == null) { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = theme.text); Spacer(Modifier.width(8.dp)); Text(t["selectCarrier"]!!, fontWeight = FontWeight.Medium, color = theme.text) } else { Text("\u2713 ${getCarrierTypeLabel(carrierType)} \u2022 ${getName(carrierUri!!)}", fontWeight = FontWeight.Medium, color = theme.text) }
                }
                if (carrierInfo.isNotEmpty()) Text(carrierInfo, style = MaterialTheme.typography.bodySmall, color = theme.subtext)
                if (carrierType == CarrierType.GENERIC && carrierUri != null) {
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFFFF3E0)).padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("\u26A0\uFE0F", fontSize = 14.sp); Text(t["warning"]!!, style = MaterialTheme.typography.bodySmall, color = Color(0xFFE65100), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Payload Card (hide mode only)
        if (!extractMode) {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = theme.cardBg), border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(theme.cardBorder, theme.cardBorder)))) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(theme.accentStart, theme.accentEnd))), contentAlignment = Alignment.Center) { Text("2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                        Text(t["selectFile"]!!, fontWeight = FontWeight.SemiBold, color = theme.text, fontSize = 16.sp)
                    }
                    Button(onClick = { pickFile.launch("*/*") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = theme.cardBg, contentColor = theme.accent), contentPadding = PaddingValues(vertical = 14.dp)) {
                        if (payloadUri == null) { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = theme.text); Spacer(Modifier.width(8.dp)); Text(t["selectFile"]!!, fontWeight = FontWeight.Medium, color = theme.text) } else { Text("\u2713 $payloadInfo", fontWeight = FontWeight.Medium, color = theme.text) }
                    }
                    Text(t["supported"]!!, style = MaterialTheme.typography.bodySmall, color = theme.subtext)
                }
            }
        }

        // Password
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = theme.cardBg), border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(theme.cardBorder, theme.cardBorder)))) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(theme.accentEnd, theme.accent))), contentAlignment = Alignment.Center) { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                    Text(t["password"]!!, fontWeight = FontWeight.SemiBold, color = theme.text, fontSize = 16.sp)
                }
                OutlinedTextField(value = pass, onValueChange = { pass = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text(t["passwordHint"]!!, color = theme.subtext) }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = theme.text, unfocusedTextColor = theme.text, focusedBorderColor = theme.accent, unfocusedBorderColor = theme.cardBorder, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent), visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { showPass = !showPass }) { Icon(if (showPass) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null, tint = theme.subtext) } })
            }
        }

        // Action Button
        Button(enabled = carrierUri != null && !extractMode && payloadUri != null && !busy || carrierUri != null && extractMode && !busy, onClick = {
            busy = true; if (!extractMode) { pendingHide = Triple(carrierData!!, payloadUri!!, pass); saveCarrier.launch(toStName(getName(carrierUri!!))) }
            else { (ctx as? ComponentActivity)?.lifecycleScope?.launch { try { status = t["extracting"]!!; val carrierBytes = withContext(Dispatchers.IO) { ctx.contentResolver.openInputStream(carrierUri!!)?.use { it.readBytes() } ?: error("file read error") }; val ex = withContext(Dispatchers.IO) { val ct = PngSteganography.detectCarrierType(getName(carrierUri!!), carrierBytes); PngSteganography.extractFromBytes(carrierBytes, pass.ifEmpty { null }, ct) }; pendingExtractResult = Pair(ex.bytes, ex.fileName); saveFile.launch(ex.fileName) } catch (e: Exception) { status = "${t["error"]}: ${e.message}"; busy = false } } }
        }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent), contentPadding = PaddingValues(0.dp)) {
            Box(Modifier.fillMaxSize().background(if (carrierUri != null && !busy) Brush.horizontalGradient(listOf(theme.accentStart, theme.accentEnd)) else Brush.horizontalGradient(listOf(Color.Gray.copy(0.3f), Color.Gray.copy(0.3f)))), contentAlignment = Alignment.Center) {
                if (busy) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.5.dp) else Text(if (!extractMode) t["hideBtn"]!! else t["extBtn"]!!, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // Status
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = theme.cardBg), border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(theme.cardBorder, theme.cardBorder)))) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(t["status"]!!, fontWeight = FontWeight.SemiBold, color = theme.text, fontSize = 14.sp)
                Text(status, color = theme.accent, fontSize = 14.sp)
                if (lastSavedUri != null) { Spacer(Modifier.height(4.dp)); HorizontalDivider(color = theme.cardBorder); Spacer(Modifier.height(4.dp)); Text(lastSavedName, color = theme.accent, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { try { ctx.startActivity(Intent(Intent.ACTION_VIEW).apply { setData(lastSavedUri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }) } catch (_: Exception) {} }) }
            }
        }
    }

    // ── Settings Page ──
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SettingsPage(t: Map<String, String>, theme: AppTheme, lang: String, langIndex: Int, setLang: (Int) -> Unit, themeIndex: Int, setTheme: (Int) -> Unit) {
        var langExpanded by remember { mutableStateOf(false) }
        var themeExpanded by remember { mutableStateOf(false) }

        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = theme.cardBg), border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(theme.cardBorder, theme.cardBorder)))) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(theme.accentStart, theme.accent))), contentAlignment = Alignment.Center) { Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                    Text(t["settings"]!!, fontWeight = FontWeight.SemiBold, color = theme.text, fontSize = 18.sp)
                }

                // Language Dropdown
                ExposedDropdownMenuBox(expanded = langExpanded, onExpandedChange = { langExpanded = it }) {
                    OutlinedTextField(value = langLabels[lang] ?: lang, onValueChange = {}, readOnly = true, label = { Text(t["language"]!!, color = theme.subtext) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(langExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(14.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = theme.text, unfocusedTextColor = theme.text, focusedBorderColor = theme.accent, unfocusedBorderColor = theme.cardBorder, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))
                    ExposedDropdownMenu(expanded = langExpanded, onDismissRequest = { langExpanded = false }, containerColor = theme.bg) {
                        languages.forEachIndexed { idx, code -> DropdownMenuItem(text = { Text(langLabels[code] ?: code, color = theme.text) }, onClick = { setLang(idx); langExpanded = false }) }
                    }
                }

                // Theme Dropdown
                ExposedDropdownMenuBox(expanded = themeExpanded, onExpandedChange = { themeExpanded = it }) {
                    OutlinedTextField(value = "${theme.label} (${theme.labelEn})", onValueChange = {}, readOnly = true, label = { Text(t["theme"]!!, color = theme.subtext) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(themeExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(14.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = theme.text, unfocusedTextColor = theme.text, focusedBorderColor = theme.accent, unfocusedBorderColor = theme.cardBorder, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))
                    ExposedDropdownMenu(expanded = themeExpanded, onDismissRequest = { themeExpanded = false }, containerColor = theme.bg) {
                        themes.forEachIndexed { idx, th -> DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { Box(Modifier.size(16.dp).clip(CircleShape).background(th.accent)); Text("${th.label} (${th.labelEn})", color = theme.text) } }, onClick = { setTheme(idx); themeExpanded = false }) }
                    }
                }
            }
        }
    }

    // ── About Page ──
    @Composable
    private fun AboutPage(t: Map<String, String>, theme: AppTheme) {
        val ctx = LocalContext.current
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = theme.cardBg), border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(theme.cardBorder, theme.cardBorder)))) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(t["aboutTitle"]!!, fontWeight = FontWeight.Bold, color = theme.text, fontSize = 20.sp)
                Text(t["aboutDesc"]!!, color = theme.subtext, lineHeight = 24.sp)
                Text(t["aboutFeatures"]!!, fontWeight = FontWeight.SemiBold, color = theme.accent, fontSize = 16.sp)
                listOf(t["aboutF1"]!!, t["aboutF2"]!!, t["aboutF3"]!!, t["aboutF4"]!!).forEach { feature ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("\u2713", color = theme.accent, fontWeight = FontWeight.Bold); Text(feature, color = theme.text, lineHeight = 22.sp)
                    }
                }
                HorizontalDivider(color = theme.cardBorder)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(t["aboutTutorial"]!!, color = theme.text)
                    Text(TUTORIAL_URL, color = theme.accent, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { try { (ctx as? ComponentActivity)?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(TUTORIAL_URL))) } catch (_: Exception) {} })
                }
            }
        }
    }

    // ── Support Page ──
    @Composable
    private fun SupportPage(t: Map<String, String>, theme: AppTheme, ctx: android.content.Context) {
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = theme.cardBg), border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(theme.cardBorder, theme.cardBorder)))) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(t["supportTitle"]!!, fontWeight = FontWeight.Bold, color = theme.text, fontSize = 20.sp)

                // Star
                Text(t["supportStar"]!!, color = theme.subtext, lineHeight = 22.sp)
                Button(onClick = { try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL))) } catch (_: Exception) {} }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = theme.accentStart)) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(t["supportStarBtn"]!!, color = Color.White, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = theme.cardBorder)

                // Crypto
                Text(t["supportCrypto"]!!, fontWeight = FontWeight.SemiBold, color = theme.text, fontSize = 16.sp)
                Text(t["supportWallet"]!!, color = theme.subtext)
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(theme.cardBorder.copy(alpha = 0.3f)).padding(12.dp)) {
                    Text(WALLET_ADDRESS, color = theme.accent, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable {
                        val clipboard = ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("wallet", WALLET_ADDRESS))
                    })
                }
                Button(onClick = { try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("ton://transfer/$WALLET_ADDRESS"))) } catch (_: Exception) { try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://tonkeeper.com/transfer/$WALLET_ADDRESS"))) } catch (_: Exception) {} } }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = theme.accent)) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(t["supportConnect"]!!, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // ── Contact Page ──
    @Composable
    private fun ContactPage(t: Map<String, String>, theme: AppTheme, ctx: android.content.Context) {
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = theme.cardBg), border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(theme.cardBorder, theme.cardBorder)))) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(t["contactTitle"]!!, fontWeight = FontWeight.Bold, color = theme.text, fontSize = 20.sp)

                listOf(
                    Triple(t["contactGithub"]!!, GITHUB_URL, Icons.Default.Code),
                    Triple(t["contactTelegram"]!!, TELEGRAM_URL, Icons.Default.Send),
                    Triple(t["contactWebsite"]!!, WEBSITE_URL, Icons.Default.Language)
                ).forEach { (label, url, icon) ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(theme.cardBorder.copy(alpha = 0.15f)).clickable { try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (_: Exception) {} }.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(icon, contentDescription = null, tint = theme.accent, modifier = Modifier.size(22.dp))
                        Column { Text(label, fontWeight = FontWeight.SemiBold, color = theme.text, fontSize = 15.sp); Text(url.removePrefix("https://"), color = theme.subtext, fontSize = 12.sp) }
                    }
                }
            }
        }
    }
}
