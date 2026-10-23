import subprocess
import json
import re

ADB_PATH = r"C:\Users\mdeangelis\AppData\Local\Android\Sdk\platform-tools\adb.exe"
DEVICE = "RFCX30T8WBL"

def get_diskstats():
    res = subprocess.run([ADB_PATH, "-s", DEVICE, "shell", "dumpsys diskstats"],
                         capture_output=True, text=True, errors="replace")
    output = res.stdout
    
    pkg_match = re.search(r"Package Names:\s*\[(.*?)\]", output, re.DOTALL)
    app_match = re.search(r"App Data Sizes:\s*\[(.*?)\]", output, re.DOTALL)
    cache_match = re.search(r"Cache Sizes:\s*\[(.*?)\]", output, re.DOTALL)
    
    if not (pkg_match and cache_match):
        print("Could not find patterns")
        return
        
    pkgs = [p.strip() for p in pkg_match.group(1).split(",") if p.strip()]
    caches = [int(c.strip()) for c in cache_match.group(1).split(",") if c.strip()]
    app_datas = [int(a.strip()) for a in app_match.group(1).split(",") if a.strip()] if app_match else [0]*len(pkgs)
    
    results = []
    for pkg, c_size, a_size in zip(pkgs, caches, app_datas):
        results.append({
            "package": pkg,
            "cache_bytes": c_size,
            "cache_mb": round(c_size / (1024*1024), 2),
            "app_data_mb": round(a_size / (1024*1024), 2)
        })
        
    results.sort(key=lambda x: x["cache_bytes"], reverse=True)
    
    print(f"Total packages analyzed: {len(results)}")
    total_cache = sum(r['cache_bytes'] for r in results)
    print(f"Total App Cache on device: {round(total_cache / (1024*1024*1024), 2)} GB\n")
    print(f"{'Package Name':<45} | {'Cache Size':<12} | {'App Data'}")
    print("-" * 75)
    for r in results[:40]:
        if r['cache_mb'] > 10:  # only show > 10 MB
            print(f"{r['package']:<45} | {r['cache_mb']:>8} MB   | {r['app_data_mb']:>8} MB")

get_diskstats()
