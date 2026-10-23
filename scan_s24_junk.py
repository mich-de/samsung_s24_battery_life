import subprocess
import json
import re

ADB_PATH = r"C:\Users\mdeangelis\AppData\Local\Android\Sdk\platform-tools\adb.exe"
DEVICE = "RFCX30T8WBL"

def adb_shell(cmd):
    try:
        res = subprocess.run([ADB_PATH, "-s", DEVICE, "shell", cmd],
                             capture_output=True, text=True, errors="replace", timeout=30)
        return res.stdout.strip()
    except Exception as e:
        return f"Error: {e}"

print("=== 1. STORAGE SUMMARY ===")
print(adb_shell("df -h /data /storage/emulated/0"))

print("\n=== 2. THUMBNAILS & MEDIA CACHE IN /sdcard ===")
# Search for .thumbnails and .thumbdata*
thumb_cmd = """
for p in /sdcard/DCIM/.thumbnails /sdcard/Pictures/.thumbnails /sdcard/.thumbnails; do
    if [ -d "$p" ]; then
        du -sh "$p" 2>/dev/null
        ls -lh "$p" 2>/dev/null | head -n 10
    fi
done
find /sdcard/DCIM /sdcard/Pictures -name "*thumbdata*" -exec ls -lh {} + 2>/dev/null
"""
print(adb_shell(thumb_cmd))

print("\n=== 3. LARGE FILES (>50MB) IN /sdcard/Download ===")
print(adb_shell("find /sdcard/Download -type f -size +50M -exec ls -lh {} + 2>/dev/null"))

print("\n=== 4. APK INSTALLERS IN /sdcard ===")
print(adb_shell("find /sdcard/Download /sdcard -maxdepth 3 -name '*.apk' -exec ls -lh {} + 2>/dev/null"))

print("\n=== 5. WHATSAPP & TELEGRAM CACHES ===")
wa_tg_cmd = """
for d in /sdcard/Android/media/com.whatsapp/WhatsApp/Media/.Statuses \
         /sdcard/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp\\ Video/Sent \
         /sdcard/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp\\ Images/Sent \
         /sdcard/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp\\ Voice\\ Notes \
         /sdcard/Android/data/org.telegram.messenger/cache \
         /sdcard/Android/data/org.thunderdog.challegram/cache \
         /sdcard/Telegram; do
    if [ -e "$d" ]; then
        du -sh "$d" 2>/dev/null
    fi
done
"""
print(adb_shell(wa_tg_cmd))

print("\n=== 6. ANDROID /sdcard/Android/data & media CACHES ===")
data_cache_cmd = """
du -d 2 -h /sdcard/Android/data 2>/dev/null | grep -E '/cache$' | sort -hr | head -n 25
du -d 2 -h /sdcard/Android/media 2>/dev/null | sort -hr | head -n 15
"""
print(adb_shell(data_cache_cmd))

print("\n=== 7. LOGS, CRASH DUMPS, TEMP IN /data/local/tmp ===")
print(adb_shell("ls -la /data/local/tmp"))
print(adb_shell("du -sh /data/local/tmp 2>/dev/null"))

print("\n=== 8. TRASH / CESTINO ===")
trash_cmd = """
find /sdcard -maxdepth 3 -name "*trash*" -o -name "*trashed*" -o -name "*recycle*" 2>/dev/null
"""
print(adb_shell(trash_cmd))
