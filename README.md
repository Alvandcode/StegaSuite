# StegaSuite

[![Stars](https://img.shields.io/github/stars/Alvandcode/StegaSuite?style=flat-square&color=a855f7)](https://github.com/Alvandcode/StegaSuite/stargazers)
[![License](https://img.shields.io/github/license/Alvandcode/StegaSuite?style=flat-square)](./LICENSE)
[![Last commit](https://img.shields.io/github/last-commit/Alvandcode/StegaSuite?style=flat-square)](https://github.com/Alvandcode/StegaSuite/commits)
[![Release](https://img.shields.io/github/v/release/Alvandcode/StegaSuite?style=flat-square&color=ec4899)](https://github.com/Alvandcode/StegaSuite/releases)

<div dir="rtl">

> 🛡️ **نهان‌نگاری امن فایل‌ها** — هر فایلی را درون هر فایلی پنهان کن، با رمزنگاری AES-256.

</div>

---

<div align="center">

**Steganography** + **Security** = **StegaSuite**

.Hide any file inside any carrier · AES-256-GCM encryption · 30+ formats · Bilingual UI

[📖 **آموزش کامل استفاده از StegaSuite**](https://alvandcode.github.io/StegaSuite/tutorial.html)

</div>

---

## 📥 Download / دانلود

| Edition | Link | Notes |
|---------|------|-------|
| 🤖 **Android (this repo)** | [Android Releases](https://github.com/Alvandcode/StegaSuite/releases) | `StegaSuite-v{version}.apk` — direct install, no root needed |
| 🪟 **Windows (sister repo)** | [Windows Releases](https://github.com/Alvandcode/StegaSuite-Windows/releases) | `StegaSuite-Install-*.zip` (self-contained installer) or `StegaSuite-Portable-*.exe` (needs .NET 10 Desktop Runtime) |

> Same wire format (`SGP2`/`SGF1`/`SGA1`) — files hidden on Android extract on Windows and vice versa.

---

## 📲 Install / نصب

1. Go to [Android Releases](https://github.com/Alvandcode/StegaSuite/releases) and download the latest `StegaSuite-v{version}.apk`.
2. On your phone, allow installs from unknown sources when asked (`Settings → Security → Install unknown apps`).
3. Open the APK file and tap **Install**. No root needed.

<div dir="rtl">

1. از صفحه [Releases](https://github.com/Alvandcode/StegaSuite/releases) آخرین فایل `StegaSuite-v{version}.apk` را دانلود کنید.
2. روی گوشی اجازه «نصب از منابع ناشناس» (`Install unknown apps`) را فعال کنید.
3. فایل APK را باز کنید و **نصب** را بزنید. بدون نیاز به روت.

**نسخه اندروید مورد نیاز:** اندروید ۸٫۰ (Oreo، سطح API ‏26‏) به بالا (`minSdk = 26`، `targetSdk = 35`).

</div>

---

## 📖 What is StegaSuite?

StegaSuite is an open-source Android steganography application that hides arbitrary files inside carrier files (images, audio, video, documents, etc.) using **LSB (Least Significant Bit)** encoding. Unlike traditional encryption which only hides the *content*, Steganography hides the *existence* itself.

<div dir="rtl">

## StegaSuite چیست؟

StegaSuite یک اپلیکیشن اندرویدی متن‌باز برای مخفی‌سازی فایل‌ها درون فایل‌های دیگر است. با استفاده از تکنیک **LSB (کم‌اهمیت‌ترین بیت)**، فایل مخفی شده در پیکسل‌های تصویر، نمونه‌های صوتی یا بایت‌های فایل‌های عمومی قرار می‌گیرد. برخلاف رمزنگاری معمولی که فقط محتوا را رمز می‌کند، این ابزار **وجود خود فایل مخفی شده را هم پنهان می‌کند**.

</div>

---

## ✨ Features / امکانات

<div dir="rtl">

| ویژگی | توضیح |
|--------|--------|
| 🔐 رمزنگاری AES-256-GCM | رمزنگاری نظامی با PBKDF2 (۶۰۰,۰۰۰ تکرار) |
| 🎨 ۳۰+ فرمت پشتیبانی | PNG, BMP, TIFF, WebP, WAV, MP3, MP4, PDF, ZIP و... |
| 🧠 تشخیص خودکار فرمت | شناسایی Magic Bytes بدون نیاز به انتخاب دستی |
| 🌍 رابط دوزبانه | فارسی (RTL) و انگلیسی (LTR) با قابلیت جابجایی آنی |
| 🌙 تم تاریک و روشن | طراحی مدرن با گرادیان بنفش |
| 📊 محاسبه ظرفیت | نمایش حداکثر حجم قابل مخفی‌سازی قبل از عملیات |
| ⚡ بدون نیاز به روت | نصب مستقیم از GitHub Releases |
| 🔓 استخراج با نام اصلی | فایل استخراج شده با نام و پسوند اصلی ذخیره می‌شود |
| 📁 سازگاری معکوس | پشتیبانی از فایل‌های نسخه‌های قدیمی‌تر |

</div>

---

## 🚀 How it works / نحوه کار

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Carrier File │  +  │ Payload File │  +  │   Password   │
│  (image/audio │     │ (any format) │     │  (optional)  │
│  /video/...)  │     │              │     │              │
└──────┬───────┘     └──────┬───────┘     └──────┬───────┘
       │                    │                    │
       └────────────────────┼────────────────────┘
                            ▼
                   ┌────────────────┐
                   │  LSB Encoding  │
                   │  + AES-256-GCM │
                   └────────┬───────┘
                            ▼
                   ┌────────────────┐
                   │  Output File   │
                   │ (looks normal) │
                   └────────────────┘
```

<div dir="rtl">

### فرآیند مخفی‌سازی:
1. **فایل حامل** را انتخاب کنید (تصویر، صدا، ویدیو، سند و...)
2. **فایل مخفی شونده** را انتخاب کنید (هر فرمتی)
3. **رمز عبور** وارد کنید (اختیاری، اما پیشنهاد می‌شود)
4. روی **«مخفی‌سازی و ذخیره»** کلیک کنید
5. فایل خروجی با ظاهر عادی ذخیره می‌شود

### فرآیند استخراج:
1. به حالت **«استخراج»** بروید
2. فایل حامل را انتخاب کنید
3. رمز عبور را وارد کنید (اگر تنظیم شده بود)
4. فایل با **نام و پسوند اصلی** ذخیره می‌شود

</div>

---

## 📦 Supported Formats / فرمت‌های پشتیبانی شده

### Carrier Formats (حالت حامل)

<div dir="rtl">

| فرمت | پسوند | روش مخفی‌سازی | کیفیت |
|------|-------|--------------|-------|
| **PNG** | `.png` | LSB پیکسلی | ✅ عالی |
| **BMP** | `.bmp` | LSB پیکسلی | ✅ عالی |
| **WAV** | `.wav` | LSB نمونه صوتی | ✅ عالی |
| **TIFF** | `.tiff`, `.tif` | Generic LSB | ⚠️ معمولی |
| **WebP** | `.webp` | Generic LSB | ⚠️ Lossy |
| **MP3** | `.mp3` | Generic LSB | ⚠️ Lossy |
| **FLAC** | `.flac` | Generic LSB | ⚠️ معمولی |
| **MP4** | `.mp4` | Generic LSB | ⚠️ Lossy |
| **PDF** | `.pdf` | Generic LSB | ⚠️ معمولی |
| **ZIP** | `.zip` | Generic LSB | ⚠️ معمولی |
| **TXT** | `.txt` | Generic LSB | ✅ عالی |
| + 20 فرمت دیگر | | Generic LSB | |

</div>

### Payload Formats (فرمت‌های مخفی شونده)

**بدون محدودیت** — هر فایلی با هر پسوندی قابل مخفی‌سازی است:

<div dir="rtl">

- 📄 **اسناد:** PDF, DOCX, XLSX, PPTX, TXT, CSV, JSON, XML, Markdown
- 🖼️ **تصاویر:** PNG, JPG, GIF, BMP, SVG, WebP, TIFF
- 🎵 **صدا:** MP3, WAV, FLAC, OGG, M4A, AAC
- 🎬 **ویدیو:** MP4, AVI, MKV, MOV, WebM
- 📦 **آرشیو:** ZIP, RAR, 7Z, TAR, GZ
- 💻 **کد:** JS, PY, TS, HTML, CSS, Java, Kotlin

</div>

---

## 🔐 Encryption Details / جزئیات رمزنگاری

<div dir="rtl">

اگر رمز عبور تنظیم شده باشد، فایل مخفی شده با **AES-256-GCM** رمز می‌شود:

</div>

| Parameter | Value |
|-----------|-------|
| Algorithm | `AES/GCM/NoPadding` |
| Key Size | 256 bits |
| Key Derivation | `PBKDF2WithHmacSHA256` |
| PBKDF2 Iterations | 600,000 |
| Salt | 16 bytes (SecureRandom) |
| Nonce (IV) | 12 bytes (SecureRandom) |
| GCM Tag | 128 bits |
| AAD | `STGS` (4 bytes magic) |

### Output File Structure

```
┌─────────────────────────────────────────────────┐
│  Magic Bytes: SGP2 / SGF1 / SGA1  (4 bytes)    │
│  Payload Length                    (8 bytes)    │
│  ┌───────────────────────────────────────────┐  │
│  │  Encrypted/CRC'd Payload                 │  │
│  │  ┌─────────────────────────────────────┐  │  │
│  │  │  Filename Length      (4 bytes)     │  │  │
│  │  │  Filename (UTF-8)    (≤1024 bytes) │  │  │
│  │  │  File Content         (N bytes)     │  │  │
│  │  └─────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
```

---

## 📐 Capacity Calculation / محاسبه ظرفیت

<div dir="rtl">

| نوع حامل | فرمول | مثال |
|----------|-------|------|
| PNG/BMP | `(عرض × ارتفاع × 3) ÷ 8` | تصویر ۱۹۲۰×۱۰۸۰ ≈ ۷۷۷ KB |
| WAV | `(نمونه‌ها × کانال‌ها) ÷ 8` | صدای ۱۰ ثانیه ۴۴.۱kHz ≈ ۲۵۹ KB |
| عمومی | `(اندازه فایل - بایت امن) ÷ 8` | فایل ۱ MB ≈ ۱۲۲ KB |

</div>

---

## ⚠️ Limitations / محدودیت‌ها

<div dir="rtl">

| محدودیت | توضیح |
|---------|-------|
| حداکثر اندازه فایل مخفی | ۵۰ مگابایت |
| طول نام فایل | حداکثر ۱۰۲۴ بایت (≈ ۳۴۰ حرف فارسی) |
| فرمت‌های Lossy | JPEG و WebP ممکن است داده را خراب کنند |
| ویرایش فایل حامل | هرگونه تغییر (crop, resize, filter) داده را از بین می‌برد |
| فشرده‌سازی مجدد | ارسال از طریق تلگرام/ایمیل ممکن است فایل را فشرده کند |

</div>

<div dir="rtl">

### 💡 بهترین شیوه‌ها

</div>

- ✅ از **PNG** یا **BMP** به عنوان فایل حامل استفاده کنید
- ✅ **رمز عبور قوی** با ترکیب حروف، اعداد و نمادها انتخاب کنید
- ✅ فایل حامل را **تغییر ندهید** (crop, rename, filter نکنید)
- ✅ از فایل‌های **بزرگ‌تر** برای ظرفیت بیشتر استفاده کنید
- ❌ از **JPEG** به عنوان فایل حامل استفاده نکنید

---

## 🔧 Build / ساخت

**Prerequisites / پیش‌نیازها:**

- Android Studio (with Android SDK: platform `android-35` + `build-tools;35.0.0`)
- JDK 17
- Gradle 8.9+ (or the project's wrapper, if present)

```bash
./gradlew assembleDebug
# اگر فایل `gradlew` در checkout شما نیست: gradle assembleDebug
```

**Output APK / خروجی:**

- `app/build/outputs/apk/debug/app-debug.apk`

<div dir="rtl">

1. پوشه پروژه را در Android Studio باز کنید.
2. صبر کنید Gradle Sync تمام شود.
3. کانفیگ `app` را انتخاب کنید.
4. گوشی با USB debugging فعال یا یک Emulator وصل کنید.
5. Run را بزنید — یا برای ساخت APK از ترمینال دستور بالا را اجرا کنید.

</div>

---

## 🤖 GitHub Actions

<div dir="rtl">

این پروژه شامل فایل `.github/workflows/build-apk.yml` است. هر بار push، یک APK Debug به صورت خودکار ساخته و آپلود می‌شود.

</div>

**Available Actions:**

| Workflow | Trigger | Description |
|----------|---------|-------------|
| `build-apk.yml` | Push to any branch | Builds Debug APK, creates Release |

**Build outputs:**
- Artifacts: `StegaSuite-v{version}.apk`
- Releases: Auto-created with version tag

---

## 📁 Project Structure

```
StegaSuite/
├── app/src/main/java/com/stegasuite/app/
│   ├── MainActivity.kt          # UI layer (Jetpack Compose)
│   ├── PngSteganography.kt      # Unified steganography engine
│   ├── FileSteganography.kt     # Generic binary LSB engine
│   ├── AudioSteganography.kt    # WAV audio LSB engine
│   └── StegaCrypto.kt           # AES-256-GCM encryption
├── app/build.gradle.kts          # Build config with auto-versioning
├── docs/
│   └── tutorial.html             # Comprehensive tutorial page
└── .github/workflows/
    └── build-apk.yml             # CI/CD pipeline
```

---

## 📊 File Size Guide / راهنمای اندازه فایل

<div dir="rtl">

### چقدر فایل می‌توانید مخفی کنید؟

| اندازه فایل حامل | ظفیت مخفی‌سازی | مثال |
|-------------------|---------------|------|
| تصویر ۱ مگاپیکسل | ≈ ۳۶۶ KB | مخفی‌سازی یک سند PDF |
| تصویر ۱۲ مگاپیکسل | ≈ ۴.۳ MB | مخفی‌سازی چند عکس |
| صدای ۱ دقیقه WAV | ≈ ۱.۵ MB | مخفی‌سازی یک فایل متنی |
| فایل ۱ مگابایتی | ≈ ۱۲۲ KB | مخفی‌سازی یک فایل متنی |
| فایل ۱۰ مگابایتی | ≈ ۱.۲ MB | مخفی‌سازی چند تصویر |

</div>

---

## 🤝 Contributing / مشارکت

<div dir="rtl">

### EN
Issues and Pull Requests are welcome. Please see [`GITHUB_GUIDE_FA.md`](GITHUB_GUIDE_FA.md) (Persian) and [`UPLOAD_GUIDE.md`](UPLOAD_GUIDE.md).

### FA
برای گزارش مشکل یا پیشنهاد قابلیت جدید، لطفا ایشو یا پول‌ریکوئست ثبت کنید. راهنما: [`GITHUB_GUIDE_FA.md`](GITHUB_GUIDE_FA.md).

</div>

---

## 📜 License

MIT — see [LICENSE](./LICENSE).

---

## 📬 Contact / ارتباط

- Telegram: https://t.me/a_c_official
- Website: https://alvandcode.github.io
- GitHub: https://github.com/Alvandcode/StegaSuite

---

<div align="center">

**Made with ❤️ by [Alvandcode](https://github.com/Alvandcode)**

</div>
