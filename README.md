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

## GitHub
Create a new empty repository on GitHub, then upload this entire project folder.

## Important
StegaSuite writes the carrier as PNG because JPEG is lossy and can destroy LSB-hidden data.

## GitHub Actions

This project includes `.github/workflows/build-apk.yml`.
Every push builds a Debug APK automatically and uploads `StegaSuite-debug-apk` under GitHub Actions → Artifacts.

See `GITHUB_GUIDE_FA.md` for simple Persian instructions.
