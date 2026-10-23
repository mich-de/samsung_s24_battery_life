import subprocess
import re

SERIAL = "RFCX30T8WBL"

with open(r"d:\s24app\android-app\app\src\main\java\com\s24optimizer\data\Optimizations.kt", "r", encoding="utf-8") as f:
    kt = f.read()

# Pattern for opt definitions
opt_pattern = re.compile(
    r'opt\(\s*"([^"]+)",\s*Optimization\.Category\.([A-Z_]+),\s*"([^"]+)",\s*"([^"]+)",\s*'
    r'"(?:[^"\\]|\\.)*",\s*"(?:[^"\\]|\\.)*",\s*'
    r'((?:(?:"(?:[^"\\]|\\.)*")|\s|\+)+?),\s*'
    r'((?:(?:"(?:[^"\\]|\\.)*")|\s|\+)+?)'
    r'(?:,\s*("""(?:.*?)"""|"(?:[^"\\]|\\.)*"))?',
    re.DOTALL
)

opts = []
for m in opt_pattern.finditer(kt):
    opt_id = m.group(1)
    cat = m.group(2)
    title_en = m.group(3)
    title_it = m.group(4)
    apply_raw = m.group(5)
    revert_raw = m.group(6)
    check_raw = m.group(7)
    
    # parse apply commands
    apply_str = ""
    for s in re.findall(r'"([^"\\]*)"', apply_raw):
        apply_str += s
    
    check_str = ""
    if check_raw:
        if check_raw.startswith('"""') and check_raw.endswith('"""'):
            check_str = check_raw[3:-3]
        elif check_raw.startswith('"') and check_raw.endswith('"'):
            check_str = check_raw[1:-1]
            
    opts.append({
        'id': opt_id,
        'cat': cat,
        'title_it': title_it,
        'title_en': title_en,
        'apply': [c.strip() for c in apply_str.split("; ") if c.strip()],
        'check': [c.strip() for c in check_str.split("; ") if c.strip()]
    })

print(f"Total optimizations: {len(opts)}")

# Let's test applyCommands on device or test checkCommands
print("\nTesting which optimizations have failing apply commands or checks...")
failed_apply = []
discrepancies = []

for item in opts:
    opt_id = item['id']
    apply_cmds = item['apply']
    check_cmds = item['check']
    
    # Test each apply command
    for cmd in apply_cmds:
        # Don't run dangerous or reset commands
        if any(skip in cmd for skip in ['reboot', 'rm -rf', 'pm clear']):
            continue
        # Test dry-run or syntax on device
        # For pm disable-user:
        if cmd.startswith("pm disable-user --user 0 "):
            pkg = cmd.split()[-1]
            # check if package exists
            res = subprocess.run(["adb", "-s", SERIAL, "shell", f"pm path {pkg}"], capture_output=True, text=True)
            if not res.stdout.strip():
                failed_apply.append((opt_id, item['title_it'], f"Package {pkg} non installato sul dispositivo"))
        elif cmd.startswith("settings put"):
            # settings put always succeeds
            pass

print(f"\nPackages not installed or unable to disable ({len(failed_apply)}):")
for opt_id, title_it, reason in failed_apply:
    print(f"  • {title_it} ({opt_id}): {reason}")
