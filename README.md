# StegaSuite

[![Stars](https://img.shields.io/github/stars/Alvandcode/StegaSuite?style=flat-square)](https://github.com/Alvandcode/StegaSuite/stargazers) [![License](https://img.shields.io/github/license/Alvandcode/StegaSuite?style=flat-square)](./LICENSE) [![Last commit](https://img.shields.io/github/last-commit/Alvandcode/StegaSuite?style=flat-square)](https://github.com/Alvandcode/StegaSuite/commits)

> Native Android steganography app (Kotlin + Jetpack Compose) — hide any file inside PNG with AES-256, fully offline.

<div dir="rtl">

## ابزار نهان‌نگاری اندروید

اپلیکیشن اندرویدی نهان‌نگاری با کاتلین و جت‌پک کامپوز؛ مخفی‌کردن هر فایل داخل عکس PNG با رمزنگاری AES-256، کاملا آفلاین.

</div>

---

# StegaSuite Android

A native Android steganography app built with Kotlin and Jetpack Compose.

## Features
- Hide any file inside a PNG image
- Extract a hidden file
- Optional AES-256-GCM encryption
- Password-based PBKDF2 key derivation
- Capacity estimation
- Android Storage Access Framework (no broad storage permission required)
- Offline local processing

## Build in Android Studio
1. Open this folder in Android Studio.
2. Let Gradle sync.
3. Select the `app` run configuration.
4. Connect an Android phone with USB debugging enabled, or use an emulator.
5. Press Run.

## Important
StegaSuite writes the carrier as PNG because JPEG is lossy and can destroy LSB-hidden data.

## GitHub Actions

This project includes `.github/workflows/build-apk.yml`.
Every push builds a Debug APK automatically and uploads `StegaSuite-debug-apk` under GitHub Actions → Artifacts.

See `GITHUB_GUIDE_FA.md` for simple Persian instructions.

---

## Contributing / مشارکت

- EN: Issues and Pull Requests are welcome. Please see `CONTRIBUTING.md`.
- FA: برای گزارش مشکل یا پیشنهاد قابلیت جدید، لطفا ایشو یا پول‌ریکوئست ثبت کنید.

## License / لایسنس

MIT — see [LICENSE](./LICENSE).

## Contact / ارتباط

- Telegram: https://t.me/a_c_official
- Website: https://alvandcode.github.io
