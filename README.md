# QH Technology Wi-Fi

نسخه 1.0.0 — پروژه Android بازسازی‌شده بر پایه قابلیت‌هایی که در فایل‌های ارائه‌شده مشخص شده بود.

## امکانات
- اسکن Wi-Fi قابل مشاهده توسط دستگاه
- SSID / BSSID / قدرت سیگنال / فرکانس
- پایگاه‌داده محلی Room/SQLite
- ذخیره Latitude / Longitude / Altitude / دقت GPS
- به‌روزرسانی رکورد بر اساس BSSID
- جست‌وجوی SSID و BSSID
- نمایش شبکه‌های دارای مختصات روی Google Maps
- نمایش جزئیات شبکه روی Marker
- نام برنامه: QH Technology

## ساخت APK
این پروژه یک پروژه Android Studio است. برای Google Maps باید کلید API متعلق به پروژه خودتان را در `local.properties` قرار دهید:

MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY

فایل `local.properties.example` نمونه آن است.

سپس پروژه را در Android Studio باز کنید و Build > Build APK(s) را اجرا کنید.

## محدودیت واقعی Android
اسکن Wi-Fi و GPS تابع مجوزها، تنظیمات Location دستگاه و محدودیت‌های نسخه Android است. برنامه رمز عبور Wi-Fi را استخراج نمی‌کند و امنیت شبکه را دور نمی‌زند.
