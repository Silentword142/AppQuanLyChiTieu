#!/usr/bin/env bash
# Build APK không cần Android Studio/Gradle, chỉ dùng công cụ có trong apt của Ubuntu:
#   sudo apt-get install aapt apksigner zipalign android-sdk-platform-23 dalvik-exchange
# Chạy từ thư mục gốc repo: npm run build:apk   (bước build:web phải chạy trước)
set -euo pipefail
cd "$(dirname "$0")"

SDK_JAR="${ANDROID_JAR:-/usr/lib/android-sdk/platforms/android-23/android.jar}"
OUT=build
APK_NAME="${APK_NAME:-VinaSpends-Mai.apk}"
KEYSTORE="${KEYSTORE:-vinaspends.keystore}"
KS_PASS="${KS_PASS:-vinaspends}"

[ -f "$SDK_JAR" ] || { echo "Thiếu $SDK_JAR (apt-get install android-sdk-platform-23)"; exit 1; }
[ -f ../dist/www/index.html ] || { echo "Chưa có dist/www – chạy: npm run build:web"; exit 1; }

rm -rf "$OUT" && mkdir -p "$OUT"/{gen,classes,assets,dex}
cp -r ../dist/www "$OUT/assets/www"

# Số phiên bản tăng theo số commit để cài đè được bản cũ (giữ nguyên dữ liệu)
VC=$(git rev-list --count HEAD 2>/dev/null || echo 1)
sed "s/android:versionCode=\"1\"/android:versionCode=\"$VC\"/" AndroidManifest.xml > "$OUT/AndroidManifest.xml"
# LITE=1: bỏ dịch vụ đọc thông báo (Play Protect ở VN chặn cài APK ngoài có quyền này).
# Mai vẫn dùng được qua: dán thông báo, chia sẻ SMS vào app, quét ảnh chụp.
if [ "${LITE:-0}" = "1" ]; then
  python3 - "$OUT/AndroidManifest.xml" <<'PY'
import re,sys
p=sys.argv[1]; s=open(p).read()
s=re.sub(r'\s*<service[\s\S]*?</service>','',s)
open(p,'w').write(s)
PY
fi

echo "→ aapt: tài nguyên"
aapt package -f -m -J "$OUT/gen" -M "$OUT/AndroidManifest.xml" -S res -I "$SDK_JAR"

echo "→ javac"
javac -nowarn -source 8 -target 8 -encoding UTF-8 -bootclasspath "$SDK_JAR" -cp "$SDK_JAR" \
  -d "$OUT/classes" $(find src "$OUT/gen" -name '*.java') 2>&1 | grep -v "bootstrap classpath\|^1 warning\|options were not\|To suppress\|source value 8\|target value 8\|warning: \[options\]" || true
[ -f "$OUT/classes/com/vinaspends/mai/MainActivity.class" ] || { echo "javac lỗi"; exit 1; }

echo "→ dx: dex"
dalvik-exchange --dex --min-sdk-version=23 --output="$OUT/dex/classes.dex" "$OUT/classes"

echo "→ đóng gói"
aapt package -f -M "$OUT/AndroidManifest.xml" -S res -A "$OUT/assets" -I "$SDK_JAR" \
  -0 traineddata -0 wasm -0 woff2 -F "$OUT/unsigned.apk"
(cd "$OUT/dex" && aapt add ../unsigned.apk classes.dex >/dev/null)
zipalign -f -p 4 "$OUT/unsigned.apk" "$OUT/aligned.apk"

if [ ! -f "$KEYSTORE" ]; then
  echo "→ tạo khoá ký mới ($KEYSTORE) – hãy giữ file này để các bản sau cài đè được"
  keytool -genkeypair -keystore "$KEYSTORE" -storepass "$KS_PASS" -keypass "$KS_PASS" -alias vinaspends \
    -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=VinaSpends, O=VinaSpends, C=VN" >/dev/null 2>&1
fi
echo "→ ký APK"
apksigner sign --ks "$KEYSTORE" --ks-pass "pass:$KS_PASS" --ks-key-alias vinaspends \
  --min-sdk-version 23 --out "$OUT/$APK_NAME" "$OUT/aligned.apk"
apksigner verify "$OUT/$APK_NAME"
ls -lh "$OUT/$APK_NAME"
