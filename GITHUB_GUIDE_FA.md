# راهنمای خیلی ساده اجرای پروژه در GitHub

## 1) فایل ZIP را از حالت فشرده خارج کن

محتویات همین پروژه را داخل یک Repository در GitHub قرار بده.

مهم:
فایل `.github/workflows/build-apk.yml` هم باید داخل GitHub قرار گرفته باشد.

## 2) اگر پوشه `.github` با آپلود گوشی منتقل نشد

در GitHub برو به:

Add file → Create new file

و نام فایل را دقیقاً این بگذار:

`.github/workflows/build-apk.yml`

بعد محتوای فایل `build-apk.yml` داخل همین پروژه را کپی کن و Commit کن.

## 3) ساخت خودکار APK

بعد از هر Push یا Commit، GitHub Actions خودش شروع به ساخت APK می‌کند.

برو به:

Actions → Build Android APK

صبر کن تا علامت سبز و وضعیت موفقیت نمایش داده شود.

## 4) دریافت APK

داخل همان اجرای موفق Workflow برو پایین صفحه.

قسمت:

Artifacts

را پیدا کن.

روی:

StegaSuite-debug-apk

بزن و فایل ZIP را دریافت کن.

داخل ZIP فایل زیر قرار دارد:

app-debug.apk

این همان APK قابل نصب روی گوشی است.

## نکته

این Workflow نسخه Debug را می‌سازد و نیازی به کلید امضای شخصی ندارد.

برای انتشار در Google Play در آینده بهتر است نسخه Release/AAB و امضای دیجیتال هم اضافه شود.
