#!/system/bin/sh
# Edge Gallery to HostAI Auto-Sync Daemon
SRC_DIR="/storage/emulated/0/Android/data/com.google.ai.edge.gallery/files"
DST_DIR="/storage/emulated/0/Download/models"
mkdir -p "$DST_DIR"

while true; do
    if [ -d "$SRC_DIR" ]; then
        for model in $(find "$SRC_DIR" -type f -name "*.litertlm" 2>/dev/null); do
            fname=$(basename "$model")
            dst_file="$DST_DIR/$fname"
            if [ ! -f "$dst_file" ] || [ "$model" -nt "$dst_file" ]; then
                echo "[edge_sync] Found new/updated model: $fname, copying..."
                cp "$model" "$dst_file"
                chmod 664 "$dst_file"
                echo "[edge_sync] Synchronized $fname"
            fi
        done
    fi
    sleep 10
done