import subprocess
import time

ADB_PATH = r"C:\Users\mdeangelis\AppData\Local\Android\Sdk\platform-tools\adb.exe"
DEVICE = "RFCX30T8WBL"

def adb_shell(cmd):
    try:
        res = subprocess.run([ADB_PATH, "-s", DEVICE, "shell", cmd],
                             capture_output=True, text=True, errors="replace", timeout=60)
        return res.stdout.strip()
    except Exception as e:
        return f"Error: {e}"

print("=== STARTING CLEANUP AND DISK OPTIMIZATION ===")

# Check initial free space
print("\n[1/7] INITIAL DISK USAGE:")
print(adb_shell("df -h /data /storage/emulated/0"))

# Step 1: WhatsApp Sent Duplicates (5.6GB Video + 3.0GB Images)
print("\n[2/7] CLEANING WHATSAPP SENT DUPLICATES...")
wa_cmd = """
rm -rf "/sdcard/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Video/Sent"/*
rm -rf "/sdcard/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images/Sent"/*
touch "/sdcard/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Video/Sent/.nomedia"
touch "/sdcard/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images/Sent/.nomedia"
"""
print(adb_shell(wa_cmd))
print("WhatsApp Sent folders cleared.")

# Step 2: Telegram & AyuGram Cache
print("\n[3/7] CLEANING TELEGRAM & AYUGRAM CACHE...")
tg_cmd = """
rm -rf /sdcard/Android/data/org.telegram.messenger/cache/*
rm -rf /sdcard/Android/data/org.telegram.messenger/files/Telegram/*
rm -rf /sdcard/Android/data/com.radolyn.ayugram/cache/*
rm -rf /sdcard/Android/data/com.radolyn.ayugram/files/*
rm -rf "/sdcard/Download/AyuGram/Saved Attachments"/*
"""
print(adb_shell(tg_cmd))
print("Telegram & AyuGram caches cleared.")

# Step 3: Gallery Thumbnails Cache
print("\n[4/7] CLEANING THUMBNAIL CACHE...")
thumb_cmd = """
rm -rf /sdcard/Pictures/.thumbnails/*
rm -rf /sdcard/DCIM/.thumbnails/*
"""
print(adb_shell(thumb_cmd))
print("Thumbnail caches cleared.")

# Step 4: Obsolete APK Installers in Download
print("\n[5/7] CLEANING OBSOLETE APK INSTALLERS IN DOWNLOAD...")
apk_cmd = """
find /sdcard/Download -maxdepth 2 -name "*.apk" -delete
rm -f "/sdcard/Download/.pending-"*
rm -f "/sdcard/Download/comet_latest_intel.exe"
"""
print(adb_shell(apk_cmd))
print("Obsolete APK installers and temporary files removed.")

# Step 5: Root /sdcard scratch XMLs and /data/local/tmp junk
print("\n[6/7] CLEANING TEMP FILES AND LOGS...")
temp_cmd = """
rm -f /sdcard/p.xml /sdcard/ui.xml /sdcard/u.xml
rm -f /data/local/tmp/app.apk
rm -f /data/local/tmp/*.dex
rm -f /data/local/tmp/*.json
rm -f /data/local/tmp/*.html
rm -f /data/local/tmp/*.js
rm -f /data/local/tmp/uidump.xml
"""
print(adb_shell(temp_cmd))

# Clear App Caches via pm trim-caches
print("\n[7/7] TRIMMING APPLICATION SYSTEM CACHES (pm trim-caches)...")
trim_result = adb_shell("pm trim-caches 999999999999")
print("pm trim-caches result:", trim_result)

# Final disk usage after cleanup
print("\n=== FINAL DISK USAGE AFTER CLEANUP ===")
print(adb_shell("df -h /data /storage/emulated/0"))

# Hardware Optimization: FSTRIM & Idle Maintenance
print("\n=== RUNNING HARDWARE FSTRIM (UFS 4.0 FLASH OPTIMIZATION) ===")
print("Executing sm fstrim...")
fstrim_res = adb_shell("sm fstrim")
print("FSTRIM output:", fstrim_res)

print("Triggering sm idle-maint run...")
idle_res = adb_shell("sm idle-maint run")
print("Idle Maintenance output:", idle_res)

print("\n=== ALL CLEANUP AND OPTIMIZATIONS COMPLETED ===")
